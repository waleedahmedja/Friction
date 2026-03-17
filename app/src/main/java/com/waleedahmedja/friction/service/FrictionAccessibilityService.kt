package com.waleedahmedja.friction.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.waleedahmedja.friction.data.BlockedAppsRepository
import com.waleedahmedja.friction.data.DataStoreManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * Monitors foreground app changes during active focus sessions.
 * When a blocked app is detected, launches TapChallengeOverlayActivity on top of it.
 *
 * Privacy: canRetrieveWindowContent is false in accessibility_service_config.xml.
 * Only package names are read — no screen content, no keystrokes, nothing else.
 */
class FrictionAccessibilityService : AccessibilityService() {

    // Dedicated IO scope — cancelled in onDestroy to prevent leaks
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var ds          : DataStoreManager
    private lateinit var blockedRepo : BlockedAppsRepository

    // Debounce — prevents hammering TapChallengeOverlayActivity when the OS
    // fires multiple TYPE_WINDOW_STATE_CHANGED events for a single app transition
    private var lastBlockedPackage = ""
    private var lastBlockedTimeMs  = 0L
    private val debounceWindowMs   = 2_000L

    // Package prefixes that should never be blocked — launchers, system UI,
    // settings, and package installer. Blocking these would trap the user.
    private val neverBlockPrefixes = listOf(
        "com.android.launcher",
        "com.google.android.apps.nexuslauncher",
        "com.transsion.launcher",
        "com.android.systemui",
        "com.android.settings",
        "com.google.android.packageinstaller",
        "com.android.packageinstaller"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()

        ds          = DataStoreManager(applicationContext)
        blockedRepo = BlockedAppsRepository(applicationContext)

        // Configure programmatically as a fallback — primary config is in
        // accessibility_service_config.xml. The ?: guard handles the rare case
        // where serviceInfo is null before the XML config has been applied.
        serviceInfo = (serviceInfo ?: AccessibilityServiceInfo()).apply {
            eventTypes          = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType        = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags               = AccessibilityServiceInfo.DEFAULT
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Never interfere with our own app or system packages
        if (packageName == this.packageName) return
        if (neverBlockPrefixes.any { packageName.startsWith(it) }) return

        serviceScope.launch {
            // Only enforce during an active session
            if (!ds.lockActive.first()) return@launch

            val blocked = blockedRepo.getBlockedPackages()
            if (packageName !in blocked) return@launch

            // Emergency allow-list overrides the block list
            val allowed = blockedRepo.getAllowedPackages()
            if (packageName in allowed) return@launch

            // Debounce — skip if same package was just handled
            val now = System.currentTimeMillis()
            if (packageName == lastBlockedPackage && now - lastBlockedTimeMs < debounceWindowMs) {
                return@launch
            }
            lastBlockedPackage = packageName
            lastBlockedTimeMs  = now

            // Activity starts must happen on the Main thread — switching here
            // avoids the "startActivity from background thread" restriction
            withContext(Dispatchers.Main) {
                applicationContext.startActivity(
                    Intent(applicationContext, TapChallengeOverlayActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra(TapChallengeOverlayActivity.EXTRA_BLOCKED_PACKAGE, packageName)
                    }
                )
            }
        }
    }

    override fun onInterrupt() {
        // Required override — nothing to do
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}