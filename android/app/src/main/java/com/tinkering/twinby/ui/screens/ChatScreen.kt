package com.tinkering.twinby.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tinkering.twinby.data.ChatSeenStore
import com.tinkering.twinby.data.MessageItem
import com.tinkering.twinby.data.Repository
import com.tinkering.twinby.ui.glass.GlassSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun ChatScreen(repo: Repository, chatId: String, padding: PaddingValues, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val msgs = remember { mutableStateOf(listOf<MessageItem>()) }
    val input = remember { mutableStateOf("") }
    val myId = remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val sending = remember { mutableStateOf(false) }
    val lastSentText = remember { mutableStateOf<String?>(null) } // kept for future (optimistic match)
    val seenStore = remember { ChatSeenStore() }
    val otherName = remember { mutableStateOf<String?>(null) }
    val otherPhoto = remember { mutableStateOf<String?>(null) }
    val attachError = remember { mutableStateOf<String?>(null) }

    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                attachError.value = null
                val (file, mime, displayName) = copyUriToTempFile(ctx, uri)
                val uploaded = repo.uploadChatAttachment(chatId, file, mime)
                val text = "📎 ${displayName}\n${uploaded.url}"
                sending.value = true
                repo.sendMessage(chatId, text)
                msgs.value = repo.messages(chatId)
                msgs.value.lastOrNull()?.let { seenStore.setSeen(ctx, chatId, it.created_at) }
                sending.value = false
            } catch (e: Exception) {
                sending.value = false
                attachError.value = e.message ?: "Ошибка загрузки файла"
            }
        }
    }

    LaunchedEffect(chatId) {
        try {
            myId.value = repo.me().user_id
            msgs.value = repo.messages(chatId)
            // Load chat list to resolve other person's name for header
            otherName.value = try {
                repo.chats().firstOrNull { it.chat_id == chatId }?.other_name
            } catch (_: Exception) {
                null
            }
            otherPhoto.value = try {
                repo.chats().firstOrNull { it.chat_id == chatId }?.other_photo_url
            } catch (_: Exception) {
                null
            }
            if (msgs.value.isNotEmpty()) {
                listState.scrollToItem(msgs.value.lastIndex)
                // Mark as read up to last message (client-side unread indicator)
                seenStore.setSeen(ctx, chatId, msgs.value.last().created_at)
            }
        } catch (_: Exception) {
        }
    }

    // Poll messages to simulate realtime updates (MVP)
    LaunchedEffect(chatId) {
        while (isActive) {
            delay(2500)
            try {
                val latest = repo.messages(chatId)
                if (latest.isNotEmpty() && latest.size != msgs.value.size) {
                    msgs.value = latest
                    seenStore.setSeen(ctx, chatId, latest.last().created_at)
                }
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(msgs.value.size) {
        if (msgs.value.isNotEmpty()) {
            // Always keep the conversation anchored to the latest message (MVP behavior).
            listState.animateScrollToItem(msgs.value.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)) {
        ChatTopBar(
            title = otherName.value ?: "Диалог",
            subtitle = "был(а) недавно",
            photoUrl = otherPhoto.value,
            onBack = onBack,
        )
        Spacer(Modifier.height(12.dp))
        if (attachError.value != null) {
            Text(attachError.value!!, color = Color(0xFFFF6B6B))
            Spacer(Modifier.height(6.dp))
        }
        val my = myId.value
        val lastMineIndex = if (my == null) -1 else msgs.value.indexOfLast { it.sender_id == my }
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(msgs.value) { idx, m ->
                val mine = (my != null && m.sender_id == my)
                val status = if (mine && idx == lastMineIndex) {
                    when {
                        sending.value -> "Отправка…"
                        else -> "Доставлено"
                    }
                } else null
                MessageBubble(text = m.text, mine = mine, status = status)
            }
        }
        Spacer(Modifier.height(8.dp))

        // Composer (Liquid / Telegram-like): attach + glass input + emoji + send
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            GlassSurface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { pickFile.launch(arrayOf("*/*")) },
                corner = 999.dp,
                tint = Color.White.copy(alpha = 0.08f),
                stroke = Color.White.copy(alpha = 0.14f),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.AttachFile,
                        contentDescription = "Attach",
                        tint = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            Spacer(Modifier.size(10.dp))

            GlassSurface(
                modifier = Modifier.weight(1f),
                corner = 999.dp,
                tint = Color.White.copy(alpha = 0.07f),
                stroke = Color.White.copy(alpha = 0.12f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input.value,
                        onValueChange = { input.value = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Сообщение", color = Color.White.copy(alpha = 0.55f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        ),
                        singleLine = true,
                    )
                    Icon(
                        imageVector = Icons.Filled.SentimentSatisfiedAlt,
                        contentDescription = "Emoji",
                        tint = Color.White.copy(alpha = 0.70f),
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }

            Spacer(Modifier.size(10.dp))

            GlassSurface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable {
                        val text = input.value.trim()
                        if (text.isEmpty()) return@clickable
                        input.value = ""
                        sending.value = true
                        lastSentText.value = text
                        scope.launch {
                            try {
                                repo.sendMessage(chatId, text)
                                msgs.value = repo.messages(chatId)
                                msgs.value.lastOrNull()?.let { seenStore.setSeen(ctx, chatId, it.created_at) }
                            } catch (_: Exception) {
                            } finally {
                                sending.value = false
                            }
                        }
                    },
                corner = 999.dp,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.90f),
                stroke = Color.White.copy(alpha = 0.10f),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

private fun copyUriToTempFile(context: Context, uri: Uri): Triple<File, String?, String> {
    val cr = context.contentResolver
    val mime = cr.getType(uri)
    val name = run {
        val c = cr.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
        c?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }
    } ?: "file"
    val ext = when (mime) {
        "image/jpeg" -> ".jpg"
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        else -> ""
    }
    val outFile = File(context.cacheDir, "attach_${UUID.randomUUID()}${ext}")
    val input = cr.openInputStream(uri) ?: error("Cannot open file")
    input.use { ins -> outFile.outputStream().use { outs -> ins.copyTo(outs) } }
    return Triple(outFile, mime, name)
}


