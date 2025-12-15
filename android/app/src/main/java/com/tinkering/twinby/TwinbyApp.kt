package com.tinkering.twinby

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tinkering.twinby.data.Repository
import com.tinkering.twinby.data.TokenStore
import com.tinkering.twinby.ui.screens.AuthScreen
import com.tinkering.twinby.ui.screens.ChatScreen
import com.tinkering.twinby.ui.screens.ChatsScreen
import com.tinkering.twinby.ui.screens.FeedScreen
import com.tinkering.twinby.ui.screens.SettingsScreen
import com.tinkering.twinby.ui.screens.SupportChatScreen
import com.tinkering.twinby.ui.screens.BottomNavBar

sealed class Route(val value: String) {
    data object Auth : Route("auth")
    data object Chats : Route("chats")
    data object Feed : Route("feed")
    data object Settings : Route("settings")
    data object Support : Route("chats/support")
    data object Chat : Route("chats/{chatId}") {
        fun create(chatId: String) = "chats/$chatId"
    }
}

@Composable
fun TwinbyApp() {
    val nav = rememberNavController()
    val tokenStore = remember { TokenStore() }
    val repo = remember { Repository(tokenStore) }

    AppNavHost(nav, repo, tokenStore)
}

@Composable
private fun AppNavHost(nav: NavHostController, repo: Repository, tokenStore: TokenStore) {
    val backStack = nav.currentBackStackEntryAsState()
    val route = backStack.value?.destination?.route ?: ""
    val showBottom = route != Route.Auth.value

    Scaffold(
        bottomBar = {
            if (showBottom) {
                BottomNavBar(
                    currentRoute = route,
                    onGoChats = { nav.navigate(Route.Chats.value) { launchSingleTop = true } },
                    onGoFeed = { nav.navigate(Route.Feed.value) { launchSingleTop = true } },
                    onGoSettings = { nav.navigate(Route.Settings.value) { launchSingleTop = true } },
                )
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = Route.Auth.value) {
            composable(Route.Auth.value) {
                AuthScreen(
                    repo = repo,
                    tokenStore = tokenStore,
                    onAuthed = { nav.navigate(Route.Feed.value) { popUpTo(Route.Auth.value) { inclusive = true } } }
                )
            }

            composable(Route.Feed.value) {
                FeedScreen(repo = repo, padding = padding, onOpenChat = { chatId -> nav.navigate(Route.Chat.create(chatId)) })
            }
            composable(Route.Chats.value) {
                ChatsScreen(
                    repo = repo,
                    padding = padding,
                    onOpenChat = { chatId -> nav.navigate(Route.Chat.create(chatId)) },
                    onOpenSupport = { nav.navigate(Route.Support.value) }
                )
            }
            composable(Route.Settings.value) {
                SettingsScreen(
                    repo = repo,
                    padding = padding,
                    onOpenSupport = { nav.navigate(Route.Support.value) }
                )
            }
            composable(Route.Support.value) {
                SupportChatScreen(repo = repo, padding = padding)
            }
            composable(Route.Chat.value) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId").orEmpty()
                ChatScreen(repo = repo, chatId = chatId, onBack = { nav.popBackStack() })
            }
        }
    }
}


