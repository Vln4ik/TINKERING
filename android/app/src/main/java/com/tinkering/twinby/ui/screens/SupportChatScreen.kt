package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
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
import androidx.compose.ui.unit.dp
import com.tinkering.twinby.data.Repository
import com.tinkering.twinby.data.SupportMessageItem
import com.tinkering.twinby.ui.glass.GlassSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun SupportChatScreen(repo: Repository, padding: PaddingValues, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val msgs = remember { mutableStateOf(listOf<SupportMessageItem>()) }
    val input = remember { mutableStateOf("") }
    val error = remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    suspend fun reload() {
        msgs.value = repo.supportMessages()
    }

    LaunchedEffect(Unit) {
        try {
            reload()
            if (msgs.value.isNotEmpty()) {
                listState.scrollToItem(msgs.value.lastIndex)
            }
        } catch (e: Exception) {
            error.value = e.message
        }
    }

    // Poll support messages so new replies appear without reopening the screen (MVP)
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(2500)
            try {
                val latest = repo.supportMessages()
                if (latest.size != msgs.value.size) {
                    msgs.value = latest
                }
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(msgs.value.size) {
        if (msgs.value.isNotEmpty()) {
            listState.animateScrollToItem(msgs.value.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)) {
        ChatTopBar(
            title = "Техподдержка",
            subtitle = null,
            photoUrl = null,
            onBack = onBack
        )
        Spacer(Modifier.height(12.dp))

        if (error.value != null) {
            Text(error.value!!, color = Color(0xFFFF6B6B))
            Spacer(Modifier.height(6.dp))
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(msgs.value) { m ->
                MessageBubble(text = m.text, mine = (m.role == "user"))
            }
        }

        Spacer(Modifier.height(8.dp))

        // Composer (same style as ChatScreen): glass input + emoji + send
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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

            GlassSurface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable {
                        val text = input.value.trim()
                        if (text.isEmpty()) return@clickable
                        input.value = ""
                        scope.launch {
                            try {
                                repo.sendSupport(text)
                                reload()
                            } catch (e: Exception) {
                                error.value = e.message
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


