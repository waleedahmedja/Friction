package com.waleedahmedja.friction.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.admin.FrictionDeviceAdminReceiver
import com.waleedahmedja.friction.ui.accentGradient
import com.waleedahmedja.friction.ui.theme.AccentStart
import com.waleedahmedja.friction.ui.theme.Danger
import com.waleedahmedja.friction.ui.theme.FrictionColors
import com.waleedahmedja.friction.ui.theme.FrictionTheme
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import com.waleedahmedja.friction.viewmodel.TapDifficulty

@Composable
fun SettingsScreen(
    vm           : FrictionViewModel,
    onBack       : () -> Unit,
    onBlockedApps: () -> Unit,
    onAbout      : () -> Unit = {}
) {
    val c       = FrictionTheme.c
    val context = LocalContext.current

    val hardMode         by vm.hardMode         .collectAsStateWithLifecycle()
    val tapDifficulty    by vm.tapDifficulty     .collectAsStateWithLifecycle()
    val gracePeriodSecs  by vm.gracePeriodSecs   .collectAsStateWithLifecycle()
    val reflectionOnExit by vm.reflectionOnExit  .collectAsStateWithLifecycle()
    val standbyMode      by vm.standbyModePref   .collectAsStateWithLifecycle()
    val ambientMode      by vm.ambientModePref   .collectAsStateWithLifecycle()
    val hapticEnabled    by vm.hapticEnabled     .collectAsStateWithLifecycle()
    val biometricReq     by vm.biometricRequired .collectAsStateWithLifecycle()
    val lockSettings     by vm.lockSettingsDuring.collectAsStateWithLifecycle()
    val lock             by vm.lock              .collectAsStateWithLifecycle()
    val blockedCount     by vm.blockedPackages   .collectAsStateWithLifecycle()
    val allowedCount     by vm.allowedPackages   .collectAsStateWithLifecycle()

    val settingsLocked = lock.isActive && lockSettings

    // Read live permission status once per composition
    val accessibilityOn = remember { vm.isAccessibilityEnabled() }
    val adminOn         = remember { FrictionDeviceAdminReceiver.isAdminActive(context) }

    var showHardModeSheet by remember { mutableStateOf(false) }
    var showResetConfirm  by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(56.dp))

            // Top bar
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(44.dp).align(Alignment.CenterStart)
                        .pointerInput(Unit) { detectTapGestures { onBack() } },
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", style = TextStyle(fontSize = 22.sp, color = c.textHint))
                }
                Text(
                    "SETTINGS",
                    style    = TextStyle(
                        fontSize      = 11.sp, fontWeight   = FontWeight.Medium,
                        color         = c.textHint,         letterSpacing = 2.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Locked banner
            if (settingsLocked) {
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(c.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        "Settings are locked during an active session.",
                        style = TextStyle(fontSize = 13.sp, color = c.textHint)
                    )
                }
            }

            // ── PERMISSIONS ───────────────────────────────────────────────────
            Spacer(Modifier.height(36.dp))
            SHeader(c, "PERMISSIONS")
            Spacer(Modifier.height(12.dp))
            SCard(c, locked = false) {
                SRow(
                    c        = c,
                    title    = "Accessibility Service",
                    subtitle = if (accessibilityOn) "Active — app blocking enabled"
                               else "Required — tap to enable in system settings",
                    value    = if (accessibilityOn) "Active" else "Required",
                    valueColor = if (accessibilityOn) AccentStart else Danger,
                    chevron  = !accessibilityOn,
                    locked   = false,
                    onClick  = if (!accessibilityOn) {{
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }} else null
                )
                SDivider(c)
                SRow(
                    c        = c,
                    title    = "Device Admin",
                    subtitle = if (adminOn) "Active — Hard Mode protection enabled"
                               else "Inactive — required for Hard Commitment Mode",
                    value    = if (adminOn) "Active" else "Inactive",
                    valueColor = if (adminOn) AccentStart else c.textHint,
                    chevron  = !adminOn,
                    locked   = false,
                    onClick  = if (!adminOn) {{
                        context.startActivity(
                            FrictionDeviceAdminReceiver.buildActivationIntent(context)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }} else null
                )
            }

            // ── FOCUS MODE ────────────────────────────────────────────────────
            Spacer(Modifier.height(32.dp))
            SHeader(c, "FOCUS MODE")
            Spacer(Modifier.height(12.dp))
            SCard(c, locked = settingsLocked) {
                SRow(
                    c       = c,
                    title   = "Commitment Mode",
                    subtitle = if (hardMode) "Hard — cannot uninstall during session"
                               else "Normal — standard enforcement",
                    chevron = true,
                    locked  = settingsLocked,
                    onClick = { showHardModeSheet = true }
                )
            }

            // ── SESSION CONTROLS ──────────────────────────────────────────────
            Spacer(Modifier.height(32.dp))
            SHeader(c, "SESSION CONTROLS")
            Spacer(Modifier.height(12.dp))
            SCard(c, locked = settingsLocked) {

                // Tap difficulty
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .alpha(if (settingsLocked) 0.4f else 1f)
                ) {
                    Text("Tap Challenge Difficulty",
                        style = TextStyle(fontSize = 16.sp, color = c.text))
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TapDifficulty.entries.forEachIndexed { idx, diff ->
                            val sel = tapDifficulty == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (sel) AccentStart.copy(alpha = 0.15f) else c.surface2
                                    )
                                    .pointerInput(idx, settingsLocked) {
                                        if (!settingsLocked)
                                            detectTapGestures { vm.setTapDifficulty(idx) }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    diff.label,
                                    style = TextStyle(
                                        fontSize   = 13.sp,
                                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                                        color      = if (sel) AccentStart else c.textHint
                                    )
                                )
                            }
                        }
                    }
                }

                SDivider(c)

                // Grace period
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .alpha(if (settingsLocked) 0.4f else 1f)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Grace Period", style = TextStyle(fontSize = 16.sp, color = c.text))
                        Text(
                            if (gracePeriodSecs == 0) "Off" else "${gracePeriodSecs}s",
                            style = TextStyle(fontSize = 14.sp, color = AccentStart)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Cancel freely within this window after starting.",
                        style = TextStyle(fontSize = 12.sp, color = c.textHint)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0, 15, 30, 60, 120).forEach { secs ->
                            val sel = gracePeriodSecs == secs
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (sel) AccentStart.copy(alpha = 0.15f) else c.surface2
                                    )
                                    .pointerInput(secs, settingsLocked) {
                                        if (!settingsLocked)
                                            detectTapGestures { vm.setGracePeriod(secs) }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    if (secs == 0) "Off" else "${secs}s",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        color    = if (sel) AccentStart else c.textHint
                                    )
                                )
                            }
                        }
                    }
                }

                SDivider(c)
                SToggle(c, "Require Biometric to End Early",
                    "Face or fingerprint before tap challenge.",
                    biometricReq, settingsLocked, vm::setBiometricRequired)
                SDivider(c)
                SToggle(c, "Reflection on Early Exit",
                    "Show a reflection prompt after completing the tap challenge.",
                    reflectionOnExit, settingsLocked, vm::setReflectionOnExit)
            }

            // ── BLOCKING ──────────────────────────────────────────────────────
            Spacer(Modifier.height(32.dp))
            SHeader(c, "BLOCKING")
            Spacer(Modifier.height(12.dp))
            SCard(c, locked = false) {
                SRow(
                    c       = c,
                    title   = "Blocked Apps",
                    subtitle = when {
                        blockedCount.isEmpty() -> "No apps blocked"
                        blockedCount.size == 1 -> "1 app blocked"
                        else                   -> "${blockedCount.size} apps blocked"
                    },
                    chevron = true,
                    locked  = false,
                    onClick = { onBlockedApps() }
                )
                SDivider(c)
                SRow(
                    c       = c,
                    title   = "Emergency Allow List",
                    subtitle = when {
                        allowedCount.isEmpty() -> "No exceptions"
                        allowedCount.size == 1 -> "1 exception"
                        else                   -> "${allowedCount.size} exceptions"
                    },
                    chevron = true,
                    locked  = false,
                    onClick = { onBlockedApps() }
                )
            }

            // ── DISPLAY ───────────────────────────────────────────────────────
            Spacer(Modifier.height(32.dp))
            SHeader(c, "DISPLAY")
            Spacer(Modifier.height(12.dp))
            SCard(c, locked = false) {
                SToggle(c, "Standby Mode",
                    "Minimal black clock during a session.",
                    standbyMode, false, vm::setStandbyMode)
                SDivider(c)
                SToggle(c, "Ambient Mode",
                    "OLED-safe dim display after 90 s of inactivity.",
                    ambientMode, false, vm::setAmbientMode)
                SDivider(c)
                SToggle(c, "Haptic Feedback",
                    "Vibration on wheel scroll and tap challenge.",
                    hapticEnabled, false, vm::setHapticEnabled)
            }

            // ── SECURITY ──────────────────────────────────────────────────────
            Spacer(Modifier.height(32.dp))
            SHeader(c, "SECURITY")
            Spacer(Modifier.height(12.dp))
            SCard(c, locked = false) {
                SToggle(c, "Lock Settings During Session",
                    "Prevent changing settings while a session is active.",
                    lockSettings, false, vm::setLockSettingsDuring)
            }

            // ── ABOUT ─────────────────────────────────────────────────────────
            Spacer(Modifier.height(32.dp))
            SHeader(c, "ABOUT")
            Spacer(Modifier.height(12.dp))
            SCard(c, locked = false) {
                SRow(
                    c       = c,
                    title   = "About Friction",
                    subtitle = "Version, privacy policy, open source",
                    chevron = true,
                    locked  = false,
                    onClick = { onAbout() }
                )
                SDivider(c)
                SRow(
                    c          = c,
                    title      = "Reset All Data",
                    subtitle   = "Clears all settings, session data, and blocked apps.",
                    titleColor = if (settingsLocked) c.textHint else Danger,
                    locked     = settingsLocked,
                    onClick    = if (!settingsLocked) {{ showResetConfirm = true }} else null
                )
            }

            Spacer(Modifier.height(64.dp))
        }

        // Hard Mode sheet
        if (showHardModeSheet) {
            HardModeSheet(
                c            = c,
                isHard       = hardMode,
                locked       = settingsLocked,
                onActivate   = { vm.setHardMode(true);  showHardModeSheet = false },
                onDeactivate = { vm.setHardMode(false); showHardModeSheet = false },
                onDismiss    = { showHardModeSheet = false }
            )
        }

        // Reset confirmation sheet
        if (showResetConfirm) {
            ResetSheet(
                c         = c,
                onConfirm = { vm.resetAllData(); showResetConfirm = false },
                onDismiss = { showResetConfirm = false }
            )
        }
    }
}

