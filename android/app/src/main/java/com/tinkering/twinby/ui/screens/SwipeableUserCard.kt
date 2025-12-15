package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.tinkering.twinby.data.ProfilePublic

@Composable
fun SwipeableUserCard(
    profile: ProfilePublic,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
) {
    val offsetX = remember { mutableFloatStateOf(0f) }
    val offsetY = remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = offsetX.floatValue
                translationY = offsetY.floatValue
                rotationZ = offsetX.floatValue / 40f
            }
            .pointerInput(profile.user_id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX.floatValue += dragAmount.x
                        offsetY.floatValue += dragAmount.y
                    },
                    onDragEnd = {
                        val threshold = 220f
                        when {
                            offsetX.floatValue > threshold -> {
                                offsetX.floatValue = 0f
                                offsetY.floatValue = 0f
                                onSwipeRight()
                            }
                            offsetX.floatValue < -threshold -> {
                                offsetX.floatValue = 0f
                                offsetY.floatValue = 0f
                                onSwipeLeft()
                            }
                            else -> {
                                offsetX.floatValue = 0f
                                offsetY.floatValue = 0f
                            }
                        }
                    }
                )
            }
    ) {
        UserCard(profile = profile)
    }
}


