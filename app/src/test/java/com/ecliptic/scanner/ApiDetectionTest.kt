package com.ecliptic.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiDetectionTest {

    private fun types(list: List<DetectedApi>) = list.map { it.type }.toSet()

    @Test
    fun plainPageYieldsNothing() {
        val html = "<html><body><a href='/about'>About</a><p>hello</p></body></html>"
        val result = detectApis(html, emptyMap(), emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun relativeGraphqlPathDetected() {
        val html = """<script>fetch("/graphql",{method:"POST"})</script>"""
        val result = detectApis(html, emptyMap(), emptyList())
        assertTrue("GraphQL" in types(result))
        assertTrue(result.any { it.endpoint == "/graphql" })
    }

    @Test
    fun xhrRequestToApiHostIsRest() {
        val reqs = listOf(
            NetworkRequest("https://api.example.com/v1/users", "xhr", "GET"),
            NetworkRequest("https://cdn.example.com/app.js", "script", "GET"),
        )
        val result = detectApis("<html></html>", emptyMap(), emptyList(), emptyList(), reqs)
        assertTrue("REST" in types(result))
        assertTrue(result.any { it.endpoint == "https://api.example.com/v1/users" })
        // the plain script asset must NOT be reported as an API
        assertTrue(result.none { it.endpoint?.endsWith("app.js") == true })
    }

    @Test
    fun websocketUrlAndConstructorDetected() {
        val html = """<script>var s = new WebSocket("wss://ws.example.com/live");</script>"""
        val result = detectApis(html, emptyMap(), emptyList())
        assertTrue("WebSocket" in types(result))
        assertTrue(result.any { it.endpoint == "wss://ws.example.com/live" })
    }

    @Test
    fun soapHeaderReportedWithNullEndpoint() {
        val result = detectApis("<html></html>", mapOf("soapaction" to "\"urn:x\""), emptyList())
        val soap = result.firstOrNull { it.type == "SOAP" }
        assertTrue(soap != null)
        assertEquals(null, soap!!.endpoint)
    }

    @Test
    fun duplicateEndpointsCollapsed() {
        val reqs = listOf(
            NetworkRequest("https://api.example.com/v1/x", "xhr", "GET"),
            NetworkRequest("https://api.example.com/v1/x", "xhr", "GET"),
        )
        val result = detectApis("<html></html>", emptyMap(), emptyList(), emptyList(), reqs)
        assertEquals(1, result.count { it.endpoint == "https://api.example.com/v1/x" })
    }
}
