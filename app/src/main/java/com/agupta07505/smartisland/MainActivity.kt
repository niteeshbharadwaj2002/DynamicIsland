/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.agupta07505.smartisland.util.PermissionGate
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agupta07505.smartisland.data.SmartIslandSettings
import com.agupta07505.smartisland.data.INotificationRepository
import com.agupta07505.smartisland.data.SmartIslandSettingsRepository
import com.agupta07505.smartisland.ui.SmartIslandHomeScreen
import com.agupta07505.smartisland.ui.SmartIslandTheme
import com.agupta07505.smartisland.util.SystemServiceRecovery
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepository: SmartIslandSettingsRepository
    @Inject lateinit var notificationRepository: INotificationRepository

    private var requestedSection by mutableStateOf<String?>(null)

    // Bluetooth connect/disconnect broadcasts (device name, battery) are only delivered once this runtime permission is granted.
    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // Whether setup (overlay + notification access) was complete at the last look; a later miss means a grant was revoked.
    private var setupWasComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedSection = sectionFrom(intent)
        SystemServiceRecovery.requestRecovery(this)
        setupWasComplete = !PermissionGate.check(this).needsSetup
        requestBluetoothPermissionIfNeeded()

        setContent {
            val themeSettings by settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = SmartIslandSettings.Default
            )
            SmartIslandTheme(
                themeMode = themeSettings.themeMode,
                liquidGlass = themeSettings.liquidGlassTheme,
                materialYou = themeSettings.materialYou,
                accentColor = themeSettings.accentColor
            ) {
                SmartIslandHomeScreen(
                    repository = settingsRepository,
                    notificationRepository = notificationRepository,
                    openSection = requestedSection,
                    onOpenSectionHandled = { requestedSection = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedSection = sectionFrom(intent)
    }

    /** The section an intent asks for: the island's own request, or the permission-recovery notification's. */
    private fun sectionFrom(intent: Intent?): String? =
        intent?.getStringExtra(com.agupta07505.smartisland.util.UtilityActions.EXTRA_OPEN_SECTION)
            ?: if (intent?.getBooleanExtra(com.agupta07505.smartisland.util.PermissionRecoveryNotifier.EXTRA_OPEN_PERMISSIONS, false) == true) {
                com.agupta07505.smartisland.util.UtilityActions.SECTION_PERMISSIONS
            } else null

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // The app widget setup screen reports back by request code; the result is also passed on as usual.
        com.agupta07505.smartisland.util.UtilityWidgets.onActivityResult(requestCode, resultCode)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        SystemServiceRecovery.requestRecovery(this)
        // Either grant can be revoked while the app is away (Rule_Book 7.1); send the user back to setup if one is
        // gone. First-run setup is left alone: this only fires after setup had been complete.
        val needsSetup = PermissionGate.check(this).needsSetup
        if (needsSetup && setupWasComplete && requestedSection == null) {
            requestedSection = com.agupta07505.smartisland.util.UtilityActions.SECTION_PERMISSIONS
        }
        setupWasComplete = !needsSetup
    }

    private fun requestBluetoothPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }
}
