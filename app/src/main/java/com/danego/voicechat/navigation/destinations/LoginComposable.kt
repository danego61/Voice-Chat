package com.danego.voicechat.navigation.destinations

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.MutableState
import androidx.navigation.NavGraphBuilder
import com.danego.voicechat.navigation.NavGraph
import com.danego.voicechat.ui.screens.login.LoginScreen
import com.google.accompanist.navigation.animation.composable

@ExperimentalAnimationApi
fun NavGraphBuilder.loginComposable(
    choiceUserImage: (MutableState<String>) -> Unit,
    loginToMain: () -> Unit
) {
    composable(
        route = NavGraph.LoginScreen.route,
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
        LoginScreen(
            choiceUserImage = choiceUserImage,
            loginToMain = loginToMain
        )
    }
}