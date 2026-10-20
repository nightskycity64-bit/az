package com.example.model

enum class HandlePosition {
    TOP_RIGHT,
    RIGHT_EDGE,
    TOP_BAR
}

enum class ControlCenterThemeStyle {
    IOS_LIQUID_GLASS,   // iOS 18 Liquid Glass
    CYBERPUNK_HUD,      // Cyberpunk Gaming Telemetry HUD
    AMOLED_MIDNIGHT,    // AMOLED Pure Black for Battery Saving
    FROSTED_AURORA      // Frosted Aurora Gradient
}

fun defaultControlCenterTiles(): List<ControlCenterTile> = listOf(
    ControlCenterTile(
        id = "torch",
        title = "چراغ قوه",
        type = TileType.SYSTEM_ACTION,
        actionKey = "FLASHLIGHT",
        customActiveColorHex = 0xFFFFD60A,
        customIconColorHex = 0xFF1C1C1E,
        customSizeDp = 68,
        shape = TileShape.SQUIRCLE
    ),
    ControlCenterTile(
        id = "timer",
        title = "تایمر",
        type = TileType.SYSTEM_ACTION,
        actionKey = "TIMER",
        customActiveColorHex = 0xFFFF9F0A,
        customIconColorHex = 0xFFFFFFFF,
        customSizeDp = 68,
        shape = TileShape.SQUIRCLE
    ),
    ControlCenterTile(
        id = "calc",
        title = "ماشین حساب",
        type = TileType.SYSTEM_ACTION,
        actionKey = "CALCULATOR",
        customActiveColorHex = 0xFF30D158,
        customIconColorHex = 0xFFFFFFFF,
        customSizeDp = 68,
        shape = TileShape.SQUIRCLE
    ),
    ControlCenterTile(
        id = "camera",
        title = "دوربین",
        type = TileType.SYSTEM_ACTION,
        actionKey = "CAMERA",
        customActiveColorHex = 0xFF0A84FF,
        customIconColorHex = 0xFFFFFFFF,
        customSizeDp = 68,
        shape = TileShape.SQUIRCLE
    ),
    ControlCenterTile(
        id = "dark_mode",
        title = "حالت شب",
        type = TileType.SYSTEM_ACTION,
        actionKey = "DARK_MODE",
        customActiveColorHex = 0xFF5E5CE6,
        customIconColorHex = 0xFFFFFFFF,
        customSizeDp = 68,
        shape = TileShape.SQUIRCLE
    ),
    ControlCenterTile(
        id = "low_power",
        title = "مصرف بهینه",
        type = TileType.SYSTEM_ACTION,
        actionKey = "LOW_POWER",
        customActiveColorHex = 0xFFFFD60A,
        customIconColorHex = 0xFF1C1C1E,
        customSizeDp = 68,
        shape = TileShape.SQUIRCLE
    ),
    ControlCenterTile(
        id = "qr_scanner",
        title = "اسکنر QR",
        type = TileType.SYSTEM_ACTION,
        actionKey = "QR_SCANNER",
        customActiveColorHex = 0xFF64D2FF,
        customIconColorHex = 0xFF1C1C1E,
        customSizeDp = 68,
        shape = TileShape.SQUIRCLE
    ),
    ControlCenterTile(
        id = "screen_timeout",
        title = "زمان صفحه",
        type = TileType.SYSTEM_ACTION,
        actionKey = "SCREEN_TIMEOUT",
        customActiveColorHex = 0xFF30D158,
        customIconColorHex = 0xFFFFFFFF,
        customSizeDp = 68,
        shape = TileShape.SQUIRCLE
    )
)

data class ControlCenterState(
    // Connectivity
    val wifiEnabled: Boolean = true,
    val wifiSsid: String = "Redmi_5G_Home",
    val bluetoothEnabled: Boolean = true,
    val bluetoothDevice: String = "AirPods Pro",
    val cellularEnabled: Boolean = true,
    val carrierName: String = "Irancell 4.5G",
    val airplaneMode: Boolean = false,
    val hotspotEnabled: Boolean = false,
    val quickShareEnabled: Boolean = true,

    // Sliders (0.0f to 1.0f)
    val brightness: Float = 0.72f,
    val volume: Float = 0.58f,

    // Controls
    val torchActive: Boolean = false,
    val torchLevel: Int = 3,
    val rotationLocked: Boolean = true,
    val focusActive: Boolean = false,
    val focusMode: String = "Personal",
    val screenMirroring: Boolean = false,
    val darkModeActive: Boolean = true,
    val lowPowerActive: Boolean = false,
    val nightShiftActive: Boolean = false,
    val screenTimeoutSec: Int = 60,

    // Media
    val mediaTitle: String = "Blinding Lights",
    val mediaArtist: String = "The Weeknd",
    val mediaApp: String = "Music",
    val isPlaying: Boolean = true,
    val mediaProgress: Float = 0.45f,
    val mediaElapsed: String = "1:32",
    val mediaDuration: String = "3:20",

    // Device telemetry (simulated Redmi Note 9S specs)
    val batteryPercentage: Int = 84,
    val isCharging: Boolean = false,
    val timeFormatted: String = "14:04",
    val dateFormatted: String = "دوشنبه ۲۸ مهر",

    // Overlay Settings
    val isServiceRunning: Boolean = false,
    val handlePosition: HandlePosition = HandlePosition.TOP_RIGHT,
    val handleWidth: Int = 120, // dp
    val handleHeight: Int = 32, // dp
    val handleOpacity: Float = 0.45f,
    val handleColorHex: Long = 0xFF0A84FF,
    val hapticFeedback: Boolean = true,
    val triggerVibration: Boolean = true,
    val backgroundBlurAlpha: Float = 0.85f,
    val isLauncherIconVisible: Boolean = true,

    // Customizable Tiles and Icons
    val globalIconSizeDp: Int = 68,
    val globalIconShape: TileShape = TileShape.SQUIRCLE,
    val customTiles: List<ControlCenterTile> = defaultControlCenterTiles(),

    // Unique Features: Theme Engine, Hardware HUD, Dynamic Island & Quick Memo
    val themeStyle: ControlCenterThemeStyle = ControlCenterThemeStyle.IOS_LIQUID_GLASS,
    val showDynamicIsland: Boolean = true,
    val isDynamicIslandExpanded: Boolean = false,
    val showHardwareHud: Boolean = true,
    val ramUsedGb: Float = 3.6f,
    val ramTotalGb: Float = 6.0f,
    val cpuLoadPercent: Int = 28,
    val batteryTempC: Int = 33,
    val isRamCleaned: Boolean = false,
    val quickMemoText: String = "خرید باتری و کابل شارژ",
    val showQuickMemo: Boolean = true
)
