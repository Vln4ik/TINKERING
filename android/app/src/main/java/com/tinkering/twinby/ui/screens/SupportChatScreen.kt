package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tinkering.twinby.data.Repository
import com.tinkering.twinby.data.SupportMessageItem
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

    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        ChatTopBar(
            title = "Техподдержка",
            subtitle = null,
            photoUrl = null,
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(10.dp))

        if (error.value != null) {
            Text(error.value!!, color = Color(0xFFFF6B6B))
            Spacer(Modifier.height(8.dp))
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = input.value,
                onValueChange = { input.value = it },
                modifier = Modifier.weight(1f),
                label = { Text("Сообщение") }
            )
            Button(
                onClick = {
                    val text = input.value.trim()
                    if (text.isEmpty()) return@Button
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
                shape = RoundedCornerShape(14.dp),
            ) { Text("Ок") }
        }
    }
}


