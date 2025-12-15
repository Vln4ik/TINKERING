package com.tinkering.twinby.ui.glass

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassBackground(modifier: Modifier = Modifier) {
    // Liquid Glass-ish background: deep dark + subtle colorful glow.
    val t = rememberInfiniteTransition(label = "bg")
    val p by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgShift"
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6F5BFF).copy(alpha = 0.32f),
                        Color(0xFF00E5FF).copy(alpha = 0.16f),
                        Color(0xFF0B0C10),
                    ),
                    center = Offset(x = 260f + 220f * p, y = 300f + 140f * (1f - p)),
                    radius = 1500f
                )
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0B0C10),
                        Color(0xFF171A34),
                        Color(0xFF0B0C10),
                    )
                    ,
                    start = Offset(x = 0f + 220f * p, y = 0f),
                    end = Offset(x = 900f - 220f * p, y = 1600f)
                )
            )
    )
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    corner: Dp = 22.dp,
    tint: Color = Color.White.copy(alpha = 0.08f),
    stroke: Color = Color.White.copy(alpha = 0.14f),
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .clip(shape)
            .background(tint)
            .border(1.dp, stroke, shape),
        content = content
    )
}


