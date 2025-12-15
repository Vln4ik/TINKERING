package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tinkering.twinby.data.Repository
import com.tinkering.twinby.data.SupportMessageItem
import kotlinx.coroutines.launch

@Composable
fun SupportScreen(repo: Repository, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val msgs = remember { mutableStateOf(listOf<SupportMessageItem>()) }
    val input = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            msgs.value = repo.supportMessages()
        } catch (_: Exception) {
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text("Техподдержка", color = Color.White)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(220.dp)) {
            items(msgs.value) { m ->
                val mine = m.role == "user"
                MessageBubble(text = m.text, mine = mine)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            msgs.value = repo.supportMessages()
                        } catch (_: Exception) {
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
            ) { Text("Ок") }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(44.dp)) { Text("Закрыть") }
    }
}


