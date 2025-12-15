package com.tinkering.twinby.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "auth")

class TokenStore {
    private var token: String? = null

    suspend fun load(context: Context) {
        val key = stringPreferencesKey("token")
        token = context.dataStore.data.first()[key]
    }

    fun getToken(): String? = token

    suspend fun setToken(context: Context, value: String?) {
        val key = stringPreferencesKey("token")
        context.dataStore.edit { prefs ->
            if (value.isNullOrBlank()) prefs.remove(key) else prefs[key] = value
        }
        token = value
    }
}


