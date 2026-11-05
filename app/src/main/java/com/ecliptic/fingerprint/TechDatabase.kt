package com.ecliptic.fingerprint

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class DomCheck(
    val selector: String,
    val attributes: Map<String, String> = emptyMap(),
    val text: String? = null,
    val properties: Map<String, String> = emptyMap(),
) {
    /** True when the check verifies more than mere element existence. */
    fun hasConstraints(): Boolean =
        attributes.isNotEmpty() || text != null || properties.isNotEmpty()
}

/**
 * A selector is "generic" when it targets common structural elements with no id,
 * class, attribute, or custom-element qualifier — e.g. "div", "body > div", "main".
 * Such a selector matches almost every page, so an existence-only check on it is
 * worthless for fingerprinting and produces false positives.
 */
internal fun isGenericSelector(selector: String): Boolean {
    val s = selector.trim()
    if (s.isEmpty()) return true
    // Any qualifier makes it specific enough to keep.
    if (s.any { it == '#' || it == '.' || it == '[' || it == ':' }) return false
    // Custom elements (contain a hyphen in the tag) are specific, e.g. "model-viewer".
    if (s.any { it == '-' }) return false
    // Otherwise it's only tag names / combinators (div, body, main, body > div, ...).
    return true
}

data class TechEntry(
    val name: String,
    val catIds: List<Int>,
    val icon: String?,
    val website: String?,
    val scriptSrc: List<String>,
    val html: List<String>,
    val headers: Map<String, String>,
    val meta: Map<String, String>,
    val js: Map<String, String>,
    val url: List<String>,
    val implies: List<String>,
    val cookies: Map<String, String>,
    val dom: List<DomCheck>,
    val requires: List<String>,
    val requiresCategory: List<Int>,
    val css: List<String>,
)

