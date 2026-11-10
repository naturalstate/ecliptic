package com.ecliptic.scanner

/**
 * Pure, synchronous security analysis of a completed scan. Flags concrete
 * misconfigurations (weak CSP / framing, CORS, missing SRI, mixed content, weak
 * HSTS, info disclosure) rather than reducing anything to a single "grade". Each
 * finding is actionable and cites what was observed. Used by the Security tab and
 * the report; no network, so it can run anywhere and be unit-tested.
 */
object SecurityAnalyzer {

    /** severity: "alert" (red, likely exploitable), "warn" (amber), "ok" (green), "info". */
    data class SecFinding(
        val title: String,
        val detail: String,
        val severity: String,
    )

    fun analyze(signals: RawSignals): List<SecFinding> {
        val out = mutableListOf<SecFinding>()
        val h = signals.headers.mapKeys { it.key.lowercase() }
        val isHttps = signals.finalUrl.startsWith("https", ignoreCase = true)
        val csp = h["content-security-policy"]
        val xfo = h["x-frame-options"]?.trim()?.lowercase()

        // ── Clickjacking / framing ────────────────────────────────────────────
        val cspFrameAncestors = csp?.contains("frame-ancestors", ignoreCase = true) == true
        when {
            xfo == null && !cspFrameAncestors ->
                out += SecFinding("Framing not restricted", "No X-Frame-Options and no CSP frame-ancestors — the page can be embedded in an iframe (clickjacking risk).", "warn")
            xfo != null && xfo.startsWith("allow-from") ->
                out += SecFinding("Deprecated X-Frame-Options", "ALLOW-FROM is obsolete and ignored by modern browsers. Use CSP frame-ancestors instead.", "warn")
            xfo != null && xfo !in setOf("deny", "sameorigin") && !cspFrameAncestors ->
                out += SecFinding("Unexpected X-Frame-Options", "Value \"$xfo\" is not DENY or SAMEORIGIN.", "warn")
        }

        // ── Content-Security-Policy quality ───────────────────────────────────
        if (csp == null) {
            out += SecFinding("No Content-Security-Policy", "No CSP header — the page has no defense-in-depth against injected scripts (XSS).", "warn")
        } else {
            val c = csp.lowercase()
            if (c.contains("'unsafe-inline'"))
                out += SecFinding("CSP allows 'unsafe-inline'", "Inline scripts/styles are permitted, which largely defeats the CSP's XSS protection.", "warn")
            if (c.contains("'unsafe-eval'"))
                out += SecFinding("CSP allows 'unsafe-eval'", "eval() and similar are permitted, weakening the CSP.", "warn")
            if (Regex("""(?:^|;)\s*default-src[^;]*\*""").containsMatchIn(c) || Regex("""(?:^|;)\s*script-src[^;]*\*""").containsMatchIn(c))
                out += SecFinding("CSP uses wildcard source", "A '*' source allows scripts/content from any origin, undermining the policy.", "warn")
        }

        // ── HSTS ──────────────────────────────────────────────────────────────
        if (isHttps) {
            val hsts = h["strict-transport-security"]
            if (hsts == null) {
                out += SecFinding("No HSTS", "No Strict-Transport-Security header — browsers may still connect over HTTP, enabling downgrade/MITM.", "warn")
            } else {
                val maxAge = Regex("""max-age\s*=\s*(\d+)""", RegexOption.IGNORE_CASE).find(hsts)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                val subs = hsts.contains("includeSubDomains", ignoreCase = true)
                val preload = hsts.contains("preload", ignoreCase = true)
                when {
                    maxAge < 15_768_000L -> out += SecFinding("Short HSTS max-age", "max-age is ${maxAge}s (< 6 months). Browsers recommend at least 1 year (31536000).", "info")
                    preload && subs && maxAge >= 31_536_000L -> out += SecFinding("HSTS preload-eligible", "Long max-age, includeSubDomains and preload are all set.", "ok")
                    !subs -> out += SecFinding("HSTS without includeSubDomains", "Subdomains are not covered by HSTS.", "info")
                }
            }
        }

        // ── CORS ──────────────────────────────────────────────────────────────
        val acao = h["access-control-allow-origin"]
        val acac = h["access-control-allow-credentials"]?.trim()?.equals("true", ignoreCase = true) == true
        if (acao != null) {
            when {
                acao.trim() == "*" && acac ->
                    out += SecFinding("Invalid, permissive CORS", "Access-Control-Allow-Origin: * together with Allow-Credentials: true is invalid and signals a misconfigured CORS policy.", "alert")
                acao.trim() == "*" ->
                    out += SecFinding("Wildcard CORS", "Access-Control-Allow-Origin: * lets any site read responses from this origin.", "warn")
                acac ->
                    out += SecFinding("Credentialed CORS", "Access-Control-Allow-Credentials: true with a reflected/explicit origin ($acao) — verify the origin is strictly validated.", "info")
            }
        }

        // ── Mixed content (https page loading http assets) ─────────────────────
        if (isHttps) {
            val assets = signals.scriptUrls + signals.cssUrls
            val mixed = assets.filter { it.startsWith("http://", ignoreCase = true) }
            val inlineMixed = Regex("""(?:src|href)\s*=\s*["']http://""", RegexOption.IGNORE_CASE).findAll(signals.htmlSource).count()
            val total = mixed.size + inlineMixed
            if (total > 0)
                out += SecFinding("Mixed content", "$total resource(s) are loaded over plain HTTP on an HTTPS page — these can be blocked or tampered with.", "warn")
        }

        // ── Subresource Integrity (SRI) on third-party scripts/styles ──────────
        val host = runCatching { java.net.URI(signals.finalUrl).host?.removePrefix("www.") }.getOrNull()
        if (host != null) {
            val tagRe = Regex("""<(script|link)\b[^>]*>""", RegexOption.IGNORE_CASE)
            var thirdParty = 0; var missing = 0
            for (m in tagRe.findAll(signals.htmlSource)) {
                val tag = m.value
                val isScript = m.groupValues[1].equals("script", true)
                if (!isScript && !tag.contains("stylesheet", ignoreCase = true)) continue
                val urlAttr = Regex("""(?:src|href)\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(tag)?.groupValues?.get(1) ?: continue
                if (!urlAttr.startsWith("http", ignoreCase = true)) continue
                val tagHost = runCatching { java.net.URI(urlAttr).host?.removePrefix("www.") }.getOrNull() ?: continue
                if (tagHost.equals(host, ignoreCase = true)) continue
                thirdParty++
                if (!tag.contains("integrity=", ignoreCase = true)) missing++
            }
            if (missing > 0)
                out += SecFinding("Third-party resources without SRI", "$missing of $thirdParty cross-origin script/style tag(s) lack a Subresource Integrity hash — a compromised CDN could inject code.", "info")
        }

        // ── Information disclosure ────────────────────────────────────────────
        h["x-powered-by"]?.let { out += SecFinding("X-Powered-By disclosed", "Reveals backend stack: \"$it\".", "info") }
        h["server"]?.takeIf { it.any(Char::isDigit) }?.let { out += SecFinding("Server version disclosed", "Server header reveals a version: \"$it\".", "info") }

        // Severity order: alert, warn, info, ok
        val rank = mapOf("alert" to 0, "warn" to 1, "info" to 2, "ok" to 3)
        return out.sortedBy { rank[it.severity] ?: 4 }
    }
}
