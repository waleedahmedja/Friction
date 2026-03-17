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

class TapChallengeOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
    }

    private val vm: FrictionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            FrictionAppTheme {
                OverlayRoot(vm = vm, onDone = {
                    // Navigate back to main app after tap challenge
                    packageManager.getLaunchIntentForPackage(packageName)
                        ?.apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                        ?.let { startActivity(it) }
                    finish()
                })
            }
        }
    }
}

@Composable
private fun OverlayRoot(vm: FrictionViewModel, onDone: () -> Unit) {
    val postDest by vm.postTapDestination.collectAsStateWithLifecycle()

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
        onReflection = {},
        onHome       = {},
        onBack       = onDone
    )
}
