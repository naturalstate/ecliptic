package com.ecliptic.fingerprint

import com.ecliptic.scanner.RawSignals

object FingerprintEngine {

    fun detect(signals: RawSignals, db: TechDatabase): List<Technology> {
        val detected = mutableMapOf<String, Technology>()

        for (entry in db.entries) {
            val result = matchEntry(entry, signals, db) ?: continue
            detected[entry.name] = result
        }

        // Resolve implies
        val implied = mutableMapOf<String, Technology>()
        for (tech in detected.values) {
            val entry = db.entries.find { it.name == tech.name } ?: continue
            for (impliedName in entry.implies) {
                if (impliedName !in detected && impliedName !in implied) {
                    val impliedEntry = db.entries.find { it.name == impliedName } ?: continue
                    implied[impliedName] = Technology(
                        name = impliedEntry.name,
                        categories = impliedEntry.catIds.mapNotNull { db.categories[it] },
                        icon = impliedEntry.icon,
                        website = impliedEntry.website,
                        confidence = 75,
                        detectionMethod = "Implied by ${tech.name}",
                        matchedValue = null,
                    )
                }
            }
        }

        val allDetected = (detected + implied).toMutableMap()

        // Filter out techs whose `requires` or `requiresCategory` constraints aren't met
        val detectedNames = allDetected.keys
        val detectedCatIds = allDetected.values
            .flatMap { tech -> db.entries.find { it.name == tech.name }?.catIds ?: emptyList() }
            .toSet()

        allDetected.entries.removeAll { (_, tech) ->
            val entry = db.entries.find { it.name == tech.name } ?: return@removeAll false
            val requiresFailed = entry.requires.isNotEmpty() &&
                entry.requires.none { it in detectedNames }
            val requiresCatFailed = entry.requiresCategory.isNotEmpty() &&
                entry.requiresCategory.none { it in detectedCatIds }
            requiresFailed || requiresCatFailed
        }

        return allDetected.values
            .sortedWith(compareBy({ it.categories.firstOrNull() ?: "" }, { it.name }))
    }

    private fun matchEntry(entry: TechEntry, signals: RawSignals, db: TechDatabase): Technology? {
        var version: String? = null
        var detectionMethod: String? = null
        var matchedValue: String? = null

        fun record(method: String, value: String) {
            if (detectionMethod == null) {
                detectionMethod = method
                matchedValue = value
            }
        }

        fun tryMatch(patterns: List<String>, values: List<String>, method: String, label: (String) -> String): Boolean {
            for (pattern in patterns) {
                val (regex, vTpl) = compilePattern(pattern) ?: continue
                for (value in values) {
                    val match = regex.find(value) ?: continue
                    if (version == null) version = extractVersion(match, vTpl)
                    record(method, label(value))
                    return true
                }
            }
            return false
        }

        fun tryMatchMap(patterns: Map<String, String>, source: Map<String, String>, method: String): Boolean {
            for ((key, pattern) in patterns) {
                val value = source[key.lowercase()] ?: continue
                val (regex, vTpl) = compilePattern(pattern) ?: continue
                val match = regex.find(value) ?: continue
                if (version == null) version = extractVersion(match, vTpl)
                record(method, "$key: ${value.take(100)}")
                return true
            }
            return false
        }

        var matched = tryMatch(entry.scriptSrc, signals.scriptUrls, "Script URL") { url ->
            url.substringAfterLast("/").take(80).let { if (it.length < url.length) "…/$it" else it }
        }
        if (!matched) matched = tryMatch(entry.html, listOf(signals.htmlSource), "HTML source") { _ -> "Pattern matched in HTML" }
        if (!matched) matched = tryMatchMap(entry.headers, signals.headers, "HTTP header")
        if (!matched) matched = tryMatchMap(entry.meta, signals.metaTags, "Meta tag")
        if (!matched) matched = tryMatchMap(entry.js, signals.jsProbe, "JS property")
        if (!matched) matched = tryMatchMap(entry.cookies, signals.cookies, "Cookie")
        if (!matched) matched = tryMatch(entry.url, listOf(signals.finalUrl), "URL") { it.take(80) }
        if (!matched && entry.dom.isNotEmpty()) {
            val foundSel = entry.dom.firstOrNull { it.selector in signals.domFound }
            if (foundSel != null) {
                matched = true
                record("DOM selector", foundSel.selector.take(80))
            }
        }

        if (!matched) return null

        return Technology(
            name = entry.name,
            categories = entry.catIds.mapNotNull { db.categories[it] },
            icon = entry.icon,
            website = entry.website,
            version = version,
            detectionMethod = detectionMethod,
            matchedValue = matchedValue,
        )
    }

    private val patternCache = HashMap<String, Pair<Regex, String?>?>()

    private fun compilePattern(raw: String): Pair<Regex, String?>? {
        return patternCache.getOrPut(raw) {
            val parts = raw.split("\\;")
            val regexStr = parts[0].ifBlank { return@getOrPut null }
            val vTpl = parts.drop(1)
                .firstOrNull { it.startsWith("version:") }
                ?.removePrefix("version:")
                ?.takeIf { it.isNotBlank() }
            val regex = try {
                Regex(regexStr, setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            } catch (_: Exception) {
                try { Regex(Regex.escape(regexStr), RegexOption.IGNORE_CASE) }
                catch (_: Exception) { return@getOrPut null }
            }
            Pair(regex, vTpl)
        }
    }

    private fun extractVersion(match: MatchResult, template: String?): String? {
        template ?: return null
        var v = template
        match.groupValues.forEachIndexed { i, group ->
            v = v?.replace("\\$i", group)
        }
        return v?.trim()?.takeIf { it.isNotEmpty() && it != template }
    }
}
