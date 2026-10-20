package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Palette
import com.example.model.ControlCenterState
import com.example.model.ControlCenterThemeStyle
import com.example.model.ControlCenterTile
import com.example.model.HandlePosition
import com.example.model.InstalledAppItem
import com.example.model.TileShape
import com.example.model.TileType
import com.example.ui.controlcenter.components.CustomizableControlTile
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosPurple
import com.example.ui.theme.IosRed
import com.example.ui.theme.IosYellow

@Composable
fun DashboardScreen(
    state: ControlCenterState,
    onStateChange: (ControlCenterState) -> Unit,
    hasOverlayPerm: Boolean,
    hasWriteSettingsPerm: Boolean,
    hasDndPerm: Boolean,
    installedApps: List<InstalledAppItem>,
    onToggleService: (Boolean) -> Unit,
    onToggleLauncherIcon: (Boolean) -> Unit,
    onOpenPreview: () -> Unit,
    onGrantOverlay: () -> Unit,
    onGrantWriteSettings: () -> Unit,
    onGrantDnd: () -> Unit,
    onOpenAutostart: () -> Unit,
    onOpenBatteryOpt: () -> Unit,
    onOpenAccessibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var isIconManagerOpen by remember { mutableStateOf(false) }
    var isAddAppOpen by remember { mutableStateOf(false) }
    var isAddToolOpen by remember { mutableStateOf(false) }
    var editingTile by remember { mutableStateOf<ControlCenterTile?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App & Target Device Banner
        TargetDeviceCard()

        // System Plugin Identity Card
        SystemPluginIdentityCard(
            isIconVisible = state.isLauncherIconVisible,
            onToggleLauncherIcon = onToggleLauncherIcon
        )

        // Master Service Control
        MasterServiceCard(
            isServiceRunning = state.isServiceRunning,
            onToggleService = onToggleService,
            onOpenPreview = onOpenPreview
        )

        // Custom Icon Studio Card (Size, Shape, Color, Photo, Reorder)
        IconStudioCard(
            state = state,
            onStateChange = onStateChange,
            onOpenManager = { isIconManagerOpen = true },
            onOpenAddApp = { isAddAppOpen = true }
        )

        // Distinctive Theme Engine & Hardware Features
        DistinctiveThemeAndFeaturesCard(
            state = state,
            onStateChange = onStateChange
        )

        // Bootloop Safety Card
        BootloopSafetyCard()

        // MIUI 14 & Android 12 Permissions Checklist
        PermissionsChecklistCard(
            hasOverlayPerm = hasOverlayPerm,
            hasWriteSettingsPerm = hasWriteSettingsPerm,
            hasDndPerm = hasDndPerm,
            onGrantOverlay = onGrantOverlay,
            onGrantWriteSettings = onGrantWriteSettings,
            onGrantDnd = onGrantDnd,
            onOpenAutostart = onOpenAutostart,
            onOpenBatteryOpt = onOpenBatteryOpt,
            onOpenAccessibility = onOpenAccessibility
        )

        // Gesture Handle Customization
        GestureHandleSettingsCard(
            state = state,
            onStateChange = onStateChange
        )

        // Guide on how to use
        UsageInstructionsCard()

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal Dialogs
    if (isIconManagerOpen) {
        IconManagerDialog(
            tiles = state.customTiles,
            onUpdateTiles = { updatedList ->
                onStateChange(state.copy(customTiles = updatedList))
            },
            onDismiss = { isIconManagerOpen = false },
            onOpenAddApp = { isAddAppOpen = true },
            onOpenAddTool = { isAddToolOpen = true },
            onEditTile = { tile ->
                editingTile = tile
            }
        )
    }

    if (editingTile != null) {
        EditTileDialog(
            tile = editingTile!!,
            onSaveTile = { updatedTile ->
                val newTiles = state.customTiles.map { if (it.id == updatedTile.id) updatedTile else it }
                onStateChange(state.copy(customTiles = newTiles))
                editingTile = null
            },
            onDismiss = { editingTile = null }
        )
    }

    if (isAddAppOpen) {
        AddAppShortcutDialog(
            installedApps = installedApps,
            onSelectApp = { appItem ->
                val newTile = ControlCenterTile(
                    id = "app_${appItem.packageName}_${System.currentTimeMillis()}",
                    title = appItem.appName,
                    type = TileType.APP_SHORTCUT,
                    packageName = appItem.packageName,
                    shape = state.globalIconShape,
                    customSizeDp = state.globalIconSizeDp,
                    customActiveColorHex = 0xFF0A84FF
                )
                onStateChange(state.copy(customTiles = state.customTiles + newTile))
            },
            onDismiss = { isAddAppOpen = false }
        )
    }

    if (isAddToolOpen) {
        AddToolDialog(
            onSelectTool = { toolTile ->
                onStateChange(state.copy(customTiles = state.customTiles + toolTile))
            },
            onDismiss = { isAddToolOpen = false }
        )
    }
}

@Composable
private fun IconStudioCard(
    state: ControlCenterState,
    onStateChange: (ControlCenterState) -> Unit,
    onOpenManager: () -> Unit,
    onOpenAddApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF192033)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF5E5CE6), Color(0xFF0A84FF)))),
        modifier = Modifier.fillMaxWidth().testTag("icon_studio_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DashboardCustomize,
                        contentDescription = "Icon Studio",
                        tint = IosBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "استودیوی شخصی‌سازی آیکون‌ها",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "سایز، شکل، عکس، رنگ دلخواه و تغییر مکان",
                            color = Color(0x99FFFFFF),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. GLOBAL ICON SIZE SLIDER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اندازه سراسری آیکون‌ها:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${state.globalIconSizeDp} dp",
                    color = IosBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = state.globalIconSizeDp.toFloat(),
                onValueChange = { newVal ->
                    onStateChange(state.copy(globalIconSizeDp = newVal.toInt()))
                },
                valueRange = 52f..84f,
                steps = 6,
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = IosBlue),
                modifier = Modifier.fillMaxWidth().testTag("icon_size_slider")
            )

            // Quick size presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetButton(label = "کوچک (56dp)", isSelected = state.globalIconSizeDp == 56, onClick = { onStateChange(state.copy(globalIconSizeDp = 56)) }, modifier = Modifier.weight(1f))
                PresetButton(label = "استاندارد (68dp)", isSelected = state.globalIconSizeDp == 68, onClick = { onStateChange(state.copy(globalIconSizeDp = 68)) }, modifier = Modifier.weight(1f))
                PresetButton(label = "بزرگ (78dp)", isSelected = state.globalIconSizeDp == 78, onClick = { onStateChange(state.copy(globalIconSizeDp = 78)) }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. GLOBAL ICON SHAPE SELECTOR
            Text(
                text = "شکل سراسری آیکون‌ها:",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetButton(
                    label = "اسکوئرکل",
                    isSelected = state.globalIconShape == TileShape.SQUIRCLE,
                    onClick = { onStateChange(state.copy(globalIconShape = TileShape.SQUIRCLE)) },
                    modifier = Modifier.weight(1f)
                )
                PresetButton(
                    label = "دایره کامل",
                    isSelected = state.globalIconShape == TileShape.CIRCLE,
                    onClick = { onStateChange(state.copy(globalIconShape = TileShape.CIRCLE)) },
                    modifier = Modifier.weight(1f)
                )
                PresetButton(
                    label = "مربع گرد",
                    isSelected = state.globalIconShape == TileShape.ROUNDED_SQUARE,
                    onClick = { onStateChange(state.copy(globalIconShape = TileShape.ROUNDED_SQUARE)) },
                    modifier = Modifier.weight(1f)
                )
                PresetButton(
                    label = "کپسولی",
                    isSelected = state.globalIconShape == TileShape.PILL,
                    onClick = { onStateChange(state.copy(globalIconShape = TileShape.PILL)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. LIVE MINI PREVIEW
            Text(
                text = "پیش‌نمایش زنده چیدمان آیکون‌ها:",
                color = Color(0xAAFFFFFF),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF10131E))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.customTiles.take(4).forEach { tile ->
                    CustomizableControlTile(
                        tile = tile,
                        isActive = true,
                        overrideSizeDp = (state.globalIconSizeDp * 0.72f).toInt(),
                        overrideShape = state.globalIconShape,
                        onClick = onOpenManager
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. ACTION BUTTONS (MANAGE & ADD APP)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenManager,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                    modifier = Modifier.weight(1f).height(42.dp).testTag("open_icon_manager_btn")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Manage", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تغییر مکان و رنگ آیکون‌ها", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenAddApp,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.weight(1f).height(42.dp).testTag("add_app_shortcut_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "افزودن برنامه گوشی", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun PresetButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(if (isSelected) IosBlue else Color(0xFF22283A), label = "btn_bg")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, if (isSelected) Color.White else Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xBBFFFFFF),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun DistinctiveThemeAndFeaturesCard(
    state: ControlCenterState,
    onStateChange: (ControlCenterState) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF192233)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFFBF5AF2), Color(0xFF0A84FF)))),
        modifier = Modifier.fillMaxWidth().testTag("distinctive_theme_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Themes",
                    tint = IosPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "موتور سبک‌های گرافیکی و امکانات هوشمند",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "انتخاب تم بصری، پایش سخت‌افزار و داینامیک آیلند",
                        color = Color(0x99FFFFFF),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Theme Styles
            Text(
                text = "سبک گرافیکی کنترل سنتر:",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresetButton(
                        label = "✨ شیشه مایع (iOS 18)",
                        isSelected = state.themeStyle == ControlCenterThemeStyle.IOS_LIQUID_GLASS,
                        onClick = { onStateChange(state.copy(themeStyle = ControlCenterThemeStyle.IOS_LIQUID_GLASS)) },
                        modifier = Modifier.weight(1f)
                    )
                    PresetButton(
                        label = "⚡ سایبرپانک نئون (HUD)",
                        isSelected = state.themeStyle == ControlCenterThemeStyle.CYBERPUNK_HUD,
                        onClick = { onStateChange(state.copy(themeStyle = ControlCenterThemeStyle.CYBERPUNK_HUD)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresetButton(
                        label = "🌑 امولد شب (سیاه مطلق)",
                        isSelected = state.themeStyle == ControlCenterThemeStyle.AMOLED_MIDNIGHT,
                        onClick = { onStateChange(state.copy(themeStyle = ControlCenterThemeStyle.AMOLED_MIDNIGHT)) },
                        modifier = Modifier.weight(1f)
                    )
                    PresetButton(
                        label = "🌌 شفق قطبی (Aurora)",
                        isSelected = state.themeStyle == ControlCenterThemeStyle.FROSTED_AURORA,
                        onClick = { onStateChange(state.copy(themeStyle = ControlCenterThemeStyle.FROSTED_AURORA)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0x22FFFFFF))
            Spacer(modifier = Modifier.height(14.dp))

            // 2. Feature Toggles
            Text(
                text = "ویجت‌ها و امکانات فعال در کنترل سنتر:",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Toggle: Dynamic Island
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "کپسول هوشمند (Dynamic Island)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text = "قرارگیری کپسول وضعیت موزیک و شارژ دور دوربین سلفی", color = Color(0x88FFFFFF), fontSize = 11.sp)
                }
                Switch(
                    checked = state.showDynamicIsland,
                    onCheckedChange = { onStateChange(state.copy(showDynamicIsland = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IosPurple)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle: Hardware Telemetry HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "پایش سخت‌افزار ردمی نوت 9 اس", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text = "نمایش زنده مصرف رم، پردازنده، دما و توربو بوست رم", color = Color(0x88FFFFFF), fontSize = 11.sp)
                }
                Switch(
                    checked = state.showHardwareHud,
                    onCheckedChange = { onStateChange(state.copy(showHardwareHud = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IosBlue)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle: Quick Memo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "یادداشت سریع (Quick Memo)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text = "پد یادداشت سریع برای نوشتن شماره یا متن بدون خروج از برنامه", color = Color(0x88FFFFFF), fontSize = 11.sp)
                }
                Switch(
                    checked = state.showQuickMemo,
                    onCheckedChange = { onStateChange(state.copy(showQuickMemo = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IosYellow)
                )
            }
        }
    }
}

@Composable
private fun TargetDeviceCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0x440A84FF), Color(0x2230D158)))),
        modifier = Modifier.fillMaxWidth().testTag("target_device_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "پلاگین رابط کاربری سیستم (iOS 18)",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "System UI Plugin (com.android.systemui.plugin)",
                        color = Color(0x99FFFFFF),
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(IosBlue.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(text = "MIUI 14.0.4.0", color = IosBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeviceBadge(label = "دستگاه", value = "Redmi Note 9S (Curtana)")
                DeviceBadge(label = "اندروید", value = "Android 12 (S)")
            }
        }
    }
}

@Composable
private fun SystemPluginIdentityCard(
    isIconVisible: Boolean,
    onToggleLauncherIcon: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF192030)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6)))),
        modifier = Modifier.fillMaxWidth().testTag("system_plugin_identity_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = "System Plugin",
                    tint = IosBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "شناسه پکیج سیستمی شیائومی",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "com.android.systemui.plugin (نسخه 14.0.4.0-iOS18)",
                        color = Color(0xAAFFFFFF),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "این فایل با پکیج رسمی 'پلاگین رابط کاربری سیستم' بیلد شده است و هنگام نصب توسط Package Installer شیائومی به عنوان بروزرسانی پلاگین موجود شناخته می‌شود.",
                color = Color(0xCCFFFFFF),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0x22FFFFFF))
            Spacer(modifier = Modifier.height(14.dp))

            // Hide launcher icon toggle for 100% invisible system operation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isIconVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Icon Visibility",
                        tint = if (isIconVisible) Color(0xCCFFFFFF) else IosOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isIconVisible) "آیکون در لیست برنامه‌ها نمایان است" else "آیکون مخفی شد (عملکرد کاملاً سیستمی)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isIconVisible) "برای حذف آیکون از لانچر، این گزینه را خاموش کنید" else "پلاگین بدون ایجاد آیکون اضافه در پس‌زمینه فعال است",
                            color = Color(0x88FFFFFF),
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = isIconVisible,
                    onCheckedChange = onToggleLauncherIcon,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = IosBlue,
                        uncheckedTrackColor = Color(0xFF383844)
                    ),
                    modifier = Modifier.testTag("launcher_icon_switch")
                )
            }
        }
    }
}

