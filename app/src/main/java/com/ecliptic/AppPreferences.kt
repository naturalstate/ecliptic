package com.ecliptic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ecliptic_prefs")

object AppPreferences {
    private val DARK_THEME = booleanPreferencesKey("dark_theme")
    private val HTTP_METHOD = stringPreferencesKey("http_method")

    fun darkThemeFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[DARK_THEME] ?: true }

    fun httpMethodFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[HTTP_METHOD] ?: "GET" }

    suspend fun setDarkTheme(context: Context, dark: Boolean) {
        context.dataStore.edit { it[DARK_THEME] = dark }
    }

    suspend fun setHttpMethod(context: Context, method: String) {
        context.dataStore.edit { it[HTTP_METHOD] = method }
    }
}
