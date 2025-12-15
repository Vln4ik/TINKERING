package com.tinkering.twinby.ui.glass

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassBackground(modifier: Modifier = Modifier) {
    // Liquid Glass-ish background: deep dark + subtle colorful glow.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6F5BFF).copy(alpha = 0.22f),
                        Color(0xFF00E5FF).copy(alpha = 0.10f),
                        Color(0xFF0B0C10),
                    ),
                    radius = 1400f
                )
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0B0C10),
                        Color(0xFF121427),
                        Color(0xFF0B0C10),
                    )
                )
            )
    )
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    corner: Dp = 22.dp,
    blurRadius: Dp = 18.dp,
    tint: Color = Color.White.copy(alpha = 0.08f),
    stroke: Color = Color.White.copy(alpha = 0.14f),
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        renderEffect = RenderEffect.createBlurEffect(
                            blurRadius.toPx(),
                            blurRadius.toPx(),
                            Shader.TileMode.CLAMP
                        )
                    }
                } else {
                    Modifier
                }
            )
            .background(tint)
            .border(1.dp, stroke, shape),
        content = content
    )
}


