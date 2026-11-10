package com.ecliptic.scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

data class TlsResult(
    val tlsVersion: String? = null,
    val cipherSuite: String? = null,
    val subject: String? = null,
    val issuer: String? = null,
    val validFrom: String? = null,
    val validTo: String? = null,
    val sans: List<String> = emptyList(),
    val error: String? = null,
)

data class DnsResult(
    val aRecords: List<String> = emptyList(),
    val cnameRecords: List<String> = emptyList(),
    val mxRecords: List<String> = emptyList(),
    val nsRecords: List<String> = emptyList(),
    val txtRecords: List<String> = emptyList(),
    val error: String? = null,
)

data class RdapResult(
    val registrar: String? = null,
    val registrant: String? = null,
    val created: String? = null,
    val expires: String? = null,
    val updated: String? = null,
    val status: List<String> = emptyList(),
    val nameServers: List<String> = emptyList(),
    val error: String? = null,
)

suspend fun fetchDnsRecords(host: String): DnsResult = withContext(Dispatchers.IO) {
    // DNS record type numbers
    val typesToFetch = listOf(1 to "A", 5 to "CNAME", 15 to "MX", 2 to "NS", 16 to "TXT")
    val records = mutableMapOf<Int, MutableList<String>>()

    for ((typeNum, _) in typesToFetch) {
        try {
            val encoded = URLEncoder.encode(host, "UTF-8")
            val req = Request.Builder()
                .url("https://dns.google/resolve?name=$encoded&type=$typeNum")
                .header("Accept", "application/dns-json")
                .build()
            httpClient.newCall(req).execute().use { response ->
                val body = response.body?.string() ?: return@use
                val answer = JSONObject(body).optJSONArray("Answer") ?: return@use
                for (i in 0 until answer.length()) {
                    val item = answer.getJSONObject(i)
                    val recType = item.optInt("type")
                    var data = item.optString("data").trim()
                    if (data.isBlank()) return@use
                    // Clean up trailing dot (DNS FQDN notation)
                    data = data.trimEnd('.')
                    // MX: "10 mail.example.com" → take hostname part
                    if (recType == 15) data = data.substringAfter(" ").trimEnd('.')
                    // TXT: strip surrounding quotes
                    if (recType == 16) data = data.removeSurrounding("\"")
                    records.getOrPut(recType) { mutableListOf() }.add(data)
                }
            }
        } catch (_: Exception) {}
    }

    DnsResult(
        aRecords      = records[1]?.distinct() ?: emptyList(),
        cnameRecords  = records[5]?.distinct() ?: emptyList(),
        mxRecords     = records[15]?.distinct() ?: emptyList(),
        nsRecords     = records[2]?.distinct() ?: emptyList(),
        txtRecords    = records[16]?.distinct() ?: emptyList(),
    )
}

private val RDAP_ENDPOINTS = listOf(
    "https://www.rdap.net/domain/",
    "https://rdap.iana.org/domain/",
)

private val USER_AGENT = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
// Fallback for WAFs (e.g. Cloudflare Bot Fight Mode) that block browser-looking UAs.
private const val FALLBACK_UA = "Ecliptic/1.0 (+https://ryand202.sg-host.com/ecliptic/)"

