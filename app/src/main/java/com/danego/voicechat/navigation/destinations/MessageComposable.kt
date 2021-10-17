package com.danego.voicechat.navigation.destinations

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.danego.voicechat.navigation.NavGraph
import com.danego.voicechat.ui.screens.message.MessageScreen
import com.google.accompanist.navigation.animation.composable

@ExperimentalAnimationApi
fun NavGraphBuilder.messageComposable(
    messageToMessageList: () -> Unit,
    messageToSettings: (String) -> Unit
) {
    composable(
        route = NavGraph.MessageScreen.route,
        arguments = listOf(navArgument("userEmail") {
            type = NavType.StringType
        }),
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
    ) { navBackStackEntry ->

        navBackStackEntry.arguments?.getString("userEmail")?.also { userEmail ->
            MessageScreen(
                userEmail = userEmail,
                messageToMessageList = messageToMessageList,
                messageToSettings = messageToSettings
            )
        }

    }
}