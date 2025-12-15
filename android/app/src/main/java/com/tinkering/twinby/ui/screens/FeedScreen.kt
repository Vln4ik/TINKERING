package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tinkering.twinby.data.ProfilePublic
import com.tinkering.twinby.data.Repository
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    repo: Repository,
    padding: PaddingValues,
    onOpenChat: (chatId: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val users = remember { mutableStateOf(listOf<ProfilePublic>()) }

    LaunchedEffect(Unit) {
        loading.value = true
        error.value = null
        try {
            users.value = repo.feed(20)
        } catch (e: Exception) {
            error.value = e.message
        } finally {
            loading.value = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        when {
            loading.value -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            error.value != null -> Text(
                text = error.value!!,
                color = Color(0xFFFF6B6B),
                modifier = Modifier.align(Alignment.Center)
            )
            users.value.isEmpty() -> Text(
                text = "Пока нет новых анкет",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
            else -> {
                val top = users.value.getOrNull(0)
                val next = users.value.getOrNull(1)

                if (top != null) {
                    SwipeStack(
                        top = top,
                        next = next,
                        onSwipeLeft = {
                            scope.launch { repo.swipe(top.user_id, "left") }
                            users.value = users.value.drop(1)
                            if (users.value.size < 5) {
                                scope.launch {
                                    try {
                                        val more = repo.feed(20)
                                        users.value = (users.value + more).distinctBy { it.user_id }
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        },
                        onSwipeRight = {
                            scope.launch {
                                val resp = repo.swipe(top.user_id, "right")
                                if (!resp.created_chat_id.isNullOrBlank()) {
                                    onOpenChat(resp.created_chat_id)
                                }
                            }
                            users.value = users.value.drop(1)
                        }
                    )
                }
            }
        }
    }
}


