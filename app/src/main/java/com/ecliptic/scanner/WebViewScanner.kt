package com.ecliptic.scanner

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.Inet4Address
import java.net.InetAddress
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

private const val SCAN_TIMEOUT_MS = 20_000L

private val ipv4Dns = object : Dns {
    override fun lookup(hostname: String): List<InetAddress> =
        Dns.SYSTEM.lookup(hostname).filter { it is Inet4Address }.ifEmpty {
            Dns.SYSTEM.lookup(hostname)
        }
}

internal val httpClient = OkHttpClient.Builder()
    .connectTimeout(8, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .followRedirects(true)
    .dns(ipv4Dns)
    .build()

// Builds the full harvest JS injected after onPageFinished
private fun buildHarvestJs(jsPaths: Set<String>, domChecksJson: String): String {
    val pathsJson = buildJsonStringArray(jsPaths)

    return """
(function() {
    function safe(fn) { try { return fn(); } catch(e) { return null; } }
    function ver(obj, key) { return safe(function() { return obj[key] || 'detected'; }); }

    // --- Hardcoded globals (legacy + quick checks) ---
    var globals = {
        react:       typeof window.React !== 'undefined' ? ver(window.React, 'version') : false,
        reactDom:    typeof window.ReactDOM !== 'undefined',
        next:        typeof window.__NEXT_DATA__ !== 'undefined',
        gatsby:      typeof window.___gatsby !== 'undefined',
        vue:         typeof window.Vue !== 'undefined' ? ver(window.Vue, 'version') : false,
        nuxt:        typeof window.__NUXT__ !== 'undefined',
        angular:     typeof window.angular !== 'undefined' ? safe(function(){ return window.angular.version.full; }) || 'detected' : false,
        ngVersion:   safe(function(){ return document.querySelector('[ng-version]').getAttribute('ng-version'); }),
        jquery:      typeof window.jQuery !== 'undefined' ? ver(window.jQuery.fn, 'jquery') : false,
        svelte:      typeof window.__svelte !== 'undefined',
        alpine:      typeof window.Alpine !== 'undefined' ? ver(window.Alpine, 'version') : false,
        ember:       typeof window.Ember !== 'undefined' ? ver(window.Ember, 'VERSION') : false,
        backbone:    typeof window.Backbone !== 'undefined' ? ver(window.Backbone, 'VERSION') : false,
        wordpress:   typeof window.wp !== 'undefined',
        shopify:     typeof window.Shopify !== 'undefined' ? safe(function(){ return window.Shopify.theme ? window.Shopify.theme.name : 'detected'; }) || 'detected' : false,
        woocommerce: typeof window.wc !== 'undefined' || typeof window.woocommerce_params !== 'undefined',
        gtag:        typeof window.gtag !== 'undefined',
        ga:          typeof window.ga !== 'undefined',
        gtm:         typeof window.google_tag_manager !== 'undefined',
        fbPixel:     typeof window.fbq !== 'undefined',
        hotjar:      typeof window.hj !== 'undefined',
        mixpanel:    typeof window.mixpanel !== 'undefined',
        segment:     typeof window.analytics !== 'undefined' && typeof window.analytics.load === 'function',
        posthog:     typeof window.posthog !== 'undefined',
        amplitude:   typeof window.amplitude !== 'undefined',
        hubspot:     typeof window.hbspt !== 'undefined' || typeof window._hsq !== 'undefined',
        intercom:    typeof window.Intercom !== 'undefined',
        drift:       typeof window.drift !== 'undefined',
        zendesk:     typeof window.zE !== 'undefined',
        stripe:      typeof window.Stripe !== 'undefined',
        recaptcha:   typeof window.grecaptcha !== 'undefined',
        webpack:     typeof window.__webpack_require__ !== 'undefined',
        cloudflare:  typeof window.__cfBeacon !== 'undefined',
        datadog:     typeof window.DD_RUM !== 'undefined',
        sentry:      typeof window.Sentry !== 'undefined',
        onetrust:    typeof window.OneTrust !== 'undefined'
    };

    // --- Dynamic JS property probe (from fingerprint DB) ---
    var jsProbe = {};
    var jsPaths = $pathsJson;
    for (var i = 0; i < jsPaths.length; i++) {
        try {
            var parts = jsPaths[i].split('.');
            var obj = window;
            for (var j = 0; j < parts.length; j++) {
                if (obj == null || (typeof obj !== 'object' && typeof obj !== 'function')) { obj = undefined; break; }
                obj = obj[parts[j]];
            }
            if (typeof obj !== 'undefined' && obj !== null && obj !== false) {
                jsProbe[jsPaths[i]] = (typeof obj === 'object' || typeof obj === 'function') ? 'detected' : String(obj).substring(0, 500);
            }
        } catch(e) {}
    }

    // --- DOM selector probe (attribute/text pattern verification) ---
    var domFound = [];
    var domChecks = $domChecksJson;
    for (var sel in domChecks) {
        try {
            var check = domChecks[sel];
            var hasAttrs = Object.keys(check.a).length > 0;
            if (hasAttrs) {
                var el = document.querySelector(sel);
                if (!el) continue;
                var attrOk = true;
                for (var attr in check.a) {
                    var attrVal = el.getAttribute(attr);
                    if (!attrVal || !(new RegExp(check.a[attr], 'i').test(attrVal))) { attrOk = false; break; }
                }
                if (!attrOk) continue;
            } else if (!document.querySelector(sel)) {
                continue;
            }
            if (check.t) {
                var textRe = new RegExp(check.t, 'i');
                var textEls = document.querySelectorAll(sel);
                var textOk = false;
                for (var k = 0; k < textEls.length && k < 20; k++) {
                    if (textRe.test(textEls[k].textContent || '')) { textOk = true; break; }
                }
                if (!textOk) continue;
            }
            domFound.push(sel);
        } catch(e) {}
    }

    // --- Meta tags ---
    var metas = {};
    var metaEls = document.getElementsByTagName('meta');
    for (var i = 0; i < metaEls.length; i++) {
        var name = metaEls[i].getAttribute('name') || metaEls[i].getAttribute('property') || metaEls[i].getAttribute('http-equiv');
        var content = metaEls[i].getAttribute('content');
        if (name && content) metas[name.toLowerCase()] = content.substring(0, 200);
    }

    // --- Script URLs ---
    var scriptSrcs = [];
    var scriptEls = document.getElementsByTagName('script');
    for (var i = 0; i < scriptEls.length && scriptSrcs.length < 60; i++) {
        if (scriptEls[i].src) scriptSrcs.push(scriptEls[i].src);
    }

    // --- CSS URLs ---
    var cssUrls = [];
    var linkEls = document.getElementsByTagName('link');
    for (var i = 0; i < linkEls.length && cssUrls.length < 20; i++) {
        var rel = linkEls[i].getAttribute('rel') || '';
        if (rel.indexOf('stylesheet') !== -1 && linkEls[i].href) cssUrls.push(linkEls[i].href);
    }

    // --- HTML attributes ---
    var htmlAttrs = {};
    var htmlEl = document.documentElement;
    for (var i = 0; i < htmlEl.attributes.length; i++) {
        htmlAttrs[htmlEl.attributes[i].name] = htmlEl.attributes[i].value;
    }

    // --- HTML source ---
    var htmlSource = '';
    try { htmlSource = document.documentElement.outerHTML.substring(0, 500000); } catch(e) {}

    // --- JS-accessible cookies ---
    var cookies = {};
    try {
        var cstr = document.cookie;
        if (cstr) {
            var cparts = cstr.split(';');
            for (var i = 0; i < cparts.length; i++) {
                var idx = cparts[i].indexOf('=');
                if (idx > 0) {
                    cookies[cparts[i].substring(0, idx).trim().toLowerCase()] = cparts[i].substring(idx + 1).trim().substring(0, 500);
                }
            }
        }
    } catch(e) {}

    // --- DOM tree ---
    var domTreeJson = '';
    try {
        function domNode(el, dep) {
            if (!el || dep > 10) return null;
            var tag = (el.tagName || '').toLowerCase();
            if (!tag || ['script','style','svg','noscript','head'].indexOf(tag) >= 0) return null;
            var n = {t: tag};
            if (el.id) n.id = el.id.substring(0, 50);
            var cls = typeof el.className === 'string' ? el.className.trim() : '';
            if (cls) n.c = cls.substring(0, 80);
            var attrKeys = ['href','src','type','name','role','aria-label','action','method','rel'];
            var attrs = {};
            for (var ai = 0; ai < attrKeys.length; ai++) {
                var av = el.getAttribute ? el.getAttribute(attrKeys[ai]) : null;
                if (av) attrs[attrKeys[ai]] = av.substring(0, 80);
            }
            if (Object.keys(attrs).length > 0) n.a = attrs;
            var kids = [];
            var ch = el.children;
            var lim = Math.min(ch.length, 200);
            for (var ci = 0; ci < lim; ci++) {
                var child = domNode(ch[ci], dep + 1);
                if (child) kids.push(child);
            }
            if (kids.length > 0) n.ch = kids;
            if (ch.length > 200) n.more = ch.length - 200;
            return n;
        }
        var dt = domNode(document.body, 0);
        if (dt) { var dts = JSON.stringify(dt); if (dts.length <= 2000000) domTreeJson = dts; }
    } catch(e) {}

    return JSON.stringify({
        globals:    globals,
        jsProbe:    jsProbe,
        domFound:   domFound,
        metaTags:   metas,
        scriptUrls: scriptSrcs,
        cssUrls:    cssUrls,
        bodyClasses: document.body ? document.body.className.substring(0, 500) : '',
        htmlAttrs:  htmlAttrs,
        htmlSource: htmlSource,
        cookies:    cookies,
        title:      document.title,
        finalUrl:   window.location.href,
        domTree:    domTreeJson
    });
})()
""".trimIndent()
}

private fun buildJsonStringArray(items: Set<String>): String {
    if (items.isEmpty()) return "[]"
    val sb = StringBuilder("[")
    var first = true
    for (item in items) {
        if (!first) sb.append(",")
        first = false
        sb.append("\"")
        sb.append(item.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", ""))
        sb.append("\"")
    }
    sb.append("]")
    return sb.toString()
}

suspend fun fetchTextContent(url: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                response.body?.string()?.take(500_000) ?: "// Empty response"
            }
        } catch (e: Exception) {
            "// Failed to load: ${e.message}"
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun configureWebView(webView: WebView) {
    webView.settings.apply {
        javaScriptEnabled = true
        allowFileAccess = false
        allowContentAccess = false
        setGeolocationEnabled(false)
        @Suppress("DEPRECATION")
        saveFormData = false
        domStorageEnabled = true
        setSupportZoom(false)
        builtInZoomControls = false
    }
    WebView.setWebContentsDebuggingEnabled(false)
}

suspend fun scanUrl(
    webView: WebView,
    rawUrl: String,
    jsPaths: Set<String> = emptySet(),
    domChecksJson: String = "{}",
    method: String = "GET",
    customHeaders: Map<String, String> = emptyMap(),
    requestBody: String = "",
    onStage: (String) -> Unit = {},
): RawSignals {
    val url = rawUrl.trim().let {
        if (!it.startsWith("http://") && !it.startsWith("https://")) "https://$it" else it
    }

    onStage("Fetching headers…")
    val fetched = withContext(Dispatchers.IO) { fetchHeaders(url, method, customHeaders, requestBody) }
    val harvestJs = buildHarvestJs(jsPaths, domChecksJson)

    return try {
        withTimeout(SCAN_TIMEOUT_MS) {
            withContext(Dispatchers.Main) {
                onStage("Loading page…")
                val json = loadAndInject(webView, url, harvestJs)
                val httpOnlyCookies = parseCookieHeader(CookieManager.getInstance().getCookie(url) ?: "")
                parseSignals(url, fetched.request, fetched.response, json, httpOnlyCookies, fetched.tls, fetched.richCookies)
            }
        }
    } catch (e: TimeoutCancellationException) {
        RawSignals(
            url = url, finalUrl = url,
            requestHeaders = fetched.request, headers = fetched.response,
            globals = emptyMap(), metaTags = emptyMap(),
            scriptUrls = emptyList(), cssUrls = emptyList(),
            bodyClasses = emptyList(), htmlAttrs = emptyMap(),
            htmlSource = "", title = "", tls = fetched.tls,
            error = "Timed out after ${SCAN_TIMEOUT_MS / 1000}s"
        )
    }
}

private suspend fun loadAndInject(webView: WebView, url: String, js: String): String =
    suspendCancellableCoroutine { cont ->
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                view.evaluateJavascript(js) { result ->
                    if (cont.isActive) cont.resume(result ?: "null")
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError,
            ) {
                if (request.isForMainFrame && cont.isActive) {
                    cont.resume("""{"error":"${error.description}"}""")
                }
            }
        }
        webView.loadUrl(url)
    }

private fun parseCookieHeader(cookieHeader: String): Map<String, String> {
    if (cookieHeader.isBlank()) return emptyMap()
    return buildMap {
        cookieHeader.split(";").forEach { part ->
            val idx = part.indexOf("=")
            if (idx > 0) {
                put(part.substring(0, idx).trim().lowercase(), part.substring(idx + 1).trim())
            }
        }
    }
}

private fun parseSignals(
    url: String,
    requestHeaders: Map<String, String>,
    responseHeaders: Map<String, String>,
    json: String,
    httpOnlyCookies: Map<String, String>,
    tls: TlsResult? = null,
    richCookies: List<CookieInfo> = emptyList(),
): RawSignals {
    return try {
        val trimmed = json.trim().removeSurrounding("\"").replace("\\\"", "\"").replace("\\\\", "\\")
        val root = JSONObject(trimmed)

        val globalsObj = root.optJSONObject("globals") ?: JSONObject()
        val globals = mutableMapOf<String, Any?>()
        globalsObj.keys().forEach { key ->
            val v = globalsObj.get(key)
            if (v != false && v != JSONObject.NULL) globals[key] = v
        }

        val jsProbeObj = root.optJSONObject("jsProbe") ?: JSONObject()
        val jsProbe = mutableMapOf<String, String>()
        jsProbeObj.keys().forEach { jsProbe[it] = jsProbeObj.getString(it) }

        val domFoundArr = root.optJSONArray("domFound")
        val domFound = if (domFoundArr != null) {
            (0 until domFoundArr.length()).map { domFoundArr.getString(it) }.toSet()
        } else emptySet()

        val metaObj = root.optJSONObject("metaTags") ?: JSONObject()
        val metas = mutableMapOf<String, String>()
        metaObj.keys().forEach { metas[it] = metaObj.getString(it) }

        val htmlAttrsObj = root.optJSONObject("htmlAttrs") ?: JSONObject()
        val htmlAttrs = mutableMapOf<String, String>()
        htmlAttrsObj.keys().forEach { htmlAttrs[it] = htmlAttrsObj.getString(it) }

        val cookiesObj = root.optJSONObject("cookies") ?: JSONObject()
        val jsCookies = mutableMapOf<String, String>()
        cookiesObj.keys().forEach { jsCookies[it] = cookiesObj.getString(it) }
        val allCookies = httpOnlyCookies + jsCookies

        fun jsonArrayToList(key: String): List<String> {
            val arr = root.optJSONArray(key) ?: return emptyList()
            return (0 until arr.length()).map { arr.getString(it) }
        }

        val htmlSource = root.optString("htmlSource")
        val scriptUrls = jsonArrayToList("scriptUrls")
        RawSignals(
            url = url,
            finalUrl = root.optString("finalUrl", url),
            requestHeaders = requestHeaders,
            headers = responseHeaders,
            globals = globals,
            jsProbe = jsProbe,
            metaTags = metas,
            scriptUrls = scriptUrls,
            cssUrls = jsonArrayToList("cssUrls"),
            bodyClasses = root.optString("bodyClasses").split(" ").filter { it.isNotBlank() },
            htmlAttrs = htmlAttrs,
            htmlSource = htmlSource,
            title = root.optString("title"),
            cookies = allCookies,
            richCookies = richCookies,
            detectedApis = detectApis(htmlSource, responseHeaders, scriptUrls),
            domFound = domFound,
            domTree = root.optString("domTree", ""),
            tls = tls,
        )
    } catch (e: Exception) {
        RawSignals(
            url = url, finalUrl = url,
            requestHeaders = requestHeaders, headers = responseHeaders,
            globals = emptyMap(), metaTags = emptyMap(),
            scriptUrls = emptyList(), cssUrls = emptyList(),
            bodyClasses = emptyList(), htmlAttrs = emptyMap(),
            htmlSource = "", title = "", tls = tls,
            error = "Parse error: ${e.message}"
        )
    }
}

private data class FetchedHeaders(
    val request: Map<String, String>,
    val response: Map<String, String>,
    val tls: TlsResult? = null,
    val richCookies: List<CookieInfo> = emptyList(),
)

private fun parseSetCookieHeader(header: String): CookieInfo {
    val parts = header.split(";").map { it.trim() }
    val nameVal = parts.first()
    val eqIdx = nameVal.indexOf('=')
    val name = if (eqIdx > 0) nameVal.substring(0, eqIdx).trim() else nameVal.trim()
    val value = if (eqIdx > 0) nameVal.substring(eqIdx + 1).trim() else ""
    var domain: String? = null
    var path: String? = null
    var expires: String? = null
    var maxAge: Long? = null
    var httpOnly = false
    var secure = false
    var sameSite: String? = null
    for (i in 1 until parts.size) {
        val p = parts[i]
        val pLower = p.lowercase()
        when {
            pLower.startsWith("domain=")   -> domain   = p.substringAfter("=").trim()
            pLower.startsWith("path=")     -> path     = p.substringAfter("=").trim()
            pLower.startsWith("expires=")  -> expires  = p.substringAfter("=").trim()
            pLower.startsWith("max-age=")  -> maxAge   = p.substringAfter("=").trim().toLongOrNull()
            pLower.startsWith("samesite=") -> sameSite = p.substringAfter("=").trim()
            pLower == "httponly"           -> httpOnly = true
            pLower == "secure"             -> secure   = true
        }
    }
    return CookieInfo(name, value, domain, path, expires, maxAge, httpOnly, secure, sameSite)
}

internal fun detectApis(html: String, headers: Map<String, String>, scriptUrls: List<String>): List<DetectedApi> {
    val results = mutableListOf<DetectedApi>()
    val lower = html.lowercase()

    // GraphQL
    val graphqlEndpoint = Regex("""['"](/[a-z0-9/_-]*graphql[a-z0-9/_-]*)['"]""", RegexOption.IGNORE_CASE).find(html)?.groupValues?.getOrNull(1)
    if (lower.contains("graphql") || lower.contains("__schema") || lower.contains("__typename")) {
        results += DetectedApi("GraphQL", 85, "GraphQL references found in HTML", graphqlEndpoint)
    } else if (scriptUrls.any { "graphql" in it.lowercase() }) {
        results += DetectedApi("GraphQL", 70, "GraphQL script URL detected", null)
    }

    // OpenAPI / Swagger
    if (lower.contains("swagger") || lower.contains("openapi") || lower.contains("api-docs")) {
        val swaggerEndpoint = Regex("""['"]([^'"]*(?:swagger|openapi|api-docs)[^'"]*)['"]""", RegexOption.IGNORE_CASE).find(html)?.groupValues?.getOrNull(1)
        results += DetectedApi("OpenAPI/Swagger", 90, "Swagger/OpenAPI reference found in HTML", swaggerEndpoint?.take(80))
    }

    // REST (API path patterns)
    val restMatches = Regex("""['"](/(?:api|v\d+)/[a-z0-9/_-]{2,40})['"]""", RegexOption.IGNORE_CASE)
        .findAll(html).map { it.groupValues[1] }.distinct().take(3).toList()
    if (restMatches.isNotEmpty()) {
        results += DetectedApi("REST", 70, "${restMatches.size} API path(s) in HTML", restMatches.first())
    }

    // SOAP
    if (lower.contains("wsdl") || lower.contains("soapenv:") || lower.contains("soap:") ||
        headers.keys.any { it.lowercase() == "soapaction" }) {
        results += DetectedApi("SOAP", 85, "WSDL/SOAP references detected")
    }

    // gRPC
    if (lower.contains("grpc") || headers.values.any { "application/grpc" in it.lowercase() }) {
        results += DetectedApi("gRPC", 85, "gRPC references detected")
    }

    // JSON-RPC
    if (lower.contains("jsonrpc") || lower.contains("json-rpc")) {
        results += DetectedApi("JSON-RPC", 80, "JSON-RPC references found in HTML")
    }

    return results.distinctBy { it.type }.sortedByDescending { it.confidence }
}

private fun String.extractCN(): String =
    split(",").firstOrNull { it.trimStart().startsWith("CN=") }
        ?.trimStart()?.removePrefix("CN=")?.trim() ?: take(100)

private const val BODY_PREVIEW_LIMIT = 100 * 1024

suspend fun sendRequest(
    method: String,
    url: String,
    headers: Map<String, String> = emptyMap(),
    body: String = "",
): RepeaterResponse = kotlinx.coroutines.withContext(Dispatchers.IO) {
    try {
        val reqBody: RequestBody? = when {
            method == "HEAD" || method == "OPTIONS" -> null
            body.isNotBlank() -> body.toRequestBody("text/plain; charset=utf-8".toMediaType())
            method !in setOf("GET", "HEAD", "OPTIONS") -> ByteArray(0).toRequestBody()
            else -> null
        }
        val request = Request.Builder()
            .url(url)
            .method(method, reqBody)
            .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
            .build()
        httpClient.newCall(request).execute().use { response ->
            val protoStr = when (response.protocol.toString()) {
                "http/1.0" -> "HTTP/1.0"
                "http/1.1" -> "HTTP/1.1"
                "h2", "h2_prior_knowledge" -> "HTTP/2"
                else -> response.protocol.toString().uppercase()
            }
            val reason = response.message.ifBlank { httpReason(response.code) }
            val statusLine = "$protoStr ${response.code} $reason"
            val resHeaders = buildMap<String, String> {
                response.headers.forEach { (name, value) -> put(name, value) }
            }
            val fullBody = response.body?.string() ?: ""
            RepeaterResponse(
                statusLine = statusLine,
                statusCode = response.code,
                headers = resHeaders,
                body = if (fullBody.length > BODY_PREVIEW_LIMIT) fullBody.take(BODY_PREVIEW_LIMIT) else fullBody,
                fullBody = fullBody,
            )
        }
    } catch (e: Exception) {
        RepeaterResponse(error = e.message ?: "Request failed")
    }
}

private fun httpReason(code: Int): String = when (code) {
    200 -> "OK"; 201 -> "Created"; 204 -> "No Content"; 206 -> "Partial Content"
    301 -> "Moved Permanently"; 302 -> "Found"; 303 -> "See Other"; 304 -> "Not Modified"; 307 -> "Temporary Redirect"; 308 -> "Permanent Redirect"
    400 -> "Bad Request"; 401 -> "Unauthorized"; 403 -> "Forbidden"; 404 -> "Not Found"
    405 -> "Method Not Allowed"; 408 -> "Request Timeout"; 409 -> "Conflict"; 410 -> "Gone"
    413 -> "Payload Too Large"; 429 -> "Too Many Requests"
    500 -> "Internal Server Error"; 502 -> "Bad Gateway"; 503 -> "Service Unavailable"; 504 -> "Gateway Timeout"
    else -> ""
}

private fun fetchHeaders(
    url: String,
    method: String = "GET",
    customHeaders: Map<String, String> = emptyMap(),
    body: String = "",
): FetchedHeaders {
    return try {
        val reqBody: RequestBody? = when {
            method == "HEAD" || method == "OPTIONS" -> null
            method == "GET" && body.isBlank() -> null
            body.isNotBlank() -> body.toRequestBody("text/plain; charset=utf-8".toMediaType())
            else -> ByteArray(0).toRequestBody()
        }
        val request = Request.Builder()
            .url(url)
            .method(method, reqBody)
            .apply { customHeaders.forEach { (k, v) -> header(k, v) } }
            .build()
        httpClient.newCall(request).execute().use { response ->
            response.body?.close()
            val reqHeaders = buildMap<String, String> {
                response.request.headers.forEach { (name, value) -> put(name, value) }
            }
            val resHeaders = buildMap<String, String> {
                put("_status", response.code.toString())
                response.headers.forEach { (name, value) -> put(name.lowercase(), value) }
            }
            val tls = response.handshake?.let { hs ->
                try {
                    val cert = hs.peerCertificates.firstOrNull() as? X509Certificate
                    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val sans = try {
                        cert?.subjectAlternativeNames
                            ?.mapNotNull { it?.getOrNull(1)?.toString() }
                            ?.take(15) ?: emptyList()
                    } catch (_: Exception) { emptyList() }
                    TlsResult(
                        tlsVersion = hs.tlsVersion.javaName,
                        cipherSuite = hs.cipherSuite.javaName,
                        subject = cert?.subjectX500Principal?.name?.extractCN(),
                        issuer = cert?.issuerX500Principal?.name?.extractCN(),
                        validFrom = cert?.notBefore?.let { fmt.format(it) },
                        validTo = cert?.notAfter?.let { fmt.format(it) },
                        sans = sans,
                    )
                } catch (_: Exception) { null }
            }
            val richCookies = response.headers.values("set-cookie").map { parseSetCookieHeader(it) }
            FetchedHeaders(reqHeaders, resHeaders, tls, richCookies)
        }
    } catch (e: Exception) {
        FetchedHeaders(emptyMap(), mapOf("_header_error" to (e.message ?: "failed")))
    }
}
