package com.ecliptic.fingerprint

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Classifies cookies against the Open Cookie Database (jkwakman/Open-Cookie-Database),
 * bundled as assets/cookies.json. Tells whether a cookie is a *known* one (with the
 * platform that sets it, its category and a description) or looks *proprietary* to the
 * site. Exact names are matched first; wildcard entries match by name prefix.
 */
class CookieDatabase private constructor(
    private val exact: Map<String, Entry>,
    private val prefixes: List<Pair<String, Entry>>,  // (prefix, entry), longest-first
) {
    data class Entry(
        val platform: String,
        val category: String,
        val description: String,
        val retention: String,
    )

    /** Known entry for a cookie name, or null if it isn't in the database (proprietary). */
    fun lookup(name: String): Entry? {
        val k = name.trim().lowercase()
        if (k.isEmpty()) return null
        exact[k]?.let { return it }
        return prefixes.firstOrNull { (p, _) -> p.length >= 2 && k.startsWith(p) }?.second
    }

    companion object {
        const val SOURCE_NAME = "Open Cookie Database"
        const val SOURCE_URL = "https://github.com/jkwakman/Open-Cookie-Database"
        private const val BROWSE_URL =
            "https://jkwakman.github.io/Open-Cookie-Database/open-cookie-database.html"

        /**
         * Link to the searchable Open Cookie Database page, scrolled/highlighted to the
         * given cookie via a browser text fragment (Chrome et al.), so the user lands on
         * the entry instead of a bare page.
         */
        fun searchUrl(cookieName: String): String =
            BROWSE_URL + "#:~:text=" + android.net.Uri.encode(cookieName.trim())

        @Volatile private var instance: CookieDatabase? = null

        suspend fun get(context: Context): CookieDatabase =
            instance ?: withContext(Dispatchers.IO) {
                instance ?: load(context).also { instance = it }
            }

        private fun load(context: Context): CookieDatabase {
            return try {
                val json = context.assets.open("cookies.json").bufferedReader().readText()
                val root = JSONObject(json)
                fun parse(obj: JSONObject?): Map<String, Entry> {
                    if (obj == null) return emptyMap()
                    return buildMap {
                        obj.keys().forEach { k ->
                            val e = obj.getJSONObject(k)
                            put(k, Entry(
                                platform = e.optString("p"),
                                category = e.optString("c"),
                                description = e.optString("d"),
                                retention = e.optString("r"),
                            ))
                        }
                    }
                }
                val exact = parse(root.optJSONObject("exact"))
                // Longest prefix first so the most specific wildcard wins.
                val prefixes = parse(root.optJSONObject("prefix"))
                    .toList()
                    .sortedByDescending { it.first.length }
                CookieDatabase(exact, prefixes)
            } catch (_: Exception) {
                CookieDatabase(emptyMap(), emptyList())
            }
        }
    }
}
