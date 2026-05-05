package com.ecliptic

import android.content.Context
import com.ecliptic.fingerprint.Technology
import com.ecliptic.scanner.CookieInfo
import com.ecliptic.scanner.DetectedApi
import com.ecliptic.scanner.NetworkRequest
import com.ecliptic.scanner.RawSignals
import com.ecliptic.scanner.TlsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object ScanHistoryStore {

    private const val MAX_SCANS = 50
    private const val DIR = "scan_history"

    private fun dir(context: Context) = File(context.filesDir, DIR).also { it.mkdirs() }

    suspend fun save(context: Context, snapshot: ScanSnapshot, saveHtmlSource: Boolean) =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = snapshot.toJson(saveHtmlSource)
                File(dir(context), "${snapshot.timestamp}.json").writeText(json.toString())
                pruneOld(context)
            }
        }

    suspend fun loadAll(context: Context): List<ScanSnapshot> = withContext(Dispatchers.IO) {
        val files = dir(context).listFiles()
            ?.filter { it.extension == "json" }
            ?.sortedBy { it.nameWithoutExtension.toLongOrNull() ?: 0L }
            ?: return@withContext emptyList()
        files.mapNotNull { runCatching { JSONObject(it.readText()).toScanSnapshot() }.getOrNull() }
    }

    private fun pruneOld(context: Context) {
        val files = dir(context).listFiles()
            ?.filter { it.extension == "json" }
            ?.sortedByDescending { it.nameWithoutExtension.toLongOrNull() ?: 0L }
            ?: return
        files.drop(MAX_SCANS).forEach { it.delete() }
    }
}

// ── Serialization ─────────────────────────────────────────────────────────────

private fun ScanSnapshot.toJson(saveHtmlSource: Boolean): JSONObject {
    val sig = signals
    return JSONObject().apply {
        put("timestamp", timestamp)
        put("url", url)
        put("title", sig.title)
        sig.error?.let { put("error", it) }
        put("rawRequest", rawRequest)
        put("wizardUrl", wizardUrl)
        put("techs", JSONArray().also { arr -> techs.forEach { arr.put(it.toJson()) } })
        put("requestHeaders", sig.requestHeaders.toJsonObject())
        put("responseHeaders", sig.headers.toJsonObject())
        put("richCookies", JSONArray().also { arr -> sig.richCookies.forEach { arr.put(it.toJson()) } })
        sig.tls?.let { put("tls", it.toJson()) }
        put("apis", JSONArray().also { arr -> sig.detectedApis.forEach { arr.put(it.toJson()) } })
        put("networkRequests", JSONArray().also { arr -> sig.networkRequests.forEach { arr.put(it.toJson()) } })
        put("wizardHeaders", JSONArray().also { arr ->
            wizardHeaders.forEach { (k, v) -> arr.put(JSONArray().put(k).put(v)) }
        })
        if (saveHtmlSource) put("htmlSource", sig.htmlSource)
    }
}

// ── Deserialization ───────────────────────────────────────────────────────────

private fun JSONObject.toScanSnapshot(): ScanSnapshot {
    val timestamp   = getLong("timestamp")
    val url         = getString("url")
    val title       = optString("title", "")
    val error       = optString("error").ifBlank { null }
    val rawRequest  = optString("rawRequest", "")
    val wizardUrl   = optString("wizardUrl", url)
    val htmlSource  = optString("htmlSource", "")

    val techs = getJSONArray("techs").mapObjects { it.toTechnology() }
    val requestHeaders  = optJSONObject("requestHeaders")?.toStringMap() ?: emptyMap()
    val responseHeaders = optJSONObject("responseHeaders")?.toStringMap() ?: emptyMap()
    val richCookies     = optJSONArray("richCookies")?.mapObjects { it.toCookieInfo() } ?: emptyList()
    val tls             = optJSONObject("tls")?.toTlsResult()
    val apis            = optJSONArray("apis")?.mapObjects { it.toDetectedApi() } ?: emptyList()
    val networkRequests = optJSONArray("networkRequests")?.mapObjects { it.toNetworkRequest() } ?: emptyList()
    val wizardHeaders   = optJSONArray("wizardHeaders")?.let { arr ->
        (0 until arr.length()).map { i ->
            val pair = arr.getJSONArray(i)
            pair.getString(0) to pair.getString(1)
        }
    } ?: emptyList()

    val signals = RawSignals(
        url = url, finalUrl = url,
        requestHeaders = requestHeaders,
        headers = responseHeaders,
        globals = emptyMap(),
        metaTags = emptyMap(),
        scriptUrls = emptyList(),
        cssUrls = emptyList(),
        bodyClasses = emptyList(),
        htmlAttrs = emptyMap(),
        htmlSource = htmlSource,
        title = title,
        richCookies = richCookies,
        detectedApis = apis,
        tls = tls,
        networkRequests = networkRequests,
        error = error,
    )

    return ScanSnapshot(
        timestamp = timestamp,
        url = url,
        signals = signals,
        techs = techs,
        networkRequests = networkRequests,
        consoleMessages = emptyList(),
        rawRequest = rawRequest,
        wizardUrl = wizardUrl,
        wizardHeaders = wizardHeaders,
    )
}

