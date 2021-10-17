package com.danego.voicechat.navigation

import android.annotation.SuppressLint

sealed class NavGraph(val route: String, val baseRoute: String = "") {

    @SuppressLint("CustomSplashScreen")
    object SplashScreen : NavGraph(route = "Splash")

    object LoginScreen : NavGraph(route = "Login")

    object MessageListScreen : NavGraph(route = "MessageList")

    object SearchScreen : NavGraph(route = "Search")

    object SettingsScreen : NavGraph(route = "Settings/{userEmail}", baseRoute = "Settings")

    object MessageScreen : NavGraph(
        route = "Message/{userEmail}",
        baseRoute = "Message"
    )

}
