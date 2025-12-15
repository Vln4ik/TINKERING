package com.tinkering.twinby

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tinkering.twinby.data.Repository
import com.tinkering.twinby.data.TokenStore
import com.tinkering.twinby.ui.screens.AuthScreen
import com.tinkering.twinby.ui.screens.ChatScreen
import com.tinkering.twinby.ui.screens.ChatsScreen
import com.tinkering.twinby.ui.screens.FeedScreen
import com.tinkering.twinby.ui.screens.SettingsScreen
import com.tinkering.twinby.ui.screens.SupportChatScreen
import com.tinkering.twinby.ui.screens.BottomNavBar
import com.tinkering.twinby.ui.screens.BottomTab
import com.tinkering.twinby.ui.glass.GlassBackground

@Composable
fun TwinbyApp() {
    val tokenStore = remember { TokenStore() }
    val repo = remember { Repository(tokenStore) }

    var authed by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(BottomTab.Feed) }
    var openChatId by remember { mutableStateOf<String?>(null) }
    var supportOpen by remember { mutableStateOf(false) }

    if (!authed) {
        AuthScreen(
            repo = repo,
            tokenStore = tokenStore,
            onAuthed = { authed = true }
        )
        return
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = tab,
                onGoChats = {
                    // tapping bottom tabs should close overlay screens (chat/support)
                    openChatId = null
                    supportOpen = false
                    tab = BottomTab.Chats
                },
                onGoFeed = {
                    openChatId = null
                    supportOpen = false
                    tab = BottomTab.Feed
                },
                onGoSettings = {
                    openChatId = null
                    supportOpen = false
                    tab = BottomTab.Settings
                },
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            GlassBackground()

            val overlayActive = supportOpen || !openChatId.isNullOrBlank()

            if (!overlayActive) {
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = {
                        val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        (fadeIn() + slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it * dir })
                            .togetherWith(
                                fadeOut() + slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it * dir }
                            )
                    },
                    label = "tabTransition"
                ) { t ->
                    when (t) {
                        BottomTab.Feed -> FeedScreen(
                            repo = repo,
                            padding = padding,
                            onOpenChat = { chatId ->
                                tab = BottomTab.Chats
                                openChatId = chatId
                            }
                        )
                        BottomTab.Chats -> ChatsScreen(
                            repo = repo,
                            padding = padding,
                            onOpenChat = { chatId -> openChatId = chatId },
                            onOpenSupport = { supportOpen = true }
                        )
                        BottomTab.Settings -> SettingsScreen(
                            repo = repo,
                            padding = padding,
                            onOpenSupport = {
                                tab = BottomTab.Chats
                                supportOpen = true
                            }
                        )
                    }
                }
            } else {
                // When an overlay is open, do NOT render the underlying tab UI (prevents visual overlap).
                AnimatedContent(
                    targetState = Pair(openChatId, supportOpen),
                    transitionSpec = {
                        (fadeIn() + slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it / 6 })
                            .togetherWith(
                                fadeOut() + slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it / 6 }
                            )
                    },
                    label = "overlayTransition"
                ) { (chatId, support) ->
                    when {
                        support -> SupportChatScreen(
                            repo = repo,
                            padding = padding,
                            onBack = { supportOpen = false }
                        )
                        !chatId.isNullOrBlank() -> ChatScreen(
                            repo = repo,
                            chatId = chatId,
                            padding = padding,
                            onBack = { openChatId = null }
                        )
                    }
                }
            }
        }
    }
}


