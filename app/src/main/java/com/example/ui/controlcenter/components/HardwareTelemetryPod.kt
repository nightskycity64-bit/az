package com.example.ui.controlcenter.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosRed

@Composable
fun HardwareTelemetryPod(
    ramUsedGb: Float,
    ramTotalGb: Float,
    cpuLoadPercent: Int,
    batteryTempC: Int,
    isCleaned: Boolean,
    onCleanRam: () -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ramProgress = (ramUsedGb / ramTotalGb).coerceIn(0f, 1f)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val boostScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = 900f),
        label = "boost_scale"
    )

    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hardware_telemetry_pod")
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x351F2636),
                        Color(0x25141924)
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x440A84FF),
                        Color(0x1A30D158)
                    )
                ),
                shape = shape
            )
            .padding(14.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Performance",
                        tint = IosBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "عملکرد سخت‌افزار (Snapdragon 720G)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // RAM Clean / Boost Turbo Button
                Box(
                    modifier = Modifier
                        .scale(boostScale)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCleaned) IosGreen else IosBlue)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onHaptic()
                            onCleanRam()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Turbo Boost",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isCleaned) "بهینه شد ✓" else "توربو بوست رم",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // RAM Progress bar & Telemetry Numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حافظه رم: ${String.format("%.1f", ramUsedGb)} از ${ramTotalGb.toInt()} گیگابایت",
                    color = Color(0xCCFFFFFF),
                    fontSize = 11.sp
                )
                Text(
                    text = "${(ramProgress * 100).toInt()}%",
                    color = if (ramProgress > 0.8f) IosRed else IosGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { ramProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (ramProgress > 0.8f) IosRed else IosBlue,
                trackColor = Color(0x22FFFFFF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Telemetry Badges (CPU, Temp, Refresh Rate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMiniBadge(
                    icon = Icons.Default.Memory,
                    label = "پردازنده",
                    value = "$cpuLoadPercent%",
                    tint = IosOrange
                )

                TelemetryMiniBadge(
                    icon = Icons.Default.Thermostat,
                    label = "دمای باتری",
                    value = "$batteryTempC°C",
                    tint = if (batteryTempC > 38) IosRed else IosGreen
                )

                TelemetryMiniBadge(
                    icon = Icons.Default.Speed,
                    label = "نرخ نوسازی",
                    value = "60 Hz",
                    tint = IosBlue
                )
            }
        }
    }
}

@Composable
private fun TelemetryMiniBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141926))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: ",
            color = Color(0x77FFFFFF),
            fontSize = 10.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
