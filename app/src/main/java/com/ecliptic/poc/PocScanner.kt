package com.ecliptic.poc

import android.annotation.SuppressLint
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

data class PocResult(
    val url: String,
    val headers: Map<String, String>,
    val jsSignals: String,
)

private val ipv4Only = object : Dns {
    override fun lookup(hostname: String): List<InetAddress> =
        Dns.SYSTEM.lookup(hostname).filter { it is Inet4Address }.ifEmpty {
            Dns.SYSTEM.lookup(hostname)
        }
}

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .followRedirects(true)
    .dns(ipv4Only)
    .build()

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

suspend fun scanUrl(webView: WebView, url: String): PocResult {
    val headers = withContext(Dispatchers.IO) { fetchHeaders(url) }
    val jsSignals = withContext(Dispatchers.Main) { loadAndInject(webView, url) }
    return PocResult(url = url, headers = headers, jsSignals = jsSignals)
}

private suspend fun loadAndInject(webView: WebView, url: String): String =
    suspendCancellableCoroutine { cont ->
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                injectJs(view) { result ->
                    if (cont.isActive) cont.resume(result)
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError,
            ) {
                if (request.isForMainFrame && cont.isActive) {
                    cont.resume("""{"error":"${error.description}","code":${error.errorCode}}""")
                }
            }
        }
        webView.loadUrl(url)
    }

private fun injectJs(view: WebView, callback: (String) -> Unit) {
    // ES5-compatible JS — safe across all WebView versions on API 26+
    val js = """
        (function() {
            var scripts = document.getElementsByTagName('script');
            var srcs = [];
            for (var i = 0; i < Math.min(scripts.length, 15); i++) {
                if (scripts[i].src) srcs.push(scripts[i].src);
            }
            var gen = document.querySelector('meta[name="generator"]');
            return JSON.stringify({
                react:     typeof window.React !== 'undefined' ? (window.React.version || true) : false,
                jquery:    typeof window.jQuery !== 'undefined' ? (window.jQuery.fn.jquery || true) : false,
                vue:       typeof window.Vue !== 'undefined',
                angular:   typeof window.angular !== 'undefined',
                next:      typeof window.__NEXT_DATA__ !== 'undefined',
                nuxt:      typeof window.__NUXT__ !== 'undefined',
                wordpress: typeof window.wp !== 'undefined',
                gtag:      typeof window.gtag !== 'undefined',
                gtm:       typeof window.google_tag_manager !== 'undefined',
                shopify:   typeof window.Shopify !== 'undefined',
                metaGenerator: gen ? gen.getAttribute('content') : null,
                scriptSrcs: srcs,
                bodyClasses: document.body ? document.body.className.substring(0, 300) : ''
            }, null, 2);
        })()
    """.trimIndent()
    view.evaluateJavascript(js, callback)
}

private fun fetchHeaders(url: String): Map<String, String> {
    return try {
        val request = Request.Builder().url(url).head().build()
        httpClient.newCall(request).execute().use { response ->
            buildMap {
                put("_http_status", response.code.toString())
                response.headers.forEach { (name, value) ->
                    put(name.lowercase(), value)
                }
            }
        }
    } catch (e: Exception) {
        mapOf("_error" to (e.message ?: "request failed"))
    }
}