suspend fun fetchRdapInfo(domain: String): RdapResult = withContext(Dispatchers.IO) {
    val encoded = URLEncoder.encode(domain, "UTF-8")
    var lastError = "All endpoints failed"
    for (endpoint in RDAP_ENDPOINTS) {
        try {
            val req = Request.Builder()
                .url("$endpoint$encoded")
                .header("Accept", "application/rdap+json")
                .header("User-Agent", USER_AGENT)
                .build()
            httpClient.newCall(req).execute().use { response ->
                if (!response.isSuccessful) {
                    lastError = "HTTP ${response.code}"
                    return@use
                }
                val body = response.body?.string() ?: run { lastError = "Empty response"; return@use }
                val obj = JSONObject(body)

                val nsArr = obj.optJSONArray("nameservers") ?: JSONArray()
                val nameServers = (0 until nsArr.length()).mapNotNull {
                    nsArr.getJSONObject(it).optString("ldhName").takeIf { s -> s.isNotBlank() }?.lowercase()
                }

                val events = obj.optJSONArray("events") ?: JSONArray()
                var created: String? = null
                var expires: String? = null
                var updated: String? = null
                for (i in 0 until events.length()) {
                    val ev = events.getJSONObject(i)
                    val action = ev.optString("eventAction").lowercase()
                    val date = ev.optString("eventDate").take(10)
                    when {
                        action.contains("registr") && !action.contains("last") -> created = date
                        action.contains("expir") -> expires = date
                        action.contains("last") -> updated = date
                    }
                }

                val entities = obj.optJSONArray("entities") ?: JSONArray()
                var registrar: String? = null
                var registrant: String? = null
                for (i in 0 until entities.length()) {
                    val entity = entities.getJSONObject(i)
                    val roles = entity.optJSONArray("roles") ?: continue
                    val roleList = (0 until roles.length()).map { roles.getString(it).lowercase() }
                    val name = extractEntityName(entity)
                    if ("registrar" in roleList && registrar == null) registrar = name
                    if ("registrant" in roleList && registrant == null) registrant = name
                }

                val statusArr = obj.optJSONArray("status") ?: JSONArray()
                val status = (0 until statusArr.length()).map { statusArr.getString(it) }

                return@withContext RdapResult(
                    registrar = registrar,
                    registrant = registrant,
                    created = created,
                    expires = expires,
                    updated = updated,
                    status = status,
                    nameServers = nameServers,
                )
            }
        } catch (e: Exception) {
            lastError = e.message?.take(80) ?: "Error"
        }
    }
    RdapResult(error = lastError)
}

data class ReconFiles(
    val robotsTxt: String? = null,
    val securityTxt: String? = null,
    val sitemapXml: String? = null,
)

suspend fun fetchReconFiles(baseUrl: String): ReconFiles = withContext(Dispatchers.IO) {
    val base = try {
        val u = java.net.URI(baseUrl)
        "${u.scheme}://${u.host}${if (u.port > 0) ":${u.port}" else ""}"
    } catch (_: Exception) { baseUrl.trimEnd('/') }

    // Accepts an absolute URL or a path relative to the origin. Tries the browser UA
    // first, then a plain UA as a fallback for WAFs that block one or the other.
    fun attempt(url: String, ua: String): String? = try {
        val req = Request.Builder().url(url).header("User-Agent", ua).build()
        httpClient.newCall(req).execute().use { resp ->
            if (resp.isSuccessful) resp.body?.string()?.take(50_000) else null
        }
    } catch (_: Exception) { null }

    fun fetch(pathOrUrl: String): String? {
        val url = if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) pathOrUrl else "$base$pathOrUrl"
        return attempt(url, USER_AGENT) ?: attempt(url, FALLBACK_UA)
    }

    val robots = fetch("/robots.txt")

    // Discover the sitemap: prefer robots.txt "Sitemap:" lines, then fall back to the
    // common paths. Sites vary — WordPress core uses /wp-sitemap.xml, SEO plugins use
    // /sitemap_index.xml or /sitemap.xml, and some use /sitemaps.xml (plural).
    val sitemapCandidates = buildList {
        robots?.let { r ->
            Regex("""(?im)^\s*sitemap:\s*(\S+)\s*$""").findAll(r).forEach { add(it.groupValues[1].trim()) }
        }
        add("/sitemap.xml")
        add("/sitemap_index.xml")
        add("/sitemaps.xml")
        add("/wp-sitemap.xml")
    }
    val sitemap = sitemapCandidates.firstNotNullOfOrNull { candidate ->
        fetch(candidate)?.takeIf { it.contains("<", ignoreCase = true) }
    }

    ReconFiles(
        robotsTxt  = robots,
        securityTxt = fetch("/.well-known/security.txt") ?: fetch("/security.txt"),
        sitemapXml  = sitemap,
    )
}

data class EmailSecurity(
    val spf: String? = null,
    val dmarc: String? = null,
    val dkimSelector: String? = null,   // first selector found, if any
    val dnssec: Boolean? = null,        // null = unknown
    val caa: List<String> = emptyList(),
)

data class HostReputation(
    val source: String = "URLhaus",
    val listed: Boolean = false,
    val status: String? = null,   // "clean" | "listed" | other query_status | null
    val detail: String? = null,
    val error: String? = null,
)

