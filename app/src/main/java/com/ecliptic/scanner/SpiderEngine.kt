package com.ecliptic.scanner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.net.URI

enum class SpiderStatus { IDLE, RUNNING, PAUSED, DONE, STOPPED }

data class SpiderState(
    val status: SpiderStatus = SpiderStatus.IDLE,
    val discovered: List<String> = emptyList(),
    val visited: Int = 0,
    val queued: Int = 0,
    val currentUrl: String = "",
    val fromSitemap: Boolean = false,
    val error: String? = null,
) {
    val isActive: Boolean get() = status == SpiderStatus.RUNNING || status == SpiderStatus.PAUSED
}

/**
 * Conservative, polite, same-site web crawler that builds a list of page URLs.
 *
 * Robustness notes (learned the hard way):
 *  - The canonical host is taken from the *final* URL after redirects, not the URL
 *    the user typed, so a non-www ⇄ www (or http→https) redirect doesn't cause every
 *    link to be discarded as "off-site".
 *  - Host matching ignores a leading "www." so both forms count as the same site.
 *  - Sitemaps are discovered from robots.txt and the common WordPress/SEO paths
 *    (/sitemap.xml, /sitemap_index.xml, /wp-sitemap.xml). Sitemap *indexes* (whose
 *    <loc> entries point to child .xml sitemaps) are followed one level to reach the
 *    actual page URLs.
 *
 * Runs sequentially with a small delay between requests to keep the footprint light.
 * Supports pause / resume / stop.
 */
class SpiderEngine(private val scope: CoroutineScope) {

    companion object {
        private const val MAX_PAGES     = 75      // conservative page cap
        private const val MAX_DEPTH     = 2       // link-follow depth
        private const val DELAY_MS      = 250L    // polite delay between fetches
        private const val MAX_BYTES     = 500_000 // per-page HTML cap
        private const val MAX_SITEMAPS  = 8       // cap sitemap files fetched
        private const val UA = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    }

    private val _state = MutableStateFlow(SpiderState())
    val state: StateFlow<SpiderState> = _state.asStateFlow()

    private var job: Job? = null

    @Volatile private var paused = false

    fun start(rawUrl: String) {
        if (job?.isActive == true) return
        val seed = normalizeSeed(rawUrl) ?: run {
            _state.value = SpiderState(status = SpiderStatus.DONE, error = "Invalid URL")
            return
        }
        paused = false
        _state.value = SpiderState(status = SpiderStatus.RUNNING, currentUrl = seed)
        job = scope.launch { crawl(seed) }
    }

    fun pause()  { if (_state.value.status == SpiderStatus.RUNNING) { paused = true; _state.update { it.copy(status = SpiderStatus.PAUSED) } } }
    fun resume() { if (_state.value.status == SpiderStatus.PAUSED)  { paused = false; _state.update { it.copy(status = SpiderStatus.RUNNING) } } }

    fun stop() {
        job?.cancel()
        job = null
        paused = false
        _state.update { it.copy(status = SpiderStatus.STOPPED, currentUrl = "") }
    }

    fun reset() {
        job?.cancel()
        job = null
        paused = false
        _state.value = SpiderState()
    }

    private suspend fun crawl(seed: String) {
        val discovered = LinkedHashSet<String>()
        val queue = ArrayDeque<Pair<String, Int>>()  // url, depth

        // Fetch the seed first so we can lock onto the canonical host after redirects.
        _state.update { it.copy(currentUrl = seed) }
        val rootPage = fetchPage(seed)
        val canonical = rootPage?.first ?: seed
        val hostKey = hostKeyOf(canonical) ?: run {
            _state.value = SpiderState(status = SpiderStatus.DONE, error = "Invalid host")
            return
        }
        var visited = if (rootPage != null) 1 else 0

        discovered += canonical
        rootPage?.second?.let { html ->
            for (link in extractLinks(html, canonical, hostKey)) {
                if (discovered.size >= MAX_PAGES) break
                if (discovered.add(link)) queue += link to 1
            }
        }

        // Seed from sitemaps (robots.txt + common paths, following sitemap indexes).
        val sitemapPages = collectSitemapPageUrls(canonical, hostKey)
        if (sitemapPages.isNotEmpty()) {
            _state.update { it.copy(fromSitemap = true) }
            for (u in sitemapPages) {
                if (discovered.size >= MAX_PAGES) break
                if (discovered.add(u)) queue += u to 1
            }
        }
        publish(discovered, visited, queue.size, canonical)

        while (queue.isNotEmpty() && scope.isActive) {
            while (paused && scope.isActive) delay(150)
            if (!scope.isActive) return
            if (discovered.size >= MAX_PAGES && queue.all { it.second >= MAX_DEPTH }) break

            val (url, depth) = queue.removeFirst()
            _state.update { it.copy(currentUrl = url, queued = queue.size) }

            // Only fetch pages we still want to expand; past max depth we keep the URL
            // (already discovered) but don't crawl it.
            if (depth < MAX_DEPTH && discovered.size < MAX_PAGES) {
                val page = fetchPage(url)
                visited++
                page?.second?.let { html ->
                    for (link in extractLinks(html, page.first, hostKey)) {
                        if (discovered.size >= MAX_PAGES) break
                        if (discovered.add(link)) queue += link to (depth + 1)
                    }
                }
                publish(discovered, visited, queue.size, url)
                delay(DELAY_MS)
            }
        }

        if (scope.isActive) {
            _state.update {
                it.copy(
                    status = SpiderStatus.DONE,
                    currentUrl = "",
                    visited = visited,
                    queued = 0,
                    discovered = discovered.toList(),
                )
            }
        }
    }

