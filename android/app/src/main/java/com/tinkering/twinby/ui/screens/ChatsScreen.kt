package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tinkering.twinby.data.ChatSeenStore
import com.tinkering.twinby.data.ChatListItem
import com.tinkering.twinby.data.Repository
import com.tinkering.twinby.data.SupportMessageItem
import com.tinkering.twinby.ui.glass.GlassSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Instant
import java.time.OffsetDateTime

@Composable
fun ChatsScreen(
    repo: Repository,
    padding: PaddingValues,
    onOpenChat: (chatId: String) -> Unit,
    onOpenSupport: () -> Unit,
) {
    val ctx = LocalContext.current
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val chats = remember { mutableStateOf(listOf<ChatListItem>()) }
    val supportLast = remember { mutableStateOf<SupportMessageItem?>(null) }
    val seenStore = remember { ChatSeenStore() }
    val seenMap = remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(Unit) {
        loading.value = true
        try {
            chats.value = repo.chats()
            val support = repo.supportMessages()
            supportLast.value = support.lastOrNull()
            // load seen markers
            val m = mutableMapOf<String, String>()
            for (c in chats.value) {
                val s = seenStore.getSeen(ctx, c.chat_id)
                if (s != null) m[c.chat_id] = s
            }
            seenMap.value = m
        } catch (e: Exception) {
            error.value = e.message
        } finally {
            loading.value = false
        }
    }

    // Poll chat list so newly sent/received messages move dialogs up (MVP).
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(3000)
            try {
                val latest = repo.chats()
                if (latest.size != chats.value.size || latest.firstOrNull()?.last_message_at != chats.value.firstOrNull()?.last_message_at) {
                    chats.value = latest
                    val m = mutableMapOf<String, String>()
                    for (c in latest) {
                        val s = seenStore.getSeen(ctx, c.chat_id)
                        if (s != null) m[c.chat_id] = s
                    }
                    seenMap.value = m
                }
            } catch (_: Exception) {
            }
        }
    }

    Column(modifier = Modifier.padding(padding)) {
        Spacer(Modifier.height(4.dp))

        when {
            loading.value -> CircularProgressIndicator()
            error.value != null -> Text(error.value!!, color = Color(0xFFFF6B6B))
            else -> {
                // Pinned support chat
                SupportRow(
                    lastText = supportLast.value?.text ?: "Напишите в поддержку",
                    onClick = onOpenSupport
                )
                Spacer(Modifier.height(16.dp))

                if (chats.value.isEmpty()) {
                    Text("Пока нет диалогов", color = Color.White)
                } else {
                    chats.value.forEach { item ->
                        val seen = seenMap.value[item.chat_id]
                        val unread = isUnread(item.last_message_at, seen)
                        ChatRow(item = item, unread = unread, onClick = { onOpenChat(item.chat_id) })
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

private fun isUnread(lastAt: String?, seenAt: String?): Boolean {
    if (lastAt.isNullOrBlank()) return false
    if (seenAt.isNullOrBlank()) return true
    fun parse(s: String): Long? {
        return try {
            Instant.parse(s).toEpochMilli()
        } catch (_: Exception) {
            try {
                OffsetDateTime.parse(s).toInstant().toEpochMilli()
            } catch (_: Exception) {
                null
            }
        }
    }
    val a = parse(lastAt)
    val b = parse(seenAt)
    if (a != null && b != null) return a > b
    // fallback: ISO strings often compare lexicographically
    return lastAt > seenAt
}

@Composable
private fun ChatRow(item: ChatListItem, unread: Boolean, onClick: () -> Unit) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 2.dp),
        corner = 20.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = item.other_photo_url,
                contentDescription = item.other_name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.other_name, color = Color.White)
                    if (unread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                        )
                    }
                }
                Text(item.last_message ?: "Начните диалог", color = Color.White.copy(alpha = 0.7f), maxLines = 1)
            }
        }
    }
}

@Composable
private fun SupportRow(lastText: String, onClick: () -> Unit) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        corner = 22.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier.size(48.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.SupportAgent,
                        contentDescription = "Support",
                        tint = Color.Black
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Техподдержка", color = Color.White)
                Text(lastText, color = Color.White.copy(alpha = 0.7f), maxLines = 1)
            }
        }
    }
}


