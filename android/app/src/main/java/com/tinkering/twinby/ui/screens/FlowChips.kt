package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun FlowChips(
    items: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    FlowRow {
        items.forEach { key ->
            val isSelected = selected.contains(key)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 8.dp)
                    .clickable { onToggle(key) }
            ) {
                Text(
                    text = key,
                    color = if (isSelected) Color.Black else Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}