    private fun publish(discovered: Set<String>, visited: Int, queued: Int, current: String) {
        _state.update {
            if (it.status == SpiderStatus.STOPPED) return@update it
            it.copy(discovered = discovered.toList(), visited = visited, queued = queued, currentUrl = current)
        }
    }

    /** Fetch a URL, returning (finalUrlAfterRedirects, htmlBody) or null. */
    private suspend fun fetchPage(url: String): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(url).header("User-Agent", UA).build()
            httpClient.newCall(req).execute().use { resp ->
                val finalUrl = resp.request.url.toString()
                if (!resp.isSuccessful) return@use null
                val ct = resp.header("Content-Type") ?: ""
                if (ct.isNotBlank() && !ct.contains("html", ignoreCase = true)) return@use null
                val body = resp.body?.string()?.take(MAX_BYTES) ?: return@use null
                finalUrl to body
            }
        } catch (_: Exception) { null }
    }

    /** Fetch any text resource (e.g. XML sitemap, robots.txt). */
    private suspend fun fetchText(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(url).header("User-Agent", UA).build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                resp.body?.string()?.take(2_000_000)
            }
        } catch (_: Exception) { null }
    }

    private suspend fun collectSitemapPageUrls(base: String, hostKey: String): List<String> {
        val origin = originOf(base) ?: return emptyList()
        val locRe = Regex("""<loc>\s*([^<\s]+)\s*</loc>""", RegexOption.IGNORE_CASE)

        // 1) Candidate sitemap URLs: robots.txt "Sitemap:" lines + common paths.
        val candidates = LinkedHashSet<String>()
        fetchText("$origin/robots.txt")?.let { robots ->
            for (m in Regex("""(?im)^\s*sitemap:\s*(\S+)\s*$""").findAll(robots)) {
                candidates += m.groupValues[1].trim()
            }
        }
        candidates += "$origin/sitemap_index.xml"
        candidates += "$origin/wp-sitemap.xml"
        candidates += "$origin/sitemap.xml"
        candidates += "$origin/sitemaps.xml"

        val pages = LinkedHashSet<String>()
        var fetched = 0
        val toProcess = ArrayDeque(candidates.toList())
        val seenSitemaps = HashSet<String>()

        while (toProcess.isNotEmpty() && fetched < MAX_SITEMAPS && pages.size < MAX_PAGES && scope.isActive) {
            val sm = toProcess.removeFirst()
            if (!seenSitemaps.add(sm) || !sameHost(sm, hostKey)) continue
            val xml = fetchText(sm) ?: continue
            fetched++
            for (m in locRe.findAll(xml)) {
                val loc = m.groupValues[1].trim()
                if (!sameHost(loc, hostKey)) continue
                if (loc.substringBefore('?').endsWith(".xml", ignoreCase = true)) {
                    // Nested sitemap — follow it (one level, bounded by MAX_SITEMAPS).
                    if (loc !in seenSitemaps) toProcess += loc
                } else {
                    pages += normalizeUrl(loc) ?: continue
                    if (pages.size >= MAX_PAGES) break
                }
            }
        }
        return pages.toList()
    }

    private fun extractLinks(html: String, baseUrl: String, hostKey: String): List<String> {
        val base = try { URI(baseUrl) } catch (_: Exception) { return emptyList() }
        val out = LinkedHashSet<String>()
        for (m in Regex("""<a\b[^>]*?\bhref\s*=\s*["']([^"'>]+)["']""", RegexOption.IGNORE_CASE).findAll(html)) {
            val raw = m.groupValues[1].trim()
            if (raw.isBlank() || raw.startsWith("#") ||
                raw.startsWith("mailto:") || raw.startsWith("tel:") ||
                raw.startsWith("javascript:") || raw.startsWith("data:")) continue
            val resolved = try { base.resolve(raw) } catch (_: Exception) { continue }
            val scheme = resolved.scheme ?: continue
            if (scheme != "http" && scheme != "https") continue
            if (hostKeyOf(resolved.toString()) != hostKey) continue
            normalizeUrl(resolved.toString())?.let { out += it }
        }
        return out.toList()
    }

    /** Strip fragment, keep query. Returns null on parse failure. */
    private fun normalizeUrl(url: String): String? = try {
        val u = URI(url)
        buildString {
            append(u.scheme).append("://").append(u.host)
            if (u.port > 0) append(":").append(u.port)
            append(if (u.rawPath.isNullOrBlank()) "/" else u.rawPath)
            u.rawQuery?.let { append("?").append(it) }
        }
    } catch (_: Exception) { null }

    private fun sameHost(url: String, hostKey: String): Boolean = hostKeyOf(url) == hostKey

    /** Lowercased host with a leading "www." removed, or null. */
    private fun hostKeyOf(url: String): String? =
        try { URI(url).host?.lowercase()?.removePrefix("www.") } catch (_: Exception) { null }

    private fun originOf(url: String): String? = try {
        val u = URI(url)
        if (u.scheme == null || u.host == null) null
        else "${u.scheme}://${u.host}${if (u.port > 0) ":${u.port}" else ""}"
    } catch (_: Exception) { null }

    private fun normalizeSeed(raw: String): String? {
        val t = raw.trim()
        if (t.isBlank()) return null
        val withScheme = if (t.startsWith("http://") || t.startsWith("https://")) t else "https://$t"
        return try { URI(withScheme); withScheme } catch (_: Exception) { null }
    }
}
