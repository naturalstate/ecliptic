package com.ecliptic.scanner

/**
 * Pattern-matches interesting strings and parameters out of page/source text.
 * Pure + synchronous so it can run on any captured content (HTML, JS, CSS) and be
 * unit-tested. Heuristic by nature — some matches (e.g. IPs that are really version
 * numbers) may be approximate; callers present them as "found", not "confirmed".
 */
object Extractor {

    /**
     * severity: "alert" (secrets), "warn" (tokens), "info" (emails/comments/IPs).
     * origin: where it was found, e.g. "HTML" or a script filename — for provenance.
     */
    data class StringFinding(val category: String, val value: String, val severity: String = "info", val origin: String = "")

    private val EMAIL = Regex("""[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}""")
    private val JWT = Regex("""eyJ[A-Za-z0-9_\-]{8,}\.[A-Za-z0-9_\-]{8,}\.[A-Za-z0-9_\-]{6,}""")
    private val IPV4 = Regex("""\b(?:(?:25[0-5]|2[0-4]\d|1?\d?\d)\.){3}(?:25[0-5]|2[0-4]\d|1?\d?\d)\b""")

    // name → regex, most specific first. "alert" = likely real secret.
    private val KEY_PATTERNS: List<Triple<String, Regex, String>> = listOf(
        Triple("Private Key", Regex("""-----BEGIN (?:RSA |EC |DSA |OPENSSH |PGP )?PRIVATE KEY-----"""), "alert"),
        Triple("AWS Access Key", Regex("""\bAKIA[0-9A-Z]{16}\b"""), "alert"),
        Triple("Google API Key", Regex("""\bAIza[0-9A-Za-z_\-]{35}\b"""), "alert"),
        Triple("GitHub Token", Regex("""\b(?:ghp|gho|ghu|ghs|ghr)_[0-9A-Za-z]{36}\b"""), "alert"),
        Triple("Slack Token", Regex("""\bxox[baprs]-[0-9A-Za-z\-]{10,48}\b"""), "alert"),
        Triple("Stripe Secret Key", Regex("""\bsk_live_[0-9A-Za-z]{20,}\b"""), "alert"),
        Triple("Stripe Publishable Key", Regex("""\bpk_live_[0-9A-Za-z]{20,}\b"""), "warn"),
        Triple("Google OAuth Client", Regex("""\b[0-9]+-[0-9A-Za-z_]{20,}\.apps\.googleusercontent\.com\b"""), "info"),
        Triple("Bearer Token", Regex("""(?i)\bbearer\s+[A-Za-z0-9\-._~+/]{20,}={0,2}"""), "warn"),
        Triple("Secret Assignment", Regex("""(?i)(?:api[_-]?key|secret|token|passwd|password|auth[_-]?token|access[_-]?key)["']?\s*[:=]\s*["'][A-Za-z0-9/_+\-=.]{12,}["']"""), "warn"),
    )

    private val COMMENT_HTML = Regex("""<!--(.*?)-->""", RegexOption.DOT_MATCHES_ALL)
    private val COMMENT_BLOCK = Regex("""/\*(.*?)\*/""", RegexOption.DOT_MATCHES_ALL)

    private val PARAM_IN_URL = Regex("""[?&]([A-Za-z0-9_.\-\[\]]{1,40})=([^&"'\s<>]{0,60})""")
    private val INPUT_NAME = Regex("""(?i)<(?:input|select|textarea)\b[^>]*\bname\s*=\s*["']([^"']{1,50})["']""")

    fun findStrings(text: String, includeComments: Boolean = true, origin: String = ""): List<StringFinding> {
        if (text.isBlank()) return emptyList()
        val out = LinkedHashSet<StringFinding>()

        for ((name, re, sev) in KEY_PATTERNS) {
            for (m in re.findAll(text).take(30)) out += StringFinding(name, m.value.trim().take(140), sev, origin)
        }
        for (m in JWT.findAll(text).take(40)) out += StringFinding("JWT", m.value.take(160), "warn", origin)
        for (m in EMAIL.findAll(text).take(100)) out += StringFinding("Email", m.value, origin = origin)
        IPV4.findAll(text).map { it.value }
            .filter { !it.startsWith("0.") && !it.startsWith("255.255.255") }
            .distinct().take(60)
            .forEach { out += StringFinding("IP Address", it, origin = origin) }

        if (includeComments) {
            fun addComment(raw: String) {
                val c = raw.trim().replace(Regex("""\s+"""), " ")
                if (c.length in 4..240) out += StringFinding("Comment", c, origin = origin)
            }
            COMMENT_HTML.findAll(text).take(80).forEach { addComment(it.groupValues[1]) }
            COMMENT_BLOCK.findAll(text).take(80).forEach { addComment(it.groupValues[1]) }
        }
        return out.toList()
    }

    /** Distinct parameter names (with a sample value if seen) from URLs + form fields. */
    fun findParams(text: String): List<Pair<String, String>> {
        if (text.isBlank()) return emptyList()
        val map = LinkedHashMap<String, String>()
        for (m in PARAM_IN_URL.findAll(text).take(400)) {
            val k = m.groupValues[1]
            val v = m.groupValues[2]
            if (k.isNotBlank()) map.merge(k, v) { old, new -> old.ifBlank { new } }
        }
        for (m in INPUT_NAME.findAll(text).take(200)) {
            val k = m.groupValues[1]
            if (k.isNotBlank()) map.putIfAbsent(k, "")
        }
        return map.entries.map { it.key to it.value }
    }
}
