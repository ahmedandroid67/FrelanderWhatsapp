package com.ahmed.clientflow.data

import android.content.Context
import android.provider.Settings
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
import kotlinx.coroutines.flow.first
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
        val deviceId = stringPreferencesKey("device_id")
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

    suspend fun unlockByBiometric() {
        context.dataStore.edit { prefs ->
            prefs[Keys.locked] = false
        }
    }

    suspend fun lock() {
        context.dataStore.edit { prefs ->
            if (!prefs[Keys.pinHash].isNullOrBlank()) {
                prefs[Keys.locked] = true
            }
        }
    }

    suspend fun getDeviceId(): String {
        val stored = context.dataStore.data.first()[Keys.deviceId]
        if (!stored.isNullOrBlank()) return stored
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        val id = "CF-" + androidId.takeLast(12).uppercase()
        context.dataStore.edit { prefs -> prefs[Keys.deviceId] = id }
        return id
    }

    suspend fun activatePro(code: String): Boolean {
        val deviceId = getDeviceId()
        val normalized = code.trim().replace("-", "").replace(" ", "").uppercase()

        val result = FirestoreHelper.activateCode(normalized, deviceId)
        return when (result) {
            is ActivationResult.Success -> {
                updateState { it.copy(isPro = true) }
                true
            }
            else -> false
        }
    }
}

enum class AuthState { Setup, Locked, Unlocked }
