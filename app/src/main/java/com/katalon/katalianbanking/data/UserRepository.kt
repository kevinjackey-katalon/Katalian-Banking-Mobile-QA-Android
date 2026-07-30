package com.katalon.katalianbanking.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "katalian_banking_store")

/**
 * Mirrors the web app's localStorage persistence:
 *   STORAGE_KEYS.USERS   -> 'katalian_users_v1'
 *   STORAGE_KEYS.SESSION -> 'katalian_session_v1'
 */
class UserRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val usersKey = stringPreferencesKey("katalian_users_v1")
    private val sessionUserIdKey = stringPreferencesKey("katalian_session_v1")

    suspend fun loadUsers(): List<User> {
        val prefs = context.dataStore.data.first()
        val stored = prefs[usersKey]
        return if (stored != null) {
            try {
                json.decodeFromString(ListSerializer(User.serializer()), stored)
            } catch (e: Exception) {
                seedUsers()
            }
        } else {
            seedUsers()
        }
    }

    suspend fun saveUsers(users: List<User>) {
        val encoded = json.encodeToString(ListSerializer(User.serializer()), users)
        context.dataStore.edit { it[usersKey] = encoded }
    }

    suspend fun loadSessionUserId(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[sessionUserIdKey]
    }

    suspend fun saveSessionUserId(userId: String?) {
        context.dataStore.edit { prefs ->
            if (userId == null) prefs.remove(sessionUserIdKey) else prefs[sessionUserIdKey] = userId
        }
    }
}