data class SimilarDomain(val domain: String, val registered: Boolean)

/**
 * Aggregated recon result for one scan. Produced once by [fetchReconData] and cached
 * in the ViewModel + persisted per snapshot, so reopening the Recon/Network tab or a
 * past scan never re-hits the network, and the report can include everything.
 */
data class ReconData(
    val dns: DnsResult? = null,
    val rdap: RdapResult? = null,
    val files: ReconFiles? = null,
    val asn: String? = null,
    val subdomains: List<String> = emptyList(),
    val ipNeighbors: List<String> = emptyList(),
    val reputation: HostReputation? = null,
    val similarDomains: List<SimilarDomain> = emptyList(),
    val email: EmailSecurity? = null,
    val httpsRedirect: Boolean? = null,   // does http:// redirect to https://? null = unknown
    val fetchedAt: Long = 0L,
) {
    val isEmpty: Boolean get() = dns == null && rdap == null && files == null && asn == null &&
        subdomains.isEmpty() && ipNeighbors.isEmpty() && reputation == null && similarDomains.isEmpty()

    /** True only when the reverse-IP lookup actually returned something. */
    val ipChecked: Boolean get() = ipNeighbors.isNotEmpty()

    /** Exactly one site on the IP → dedicated. Only meaningful when [ipChecked]. */
    val dedicatedIp: Boolean get() = ipNeighbors.size == 1
}

/**
 * Runs the full recon suite in parallel, once: DNS, RDAP/WHOIS, recon files, ASN,
 * subdomains (crt.sh), host reputation (URLhaus), typosquat/similar domains, and —
 * once the A record is known — reverse-IP neighbors. Every lookup is best-effort:
 * one failure never blocks the rest.
 */
suspend fun fetchReconData(url: String, host: String?): ReconData = coroutineScope {
    val filesD = async { runCatching { fetchReconFiles(url) }.getOrNull() }
    val rdapD  = host?.let { h -> async { runCatching { fetchRdapInfo(h) }.getOrNull() } }
    val dnsD   = host?.let { h -> async { runCatching { fetchDnsRecords(h) }.getOrNull() } }
    val subsD  = host?.let { h -> async { runCatching { fetchSubdomains(h) }.getOrDefault(emptyList()) } }
    val repD   = host?.let { h -> async { runCatching { fetchHostReputation(h) }.getOrNull() } }
    val simD   = host?.let { h -> async { runCatching { fetchSimilarDomains(h) }.getOrDefault(emptyList()) } }
    val emailD = host?.let { h -> async { runCatching { fetchEmailSecurity(h) }.getOrNull() } }
    val redirD = host?.let { h -> async { runCatching { fetchHttpRedirect(h) }.getOrNull() } }

    val dns = dnsD?.await()
    val ip  = dns?.aRecords?.firstOrNull()
    val asn = ip?.let { runCatching { fetchAsn(it) }.getOrNull() }
    val neighbors = ip?.let { runCatching { fetchIpNeighbors(it) }.getOrDefault(emptyList()) } ?: emptyList()

    ReconData(
        dns = dns,
        rdap = rdapD?.await(),
        files = filesD.await(),
        asn = asn,
        subdomains = subsD?.await() ?: emptyList(),
        ipNeighbors = neighbors,
        reputation = repD?.await(),
        similarDomains = simD?.await() ?: emptyList(),
        email = emailD?.await(),
        httpsRedirect = redirD?.await(),
        fetchedAt = System.currentTimeMillis(),
    )
}

/** Generic DNS query via dns.google; returns the raw "data" strings for a record type. */
private suspend fun queryDns(name: String, type: Int): List<String> = withContext(Dispatchers.IO) {
    try {
        val enc = URLEncoder.encode(name, "UTF-8")
        val req = Request.Builder()
            .url("https://dns.google/resolve?name=$enc&type=$type")
            .header("Accept", "application/dns-json")
            .build()
        httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: return@withContext emptyList()
            val ans = JSONObject(body).optJSONArray("Answer") ?: return@withContext emptyList()
            (0 until ans.length()).mapNotNull { i ->
                ans.optJSONObject(i)?.optString("data")?.trim()?.removeSurrounding("\"")?.ifBlank { null }
            }
        }
    } catch (_: Exception) { emptyList() }
}

