package com.danego.voicechat.navigation.destinations

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.danego.voicechat.navigation.NavGraph
import com.danego.voicechat.ui.screens.settings.SettingsScreen
import com.google.accompanist.navigation.animation.composable

@ExperimentalAnimationApi
fun NavGraphBuilder.settingsComposable(
    settingsToLogin: () -> Unit,
    settingsToMessageList: () -> Unit,
    settingsToMessage: () -> Unit
) {
    composable(
        route = NavGraph.SettingsScreen.route,
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

        navBackStackEntry.arguments?.getString("userEmail")?.let {
            SettingsScreen(
                settingsToLogin = settingsToLogin,
                settingsToMessageList = settingsToMessageList,
                userEmail = it,
                settingsToMessage = settingsToMessage
            )
        }

    }
}