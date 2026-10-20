/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object OemAutostartUtil {

    /**
     * Opens the manufacturer's Autostart / background management screen, so OEM power managers (MIUI Powerkeeper,
     * ColorOS, Samsung Care, Vivo) stop killing background accessibility services. Never throws.
     */
    fun openAutostartSettings(context: Context, deviceType: String? = null): Boolean {
        return try {
            val device = OemDeviceRules.resolveEffectiveDevice(deviceType)
            val intents = OemDeviceRules.getAutostartIntents(context, device)

            for (intent in intents) {
                if (context.safeStartActivity(intent, errorMessage = null)) {
                    return true
                }
            }

            false
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Onboarding's "Lock in background / Auto-start" step (Rule_Book 7.2): tries each vendor panel from
     * [OemIntentFactory] in turn, then App info. Returns true when something opened.
     */
    fun openBackgroundLockSettings(context: Context, deviceType: String? = null): Boolean {
        return try {
            val device = OemDeviceRules.resolveEffectiveDevice(deviceType)
            val label = runCatching { context.applicationInfo.loadLabel(context.packageManager).toString() }
                .getOrNull()
            for (target in OemIntentFactory.backgroundTargets(device)) {
                val intent = Intent().setComponent(ComponentName(target.packageName, target.className))
                if (target.passPackage) {
                    intent.putExtra(OemIntentFactory.EXTRA_PACKAGE_NAME, context.packageName)
                    if (label != null) intent.putExtra(OemIntentFactory.EXTRA_PACKAGE_LABEL, label)
                }
                if (context.safeStartActivity(intent, errorMessage = null)) return true
            }
            context.safeStartActivity(appDetailsIntent(context), errorMessage = null)
        } catch (_: Throwable) {
            false
        }
    }

    private fun appDetailsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
}
