package com.danego.voicechat.navigation

import androidx.navigation.NavHostController

class NavRoutes(navController: NavHostController) {

    val splashToLogin: () -> Unit = {
        navController.navigate(route = NavGraph.LoginScreen.route) {
            popUpTo(NavGraph.SplashScreen.route) { inclusive = true }
        }
    }

    val splashToMain: () -> Unit = {
        navController.navigate(route = NavGraph.MessageListScreen.route) {
            popUpTo(NavGraph.SplashScreen.route) { inclusive = true }
        }
    }

    val loginToMain: () -> Unit = {
        navController.navigate(route = NavGraph.MessageListScreen.route) {
            popUpTo(NavGraph.LoginScreen.route) { inclusive = true }
        }
    }

    val messageListToMessage: (String) -> Unit = {
        navController.navigate(route = "${NavGraph.MessageScreen.baseRoute}/${it}")
    }

    val messageToMessageList: () -> Unit = {
        navController.navigate(route = NavGraph.MessageListScreen.route) {
            popUpTo(NavGraph.MessageScreen.baseRoute) { inclusive = true }
        }
    }

    val messageListToSearch: () -> Unit = {
        navController.navigate(route = NavGraph.SearchScreen.route)
    }

    val searchToMessageList: () -> Unit = {
        navController.navigate(route = NavGraph.MessageListScreen.route) {
            popUpTo(NavGraph.SearchScreen.route) { inclusive = true }
        }
    }

    val searchToMessage: (String) -> Unit = {
        navController.navigate(route = "${NavGraph.MessageScreen.baseRoute}/${it}") {
            popUpTo(NavGraph.SearchScreen.route) { inclusive = true }
        }
    }

    val messageListToSettings: () -> Unit = {
        navController.navigate(route = "${NavGraph.SettingsScreen.baseRoute}/?")
    }

    val settingsToMessageList: () -> Unit = {
        navController.navigate(route = NavGraph.MessageListScreen.route) {
            popUpTo(NavGraph.SettingsScreen.baseRoute) { inclusive = true }
        }
    }

    val settingsToLogin: () -> Unit = {
        navController.navigate(route = NavGraph.LoginScreen.route) {
            popUpTo(NavGraph.SettingsScreen.baseRoute) { inclusive = true }
            popUpTo(NavGraph.MessageListScreen.route) { inclusive = true }
        }
    }

    val messageToSettings: (String) -> Unit = {
        navController.navigate(route = "${NavGraph.SettingsScreen.baseRoute}/$it")
    }

    val settingsToMessage: () -> Unit = {
        navController.popBackStack()
    }

}