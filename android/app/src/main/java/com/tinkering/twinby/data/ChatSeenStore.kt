package com.tinkering.twinby.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.chatDataStore by preferencesDataStore(name = "chats")

class ChatSeenStore {
    private fun key(chatId: String) = stringPreferencesKey("seen_$chatId")

    suspend fun getSeen(context: Context, chatId: String): String? {
        return context.chatDataStore.data.first()[key(chatId)]
    }

    suspend fun setSeen(context: Context, chatId: String, lastSeenAt: String) {
        context.chatDataStore.edit { prefs ->
            prefs[key(chatId)] = lastSeenAt
        }
    }
}