/** SPF, DMARC, DKIM (common selectors), DNSSEC and CAA for a domain. */
private val DKIM_SELECTORS = listOf("default", "google", "selector1", "selector2", "s1", "s2", "k1", "dkim", "mail", "smtp", "mandrill", "mailjet")

suspend fun fetchEmailSecurity(domain: String): EmailSecurity = coroutineScope {
    val spfD   = async { queryDns(domain, 16).firstOrNull { it.startsWith("v=spf1", ignoreCase = true) } }
    val dmarcD = async { queryDns("_dmarc.$domain", 16).firstOrNull { it.contains("v=DMARC1", ignoreCase = true) } }
    val caaD   = async { queryDns(domain, 257) }
    // DNSKEY present → DNSSEC signed
    val dnskeyD = async { queryDns(domain, 48).isNotEmpty() }
    val dkimD  = async {
        DKIM_SELECTORS.firstOrNull { sel ->
            queryDns("$sel._domainkey.$domain", 16).any { it.contains("DKIM1", true) || it.contains("k=rsa", true) || it.contains("p=", true) }
        }
    }
    EmailSecurity(
        spf = spfD.await(),
        dmarc = dmarcD.await(),
        dkimSelector = dkimD.await(),
        dnssec = dnskeyD.await(),
        caa = caaD.await().take(10),
    )
}

/** Does http://host redirect to https? null if the probe fails. */
suspend fun fetchHttpRedirect(host: String): Boolean? = withContext(Dispatchers.IO) {
    try {
        val req = Request.Builder().url("http://$host/").header("User-Agent", USER_AGENT).build()
        httpClient.newCall(req).execute().use { resp ->
            // OkHttp follows redirects by default; the final request URL scheme tells us.
            resp.request.url.scheme.equals("https", ignoreCase = true)
        }
    } catch (_: Exception) { null }
}

/** Subdomains via crt.sh certificate transparency logs. */
suspend fun fetchSubdomains(domain: String): List<String> = withContext(Dispatchers.IO) {
    val encoded = URLEncoder.encode("%.$domain", "UTF-8")
    try {
        val req = Request.Builder()
            .url("https://crt.sh/?q=$encoded&output=json")
            .header("User-Agent", USER_AGENT)
            .build()
        httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext emptyList()
            val body = resp.body?.string() ?: return@withContext emptyList()
            val arr = runCatching { JSONArray(body) }.getOrNull() ?: return@withContext emptyList()
            val set = sortedSetOf<String>()
            for (i in 0 until arr.length()) {
                val nv = arr.optJSONObject(i)?.optString("name_value") ?: continue
                for (raw in nv.split("\n")) {
                    val name = raw.trim().lowercase().removePrefix("*.")
                    if (name.endsWith(domain) && name != domain && !name.contains(' ') && !name.contains('@')) set += name
                }
            }
            set.toList().take(200)
        }
    } catch (_: Exception) { emptyList() }
}

/** Reverse-IP neighbors (other domains sharing the IP) via HackerTarget. */
suspend fun fetchIpNeighbors(ip: String): List<String> = withContext(Dispatchers.IO) {
    try {
        val req = Request.Builder()
            .url("https://api.hackertarget.com/reverseiplookup/?q=$ip")
            .header("User-Agent", USER_AGENT)
            .build()
        httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string()?.trim() ?: return@withContext emptyList()
            if (body.isBlank() || body.contains("API count exceeded") ||
                body.startsWith("error", true) || body.contains("no records", true)) return@withContext emptyList()
            body.lines().map { it.trim().lowercase() }
                .filter { it.isNotBlank() && !it.contains(' ') && it.contains('.') }
                .distinct().take(100)
        }
    } catch (_: Exception) { emptyList() }
}

