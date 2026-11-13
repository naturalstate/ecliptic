package com.ecliptic.fingerprint

/**
 * Plain-language definitions + canonical reference links for the security headers and
 * concepts Ecliptic surfaces. Lets the user tap a header (or an analysis finding) to
 * learn what it is and where it's defined. Static, offline; links open in a browser.
 *
 * Sources are the canonical ones: MDN for HTTP headers, OWASP / W3C where more apt.
 */
object SecurityReference {

    data class Ref(
        val title: String,
        val summary: String,
        val url: String,
        val source: String = "MDN Web Docs",
    )

    private const val MDN = "https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/"

    // Keyed by lowercase HTTP header name.
    private val headers: Map<String, Ref> = mapOf(
        "content-security-policy" to Ref(
            "Content-Security-Policy",
            "An allow-list that tells the browser which sources of scripts, styles, images and other content are trusted. It's the strongest defence against cross-site scripting (XSS) and content injection: anything not on the list is blocked.",
            MDN + "Content-Security-Policy",
        ),
        "strict-transport-security" to Ref(
            "Strict-Transport-Security (HSTS)",
            "Forces browsers to connect over HTTPS only for a set time, even if the user types http:// or clicks an insecure link. Prevents protocol-downgrade and SSL-stripping attacks. `includeSubDomains` and `preload` widen the protection.",
            MDN + "Strict-Transport-Security",
        ),
        "x-frame-options" to Ref(
            "X-Frame-Options",
            "Controls whether the page may be embedded in a <frame>/<iframe>. Set to DENY or SAMEORIGIN to stop clickjacking, where an attacker frames your site invisibly over their own. Superseded by CSP's frame-ancestors, but still honoured.",
            MDN + "X-Frame-Options",
        ),
        "x-content-type-options" to Ref(
            "X-Content-Type-Options",
            "The single value `nosniff` stops the browser from guessing (\"MIME sniffing\") a response's content type. Without it, a file served as text could be executed as script, enabling some XSS and drive-by attacks.",
            MDN + "X-Content-Type-Options",
        ),
        "referrer-policy" to Ref(
            "Referrer-Policy",
            "Decides how much of the current URL is sent in the Referer header when the user follows a link or loads a resource. Tighter values (e.g. strict-origin-when-cross-origin) prevent leaking paths and query strings to third parties.",
            MDN + "Referrer-Policy",
        ),
        "permissions-policy" to Ref(
            "Permissions-Policy",
            "Selectively enables or disables browser features (camera, microphone, geolocation, autoplay, etc.) for the page and any iframes it loads. Shrinks the attack surface and limits what embedded third-party content can do.",
            MDN + "Permissions-Policy",
        ),
        "x-xss-protection" to Ref(
            "X-XSS-Protection",
            "A legacy switch for the old built-in XSS filters in Internet Explorer and early Chrome. Modern browsers ignore it, and it could itself introduce bugs, so current guidance is to set `0` and rely on a strong Content-Security-Policy instead.",
            MDN + "X-XSS-Protection",
        ),
        "cross-origin-opener-policy" to Ref(
            "Cross-Origin-Opener-Policy (COOP)",
            "Isolates your page into its own browsing-context group so cross-origin windows it opens (or that opened it) can't share references. With COEP it enables cross-origin isolation, a prerequisite for powerful APIs and a defence against Spectre-type attacks.",
            MDN + "Cross-Origin-Opener-Policy",
        ),
        "cross-origin-embedder-policy" to Ref(
            "Cross-Origin-Embedder-Policy (COEP)",
            "Requires every cross-origin resource the page loads to explicitly opt in (via CORP or CORS). Together with COOP it turns on cross-origin isolation, mitigating side-channel attacks like Spectre.",
            MDN + "Cross-Origin-Embedder-Policy",
        ),
        "cross-origin-resource-policy" to Ref(
            "Cross-Origin-Resource-Policy (CORP)",
            "Lets a resource declare who may embed it (same-origin, same-site or cross-origin). Blocks other sites from loading your resources into their pages, a defence against cross-origin leaks and side-channel attacks.",
            MDN + "Cross-Origin-Resource-Policy",
        ),
    )

    // Concepts referenced by the analysis findings (keyed by a lowercase keyword).
    private val concepts: List<Pair<String, Ref>> = listOf(
        "clickjack" to (headers["x-frame-options"]!!),
        "frame-ancestors" to Ref(
            "CSP: frame-ancestors",
            "The CSP directive that controls which origins may embed the page in a frame. The modern replacement for X-Frame-Options, and it supports multiple allowed origins.",
            "https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/frame-ancestors",
        ),
        "cors" to Ref(
            "CORS (Access-Control-Allow-Origin)",
            "Cross-Origin Resource Sharing controls which other origins may read responses from your server via JavaScript. A wildcard `*`, especially combined with credentials, can expose private data to any site.",
            MDN + "Access-Control-Allow-Origin",
            "MDN Web Docs",
        ),
        "sri" to Ref(
            "Subresource Integrity (SRI)",
            "An `integrity` hash on a <script> or <link> so the browser refuses the file if a CDN is compromised and the bytes don't match. Without it, a hacked third-party host can inject code into your page.",
            "https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity",
        ),
        "subresource integrity" to Ref(
            "Subresource Integrity (SRI)",
            "An `integrity` hash on a <script> or <link> so the browser refuses the file if a CDN is compromised and the bytes don't match.",
            "https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity",
        ),
        "mixed content" to Ref(
            "Mixed content",
            "An HTTPS page that also loads some resources over plain HTTP. The insecure parts can be read or tampered with in transit, undermining the lock icon. Browsers block or upgrade active mixed content.",
            "https://developer.mozilla.org/en-US/docs/Web/Security/Mixed_content",
        ),
        "hsts" to (headers["strict-transport-security"]!!),
        "disclosure" to Ref(
            "Information disclosure (Server / X-Powered-By)",
            "Headers like Server and X-Powered-By can reveal exact software versions, helping an attacker target known vulnerabilities. Best practice is to remove or genericise them.",
            MDN + "X-Powered-By",
        ),
    )

    /** Reference for an exact HTTP header name (case-insensitive). */
    fun forHeader(headerName: String): Ref? = headers[headerName.trim().lowercase()]

    /** Best-effort reference for a finding title / free text that names a header or concept. */
    fun forText(text: String): Ref? {
        val t = text.lowercase()
        headers.entries.firstOrNull { (k, ref) -> t.contains(k) || t.contains(ref.title.lowercase()) }?.let { return it.value }
        return concepts.firstOrNull { (kw, _) -> t.contains(kw) }?.second
    }
}