// ── Per-type helpers ──────────────────────────────────────────────────────────

private fun Technology.toJson() = JSONObject().apply {
    put("name", name)
    put("categories", JSONArray(categories))
    icon?.let { put("icon", it) }
    website?.let { put("website", it) }
    version?.let { put("version", it) }
    put("confidence", confidence)
    detectionMethod?.let { put("detectionMethod", it) }
    matchedValue?.let { put("matchedValue", it) }
}

private fun JSONObject.toTechnology() = Technology(
    name = getString("name"),
    categories = getJSONArray("categories").let { arr -> (0 until arr.length()).map { arr.getString(it) } },
    icon = optString("icon").ifBlank { null },
    website = optString("website").ifBlank { null },
    version = optString("version").ifBlank { null },
    confidence = optInt("confidence", 100),
    detectionMethod = optString("detectionMethod").ifBlank { null },
    matchedValue = optString("matchedValue").ifBlank { null },
)

private fun CookieInfo.toJson() = JSONObject().apply {
    put("name", name)
    put("value", value)
    domain?.let { put("domain", it) }
    path?.let { put("path", it) }
    expires?.let { put("expires", it) }
    maxAge?.let { put("maxAge", it) }
    put("httpOnly", httpOnly)
    put("secure", secure)
    sameSite?.let { put("sameSite", it) }
}

private fun JSONObject.toCookieInfo() = CookieInfo(
    name = getString("name"),
    value = optString("value", ""),
    domain = optString("domain").ifBlank { null },
    path = optString("path").ifBlank { null },
    expires = optString("expires").ifBlank { null },
    maxAge = if (has("maxAge")) getLong("maxAge") else null,
    httpOnly = optBoolean("httpOnly", false),
    secure = optBoolean("secure", false),
    sameSite = optString("sameSite").ifBlank { null },
)

private fun TlsResult.toJson() = JSONObject().apply {
    tlsVersion?.let { put("tlsVersion", it) }
    cipherSuite?.let { put("cipherSuite", it) }
    subject?.let { put("subject", it) }
    issuer?.let { put("issuer", it) }
    validFrom?.let { put("validFrom", it) }
    validTo?.let { put("validTo", it) }
    put("sans", JSONArray(sans))
    error?.let { put("error", it) }
}

private fun JSONObject.toTlsResult() = TlsResult(
    tlsVersion = optString("tlsVersion").ifBlank { null },
    cipherSuite = optString("cipherSuite").ifBlank { null },
    subject = optString("subject").ifBlank { null },
    issuer = optString("issuer").ifBlank { null },
    validFrom = optString("validFrom").ifBlank { null },
    validTo = optString("validTo").ifBlank { null },
    sans = optJSONArray("sans")?.let { arr -> (0 until arr.length()).map { arr.getString(it) } } ?: emptyList(),
    error = optString("error").ifBlank { null },
)

private fun DetectedApi.toJson() = JSONObject().apply {
    put("type", type)
    put("confidence", confidence)
    put("evidence", evidence)
    endpoint?.let { put("endpoint", it) }
}

private fun JSONObject.toDetectedApi() = DetectedApi(
    type = getString("type"),
    confidence = optInt("confidence", 0),
    evidence = optString("evidence", ""),
    endpoint = optString("endpoint").ifBlank { null },
)

private fun NetworkRequest.toJson() = JSONObject().apply {
    put("url", url)
    put("type", type)
    put("method", method)
    put("timestamp", timestamp)
}

private fun JSONObject.toNetworkRequest() = NetworkRequest(
    url = getString("url"),
    type = optString("type", "other"),
    method = optString("method", "GET"),
    timestamp = optLong("timestamp", 0L),
)

private fun Map<String, String>.toJsonObject() = JSONObject().also { obj -> forEach { (k, v) -> obj.put(k, v) } }

private fun JSONObject.toStringMap(): Map<String, String> = buildMap {
    keys().forEach { put(it, optString(it, "")) }
}

private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    (0 until length()).map { transform(getJSONObject(it)) }
