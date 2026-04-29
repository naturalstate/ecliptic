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
import com.ecliptic.scanner.RepeaterResponse
import com.ecliptic.scanner.scanUrl
import com.ecliptic.scanner.sendRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = getApplication<Application>()
    private val dbDeferred = viewModelScope.async { TechDatabase.get(getApplication()) }

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val method = AppPreferences.httpMethodFlow(appContext).first()
            val count  = AppPreferences.scanCountFlow(appContext).first()
            _state.update { it.copy(httpMethod = method, scanCount = count) }
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
        _state.update {
            it.copy(
                isScanning = false, hasScanned = false,
                signals = null, techs = emptyList(),
                rawRequest = "", repeaterResponse = null,
                isSendingRequest = false, scanStage = "",
                wizardSent = false, wizardHeaders = emptyList(),
                wizardBody = "", wizardMethod = "GET", wizardUrl = "",
                liveNetworkRequests = emptyList(), consoleMessages = emptyList(),
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

        _state.update { it.copy(isScanning = true, scanStage = "Initializing…") }
        viewModelScope.launch {
            try {
                val db = dbDeferred.await()
                val userAgent    = AppPreferences.userAgentFlow(appContext).first()
                val followRedir  = AppPreferences.followRedirectsFlow(appContext).first()
                val timeoutSec   = AppPreferences.scanTimeoutFlow(appContext).first()
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
                android.util.Log.d("Ecliptic", "Detected: ${detected.size} techs, ${wpPlugins.size} WP plugins")
                val newCount = AppPreferences.incrementScanCount(appContext)
                _state.update {
                    it.copy(
                        isScanning = false, scanStage = "", hasScanned = true,
                        scanCount = newCount,
                        signals = result, techs = detected + wpPlugins,
                        rawRequest = buildRawRequest(result, method),
                        wizardUrl = result.finalUrl,
                        wizardHeaders = result.requestHeaders.entries.map { e -> e.key to e.value },
                        wizardMethod = "GET",
                        wizardBody = "",
                        wizardSent = false,
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
