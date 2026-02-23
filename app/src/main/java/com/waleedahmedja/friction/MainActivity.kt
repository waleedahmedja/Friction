package com.waleedahmedja.friction

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.waleedahmedja.friction.navigation.Screen
import com.waleedahmedja.friction.ui.screens.CompletionScreen
import com.waleedahmedja.friction.ui.screens.HomeScreen
import com.waleedahmedja.friction.ui.screens.LockScreen
import com.waleedahmedja.friction.ui.screens.ReflectionScreen
import com.waleedahmedja.friction.ui.screens.TapChallengeScreen
import com.waleedahmedja.friction.ui.theme.Black
import com.waleedahmedja.friction.ui.theme.FrictionTheme
import com.waleedahmedja.friction.viewmodel.FrictionViewModel

class MainActivity : ComponentActivity() {

    private val vm: FrictionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FrictionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = Black
                ) {
                    val nav       = rememberNavController()
                    val isStandby by vm.isStandby.collectAsStateWithLifecycle()

                    LaunchedEffect(isStandby) {
                        requestedOrientation = if (isStandby)
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        else
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }

                    NavHost(nav, startDestination = Screen.Home.route) {

                        composable(Screen.Home.route) {
                            LaunchedEffect(Unit) {
                                requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                vm.exitStandby()
                            }
                            HomeScreen(vm) {
                                nav.navigate(Screen.Lock.route) {
                                    launchSingleTop = true
                                }
                            }
                        }

                        composable(Screen.Lock.route) {
                            LockScreen(
                                vm = vm,
                                onExit = {
                                    nav.navigate(Screen.TapChallenge.route) {
                                        launchSingleTop = true
                                    }
                                },
                                onExpire = {
                                    requestedOrientation =
                                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    vm.exitStandby()
                                    nav.navigate(Screen.Completion.route) {
                                        popUpTo(Screen.Home.route)
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(Screen.TapChallenge.route) {
                            TapChallengeScreen(
                                vm = vm,
                                onDone = {
                                    requestedOrientation =
                                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    nav.navigate(Screen.Reflection.route) {
                                        launchSingleTop = true
                                    }
                                },
                                onBack = {
                                    nav.navigate(Screen.Lock.route) {
                                        popUpTo(Screen.Lock.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(Screen.Reflection.route) {
                            LaunchedEffect(Unit) {
                                requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                            ReflectionScreen(vm) {
                                nav.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }

                        composable(Screen.Completion.route) {
                            LaunchedEffect(Unit) {
                                requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                            CompletionScreen(vm) {
                                nav.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}