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

data class CookieInfo(
    val name: String,
    val value: String,
    val domain: String? = null,
    val path: String? = null,
    val expires: String? = null,
    val maxAge: Long? = null,
    val httpOnly: Boolean = false,
    val secure: Boolean = false,
    val sameSite: String? = null,
)

data class DetectedApi(
    val type: String,
    val confidence: Int,
    val evidence: String,
    val endpoint: String? = null,
)

data class NetworkRequest(
    val url: String,
    val type: String,   // "script" | "stylesheet" | "xhr" | "image" | "font" | "media" | "other"
    val method: String = "GET",
    val timestamp: Long = System.currentTimeMillis(),
)

data class ConsoleMessage(
    val level: String,  // "log" | "warn" | "error" | "info" | "debug"
    val message: String,
    val sourceUrl: String? = null,
    val lineNumber: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
)

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
    val richCookies: List<CookieInfo> = emptyList(),
    val detectedApis: List<DetectedApi> = emptyList(),
    val domFound: Set<String> = emptySet(),
    val domTree: String = "",
    val tls: TlsResult? = null,
    val networkRequests: List<NetworkRequest> = emptyList(),
    val error: String? = null,
)
