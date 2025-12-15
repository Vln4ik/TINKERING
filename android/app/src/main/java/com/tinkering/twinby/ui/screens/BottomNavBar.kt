package com.tinkering.twinby.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tinkering.twinby.ui.glass.GlassSurface

enum class BottomTab { Chats, Feed, Settings }

@Composable
fun BottomNavBar(
    selectedTab: BottomTab,
    onGoChats: () -> Unit,
    onGoFeed: () -> Unit,
    onGoSettings: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        corner = 30.dp,
        tint = Color.White.copy(alpha = 0.06f),
        stroke = Color.White.copy(alpha = 0.12f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavPillItem(
                selected = selectedTab == BottomTab.Chats,
                label = "Чаты",
                icon = Icons.Filled.ChatBubble,
                onClick = onGoChats
            )
            NavPillItem(
                selected = selectedTab == BottomTab.Feed,
                label = "Лента",
                icon = Icons.Filled.Favorite,
                onClick = onGoFeed
            )
            NavPillItem(
                selected = selectedTab == BottomTab.Settings,
                label = "Настройки",
                icon = Icons.Filled.Settings,
                onClick = onGoSettings
            )
        }
    }
}

@Composable
private fun NavPillItem(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.86f)
    val textColor = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.86f)

    GlassSurface(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        corner = 24.dp,
        tint = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
        stroke = if (selected) Color.White.copy(alpha = 0.10f) else Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.size(4.dp))
            Text(
                label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}


