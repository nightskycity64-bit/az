package com.example.ui.controlcenter.components

import android.net.Uri
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.ControlCenterTile
import com.example.model.TileShape
import com.example.model.TileType

fun resolveTileShape(shape: TileShape, sizeDp: Int): Shape {
    return when (shape) {
        TileShape.SQUIRCLE -> RoundedCornerShape(22.dp)
        TileShape.CIRCLE -> CircleShape
        TileShape.ROUNDED_SQUARE -> RoundedCornerShape(12.dp)
        TileShape.PILL -> RoundedCornerShape(28.dp)
    }
}

fun resolveSystemIcon(actionKey: String): ImageVector {
    return when (actionKey) {
        "FLASHLIGHT" -> Icons.Default.FlashlightOn
        "TIMER" -> Icons.Outlined.Timer
        "CALCULATOR" -> Icons.Default.Calculate
        "CAMERA" -> Icons.Default.PhotoCamera
        "DARK_MODE" -> Icons.Default.DarkMode
        "LOW_POWER" -> Icons.Outlined.BatteryAlert
        "QR_SCANNER" -> Icons.Default.QrCodeScanner
        "SCREEN_TIMEOUT" -> Icons.Default.HourglassTop
        "SETTINGS" -> Icons.Default.Settings
        else -> Icons.Default.Apps
    }
}

@Composable
fun CustomizableControlTile(
    tile: ControlCenterTile,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    overrideSizeDp: Int? = null,
    overrideShape: TileShape? = null
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(stiffness = 900f),
        label = "tile_scale"
    )

    val actualSizeDp = overrideSizeDp ?: tile.customSizeDp
    val actualShapeType = overrideShape ?: tile.shape
    val shape = resolveTileShape(actualShapeType, actualSizeDp)

    val defaultBg = Color(tile.customColorHex)
    val activeBg = Color(tile.customActiveColorHex)
    val iconColor = Color(tile.customIconColorHex)

    val bgColor by animateColorAsState(
        targetValue = if (isActive) activeBg else defaultBg,
        label = "tile_bg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(actualSizeDp.dp)
                .scale(scale)
                .testTag("tile_${tile.id}")
                .clip(shape)
                .background(bgColor, shape)
                .border(
                    width = 1.dp,
                    color = if (isActive) Color(0x44FFFFFF) else Color(0x24FFFFFF),
                    shape = shape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // If custom photo is chosen, display it!
            if (!tile.customPhotoUri.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(tile.customPhotoUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = tile.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Frosted subtle overlay so it blends into Control Center
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x33000000))
                )
            } else if (tile.type == TileType.APP_SHORTCUT && tile.packageName != null) {
                // Load installed app icon
                val pm = context.packageManager
                val appIcon = remember(tile.packageName) {
                    try {
                        pm.getApplicationIcon(tile.packageName)
                    } catch (_: Exception) {
                        null
                    }
                }

                if (appIcon != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(appIcon)
                            .crossfade(true)
                            .build(),
                        contentDescription = tile.title,
                        modifier = Modifier.size((actualSizeDp * 0.55f).dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = tile.title,
                        tint = iconColor,
                        modifier = Modifier.size((actualSizeDp * 0.45f).dp)
                    )
                }
            } else {
                // System Action vector icon
                Icon(
                    imageVector = resolveSystemIcon(tile.actionKey),
                    contentDescription = tile.title,
                    tint = if (isActive && tile.customActiveColorHex == 0xFFFFD60AL) Color(0xFF1C1C1E) else iconColor,
                    modifier = Modifier.size((actualSizeDp * 0.44f).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tile.title,
            color = Color(0xCCFFFFFF),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SquircleToggle(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFFFFFFFF),
    activeIconColor: Color = Color(0xFF1C1C1E),
    size: Dp = 68.dp,
    label: String? = null,
    testTag: String = "squircle_toggle"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = 900f),
        label = "toggle_scale"
    )

    val shape = RoundedCornerShape(22.dp)

    val bgColor by animateColorAsState(
        targetValue = if (isActive) activeColor else Color(0x352A2A32),
        label = "toggle_bg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isActive) activeIconColor else Color(0xFFFFFFFF),
        label = "toggle_icon"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(scale)
                .testTag(testTag)
                .clip(shape)
                .background(bgColor, shape)
                .border(
                    width = 1.dp,
                    color = if (isActive) Color(0x33FFFFFF) else Color(0x28FFFFFF),
                    shape = shape
                )
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
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
        }

        if (label != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = Color(0xCCFFFFFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WidePillToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF5E5CE6),
    testTag: String = "wide_pill_toggle"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = 900f),
        label = "pill_scale"
    )

    val shape = RoundedCornerShape(24.dp)
    val bgColor by animateColorAsState(
        targetValue = if (isActive) activeColor else Color(0x352A2A32),
        label = "pill_bg"
    )

    Row(
        modifier = modifier
            .height(72.dp)
            .scale(scale)
            .testTag(testTag)
            .clip(shape)
            .background(bgColor, shape)
            .border(
                width = 1.dp,
                color = if (isActive) Color(0x44FFFFFF) else Color(0x28FFFFFF),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isActive) Color(0x33FFFFFF) else Color(0x22FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = Color(0xAAFFFFFF),
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}
