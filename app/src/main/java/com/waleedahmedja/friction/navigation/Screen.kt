package com.waleedahmedja.friction.navigation

sealed class Screen(val route: String) {
    object Home         : Screen("home")
    object TapChallenge : Screen("tap_challenge")
    object Reflection   : Screen("reflection")
    object Completion   : Screen("completion")
    object Settings     : Screen("settings")
    object BlockedApps  : Screen("blocked_apps")
    object About        : Screen("about")
    object Privacy      : Screen("privacy")
}