class TechDatabase private constructor(
    val entries: List<TechEntry>,
    val categories: Map<Int, String>,
) {
    fun allJsPaths(): Set<String> = entries.flatMap { it.js.keys }.toSet()
    fun hasCssChecks(): Boolean = entries.any { it.css.isNotEmpty() }

    // Returns a JSON object string mapping selector → {a: {attrName: regex}, t: textRegex|null}
    // If multiple techs share a selector, preference is given to the one with attribute constraints.
    fun allDomChecksJson(): String {
        val allChecks = linkedMapOf<String, DomCheck>()
        for (entry in entries) {
            for (check in entry.dom) {
                // Skip existence-only checks on overly-generic selectors: a bare
                // selector like "body > div" or "div" exists on almost every page
                // and would assert a technology on nearly every site (false positive).
                if (!check.hasConstraints() && isGenericSelector(check.selector)) continue
                val existing = allChecks[check.selector]
                if (existing == null || (!existing.hasConstraints() && check.hasConstraints())) {
                    allChecks[check.selector] = check
                }
            }
        }
        return buildDomChecksJson(allChecks)
    }

    companion object {
        @Volatile private var instance: TechDatabase? = null

        suspend fun get(context: Context): TechDatabase =
            instance ?: withContext(Dispatchers.IO) {
                instance ?: load(context).also { instance = it }
            }

        private fun load(context: Context): TechDatabase {
            val cats = loadCategories(context)
            val entries = mutableListOf<TechEntry>()
            val files = listOf("_") + ('a'..'z').map { it.toString() }
            val assets = context.assets

            for (file in files) {
                try {
                    val json = assets.open("technologies/$file.json")
                        .bufferedReader().readText()
                    val obj = JSONObject(json)
                    obj.keys().forEach { name ->
                        entries += parseTech(name, obj.getJSONObject(name))
                    }
                } catch (_: Exception) {}
            }
            return TechDatabase(entries, cats)
        }

        private fun loadCategories(context: Context): Map<Int, String> {
            return try {
                val json = context.assets.open("categories.json").bufferedReader().readText()
                val obj = JSONObject(json)
                buildMap {
                    obj.keys().forEach { key ->
                        val id = key.toIntOrNull() ?: return@forEach
                        put(id, obj.getJSONObject(key).optString("name", key))
                    }
                }
            } catch (_: Exception) { emptyMap() }
        }

        private fun parseTech(name: String, obj: JSONObject): TechEntry {
            fun strList(key: String): List<String> {
                val v = obj.opt(key) ?: return emptyList()
                return when (v) {
                    is String -> if (v.isNotBlank()) listOf(v) else emptyList()
                    is org.json.JSONArray -> (0 until v.length()).mapNotNull {
                        v.optString(it).takeIf { s -> s.isNotBlank() }
                    }
                    else -> emptyList()
                }
            }

            fun strMap(key: String): Map<String, String> {
                val v = obj.optJSONObject(key) ?: return emptyMap()
                return buildMap {
                    v.keys().forEach { k -> put(k.lowercase(), v.optString(k)) }
                }
            }

            fun catIds(): List<Int> {
                val arr = obj.optJSONArray("cats") ?: return emptyList()
                return (0 until arr.length()).mapNotNull { arr.optInt(it).takeIf { id -> id > 0 } }
            }

            fun implies(): List<String> {
                val v = obj.opt("implies") ?: return emptyList()
                return when (v) {
                    is String -> if (v.isNotBlank()) listOf(v.split("\\;")[0]) else emptyList()
                    is org.json.JSONArray -> (0 until v.length()).mapNotNull {
                        v.optString(it).split("\\;")[0].takeIf { s -> s.isNotBlank() }
                    }
                    else -> emptyList()
                }
            }

            fun requires(): List<String> {
                val v = obj.opt("requires") ?: return emptyList()
                return when (v) {
                    is String -> if (v.isNotBlank()) listOf(v) else emptyList()
                    is org.json.JSONArray -> (0 until v.length()).mapNotNull {
                        v.optString(it).takeIf { s -> s.isNotBlank() }
                    }
                    else -> emptyList()
                }
            }

            fun requiresCategory(): List<Int> {
                val v = obj.opt("requiresCategory") ?: return emptyList()
                return when (v) {
                    is Int -> if (v > 0) listOf(v) else emptyList()
                    is org.json.JSONArray -> (0 until v.length()).mapNotNull {
                        v.optInt(it).takeIf { id -> id > 0 }
                    }
                    else -> emptyList()
                }
            }

            fun domChecks(): List<DomCheck> {
                val v = obj.opt("dom") ?: return emptyList()
                return when (v) {
                    is String -> if (v.isNotBlank()) listOf(DomCheck(v)) else emptyList()
                    is org.json.JSONArray -> (0 until v.length()).mapNotNull {
                        v.optString(it).takeIf { s -> s.isNotBlank() }?.let { sel -> DomCheck(sel) }
                    }
                    is JSONObject -> {
                        val result = mutableListOf<DomCheck>()
                        v.keys().forEach { selector ->
                            if (selector.isBlank()) return@forEach
                            val checkObj = v.optJSONObject(selector)
                            if (checkObj == null) {
                                result += DomCheck(selector)
                                return@forEach
                            }
                            val attrs = mutableMapOf<String, String>()
                            checkObj.optJSONObject("attributes")?.let { attrsObj ->
                                attrsObj.keys().forEach { attrName ->
                                    val pattern = attrsObj.optString(attrName)
                                    if (pattern.isNotBlank()) attrs[attrName] = pattern
                                }
                            }
                            val props = mutableMapOf<String, String>()
                            checkObj.optJSONObject("properties")?.let { propsObj ->
                                propsObj.keys().forEach { propName ->
                                    // Value may be "" (presence check) or a regex pattern
                                    props[propName] = propsObj.optString(propName)
                                }
                            }
                            val text = checkObj.optString("text").takeIf { it.isNotBlank() }
                            result += DomCheck(selector, attrs, text, props)
                        }
                        result
                    }
                    else -> emptyList()
                }
            }

            return TechEntry(
                name = name,
                catIds = catIds(),
                icon = obj.optString("icon").takeIf { it.isNotBlank() },
                website = obj.optString("website").takeIf { it.isNotBlank() },
                scriptSrc = strList("scriptSrc"),
                html = strList("html"),
                headers = strMap("headers"),
                meta = strMap("meta"),
                js = strMap("js"),
                url = strList("url"),
                implies = implies(),
                cookies = strMap("cookies"),
                dom = domChecks(),
                requires = requires(),
                requiresCategory = requiresCategory(),
                css = strList("css"),
            )
        }

        private fun buildDomChecksJson(checks: Map<String, DomCheck>): String {
            if (checks.isEmpty()) return "{}"
            val sb = StringBuilder("{")
            var first = true
            for ((_, check) in checks) {
                if (!first) sb.append(",")
                first = false
                sb.append('"').append(check.selector.jsEscape()).append('"')
                sb.append(":{\"a\":{")
                var firstAttr = true
                for ((attrName, pattern) in check.attributes) {
                    if (!firstAttr) sb.append(",")
                    firstAttr = false
                    sb.append('"').append(attrName.jsEscape()).append('"')
                    sb.append(":\"").append(pattern.jsEscape()).append('"')
                }
                sb.append("},\"p\":{")
                var firstProp = true
                for ((propName, pattern) in check.properties) {
                    if (!firstProp) sb.append(",")
                    firstProp = false
                    sb.append('"').append(propName.jsEscape()).append('"')
                    sb.append(":\"").append(pattern.jsEscape()).append('"')
                }
                sb.append("},\"t\":")
                val text = check.text
                if (text != null) sb.append('"').append(text.jsEscape()).append('"')
                else sb.append("null")
                sb.append("}")
            }
            sb.append("}")
            return sb.toString()
        }

        private fun String.jsEscape(): String = this
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
    }
}
