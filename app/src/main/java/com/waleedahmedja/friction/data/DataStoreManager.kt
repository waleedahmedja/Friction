package com.waleedahmedja.friction.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "friction_prefs")

class DataStoreManager(private val ctx: Context) {

    companion object {
        val KEY_UNLOCK_TIME          = longPreferencesKey("unlock_time")
        val KEY_TOTAL_DURATION       = longPreferencesKey("total_duration_ms")
        val KEY_LOCK_ACTIVE          = booleanPreferencesKey("lock_active")
        val KEY_MSG_INDEX            = intPreferencesKey("msg_index")
        val KEY_HARD_MODE            = booleanPreferencesKey("hard_mode")
        val KEY_TAP_DIFFICULTY       = intPreferencesKey("tap_difficulty")
        val KEY_GRACE_PERIOD_SECS    = intPreferencesKey("grace_period_secs")
        val KEY_REFLECTION_ON_EXIT   = booleanPreferencesKey("reflection_on_exit")
        val KEY_STANDBY_MODE         = booleanPreferencesKey("standby_mode")
        val KEY_AMBIENT_MODE         = booleanPreferencesKey("ambient_mode")
        val KEY_HAPTIC_ENABLED       = booleanPreferencesKey("haptic_enabled")
        val KEY_BIOMETRIC_REQUIRED   = booleanPreferencesKey("biometric_required")
        val KEY_LOCK_SETTINGS_DURING = booleanPreferencesKey("lock_settings_during")
    }

    // ── Session ───────────────────────────────────────────────────────────────
    val unlockTime   : Flow<Long>    = ctx.dataStore.data.map { it[KEY_UNLOCK_TIME]    ?: 0L    }
    val totalDuration: Flow<Long>    = ctx.dataStore.data.map { it[KEY_TOTAL_DURATION] ?: 0L    }
    val lockActive   : Flow<Boolean> = ctx.dataStore.data.map { it[KEY_LOCK_ACTIVE]    ?: false }
    val msgIndex     : Flow<Int>     = ctx.dataStore.data.map { it[KEY_MSG_INDEX]      ?: 0     }

    suspend fun startLock(unlockAt: Long, durationMs: Long) {
        ctx.dataStore.edit {
            it[KEY_UNLOCK_TIME]    = unlockAt
            it[KEY_TOTAL_DURATION] = durationMs
            it[KEY_LOCK_ACTIVE]    = true
        }
    }

    suspend fun clearLock() {
        ctx.dataStore.edit {
            it[KEY_LOCK_ACTIVE]    = false
            it[KEY_UNLOCK_TIME]    = 0L
            it[KEY_TOTAL_DURATION] = 0L
        }
    }

    suspend fun saveMsgIndex(idx: Int) {
        ctx.dataStore.edit { it[KEY_MSG_INDEX] = idx }
    }

    // ── Settings ──────────────────────────────────────────────────────────────
    val hardMode          : Flow<Boolean> = ctx.dataStore.data.map { it[KEY_HARD_MODE]           ?: false }
    val tapDifficulty     : Flow<Int>     = ctx.dataStore.data.map { it[KEY_TAP_DIFFICULTY]       ?: 1     }
    val gracePeriodSecs   : Flow<Int>     = ctx.dataStore.data.map { it[KEY_GRACE_PERIOD_SECS]    ?: 30    }
    val reflectionOnExit  : Flow<Boolean> = ctx.dataStore.data.map { it[KEY_REFLECTION_ON_EXIT]   ?: true  }
    val standbyMode       : Flow<Boolean> = ctx.dataStore.data.map { it[KEY_STANDBY_MODE]         ?: true  }
    val ambientMode       : Flow<Boolean> = ctx.dataStore.data.map { it[KEY_AMBIENT_MODE]         ?: true  }
    val hapticEnabled     : Flow<Boolean> = ctx.dataStore.data.map { it[KEY_HAPTIC_ENABLED]       ?: true  }
    val biometricRequired : Flow<Boolean> = ctx.dataStore.data.map { it[KEY_BIOMETRIC_REQUIRED]   ?: false }
    val lockSettingsDuring: Flow<Boolean> = ctx.dataStore.data.map { it[KEY_LOCK_SETTINGS_DURING] ?: true  }

    suspend fun setHardMode(v: Boolean)          { ctx.dataStore.edit { it[KEY_HARD_MODE]           = v } }
    suspend fun setTapDifficulty(v: Int)         { ctx.dataStore.edit { it[KEY_TAP_DIFFICULTY]       = v } }
    suspend fun setGracePeriod(v: Int)           { ctx.dataStore.edit { it[KEY_GRACE_PERIOD_SECS]    = v } }
    suspend fun setReflectionOnExit(v: Boolean)  { ctx.dataStore.edit { it[KEY_REFLECTION_ON_EXIT]   = v } }
    suspend fun setStandbyMode(v: Boolean)       { ctx.dataStore.edit { it[KEY_STANDBY_MODE]         = v } }
    suspend fun setAmbientMode(v: Boolean)       { ctx.dataStore.edit { it[KEY_AMBIENT_MODE]         = v } }
    suspend fun setHapticEnabled(v: Boolean)     { ctx.dataStore.edit { it[KEY_HAPTIC_ENABLED]       = v } }
    suspend fun setBiometricRequired(v: Boolean) { ctx.dataStore.edit { it[KEY_BIOMETRIC_REQUIRED]   = v } }
    suspend fun setLockSettingsDuring(v: Boolean){ ctx.dataStore.edit { it[KEY_LOCK_SETTINGS_DURING] = v } }
    suspend fun resetAll()                       { ctx.dataStore.edit { it.clear() }                      }
}
