package com.ecliptic

import android.content.Context
import com.ecliptic.fingerprint.Technology
import com.ecliptic.scanner.CookieInfo
import com.ecliptic.scanner.DetectedApi
import com.ecliptic.scanner.DnsResult
import com.ecliptic.scanner.EmailSecurity
import com.ecliptic.scanner.HostReputation
import com.ecliptic.scanner.NetworkRequest
import com.ecliptic.scanner.PortResult
import com.ecliptic.scanner.RawSignals
import com.ecliptic.scanner.RdapResult
import com.ecliptic.scanner.ReconData
import com.ecliptic.scanner.ReconFiles
import com.ecliptic.scanner.SimilarDomain
import com.ecliptic.scanner.StagingHost
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

    /** Permanently removes one scan's persisted file. No-op if it's already gone. */
    suspend fun delete(context: Context, timestamp: Long) = withContext(Dispatchers.IO) {
        runCatching { File(dir(context), "$timestamp.json").delete() }
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
        // Recon (WHOIS/DNS/ASN/robots/security.txt/sitemap) — small, always retained so a
        // revisited scan restores its recon without re-hitting the network.
        reconData?.let { put("reconData", it.toJson()) }
        // Small fields — always retained so a revisited scan keeps JS globals,
        // source file lists and meta even after an app restart.
        put("globals", JSONObject().also { o -> sig.globals.forEach { (k, v) -> o.put(k, v?.toString() ?: "true") } })
        put("jsProbe", sig.jsProbe.toJsonObject())
        put("metaTags", sig.metaTags.toJsonObject())
        put("scriptUrls", JSONArray(sig.scriptUrls))
        put("cssUrls", JSONArray(sig.cssUrls))
        put("cssProps", JSONArray(sig.cssProps))
        // Large fields (HTML source + DOM tree) — gated by the save-source setting.
        if (saveHtmlSource) {
            put("htmlSource", sig.htmlSource)
            put("domTree", sig.domTree)
        }
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
    val domTree     = optString("domTree", "")
    val globals     = optJSONObject("globals")?.toStringMap() ?: emptyMap()
    val jsProbe     = optJSONObject("jsProbe")?.toStringMap() ?: emptyMap()
    val metaTags    = optJSONObject("metaTags")?.toStringMap() ?: emptyMap()
    val scriptUrls  = optJSONArray("scriptUrls")?.toStringList() ?: emptyList()
    val cssUrls     = optJSONArray("cssUrls")?.toStringList() ?: emptyList()
    val cssProps    = optJSONArray("cssProps")?.toStringList() ?: emptyList()

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
    val reconData       = optJSONObject("reconData")?.toReconData()

    val signals = RawSignals(
        url = url, finalUrl = url,
        requestHeaders = requestHeaders,
        headers = responseHeaders,
        globals = globals,
        jsProbe = jsProbe,
        metaTags = metaTags,
        scriptUrls = scriptUrls,
        cssUrls = cssUrls,
        bodyClasses = emptyList(),
        htmlAttrs = emptyMap(),
        htmlSource = htmlSource,
        title = title,
        richCookies = richCookies,
        detectedApis = apis,
        cssProps = cssProps,
        domTree = domTree,
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
        reconData = reconData,
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

private fun ReconData.toJson() = JSONObject().apply {
    dns?.let { put("dns", it.toJson()) }
    rdap?.let { put("rdap", it.toJson()) }
    files?.let { put("files", it.toJson()) }
    asn?.let { put("asn", it) }
    if (subdomains.isNotEmpty()) put("subdomains", JSONArray(subdomains))
    if (subdomainsChecked) put("subdomainsChecked", true)
    if (ipNeighbors.isNotEmpty()) put("ipNeighbors", JSONArray(ipNeighbors))
    reputation?.let { put("reputation", it.toJson()) }
    if (similarDomains.isNotEmpty()) put("similarDomains", JSONArray().also { arr ->
        similarDomains.forEach { s -> arr.put(JSONObject().put("domain", s.domain).put("registered", s.registered)) }
    })
    email?.let { put("email", it.toJson()) }
    httpsRedirect?.let { put("httpsRedirect", it) }
    if (portsChecked) {
        put("portsChecked", true)
        put("openPorts", JSONArray().also { arr ->
            openPorts.forEach { p -> arr.put(JSONObject().put("port", p.port).put("service", p.service)) }
        })
    }
    if (stagingChecked) {
        put("stagingChecked", true)
        stagingTier?.let { put("stagingTier", it) }
        put("staging", JSONArray().also { arr ->
            staging.forEach { s -> arr.put(JSONObject().put("host", s.host).put("source", s.source).apply { s.ip?.let { put("ip", it) } }) }
        })
    }
    put("fetchedAt", fetchedAt)
}

private fun JSONObject.toReconData() = ReconData(
    dns = optJSONObject("dns")?.toDnsResult(),
    rdap = optJSONObject("rdap")?.toRdapResult(),
    files = optJSONObject("files")?.toReconFiles(),
    asn = optString("asn").ifBlank { null },
    subdomains = optJSONArray("subdomains")?.toStringList() ?: emptyList(),
    subdomainsChecked = optBoolean("subdomainsChecked", false),
    ipNeighbors = optJSONArray("ipNeighbors")?.toStringList() ?: emptyList(),
    reputation = optJSONObject("reputation")?.toHostReputation(),
    similarDomains = optJSONArray("similarDomains")?.let { arr ->
        (0 until arr.length()).mapNotNull { i ->
            arr.optJSONObject(i)?.let { SimilarDomain(it.optString("domain"), it.optBoolean("registered")) }
        }
    } ?: emptyList(),
    email = optJSONObject("email")?.toEmailSecurity(),
    httpsRedirect = if (has("httpsRedirect")) optBoolean("httpsRedirect") else null,
    openPorts = optJSONArray("openPorts")?.let { arr ->
        (0 until arr.length()).mapNotNull { i ->
            arr.optJSONObject(i)?.let { PortResult(it.optInt("port"), it.optString("service")) }
        }
    } ?: emptyList(),
    portsChecked = optBoolean("portsChecked", false),
    staging = optJSONArray("staging")?.let { arr ->
        (0 until arr.length()).mapNotNull { i ->
            arr.optJSONObject(i)?.let { StagingHost(it.optString("host"), it.optString("source"), it.optString("ip").ifBlank { null }) }
        }
    } ?: emptyList(),
    stagingChecked = optBoolean("stagingChecked", false),
    stagingTier = optString("stagingTier").ifBlank { null },
    fetchedAt = optLong("fetchedAt", 0L),
)

private fun EmailSecurity.toJson() = JSONObject().apply {
    spf?.let { put("spf", it) }
    dmarc?.let { put("dmarc", it) }
    dkimSelector?.let { put("dkimSelector", it) }
    dnssec?.let { put("dnssec", it) }
    if (caa.isNotEmpty()) put("caa", JSONArray(caa))
}

private fun JSONObject.toEmailSecurity() = EmailSecurity(
    spf = optString("spf").ifBlank { null },
    dmarc = optString("dmarc").ifBlank { null },
    dkimSelector = optString("dkimSelector").ifBlank { null },
    dnssec = if (has("dnssec")) optBoolean("dnssec") else null,
    caa = optJSONArray("caa")?.toStringList() ?: emptyList(),
)

private fun HostReputation.toJson() = JSONObject().apply {
    put("source", source)
    put("listed", listed)
    status?.let { put("status", it) }
    detail?.let { put("detail", it) }
    error?.let { put("error", it) }
}

private fun JSONObject.toHostReputation() = HostReputation(
    source = optString("source", "URLhaus"),
    listed = optBoolean("listed", false),
    status = optString("status").ifBlank { null },
    detail = optString("detail").ifBlank { null },
    error = optString("error").ifBlank { null },
)

private fun DnsResult.toJson() = JSONObject().apply {
    put("a", JSONArray(aRecords))
    put("cname", JSONArray(cnameRecords))
    put("mx", JSONArray(mxRecords))
    put("ns", JSONArray(nsRecords))
    put("txt", JSONArray(txtRecords))
    error?.let { put("error", it) }
}

private fun JSONObject.toDnsResult() = DnsResult(
    aRecords = optJSONArray("a")?.toStringList() ?: emptyList(),
    cnameRecords = optJSONArray("cname")?.toStringList() ?: emptyList(),
    mxRecords = optJSONArray("mx")?.toStringList() ?: emptyList(),
    nsRecords = optJSONArray("ns")?.toStringList() ?: emptyList(),
    txtRecords = optJSONArray("txt")?.toStringList() ?: emptyList(),
    error = optString("error").ifBlank { null },
)

private fun RdapResult.toJson() = JSONObject().apply {
    registrar?.let { put("registrar", it) }
    registrant?.let { put("registrant", it) }
    created?.let { put("created", it) }
    expires?.let { put("expires", it) }
    updated?.let { put("updated", it) }
    put("status", JSONArray(status))
    put("nameServers", JSONArray(nameServers))
    error?.let { put("error", it) }
}

private fun JSONObject.toRdapResult() = RdapResult(
    registrar = optString("registrar").ifBlank { null },
    registrant = optString("registrant").ifBlank { null },
    created = optString("created").ifBlank { null },
    expires = optString("expires").ifBlank { null },
    updated = optString("updated").ifBlank { null },
    status = optJSONArray("status")?.toStringList() ?: emptyList(),
    nameServers = optJSONArray("nameServers")?.toStringList() ?: emptyList(),
    error = optString("error").ifBlank { null },
)

private fun ReconFiles.toJson() = JSONObject().apply {
    robotsTxt?.let { put("robots", it) }
    securityTxt?.let { put("security", it) }
    sitemapXml?.let { put("sitemap", it) }
}

private fun JSONObject.toReconFiles() = ReconFiles(
    robotsTxt = optString("robots").ifBlank { null },
    securityTxt = optString("security").ifBlank { null },
    sitemapXml = optString("sitemap").ifBlank { null },
)

private fun Map<String, String>.toJsonObject() = JSONObject().also { obj -> forEach { (k, v) -> obj.put(k, v) } }

private fun JSONObject.toStringMap(): Map<String, String> = buildMap {
    keys().forEach { put(it, optString(it, "")) }
}

private fun JSONArray.toStringList(): List<String> = (0 until length()).map { optString(it, "") }

private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    (0 until length()).map { transform(getJSONObject(it)) }
