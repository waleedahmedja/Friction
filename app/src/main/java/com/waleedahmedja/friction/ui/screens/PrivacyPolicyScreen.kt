package com.waleedahmedja.friction.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waleedahmedja.friction.ui.theme.FrictionColors
import com.waleedahmedja.friction.ui.theme.FrictionTheme

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val c = FrictionTheme.c

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
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
                "PRIVACY POLICY",
                style = TextStyle(
                    fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    color = c.textHint, letterSpacing = 2.sp
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(40.dp))

        Text("Effective January 2026",
            style = TextStyle(fontSize = 12.sp, color = c.textHint))

        Spacer(Modifier.height(32.dp))

        PolicySection(c, "1. Data Collection",
            "Friction collects no personal data. The app operates entirely on your device. " +
            "No usage analytics, crash reports, or behavioural data are collected or transmitted."
        )
        PolicySection(c, "2. Accessibility Service",
            "The Accessibility Service monitors which app is in the foreground. " +
            "This information is used solely to enforce your focus session by triggering the tap challenge " +
            "when a blocked app is opened. No screen content is read. No data leaves your device."
        )
        PolicySection(c, "3. Device Administrator",
            "When Hard Commitment Mode is enabled, Friction registers as a Device Administrator. " +
            "This prevents the app from being uninstalled during an active session. " +
            "Administrator rights are removed automatically when the session ends, " +
            "or can be revoked manually at any time via Android Settings."
        )
        PolicySection(c, "4. Biometric Authentication",
            "If biometric verification is enabled, authentication is handled entirely by Android's " +
            "BiometricPrompt API. Friction never accesses, stores, or transmits biometric data."
        )
        PolicySection(c, "5. Local Storage",
            "Session settings, duration preferences, and blocked app lists are stored locally " +
            "using Android DataStore. This data never leaves your device and is deleted when you " +
            "reset the app or uninstall it."
        )
        PolicySection(c, "6. Third-Party Services",
            "Friction uses no third-party SDKs, analytics services, advertising networks, " +
            "or cloud services of any kind."
        )
        PolicySection(c, "7. Ethical Use",
            "Friction is a personal focus tool. It is not intended for use on devices you do not own, " +
            "or to enforce restrictions on other individuals without their informed consent."
        )
        PolicySection(c, "8. Children's Privacy",
            "Friction is not directed at children under 13 and does not knowingly collect " +
            "data from minors."
        )
        PolicySection(c, "9. Changes to This Policy",
            "If this policy changes, the effective date above will be updated. " +
            "Continued use of the app after changes constitutes acceptance."
        )
        PolicySection(c, "10. Contact",
            "Questions about this policy: waleedahmedja@gmail.com"
        )

        Spacer(Modifier.height(48.dp))

        Text(
            "Your data remains yours.",
            style    = TextStyle(fontSize = 13.sp, color = c.textHint, fontWeight = FontWeight.Light),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(64.dp))
    }
}

@Composable
private fun PolicySection(c: FrictionColors, heading: String, body: String) {
    Text(heading, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text))
    Spacer(Modifier.height(8.dp))
    Text(body, style = TextStyle(fontSize = 14.sp, color = c.textSub, lineHeight = 22.sp))
    Spacer(Modifier.height(28.dp))
}
