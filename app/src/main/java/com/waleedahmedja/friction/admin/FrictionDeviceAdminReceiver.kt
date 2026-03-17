package com.waleedahmedja.friction.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class FrictionDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {}
    override fun onDisabled(context: Context, intent: Intent) {}

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        val active = context
            .getSharedPreferences("friction_admin_cache", Context.MODE_PRIVATE)
            .getBoolean("session_active", false)
        return if (active)
            "A Friction Hard Mode session is active. Disabling admin will end your session."
        else null
    }

    companion object {

        fun getComponentName(ctx: Context): ComponentName =
            ComponentName(ctx, FrictionDeviceAdminReceiver::class.java)

        fun isAdminActive(ctx: Context): Boolean {
            val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return dpm.isAdminActive(getComponentName(ctx))
        }

        fun buildActivationIntent(ctx: Context): Intent =
            Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(ctx))
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Required for Hard Commitment Mode. Removed automatically when your session ends."
                )
            }

        fun revokeAdmin(ctx: Context) {
            val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val cn  = getComponentName(ctx)
            if (dpm.isAdminActive(cn)) dpm.removeActiveAdmin(cn)
            markSessionActive(ctx, false)
        }

        fun markSessionActive(ctx: Context, active: Boolean) {
            ctx.getSharedPreferences("friction_admin_cache", Context.MODE_PRIVATE)
                .edit().putBoolean("session_active", active).apply()
        }
    }
}