@Composable
private fun DeviceBadge(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF222736))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$label: ", color = Color(0x88FFFFFF), fontSize = 11.sp)
            Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MasterServiceCard(
    isServiceRunning: Boolean,
    onToggleService: (Boolean) -> Unit,
    onOpenPreview: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1F2E)),
        modifier = Modifier.fillMaxWidth().testTag("master_service_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isServiceRunning) IosGreen else IosRed)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "سرویس شناور کنترل سنتر",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isServiceRunning) "پلاگین در پس‌زمینه فعال است" else "سرویس غیرفعال است",
                            color = if (isServiceRunning) IosGreen else Color(0x99FFFFFF),
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = isServiceRunning,
                    onCheckedChange = onToggleService,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = IosGreen,
                        uncheckedTrackColor = Color(0xFF383844)
                    ),
                    modifier = Modifier.testTag("service_toggle_switch")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenPreview,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                modifier = Modifier.fillMaxWidth().testTag("open_preview_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Preview",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مشاهده و تست کنترل سنتر آیفون 18",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun BootloopSafetyCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14241B)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF30D158), Color(0xFF0A84FF)))),
        modifier = Modifier.fillMaxWidth().testTag("bootloop_safety_card")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Safe from Bootloop",
                tint = IosGreen,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "تضمین ۱۰۰٪ عدم بروز بوت‌لوپ (Bootloop Safe)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "این پلاگین کاملاً امن است چون به عنوان یک لایه سیستمی استاندارد (System Overlay) عمل می‌کند و هرگز فایل‌های حساس سیستمی یا MiuiSystemUI.apk را در روت دستکاری نمی‌کند. با خیال راحت نصب و استفاده کنید.",
                    color = Color(0xCCFFFFFF),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PermissionsChecklistCard(
    hasOverlayPerm: Boolean,
    hasWriteSettingsPerm: Boolean,
    hasDndPerm: Boolean,
    onGrantOverlay: () -> Unit,
    onGrantWriteSettings: () -> Unit,
    onGrantDnd: () -> Unit,
    onOpenAutostart: () -> Unit,
    onOpenBatteryOpt: () -> Unit,
    onOpenAccessibility: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1F2E)),
        modifier = Modifier.fillMaxWidth().testTag("permissions_checklist_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "مجوزها و تنظیمات MIUI 14",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "برای اجرای بدون نقص کنترل سنتر، مجوزهای زیر را فعال کنید:",
                color = Color(0x99FFFFFF),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Overlay
            PermissionItemRow(
                title = "نمایش روی سایر برنامه‌ها (Overlay)",
                description = "اجازه باز شدن کنترل سنتر روی تمام برنامه‌ها و بازی‌ها",
                isGranted = hasOverlayPerm,
                onClick = onGrantOverlay
            )

            HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 10.dp))

            // 2. Write Settings
            PermissionItemRow(
                title = "تغییر تنظیمات سیستم (نور صفحه)",
                description = "امکان تنظیم نور صفحه و چرخش خودکار از طریق اسلایدرها",
                isGranted = hasWriteSettingsPerm,
                onClick = onGrantWriteSettings
            )

            HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 10.dp))

            // 3. DND
            PermissionItemRow(
                title = "دسترسی مزاحم نشوید (Focus / DND)",
                description = "امکان فعال/غیرفعال کردن حالت سکوت و تمرکز",
                isGranted = hasDndPerm,
                onClick = onGrantDnd
            )

            HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 10.dp))

            // 4. MIUI Autostart
            PermissionItemRow(
                title = "راه‌اندازی خودکار شیائومی (Autostart)",
                description = "جلوگیری از بسته شدن سرویس توسط مدیریت رم شیائومی",
                isGranted = null,
                actionLabel = "باز کردن تنظیمات",
                onClick = onOpenAutostart
            )

            HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 10.dp))

            // 5. Battery Saver
            PermissionItemRow(
                title = "بهینه‌سازی باتری MIUI (بدون محدودیت)",
                description = "تنظیم حالت مصرف باتری روی 'No restrictions'",
                isGranted = null,
                actionLabel = "تنظیم باتری",
                onClick = onOpenBatteryOpt
            )

            HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 10.dp))

            // 6. Accessibility
            PermissionItemRow(
                title = "سرویس دسترس‌پذیری (اختیاری)",
                description = "جهت بستن اعلان‌ها یا رفتن به صفحه خانه با حرکات لمسی",
                isGranted = null,
                actionLabel = "تنظیمات کمکی",
                onClick = onOpenAccessibility
            )
        }
    }
}

