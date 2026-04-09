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

        // Header
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.CenterStart)
                    .pointerInput(Unit) { detectTapGestures { onBack() } },
                contentAlignment = Alignment.Center
            ) {
                Text("←", style = TextStyle(fontSize = 22.sp, color = c.textHint))
            }
            Text(
                "PRIVACY POLICY",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textHint,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(40.dp))
        Text("Last updated: April 2026", style = TextStyle(fontSize = 12.sp, color = c.textHint))
        Spacer(Modifier.height(24.dp))

        // Introduction / Short Version
        Text(
            "Friction is a commitment enforcement system designed for voluntary personal use. Everything stays on your device.",
            style = TextStyle(fontSize = 14.sp, color = c.text, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
        )
        Spacer(Modifier.height(32.dp))

        // Policy Sections
        PS(c, "1. Data Collection", "Friction does not collect, store, transmit, or sell any personal data. All focus session data (duration, blocked apps, settings) is stored locally on your device using Android DataStore. Nothing leaves your device.")

        PS(c, "2. Accessibility Service", "Used solely to detect foreground applications to enforce blocks. Friction does not read screen content, record keystrokes, or track browsing behavior. 'canRetrieveWindowContent' is explicitly set to false.")

        PS(c, "3. Device Administrator", "When Hard Commitment Mode is enabled, Friction uses Device Admin permissions strictly to prevent uninstallation mid-session. Rights are released automatically when the session ends.")

        PS(c, "4. Biometric Authentication", "Verification is handled entirely by Android's system-level secure APIs (BiometricPrompt). Friction does not access, store, or transmit any biometric information.")

        PS(c, "5. Local Storage", "All app data is stored locally via Android DataStore. Uninstalling the app removes all of this data permanently.")

        PS(c, "6. Third-Party Services", "Friction uses none. No analytics, advertising SDKs, or tracking frameworks. The app operates entirely offline.")

        PS(c, "7. Children's Privacy", "Friction does not knowingly collect data from anyone. Since no personal data is collected from any user, no special processing applies.")

        PS(c, "8. Ethical Use", "Friction is for voluntary self-discipline. It must not be used to monitor or control another person's device without their informed consent.")

        PS(c, "9. Changes", "This policy may be updated to reflect new features. Significant changes will be noted in the release notes.")

        PS(c, "10. Contact", "If you have questions, open a GitHub Issue at: github.com/waleedahmedja/Friction")

        Spacer(Modifier.height(48.dp))
        Text(
            "Friction is built on transparency and discipline. Your data is yours.",
            style = TextStyle(fontSize = 13.sp, color = c.textHint, fontWeight = FontWeight.Light),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(64.dp))
    }
}

@Composable
private fun PS(c: FrictionColors, heading: String, body: String) {
    Text(heading, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text))
    Spacer(Modifier.height(8.dp))
    Text(body, style = TextStyle(fontSize = 14.sp, color = c.textSub, lineHeight = 22.sp))
    Spacer(Modifier.height(28.dp))
}
