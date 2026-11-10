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
    darkMode: Boolean = false,
    recon: ReconData? = null,
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
                    val iconUrl = DevIconMapper.iconUrlFor(t.name)
                    val icon = if (iconUrl != null)
                        "<img class='ico' src='$iconUrl' alt='' onerror=\"this.style.display='none'\"/>" else ""
                    val ver = t.version?.let { " <span class='ver'>$it</span>" } ?: ""
                    val conf = if (t.confidence < 100) " <span class='badge'>~${t.confidence}%</span>" else ""
                    append("<tr><td>$icon${esc(t.name)}$ver$conf</td><td class='muted'>${esc(t.detectionMethod)}</td></tr>")
                }
                append("</table>")
            }
        }
    }

    fun analysisHtml(): String {
        val findings = SecurityAnalyzer.analyze(signals)
        if (findings.isEmpty()) return "<p class='muted'>No issues flagged</p>"
        return buildString {
            append("<table>")
            for (f in findings) {
                val cls = when (f.severity) { "alert" -> "fail"; "warn" -> "warn"; "ok" -> "pass"; else -> "muted" }
                val mark = when (f.severity) { "alert", "warn" -> "⚠"; "ok" -> "✓"; else -> "ⓘ" }
                append("<tr><td><span class='$cls'>$mark ${esc(f.title)}</span><br><span class='muted'>${esc(f.detail)}</span></td></tr>")
            }
            append("</table>")
        }
    }

    fun headersHtml(): String = buildString {
        append("<table><tr><th>Header</th><th>Value</th><th>Status</th></tr>")
        for ((key, label) in securityHeaders) {
            val value = signals.headers[key]
            val (status, cls) = if (value != null) ("Present" to "pass") else ("Missing" to "fail")
            append("<tr><td>${esc(label)}</td><td class='mono'>${esc(value?.take(160))}</td><td class='$cls'>$status</td></tr>")
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
            for ((name, url) in found) {
                val ic = DevIconMapper.iconUrlFor(name)?.let {
                    "<img class='ico' src='$it' alt='' onerror=\"this.style.display='none'\"/>"
                } ?: ""
                append("<tr><td>$ic${esc(name)}</td><td class='mono'>${esc(url)}</td></tr>")
            }
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

    fun whoisHtml(): String {
        val r = recon?.rdap ?: return "<p class='muted'>WHOIS data not fetched</p>"
        if (r.error != null) return "<p class='muted'>${esc(r.error)}</p>"
        return buildString {
            append("<table>")
            r.registrar?.let { append("<tr><td>Registrar</td><td>${esc(it)}</td></tr>") }
            r.registrant?.let { append("<tr><td>Registrant</td><td>${esc(it)}</td></tr>") }
            r.created?.let { append("<tr><td>Created</td><td class='mono'>${esc(it)}</td></tr>") }
            r.expires?.let { append("<tr><td>Expires</td><td class='mono'>${esc(it)}</td></tr>") }
            r.updated?.let { append("<tr><td>Updated</td><td class='mono'>${esc(it)}</td></tr>") }
            if (r.status.isNotEmpty()) append("<tr><td>Status</td><td class='mono'>${esc(r.status.joinToString(", "))}</td></tr>")
            if (r.nameServers.isNotEmpty()) append("<tr><td>Name servers</td><td class='mono'>${esc(r.nameServers.joinToString(", "))}</td></tr>")
            append("</table>")
        }
    }

    fun dnsHtml(): String {
        val d = recon?.dns ?: return "<p class='muted'>DNS not fetched</p>"
        if (d.error != null) return "<p class='muted'>${esc(d.error)}</p>"
        val groups = listOf(
            "A" to d.aRecords, "CNAME" to d.cnameRecords, "MX" to d.mxRecords,
            "NS" to d.nsRecords, "TXT" to d.txtRecords,
        ).filter { it.second.isNotEmpty() }
        if (groups.isEmpty()) return "<p class='muted'>No DNS records found</p>"
        return buildString {
            append("<table>")
            for ((type, list) in groups) {
                append("<tr><td>${esc(type)}</td><td class='mono'>${list.joinToString("<br>") { esc(it) }}</td></tr>")
            }
            recon?.asn?.let { append("<tr><td>ASN</td><td class='mono'>${esc(it)}</td></tr>") }
            append("</table>")
        }
    }

    fun emailSecHtml(): String {
        val em = recon?.email ?: return "<p class='muted'>Not checked</p>"
        fun row(label: String, pass: Boolean, value: String) =
            "<tr><td><span class='${if (pass) "pass" else "muted"}'>${if (pass) "✓" else "✗"}</span> $label</td><td class='mono'>${esc(value)}</td></tr>"
        return buildString {
            append("<table>")
            append(row("SPF", em.spf != null, em.spf ?: "No SPF record"))
            append(row("DMARC", em.dmarc != null, em.dmarc ?: "No DMARC record"))
            append(row("DKIM", em.dkimSelector != null, em.dkimSelector?.let { "selector: $it" } ?: "No common selector found"))
            append(row("DNSSEC", em.dnssec == true, if (em.dnssec == true) "Signed" else "Not signed"))
            append(row("CAA", em.caa.isNotEmpty(), if (em.caa.isNotEmpty()) em.caa.joinToString("; ") else "No CAA records"))
            recon?.httpsRedirect?.let { append(row("HTTP→HTTPS", it, if (it) "Redirects to HTTPS" else "Does NOT redirect")) }
            append("</table>")
        }
    }

    fun reputationHtml(): String {
        val rep = recon?.reputation ?: return "<p class='muted'>Not checked</p>"
        if (rep.error != null) return "<p class='muted'>Check unavailable (${esc(rep.source)})</p>"
        return if (rep.listed)
            "<p class='fail'>⚠ Listed on ${esc(rep.source)}${rep.detail?.let { " — ${esc(it)}" } ?: ""}</p>"
        else
            "<p class='pass'>✓ No malicious records on ${esc(rep.source)}</p>"
    }

    fun subdomainsHtml(): String {
        val subs = recon?.subdomains ?: emptyList()
        if (subs.isEmpty()) return "<p class='muted'>None found</p>"
        return "<table>" + subs.joinToString("") { "<tr><td class='mono'>${esc(it)}</td></tr>" } + "</table>"
    }

    fun neighborsHtml(): String {
        val r = recon ?: return "<p class='muted'>Not checked</p>"
        val ip = r.dns?.aRecords?.firstOrNull() ?: return "<p class='muted'>No A record</p>"
        val badge = when {
            !r.ipChecked -> "<p class='muted'>Could not determine (reverse-IP lookup unavailable)</p>"
            r.dedicatedIp -> "<p class='pass'>Dedicated IP — only this site detected on ${esc(ip)}</p>"
            else -> "<p>Shared IP — ${r.ipNeighbors.size} sites on ${esc(ip)}</p>"
        }
        val list = if (r.ipNeighbors.isEmpty()) "" else
            "<table>" + r.ipNeighbors.take(100).joinToString("") { "<tr><td class='mono'>${esc(it)}</td></tr>" } + "</table>"
        return badge + list
    }

    fun similarDomainsHtml(): String {
        val sims = recon?.similarDomains ?: emptyList()
        if (sims.isEmpty()) return "<p class='muted'>No registered look-alikes found</p>"
        return "<p class='muted'>Registered look-alike domains (possible typosquats):</p><table>" +
            sims.joinToString("") { "<tr><td class='mono'>${esc(it.domain)}</td></tr>" } + "</table>"
    }

    fun reconFileHtml(label: String, txt: String?): String {
        if (txt.isNullOrBlank()) return "<p class='muted'>$label not found</p>"
        return "<pre class='code'>${esc(txt.take(20_000))}</pre>"
    }

    val statusCode = signals.headers["_status"] ?: "—"

    val bg      = if (darkMode) "#0a0a0a" else "#ffffff"
    val fg      = if (darkMode) "#e8e8e8" else "#1a1a1a"
    val mut     = if (darkMode) "#8a8a8a" else "#999999"
    val bord    = if (darkMode) "#232323" else "#e5e5e5"
    val rowBord = if (darkMode) "#161616" else "#f0f0f0"
    val accent  = if (darkMode) "#00e5c8" else "#0a7f70"
    val codeBg  = if (darkMode) "#111318" else "#f6f8fa"

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
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:$bg;color:$fg;padding:28px;font-size:14px;line-height:1.5;max-width:900px;margin:0 auto}
.header{border-bottom:3px solid $accent;padding-bottom:16px;margin-bottom:28px}
.brand{color:$accent;font-size:30px;font-weight:800;letter-spacing:.12em}
.url{color:$fg;font-size:13px;margin-top:6px;word-break:break-all;font-family:'Courier New',monospace}
.meta{color:$mut;font-size:12px;margin-top:8px}
h2{color:$accent;font-size:13px;font-weight:700;text-transform:uppercase;letter-spacing:.08em;margin:30px 0 10px;padding-bottom:5px;border-bottom:1px solid $bord}
.appendix{margin-top:44px;border-top:2px dashed $bord;padding-top:6px}
table{width:100%;border-collapse:collapse;margin-bottom:10px}
th{text-align:left;color:$mut;font-size:11px;text-transform:uppercase;letter-spacing:.05em;padding:6px 8px;border-bottom:1px solid $bord}
td{padding:6px 8px;border-bottom:1px solid $rowBord;vertical-align:top;word-break:break-word}
.ico{width:15px;height:15px;vertical-align:-2px;margin-right:7px}
.mono{font-family:'Courier New',monospace;font-size:12px;color:$fg}
.muted{color:$mut}
.pass{color:#1a9d4e;font-weight:600}
.fail{color:#e5484d;font-weight:600}
.warn{color:#d9920a;font-weight:600}
.ver{color:$accent;font-size:12px;font-weight:600}
.badge{background:$codeBg;color:$mut;font-size:11px;padding:1px 5px;border-radius:4px}
.section-label{color:$mut;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.06em;margin:14px 0 4px}
.code{background:$codeBg;border:1px solid $bord;border-radius:6px;padding:12px;font-family:'Courier New',monospace;font-size:11px;white-space:pre-wrap;word-break:break-all;overflow-x:auto;color:$fg}
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

<h2>Security Analysis</h2>
${analysisHtml()}

<h2>Detected APIs</h2>
${apisHtml()}

<h2>Parameters</h2>
${paramsHtml()}

<h2>Interesting Strings</h2>
${stringsHtml()}

<h2>Social Links</h2>
${socialHtml()}

<h2>WHOIS / Registrar</h2>
${whoisHtml()}

<h2>DNS Records</h2>
${dnsHtml()}

<h2>DNS / Email Security</h2>
${emailSecHtml()}

<h2>Host Reputation</h2>
${reputationHtml()}

<h2>Subdomains (${recon?.subdomains?.size ?: 0})</h2>
${subdomainsHtml()}

<h2>IP Neighbors</h2>
${neighborsHtml()}

<h2>Similar Domains</h2>
${similarDomainsHtml()}

<h2>TLS / Certificate</h2>
${tlsHtml()}

<h2>Cookies (${signals.richCookies.size})</h2>
${cookiesHtml()}

<div class="appendix">
<h2>Network Requests (${signals.networkRequests.size})</h2>
${networkHtml()}

<h2>Source Files</h2>
${sourceFilesHtml()}

<h2>robots.txt</h2>
${reconFileHtml("robots.txt", recon?.files?.robotsTxt)}

<h2>security.txt</h2>
${reconFileHtml("security.txt", recon?.files?.securityTxt)}

<h2>sitemap.xml</h2>
${reconFileHtml("sitemap.xml", recon?.files?.sitemapXml)}

<h2>Response Headers</h2>
<table>${signals.headers.entries.filter { it.key != "_status" }.sortedBy { it.key }.joinToString("") { "<tr><td class='mono'>${esc(it.key)}</td><td class='mono'>${esc(it.value.take(220))}</td></tr>" }}</table>

<h2>Request Headers</h2>
<table>${signals.requestHeaders.entries.sortedBy { it.key }.joinToString("") { "<tr><td class='mono'>${esc(it.key)}</td><td class='mono'>${esc(it.value.take(220))}</td></tr>" }}</table>

$optionalSections
</div>

</body>
</html>"""
}
