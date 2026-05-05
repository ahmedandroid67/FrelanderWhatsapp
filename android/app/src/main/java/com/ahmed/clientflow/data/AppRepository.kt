package com.ahmed.clientflow.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "clientflow_prefs")

class AppRepository(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private object Keys {
        val appState = stringPreferencesKey("app_state")
        val pinHash = stringPreferencesKey("pin_hash")
        val locked = booleanPreferencesKey("locked")
        val freeClientLimit = intPreferencesKey("free_client_limit")
    }

    val appState: Flow<AppState> = context.dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { prefs ->
            val raw = prefs[Keys.appState]
            val base = raw?.let {
                runCatching { json.decodeFromString<AppState>(it) }.getOrNull()
            } ?: AppState()
            base.copy(freeClientLimit = prefs[Keys.freeClientLimit] ?: base.freeClientLimit)
        }

    val authState: Flow<AuthState> = context.dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { prefs ->
            val pinHash = prefs[Keys.pinHash]
            val locked = prefs[Keys.locked] ?: true
            when {
                pinHash.isNullOrBlank() -> AuthState.Setup
                locked -> AuthState.Locked
                else -> AuthState.Unlocked
            }
        }

    suspend fun updateState(transform: (AppState) -> AppState) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.appState]?.let {
                runCatching { json.decodeFromString<AppState>(it) }.getOrNull()
            } ?: AppState()
            prefs[Keys.appState] = json.encodeToString(AppState.serializer(), transform(current))
        }
    }

    suspend fun setPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.pinHash] = hashPin(pin)
            prefs[Keys.locked] = false
        }
    }

    suspend fun clearPin() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.pinHash)
            prefs[Keys.locked] = true
        }
    }

    suspend fun unlock(pin: String): Boolean {
        var ok = false
        context.dataStore.edit { prefs ->
            ok = prefs[Keys.pinHash] == hashPin(pin)
            if (ok) prefs[Keys.locked] = false
        }
        return ok
    }

    suspend fun lock() {
        context.dataStore.edit { prefs ->
            if (!prefs[Keys.pinHash].isNullOrBlank()) {
                prefs[Keys.locked] = true
            }
        }
    }

    suspend fun activatePro(code: String): Boolean {
        val normalized = code.trim().uppercase()
        val ok = normalized == "CF-PRO-V1-X7K9"
        if (ok) {
            updateState { it.copy(isPro = true) }
        }
        return ok
    }
}

enum class AuthState { Setup, Locked, Unlocked }
