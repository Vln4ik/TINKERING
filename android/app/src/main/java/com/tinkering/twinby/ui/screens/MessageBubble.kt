package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MessageBubble(text: String, mine: Boolean) {
    val bg = if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val fg = if (mine) Color.Black else Color.White
    val align = if (mine) 1f else 0.9f
    Surface(
        color = bg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(align)
    ) {
        Text(text = text, color = fg, modifier = Modifier.padding(12.dp))
    }
}


