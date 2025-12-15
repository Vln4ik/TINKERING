package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tinkering.twinby.data.MessageItem
import com.tinkering.twinby.data.Repository
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(repo: Repository, chatId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val msgs = remember { mutableStateOf(listOf<MessageItem>()) }
    val input = remember { mutableStateOf("") }
    val myId = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(chatId) {
        try {
            myId.value = repo.me().user_id
            msgs.value = repo.messages(chatId)
        } catch (_: Exception) {
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "← Назад",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(4.dp).clickable { onBack() }
            )
        }
        Text("Диалог", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Spacer(Modifier.height(10.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(msgs.value) { m ->
                MessageBubble(text = m.text, mine = (myId.value != null && m.sender_id == myId.value))
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
                            repo.sendMessage(chatId, text)
                            msgs.value = repo.messages(chatId)
                        } catch (_: Exception) {
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp)
            ) { Text("Отпр.") }
        }
    }
}


