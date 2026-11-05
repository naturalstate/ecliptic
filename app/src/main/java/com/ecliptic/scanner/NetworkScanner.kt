package com.ecliptic.scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    // Accepts an absolute URL or a path relative to the origin.
    fun fetch(pathOrUrl: String): String? = try {
        val url = if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) pathOrUrl else "$base$pathOrUrl"
        val req = Request.Builder().url(url).header("User-Agent", USER_AGENT).build()
        httpClient.newCall(req).execute().use { resp ->
            if (resp.isSuccessful) resp.body?.string()?.take(50_000) else null
        }
    } catch (_: Exception) { null }

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
