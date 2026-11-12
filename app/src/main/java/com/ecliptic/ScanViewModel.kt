package com.ecliptic

import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptic.fingerprint.FingerprintEngine
import com.ecliptic.fingerprint.TechDatabase
import com.ecliptic.fingerprint.Technology
import com.ecliptic.scanner.ConsoleMessage
import com.ecliptic.scanner.NetworkRequest
import com.ecliptic.scanner.RawSignals
import com.ecliptic.scanner.EnumTier
import com.ecliptic.scanner.ReconData
import com.ecliptic.scanner.RepeaterResponse
import com.ecliptic.scanner.enumStaging
import com.ecliptic.scanner.fetchReconData
import com.ecliptic.scanner.fetchSubdomains
import com.ecliptic.scanner.parseSubdomains
import com.ecliptic.scanner.scanPorts
import com.ecliptic.scanner.SpiderEngine
import com.ecliptic.scanner.SpiderState
import com.ecliptic.scanner.scanUrl
import com.ecliptic.scanner.sendRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanSnapshot(
    val timestamp: Long = System.currentTimeMillis(),
    val url: String,
    val signals: RawSignals,
    val techs: List<Technology>,
    val networkRequests: List<NetworkRequest>,
    val consoleMessages: List<ConsoleMessage>,
    val rawRequest: String,
    val wizardUrl: String,
    val wizardHeaders: List<Pair<String, String>>,
    val reconData: ReconData? = null,
)

