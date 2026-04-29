package com.ecliptic.scanner

import com.ecliptic.fingerprint.Technology
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateHtmlReport(
    signals: RawSignals,
    techs: List<Technology>,
    appVersion: String,
): String {
    val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    val grouped = techs.groupBy { it.categories.firstOrNull() ?: "Other" }
        .entries.sortedBy { it.key }

    val securityHeaders = mapOf(
        "content-security-policy"          to "Content-Security-Policy",
        "strict-transport-security"        to "Strict-Transport-Security",
        "x-frame-options"                  to "X-Frame-Options",
        "x-content-type-options"           to "X-Content-Type-Options",
        "referrer-policy"                  to "Referrer-Policy",
        "permissions-policy"               to "Permissions-Policy",
        "x-xss-protection"                 to "X-XSS-Protection",
        "cross-origin-embedder-policy"     to "COEP",
        "cross-origin-opener-policy"       to "COOP",
        "cross-origin-resource-policy"     to "CORP",
    )

    fun esc(s: String?) = s.orEmpty()
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    fun techsHtml(): String {
        if (techs.isEmpty()) return "<p class='muted'>No technologies detected</p>"
        return buildString {
            for ((cat, list) in grouped) {
                append("<div class='section-label'>$cat</div>")
                append("<table>")
                for (t in list.sortedBy { it.name }) {
                    val ver = t.version?.let { " <span class='ver'>$it</span>" } ?: ""
                    val conf = if (t.confidence < 100) " <span class='badge'>~${t.confidence}%</span>" else ""
                    append("<tr><td>${esc(t.name)}$ver$conf</td><td class='muted'>${esc(t.detectionMethod)}</td></tr>")
                }
                append("</table>")
            }
        }
    }

    fun headersHtml(): String = buildString {
        append("<table>")
        append("<tr><th>Header</th><th>Status</th><th>Value</th></tr>")
        for ((key, label) in securityHeaders) {
            val value = signals.headers[key]
            val (status, cls) = if (value != null) ("✓ Present" to "pass") else ("✗ Missing" to "fail")
            append("<tr><td>${esc(label)}</td><td class='$cls'>$status</td><td class='mono'>${esc(value?.take(120))}</td></tr>")
        }
        append("</table>")
    }

    fun allHeadersHtml(): String = buildString {
        append("<table>")
        for ((k, v) in signals.headers.entries.sortedBy { it.key }) {
            if (k == "_status") continue
            append("<tr><td class='mono'>${esc(k)}</td><td class='mono'>${esc(v.take(200))}</td></tr>")
        }
        append("</table>")
    }

    fun reqHeadersHtml(): String = buildString {
        append("<table>")
        for ((k, v) in signals.requestHeaders.entries.sortedBy { it.key }) {
            append("<tr><td class='mono'>${esc(k)}</td><td class='mono'>${esc(v.take(200))}</td></tr>")
        }
        append("</table>")
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
            if (tls.sans.isNotEmpty()) {
                append("<tr><td>SANs</td><td class='mono'>${tls.sans.take(5).joinToString(", ") { esc(it) }}</td></tr>")
            }
            append("</table>")
        }
    }

    fun cookiesHtml(): String {
        if (signals.richCookies.isEmpty() && signals.cookies.isEmpty()) return "<p class='muted'>No cookies</p>"
        return buildString {
            append("<table>")
            append("<tr><th>Name</th><th>HttpOnly</th><th>Secure</th><th>SameSite</th><th>Domain</th></tr>")
            for (c in signals.richCookies) {
                val ho = if (c.httpOnly) "<span class='pass'>✓</span>" else "<span class='fail'>✗</span>"
                val sec = if (c.secure) "<span class='pass'>✓</span>" else "<span class='fail'>✗</span>"
                append("<tr><td class='mono'>${esc(c.name)}</td><td>$ho</td><td>$sec</td><td>${esc(c.sameSite)}</td><td class='mono'>${esc(c.domain)}</td></tr>")
            }
            append("</table>")
        }
    }

    fun apisHtml(): String {
        if (signals.detectedApis.isEmpty()) return "<p class='muted'>No APIs detected</p>"
        return buildString {
            append("<table>")
            for (api in signals.detectedApis) {
                append("<tr><td>${esc(api.type)}</td><td class='muted'>${esc(api.evidence.take(120))}</td></tr>")
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
                append("<div class='section-label'>$type (${list.size})</div>")
                append("<table>")
                for (r in list.take(50)) {
                    append("<tr><td class='mono'>${esc(r.url.take(200))}</td></tr>")
                }
                if (list.size > 50) append("<tr><td class='muted'>… ${list.size - 50} more</td></tr>")
                append("</table>")
            }
        }
    }

    val statusCode = signals.headers["_status"] ?: "—"

    return """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Ecliptic Report — ${esc(signals.finalUrl)}</title>
<style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#0a0a0a;color:#e0e0e0;padding:24px;font-size:14px;line-height:1.5}
.header{border-bottom:2px solid #00e5c8;padding-bottom:16px;margin-bottom:28px}
.brand{color:#00e5c8;font-size:28px;font-weight:800;letter-spacing:.12em}
.url{color:#aaa;font-size:13px;margin-top:4px;word-break:break-all}
.meta{color:#666;font-size:12px;margin-top:8px}
h2{color:#00e5c8;font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:.1em;margin:28px 0 10px;padding-bottom:4px;border-bottom:1px solid #1e1e1e}
table{width:100%;border-collapse:collapse;margin-bottom:8px}
th{text-align:left;color:#888;font-size:11px;text-transform:uppercase;letter-spacing:.05em;padding:5px 8px;border-bottom:1px solid #1e1e1e}
td{padding:5px 8px;border-bottom:1px solid #141414;vertical-align:top;word-break:break-word}
.mono{font-family:'Courier New',monospace;font-size:12px;color:#ccc}
.muted{color:#666;font-size:12px}
.pass{color:#4caf50}
.fail{color:#f44336}
.warn{color:#ff9800}
.ver{color:#00e5c8;font-size:12px}
.badge{background:#1e1e1e;color:#888;font-size:11px;padding:1px 5px;border-radius:4px}
.section-label{color:#888;font-size:11px;font-weight:600;text-transform:uppercase;letter-spacing:.08em;margin:12px 0 4px}
@media print{body{background:#fff;color:#111}h2{color:#007acc}.brand{color:#007acc}.url,.meta{color:#555}.pass{color:#2a7a2a}.fail{color:#c00}.mono{color:#333}.muted{color:#888}}
</style>
</head>
<body>
<div class="header">
  <div class="brand">ECLIPTIC</div>
  <div class="url">${esc(signals.finalUrl)}</div>
  <div class="meta">Generated $ts · HTTP $statusCode · Ecliptic v$appVersion · ${techs.size} technologies detected</div>
</div>

<h2>Technologies (${techs.size})</h2>
${techsHtml()}

<h2>Security Headers</h2>
${headersHtml()}

<h2>TLS / Certificate</h2>
${tlsHtml()}

<h2>Cookies (${signals.richCookies.size})</h2>
${cookiesHtml()}

<h2>Detected APIs</h2>
${apisHtml()}

<h2>Network Requests (${signals.networkRequests.size})</h2>
${networkHtml()}

<h2>Response Headers</h2>
${allHeadersHtml()}

<h2>Request Headers</h2>
${reqHeadersHtml()}

</body>
</html>"""
}
