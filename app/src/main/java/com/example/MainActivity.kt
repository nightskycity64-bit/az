package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.model.ControlCenterState
import com.example.service.ControlCenterOverlayService
import com.example.system.SystemControlManager
import com.example.ui.controlcenter.ControlCenterView
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var systemControlManager: SystemControlManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        systemControlManager = SystemControlManager(this)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                var state by remember {
                    mutableStateOf(
                        ControlCenterState(
                            brightness = systemControlManager.getSystemBrightness(),
                            volume = systemControlManager.getSystemVolume(),
                            isLauncherIconVisible = systemControlManager.isLauncherIconVisible()
                        )
                    )
                }

                var isPreviewOpen by remember { mutableStateOf(false) }
                var hasOverlayPerm by remember { mutableStateOf(systemControlManager.hasOverlayPermission()) }
                var hasWriteSettingsPerm by remember { mutableStateOf(systemControlManager.hasWriteSettingsPermission()) }
                var hasDndPerm by remember { mutableStateOf(systemControlManager.hasNotificationPolicyAccess()) }

                // Refresh permissions on resume
                val lifecycleOwner = LocalLifecycleOwner.current
                LaunchedEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasOverlayPerm = systemControlManager.hasOverlayPermission()
                            hasWriteSettingsPerm = systemControlManager.hasWriteSettingsPermission()
                            hasDndPerm = systemControlManager.hasNotificationPolicyAccess()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                }

                BackHandler(enabled = isPreviewOpen) {
                    isPreviewOpen = false
                }

                val installedApps = remember { systemControlManager.getInstalledApps() }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0C0E14))
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        DashboardScreen(
                            state = state,
                            onStateChange = { state = it },
                            hasOverlayPerm = hasOverlayPerm,
                            hasWriteSettingsPerm = hasWriteSettingsPerm,
                            hasDndPerm = hasDndPerm,
                            installedApps = installedApps,
                            onToggleService = { enable ->
                                if (enable) {
                                    if (!systemControlManager.hasOverlayPermission()) {
                                        systemControlManager.openOverlaySettings()
                                    } else {
                                        startOverlayService()
                                        state = state.copy(isServiceRunning = true)
                                    }
                                } else {
                                    stopOverlayService()
                                    state = state.copy(isServiceRunning = false)
                                }
                            },
                            onToggleLauncherIcon = { visible ->
                                val success = systemControlManager.setLauncherIconVisible(visible)
                                if (success) {
                                    state = state.copy(isLauncherIconVisible = visible)
                                }
                            },
                            onOpenPreview = {
                                systemControlManager.performHapticClick()
                                isPreviewOpen = true
                            },
                            onGrantOverlay = { systemControlManager.openOverlaySettings() },
                            onGrantWriteSettings = { systemControlManager.openWriteSettings() },
                            onGrantDnd = { systemControlManager.openNotificationPolicySettings() },
                            onOpenAutostart = { systemControlManager.openMiuiAutostartSettings() },
                            onOpenBatteryOpt = { systemControlManager.openMiuiBatteryOptimization() },
                            onOpenAccessibility = { systemControlManager.openAccessibilitySettings() },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    // Full-screen interactive iOS 18 Control Center preview overlay
                    AnimatedVisibility(
                        visible = isPreviewOpen,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    ) {
                        ControlCenterView(
                            state = state,
                            onStateChange = { state = it },
                            onDismiss = {
                                systemControlManager.performHapticClick()
                                isPreviewOpen = false
                            },
                            onHaptic = { systemControlManager.performHapticClick() },
                            onLaunchCamera = {
                                isPreviewOpen = false
                                systemControlManager.launchCamera()
                            },
                            onLaunchCalculator = {
                                isPreviewOpen = false
                                systemControlManager.launchCalculator()
                            },
                            onLaunchClock = {
                                isPreviewOpen = false
                                systemControlManager.launchClockOrTimer()
                            },
                            onOpenWifi = {
                                isPreviewOpen = false
                                systemControlManager.openWifiSettings()
                            },
                            onOpenBluetooth = {
                                isPreviewOpen = false
                                systemControlManager.openBluetoothSettings()
                            },
                            onOpenAirplane = {
                                isPreviewOpen = false
                                systemControlManager.openAirplaneModeSettings()
                            },
                            onOpenNetwork = {
                                isPreviewOpen = false
                                systemControlManager.openNetworkSettings()
                            },
                            onBrightnessChange = { value ->
                                systemControlManager.setSystemBrightness(value)
                            },
                            onVolumeChange = { value ->
                                systemControlManager.setSystemVolume(value)
                            },
                            onFlashlightToggle = { active ->
                                systemControlManager.setFlashlight(active)
                            },
                            onRotationToggle = {
                                systemControlManager.toggleRotationLock(state.rotationLocked)
                            },
                            onDndToggle = {
                                systemControlManager.toggleDoNotDisturb(state.focusActive)
                            },
                            onLaunchApp = { pkg ->
                                isPreviewOpen = false
                                systemControlManager.launchApp(pkg)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, ControlCenterOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopOverlayService() {
        val intent = Intent(this, ControlCenterOverlayService::class.java).apply {
            action = ControlCenterOverlayService.ACTION_STOP
        }
        startService(intent)
    }
}
