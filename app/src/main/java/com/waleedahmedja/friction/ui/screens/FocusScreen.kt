package com.waleedahmedja.friction.ui.screens

// FocusScreen — idle wheel picker, active countdown clock.
// Biometric gate wired into the exit flow via the activity parameter.
// The activity is passed directly from MainActivity — no ContextWrapper
// walking which fails on certain OEM devices (Transsion, some Xiaomi ROMs).

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.ui.accentGradient
import com.waleedahmedja.friction.ui.components.GradientButton
import com.waleedahmedja.friction.ui.components.InfiniteWheel
import com.waleedahmedja.friction.ui.theme.FrictionColors
import com.waleedahmedja.friction.ui.theme.FrictionTheme
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun FocusScreen(
    vm            : FrictionViewModel,
    activity      : FragmentActivity,   // passed from MainActivity — never from LocalContext
    onTapChallenge: () -> Unit,
    onGraceCancel : () -> Unit,
    onExpire      : () -> Unit,
    onSettings    : () -> Unit
) {
    val c                 = FrictionTheme.c
    val lock              by vm.lock              .collectAsStateWithLifecycle()
    val isStandby         by vm.isStandby         .collectAsStateWithLifecycle()
    val graceActive       by vm.graceActive       .collectAsStateWithLifecycle()
    val ambientPref       by vm.ambientModePref   .collectAsStateWithLifecycle()
    val hapticPref        by vm.hapticEnabled     .collectAsStateWithLifecycle()
    val biometricRequired by vm.biometricRequired .collectAsStateWithLifecycle()
    val lifecycle         = LocalLifecycleOwner.current
    val isLocked          = lock.isActive

    // Pause/resume timer with app lifecycle
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (isLocked) vm.resumeTimer()
                Lifecycle.Event.ON_PAUSE  -> vm.pauseTimer()
                else                      -> Unit
            }
        }
        lifecycle.lifecycle.addObserver(obs)
        onDispose { lifecycle.lifecycle.removeObserver(obs) }
    }

    // Fire onExpire exactly once when the session timer reaches zero
    var expireHandled by remember { mutableStateOf(false) }
    LaunchedEffect(lock.isActive) { if (lock.isActive) expireHandled = false }
    LaunchedEffect(lock.isActive, lock.remainingMs) {
        if (!lock.isActive && lock.remainingMs == 0L && lock.unlockAt > 0L && !expireHandled) {
            expireHandled = true
            onExpire()
        }
    }

    // Time components from remaining ms
    val rem = lock.remainingMs.coerceAtLeast(0L)
    val hh  = (rem / 3_600_000L).toInt()
    val mm  = ((rem % 3_600_000L) / 60_000L).toInt()
    val ss  = ((rem % 60_000L) / 1_000L).toInt()

    // Ambient mode — dims screen after 90 s of no touch
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var ambientActive   by remember { mutableStateOf(false) }
    LaunchedEffect(isLocked) {
        if (!isLocked) { ambientActive = false; return@LaunchedEffect }
        while (true) {
            delay(5_000L)
            if (ambientPref && !ambientActive &&
                System.currentTimeMillis() - lastInteraction > 90_000L
            ) ambientActive = true
        }
    }

    // OLED pixel drift — micro-shifts layout to prevent burn-in
    var driftX by remember { mutableFloatStateOf(0f) }
    var driftY by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isLocked, c.isDark) {
        if (!isLocked || !c.isDark) { driftX = 0f; driftY = 0f; return@LaunchedEffect }
        val positions = listOf(0f to 0f, 6f to -4f, -5f to 5f, 4f to 7f, -6f to -3f)
        var i = 0
        while (true) {
            delay((45_000L..65_000L).random())
            driftX = positions[i % positions.size].first
            driftY = positions[i % positions.size].second
            i++
        }
    }
    val animDriftX by animateFloatAsState(
        targetValue   = driftX,
        animationSpec = tween(3_000, easing = FastOutSlowInEasing),
        label         = "driftX"
    )
    val animDriftY by animateFloatAsState(
        targetValue   = driftY,
        animationSpec = tween(3_000, easing = FastOutSlowInEasing),
        label         = "driftY"
    )

    // Clock animations
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val opacityPulse by pulseAnim.animateFloat(
        initialValue  = 0.97f, targetValue  = 1.00f,
        animationSpec = infiniteRepeatable(
            tween(3_000, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "opacityPulse"
    )
    val colonAnim = rememberInfiniteTransition(label = "colon")
    val colonAlpha by colonAnim.animateFloat(
        initialValue  = 1f, targetValue  = 0.18f,
        animationSpec = infiniteRepeatable(
            tween(500, easing = LinearEasing), RepeatMode.Reverse
        ),
        label = "colonAlpha"
    )

    var showExitModal by remember { mutableStateOf(false) }

    // Biometric gate — only called when user confirms they want to end.
    // activity is already the real FragmentActivity (passed from MainActivity).
    // No context walking, no cast, no crash.
    val proceedToTapChallenge: () -> Unit = remember(biometricRequired) {
        {
            if (biometricRequired) {
                vm.launchBiometricPrompt(activity) { onTapChallenge() }
            } else {
                onTapChallenge()
            }
        }
    }

    // ── Standby screen ────────────────────────────────────────────────────────
    if (isLocked && isStandby) {
        StandbyScreen(vm = vm, onBack = { vm.exitStandby() })
        return
    }

    // ── Ambient mode overlay ──────────────────────────────────────────────────
    if (isLocked && ambientActive) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (c.isDark) Color.Black else c.bg)
                .pointerInput(Unit) {
                    detectTapGestures {
                        ambientActive   = false
                        lastInteraction = System.currentTimeMillis()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "%02d:%02d:%02d".format(hh, mm, ss),
                style = TextStyle(
                    fontSize   = 40.sp,
                    fontWeight = FontWeight.Thin,
                    color      = c.text.copy(alpha = 0.55f),
                    fontFamily = FontFamily.Monospace
                ),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // ── Main screen ───────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .pointerInput(isLocked) {
                if (isLocked) detectTapGestures {
                    lastInteraction = System.currentTimeMillis()
                    ambientActive   = false
                }
            }
    ) {

        // ── Top bar ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp)
        ) {
            // Back arrow — locked state only
            AnimatedVisibility(
                visible  = isLocked,
                modifier = Modifier.align(Alignment.CenterStart),
                enter    = fadeIn(tween(300)),
                exit     = fadeOut(tween(300))
            ) {
                Box(
                    modifier = Modifier.size(44.dp).pointerInput(Unit) {
                        detectTapGestures {
                            lastInteraction = System.currentTimeMillis()
                            showExitModal   = true
                        }
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", style = TextStyle(fontSize = 22.sp, color = c.textHint))
                }
            }

            // App name — idle only
            AnimatedVisibility(
                visible  = !isLocked,
                modifier = Modifier.align(Alignment.Center),
                enter    = fadeIn(tween(300)),
                exit     = fadeOut(tween(300))
            ) {
                Text(
                    "FRICTION",
                    style = TextStyle(
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Medium,
                        color         = c.textHint,
                        letterSpacing = 3.sp
                    )
                )
            }

            // Settings — idle only
            AnimatedVisibility(
                visible  = !isLocked,
                modifier = Modifier.align(Alignment.CenterEnd),
                enter    = fadeIn(tween(300)),
                exit     = fadeOut(tween(300))
            ) {
                Box(
                    modifier = Modifier.size(44.dp)
                        .pointerInput(Unit) { detectTapGestures { onSettings() } },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙", style = TextStyle(fontSize = 20.sp, color = c.textHint))
                }
            }
        }

        // ── Centre ────────────────────────────────────────────────────────────
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(0.3f))

            // Countdown clock — active session
            AnimatedVisibility(
                visible = isLocked,
                enter   = fadeIn(tween(400)),
                exit    = fadeOut(tween(300))
            ) {
                Box(modifier = Modifier.offset {
                    IntOffset(animDriftX.dp.roundToPx(), animDriftY.dp.roundToPx())
                }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier              = Modifier.alpha(opacityPulse)
                        ) {
                            ClockDigit(hh)
                            ClockColon(colonAlpha, c)
                            ClockDigit(mm)
                            ClockColon(colonAlpha, c)
                            ClockDigit(ss)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Remaining",
                            style = TextStyle(
                                fontSize      = 12.sp,
                                color         = c.textHint,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }

            // Duration picker — idle
            AnimatedVisibility(
                visible = !isLocked,
                enter   = fadeIn(tween(400)),
                exit    = fadeOut(tween(300))
            ) {
                DurationPicker(vm = vm, hapticEnabled = hapticPref)
            }

            // Progress bar + end time — active session
            AnimatedVisibility(
                visible = isLocked,
                enter   = fadeIn(tween(600)),
                exit    = fadeOut(tween(200))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(36.dp))
                    Box(
                        modifier = Modifier
                            .width(240.dp).height(1.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(c.text.copy(alpha = 0.08f))
                    ) {
                        val animProg by animateFloatAsState(
                            targetValue   = lock.progressFraction,
                            animationSpec = tween(1_000),
                            label         = "sessionProgress"
                        )
                        Box(
                            Modifier.fillMaxWidth(animProg).fillMaxHeight()
                                .clip(RoundedCornerShape(1.dp)).background(accentGradient)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    if (lock.sessionEndTime.isNotBlank()) {
                        Text(
                            "Ends at ${lock.sessionEndTime}",
                            style = TextStyle(fontSize = 12.sp, color = c.textHint)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))
        }

        // ── Bottom buttons ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Start Focus — idle
            AnimatedVisibility(
                visible = !isLocked,
                enter   = fadeIn(tween(300)),
                exit    = fadeOut(tween(200))
            ) {
                val total by vm.totalMinutes.collectAsStateWithLifecycle()
                GradientButton(
                    text    = "Start Focus",
                    onClick = { if (total > 0) vm.startLock() },
                    enabled = total > 0
                )
            }

            // Enter Standby — locked, outside grace window
            AnimatedVisibility(
                visible = isLocked && !graceActive,
                enter   = fadeIn(tween(400)),
                exit    = fadeOut(tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(50.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(c.surface)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                lastInteraction = System.currentTimeMillis()
                                vm.enterStandby()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Enter Standby", style = TextStyle(fontSize = 15.sp, color = c.textSub))
                }
            }

            // Cancel Session — grace window only
            AnimatedVisibility(
                visible = isLocked && graceActive,
                enter   = fadeIn(tween(300)),
                exit    = fadeOut(tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(50.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(c.surface)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                vm.recordEarlyExit()
                                onGraceCancel()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel session", style = TextStyle(fontSize = 15.sp, color = c.textSub))
                }
            }
        }

        // ── Exit modal ────────────────────────────────────────────────────────
        if (showExitModal) {
            ExitSessionModal(
                c                 = c,
                biometricRequired = biometricRequired,
                onContinue        = { showExitModal = false },
                onChallenge       = { showExitModal = false; proceedToTapChallenge() }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Duration picker
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DurationPicker(vm: FrictionViewModel, hapticEnabled: Boolean) {
    val c       = FrictionTheme.c
    val hours   by vm.hours      .collectAsStateWithLifecycle()
    val minutes by vm.minutes    .collectAsStateWithLifecycle()
    val total   by vm.totalMinutes.collectAsStateWithLifecycle()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            InfiniteWheel(range = 0..23, selected = hours,   onSelect = vm::setHours,   hapticEnabled = hapticEnabled)
            Spacer(Modifier.width(4.dp))
            Text("hr",  style = TextStyle(fontSize = 13.sp, color = c.textHint, fontWeight = FontWeight.Light))
            Spacer(Modifier.width(24.dp))
            InfiniteWheel(range = 0..59, selected = minutes, onSelect = vm::setMinutes, hapticEnabled = hapticEnabled)
            Spacer(Modifier.width(4.dp))
            Text("min", style = TextStyle(fontSize = 13.sp, color = c.textHint, fontWeight = FontWeight.Light))
        }
        Spacer(Modifier.height(20.dp))
        val label = when {
            total == 0      -> "Set a duration"
            total < 60      -> "$total minutes"
            total % 60 == 0 -> "${total / 60} hours"
            else            -> "${total / 60} hr ${total % 60} min"
        }
        Text(
            text      = label,
            style     = TextStyle(
                fontSize   = 15.sp,
                color      = if (total > 0) c.textSub else c.textHint,
                fontWeight = FontWeight.Light
            ),
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Clock sub-components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ClockDigit(value: Int) {
    val c = FrictionTheme.c
    AnimatedContent(
        targetState    = "%02d".format(value),
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
        label          = "clockDigit"
    ) { v ->
        Text(
            text  = v,
            style = TextStyle(
                fontSize   = 68.sp,
                fontWeight = FontWeight.Bold,
                color      = c.text,
                fontFamily = FontFamily.Monospace
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ClockColon(alpha: Float, c: FrictionColors) {
    Text(
        ":",
        style    = TextStyle(
            fontSize   = 60.sp,
            fontWeight = FontWeight.Bold,
            color      = c.text.copy(alpha = alpha),
            fontFamily = FontFamily.Monospace
        ),
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Exit confirmation modal
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExitSessionModal(
    c                 : FrictionColors,
    biometricRequired : Boolean,
    onContinue        : () -> Unit,
    onChallenge       : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (c.isDark) 0.92f else 0.60f))
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(c.surface)
                .padding(horizontal = 24.dp, vertical = 36.dp)
                .pointerInput(Unit) { detectTapGestures { } },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "End this session?",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = c.text)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (biometricRequired)
                    "Verify your identity first, then complete the tap challenge."
                else
                    "You'll need to complete the tap challenge to exit.",
                style     = TextStyle(fontSize = 14.sp, color = c.textSub),
                textAlign = TextAlign.Center
            )

            if (biometricRequired) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(c.surface2)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("🔒", style = TextStyle(fontSize = 12.sp))
                    Text(
                        "Biometric required",
                        style = TextStyle(fontSize = 12.sp, color = c.textHint)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth().height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(accentGradient)
                    .pointerInput(Unit) { detectTapGestures { onChallenge() } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = if (biometricRequired) "Verify & Exit" else "Start Tap Challenge",
                    style = TextStyle(
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = c.btnText
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth().height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(c.surface2)
                    .pointerInput(Unit) { detectTapGestures { onContinue() } },
                contentAlignment = Alignment.Center
            ) {
                Text("Continue Focus", style = TextStyle(fontSize = 16.sp, color = c.textSub))
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
