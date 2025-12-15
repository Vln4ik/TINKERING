package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinkering.twinby.data.ProfilePublic

@Composable
fun SwipeStack(
    top: ProfilePublic,
    next: ProfilePublic?,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        if (next != null) {
            UserCard(profile = next, modifier = Modifier.padding(top = 14.dp))
        }
        SwipeableUserCard(profile = top, onSwipeLeft = onSwipeLeft, onSwipeRight = onSwipeRight)
    }
}


