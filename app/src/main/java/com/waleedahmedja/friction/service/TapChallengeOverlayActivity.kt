package com.waleedahmedja.friction.service

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.ui.screens.TapChallengeScreen
import com.waleedahmedja.friction.ui.theme.FrictionAppTheme
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import com.waleedahmedja.friction.viewmodel.PostTapDestination

/**
 * Full-screen activity that overlays the blocked app.
 * Launched by FrictionAccessibilityService when it detects a blocked package
 * in the foreground during an active focus session.
 *
 * Uses FrictionAppTheme — same as MainActivity. The window theme in the
 * manifest (Theme.Friction.Overlay) only controls the window chrome.
 */
class TapChallengeOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
    }

    private val vm: FrictionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            // FrictionAppTheme — NOT FrictionTheme (which doesn't exist)
            FrictionAppTheme {
                OverlayContent(
                    vm     = vm,
                    onDone = {
                        // Return to main app after tap challenge completes
                        packageManager.getLaunchIntentForPackage(packageName)
                            ?.apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                            ?.let { startActivity(it) }
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun OverlayContent(vm: FrictionViewModel, onDone: () -> Unit) {
    val postDest by vm.postTapDestination.collectAsStateWithLifecycle()

    // When the tap challenge finishes, the ViewModel signals via postTapDestination.
    // We close this overlay so the main app (MainActivity) can handle the routing.
    LaunchedEffect(postDest) {
        when (postDest) {
            PostTapDestination.REFLECTION,
            PostTapDestination.HOME -> {
                vm.clearPostTapDestination()
                onDone()
            }
            else -> Unit
        }
    }

    TapChallengeScreen(
        vm           = vm,
        onReflection = { /* handled above */ },
        onHome       = { /* handled above */ },
        onBack       = onDone
    )
}