data class ScanUiState(
    val urlInput: String = "https://wordpress.org",
    val isScanning: Boolean = false,
    val hasScanned: Boolean = false,
    val signals: RawSignals? = null,
    val techs: List<Technology> = emptyList(),
    val httpMethod: String = "GET",
    val customHeaders: List<Pair<String, String>> = emptyList(),
    val requestBody: String = "",
    val rawRequest: String = "",
    val isRepeaterWizard: Boolean = false,
    val scanStage: String = "",
    val repeaterResponse: RepeaterResponse? = null,
    val isSendingRequest: Boolean = false,
    // Wizard state — persists across tab switches
    val wizardMethod: String = "GET",
    val wizardUrl: String = "",
    val wizardHeaders: List<Pair<String, String>> = emptyList(),
    val wizardBody: String = "",
    val wizardSent: Boolean = false,
    // Live capture — continues after scan completes
    val liveNetworkRequests: List<NetworkRequest> = emptyList(),
    val consoleMessages: List<ConsoleMessage> = emptyList(),
    // Lifetime scan count — used to trigger in-app review
    val scanCount: Int = 0,
    // Scan history navigation
    val historySize: Int = 0,
    val historyPosition: Int = 0,
    // Recon (WHOIS / DNS / ASN / robots / security.txt / sitemap) — fetched once, cached
    val reconData: ReconData? = null,
    val reconLoading: Boolean = false,
    // Manual, on-demand enumeration run from the Recon tab
    val portsLoading: Boolean = false,
    val stagingLoading: Boolean = false,
    val subsLoading: Boolean = false,
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = getApplication<Application>()
    private val dbDeferred = viewModelScope.async { TechDatabase.get(getApplication()) }

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    // ── Spider (site crawler) ──────────────────────────────────────────────
    private val spider = SpiderEngine(viewModelScope)
    val spiderState: StateFlow<SpiderState> = spider.state
    fun startSpider(url: String) = spider.start(url)
    fun pauseSpider()  = spider.pause()
    fun resumeSpider() = spider.resume()
    fun stopSpider()   = spider.stop()
    fun resetSpider()  = spider.reset()

    private val snapshots = mutableListOf<ScanSnapshot>()
    private var currentSnapshotIdx = -1

    private val PAYMENT_CARD_BRANDS = setOf(
        "Visa", "Mastercard", "American Express", "Discover", "JCB",
        "Diners Club", "UnionPay", "Maestro", "Visa Checkout", "Masterpass",
        "American Express Checkout",
    )

    private fun groupPaymentCards(techs: List<Technology>): List<Technology> {
        val cards = techs.filter { it.name in PAYMENT_CARD_BRANDS }
        if (cards.size < 2) return techs
        val merged = Technology(
            name = "Major Credit Cards",
            categories = listOf("Payment Processors"),
            icon = null,
            website = null,
            version = cards.joinToString(", ") { it.name },
            confidence = cards.minOf { it.confidence },
            detectionMethod = "Grouped",
            matchedValue = cards.joinToString(", ") { it.name },
        )
        return techs.filter { it.name !in PAYMENT_CARD_BRANDS } + merged
    }

    fun navigateHistory(delta: Int) {
        val newIdx = (currentSnapshotIdx + delta).coerceIn(0, snapshots.lastIndex)
        if (newIdx == currentSnapshotIdx) return
        // Persist the current scan's live capture back into its snapshot before leaving,
        // so returning to it later restores its own data (not the next scan's).
        if (currentSnapshotIdx in snapshots.indices) {
            snapshots[currentSnapshotIdx] = snapshots[currentSnapshotIdx].copy(
                networkRequests = _state.value.liveNetworkRequests,
                consoleMessages = _state.value.consoleMessages,
            )
        }
        currentSnapshotIdx = newIdx
        // Spider state is live and specific to the scan it was run against; switching
        // to another scan must not carry it over.
        spider.reset()
        val snap = snapshots[newIdx]
        _state.update {
            it.copy(
                urlInput = snap.url,
                signals = snap.signals,
                techs = snap.techs,
                liveNetworkRequests = snap.networkRequests,
                consoleMessages = snap.consoleMessages,
                rawRequest = snap.rawRequest,
                wizardUrl = snap.wizardUrl,
                wizardHeaders = snap.wizardHeaders,
                hasScanned = true,
                historyPosition = newIdx + 1,
                // Recon is per-scan; restore this snapshot's cached recon (if any).
                reconData = snap.reconData,
                reconLoading = false,
            )
        }
    }

    /**
     * Fetch recon data (WHOIS/DNS/ASN/robots/security.txt/sitemap) once for the current
     * scan and cache it in state + the snapshot + disk. No-op if already loaded unless
     * [force] (the Recon tab's manual refresh button).
     */
    fun loadRecon(force: Boolean = false) {
        val s = _state.value
        if (s.reconLoading) return
        if (!force && s.reconData != null) return
        val sig = s.signals ?: return
        val url = sig.finalUrl.ifBlank { s.urlInput }.trim()
        if (url.isBlank()) return
        val host = runCatching { java.net.URI(url).host?.removePrefix("www.") }.getOrNull()
        _state.update { it.copy(reconLoading = true) }
        viewModelScope.launch {
            val fetched = fetchReconData(url, host)
            // Augment crt.sh subdomains with any found in the scanned page source itself
            // (links/attrs/inline JS), so e.g. dev.site.com in the HTML shows up automatically.
            val parsed = host?.let { parseSubdomains(it, sourceBlob(sig)) } ?: emptyList()
            val data = fetched.copy(subdomains = (fetched.subdomains + parsed).distinct().sorted())
            _state.update { it.copy(reconData = data, reconLoading = false) }
            // Persist into the current snapshot (so revisiting restores it) + disk.
            if (currentSnapshotIdx in snapshots.indices) {
                snapshots[currentSnapshotIdx] = snapshots[currentSnapshotIdx].copy(reconData = data)
                val saveHtml = AppPreferences.saveHtmlSourceFlow(appContext).first()
                ScanHistoryStore.save(appContext, snapshots[currentSnapshotIdx], saveHtml)
            }
        }
    }

    /** Concatenated page source (HTML + script/CSS URLs) for subdomain/text mining. */
    private fun sourceBlob(sig: com.ecliptic.scanner.RawSignals): String = buildString {
        append(sig.htmlSource)
        append('\n'); append(sig.scriptUrls.joinToString("\n"))
        append('\n'); append(sig.cssUrls.joinToString("\n"))
    }

    /**
     * Manual subdomain discovery (like the port/staging buttons): re-runs crt.sh
     * certificate-transparency enumeration and re-parses the page source, merging
     * everything into the cached subdomain list. Never clears existing finds.
     */
    fun findSubdomains() {
        if (_state.value.subsLoading) return
        val host = currentHost() ?: return
        val sig = _state.value.signals
        _state.update { it.copy(subsLoading = true) }
        viewModelScope.launch {
            val crt = runCatching { fetchSubdomains(host) }.getOrDefault(emptyList())
            val parsed = sig?.let { parseSubdomains(host, sourceBlob(it)) } ?: emptyList()
            updateReconAndPersist { r ->
                val merged = (r.subdomains + crt + parsed).map { it.lowercase() }.distinct().sorted()
                r.copy(subdomains = merged, subdomainsChecked = true)
            }
            _state.update { it.copy(subsLoading = false) }
        }
    }

    /** Host of the current scan, stripped of www. null if nothing is scanned. */
    private fun currentHost(): String? {
        val s = _state.value
        val raw = (s.signals?.finalUrl?.ifBlank { null } ?: s.urlInput).trim()
        if (raw.isBlank()) return null
        val withScheme = if (raw.startsWith("http")) raw else "https://$raw"
        return runCatching { java.net.URI(withScheme).host?.removePrefix("www.") }.getOrNull()
    }

    /** Merge a change into the cached recon (creating it if absent) and persist it. */
    private fun updateReconAndPersist(transform: (ReconData) -> ReconData) {
        val updated = transform(_state.value.reconData ?: ReconData())
        _state.update { it.copy(reconData = updated) }
        if (currentSnapshotIdx in snapshots.indices) {
            snapshots[currentSnapshotIdx] = snapshots[currentSnapshotIdx].copy(reconData = updated)
            viewModelScope.launch {
                val saveHtml = AppPreferences.saveHtmlSourceFlow(appContext).first()
                ScanHistoryStore.save(appContext, snapshots[currentSnapshotIdx], saveHtml)
            }
        }
    }

    /** Probe the project host for open alt ports (manual; authorized-use feature). */
    fun scanPorts() {
        if (_state.value.portsLoading) return
        val host = currentHost() ?: return
        _state.update { it.copy(portsLoading = true) }
        viewModelScope.launch {
            val ports = runCatching { scanPorts(host) }.getOrDefault(emptyList())
            updateReconAndPersist { it.copy(openPorts = ports, portsChecked = true) }
            _state.update { it.copy(portsLoading = false) }
        }
    }

    /**
     * Enumerate staging/pre-prod hosts at the chosen depth (manual). Incremental: if a
     * lower tier already ran, only the prefixes this tier adds are queried and the result
     * is merged into the existing list — already-discovered hosts are never cleared or
     * re-checked. Picking a tier already covered is a no-op.
     */
    fun enumStaging(tier: EnumTier) {
        if (_state.value.stagingLoading) return
        val host = currentHost() ?: return
        val recon = _state.value.reconData
        val prev = recon?.stagingTier?.let { runCatching { EnumTier.valueOf(it) }.getOrNull() }
        // Already enumerated at this depth or deeper → keep what we have, do nothing.
        if (prev != null && prev.ordinal >= tier.ordinal) return
        // Feed provider detection with whatever DNS/ASN signals we already have cached.
        val signals = buildList {
            recon?.dns?.cnameRecords?.let { addAll(it) }
            recon?.dns?.nsRecords?.let { addAll(it) }
            recon?.asn?.let { add(it) }
        }
        _state.update { it.copy(stagingLoading = true) }
        viewModelScope.launch {
            val found = runCatching { enumStaging(host, signals, tier, prev) }.getOrDefault(emptyList())
            updateReconAndPersist { r ->
                val merged = (r.staging + found).distinctBy { it.host }
                r.copy(staging = merged, stagingChecked = true, stagingTier = tier.name)
            }
            _state.update { it.copy(stagingLoading = false) }
        }
    }

    /** Permanently delete the scan currently in view, then show an adjacent one. */
    fun deleteCurrentScan() {
        if (currentSnapshotIdx !in snapshots.indices) return
        val removed = snapshots.removeAt(currentSnapshotIdx)
        viewModelScope.launch { ScanHistoryStore.delete(appContext, removed.timestamp) }
        spider.reset()
        if (snapshots.isEmpty()) {
            currentSnapshotIdx = -1
            _state.update {
                it.copy(
                    urlInput = "", hasScanned = false,
                    signals = null, techs = emptyList(),
                    liveNetworkRequests = emptyList(), consoleMessages = emptyList(),
                    rawRequest = "", wizardUrl = "", wizardHeaders = emptyList(),
                    reconData = null, reconLoading = false,
                    portsLoading = false, stagingLoading = false,
                    historySize = 0, historyPosition = 0,
                )
            }
            return
        }
        currentSnapshotIdx = currentSnapshotIdx.coerceAtMost(snapshots.lastIndex)
        val snap = snapshots[currentSnapshotIdx]
        _state.update {
            it.copy(
                urlInput = snap.url,
                signals = snap.signals,
                techs = snap.techs,
                liveNetworkRequests = snap.networkRequests,
                consoleMessages = snap.consoleMessages,
                rawRequest = snap.rawRequest,
                wizardUrl = snap.wizardUrl,
                wizardHeaders = snap.wizardHeaders,
                hasScanned = true,
                historySize = snapshots.size,
                historyPosition = currentSnapshotIdx + 1,
                reconData = snap.reconData,
                reconLoading = false,
                portsLoading = false, stagingLoading = false,
            )
        }
    }

    init {
        viewModelScope.launch {
            val method = AppPreferences.httpMethodFlow(appContext).first()
            val count  = AppPreferences.scanCountFlow(appContext).first()
            _state.update { it.copy(httpMethod = method, scanCount = count) }
        }
        viewModelScope.launch {
            val loaded = ScanHistoryStore.loadAll(appContext)
            if (loaded.isNotEmpty()) {
                snapshots.addAll(loaded)
                currentSnapshotIdx = snapshots.lastIndex
                val latest = snapshots.last()
                _state.update {
                    it.copy(
                        urlInput = latest.url,
                        signals = latest.signals,
                        techs = latest.techs,
                        liveNetworkRequests = latest.networkRequests,
                        consoleMessages = latest.consoleMessages,
                        rawRequest = latest.rawRequest,
                        wizardUrl = latest.wizardUrl,
                        wizardHeaders = latest.wizardHeaders,
                        hasScanned = true,
                        historySize = snapshots.size,
                        historyPosition = snapshots.size,
                        reconData = latest.reconData,
                    )
                }
            }
        }
    }

    fun onUrlChanged(url: String) {
        _state.update { it.copy(urlInput = url) }
    }

    fun onMethodChanged(method: String) {
        _state.update { it.copy(httpMethod = method) }
        viewModelScope.launch { AppPreferences.setHttpMethod(appContext, method) }
    }

    fun onAddHeader(key: String, value: String) {
        if (key.isBlank()) return
        _state.update { it.copy(customHeaders = it.customHeaders + (key.trim() to value.trim())) }
    }

    fun onRemoveHeader(index: Int) {
        _state.update { s ->
            val list = s.customHeaders.toMutableList()
            if (index in list.indices) list.removeAt(index)
            s.copy(customHeaders = list)
        }
    }

    fun onBodyChanged(body: String) {
        _state.update { it.copy(requestBody = body) }
    }

    fun onRawRequestChanged(text: String) {
        _state.update { it.copy(rawRequest = text) }
    }

    fun onToggleRepeaterMode() {
        _state.update { it.copy(isRepeaterWizard = !it.isRepeaterWizard) }
    }

    fun buildTemplateForUrl(urlInput: String, method: String): String {
        val normalized = if (urlInput.startsWith("http")) urlInput else "https://$urlInput"
        return buildString {
            try {
                val url = java.net.URL(normalized)
                val path = url.path.ifBlank { "/" } + (url.query?.let { "?$it" } ?: "")
                appendLine("$method $path HTTP/1.1")
                appendLine("Host: ${url.host}")
                appendLine("User-Agent: Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                appendLine("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                appendLine("Accept-Language: en-US,en;q=0.9")
                appendLine("Accept-Encoding: gzip, deflate, br")
                append("Connection: keep-alive")
            } catch (e: Exception) {
                appendLine("$method / HTTP/1.1")
                appendLine("Host: $urlInput")
                appendLine("User-Agent: Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                append("Accept: text/html,application/xhtml+xml,*/*;q=0.8")
            }
        }
    }

    private fun buildRawRequest(signals: RawSignals, method: String): String {
        return buildString {
            val url = try { java.net.URL(signals.finalUrl) } catch (e: Exception) { null }
            val path = url?.let {
                it.path.ifBlank { "/" } + (it.query?.let { q -> "?$q" } ?: "")
            } ?: "/"
            val host = url?.host ?: signals.finalUrl
                .removePrefix("https://").removePrefix("http://").split("/").first()

            appendLine("$method $path HTTP/1.1")
            appendLine("Host: $host")
            signals.requestHeaders.filterKeys { it.lowercase() != "host" }.forEach { (k, v) ->
                appendLine("$k: $v")
            }
            // blank line separating headers from body
            append("")
        }
    }

    private data class ParsedRequest(
        val method: String,
        val url: String,
        val headers: Map<String, String>,
        val body: String,
    )

    private fun parseRawRequest(rawText: String, fallbackUrl: String = ""): ParsedRequest {
        val lines = rawText.replace("\r\n", "\n").lines()
        if (lines.isEmpty()) return ParsedRequest("GET", fallbackUrl, emptyMap(), "")

        val firstParts = lines[0].trim().split(" ")
        val method = firstParts.getOrNull(0)?.uppercase()?.ifBlank { "GET" } ?: "GET"
        val pathOrUrl = firstParts.getOrNull(1) ?: "/"

        val headers = mutableMapOf<String, String>()
        var bodyStart = lines.size
        for (i in 1 until lines.size) {
            if (lines[i].isBlank()) { bodyStart = i + 1; break }
            val colon = lines[i].indexOf(':')
            if (colon > 0) headers[lines[i].substring(0, colon).trim()] = lines[i].substring(colon + 1).trim()
        }

        val body = if (bodyStart < lines.size) lines.subList(bodyStart, lines.size).joinToString("\n") else ""

        val host = headers.entries.firstOrNull { it.key.lowercase() == "host" }?.value ?: ""
        val url = when {
            pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://") -> pathOrUrl
            host.isNotBlank() -> "https://$host$pathOrUrl"
            fallbackUrl.isNotBlank() -> {
                val base = if (fallbackUrl.startsWith("http")) fallbackUrl else "https://$fallbackUrl"
                runCatching {
                    val u = java.net.URL(base)
                    "${u.protocol}://${u.host}$pathOrUrl"
                }.getOrDefault(base)
            }
            else -> pathOrUrl
        }

        return ParsedRequest(method, url, headers, body)
    }

    private fun extractWpPlugins(htmlSource: String, assetUrls: List<String>): List<Technology> {
        val pluginRegex = Regex("/wp-content/plugins/([a-zA-Z0-9_-]+)/")
        val seen = mutableSetOf<String>()
        val results = mutableListOf<Technology>()
        val targets = assetUrls + listOf(htmlSource.take(200_000))
        for (target in targets) {
            for (match in pluginRegex.findAll(target)) {
                val slug = match.groupValues[1]
                if (slug.isBlank() || slug in seen) continue
                seen += slug
                val version = Regex("[?&]ver=([\\d.]+)").find(
                    target.substringAfter("/wp-content/plugins/$slug/").take(300)
                )?.groupValues?.get(1)
                val name = slug.split('-').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                results += Technology(
                    name = name,
                    categories = listOf("WordPress Plugin"),
                    icon = null,
                    website = null,
                    version = version,
                    confidence = 100,
                    detectionMethod = "WP Plugin URL",
                    matchedValue = "/wp-content/plugins/$slug/",
                )
            }
        }
        return results.sortedBy { it.name }
    }

    fun appendNetworkRequest(req: NetworkRequest) {
        _state.update { it.copy(liveNetworkRequests = it.liveNetworkRequests + req) }
    }

    fun appendConsoleMessage(msg: ConsoleMessage) {
        _state.update { it.copy(consoleMessages = it.consoleMessages + msg) }
    }

    fun onNewScan() {
        spider.reset()
        _state.update {
            it.copy(
                urlInput = "",
                isScanning = false, hasScanned = false,
                signals = null, techs = emptyList(),
                rawRequest = "", repeaterResponse = null,
                isSendingRequest = false, scanStage = "",
                wizardSent = false, wizardHeaders = emptyList(),
                wizardBody = "", wizardMethod = "GET", wizardUrl = "",
                liveNetworkRequests = emptyList(), consoleMessages = emptyList(),
                reconData = null, reconLoading = false,
                // historySize and historyPosition preserved so arrows remain visible
            )
        }
    }

    fun onWizardMethodChanged(m: String) { _state.update { it.copy(wizardMethod = m) } }
    fun onWizardUrlChanged(u: String) { _state.update { it.copy(wizardUrl = u) } }
    fun onWizardBodyChanged(b: String) { _state.update { it.copy(wizardBody = b) } }
    fun onWizardSentChanged(sent: Boolean) { _state.update { it.copy(wizardSent = sent) } }
    fun onWizardHeaderAdd(k: String, v: String) {
        if (k.isBlank()) return
        _state.update { it.copy(wizardHeaders = it.wizardHeaders + (k.trim() to v)) }
    }
    fun onWizardHeaderRemove(i: Int) {
        _state.update { s ->
            s.copy(wizardHeaders = s.wizardHeaders.toMutableList().also { it.removeAt(i) })
        }
    }
    fun onWizardHeaderValueChanged(i: Int, v: String) {
        _state.update { s ->
            val list = s.wizardHeaders.toMutableList()
            if (i in list.indices) list[i] = list[i].first to v
            s.copy(wizardHeaders = list)
        }
    }

    /** Prefill the Repeater (wizard mode) with a URL — e.g. from a Spider result. */
    fun prefillRepeater(url: String) {
        val full = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        _state.update {
            it.copy(
                isRepeaterWizard = true,
                wizardUrl = full,
                wizardMethod = "GET",
                wizardHeaders = emptyList(),
                wizardBody = "",
                wizardSent = false,
                repeaterResponse = null,
            )
        }
    }

    fun onRepeaterReset() {
        _state.update {
            it.copy(
                repeaterResponse = null, isSendingRequest = false,
                wizardSent = false, wizardHeaders = emptyList(),
                wizardBody = "", wizardMethod = "GET",
            )
        }
    }

    fun sendRepeaterRequest(rawText: String) {
        if (_state.value.isSendingRequest) return
        val parsed = parseRawRequest(rawText, _state.value.urlInput.trim())
        val url = parsed.url.ifBlank { return }
        val skip = setOf("host", "content-length", "connection", "transfer-encoding")
        val headers = parsed.headers.filterKeys { it.lowercase() !in skip }
        _state.update { it.copy(isSendingRequest = true, repeaterResponse = null) }
        viewModelScope.launch {
            val result = sendRequest(parsed.method, url, headers, parsed.body)
            _state.update { it.copy(isSendingRequest = false, repeaterResponse = result) }
        }
    }

    fun sendRepeaterRequestFromParts(method: String, url: String, headers: Map<String, String>, body: String) {
        if (_state.value.isSendingRequest) return
        val fullUrl = if (url.startsWith("http")) url else "https://$url"
        val skip = setOf("host", "content-length", "connection", "transfer-encoding")
        val filtered = headers.filterKeys { it.lowercase() !in skip }
        _state.update { it.copy(isSendingRequest = true, repeaterResponse = null) }
        viewModelScope.launch {
            val result = sendRequest(method, fullUrl, filtered, body)
            _state.update { it.copy(isSendingRequest = false, repeaterResponse = result) }
        }
    }

    fun scan(webView: WebView) {
        if (_state.value.isScanning) return

        val isRawMode = !_state.value.isRepeaterWizard
        val rawText = _state.value.rawRequest

        val url: String
        val method: String
        val customHeaders: Map<String, String>
        val requestBody: String

        if (isRawMode && rawText.isNotBlank()) {
            val parsed = parseRawRequest(rawText, _state.value.urlInput.trim())
            url = parsed.url.ifBlank { _state.value.urlInput.trim() }
            method = parsed.method
            // exclude headers OkHttp manages automatically
            val skip = setOf("host", "content-length", "connection", "transfer-encoding")
            customHeaders = parsed.headers.filterKeys { it.lowercase() !in skip }
            requestBody = parsed.body
            _state.update { it.copy(urlInput = url, httpMethod = method) }
        } else {
            url = _state.value.urlInput.trim()
            method = _state.value.httpMethod
            customHeaders = _state.value.customHeaders.associate { it.first to it.second }
            requestBody = _state.value.requestBody
        }

        if (url.isBlank()) return

        // Each scan is its own "project" — clear the live Spider, network and console
        // capture from any prior scan so data never bleeds between scans.
        spider.reset()
        _state.update {
            it.copy(
                isScanning = true, scanStage = "Initializing…",
                liveNetworkRequests = emptyList(), consoleMessages = emptyList(),
            )
        }
        viewModelScope.launch {
            try {
                val db = dbDeferred.await()
                val userAgent    = AppPreferences.userAgentFlow(appContext).first()
                val followRedir  = AppPreferences.followRedirectsFlow(appContext).first()
                val timeoutSec   = AppPreferences.scanTimeoutFlow(appContext).first()
                val saveHtml     = AppPreferences.saveHtmlSourceFlow(appContext).first()
                val result = scanUrl(
                    webView, url,
                    db.allJsPaths(), db.allDomChecksJson(),
                    method, customHeaders, requestBody,
                    timeoutMs       = timeoutSec * 1000L,
                    userAgent       = userAgent,
                    followRedirects = followRedir,
                ) { stage ->
                    _state.update { it.copy(scanStage = stage) }
                }
                _state.update { it.copy(scanStage = "Fingerprinting…") }
                val detected = FingerprintEngine.detect(result, db)
                val wpPlugins = if (detected.any { it.name.equals("WordPress", ignoreCase = true) }) {
                    extractWpPlugins(result.htmlSource, result.scriptUrls + result.cssUrls)
                } else emptyList()
                val allTechs = groupPaymentCards(detected + wpPlugins)
                android.util.Log.d("Ecliptic", "Detected: ${allTechs.size} techs, ${wpPlugins.size} WP plugins")
                val newCount = AppPreferences.incrementScanCount(appContext)
                val rawReq = buildRawRequest(result, method)
                val wizHeaders = result.requestHeaders.entries.map { e -> e.key to e.value }
                val snapshot = ScanSnapshot(
                    url = result.finalUrl,
                    signals = result,
                    techs = allTechs,
                    networkRequests = emptyList(),
                    consoleMessages = emptyList(),
                    rawRequest = rawReq,
                    wizardUrl = result.finalUrl,
                    wizardHeaders = wizHeaders,
                )
                snapshots.add(snapshot)
                currentSnapshotIdx = snapshots.lastIndex
                ScanHistoryStore.save(appContext, snapshot, saveHtml)
                _state.update {
                    it.copy(
                        isScanning = false, scanStage = "", hasScanned = true,
                        scanCount = newCount,
                        signals = result, techs = allTechs,
                        rawRequest = rawReq,
                        wizardUrl = result.finalUrl,
                        wizardHeaders = wizHeaders,
                        wizardMethod = "GET",
                        wizardBody = "",
                        wizardSent = false,
                        historySize = snapshots.size,
                        historyPosition = snapshots.size,
                        reconData = null,
                        reconLoading = false,
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("Ecliptic", "Scan exception", e)
                _state.update {
                    it.copy(
                        isScanning = false,
                        scanStage = "",
                        hasScanned = true,
                        signals = RawSignals(
                            url = url, finalUrl = url,
                            headers = emptyMap(), globals = emptyMap(),
                            metaTags = emptyMap(), scriptUrls = emptyList(),
                            cssUrls = emptyList(), bodyClasses = emptyList(),
                            htmlAttrs = emptyMap(), htmlSource = "", title = "",
                            error = "Error: ${e.message}",
                        ),
                    )
                }
            }
        }
    }
}
