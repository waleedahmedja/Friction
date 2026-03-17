package com.waleedahmedja.friction

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.waleedahmedja.friction.navigation.Screen
import com.waleedahmedja.friction.ui.screens.*
import com.waleedahmedja.friction.ui.theme.FrictionAppTheme
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import com.waleedahmedja.friction.viewmodel.PostTapDestination

// FragmentActivity is the correct superclass here.
// It is the parent of ComponentActivity and provides everything we need.
// Declaring it explicitly means `this` has static type FragmentActivity,
// which satisfies FrictionApp's `activity: FragmentActivity` parameter
// without any cast.
class MainActivity : FragmentActivity() {

    private val vm: FrictionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            FrictionAppTheme {
                // Pass `this` (the actual FragmentActivity) directly into the
                // composition tree. This avoids walking the ContextWrapper chain
                // which fails on some OEM devices (e.g. Transsion) because their
                // Navigation implementation doesn't propagate the Activity context.
                FrictionApp(vm = vm, activity = this)
            }
        }
    }
}

@Composable
private fun FrictionApp(
    vm      : FrictionViewModel,
    activity: FragmentActivity          // the real MainActivity, passed directly
) {
    val nav        = rememberNavController()
    val postDest   by vm.postTapDestination.collectAsStateWithLifecycle()
    val completion by vm.completion        .collectAsStateWithLifecycle()

    // Session complete → Completion screen
    LaunchedEffect(completion.visible) {
        if (completion.visible) {
            nav.navigate(Screen.Completion.route) { popUpTo(0) { inclusive = true } }
        }
    }

    // Post-tap routing
    LaunchedEffect(postDest) {
        when (postDest) {
            PostTapDestination.REFLECTION -> {
                vm.clearPostTapDestination()
                nav.navigate(Screen.Reflection.route) {
                    popUpTo(Screen.TapChallenge.route) { inclusive = true }
                }
            }
            PostTapDestination.HOME -> {
                vm.clearPostTapDestination()
                nav.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
            }
            PostTapDestination.NONE -> Unit
        }
    }

    NavHost(navController = nav, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            FocusScreen(
                vm             = vm,
                activity       = activity,          // ← passed directly, no context chain
                onTapChallenge = { nav.navigate(Screen.TapChallenge.route) },
                onGraceCancel  = { },
                onExpire       = { },
                onSettings     = { nav.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.TapChallenge.route) {
            TapChallengeScreen(
                vm           = vm,
                onReflection = {
                    nav.navigate(Screen.Reflection.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onHome       = { nav.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } },
                onBack       = { nav.popBackStack() }
            )
        }

        composable(Screen.Reflection.route) {
            ReflectionScreen(
                vm         = vm,
                onContinue = { nav.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(Screen.Completion.route) {
            CompletionScreen(
                vm     = vm,
                onHome = { nav.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                vm            = vm,
                onBack        = { nav.popBackStack() },
                onBlockedApps = { nav.navigate(Screen.BlockedApps.route) },
                onAbout       = { nav.navigate(Screen.About.route) }
            )
        }

        composable(Screen.BlockedApps.route) {
            BlockedAppsScreen(vm = vm, onBack = { nav.popBackStack() })
        }

        composable(Screen.About.route) {
            AboutScreen(
                vm        = vm,
                onBack    = { nav.popBackStack() },
                onPrivacy = { nav.navigate(Screen.Privacy.route) }
            )
        }

        composable(Screen.Privacy.route) {
            PrivacyPolicyScreen(onBack = { nav.popBackStack() })
        }
    }
}
