package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import com.tinkering.twinby.data.Repository

private enum class Tab(val title: String) {
    Chats("Чаты"),
    Feed("Лента"),
    Settings("Настройки"),
}

@Composable
fun MainTabs(
    repo: Repository,
    contentPadding: PaddingValues,
    onOpenChat: (chatId: String) -> Unit,
) {
    val tab = remember { mutableStateOf(Tab.Feed) }

    androidx.compose.material3.Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab.value == t,
                        onClick = { tab.value = t },
                        label = { Text(t.title) },
                        icon = {
                            Icon(
                                imageVector = when (t) {
                                    Tab.Chats -> Icons.Filled.ChatBubble
                                    Tab.Feed -> Icons.Filled.Favorite
                                    Tab.Settings -> Icons.Filled.Settings
                                },
                                contentDescription = t.title
                            )
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        content = { inner ->
            val pad = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 12.dp,
                bottom = inner.calculateBottomPadding() + 12.dp
            )
            when (tab.value) {
                Tab.Chats -> ChatsScreen(repo = repo, padding = pad, onOpenChat = onOpenChat)
                Tab.Feed -> FeedScreen(repo = repo, padding = pad, onOpenChat = onOpenChat)
                Tab.Settings -> SettingsScreen(repo = repo, padding = pad)
            }
        }
    )
}


