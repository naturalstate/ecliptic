package com.ecliptic.scanner

import com.ecliptic.fingerprint.Technology
import com.ecliptic.ui.DevIconMapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateHtmlReport(
    signals: RawSignals,
    techs: List<Technology>,
    appVersion: String,
    includeFullHtml: Boolean = false,
    includeDomTree: Boolean = false,
): String {
    val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    val grouped = techs.groupBy { it.categories.firstOrNull() ?: "Other" }
        .entries.sortedBy { it.key }

    val securityHeaders = mapOf(
        "content-security-policy"      to "Content-Security-Policy",
        "strict-transport-security"    to "Strict-Transport-Security",
        "x-frame-options"              to "X-Frame-Options",
        "x-content-type-options"       to "X-Content-Type-Options",
        "referrer-policy"              to "Referrer-Policy",
        "permissions-policy"           to "Permissions-Policy",
        "x-xss-protection"             to "X-XSS-Protection",
        "cross-origin-opener-policy"   to "COOP",
        "cross-origin-embedder-policy" to "COEP",
        "cross-origin-resource-policy" to "CORP",
    )

    fun esc(s: String?) = s.orEmpty()
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    fun techsHtml(): String {
        if (techs.isEmpty()) return "<p class='muted'>No technologies detected</p>"
        return buildString {
            for ((cat, list) in grouped) {
                append("<div class='section-label'>${esc(cat)}</div>")
                append("<table>")
                for (t in list.sortedBy { it.name }) {
                    val slug = DevIconMapper.slugFor(t.name)
                    val icon = if (slug != null)
                        "<img class='ico' src='${DevIconMapper.iconUrl(slug)}' alt='' onerror=\"this.style.display='none'\"/>" else ""
                    val ver = t.version?.let { " <span class='ver'>$it</span>" } ?: ""
                    val conf = if (t.confidence < 100) " <span class='badge'>~${t.confidence}%</span>" else ""
                    append("<tr><td>$icon${esc(t.name)}$ver$conf</td><td class='muted'>${esc(t.detectionMethod)}</td></tr>")
                }
                append("</table>")
            }
        }
    }

    fun headersHtml(): String = buildString {
        append("<table><tr><th>Header</th><th>Status</th><th>Value</th></tr>")
        for ((key, label) in securityHeaders) {
            val value = signals.headers[key]
            val (status, cls) = if (value != null) ("Present" to "pass") else ("Missing" to "fail")
            append("<tr><td>${esc(label)}</td><td class='$cls'>$status</td><td class='mono'>${esc(value?.take(160))}</td></tr>")
        }
        append("</table>")
    }

    fun apisHtml(): String {
        if (signals.detectedApis.isEmpty()) return "<p class='muted'>No APIs detected</p>"
        val byType = signals.detectedApis.groupBy { it.type }.entries.sortedBy { it.key }
        return buildString {
            for ((type, items) in byType) {
                append("<div class='section-label'>${esc(type)} (${items.size})</div>")
                append("<table>")
                for (api in items) {
                    val target = api.endpoint ?: "(${esc(api.evidence)})"
                    append("<tr><td class='mono'>${esc(target)}</td></tr>")
                }
                append("</table>")
            }
        }
    }

    fun paramsHtml(): String {
        val params = Extractor.findParams(signals.htmlSource)
        if (params.isEmpty()) return "<p class='muted'>No parameters found</p>"
        return buildString {
            append("<table><tr><th>Name</th><th>Sample value</th></tr>")
            for ((name, value) in params.take(200)) {
                append("<tr><td class='mono'>${esc(name)}</td><td class='mono muted'>${esc(value.take(80))}</td></tr>")
            }
            append("</table>")
        }
    }

    fun stringsHtml(): String {
        val findings = Extractor.findStrings(signals.htmlSource)
        if (findings.isEmpty()) return "<p class='muted'>No interesting strings in HTML</p>"
        val byCat = findings.groupBy { it.category }.entries
        return buildString {
            for (entry in byCat) {
                val cls = when (entry.value.first().severity) { "alert" -> "fail"; "warn" -> "warn"; else -> "" }
                append("<div class='section-label $cls'>${esc(entry.key)} (${entry.value.size})</div>")
                append("<table>")
                for (f in entry.value.take(60)) append("<tr><td class='mono'>${esc(f.value.take(200))}</td></tr>")
                append("</table>")
            }
        }
    }

    fun socialHtml(): String {
        val patterns = listOf(
            "Twitter / X" to Regex("""https?://(?:www\.)?(?:twitter|x)\.com/[^/"'\s?#]{2,30}""", RegexOption.IGNORE_CASE),
            "Facebook"    to Regex("""https?://(?:www\.)?facebook\.com/[^/"'\s?#]{2,}""", RegexOption.IGNORE_CASE),
            "Instagram"   to Regex("""https?://(?:www\.)?instagram\.com/[^/"'\s?#]{2,}""", RegexOption.IGNORE_CASE),
            "LinkedIn"    to Regex("""https?://(?:www\.)?linkedin\.com/(?:company|in)/[^/"'\s?#]{2,}""", RegexOption.IGNORE_CASE),
            "YouTube"     to Regex("""https?://(?:www\.)?youtube\.com/(?:@|c/|channel/|user/)[^/"'\s?#]{2,}""", RegexOption.IGNORE_CASE),
            "GitHub"      to Regex("""https?://(?:www\.)?github\.com/[^/"'\s?#]{2,}""", RegexOption.IGNORE_CASE),
            "TikTok"      to Regex("""https?://(?:www\.)?tiktok\.com/@[^/"'\s?#]{2,}""", RegexOption.IGNORE_CASE),
        )
        val found = patterns.mapNotNull { (name, re) -> re.find(signals.htmlSource)?.value?.let { name to it } }
        if (found.isEmpty()) return "<p class='muted'>No social links found</p>"
        return buildString {
            append("<table>")
            for ((name, url) in found) append("<tr><td>${esc(name)}</td><td class='mono'>${esc(url)}</td></tr>")
            append("</table>")
        }
    }

    fun tlsHtml(): String {
        val tls = signals.tls ?: return "<p class='muted'>TLS info unavailable</p>"
        if (tls.error != null) return "<p class='fail'>${esc(tls.error)}</p>"
        return buildString {
            append("<table>")
            tls.tlsVersion?.let { append("<tr><td>Protocol</td><td class='mono'>${esc(it)}</td></tr>") }
            tls.cipherSuite?.let { append("<tr><td>Cipher</td><td class='mono'>${esc(it)}</td></tr>") }
            tls.subject?.let { append("<tr><td>Subject</td><td class='mono'>${esc(it)}</td></tr>") }
            tls.issuer?.let { append("<tr><td>Issuer</td><td class='mono'>${esc(it)}</td></tr>") }
            tls.validFrom?.let { append("<tr><td>Valid from</td><td class='mono'>${esc(it)}</td></tr>") }
            tls.validTo?.let { append("<tr><td>Valid to</td><td class='mono'>${esc(it)}</td></tr>") }
            if (tls.sans.isNotEmpty()) append("<tr><td>SANs</td><td class='mono'>${tls.sans.take(15).joinToString(", ") { esc(it) }}</td></tr>")
            append("</table>")
        }
    }

    fun cookiesHtml(): String {
        if (signals.richCookies.isEmpty() && signals.cookies.isEmpty()) return "<p class='muted'>No cookies</p>"
        return buildString {
            append("<table><tr><th>Name</th><th>HttpOnly</th><th>Secure</th><th>SameSite</th><th>Domain</th></tr>")
            for (c in signals.richCookies) {
                val ho = if (c.httpOnly) "<span class='pass'>yes</span>" else "<span class='fail'>no</span>"
                val sec = if (c.secure) "<span class='pass'>yes</span>" else "<span class='fail'>no</span>"
                append("<tr><td class='mono'>${esc(c.name)}</td><td>$ho</td><td>$sec</td><td>${esc(c.sameSite)}</td><td class='mono'>${esc(c.domain)}</td></tr>")
            }
            append("</table>")
        }
    }

    fun networkHtml(): String {
        val reqs = signals.networkRequests
        if (reqs.isEmpty()) return "<p class='muted'>No network requests captured</p>"
        val byType = reqs.groupBy { it.type }.entries.sortedBy { it.key }
        return buildString {
            for ((type, list) in byType) {
                append("<div class='section-label'>${esc(type)} (${list.size})</div><table>")
                for (r in list.take(80)) append("<tr><td class='mono'>${esc(r.url.take(220))}</td></tr>")
                if (list.size > 80) append("<tr><td class='muted'>… ${list.size - 80} more</td></tr>")
                append("</table>")
            }
        }
    }

    fun sourceFilesHtml(): String {
        val scripts = signals.scriptUrls
        val css = signals.cssUrls
        if (scripts.isEmpty() && css.isEmpty()) return "<p class='muted'>No external files</p>"
        fun name(u: String) = u.substringAfterLast('/').substringBefore('?').ifBlank { u }
        return buildString {
            if (scripts.isNotEmpty()) {
                append("<div class='section-label'>Scripts (${scripts.size})</div><table>")
                scripts.take(80).forEach { append("<tr><td class='mono'>${esc(name(it))}</td><td class='mono muted'>${esc(it.take(160))}</td></tr>") }
                append("</table>")
            }
            if (css.isNotEmpty()) {
                append("<div class='section-label'>Stylesheets (${css.size})</div><table>")
                css.take(40).forEach { append("<tr><td class='mono'>${esc(name(it))}</td><td class='mono muted'>${esc(it.take(160))}</td></tr>") }
                append("</table>")
            }
        }
    }

    val statusCode = signals.headers["_status"] ?: "—"

    val optionalSections = buildString {
        if (includeFullHtml && signals.htmlSource.isNotBlank()) {
            append("<h2>Full HTML Source</h2><pre class='code'>${esc(signals.htmlSource.take(400_000))}</pre>")
        }
        if (includeDomTree && signals.domTree.isNotBlank()) {
            append("<h2>DOM Tree (raw)</h2><pre class='code'>${esc(signals.domTree.take(400_000))}</pre>")
        }
    }

    return """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Ecliptic Report — ${esc(signals.finalUrl)}</title>
<style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#ffffff;color:#1a1a1a;padding:28px;font-size:14px;line-height:1.5;max-width:900px;margin:0 auto}
.header{border-bottom:3px solid #0a7f70;padding-bottom:16px;margin-bottom:28px}
.brand{color:#0a7f70;font-size:30px;font-weight:800;letter-spacing:.12em}
.url{color:#333;font-size:13px;margin-top:6px;word-break:break-all;font-family:'Courier New',monospace}
.meta{color:#888;font-size:12px;margin-top:8px}
h2{color:#0a7f70;font-size:13px;font-weight:700;text-transform:uppercase;letter-spacing:.08em;margin:30px 0 10px;padding-bottom:5px;border-bottom:1px solid #e5e5e5}
table{width:100%;border-collapse:collapse;margin-bottom:10px}
th{text-align:left;color:#888;font-size:11px;text-transform:uppercase;letter-spacing:.05em;padding:6px 8px;border-bottom:1px solid #e5e5e5}
td{padding:6px 8px;border-bottom:1px solid #f0f0f0;vertical-align:top;word-break:break-word}
.ico{width:15px;height:15px;vertical-align:-2px;margin-right:7px}
.mono{font-family:'Courier New',monospace;font-size:12px;color:#444}
.muted{color:#999}
.pass{color:#1a9d4e;font-weight:600}
.fail{color:#d23;font-weight:600}
.warn{color:#c47d00;font-weight:600}
.ver{color:#0a7f70;font-size:12px;font-weight:600}
.badge{background:#eee;color:#777;font-size:11px;padding:1px 5px;border-radius:4px}
.section-label{color:#666;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.06em;margin:14px 0 4px}
.code{background:#f6f8fa;border:1px solid #e5e5e5;border-radius:6px;padding:12px;font-family:'Courier New',monospace;font-size:11px;white-space:pre-wrap;word-break:break-all;overflow-x:auto;color:#333}
</style>
</head>
<body>
<div class="header">
  <div class="brand">ECLIPTIC</div>
  <div class="url">${esc(signals.finalUrl)}</div>
  <div class="meta">Generated $ts · HTTP $statusCode · Ecliptic v$appVersion · ${techs.size} technologies</div>
</div>

<h2>Technologies (${techs.size})</h2>
${techsHtml()}

<h2>Security Headers</h2>
${headersHtml()}

<h2>Detected APIs</h2>
${apisHtml()}

<h2>Parameters</h2>
${paramsHtml()}

<h2>Interesting Strings</h2>
${stringsHtml()}

<h2>Social Links</h2>
${socialHtml()}

<h2>TLS / Certificate</h2>
${tlsHtml()}

<h2>Cookies (${signals.richCookies.size})</h2>
${cookiesHtml()}

<h2>Network Requests (${signals.networkRequests.size})</h2>
${networkHtml()}

<h2>Source Files</h2>
${sourceFilesHtml()}

<h2>Response Headers</h2>
<table>${signals.headers.entries.filter { it.key != "_status" }.sortedBy { it.key }.joinToString("") { "<tr><td class='mono'>${esc(it.key)}</td><td class='mono'>${esc(it.value.take(220))}</td></tr>" }}</table>

<h2>Request Headers</h2>
<table>${signals.requestHeaders.entries.sortedBy { it.key }.joinToString("") { "<tr><td class='mono'>${esc(it.key)}</td><td class='mono'>${esc(it.value.take(220))}</td></tr>" }}</table>

$optionalSections

</body>
</html>"""
}
