package com.danego.voicechat.navigation.destinations

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavGraphBuilder
import com.danego.voicechat.navigation.NavGraph
import com.danego.voicechat.ui.screens.splash.SplashScreen
import com.google.accompanist.navigation.animation.composable

@ExperimentalAnimationApi
fun NavGraphBuilder.splashComposable(
    splashToLogin: () -> Unit,
    splashToMain: () -> Unit
) {
    composable(
        route = NavGraph.SplashScreen.route,
        exitTransition = { _, _ ->
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 500)
            )
        }
    ) {
        SplashScreen(
            splashToLogin = splashToLogin,
            splashToMain = splashToMain
        )
    }
}