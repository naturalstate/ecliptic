package com.ecliptic.scanner

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
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

internal val httpClientNoRedirect = httpClient.newBuilder()
    .followRedirects(false)
    .followSslRedirects(false)
    .build()

// Some WAFs (e.g. Cloudflare Bot Fight Mode) block requests that claim to be a browser
// but can't solve a JS challenge, returning 403/503/520-class errors; others block
// obvious bot UAs. We try the browser UA first, then fall back to a plain tool UA.
private const val BROWSER_UA = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 " +
    "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
private const val FALLBACK_UA = "Ecliptic/1.0 (+https://ryand202.sg-host.com/ecliptic/)"
private val WAF_BLOCK_CODES = setOf(403, 429, 503, 520, 521, 522, 523, 524, 525, 526)

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

    // --- DOM selector probe (attribute / JS-property / text verification) ---
    // A selector alone is not enough: we require every declared constraint
    // (attribute regex, JS property presence/value, text regex) to hold on a
    // single matching element. This prevents generic selectors (e.g. "body > div"
    // paired with a React "_reactRootContainer" property) from matching every page.
    var domFound = [];
    var domChecks = $domChecksJson;
    for (var sel in domChecks) {
        try {
            var check    = domChecks[sel];
            var attrs    = check.a || {};
            var props    = check.p || {};
            var attrKeys = Object.keys(attrs);
            var propKeys = Object.keys(props);
            var textRe   = check.t ? new RegExp(check.t, 'i') : null;
            var els = document.querySelectorAll(sel);
            if (!els || els.length === 0) continue;
            var matched = false;
            for (var ei = 0; ei < els.length && ei < 50; ei++) {
                var el = els[ei];
                var ok = true;
                for (var ai = 0; ai < attrKeys.length; ai++) {
                    var av = el.getAttribute(attrKeys[ai]);
                    if (av == null) { ok = false; break; }
                    var apat = attrs[attrKeys[ai]];
                    if (apat && !(new RegExp(apat, 'i').test(av))) { ok = false; break; }
                }
                if (!ok) continue;
                for (var pi = 0; pi < propKeys.length; pi++) {
                    var pk = propKeys[pi];
                    if (!(pk in el)) { ok = false; break; }
                    var ppat = props[pk];
                    if (ppat) {
                        var pv = el[pk];
                        if (pv == null || !(new RegExp(ppat, 'i').test(String(pv)))) { ok = false; break; }
                    }
                }
                if (!ok) continue;
                if (textRe && !textRe.test(el.textContent || '')) continue;
                matched = true; break;
            }
            if (matched) domFound.push(sel);
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

    // --- CSS custom properties (same-origin stylesheets + computed style) ---
    var cssCustomProps = [];
    try {
        var cpropSet = {};
        function addCssProp(p) { if (p && p.charAt(0) === '-' && p.charAt(1) === '-' && !cpropSet[p]) { cpropSet[p] = true; cssCustomProps.push(p); } }
        // Scan accessible (same-origin) stylesheet rules
        var sheets = document.styleSheets;
        for (var si = 0; si < sheets.length; si++) {
            try {
                var rules = sheets[si].cssRules || [];
                for (var ri = 0; ri < rules.length; ri++) {
                    var ruleStyle = rules[ri].style;
                    if (ruleStyle) { for (var pi = 0; pi < ruleStyle.length; pi++) addCssProp(ruleStyle[pi]); }
                    // Nested rules (media queries, supports, etc.)
                    var nested = rules[ri].cssRules;
                    if (nested) { for (var ni = 0; ni < nested.length; ni++) {
                        var nStyle = nested[ni].style;
                        if (nStyle) { for (var npi = 0; npi < nStyle.length; npi++) addCssProp(nStyle[npi]); }
                    }}
                }
            } catch(cssE) {}  // CORS — skip cross-origin sheets
        }
        // Check computed style on :root and body (catches runtime-set vars and * rules)
        var checkEls = [document.documentElement, document.body];
        for (var ci = 0; ci < checkEls.length; ci++) {
            if (!checkEls[ci]) continue;
            var cs = window.getComputedStyle(checkEls[ci]);
            for (var cpi = 0; cpi < cs.length; cpi++) addCssProp(cs[cpi]);
        }
    } catch(cssErr) {}

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
        cssCustomProps: cssCustomProps,
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

suspend fun fetchTextContent(url: String): String = withContext(Dispatchers.IO) {
    fun attempt(ua: String): String? = try {
        val request = Request.Builder().url(url).header("User-Agent", ua).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) null else response.body?.string()?.take(500_000)
        }
    } catch (_: Exception) { null }
    // Browser UA first, plain UA fallback — same WAF handling as the header fetch.
    attempt(BROWSER_UA) ?: attempt(FALLBACK_UA) ?: "// Failed to load"
}

