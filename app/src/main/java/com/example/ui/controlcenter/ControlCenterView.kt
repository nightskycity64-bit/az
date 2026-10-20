package com.example.ui.controlcenter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ControlCenterState
import com.example.model.ControlCenterThemeStyle
import com.example.model.TileType
import com.example.ui.controlcenter.components.CapsuleSlider
import com.example.ui.controlcenter.components.ConnectivityPod
import com.example.ui.controlcenter.components.CustomizableControlTile
import com.example.ui.controlcenter.components.DynamicIslandCapsule
import com.example.ui.controlcenter.components.HardwareTelemetryPod
import com.example.ui.controlcenter.components.MediaPlayerPod
import com.example.ui.controlcenter.components.QuickMemoPod
import com.example.ui.controlcenter.components.SquircleToggle
import com.example.ui.controlcenter.components.WidePillToggle
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosIndigo
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosPurple
import com.example.ui.theme.IosRed
import com.example.ui.theme.IosYellow

@Composable
fun ControlCenterView(
    state: ControlCenterState,
    onStateChange: (ControlCenterState) -> Unit,
    onDismiss: () -> Unit,
    onHaptic: () -> Unit,
    onLaunchCamera: () -> Unit,
    onLaunchCalculator: () -> Unit,
    onLaunchClock: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenAirplane: () -> Unit,
    onOpenNetwork: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onFlashlightToggle: (Boolean) -> Unit,
    onRotationToggle: () -> Unit,
    onDndToggle: () -> Unit,
    onLaunchApp: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Dynamic Theme Backdrop
    val backdropBrush = when (state.themeStyle) {
        ControlCenterThemeStyle.IOS_LIQUID_GLASS -> Brush.verticalGradient(
            listOf(Color(0xE60A0A0E), Color(0xF2050508))
        )
        ControlCenterThemeStyle.CYBERPUNK_HUD -> Brush.verticalGradient(
            listOf(Color(0xF0050814), Color(0xF50D0518))
        )
        ControlCenterThemeStyle.AMOLED_MIDNIGHT -> Brush.verticalGradient(
            listOf(Color(0xFF000000), Color(0xFF000000))
        )
        ControlCenterThemeStyle.FROSTED_AURORA -> Brush.verticalGradient(
            listOf(Color(0xE6091822), Color(0xF2160926))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("control_center_fullscreen")
            .background(backdropBrush)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -15f) {
                        onDismiss()
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Dynamic Island & Status Bar
            IosHeaderBar(
                state = state,
                onStateChange = onStateChange,
                onHaptic = onHaptic
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Scrollable Grid
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 480.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ROW 1: Connectivity & Media Pods
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ConnectivityPod(
                        airplaneMode = state.airplaneMode,
                        cellularEnabled = state.cellularEnabled,
                        wifiEnabled = state.wifiEnabled,
                        bluetoothEnabled = state.bluetoothEnabled,
                        onAirplaneToggle = {
                            onHaptic()
                            onStateChange(state.copy(airplaneMode = !state.airplaneMode))
                            onOpenAirplane()
                        },
                        onCellularToggle = {
                            onHaptic()
                            onStateChange(state.copy(cellularEnabled = !state.cellularEnabled))
                            onOpenNetwork()
                        },
                        onWifiToggle = {
                            onHaptic()
                            onStateChange(state.copy(wifiEnabled = !state.wifiEnabled))
                            onOpenWifi()
                        },
                        onBluetoothToggle = {
                            onHaptic()
                            onStateChange(state.copy(bluetoothEnabled = !state.bluetoothEnabled))
                            onOpenBluetooth()
                        },
                        onExpand = onOpenWifi,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    MediaPlayerPod(
                        title = state.mediaTitle,
                        artist = state.mediaArtist,
                        isPlaying = state.isPlaying,
                        progress = state.mediaProgress,
                        elapsed = state.mediaElapsed,
                        duration = state.mediaDuration,
                        onPlayPause = {
                            onHaptic()
                            onStateChange(state.copy(isPlaying = !state.isPlaying))
                        },
                        onNext = {
                            onHaptic()
                            onStateChange(state.copy(mediaTitle = "Starboy", mediaProgress = 0.1f))
                        },
                        onPrevious = {
                            onHaptic()
                            onStateChange(state.copy(mediaTitle = "Save Your Tears", mediaProgress = 0.7f))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // ROW 2: Left (Rotation + Mirroring + Focus) & Right (Sliders)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SquircleToggle(
                                icon = Icons.Default.ScreenRotation,
                                isActive = state.rotationLocked,
                                activeColor = IosRed,
                                activeIconColor = Color.White,
                                onClick = {
                                    onHaptic()
                                    onRotationToggle()
                                    onStateChange(state.copy(rotationLocked = !state.rotationLocked))
                                },
                                contentDescription = "Rotation Lock",
                                size = 68.dp,
                                testTag = "rotation_toggle"
                            )

                            SquircleToggle(
                                icon = Icons.Default.ScreenShare,
                                isActive = state.screenMirroring,
                                activeColor = Color.White,
                                activeIconColor = Color(0xFF1C1C1E),
                                onClick = {
                                    onHaptic()
                                    onStateChange(state.copy(screenMirroring = !state.screenMirroring))
                                },
                                contentDescription = "Screen Mirroring",
                                size = 68.dp,
                                testTag = "mirroring_toggle"
                            )
                        }

                        WidePillToggle(
                            icon = Icons.Default.Nightlight,
                            title = "Focus",
                            subtitle = if (state.focusActive) state.focusMode else "Off",
                            isActive = state.focusActive,
                            activeColor = IosPurple,
                            onClick = {
                                onHaptic()
                                onDndToggle()
                                onStateChange(state.copy(focusActive = !state.focusActive))
                            },
                            contentDescription = "Focus Do Not Disturb",
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "focus_pill"
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Right Column: Vertical Capsule Sliders
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        CapsuleSlider(
                            value = state.brightness,
                            onValueChange = { newVal ->
                                onStateChange(state.copy(brightness = newVal))
                                onBrightnessChange(newVal)
                            },
                            icon = Icons.Default.BrightnessMedium,
                            contentDescription = "Screen Brightness",
                            width = 68.dp,
                            height = 152.dp,
                            testTag = "brightness_slider",
                            onDragEnd = onHaptic
                        )

                        CapsuleSlider(
                            value = state.volume,
                            onValueChange = { newVal ->
                                onStateChange(state.copy(volume = newVal))
                                onVolumeChange(newVal)
                            },
                            icon = Icons.Default.VolumeUp,
                            contentDescription = "Media Volume",
                            width = 68.dp,
                            height = 152.dp,
                            testTag = "volume_slider",
                            onDragEnd = onHaptic
                        )
                    }
                }

                // DISTINCTIVE FEATURE 1: REDMI NOTE 9S HARDWARE TELEMETRY & TURBO BOOST
                if (state.showHardwareHud) {
                    HardwareTelemetryPod(
                        ramUsedGb = state.ramUsedGb,
                        ramTotalGb = state.ramTotalGb,
                        cpuLoadPercent = state.cpuLoadPercent,
                        batteryTempC = state.batteryTempC,
                        isCleaned = state.isRamCleaned,
                        onCleanRam = {
                            onStateChange(
                                state.copy(
                                    ramUsedGb = 2.4f,
                                    cpuLoadPercent = 14,
                                    isRamCleaned = true
                                )
                            )
                        },
                        onHaptic = onHaptic
                    )
                }

                // DISTINCTIVE FEATURE 2: DYNAMIC CUSTOMIZABLE TILES & APP SHORTCUTS
                val chunkedTiles = remember(state.customTiles) {
                    state.customTiles.filter { it.isEnabled }.chunked(4)
                }

                chunkedTiles.forEach { rowTiles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowTiles.forEach { tile ->
                            val isActive = when (tile.actionKey) {
                                "FLASHLIGHT" -> state.torchActive
                                "DARK_MODE" -> state.darkModeActive
                                "LOW_POWER" -> state.lowPowerActive
                                "SCREEN_TIMEOUT" -> state.screenTimeoutSec == 120
                                else -> false
                            }

                            CustomizableControlTile(
                                tile = tile,
                                isActive = isActive,
                                overrideSizeDp = state.globalIconSizeDp,
                                overrideShape = state.globalIconShape,
                                onClick = {
                                    onHaptic()
                                    if (tile.type == TileType.APP_SHORTCUT && tile.packageName != null) {
                                        onLaunchApp(tile.packageName)
                                    } else {
                                        when (tile.actionKey) {
                                            "FLASHLIGHT" -> {
                                                val next = !state.torchActive
                                                onFlashlightToggle(next)
                                                onStateChange(state.copy(torchActive = next))
                                            }
                                            "TIMER" -> onLaunchClock()
                                            "CALCULATOR" -> onLaunchCalculator()
                                            "CAMERA" -> onLaunchCamera()
                                            "DARK_MODE" -> onStateChange(state.copy(darkModeActive = !state.darkModeActive))
                                            "LOW_POWER" -> onStateChange(state.copy(lowPowerActive = !state.lowPowerActive))
                                            "QR_SCANNER" -> onLaunchCamera()
                                            "SCREEN_TIMEOUT" -> {
                                                val nextSec = if (state.screenTimeoutSec == 60) 120 else 60
                                                onStateChange(state.copy(screenTimeoutSec = nextSec))
                                            }
                                            "SETTINGS" -> onOpenWifi()
                                        }
                                    }
                                }
                            )
                        }

                        repeat(4 - rowTiles.size) {
                            Spacer(modifier = Modifier.size(state.globalIconSizeDp.dp))
                        }
                    }
                }

                // DISTINCTIVE FEATURE 3: QUICK MEMO & SCRATCHPAD
                if (state.showQuickMemo) {
                    QuickMemoPod(
                        memoText = state.quickMemoText,
                        onSaveMemo = { newMemo ->
                            onStateChange(state.copy(quickMemoText = newMemo))
                        },
                        onHaptic = onHaptic
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Bottom Dismiss Swipe Pill Handle
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(134.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x88FFFFFF))
                    .clickable { onDismiss() }
            )
        }
    }
}

@Composable
private fun IosHeaderBar(
    state: ControlCenterState,
    onStateChange: (ControlCenterState) -> Unit,
    onHaptic: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time & Carrier
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.timeFormatted,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "IRN-4.5G",
                color = Color(0x99FFFFFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // DISTINCTIVE: Dynamic Island Capsule around Redmi Note 9S Hole Punch
        if (state.showDynamicIsland) {
            DynamicIslandCapsule(
                isExpanded = state.isDynamicIslandExpanded,
                onToggleExpand = {
                    onStateChange(state.copy(isDynamicIslandExpanded = !state.isDynamicIslandExpanded))
                },
                title = state.mediaTitle,
                artist = state.mediaArtist,
                batteryPercent = state.batteryPercentage,
                isPlaying = state.isPlaying,
                onPlayPause = {
                    onStateChange(state.copy(isPlaying = !state.isPlaying))
                },
                onHaptic = onHaptic
            )
        } else {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF000000))
            )
        }

        // Battery & Signal status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wi-Fi Connected",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = "${state.batteryPercentage}%",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = "Battery",
                tint = if (state.batteryPercentage > 20) Color.White else IosRed,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
