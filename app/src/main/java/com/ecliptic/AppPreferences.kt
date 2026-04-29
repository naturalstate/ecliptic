package com.ecliptic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ecliptic_prefs")

const val DEFAULT_SCAN_TIMEOUT_S = 20
const val DEFAULT_UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

object AppPreferences {
    private val DARK_THEME       = booleanPreferencesKey("dark_theme")
    private val HTTP_METHOD      = stringPreferencesKey("http_method")
    private val USER_AGENT       = stringPreferencesKey("user_agent")
    private val FOLLOW_REDIRECTS = booleanPreferencesKey("follow_redirects")
    private val SCAN_TIMEOUT_S   = intPreferencesKey("scan_timeout_s")
    private val SCAN_COUNT       = intPreferencesKey("scan_count")

    fun darkThemeFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[DARK_THEME] ?: true }

    fun httpMethodFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[HTTP_METHOD] ?: "GET" }

    fun userAgentFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[USER_AGENT] ?: DEFAULT_UA }

    fun followRedirectsFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[FOLLOW_REDIRECTS] ?: true }

    fun scanTimeoutFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { it[SCAN_TIMEOUT_S] ?: DEFAULT_SCAN_TIMEOUT_S }

    fun scanCountFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { it[SCAN_COUNT] ?: 0 }

    suspend fun setDarkTheme(context: Context, dark: Boolean) {
        context.dataStore.edit { it[DARK_THEME] = dark }
    }

    suspend fun setHttpMethod(context: Context, method: String) {
        context.dataStore.edit { it[HTTP_METHOD] = method }
    }

    suspend fun setUserAgent(context: Context, ua: String) {
        context.dataStore.edit { it[USER_AGENT] = ua }
    }

    suspend fun setFollowRedirects(context: Context, follow: Boolean) {
        context.dataStore.edit { it[FOLLOW_REDIRECTS] = follow }
    }

    suspend fun setScanTimeout(context: Context, seconds: Int) {
        context.dataStore.edit { it[SCAN_TIMEOUT_S] = seconds }
    }

    suspend fun incrementScanCount(context: Context): Int {
        var newCount = 0
        context.dataStore.edit { prefs ->
            newCount = (prefs[SCAN_COUNT] ?: 0) + 1
            prefs[SCAN_COUNT] = newCount
        }
        return newCount
    }
}
