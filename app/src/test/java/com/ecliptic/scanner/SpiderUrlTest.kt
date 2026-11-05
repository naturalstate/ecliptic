package com.ecliptic.scanner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpiderUrlTest {

    private val eng = SpiderEngine(CoroutineScope(Dispatchers.Unconfined))

    @Test
    fun hostKeyStripsWww() {
        assertEquals("example.com", eng.hostKeyOf("https://www.example.com/path"))
        assertEquals("example.com", eng.hostKeyOf("https://example.com"))
        assertEquals("example.com", eng.hostKeyOf("http://EXAMPLE.com/x?y=1"))
    }

    @Test
    fun sameHostTreatsWwwAndApexEqual() {
        assertTrue(eng.sameHost("https://www.example.com/a", "example.com"))
        assertTrue(eng.sameHost("https://example.com/a", "example.com"))
        assertFalse(eng.sameHost("https://other.com/a", "example.com"))
    }

    @Test
    fun normalizeUrlDropsFragmentKeepsQuery() {
        assertEquals("https://example.com/p?x=1", eng.normalizeUrl("https://example.com/p?x=1#frag"))
        assertEquals("https://example.com/", eng.normalizeUrl("https://example.com"))
    }

    @Test
    fun extractLinksResolvesInternalAndFiltersOffsite() {
        val html = """
            <a href="/about">About</a>
            <a href="https://www.example.com/pricing">Pricing</a>
            <a href="https://evil.com/x">Offsite</a>
            <a href="#top">Anchor</a>
            <a href="mailto:a@b.com">Mail</a>
            <a href="javascript:void(0)">JS</a>
        """.trimIndent()
        val links = eng.extractLinks(html, "https://example.com/", "example.com")
        assertTrue(links.contains("https://example.com/about"))
        assertTrue(links.contains("https://www.example.com/pricing"))
        assertFalse(links.any { it.contains("evil.com") })
        assertFalse(links.any { it.contains("mailto") || it.contains("javascript") || it.contains("#top") })
        assertEquals(2, links.size)
    }
}