@SuppressLint("SetJavaScriptEnabled")
// ── Persistent WebView clients ────────────────────────────────────────────────

data class WebViewClients(
    val viewClient: EclipticWebViewClient,
    val chromeClient: EclipticChromeClient,
)

class EclipticWebViewClient : WebViewClient() {
    @Volatile var onPageReady: (() -> Unit)? = null
    @Volatile var onPageError: ((String) -> Unit)? = null
    @Volatile var onRequest: ((NetworkRequest) -> Unit)? = null

    override fun onPageFinished(view: WebView, url: String) {
        onPageReady?.invoke()
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (request.isForMainFrame) onPageError?.invoke(error.description.toString())
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        if (!url.startsWith("data:") && !url.startsWith("blob:")) {
            onRequest?.invoke(NetworkRequest(
                url = url,
                type = inferResourceType(url),
                method = request.method ?: "GET",
            ))
        }
        return null
    }
}

class EclipticChromeClient : WebChromeClient() {
    @Volatile var onConsole: ((ConsoleMessage) -> Unit)? = null

    override fun onConsoleMessage(msg: android.webkit.ConsoleMessage): Boolean {
        onConsole?.invoke(ConsoleMessage(
            level = when (msg.messageLevel()) {
                android.webkit.ConsoleMessage.MessageLevel.ERROR   -> "error"
                android.webkit.ConsoleMessage.MessageLevel.WARNING -> "warn"
                android.webkit.ConsoleMessage.MessageLevel.DEBUG   -> "debug"
                android.webkit.ConsoleMessage.MessageLevel.TIP     -> "info"
                else                                                -> "log"
            },
            message    = msg.message(),
            sourceUrl  = msg.sourceId().takeIf { it.isNotBlank() },
            lineNumber = msg.lineNumber(),
        ))
        return true
    }
}

private fun inferResourceType(url: String): String {
    val path = url.substringBefore("?").substringBefore("#").lowercase()
    return when {
        path.endsWith(".js") || path.endsWith(".mjs") || path.endsWith(".jsx") -> "script"
        path.endsWith(".css")                                                   -> "stylesheet"
        path.endsWith(".woff") || path.endsWith(".woff2") || path.endsWith(".ttf") ||
            path.endsWith(".otf") || path.endsWith(".eot")                     -> "font"
        path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") ||
            path.endsWith(".gif") || path.endsWith(".svg") || path.endsWith(".webp") ||
            path.endsWith(".ico") || path.endsWith(".avif")                    -> "image"
        path.endsWith(".mp4") || path.endsWith(".webm") || path.endsWith(".mp3") ||
            path.endsWith(".ogg") || path.endsWith(".wav")                     -> "media"
        path.contains("/graphql") || path.contains("/api/") ||
            path.contains("/rest/") || path.contains("/v1/") ||
            path.contains("/v2/") || path.contains("/v3/")                     -> "xhr"
        else -> "other"
    }
}

