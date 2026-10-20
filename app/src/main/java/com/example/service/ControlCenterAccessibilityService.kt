package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class ControlCenterAccessibilityService : AccessibilityService() {

    companion object {
        var instance: ControlCenterAccessibilityService? = null
            private set

        const val ACTION_PULL_NOTIFICATIONS = "ACTION_PULL_NOTIFICATIONS"
        const val ACTION_GO_HOME = "ACTION_GO_HOME"
        const val ACTION_LOCK_SCREEN = "ACTION_LOCK_SCREEN"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PULL_NOTIFICATIONS -> performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            ACTION_GO_HOME -> performGlobalAction(GLOBAL_ACTION_HOME)
            ACTION_LOCK_SCREEN -> performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: We only use this service for optional global gesture actions
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}
