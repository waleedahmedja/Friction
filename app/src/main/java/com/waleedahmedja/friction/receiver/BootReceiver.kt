package com.waleedahmedja.friction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.waleedahmedja.friction.data.DataStoreManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action !in listOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED
            )
        ) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ds       = DataStoreManager(context)
                val active   = ds.lockActive.first()
                val unlockAt = ds.unlockTime.first()
                if (!active || unlockAt <= 0L) return@launch
                if (System.currentTimeMillis() >= unlockAt) { ds.clearLock(); return@launch }
                context.packageManager
                    .getLaunchIntentForPackage(context.packageName)
                    ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    ?.let { context.startActivity(it) }
            } finally {
                pending.finish()
            }
        }
    }
}