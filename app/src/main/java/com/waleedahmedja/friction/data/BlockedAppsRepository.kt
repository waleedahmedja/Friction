package com.waleedahmedja.friction.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.blockedAppsStore: DataStore<Preferences>
        by preferencesDataStore(name = "friction_blocked_apps")

class BlockedAppsRepository(private val ctx: Context) {

    companion object {
        private val KEY_BLOCKED = stringSetPreferencesKey("blocked_packages")
        private val KEY_ALLOWED = stringSetPreferencesKey("allowed_packages")
    }

    val blockedPackages: Flow<Set<String>> =
        ctx.blockedAppsStore.data.map { it[KEY_BLOCKED] ?: emptySet() }

    val allowedPackages: Flow<Set<String>> =
        ctx.blockedAppsStore.data.map { it[KEY_ALLOWED] ?: emptySet() }

    suspend fun getBlockedPackages(): Set<String> =
        ctx.blockedAppsStore.data.map { it[KEY_BLOCKED] ?: emptySet() }.first()

    suspend fun getAllowedPackages(): Set<String> =
        ctx.blockedAppsStore.data.map { it[KEY_ALLOWED] ?: emptySet() }.first()

    suspend fun addBlocked(pkg: String) {
        ctx.blockedAppsStore.edit { it[KEY_BLOCKED] = (it[KEY_BLOCKED] ?: emptySet()) + pkg }
    }

    suspend fun removeBlocked(pkg: String) {
        ctx.blockedAppsStore.edit { it[KEY_BLOCKED] = (it[KEY_BLOCKED] ?: emptySet()) - pkg }
    }

    suspend fun addAllowed(pkg: String) {
        ctx.blockedAppsStore.edit { it[KEY_ALLOWED] = (it[KEY_ALLOWED] ?: emptySet()) + pkg }
    }

    suspend fun removeAllowed(pkg: String) {
        ctx.blockedAppsStore.edit { it[KEY_ALLOWED] = (it[KEY_ALLOWED] ?: emptySet()) - pkg }
    }

    suspend fun clearAll() { ctx.blockedAppsStore.edit { it.clear() } }
}