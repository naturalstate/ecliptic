package com.ecliptic.fingerprint

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards for the React/Svelte false-positive fix: generic existence-only DOM
 * selectors must be rejected, and a check only "has constraints" when it verifies
 * an attribute, text, or JS property (not mere element existence).
 */
class DetectionGuardTest {

    @Test
    fun genericStructuralSelectorsAreGeneric() {
        assertTrue(isGenericSelector("body > div"))
        assertTrue(isGenericSelector("div"))
        assertTrue(isGenericSelector("main"))
        assertTrue(isGenericSelector("body"))
    }

    @Test
    fun qualifiedSelectorsAreNotGeneric() {
        assertFalse(isGenericSelector("#app"))
        assertFalse(isGenericSelector(".hero"))
        assertFalse(isGenericSelector("div[id*='react-root']"))
        assertFalse(isGenericSelector("model-viewer")) // custom element (hyphen)
    }

    @Test
    fun hasConstraintsRequiresMoreThanExistence() {
        assertFalse(DomCheck("body > div").hasConstraints())
        assertTrue(DomCheck("body > div", properties = mapOf("_reactRootContainer" to "")).hasConstraints())
        assertTrue(DomCheck("meta", attributes = mapOf("name" to "generator")).hasConstraints())
        assertTrue(DomCheck("h1", text = "Welcome").hasConstraints())
    }
}
