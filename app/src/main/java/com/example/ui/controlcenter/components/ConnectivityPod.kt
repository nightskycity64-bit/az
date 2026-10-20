package com.example.ui.controlcenter.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosOrange

@Composable
fun ConnectivityPod(
    airplaneMode: Boolean,
    cellularEnabled: Boolean,
    wifiEnabled: Boolean,
    bluetoothEnabled: Boolean,
    onAirplaneToggle: () -> Unit,
    onCellularToggle: () -> Unit,
    onWifiToggle: () -> Unit,
    onBluetoothToggle: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 152.dp,
    testTag: String = "connectivity_pod"
) {
    val shape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .size(size)
            .testTag(testTag)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x382A2A32),
                        Color(0x281C1C22)
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x35FFFFFF),
                        Color(0x10FFFFFF)
                    )
                ),
                shape = shape
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxSize().weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Airplane Mode
                ConnectivityRoundButton(
                    icon = Icons.Default.AirplanemodeActive,
                    isActive = airplaneMode,
                    activeColor = IosOrange,
                    onClick = onAirplaneToggle,
                    contentDescription = "Airplane Mode",
                    testTag = "airplane_toggle"
                )

                // Cellular Data
                ConnectivityRoundButton(
                    icon = Icons.Default.NetworkCell,
                    isActive = cellularEnabled,
                    activeColor = IosGreen,
                    onClick = onCellularToggle,
                    contentDescription = "Cellular Data",
                    testTag = "cellular_toggle"
                )
            }

            Row(
                modifier = Modifier.fillMaxSize().weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wi-Fi
                ConnectivityRoundButton(
                    icon = Icons.Default.Wifi,
                    isActive = wifiEnabled,
                    activeColor = IosBlue,
                    onClick = onWifiToggle,
                    contentDescription = "Wi-Fi",
                    testTag = "wifi_toggle"
                )

                // Bluetooth
                ConnectivityRoundButton(
                    icon = Icons.Default.Bluetooth,
                    isActive = bluetoothEnabled,
                    activeColor = IosBlue,
                    onClick = onBluetoothToggle,
                    contentDescription = "Bluetooth",
                    testTag = "bluetooth_toggle"
                )
            }
        }
    }
}

@Composable
fun ConnectivityRoundButton(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    testTag: String = "conn_round_btn"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = 1000f),
        label = "btn_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isActive) activeColor else Color(0x33555562),
        label = "btn_bg"
    )

    Box(
        modifier = modifier
            .size(52.dp)
            .scale(scale)
            .testTag(testTag)
            .clip(CircleShape)
            .background(bgColor, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
