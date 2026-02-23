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
        val KEY_UNLOCK_TIME    = longPreferencesKey("unlock_time")
        val KEY_TOTAL_DURATION = longPreferencesKey("total_duration_ms")
        val KEY_LOCK_ACTIVE    = booleanPreferencesKey("lock_active")
        val KEY_MSG_INDEX      = intPreferencesKey("msg_index")
    }

    val unlockTime: Flow<Long>    = ctx.dataStore.data.map { it[KEY_UNLOCK_TIME]    ?: 0L }
    val totalDuration: Flow<Long> = ctx.dataStore.data.map { it[KEY_TOTAL_DURATION] ?: 0L }
    val lockActive: Flow<Boolean> = ctx.dataStore.data.map { it[KEY_LOCK_ACTIVE]    ?: false }
    val msgIndex: Flow<Int>       = ctx.dataStore.data.map { it[KEY_MSG_INDEX]      ?: 0 }

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
}