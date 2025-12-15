package com.tinkering.twinby.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinkering.twinby.ui.glass.GlassSurface

@Composable
fun BottomNavBar(
    currentRoute: String,
    onGoChats: () -> Unit,
    onGoFeed: () -> Unit,
    onGoSettings: () -> Unit,
) {
    val selected = when {
        currentRoute.startsWith("chats") -> "chats"
        currentRoute.startsWith("feed") -> "feed"
        currentRoute.startsWith("settings") -> "settings"
        else -> "feed"
    }

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        corner = 26.dp,
        blurRadius = 20.dp,
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f),
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = selected == "chats",
                onClick = onGoChats,
                label = { Text("Чаты") },
                icon = { Icon(Icons.Filled.ChatBubble, contentDescription = "Чаты") }
            )
            NavigationBarItem(
                selected = selected == "feed",
                onClick = onGoFeed,
                label = { Text("Лента") },
                icon = { Icon(Icons.Filled.Favorite, contentDescription = "Лента") }
            )
            NavigationBarItem(
                selected = selected == "settings",
                onClick = onGoSettings,
                label = { Text("Настройки") },
                icon = { Icon(Icons.Filled.Settings, contentDescription = "Настройки") }
            )
        }
    }
}


