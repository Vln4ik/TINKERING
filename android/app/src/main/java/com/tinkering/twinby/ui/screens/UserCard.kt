package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tinkering.twinby.data.ProfilePublic

@Composable
fun UserCard(profile: ProfilePublic, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AsyncImage(
            model = profile.photo_url,
            contentDescription = profile.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000)),
                        startY = 300f
                    )
                )
        )

        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Text(
                text = "${profile.name}, ${profile.age}",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Text(text = profile.about, color = Color.White.copy(alpha = 0.9f))
            Text(
                text = profile.interests.joinToString(" · "),
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


