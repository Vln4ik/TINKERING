package com.tinkering.twinby.ui.brand

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Lightweight logo mark inspired by provided "Tinkering" logo:
 * heart + sparkle (no gear, per request). Uses built-in Material icons.
 */
@Composable
fun LogoMark(modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary) {
    Box(modifier = modifier.size(26.dp), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = "Logo",
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.88f),
            modifier = Modifier.size(13.dp)
        )
    }
}