@Composable
private fun PermissionItemRow(
    title: String,
    description: String,
    isGranted: Boolean?,
    actionLabel: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = Color(0x88FFFFFF),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        if (isGranted == true) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(IosGreen.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Granted",
                        tint = IosGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "فعال", color = IosGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = IosBlue),
                modifier = Modifier.height(34.dp)
            ) {
                Text(text = actionLabel ?: "فعال‌سازی", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun GestureHandleSettingsCard(
    state: ControlCenterState,
    onStateChange: (ControlCenterState) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1F2E)),
        modifier = Modifier.fillMaxWidth().testTag("gesture_settings_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "تنظیمات دستگیره لمسی گوشه صفحه",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "برای باز کردن کنترل سنتر مثل آیفون، از گوشه بالا سمت راست به پایین بکشید.",
                color = Color(0x99FFFFFF),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Position selector
            Text(
                text = "موقعیت باز شدن لمسی:",
                color = Color(0xCCFFFFFF),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PositionOptionButton(
                    label = "گوشه بالا راست (آیفون)",
                    isSelected = state.handlePosition == HandlePosition.TOP_RIGHT,
                    onClick = { onStateChange(state.copy(handlePosition = HandlePosition.TOP_RIGHT)) },
                    modifier = Modifier.weight(1f)
                )

                PositionOptionButton(
                    label = "لبه راست صفحه",
                    isSelected = state.handlePosition == HandlePosition.RIGHT_EDGE,
                    onClick = { onStateChange(state.copy(handlePosition = HandlePosition.RIGHT_EDGE)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Haptic Feedback Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Haptic",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ویبره و بازخورد لمسی (Haptic)", color = Color.White, fontSize = 13.sp)
                }

                Switch(
                    checked = state.hapticFeedback,
                    onCheckedChange = { onStateChange(state.copy(hapticFeedback = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IosBlue)
                )
            }
        }
    }
}

@Composable
private fun PositionOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(if (isSelected) IosBlue else Color(0xFF282C3C), label = "bg")
    val textColor = if (isSelected) Color.White else Color(0xBBFFFFFF)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun UsageInstructionsCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141722)),
        modifier = Modifier.fillMaxWidth().testTag("usage_instructions_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "نحوه کارکرد و بروزرسانی سیستمی",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "۱. شناسه این فایل APK دقیقاً com.android.systemui.plugin می‌باشد که پکیج رسمی پلاگین کنترل سنتر MIUI 14 است.\n" +
                        "۲. نصاب شیائومی فایل را به عنوان آپدیت پلاگین پیش‌فرض شناسایی می‌کند.\n" +
                        "۳. در صورت تمایل به پنهان شدن کامل از لیست برنامه‌ها، گزینه 'مخفی کردن آیکون' در بالا را خاموش کنید تا سیستم بدون آیکون لانچر کار کند.\n" +
                        "۴. کنترل سنتر شیشه‌ای iOS 18 همیشه با کشیدن دست از گوشه بالای سمت راست به پایین باز خواهد شد.",
                color = Color(0xBBFFFFFF),
                fontSize = 12.sp,
                lineHeight = 20.sp
            )
        }
    }
}
