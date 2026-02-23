package com.waleedahmedja.friction.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import com.waleedahmedja.friction.ui.accentGradient
import com.waleedahmedja.friction.ui.theme.*

@Composable
fun LockScreen(
    vm      : FrictionViewModel,
    onExit  : () -> Unit,
    onExpire: () -> Unit
) {
    val lock           by vm.lock.collectAsStateWithLifecycle()
    val isStandby      by vm.isStandby.collectAsStateWithLifecycle()
    val totalMinutes   by vm.totalMinutes.collectAsStateWithLifecycle()
    val lifecycleOwner  = LocalLifecycleOwner.current
    var showExitModal   by remember { mutableStateOf(false) }
    var expireHandled   by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> vm.resumeTimer()
                Lifecycle.Event.ON_PAUSE  -> vm.pauseTimer()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    LaunchedEffect(lock.isActive, lock.remainingMs) {
        if (!lock.isActive && lock.remainingMs == 0L && lock.unlockAt > 0L && !expireHandled) {
            expireHandled = true
            vm.showCompletion(totalMinutes)
            onExpire()
        }
    }

    val colonAlpha by rememberInfiniteTransition(label = "colon").animateFloat(
        initialValue  = 1f,
        targetValue   = 0.20f,
        animationSpec = infiniteRepeatable(
            animation  = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    val rem = lock.remainingMs.coerceAtLeast(0L)
    val hh  = (rem / 3_600_000L).toInt()
    val mm  = ((rem % 3_600_000L) / 60_000L).toInt()
    val ss  = ((rem % 60_000L) / 1_000L).toInt()

    if (isStandby) {
        StandbyScreen(
            lock = lock,
            hh = hh, mm = mm, ss = ss,
            onReturn = { vm.exitStandby() },
            onEndSession = {
                vm.exitStandby()
                vm.recordEarlyExit()
                onExit()
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        // Back arrow — top left
        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = 24.dp)
                .size(44.dp)
                .align(Alignment.TopStart)
                .pointerInput(Unit) { detectTapGestures { showExitModal = true } },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "←",
                style = TextStyle(fontSize = 22.sp, color = White.copy(alpha = 0.70f))
            )
        }

        // Standby button — top right
        Box(
            modifier = Modifier
                .padding(end = 16.dp, top = 24.dp)
                .size(44.dp)
                .align(Alignment.TopEnd)
                .pointerInput(Unit) { detectTapGestures { vm.enterStandby() } },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "⤢",
                style = TextStyle(fontSize = 20.sp, color = White.copy(alpha = 0.70f))
            )
        }

        // Centre content
        Column(
            modifier              = Modifier.fillMaxSize(),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.40f))

            // HH:MM:SS countdown with animated digits
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AnimatedDigitGroup(value = hh)
                ColonDivider(alpha = colonAlpha)
                AnimatedDigitGroup(value = mm)
                ColonDivider(alpha = colonAlpha)
                AnimatedDigitGroup(value = ss)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text  = "Remaining",
                style = TextStyle(
                    fontSize      = 13.sp,
                    color         = White.copy(alpha = 0.40f),
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(Modifier.height(40.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(White.copy(alpha = 0.10f))
            ) {
                val animProg by animateFloatAsState(
                    targetValue   = lock.progressFraction,
                    animationSpec = tween(1_000),
                    label         = "lock_prog"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProg)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(1.dp))
                        .background(accentGradient)
                )
            }

            Spacer(Modifier.height(16.dp))

            if (lock.sessionEndTime.isNotBlank()) {
                Text(
                    text  = "Session ends at ${lock.sessionEndTime}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color    = White.copy(alpha = 0.35f)
                    )
                )
            }

            Spacer(Modifier.weight(1f))
        }

        if (showExitModal) {
            LockExitModal(
                onContinue = { showExitModal = false },
                onEnd      = {
                    showExitModal = false
                    vm.recordEarlyExit()
                    onExit()
                }
            )
        }
    }
}

@Composable
private fun AnimatedDigitGroup(value: Int) {
    AnimatedContent(
        targetState    = "%02d".format(value),
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
        label          = "digit"
    ) { v ->
        Text(
            text  = v,
            style = TextStyle(
                fontSize   = 68.sp,
                fontWeight = FontWeight.Bold,
                color      = White,
                fontFamily = FontFamily.Monospace
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ColonDivider(alpha: Float) {
    Text(
        text     = ":",
        style    = TextStyle(
            fontSize   = 60.sp,
            fontWeight = FontWeight.Bold,
            color      = White.copy(alpha = alpha),
            fontFamily = FontFamily.Monospace
        ),
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

@Composable
private fun LockExitModal(
    onContinue: () -> Unit,
    onEnd     : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black.copy(alpha = 0.94f))
            .pointerInput(Unit) { detectTapGestures { /* consume */ } }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFF1C1C1E))
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = "Finish lock instead?",
                style = TextStyle(
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = White
                )
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text      = "Returning will stop your session.",
                style     = TextStyle(fontSize = 14.sp, color = White.copy(alpha = 0.50f)),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Continue Focus — primary gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(accentGradient)
                    .pointerInput(Unit) { detectTapGestures { onContinue() } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Continue Focus",
                    style = TextStyle(
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Black
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // End Session — secondary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF2C2C2E))
                    .pointerInput(Unit) { detectTapGestures { onEnd() } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "End Session",
                    style = TextStyle(
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color      = White.copy(alpha = 0.60f)
                    )
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}