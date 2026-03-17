package com.waleedahmedja.friction.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waleedahmedja.friction.BuildConfig
import com.waleedahmedja.friction.admin.FrictionDeviceAdminReceiver
import com.waleedahmedja.friction.ui.theme.AccentStart
import com.waleedahmedja.friction.ui.theme.Danger
import com.waleedahmedja.friction.ui.theme.FrictionColors
import com.waleedahmedja.friction.ui.theme.FrictionTheme
import com.waleedahmedja.friction.viewmodel.FrictionViewModel

@Composable
fun AboutScreen(
    vm       : FrictionViewModel,
    onBack   : () -> Unit,
    onPrivacy: () -> Unit
) {
    val c       = FrictionTheme.c
    val context = LocalContext.current
    val lock    by vm.lock.collectAsStateWithLifecycle()

    val accessibilityOn = remember { vm.isAccessibilityEnabled() }
    val adminOn         = remember { FrictionDeviceAdminReceiver.isAdminActive(context) }

    // Version easter egg — 7 taps reveals debug panel
    var versionTaps  by remember { mutableIntStateOf(0) }
    var showDebug    by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
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
                    "ABOUT",
                    style = TextStyle(
                        fontSize = 11.sp, fontWeight = FontWeight.Medium,
                        color = c.textHint, letterSpacing = 2.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(48.dp))

            // ── IDENTITY ──────────────────────────────────────────────────────
            AHeader(c, "IDENTITY")
            Spacer(Modifier.height(12.dp))
            ACard(c) {
                ARow(c, "App", "Friction")
                ADivider(c)
                // Version row — 7 taps reveals debug info
                ARow(
                    c     = c,
                    label = "Version",
                    value = try { BuildConfig.VERSION_NAME } catch (e: Exception) { "1.0.0" },
                    onClick = {
                        versionTaps++
                        if (versionTaps >= 7) { showDebug = true; versionTaps = 0 }
                    }
                )
                ADivider(c)
                ARow(c, "Build", try { BuildConfig.VERSION_CODE.toString() } catch (e: Exception) { "1" })
            }

            // Debug panel (easter egg)
            if (showDebug) {
                Spacer(Modifier.height(12.dp))
                ACard(c) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Text("DEBUG", style = TextStyle(fontSize = 10.sp, color = AccentStart, letterSpacing = 2.sp))
                        Spacer(Modifier.height(10.dp))
                        listOf(
                            "Accessibility Service" to if (accessibilityOn) "active" else "not enabled",
                            "Device Admin"          to if (adminOn) "active" else "inactive",
                            "Session Active"        to if (lock.isActive) "yes" else "no",
                            "Remaining"             to if (lock.isActive)
                                "${lock.remainingMs / 1000}s" else "–"
                        ).forEach { (k, v) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(k, style = TextStyle(fontSize = 13.sp, color = c.textHint))
                                Text(v, style = TextStyle(fontSize = 13.sp, color = c.textSub))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .pointerInput(Unit) { detectTapGestures { showDebug = false } }
                                .padding(top = 4.dp)
                        ) {
                            Text("Dismiss", style = TextStyle(fontSize = 13.sp, color = c.textHint))
                        }
                    }
                }
            }

            // ── TRANSPARENCY ──────────────────────────────────────────────────
            Spacer(Modifier.height(32.dp))
            AHeader(c, "TRANSPARENCY")
            Spacer(Modifier.height(12.dp))
            ACard(c) {
                ARow(
                    c       = c,
                    label   = "Accessibility Service",
                    value   = if (accessibilityOn) "Active" else "Not enabled",
                    valueColor = if (accessibilityOn) AccentStart else Danger,
                    chevron = !accessibilityOn,
                    onClick = if (!accessibilityOn) {{
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }} else null
                )
                ADivider(c)
                ARow(
                    c       = c,
                    label   = "Device Admin",
                    value   = if (adminOn) "Active" else "Inactive",
                    valueColor = if (adminOn) AccentStart else c.textHint
                )
                ADivider(c)
                ARow(c, "Privacy Policy", chevron = true, onClick = { onPrivacy() })
            }

            // ── LINKS ─────────────────────────────────────────────────────────
            Spacer(Modifier.height(32.dp))
            AHeader(c, "LINKS")
            Spacer(Modifier.height(12.dp))
            ACard(c) {
                ARow(
                    c       = c,
                    label   = "GitHub",
                    value   = "Source code",
                    chevron = true,
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/waleedahmedja/Friction"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                )
                ADivider(c)
                ARow(
                    c       = c,
                    label   = "Contact",
                    value   = "waleedahmedja@gmail.com",
                    chevron = true,
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO,
                                Uri.parse("mailto:waleedahmedja@gmail.com"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                )
            }

            Spacer(Modifier.height(64.dp))

            Text(
                "Your data stays on your device.",
                style = TextStyle(fontSize = 12.sp, color = c.textHint),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}

// ── Shared primitives ─────────────────────────────────────────────────────────

@Composable
private fun AHeader(c: FrictionColors, text: String) {
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
private fun ACard(c: FrictionColors, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface),
        content  = content
    )
}

@Composable
private fun ADivider(c: FrictionColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(0.5.dp)
            .background(c.divider)
    )
}

@Composable
private fun ARow(
    c          : FrictionColors,
    label      : String,
    value      : String?   = null,
    valueColor : androidx.compose.ui.graphics.Color = c.textHint,
    chevron    : Boolean   = false,
    onClick    : (() -> Unit)? = null
) {
    val mod = if (onClick != null)
        Modifier.pointerInput(Unit) { detectTapGestures { onClick() } }
    else Modifier

    Row(
        modifier              = mod.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = TextStyle(fontSize = 16.sp, color = c.text))
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
