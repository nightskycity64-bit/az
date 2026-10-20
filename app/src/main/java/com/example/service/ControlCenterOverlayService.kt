package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.model.ControlCenterState
import com.example.system.SystemControlManager
import com.example.ui.controlcenter.ControlCenterView

class ControlCenterOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var systemControlManager: SystemControlManager

    private var handleView: View? = null
    private var overlayComposeView: ComposeView? = null
    private var isOverlayOpen = false

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val appViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = appViewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var currentState = ControlCenterState(isServiceRunning = true)

    companion object {
        const val CHANNEL_ID = "control_center_service_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_CONTROL_CENTER"
        const val ACTION_STOP = "ACTION_STOP_CONTROL_CENTER"
        const val ACTION_TOGGLE_VIEW = "ACTION_TOGGLE_VIEW"
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        systemControlManager = SystemControlManager(this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        createEdgeHandle()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_TOGGLE_VIEW -> {
                toggleControlCenterOverlay()
            }
        }
        return START_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createEdgeHandle() {
        if (handleView != null) return

        val params = WindowManager.LayoutParams(
            220, // width in pixels
            70,  // height in pixels
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 10
            y = 0
        }

        val handle = View(this).apply {
            setBackgroundColor(android.graphics.Color.argb(70, 10, 132, 255))
            var initialY = 0f
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dy = event.rawY - initialY
                        if (dy > 30f && !isOverlayOpen) {
                            openControlCenterOverlay()
                            true
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val dy = event.rawY - initialY
                        if (dy > 20f || Math.abs(dy) < 10f) {
                            openControlCenterOverlay()
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        try {
            windowManager.addView(handle, params)
            handleView = handle
        } catch (_: Exception) {}
    }

    private fun openControlCenterOverlay() {
        if (isOverlayOpen) return
        systemControlManager.performHapticClick()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@ControlCenterOverlayService)
            setViewTreeViewModelStoreOwner(this@ControlCenterOverlayService)
            setViewTreeSavedStateRegistryOwner(this@ControlCenterOverlayService)

            setContent {
                ControlCenterView(
                    state = currentState,
                    onStateChange = { newState ->
                        currentState = newState
                    },
                    onDismiss = {
                        closeControlCenterOverlay()
                    },
                    onHaptic = { systemControlManager.performHapticClick() },
                    onLaunchCamera = {
                        closeControlCenterOverlay()
                        systemControlManager.launchCamera()
                    },
                    onLaunchCalculator = {
                        closeControlCenterOverlay()
                        systemControlManager.launchCalculator()
                    },
                    onLaunchClock = {
                        closeControlCenterOverlay()
                        systemControlManager.launchClockOrTimer()
                    },
                    onOpenWifi = {
                        closeControlCenterOverlay()
                        systemControlManager.openWifiSettings()
                    },
                    onOpenBluetooth = {
                        closeControlCenterOverlay()
                        systemControlManager.openBluetoothSettings()
                    },
                    onOpenAirplane = {
                        closeControlCenterOverlay()
                        systemControlManager.openAirplaneModeSettings()
                    },
                    onOpenNetwork = {
                        closeControlCenterOverlay()
                        systemControlManager.openNetworkSettings()
                    },
                    onBrightnessChange = { valFloat ->
                        systemControlManager.setSystemBrightness(valFloat)
                    },
                    onVolumeChange = { valFloat ->
                        systemControlManager.setSystemVolume(valFloat)
                    },
                    onFlashlightToggle = { enabled ->
                        systemControlManager.setFlashlight(enabled)
                    },
                    onRotationToggle = {
                        systemControlManager.toggleRotationLock(currentState.rotationLocked)
                    },
                    onDndToggle = {
                        systemControlManager.toggleDoNotDisturb(currentState.focusActive)
                    },
                    onLaunchApp = { pkg ->
                        closeControlCenterOverlay()
                        systemControlManager.launchApp(pkg)
                    }
                )
            }
        }

        try {
            windowManager.addView(composeView, params)
            overlayComposeView = composeView
            isOverlayOpen = true
        } catch (_: Exception) {}
    }

    private fun closeControlCenterOverlay() {
        if (!isOverlayOpen || overlayComposeView == null) return
        systemControlManager.performHapticClick()
        try {
            windowManager.removeView(overlayComposeView)
            overlayComposeView = null
            isOverlayOpen = false
        } catch (_: Exception) {}
    }

    private fun toggleControlCenterOverlay() {
        if (isOverlayOpen) closeControlCenterOverlay() else openControlCenterOverlay()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "iOS 18 Control Center Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps edge gesture listener active without battery drain"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("کنترل سنتر iOS 18 ردمی نوت 9 اس")
            .setContentText("فعال • برای باز شدن از گوشه بالا سمت راست به پایین بکشید")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        closeControlCenterOverlay()
        handleView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
