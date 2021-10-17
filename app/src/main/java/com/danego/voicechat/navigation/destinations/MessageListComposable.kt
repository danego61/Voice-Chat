package com.danego.voicechat.navigation.destinations

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavGraphBuilder
import com.danego.voicechat.navigation.NavGraph
import com.danego.voicechat.ui.screens.messagelist.MessageListScreen
import com.google.accompanist.navigation.animation.composable

@ExperimentalAnimationApi
fun NavGraphBuilder.messageListComposable(
    messageListToMessage: (String) -> Unit,
    messageListToSearch: () -> Unit,
    messageListToSettings: () -> Unit
) {
    composable(
        route = NavGraph.MessageListScreen.route,
        exitTransition = { _, _ ->
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 500)
            )
        },
        enterTransition = { _, _ ->
            slideInHorizontally(
                initialOffsetX = { fullWidth -> 2 * fullWidth },
                animationSpec = tween(durationMillis = 500)
            )
        }
    ) {
        MessageListScreen(
            messageListToMessage = messageListToMessage,
            messageListToSearch = messageListToSearch,
            messageListToSettings = messageListToSettings
        )
    }
}