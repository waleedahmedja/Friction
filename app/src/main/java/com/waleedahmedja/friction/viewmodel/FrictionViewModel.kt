package com.waleedahmedja.friction.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.waleedahmedja.friction.admin.FrictionDeviceAdminReceiver
import com.waleedahmedja.friction.data.BlockedAppsRepository
import com.waleedahmedja.friction.data.DataStoreManager
import com.waleedahmedja.friction.messages.CompletionMessages
import com.waleedahmedja.friction.messages.ReflectiveMessages
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
// State models
// ─────────────────────────────────────────────────────────────────────────────

data class LockState(
    val isActive        : Boolean = false,
    val unlockAt        : Long    = 0L,
    val totalDurationMs : Long    = 0L,
    val remainingMs     : Long    = 0L
) {
    val progressFraction: Float
        get() = if (totalDurationMs <= 0L) 0f
        else (1f - remainingMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)

    val durationMs: Long get() = totalDurationMs
    val endsAtMs  : Long get() = unlockAt

    val sessionEndTime: String
        get() = if (unlockAt == 0L) ""
        else SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(unlockAt))
}

data class TapState(
    val required : Int     = 75,
    val current  : Int     = 0,
    val done     : Boolean = false
)

data class CompletionState(
    val visible       : Boolean = false,
    val message       : String  = "",
    val durationLabel : String  = ""
)

data class AppInfo(
    val packageName : String,
    val label       : String
)

enum class PostTapDestination { NONE, REFLECTION, HOME }

enum class TapDifficulty(val label: String) {
    EASY("Easy"), MEDIUM("Medium"), HARD("Hard")
}

