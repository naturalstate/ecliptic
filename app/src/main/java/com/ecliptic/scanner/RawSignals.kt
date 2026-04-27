package com.ecliptic.scanner

data class RepeaterResponse(
    val statusLine: String = "",
    val statusCode: Int = 0,
    val headers: Map<String, String> = emptyMap(),
    val body: String = "",
    val fullBody: String = "",
    val error: String? = null,
) {
    val isBodyTruncated: Boolean get() = body.length < fullBody.length
}

data class RawSignals(
    val url: String,
    val finalUrl: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val headers: Map<String, String>,
    val globals: Map<String, Any?>,
    val jsProbe: Map<String, String> = emptyMap(),
    val metaTags: Map<String, String>,
    val scriptUrls: List<String>,
    val cssUrls: List<String>,
    val bodyClasses: List<String>,
    val htmlAttrs: Map<String, String>,
    val htmlSource: String,
    val title: String,
    val cookies: Map<String, String> = emptyMap(),
    val domFound: Set<String> = emptySet(),
    val domTree: String = "",
    val tls: TlsResult? = null,
    val error: String? = null,
)
