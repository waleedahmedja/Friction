package com.waleedahmedja.friction.navigation

sealed class Screen(val route: String) {
    object Home         : Screen("home")
    object Lock         : Screen("lock")
    object TapChallenge : Screen("tap_challenge")
    object Reflection   : Screen("reflection")
    object Completion   : Screen("completion")
}