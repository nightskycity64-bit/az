package com.example.system

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

class SystemControlManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private var cameraIdWithFlash: String? = null

    init {
        try {
            cameraManager?.cameraIdList?.firstOrNull()?.let { id ->
                cameraIdWithFlash = id
            }
        } catch (e: Exception) {
            Log.w("SystemControlManager", "Could not query camera flash: ${e.message}")
        }
    }

    // --- Vibration / Haptics ---
    fun performHapticClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}
    }

    // --- Flashlight ---
    fun setFlashlight(enabled: Boolean): Boolean {
        return try {
            val camId = cameraIdWithFlash ?: "0"
            cameraManager?.setTorchMode(camId, enabled)
            true
        } catch (e: CameraAccessException) {
            Log.w("SystemControlManager", "Torch mode error: ${e.message}")
            false
        } catch (e: Exception) {
            Log.w("SystemControlManager", "Torch general error: ${e.message}")
            false
        }
    }

    // --- Brightness ---
    fun getSystemBrightness(): Float {
        return try {
            val raw = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                128
            )
            (raw / 255f).coerceIn(0f, 1f)
        } catch (e: Exception) {
            0.7f
        }
    }

    fun setSystemBrightness(value: Float) {
        if (!hasWriteSettingsPermission()) return
        try {
            val intVal = (value.coerceIn(0.05f, 1.0f) * 255).toInt()
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                intVal
            )
        } catch (e: Exception) {
            Log.w("SystemControlManager", "Failed to set brightness: ${e.message}")
        }
    }

    // --- Volume ---
    fun getSystemVolume(): Float {
        audioManager ?: return 0.5f
        return try {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (max > 0) (current.toFloat() / max).coerceIn(0f, 1f) else 0.5f
        } catch (e: Exception) {
            0.5f
        }
    }

    fun setSystemVolume(value: Float) {
        audioManager ?: return
        try {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (value.coerceIn(0f, 1f) * max).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        } catch (e: Exception) {
            Log.w("SystemControlManager", "Failed to set volume: ${e.message}")
        }
    }

    // --- Rotation Lock ---
    fun toggleRotationLock(currentlyLocked: Boolean): Boolean {
        if (!hasWriteSettingsPermission()) return !currentlyLocked
        return try {
            val newLocked = !currentlyLocked
            val autoRotateValue = if (newLocked) 0 else 1
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                autoRotateValue
            )
            newLocked
        } catch (e: Exception) {
            !currentlyLocked
        }
    }

    // --- Do Not Disturb (Focus) ---
    fun toggleDoNotDisturb(currentlyActive: Boolean): Boolean {
        notificationManager ?: return !currentlyActive
        if (!hasNotificationPolicyAccess()) return !currentlyActive
        return try {
            val newActive = !currentlyActive
            if (newActive) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
            newActive
        } catch (e: Exception) {
            !currentlyActive
        }
    }

    // --- Permissions ---
    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun hasWriteSettingsPermission(): Boolean {
        return Settings.System.canWrite(context)
    }

    fun hasNotificationPolicyAccess(): Boolean {
        return notificationManager?.isNotificationPolicyAccessGranted == true
    }

    // --- Intent Openers ---
    fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        tryLaunch(intent)
    }

    fun openWriteSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        tryLaunch(intent)
    }

    fun openNotificationPolicySettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        tryLaunch(intent)
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        tryLaunch(intent)
    }

    fun openMiuiAutostartSettings(): Boolean {
        val intent = Intent().apply {
            component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return tryLaunch(intent)
    }

    fun openMiuiBatteryOptimization(): Boolean {
        val intent = Intent().apply {
            component = ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")
            putExtra("package_name", context.packageName)
            putExtra("package_label", "Control Center iOS 18")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return tryLaunch(intent)
    }

    fun launchCamera() {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        tryLaunch(intent)
    }

    fun launchCalculator() {
        val miuiCalc = Intent().apply {
            component = ComponentName("com.miui.calculator", "com.miui.calculator.cal.CalculatorActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (!tryLaunch(miuiCalc)) {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            tryLaunch(intent)
        }
    }

    fun launchClockOrTimer() {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (!tryLaunch(intent)) {
            val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, 60)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            tryLaunch(timerIntent)
        }
    }

    fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        tryLaunch(intent)
    }

    fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        tryLaunch(intent)
    }

    fun openAirplaneModeSettings() {
        val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        tryLaunch(intent)
    }

    fun openNetworkSettings() {
        val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (!tryLaunch(intent)) {
            tryLaunch(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }
    }

    fun setLauncherIconVisible(visible: Boolean): Boolean {
        return try {
            val componentName = ComponentName(context.packageName, "${context.packageName}.LauncherAlias")
            val newState = if (visible) {
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            context.packageManager.setComponentEnabledSetting(
                componentName,
                newState,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
            true
        } catch (e: Exception) {
            Log.w("SystemControlManager", "Could not toggle launcher alias: ${e.message}")
            false
        }
    }

    fun isLauncherIconVisible(): Boolean {
        return try {
            val componentName = ComponentName(context.packageName, "${context.packageName}.LauncherAlias")
            val setting = context.packageManager.getComponentEnabledSetting(componentName)
            setting != android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } catch (e: Exception) {
            true
        }
    }

    fun getInstalledApps(): List<com.example.model.InstalledAppItem> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return try {
            val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(mainIntent, android.content.pm.PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(mainIntent, 0)
            }
            resolved.mapNotNull { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(pm).toString()
                if (pkg != context.packageName) {
                    com.example.model.InstalledAppItem(packageName = pkg, appName = label)
                } else null
            }.distinctBy { it.packageName }.sortedBy { it.appName }
        } catch (e: Exception) {
            listOf(
                com.example.model.InstalledAppItem("com.whatsapp", "WhatsApp"),
                com.example.model.InstalledAppItem("org.telegram.messenger", "Telegram"),
                com.example.model.InstalledAppItem("com.instagram.android", "Instagram"),
                com.example.model.InstalledAppItem("com.google.android.youtube", "YouTube"),
                com.example.model.InstalledAppItem("com.spotify.music", "Spotify"),
                com.example.model.InstalledAppItem("com.android.chrome", "Chrome"),
                com.example.model.InstalledAppItem("com.miui.gallery", "گالری شیائومی (Gallery)"),
                com.example.model.InstalledAppItem("com.android.settings", "تنظیمات (Settings)")
            )
        }
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun tryLaunch(intent: Intent): Boolean {
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.w("SystemControlManager", "Could not start activity: ${e.message}")
            false
        }
    }
}
