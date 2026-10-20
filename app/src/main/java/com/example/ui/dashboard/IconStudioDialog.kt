package com.example.ui.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.ControlCenterTile
import com.example.model.InstalledAppItem
import com.example.model.TileShape
import com.example.model.TileType
import com.example.model.defaultControlCenterTiles
import com.example.ui.controlcenter.components.CustomizableControlTile
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosPurple
import com.example.ui.theme.IosRed
import com.example.ui.theme.IosYellow

val PALETTE_COLORS = listOf(
    0xFF0A84FF, // Electric Blue
    0xFF30D158, // Emerald Green
    0xFFFF9F0A, // Sunset Orange
    0xFFFF453A, // Ruby Red
    0xFFBF5AF2, // Cyber Purple
    0xFFFFD60A, // Golden Yellow
    0xFF5E5CE6, // Deep Indigo
    0xFF64D2FF, // Cyan Mint
    0xFFFF375F, // Rose Pink
    0xFFFFFFFF, // Pure White
    0xFF2A2A34  // Dark Obsidian
)

@Composable
fun IconManagerDialog(
    tiles: List<ControlCenterTile>,
    onUpdateTiles: (List<ControlCenterTile>) -> Unit,
    onDismiss: () -> Unit,
    onOpenAddApp: () -> Unit,
    onOpenAddTool: () -> Unit,
    onEditTile: (ControlCenterTile) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .testTag("icon_manager_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF181B26)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "مدیریت و چینش آیکون‌ها",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تغییر مکان، ویرایش ظاهر و حذف آیکون‌ها",
                            color = Color(0x99FFFFFF),
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons to add
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenAddApp,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Apps, contentDescription = "Apps", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "+ برنامه گوشی", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onOpenAddTool,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tool", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "+ ابزار سیستمی", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tiles List with Reorder Controls
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(tiles) { index, tile ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF222638)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail Preview
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    CustomizableControlTile(
                                        tile = tile,
                                        isActive = false,
                                        overrideSizeDp = 44,
                                        onClick = { onEditTile(tile) }
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = tile.title,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (tile.type == TileType.APP_SHORTCUT) "برنامه گوشی" else "ابزار کنترل سنتر",
                                            color = Color(0x88FFFFFF),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // Actions (Up, Down, Edit, Delete)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Move Up
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val mutable = tiles.toMutableList()
                                                val item = mutable.removeAt(index)
                                                mutable.add(index - 1, item)
                                                onUpdateTiles(mutable)
                                            }
                                        },
                                        enabled = index > 0,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Move Up",
                                            tint = if (index > 0) Color.White else Color(0x44FFFFFF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Move Down
                                    IconButton(
                                        onClick = {
                                            if (index < tiles.size - 1) {
                                                val mutable = tiles.toMutableList()
                                                val item = mutable.removeAt(index)
                                                mutable.add(index + 1, item)
                                                onUpdateTiles(mutable)
                                            }
                                        },
                                        enabled = index < tiles.size - 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Move Down",
                                            tint = if (index < tiles.size - 1) Color.White else Color(0x44FFFFFF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Edit
                                    IconButton(
                                        onClick = { onEditTile(tile) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Tile",
                                            tint = IosBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete
                                    IconButton(
                                        onClick = {
                                            val mutable = tiles.toMutableList()
                                            mutable.removeAt(index)
                                            onUpdateTiles(mutable)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Tile",
                                            tint = IosRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reset to Default button
                OutlinedButton(
                    onClick = { onUpdateTiles(defaultControlCenterTiles()) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xCCFFFFFF)),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Reset", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "بازنشانی چیدمان به حالت اولیه آیفون 18", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun EditTileDialog(
    tile: ControlCenterTile,
    onSaveTile: (ControlCenterTile) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(tile.title) }
    var selectedColor by remember { mutableStateOf(tile.customActiveColorHex) }
    var selectedShape by remember { mutableStateOf(tile.shape) }
    var customPhotoUri by remember { mutableStateOf(tile.customPhotoUri) }

    // Android Photo Picker Launcher (Zero Permission)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customPhotoUri = uri.toString()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("edit_tile_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF181B26)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "شخصی‌سازی آیکون",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Preview of the Tile
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F121C)),
                    contentAlignment = Alignment.Center
                ) {
                    CustomizableControlTile(
                        tile = tile.copy(
                            title = title,
                            customActiveColorHex = selectedColor,
                            shape = selectedShape,
                            customPhotoUri = customPhotoUri
                        ),
                        isActive = true,
                        overrideSizeDp = 76,
                        onClick = {}
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rename title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نام آیکون", color = Color(0x99FFFFFF), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = IosBlue,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Photo on Icon
                Text(
                    text = "عکس دلخواه روی آیکون:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IosPurple),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "Pick Photo", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "انتخاب از گالری", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (customPhotoUri != null) {
                        OutlinedButton(
                            onClick = { customPhotoUri = null },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IosRed),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(text = "حذف عکس", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Shape Picker
                Text(
                    text = "شکل اختصاصی این آیکون:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShapeSelectButton(
                        label = "اسکوئرکل",
                        isSelected = selectedShape == TileShape.SQUIRCLE,
                        onClick = { selectedShape = TileShape.SQUIRCLE },
                        modifier = Modifier.weight(1f)
                    )
                    ShapeSelectButton(
                        label = "دایره",
                        isSelected = selectedShape == TileShape.CIRCLE,
                        onClick = { selectedShape = TileShape.CIRCLE },
                        modifier = Modifier.weight(1f)
                    )
                    ShapeSelectButton(
                        label = "مربع گرد",
                        isSelected = selectedShape == TileShape.ROUNDED_SQUARE,
                        onClick = { selectedShape = TileShape.ROUNDED_SQUARE },
                        modifier = Modifier.weight(1f)
                    )
                    ShapeSelectButton(
                        label = "کپسولی",
                        isSelected = selectedShape == TileShape.PILL,
                        onClick = { selectedShape = TileShape.PILL },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color Palette
                Text(
                    text = "رنگ دلخواه آیکون:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PALETTE_COLORS.take(7).forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (selectedColor == colorHex) 3.dp else 1.dp,
                                    color = if (selectedColor == colorHex) Color.White else Color(0x33FFFFFF),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Save button
                Button(
                    onClick = {
                        onSaveTile(
                            tile.copy(
                                title = title,
                                customActiveColorHex = selectedColor,
                                shape = selectedShape,
                                customPhotoUri = customPhotoUri
                            )
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(text = "ذخیره تغییرات آیکون", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun AddAppShortcutDialog(
    installedApps: List<InstalledAppItem>,
    onSelectApp: (InstalledAppItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) installedApps else {
            installedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("add_app_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF181B26)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "افزودن برنامه به کنترل سنتر",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "برنامه‌ای را برای دسترسی سریع انتخاب کنید",
                            color = Color(0x99FFFFFF),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("جستجوی برنامه...", color = Color(0x66FFFFFF), fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = IosBlue,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Apps List
                val context = LocalContext.current
                val pm = context.packageManager

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps) { appItem ->
                        val icon = remember(appItem.packageName) {
                            try { pm.getApplicationIcon(appItem.packageName) } catch (_: Exception) { null }
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF222638)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectApp(appItem)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (icon != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(icon).crossfade(true).build(),
                                        contentDescription = appItem.appName,
                                        modifier = Modifier.size(40.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = appItem.appName,
                                        tint = IosBlue,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = appItem.appName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = appItem.packageName,
                                        color = Color(0x77FFFFFF),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddToolDialog(
    onSelectTool: (ControlCenterTile) -> Unit,
    onDismiss: () -> Unit
) {
    val systemTools = remember {
        listOf(
            ControlCenterTile("torch_${System.currentTimeMillis()}", "چراغ قوه", TileType.SYSTEM_ACTION, "FLASHLIGHT", customActiveColorHex = 0xFFFFD60A),
            ControlCenterTile("timer_${System.currentTimeMillis()}", "تایمر", TileType.SYSTEM_ACTION, "TIMER", customActiveColorHex = 0xFFFF9F0A),
            ControlCenterTile("calc_${System.currentTimeMillis()}", "ماشین حساب", TileType.SYSTEM_ACTION, "CALCULATOR", customActiveColorHex = 0xFF30D158),
            ControlCenterTile("camera_${System.currentTimeMillis()}", "دوربین", TileType.SYSTEM_ACTION, "CAMERA", customActiveColorHex = 0xFF0A84FF),
            ControlCenterTile("dark_mode_${System.currentTimeMillis()}", "حالت شب", TileType.SYSTEM_ACTION, "DARK_MODE", customActiveColorHex = 0xFF5E5CE6),
            ControlCenterTile("low_power_${System.currentTimeMillis()}", "مصرف بهینه", TileType.SYSTEM_ACTION, "LOW_POWER", customActiveColorHex = 0xFFFFD60A),
            ControlCenterTile("qr_scanner_${System.currentTimeMillis()}", "اسکنر QR", TileType.SYSTEM_ACTION, "QR_SCANNER", customActiveColorHex = 0xFF64D2FF),
            ControlCenterTile("screen_timeout_${System.currentTimeMillis()}", "زمان صفحه", TileType.SYSTEM_ACTION, "SCREEN_TIMEOUT", customActiveColorHex = 0xFF30D158),
            ControlCenterTile("settings_${System.currentTimeMillis()}", "تنظیمات", TileType.SYSTEM_ACTION, "SETTINGS", customActiveColorHex = 0xFF8E8E93)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .testTag("add_tool_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF181B26)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "افزودن ابزار سیستمی",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(systemTools) { tool ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF222638)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectTool(tool)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CustomizableControlTile(
                                    tile = tool,
                                    isActive = false,
                                    overrideSizeDp = 44,
                                    onClick = {}
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tool.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapeSelectButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) IosBlue else Color(0xFF24283A))
            .border(1.dp, if (isSelected) Color.White else Color(0x22FFFFFF), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
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
