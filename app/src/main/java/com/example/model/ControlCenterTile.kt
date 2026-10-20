package com.example.model

enum class TileShape {
    SQUIRCLE,       // iOS 18 squircle (22dp)
    CIRCLE,         // Circular (50%)
    ROUNDED_SQUARE, // Subtle rounded (12dp)
    PILL            // Smooth superellipse (28dp)
}

enum class TileType {
    SYSTEM_ACTION,
    APP_SHORTCUT
}

data class ControlCenterTile(
    val id: String,
    val title: String,
    val type: TileType = TileType.SYSTEM_ACTION,
    val actionKey: String = "", // e.g. "FLASHLIGHT", "TIMER", "CALCULATOR", "CAMERA", "DARK_MODE", "LOW_POWER", "QR_SCANNER", "SCREEN_TIMEOUT"
    val packageName: String? = null,
    val shape: TileShape = TileShape.SQUIRCLE,
    val customSizeDp: Int = 68,
    val customColorHex: Long = 0xFF352A2A32,
    val customActiveColorHex: Long = 0xFF0A84FF,
    val customIconColorHex: Long = 0xFFFFFFFF,
    val customPhotoUri: String? = null,
    val isWide: Boolean = false,
    val isEnabled: Boolean = true
)

data class InstalledAppItem(
    val packageName: String,
    val appName: String
)