fun getWebViewClients(webView: WebView): WebViewClients? = webView.tag as? WebViewClients

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
    webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
    val clients = WebViewClients(EclipticWebViewClient(), EclipticChromeClient())
    webView.webViewClient  = clients.viewClient
    webView.webChromeClient = clients.chromeClient
    webView.tag = clients
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
    timeoutMs: Long = 20_000L,
    userAgent: String? = null,
    followRedirects: Boolean = true,
    onStage: (String) -> Unit = {},
): RawSignals {
    val url = rawUrl.trim().let {
        if (!it.startsWith("http://") && !it.startsWith("https://")) "https://$it" else it
    }

    onStage("Fetching headers…")
    val client = if (followRedirects) httpClient else httpClientNoRedirect
    val fetched = withContext(Dispatchers.IO) { fetchHeaders(url, method, customHeaders, requestBody, client) }
    val harvestJs = buildHarvestJs(jsPaths, domChecksJson)

    val networkRequests = mutableListOf<NetworkRequest>()

    return try {
        withTimeout(timeoutMs) {
            withContext(Dispatchers.Main) {
                if (userAgent != null) webView.settings.userAgentString = userAgent
                // Capture network requests made during page load into local list;
                // after scan the callback is replaced by ViewModel to continue live capture.
                (webView.tag as? WebViewClients)?.viewClient?.onRequest = { req ->
                    synchronized(networkRequests) { networkRequests.add(req) }
                }
                onStage("Loading page…")
                val json = loadAndInject(webView, url, harvestJs)
                val httpOnlyCookies = parseCookieHeader(CookieManager.getInstance().getCookie(url) ?: "")
                parseSignals(url, fetched.request, fetched.response, json, httpOnlyCookies, fetched.tls, fetched.richCookies, networkRequests.toList())
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
            error = "Timed out after ${timeoutMs / 1000}s"
        )
    }
}

private suspend fun loadAndInject(webView: WebView, url: String, js: String): String =
    suspendCancellableCoroutine { cont ->
        val client = (webView.tag as? WebViewClients)?.viewClient ?: run {
            // Safety fallback: should never happen after configureWebView
            val wvc = EclipticWebViewClient()
            webView.webViewClient = wvc
            wvc
        }
        client.onPageReady = {
            client.onPageReady = null
            client.onPageError = null
            webView.evaluateJavascript(js) { result ->
                if (cont.isActive) cont.resume(result ?: "null")
            }
        }
        client.onPageError = { errMsg ->
            client.onPageReady = null
            client.onPageError = null
            if (cont.isActive) cont.resume("""{"error":"${errMsg.replace("\"", "\\\"")}"}""")
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
    networkRequests: List<NetworkRequest> = emptyList(),
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
        val staticScriptUrls = jsonArrayToList("scriptUrls")
        // Merge runtime-intercepted script/stylesheet URLs into scriptUrls for richer detection
        val runtimeScriptUrls = networkRequests
            .filter { it.type == "script" || it.type == "stylesheet" }
            .map { it.url }
        val allScriptUrls = (staticScriptUrls + runtimeScriptUrls).distinct()
        RawSignals(
            url = url,
            finalUrl = root.optString("finalUrl", url),
            requestHeaders = requestHeaders,
            headers = responseHeaders,
            globals = globals,
            jsProbe = jsProbe,
            metaTags = metas,
            scriptUrls = allScriptUrls,
            cssUrls = jsonArrayToList("cssUrls"),
            bodyClasses = root.optString("bodyClasses").split(" ").filter { it.isNotBlank() },
            htmlAttrs = htmlAttrs,
            htmlSource = htmlSource,
            title = root.optString("title"),
            cookies = allCookies,
            richCookies = richCookies,
            detectedApis = detectApis(htmlSource, responseHeaders, allScriptUrls, jsonArrayToList("cssUrls"), networkRequests),
            domFound = domFound,
            cssProps = jsonArrayToList("cssCustomProps"),
            domTree = root.optString("domTree", ""),
            tls = tls,
            networkRequests = networkRequests,
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

/** Classify a URL or path as an API type, or null if it does not look like an API. */
private fun classifyApi(raw: String): String? {
    val u = raw.lowercase()
    return when {
        u.startsWith("ws://") || u.startsWith("wss://")        -> "WebSocket"
        "graphql" in u                                          -> "GraphQL"
        "jsonrpc" in u || "json-rpc" in u                       -> "JSON-RPC"
        "swagger" in u || "openapi" in u || "api-docs" in u     -> "OpenAPI/Swagger"
        ".wsdl" in u || "wsdl?" in u || "soapaction" in u       -> "SOAP"
        "grpc" in u                                             -> "gRPC"
        "/api/" in u || u.endsWith("/api") || "/rest/" in u ||
            Regex("""://api[.\-]""").containsMatchIn(u) ||
            Regex("""/v\d+(/|$|\?)""").containsMatchIn(u)        -> "REST"
        else -> null
    }
}

/**
 * Collects the concrete API endpoints a web app uses, from signals already captured
 * during the scan (no additional network fetches):
 *   1. XHR/fetch requests intercepted while the page loaded (the real API calls)
 *   2. absolute URLs and relative API paths embedded in the HTML source
 *   3. script/stylesheet resource URLs that live on API hosts/paths
 *   4. WebSocket endpoints and Socket.IO usage
 *   5. protocol hints from response headers (SOAP, gRPC, …)
 * Each distinct endpoint is returned as a DetectedApi with the endpoint URL and the
 * source it was found in. Endpoints without a concrete URL carry a null endpoint.
 */
internal fun detectApis(
    html: String,
    headers: Map<String, String>,
    scriptUrls: List<String>,
    cssUrls: List<String> = emptyList(),
    networkRequests: List<NetworkRequest> = emptyList(),
): List<DetectedApi> {
    val found = LinkedHashMap<String, DetectedApi>()
    fun add(type: String, url: String?, source: String, confidence: Int) {
        val clean = url?.trim()?.trimEnd('.', ',', ';', ')', '"', '\'')?.take(300)
        val key = (type + "|" + (clean ?: "")).lowercase()
        val existing = found[key]
        if (existing == null || confidence > existing.confidence) {
            found[key] = DetectedApi(type, confidence, source, clean)
        }
    }

    // 1) Requests the page actually made
    for (req in networkRequests) {
        val cls = classifyApi(req.url) ?: if (req.type == "xhr") "REST" else continue
        add(cls, req.url, "${req.method} request during page load", 90)
    }

    // 2) Resource URLs (scripts / stylesheets) on API hosts or paths
    for (src in (scriptUrls + cssUrls)) {
        val cls = classifyApi(src) ?: continue
        add(cls, src, "Referenced resource URL", 70)
    }

    // 3) Absolute URLs embedded in the HTML source
    val urlRe = Regex("""https?://[^\s"'<>()\\]+""", RegexOption.IGNORE_CASE)
    for (m in urlRe.findAll(html).take(600)) {
        val cls = classifyApi(m.value) ?: continue
        add(cls, m.value, "URL in HTML source", 65)
    }

    // 4) Relative API paths in the HTML source
    val relRe = Regex("""["'](/(?:api|rest|graphql|v\d+)[A-Za-z0-9/_\-.]{0,80})["']""", RegexOption.IGNORE_CASE)
    for (m in relRe.findAll(html).take(200)) {
        val path = m.groupValues[1]
        add(classifyApi(path) ?: "REST", path, "API path in HTML source", 70)
    }

    // 5) WebSockets — not captured by the request interceptor, so scan the source
    val wsRe = Regex("""wss?://[^\s"'<>()\\]+""", RegexOption.IGNORE_CASE)
    for (m in wsRe.findAll(html).take(50)) add("WebSocket", m.value, "WebSocket URL in source", 85)
    if (Regex("""new\s+WebSocket\s*\(""").containsMatchIn(html))
        add("WebSocket", null, "new WebSocket() call in source", 80)
    if ("socket.io" in html.lowercase() || scriptUrls.any { "socket.io" in it.lowercase() })
        add("WebSocket", null, "Socket.IO library detected", 80)

    // 6) Protocol hints from headers / inline markup without a concrete URL
    val lower = html.lowercase()
    if (headers.keys.any { it.equals("soapaction", true) } || "soapenv:" in lower || "<wsdl" in lower)
        add("SOAP", null, "WSDL/SOAP references detected", 85)
    if (headers.values.any { "application/grpc" in it.lowercase() } || "grpc-web" in lower)
        add("gRPC", null, "gRPC references detected", 85)
    if ("__schema" in lower || "__typename" in lower)
        add("GraphQL", null, "GraphQL schema references in HTML", 75)

    return found.values.sortedWith(
        compareByDescending<DetectedApi> { it.endpoint != null }
            .thenBy { it.type }
            .thenByDescending { it.confidence }
    )
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
    client: OkHttpClient = httpClient,
): FetchedHeaders {
    // Respect an explicit User-Agent supplied via custom headers (e.g. Repeater raw
    // mode); otherwise try the browser UA first and fall back to a plain UA on a
    // WAF-style block so Cloudflare-protected sites still return real headers.
    val hasCustomUa = customHeaders.keys.any { it.equals("User-Agent", ignoreCase = true) }

    fun attempt(uaOverride: String?): FetchedHeaders? = try {
        val reqBody: RequestBody? = when {
            method == "HEAD" || method == "OPTIONS" -> null
            method == "GET" && body.isBlank() -> null
            body.isNotBlank() -> body.toRequestBody("text/plain; charset=utf-8".toMediaType())
            else -> ByteArray(0).toRequestBody()
        }
        val request = Request.Builder()
            .url(url)
            .method(method, reqBody)
            .apply {
                customHeaders.forEach { (k, v) -> header(k, v) }
                if (uaOverride != null && !hasCustomUa) header("User-Agent", uaOverride)
            }
            .build()
        client.newCall(request).execute().use { response ->
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
    } catch (e: Exception) { null }

    val first = attempt(if (hasCustomUa) null else BROWSER_UA)
    if (!hasCustomUa) {
        val code = first?.response?.get("_status")?.toIntOrNull()
        if (first == null || code == null || code in WAF_BLOCK_CODES) {
            val second = attempt(FALLBACK_UA)
            val scode = second?.response?.get("_status")?.toIntOrNull()
            if (second != null && scode != null && scode !in WAF_BLOCK_CODES) return second
        }
    }
    return first ?: FetchedHeaders(emptyMap(), mapOf("_header_error" to "failed"))
}