enum class BiometricResult { SUCCESS, FAILED, ERROR, NOT_ENROLLED }

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class FrictionViewModel(app: Application) : AndroidViewModel(app) {

    val ds          = DataStoreManager(app.applicationContext)
    val blockedRepo = BlockedAppsRepository(app.applicationContext)

    // ── Wheel picker ──────────────────────────────────────────────────────────

    private val _selectedHours   = MutableStateFlow(0)
    private val _selectedMinutes = MutableStateFlow(25)

    val selectedHours  : StateFlow<Int> = _selectedHours  .asStateFlow()
    val selectedMinutes: StateFlow<Int> = _selectedMinutes.asStateFlow()
    val hours          : StateFlow<Int> = _selectedHours  .asStateFlow()
    val minutes        : StateFlow<Int> = _selectedMinutes.asStateFlow()

    val totalMinutes: StateFlow<Int> = combine(_selectedHours, _selectedMinutes) { h, m ->
        h * 60 + m
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 25)

    // ── Lock state ────────────────────────────────────────────────────────────

    private val _lock = MutableStateFlow(LockState())
    val lock: StateFlow<LockState> = _lock.asStateFlow()

    private var sessionDurationMinutes = 0
    private var timerJob: Job? = null

    // ── Sub-states ────────────────────────────────────────────────────────────

    private val _isStandby   = MutableStateFlow(false)
    val isStandby: StateFlow<Boolean> = _isStandby.asStateFlow()

    private val _graceActive = MutableStateFlow(false)
    val graceActive: StateFlow<Boolean> = _graceActive.asStateFlow()

    // ── Tap challenge ─────────────────────────────────────────────────────────

    private val _tap = MutableStateFlow(TapState())
    val tap: StateFlow<TapState> = _tap.asStateFlow()

    private val _postTapDestination = MutableStateFlow(PostTapDestination.NONE)
    val postTapDestination: StateFlow<PostTapDestination> = _postTapDestination.asStateFlow()

    // ── Reflection ────────────────────────────────────────────────────────────

    private val _reflectionMessage      = MutableStateFlow("")
    val reflectionMessage: StateFlow<String> = _reflectionMessage.asStateFlow()

    private val _showReflectionContinue = MutableStateFlow(false)
    val showReflectionContinue: StateFlow<Boolean> = _showReflectionContinue.asStateFlow()

    // ── Completion ────────────────────────────────────────────────────────────

    private val _completion = MutableStateFlow(CompletionState())
    val completion: StateFlow<CompletionState> = _completion.asStateFlow()

    // ── Biometric ────────────────────────────────────────────────────────────

    private val _biometricResult = MutableStateFlow<BiometricResult?>(null)
    val biometricResult: StateFlow<BiometricResult?> = _biometricResult.asStateFlow()

    fun clearBiometricResult() { _biometricResult.value = null }

    // ── Settings ──────────────────────────────────────────────────────────────

    val hardMode           = ds.hardMode          .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val tapDifficulty      = ds.tapDifficulty     .stateIn(viewModelScope, SharingStarted.Eagerly, 1)
    val gracePeriodSecs    = ds.gracePeriodSecs   .stateIn(viewModelScope, SharingStarted.Eagerly, 30)
    val reflectionOnExit   = ds.reflectionOnExit  .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val standbyModePref    = ds.standbyMode       .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val ambientModePref    = ds.ambientMode       .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val hapticEnabled      = ds.hapticEnabled     .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val biometricRequired  = ds.biometricRequired .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val lockSettingsDuring = ds.lockSettingsDuring.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // ── Blocked / allowed apps ────────────────────────────────────────────────

    val blockedPackages: StateFlow<Set<String>> =
        blockedRepo.blockedPackages.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val allowedPackages: StateFlow<Set<String>> =
        blockedRepo.allowedPackages.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    // ── Init — restore session after process death or reboot ──────────────────

    init {
        viewModelScope.launch {
            val wasActive  = ds.lockActive   .first()
            val unlockAt   = ds.unlockTime   .first()
            val totalDurMs = ds.totalDuration.first()

            if (wasActive && unlockAt > 0L) {
                val remaining = unlockAt - System.currentTimeMillis()
                if (remaining > 0L) {
                    sessionDurationMinutes = (totalDurMs / 60_000L).toInt()
                    _lock.value = LockState(
                        isActive        = true,
                        unlockAt        = unlockAt,
                        totalDurationMs = totalDurMs,
                        remainingMs     = remaining
                    )
                    runTimer(unlockAt, totalDurMs)
                } else {
                    ds.clearLock()
                }
            }
        }
    }

    // ── Biometric prompt ──────────────────────────────────────────────────────

    fun launchBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit) {
        val bm      = BiometricManager.from(activity)
        val canAuth = bm.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            _biometricResult.value = BiometricResult.NOT_ENROLLED
            onSuccess()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                _biometricResult.value = BiometricResult.SUCCESS
                onSuccess()
            }
            override fun onAuthenticationFailed() {
                _biometricResult.value = BiometricResult.FAILED
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                _biometricResult.value = BiometricResult.ERROR
            }
        }

        BiometricPrompt(activity, executor, callback).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirm Exit")
                .setSubtitle("Verify to end this focus session")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build()
        )
    }

    // ── Permission helpers ────────────────────────────────────────────────────

    fun isAdminActive(): Boolean =
        FrictionDeviceAdminReceiver.isAdminActive(getApplication())

    fun isAccessibilityEnabled(): Boolean {
        val ctx     = getApplication<Application>()
        val enabled = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains(ctx.packageName, ignoreCase = true)
    }

    // ── Installed apps ────────────────────────────────────────────────────────

    /**
     * Loads ALL apps that have a visible launcher icon — both user-installed
     * and pre-installed (WhatsApp, YouTube, Chrome, system camera, gallery, etc).
     *
     * Strategy: use queryIntentActivities() for the MAIN/LAUNCHER intent.
     * This returns exactly the apps that appear on the device's home screen —
     * no more, no less. It correctly includes:
     *   - Apps installed by the user (WhatsApp, Instagram, etc.)
     *   - Apps pre-installed by the manufacturer (Camera, Gallery, Clock, etc.)
     *   - Apps updated by Play Store that started as system apps (YouTube, Chrome)
     *
     * Friction itself is excluded so it can never accidentally block itself.
     *
     * Sorted alphabetically by display name, case-insensitive.
     */
    fun loadInstalledApps() {
        viewModelScope.launch {
            val pm         = getApplication<Application>().packageManager
            val ownPackage = getApplication<Application>().packageName

            // Query every app that can handle a home-screen launch
            val launchIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            }

            val apps = pm.queryIntentActivities(launchIntent, 0)
                .map { resolveInfo ->
                    AppInfo(
                        packageName = resolveInfo.activityInfo.packageName,
                        label       = resolveInfo.loadLabel(pm).toString()
                    )
                }
                .filter  { it.packageName != ownPackage }   // exclude Friction itself
                .distinctBy { it.packageName }              // deduplicate (some apps register multiple activities)
                .sortedBy { it.label.lowercase() }

            _installedApps.value = apps
        }
    }

    fun toggleBlocked(pkg: String) {
        viewModelScope.launch {
            if (pkg in blockedRepo.getBlockedPackages()) blockedRepo.removeBlocked(pkg)
            else                                          blockedRepo.addBlocked(pkg)
        }
    }

    fun toggleAllowed(pkg: String) {
        viewModelScope.launch {
            if (pkg in blockedRepo.getAllowedPackages()) blockedRepo.removeAllowed(pkg)
            else                                         blockedRepo.addAllowed(pkg)
        }
    }

    // ── Wheel setters ─────────────────────────────────────────────────────────

    fun setHours(h: Int)   { _selectedHours.value   = h.coerceIn(0, 23) }
    fun setMinutes(m: Int) { _selectedMinutes.value = m.coerceIn(0, 59) }

    // ── Start session ─────────────────────────────────────────────────────────

    fun startLock() {
        val durationMs = (_selectedHours.value * 3_600L + _selectedMinutes.value * 60L) * 1_000L
        if (durationMs <= 0L) return

        sessionDurationMinutes = _selectedHours.value * 60 + _selectedMinutes.value
        val unlockAt = System.currentTimeMillis() + durationMs

        viewModelScope.launch {
            ds.startLock(unlockAt, durationMs)
            _lock.value        = LockState(true, unlockAt, durationMs, durationMs)
            _graceActive.value = true

            if (hardMode.value) {
                FrictionDeviceAdminReceiver.markSessionActive(getApplication(), true)
            }

            runTimer(unlockAt, durationMs)

            val graceMs = gracePeriodSecs.value * 1_000L
            if (graceMs > 0L) delay(graceMs)
            _graceActive.value = false
        }
    }

    fun startSession() = startLock()

    // ── Timer ─────────────────────────────────────────────────────────────────

    fun runTimer(unlockAt: Long, totalDurMs: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val remaining = unlockAt - System.currentTimeMillis()
                if (remaining <= 0L) {
                    _lock.value = _lock.value.copy(remainingMs = 0L, isActive = false)
                    ds.clearLock()
                    FrictionDeviceAdminReceiver.markSessionActive(getApplication(), false)
                    triggerCompletion()
                    break
                }
                _lock.value = _lock.value.copy(remainingMs = remaining)
                delay(1_000L)
            }
        }
    }

    fun pauseTimer() { timerJob?.cancel(); timerJob = null }

    fun resumeTimer() {
        val s = _lock.value
        if (s.isActive && s.unlockAt > 0L) runTimer(s.unlockAt, s.totalDurationMs)
    }

    // ── Standby ───────────────────────────────────────────────────────────────

    fun enterStandby() { _isStandby.value = true  }
    fun exitStandby()  { _isStandby.value = false }

    // ── End session ───────────────────────────────────────────────────────────

    fun requestEndSession() {
        if (_graceActive.value) recordEarlyExit()
    }

    // ── Tap challenge ─────────────────────────────────────────────────────────

    fun initTap() {
        val mins     = sessionDurationMinutes.takeIf { it > 0 } ?: totalMinutes.value
        val required = when (tapDifficulty.value) {
            0    -> (mins * 5).coerceIn(50, 150)       // Easy
            2    -> 300                                  // Hard
            else -> (mins * 12).coerceIn(75, 300)      // Medium (default)
        }
        _tap.value = TapState(required = required, current = 0, done = false)
    }

    fun registerTap() {
        val s    = _tap.value
        if (s.done) return
        val next = (s.current + 1).coerceAtMost(s.required)
        val done = next >= s.required
        _tap.value = s.copy(current = next, done = done)
        if (done) routeAfterTapChallenge()
    }

    private fun routeAfterTapChallenge() {
        viewModelScope.launch {
            pauseTimer()
            ds.clearLock()
            FrictionDeviceAdminReceiver.markSessionActive(getApplication(), false)
            _lock.value        = LockState()
            _isStandby.value   = false
            _graceActive.value = false
            _postTapDestination.value =
                if (reflectionOnExit.value) PostTapDestination.REFLECTION
                else                        PostTapDestination.HOME
        }
    }

    fun clearPostTapDestination() { _postTapDestination.value = PostTapDestination.NONE }

    // ── Grace cancel ──────────────────────────────────────────────────────────

    fun recordEarlyExit() {
        viewModelScope.launch {
            pauseTimer()
            ds.clearLock()
            FrictionDeviceAdminReceiver.markSessionActive(getApplication(), false)
            _lock.value        = LockState()
            _isStandby.value   = false
            _graceActive.value = false
        }
    }

    // ── Reflection ────────────────────────────────────────────────────────────

    fun loadReflectionMessage() {
        viewModelScope.launch {
            val idx = ds.msgIndex.first()
            _reflectionMessage.value      = ReflectiveMessages.getForIndex(idx)
            _showReflectionContinue.value = false
            delay(1_500L)
            _showReflectionContinue.value = true
            ds.saveMsgIndex(ReflectiveMessages.nextIndex(idx))
        }
    }

    fun clearReflectionContinue() { _showReflectionContinue.value = false }

    // ── Completion ────────────────────────────────────────────────────────────

    private fun triggerCompletion() {
        viewModelScope.launch {
            val label = CompletionMessages.durationLabel(sessionDurationMinutes)
            val idx   = ds.msgIndex.first()
            val msg   = CompletionMessages.getForIndex(idx, label)
            ds.saveMsgIndex(CompletionMessages.nextIndex(idx))
            _completion.value = CompletionState(visible = true, message = msg, durationLabel = label)
        }
    }

    fun showCompletion()  = triggerCompletion()
    fun clearCompletion() { _completion.value = CompletionState() }

    // ── Settings writes ───────────────────────────────────────────────────────

    fun setHardMode(v: Boolean)           { viewModelScope.launch { ds.setHardMode(v)           } }
    fun setTapDifficulty(v: Int)          { viewModelScope.launch { ds.setTapDifficulty(v)      } }
    fun setGracePeriod(v: Int)            { viewModelScope.launch { ds.setGracePeriod(v)        } }
    fun setReflectionOnExit(v: Boolean)   { viewModelScope.launch { ds.setReflectionOnExit(v)   } }
    fun setStandbyMode(v: Boolean)        { viewModelScope.launch { ds.setStandbyMode(v)        } }
    fun setAmbientMode(v: Boolean)        { viewModelScope.launch { ds.setAmbientMode(v)        } }
    fun setHapticEnabled(v: Boolean)      { viewModelScope.launch { ds.setHapticEnabled(v)      } }
    fun setBiometricRequired(v: Boolean)  { viewModelScope.launch { ds.setBiometricRequired(v)  } }
    fun setLockSettingsDuring(v: Boolean) { viewModelScope.launch { ds.setLockSettingsDuring(v) } }

    fun resetAllData() {
        viewModelScope.launch {
            ds.resetAll()
            blockedRepo.clearAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}