// ── Bottom sheets ─────────────────────────────────────────────────────────────

@Composable
private fun HardModeSheet(
    c           : FrictionColors,
    isHard      : Boolean,
    locked      : Boolean,
    onActivate  : () -> Unit,
    onDeactivate: () -> Unit,
    onDismiss   : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (c.isDark) 0.92f else 0.55f))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(c.surface)
                .padding(horizontal = 24.dp, vertical = 36.dp)
                .pointerInput(Unit) { detectTapGestures { /* consume */ } },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (isHard) "Disable Hard Mode?" else "Hard Commitment Mode",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = c.text)
            )
            Spacer(Modifier.height(16.dp))

            if (!isHard) {
                listOf(
                    "Cannot uninstall Friction during a session",
                    "Settings locked for the full duration",
                    "Only the tap challenge can end it"
                ).forEach { line ->
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("·  ", style = TextStyle(fontSize = 14.sp, color = c.textHint))
                        Text(line, style = TextStyle(fontSize = 14.sp, color = c.textHint))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "This is a voluntary commitment. You chose it.",
                    style     = TextStyle(fontSize = 13.sp, color = c.textHint.copy(alpha = 0.6f)),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    "Switching to Normal Mode removes uninstall protection immediately.",
                    style     = TextStyle(fontSize = 14.sp, color = c.textSub),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))

            if (locked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(54.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(c.surface2),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Cannot change mode during an active session",
                        style     = TextStyle(fontSize = 14.sp, color = c.textHint),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val actionBg = if (!isHard) accentGradient
                               else androidx.compose.ui.graphics.Brush.horizontalGradient(
                                   listOf(c.surface2, c.surface2)
                               )
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(54.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(actionBg)
                        .pointerInput(Unit) {
                            detectTapGestures { if (isHard) onDeactivate() else onActivate() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isHard) "Switch to Normal" else "I Understand — Activate",
                        style = TextStyle(
                            fontSize   = 16.sp, fontWeight = FontWeight.SemiBold,
                            color      = if (!isHard) c.btnText else c.textSub
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(c.surface2)
                    .pointerInput(Unit) { detectTapGestures { onDismiss() } },
                contentAlignment = Alignment.Center
            ) {
                Text("Cancel", style = TextStyle(fontSize = 16.sp, color = c.textSub))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ResetSheet(
    c        : FrictionColors,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (c.isDark) 0.92f else 0.55f))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(c.surface)
                .padding(horizontal = 24.dp, vertical = 36.dp)
                .pointerInput(Unit) { detectTapGestures { /* consume */ } },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Reset All Data?",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = c.text)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Clears all settings, blocked apps, and session history. Cannot be undone.",
                style     = TextStyle(fontSize = 14.sp, color = c.textSub),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth().height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Danger.copy(alpha = 0.15f))
                    .pointerInput(Unit) { detectTapGestures { onConfirm() } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Reset Everything",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Danger)
                )
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(c.surface2)
                    .pointerInput(Unit) { detectTapGestures { onDismiss() } },
                contentAlignment = Alignment.Center
            ) {
                Text("Cancel", style = TextStyle(fontSize = 16.sp, color = c.textSub))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Shared primitives ─────────────────────────────────────────────────────────

@Composable
private fun SHeader(c: FrictionColors, text: String) {
    Text(
        text,
        style    = TextStyle(
            fontSize      = 11.sp, fontWeight   = FontWeight.Medium,
            color         = c.textHint,         letterSpacing = 1.5.sp
        ),
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SCard(
    c      : FrictionColors,
    locked : Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface)
            .alpha(if (locked) 0.55f else 1f),
        content  = content
    )
}

@Composable
private fun SDivider(c: FrictionColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(0.5.dp)
            .background(c.divider)
    )
}

@Composable
private fun SRow(
    c          : FrictionColors,
    title      : String,
    subtitle   : String?   = null,
    titleColor : Color     = c.text,
    value      : String?   = null,
    valueColor : Color     = c.textHint,
    chevron    : Boolean   = false,
    locked     : Boolean   = false,
    onClick    : (() -> Unit)? = null
) {
    val mod = if (onClick != null && !locked)
        Modifier.pointerInput(Unit) { detectTapGestures { onClick() } }
    else Modifier

    Row(
        modifier              = mod.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontSize = 16.sp, color = titleColor))
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = TextStyle(fontSize = 13.sp, color = c.textHint))
            }
        }
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (value != null)
                Text(value, style = TextStyle(fontSize = 14.sp, color = valueColor))
            if (chevron)
                Text("›", style = TextStyle(fontSize = 20.sp, color = c.textHint))
        }
    }
}

@Composable
private fun SToggle(
    c       : FrictionColors,
    title   : String,
    subtitle: String? = null,
    checked : Boolean,
    locked  : Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = TextStyle(fontSize = 16.sp, color = c.text))
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = TextStyle(fontSize = 12.sp, color = c.textHint))
            }
        }
        Switch(
            checked         = checked,
            onCheckedChange = if (!locked) onToggle else null,
            enabled         = !locked,
            colors          = SwitchDefaults.colors(
                checkedThumbColor           = c.btnText,
                checkedTrackColor           = AccentStart,
                uncheckedThumbColor         = c.textSub,
                uncheckedTrackColor         = c.surface2,
                uncheckedBorderColor        = Color.Transparent,
                checkedBorderColor          = Color.Transparent,
                disabledCheckedThumbColor   = c.btnText.copy(alpha = 0.4f),
                disabledCheckedTrackColor   = AccentStart.copy(alpha = 0.3f),
                disabledUncheckedThumbColor = c.textSub.copy(alpha = 0.3f),
                disabledUncheckedTrackColor = c.surface2.copy(alpha = 0.3f)
            )
        )
    }
}