/** Host reputation via abuse.ch URLhaus (best-effort; no key = graceful "unavailable"). */
suspend fun fetchHostReputation(host: String): HostReputation = withContext(Dispatchers.IO) {
    try {
        val form = FormBody.Builder().add("host", host).build()
        val req = Request.Builder().url("https://urlhaus-api.abuse.ch/v1/host/").post(form).build()
        httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: return@withContext HostReputation(error = "No response")
            val obj = runCatching { JSONObject(body) }.getOrNull()
                ?: return@withContext HostReputation(error = "Unavailable")
            when (val qs = obj.optString("query_status")) {
                "no_results" -> HostReputation(listed = false, status = "clean")
                "ok" -> {
                    val count = obj.optJSONArray("urls")?.length() ?: 0
                    HostReputation(listed = true, status = "listed", detail = "$count malicious URL(s) on record")
                }
                else -> HostReputation(status = qs.ifBlank { null }, error = if (qs.isBlank()) "Unavailable" else null)
            }
        }
    } catch (e: Exception) {
        HostReputation(error = e.message?.take(80) ?: "Error")
    }
}

// Typosquat / look-alike domain generation + registration check.
private val SIMILAR_TLDS = listOf("com", "net", "org", "co", "io", "info", "app", "online", "site", "xyz", "shop", "store")

private fun generateSimilarDomains(domain: String): List<String> {
    val dot = domain.lastIndexOf('.')
    if (dot <= 0) return emptyList()
    val base = domain.substring(0, dot)
    val tld  = domain.substring(dot + 1)
    if (base.length < 2) return emptyList()
    val out = LinkedHashSet<String>()
    // Alternate TLDs
    SIMILAR_TLDS.filter { it != tld }.forEach { out += "$base.$it" }
    // Character omission
    for (i in base.indices) out += base.removeRange(i, i + 1) + "." + tld
    // Adjacent transposition
    for (i in 0 until base.length - 1) {
        val c = base.toCharArray(); val t = c[i]; c[i] = c[i + 1]; c[i + 1] = t
        out += String(c) + "." + tld
    }
    // Character doubling
    for (i in base.indices) out += base.substring(0, i + 1) + base[i] + base.substring(i + 1) + "." + tld
    // Common homoglyph swaps
    val homo = mapOf('o' to '0', 'l' to '1', 'i' to '1', 'e' to '3', 'a' to '4', 's' to '5')
    for (i in base.indices) homo[base[i]]?.let { sub ->
        out += base.substring(0, i) + sub + base.substring(i + 1) + "." + tld
    }
    return out.filter { it != domain && it.length in 4..60 }.take(30)
}

/** Generates look-alike domains and returns the ones that actually resolve (registered). */
suspend fun fetchSimilarDomains(domain: String): List<SimilarDomain> = coroutineScope {
    val candidates = generateSimilarDomains(domain)
    candidates.map { cand ->
        async(Dispatchers.IO) {
            val registered = runCatching {
                val enc = URLEncoder.encode(cand, "UTF-8")
                val req = Request.Builder()
                    .url("https://dns.google/resolve?name=$enc&type=1")
                    .header("Accept", "application/dns-json")
                    .build()
                httpClient.newCall(req).execute().use { r ->
                    val b = r.body?.string() ?: return@use false
                    val o = JSONObject(b)
                    o.optInt("Status") == 0 && (o.optJSONArray("Answer")?.length() ?: 0) > 0
                }
            }.getOrDefault(false)
            SimilarDomain(cand, registered)
        }
    }.map { it.await() }.filter { it.registered }
}

suspend fun fetchAsn(ip: String): String? = withContext(Dispatchers.IO) {
    try {
        val req = Request.Builder()
            .url("https://ipinfo.io/$ip/org")
            .header("Accept", "text/plain")
            .header("User-Agent", USER_AGENT)
            .build()
        httpClient.newCall(req).execute().use { resp ->
            if (resp.isSuccessful) resp.body?.string()?.trim()?.take(100) else null
        }
    } catch (_: Exception) { null }
}

private fun extractEntityName(entity: JSONObject): String? {
    val vcard = entity.optJSONArray("vcardArray") ?: return null
    if (vcard.length() < 2) return null
    val props = vcard.optJSONArray(1) ?: return null
    for (i in 0 until props.length()) {
        val prop = props.optJSONArray(i) ?: continue
        val name = prop.optString(0).lowercase()
        if (name == "fn" || name == "org") {
            val value = prop.optString(3).trim()
            if (value.isNotBlank()) return value
        }
    }
    return null
}
