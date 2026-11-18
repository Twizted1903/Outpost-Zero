package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.model.CameraViewBounds
import com.example.game.model.GameMapId
import com.example.game.model.GameSettingsState
import com.example.game.model.OutbreakTimelineCatalog
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BunkerObsidian
import com.example.ui.theme.BunkerPanel
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanFloor
import com.example.ui.theme.EmeraldHud
import com.example.ui.theme.OliveBorder
import com.example.ui.theme.PurpleRare
import com.example.ui.theme.pixelBevelFrame
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    credits: Int,
    rosterCount: Int,
    bunkMax: Int,
    downedCount: Int,
    onEnterOutpost: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val currentEra = OutbreakTimelineCatalog.currentEra
    val entryAccent = Color(currentEra.accentHex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BunkerObsidian)
    ) {
        // Pre-bundled Inked Graphic-Novel Noir DeLaine key art background
        Image(
            painter = painterResource(id = R.drawable.img_noir_splash_1794990595891),
            contentDescription = "Inked graphic-novel view of Outpost Zero overlooking quarantined DeLaine Metro at night",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Balanced noir gradient vignette: keeps the middle illustration vivid while grounding top/bottom HUD readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xB8060A0F),
                            Color(0x14060A0F),
                            Color(0x28060A0F),
                            Color(0xE8060A0F)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .widthIn(max = 640.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar: Fixed 2026 Outbreak Badge + Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier
                            .pixelBevelFrame(fillColor = Color(0xD80A121A), borderColor = AmberSignal, borderWidth = 1.5.dp)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(AmberSignal, CircleShape))
                        Text(
                            text = "DELAINE METRO QUARANTINE // NOV 2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberBright
                        )
                    }
                    if (rosterCount > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CredBadge(amount = credits)
                            Text(
                                text = "CREW $rosterCount/$bunkMax" + if (downedCount > 0) " • $downedCount DOWNED" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (downedCount > 0) CrimsonAlert else EmeraldHud
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .pixelBevelFrame(
                            fillColor = BunkerPanel.copy(alpha = 0.90f),
                            borderColor = CyanFloor.copy(alpha = 0.85f),
                            borderWidth = 1.5.dp
                        )
                        .clickable(onClick = onOpenSettings)
                        .padding(horizontal = 13.dp, vertical = 10.dp)
                        .testTag("splash_settings_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Open Settings", tint = CyanFloor, modifier = Modifier.size(18.dp))
                        Text("SETTINGS", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Middle-Bottom Hero Title + Locked Shared 2026 Era Card + Launch CTA
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(3.dp, 26.dp).background(AmberSignal))
                        Text(
                            text = "OUTPOST ZERO",
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Fortified hilltop compound overlooking quarantined DeLaine, Florida.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFCDE1EC)
                    )
                }

                // Fixed Shared World Era Card (Option-3 tactical slate-glass instrumentation card)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pixelBevelFrame(
                            fillColor = BunkerPanel.copy(alpha = 0.90f),
                            borderColor = CyanFloor.copy(alpha = 0.72f),
                            borderWidth = 1.5.dp
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.History, contentDescription = null, tint = CyanFloor, modifier = Modifier.size(16.dp))
                            Text(currentEra.phaseTag, style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                        }
                        Text(
                            text = currentEra.dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = entryAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = OliveBorder.copy(alpha = 0.65f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BunkerObsidian.copy(alpha = 0.78f), RoundedCornerShape(4.dp))
                            .border(1.dp, entryAccent.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(currentEra.title, style = MaterialTheme.typography.titleMedium, color = AmberBright, fontWeight = FontWeight.Bold)
                        Text(currentEra.summary, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD5E3EC))
                    }
                }

                Button(
                    onClick = onEnterOutpost,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.5.dp, Color(0xFF06080C), RoundedCornerShape(4.dp))
                        .testTag("splash_enter_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberSignal, contentColor = Color(0xFF06080C)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (rosterCount > 0) "RETURN TO OUTPOST ZERO" else "ENTER OUTPOST ZERO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsModal(
    settings: GameSettingsState,
    showReturnToSplash: Boolean,
    onUpdateSettings: ((GameSettingsState) -> GameSettingsState) -> Unit,
    onReturnToSplash: () -> Unit,
    onClose: () -> Unit
) {
    ModalScrim(onDismiss = onClose) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = CyanFloor)
                    Column {
                        Text("SYSTEM SETTINGS", style = MaterialTheme.typography.headlineMedium, color = CyanFloor)
                        Text("Audio, HUD clutter & tactical optics preferences", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBFD3C7))
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(48.dp).testTag("settings_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close settings", tint = Color.White)
                }
            }

            HorizontalDivider(color = OliveBorder)

            SettingsToggleCard(
                title = "Mute All Audio & CDs",
                subtitle = "Silences gunfire, sirens, doors, and CD playback.",
                checked = settings.muteAudio,
                accent = CrimsonAlert,
                tag = "setting_mute"
            ) { v -> onUpdateSettings { it.copy(muteAudio = v) } }

            SettingsToggleCard(
                title = "Default Target-Lock Auto-Fire",
                subtitle = "Automatically fire firearms when infected enter aim cone.",
                checked = settings.autoFireDefault,
                accent = EmeraldHud,
                tag = "setting_autofire"
            ) { v -> onUpdateSettings { it.copy(autoFireDefault = v) } }

            SettingsToggleCard(
                title = "Show Tactical Minimap",
                subtitle = "Displays top-right radar with buildings, exits, and hostiles.",
                checked = settings.showMinimap,
                accent = CyanFloor,
                tag = "setting_minimap"
            ) { v -> onUpdateSettings { it.copy(showMinimap = v) } }

            SettingsToggleCard(
                title = "Rotate Minimap With Facing",
                subtitle = "Locks radar to operator aim heading instead of North-Up.",
                checked = settings.rotateMinimap,
                accent = CyanFloor,
                tag = "setting_rot_minimap"
            ) { v -> onUpdateSettings { it.copy(rotateMinimap = v) } }

            SettingsToggleCard(
                title = "Show HQ & Favor Trackers",
                subtitle = "Displays active Mission and Survivor Favor progress below vitals.",
                checked = settings.showTaskTrackers,
                accent = AmberSignal,
                tag = "setting_trackers"
            ) { v -> onUpdateSettings { it.copy(showTaskTrackers = v) } }

            SettingsToggleCard(
                title = "Ambient Wind & Debris",
                subtitle = "Blowing leaves in Outpost woods and newspapers in DeLaine.",
                checked = settings.ambientWind,
                accent = EmeraldHud,
                tag = "setting_wind"
            ) { v -> onUpdateSettings { it.copy(ambientWind = v) } }

            SettingsToggleCard(
                title = "High-Visibility Night Optics",
                subtitle = "Brightens night, dusk, and blackout darkness outside your beam.",
                checked = settings.highVisibilityNight,
                accent = AmberBright,
                tag = "setting_night_vis"
            ) { v -> onUpdateSettings { it.copy(highVisibilityNight = v) } }

            // 2.5D Camera Zoom & Voxel Extrusion Settings (strictly clamped to prevent ground clipping or excessive zoom-out)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerSurface, RoundedCornerShape(6.dp))
                    .border(1.dp, CyanFloor.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val zoomPct = (settings.cameraZoom * 100f).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("2.5D Camera Zoom & Voxel Depth", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "Locked 72%–135% so camera never clips ground or pulls too far away.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9FB0BE)
                        )
                    }
                    Text(
                        "${zoomPct}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanFloor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = CameraViewBounds.clampZoom(settings.cameraZoom),
                    onValueChange = { raw ->
                        val clamped = CameraViewBounds.clampZoom(raw)
                        onUpdateSettings { it.copy(cameraZoom = clamped) }
                    },
                    valueRange = CameraViewBounds.MIN_CAMERA_ZOOM..CameraViewBounds.MAX_CAMERA_ZOOM,
                    modifier = Modifier.fillMaxWidth().testTag("setting_zoom_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = CyanFloor,
                        activeTrackColor = CyanFloor,
                        inactiveTrackColor = OliveBorder
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = kotlin.math.abs(settings.cameraZoom - CameraViewBounds.MIN_CAMERA_ZOOM) < 0.04f,
                        onClick = { onUpdateSettings { it.copy(cameraZoom = CameraViewBounds.MIN_CAMERA_ZOOM) } },
                        label = { Text("Wide 72%", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("setting_zoom_wide")
                    )
                    FilterChip(
                        selected = kotlin.math.abs(settings.cameraZoom - CameraViewBounds.DEFAULT_CAMERA_ZOOM) < 0.04f,
                        onClick = { onUpdateSettings { it.copy(cameraZoom = CameraViewBounds.DEFAULT_CAMERA_ZOOM) } },
                        label = { Text("Classic 100%", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("setting_zoom_default")
                    )
                    FilterChip(
                        selected = kotlin.math.abs(settings.cameraZoom - CameraViewBounds.MAX_CAMERA_ZOOM) < 0.04f,
                        onClick = { onUpdateSettings { it.copy(cameraZoom = CameraViewBounds.MAX_CAMERA_ZOOM) } },
                        label = { Text("Close 135%", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("setting_zoom_close")
                    )
                }

                Text(
                    "VOXEL BUILDING HEIGHT (${(settings.voxelDepthScale * 100f).toInt()}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberBright,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = kotlin.math.abs(settings.voxelDepthScale - CameraViewBounds.MIN_VOXEL_DEPTH) < 0.04f,
                        onClick = { onUpdateSettings { it.copy(voxelDepthScale = CameraViewBounds.MIN_VOXEL_DEPTH) } },
                        label = { Text("Low 85%", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("setting_voxel_low")
                    )
                    FilterChip(
                        selected = kotlin.math.abs(settings.voxelDepthScale - CameraViewBounds.DEFAULT_VOXEL_DEPTH) < 0.04f,
                        onClick = { onUpdateSettings { it.copy(voxelDepthScale = CameraViewBounds.DEFAULT_VOXEL_DEPTH) } },
                        label = { Text("Standard", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("setting_voxel_default")
                    )
                    FilterChip(
                        selected = kotlin.math.abs(settings.voxelDepthScale - CameraViewBounds.MAX_VOXEL_DEPTH) < 0.04f,
                        onClick = { onUpdateSettings { it.copy(voxelDepthScale = CameraViewBounds.MAX_VOXEL_DEPTH) } },
                        label = { Text("Deep 118%", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("setting_voxel_deep")
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerObsidian, RoundedCornerShape(10.dp))
                    .border(1.dp, AmberSignal.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, tint = AmberBright, modifier = Modifier.size(16.dp))
                    Text(
                        text = "SHARED WORLD TIMELINE // NOV 2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberBright,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = OutbreakTimelineCatalog.currentEra.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD5E3DA)
                )
            }

            if (showReturnToSplash) {
                HorizontalDivider(color = OliveBorder)
                Button(
                    onClick = onReturnToSplash,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("settings_return_splash_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A1D1D), contentColor = Color(0xFFFCA5A5))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("RETURN TO MAIN SPLASH SCREEN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    accent: Color,
    tag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BunkerSurface, RoundedCornerShape(4.dp))
            .border(1.dp, if (checked) accent.copy(alpha = 0.72f) else OliveBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9FB0BE))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag),
            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = accent)
        )
    }
}

@Composable
fun OpsHubModal(
    mapId: GameMapId,
    hasHelipad: Boolean = false,
    onOpenModal: (ActiveModal) -> Unit,
    onEnterBaseBuilder: () -> Unit = {},
    onClose: () -> Unit
) {
    ModalScrim(onDismiss = onClose) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("FIELD OPERATIONS HUB", style = MaterialTheme.typography.headlineMedium, color = AmberSignal)
                    Text(
                        text = if (mapId == GameMapId.OUTPOST_ZERO) "Quick access to Outpost Zero stations & tools" else "Tactical DeLaine Metro navigation & binder",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFBFD3C7)
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(48.dp).testTag("ops_hub_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close ops hub", tint = Color.White)
                }
            }

            if (mapId == GameMapId.OUTPOST_ZERO) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OpsTileButton("WORKBENCH", "Craft Gear & Plates", Icons.Default.Build, CyanFloor, "ops_workbench_btn", Modifier.weight(1f)) {
                        onOpenModal(ActiveModal.WORKBENCH)
                    }
                    OpsTileButton(
                        title = if (hasHelipad) "EAST HELIPAD" else "SOUTH GATE",
                        subtitle = if (hasHelipad) "Drop Anywhere on Map" else "Foot Entry / Upgrade",
                        icon = Icons.Default.FlightTakeoff,
                        tint = EmeraldHud,
                        tag = "ops_helipad_btn",
                        modifier = Modifier.weight(1f)
                    ) {
                        onOpenModal(ActiveModal.HELIPAD)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OpsTileButton("HQ TERMINAL", "Crew, Missions & Upgrades", Icons.Default.Computer, AmberSignal, "ops_laptop_btn", Modifier.weight(1f)) {
                        onOpenModal(ActiveModal.LAPTOP)
                    }
                    OpsTileButton("LIVE MARKET", "Player Trade Board", Icons.Default.Storefront, EmeraldHud, "ops_market_btn", Modifier.weight(1f)) {
                        onOpenModal(ActiveModal.MARKET)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OpsTileButton("CD RACK", "Collected Albums", Icons.Default.Album, PurpleRare, "ops_cds_btn", Modifier.weight(1f)) {
                        onOpenModal(ActiveModal.CD_BINDER)
                    }
                    OpsTileButton("SETTINGS", "Options & Splash", Icons.Default.Settings, CyanFloor, "ops_settings_btn", Modifier.weight(1f)) {
                        onOpenModal(ActiveModal.SETTINGS)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OpsTileButton("BASE BUILDER", "Move Buildings, Bunks & Laptop", Icons.Default.Construction, CyanFloor, "ops_builder_btn", Modifier.fillMaxWidth()) {
                        onEnterBaseBuilder()
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OpsTileButton("EXITS & TASKS", "Open Heli Extractions", Icons.Default.Explore, EmeraldHud, "ops_exits_btn", Modifier.weight(1f)) {
                        onOpenModal(ActiveModal.LAPTOP)
                    }
                    OpsTileButton("CD RACK", "Collected Albums", Icons.Default.Album, PurpleRare, "ops_cds_btn", Modifier.weight(1f)) {
                        onOpenModal(ActiveModal.CD_BINDER)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OpsTileButton("SETTINGS", "Options & Splash", Icons.Default.Settings, CyanFloor, "ops_settings_btn", Modifier.fillMaxWidth()) {
                        onOpenModal(ActiveModal.SETTINGS)
                    }
                }
            }
        }
    }
}

@Composable
private fun OpsTileButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = 72.dp)
            .pixelBevelFrame(
                fillColor = BunkerSurface,
                borderColor = tint.copy(alpha = 0.68f),
                borderWidth = 1.5.dp
            )
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(24.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9FB0BE), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun RescueTeamAnimationModal(
    mission: RescueMissionAnimState,
    onComplete: () -> Unit
) {
    var progress by remember(mission.operator.id) { mutableFloatStateOf(0f) }
    var completedDispatched by remember(mission.operator.id) { mutableStateOf(false) }

    LaunchedEffect(mission.operator.id) {
        var lastNs = 0L
        while (isActive && progress < 1f) {
            withFrameNanos { ns ->
                val dt = if (lastNs == 0L) 0.016f else ((ns - lastNs) / 1_000_000_000f).coerceAtMost(0.05f)
                lastNs = ns
                progress = (progress + dt / 5.4f).coerceAtMost(1f)
            }
        }
        if (!completedDispatched) {
            completedDispatched = true
            onComplete()
        }
    }

    val phaseText = when {
        progress < 0.28f -> "CSAR CHOPPER INBOUND TO ${mission.zoneTitle.substringAfter("// ").trim()}"
        progress < 0.62f -> "RESCUE TEAM FAST-ROPING TO (${mission.operator.downedAtX.toInt()}, ${mission.operator.downedAtY.toInt()}) — CLEARING INFECTED"
        progress < 0.88f -> "EXTRACTION SQUAD CARRYING ${mission.operator.callsign} TO HELO"
        else -> "AIRBORNE — RETURNING TO OUTPOST ZERO"
    }

    ModalScrim(onDismiss = null) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = EmeraldHud)
                    Column {
                        Text("CSAR EXTRACTION // ${mission.operator.callsign}", style = MaterialTheme.typography.headlineMedium, color = EmeraldHud)
                        Text("${mission.zoneTitle} • Fee Paid: ${mission.feePaid} CRED", style = MaterialTheme.typography.labelSmall, color = AmberBright)
                    }
                }
                CredBadge(amount = mission.feePaid, showUnit = false)
            }

            // Live Animated Top-Down Tactical Rescue Canvas around exact downed coordinates
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A100D))
                    .border(1.5.dp, EmeraldHud.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            ) {
                val w = size.width
                val h = size.height
                // Downed operator sits near lower-right/center based on normalized map coords
                val normX = ((mission.operator.downedAtX / 1200f).coerceIn(-0.6f, 0.6f)) * w * 0.25f
                val normY = ((mission.operator.downedAtY / 1400f).coerceIn(-0.6f, 0.6f)) * h * 0.22f
                val opPos = Offset(w * 0.64f + normX, h * 0.60f + normY)
                val lzPos = Offset(w * 0.24f, h * 0.34f)
                val entryPos = Offset(-60f, -40f)
                val exitPos = Offset(w + 70f, -50f)

                // Draw DeLaine grid streets + building footprints around crash site
                drawRect(Color(0xFF1A2229), Offset(0f, opPos.y - 26f), Size(w, 52f))
                drawRect(Color(0xFF1A2229), Offset(lzPos.x - 28f, 0f), Size(56f, h))
                drawLine(
                    color = Color(0xFFD99B16).copy(alpha = 0.5f),
                    start = Offset(0f, opPos.y),
                    end = Offset(w, opPos.y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f))
                )
                drawRect(Color(0xFF283029), Offset(w * 0.56f, 18f), Size(w * 0.32f, 54f))
                drawRect(Color(0xFF242B30), Offset(18f, h * 0.65f), Size(w * 0.28f, 52f))

                // Pulsing red SOS beacon where operator fell
                val pulse = 0.5f + 0.5f * sin(progress * 26f)
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = 0.24f + 0.22f * pulse),
                    radius = 22f + pulse * 14f,
                    center = opPos
                )
                drawCircle(
                    color = Color(0xFFFCA5A5).copy(alpha = 0.85f),
                    radius = 16f,
                    center = opPos,
                    style = Stroke(width = 2f)
                )

                // Downed operator position (carried back in phase 3)
                val carrierT = ((progress - 0.62f) / 0.26f).coerceIn(0f, 1f)
                val currentOpPos = if (progress < 0.62f) opPos else Offset(
                    opPos.x + (lzPos.x - opPos.x) * carrierT,
                    opPos.y + (lzPos.y - opPos.y) * carrierT
                )
                if (progress < 0.88f) {
                    drawCircle(Color(mission.operator.vestHex), radius = 8.5f, center = currentOpPos)
                    drawCircle(Color(mission.operator.skinHex), radius = 5f, center = Offset(currentOpPos.x, currentOpPos.y - 3f))
                }

                // 3 Nearby Infected silhouettes around fallen operator (neutralized during phase 2)
                val threatOffsets = listOf(Offset(54f, -34f), Offset(64f, 28f), Offset(-36f, 44f))
                threatOffsets.forEachIndexed { idx, off ->
                    val zPos = opPos + off
                    val deadThreshold = 0.38f + idx * 0.08f
                    if (progress < deadThreshold) {
                        val wiggle = sin(progress * 30f + idx) * 4f
                        drawCircle(Color(0xFF84CC16), radius = 6f, center = Offset(zPos.x + wiggle, zPos.y))
                        drawCircle(Color(0xFFEF4444), radius = 2f, center = Offset(zPos.x + wiggle - 2f, zPos.y - 1f))
                    }
                }

                // 3 CSAR Rescue Team Operators rushing from LZ to opPos & back
                if (progress in 0.24f..0.88f) {
                    val squadProgress = when {
                        progress < 0.56f -> ((progress - 0.24f) / 0.32f).coerceIn(0f, 1f)
                        progress < 0.62f -> 1f
                        else -> (1f - ((progress - 0.62f) / 0.26f)).coerceIn(0f, 1f)
                    }
                    listOf(Offset(-14f, -12f), Offset(0f, 0f), Offset(-14f, 12f)).forEachIndexed { idx, form ->
                        val sx = lzPos.x + (opPos.x - lzPos.x) * squadProgress + form.x
                        val sy = lzPos.y + (opPos.y - lzPos.y) * squadProgress + form.y
                        drawCircle(Color(0xFF34D399), radius = 7.5f, center = Offset(sx, sy))
                        drawCircle(Color(0xFF065F46), radius = 7.5f, center = Offset(sx, sy), style = Stroke(width = 1.8f))
                        // Muzzle flash tracers toward infected in phase 2
                        if (progress in 0.34f..0.56f && ((progress * 40f).toInt() + idx) % 2 == 0) {
                            val tgt = opPos + threatOffsets[idx]
                            drawLine(Color(0xFFFDE047), Offset(sx, sy), tgt, strokeWidth = 2.2f, cap = StrokeCap.Round)
                        }
                    }
                }

                // Medevac Helicopter
                val heloPos = when {
                    progress < 0.28f -> {
                        val t = (progress / 0.28f).coerceIn(0f, 1f)
                        Offset(entryPos.x + (lzPos.x - entryPos.x) * t, entryPos.y + (lzPos.y - entryPos.y) * t)
                    }
                    progress < 0.88f -> lzPos
                    else -> {
                        val t = ((progress - 0.88f) / 0.12f).coerceIn(0f, 1f)
                        Offset(lzPos.x + (exitPos.x - lzPos.x) * t, lzPos.y + (exitPos.y - lzPos.y) * t)
                    }
                }
                // Helo shadow + fuselage + spinning rotor blades
                drawOval(Color.Black.copy(alpha = 0.45f), Offset(heloPos.x - 24f, heloPos.y + 12f), Size(48f, 22f))
                drawRect(Color(0xFF1F2937), Offset(heloPos.x - 22f, heloPos.y - 12f), Size(44f, 24f))
                drawRect(Color(0xFF38BDF8), Offset(heloPos.x + 12f, heloPos.y - 8f), Size(9f, 16f))
                drawCircle(Color(0xFF34D399), radius = 3.5f, center = Offset(heloPos.x - 18f, heloPos.y - 12f))
                rotate(degrees = progress * 360f * 16f, pivot = heloPos) {
                    drawLine(Color(0xFFE2E8F0).copy(alpha = 0.85f), Offset(heloPos.x - 42f, heloPos.y), Offset(heloPos.x + 42f, heloPos.y), strokeWidth = 3f)
                    drawLine(Color(0xFFE2E8F0).copy(alpha = 0.85f), Offset(heloPos.x, heloPos.y - 42f), Offset(heloPos.x, heloPos.y + 42f), strokeWidth = 3f)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(phaseText, style = MaterialTheme.typography.labelSmall, color = CyanFloor, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = EmeraldHud,
                    trackColor = BunkerObsidian
                )
            }

            Text(
                text = "Surviving Pack: ${mission.operator.backpackItems.size} item(s) • Untouchable Safe Pockets: ${mission.operator.safePocketItems.size} item(s)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD5E3DA)
            )

            OutlinedButton(
                onClick = {
                    if (!completedDispatched) {
                        completedDispatched = true
                        onComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("rescue_skip_btn")
            ) {
                Text("COMPLETE EXTRACTION TO BASE", style = MaterialTheme.typography.labelSmall, color = AmberBright, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PermadeathReportModal(
    report: PermadeathReport,
    onDismiss: () -> Unit
) {
    ModalScrim(onDismiss = null) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(28.dp))
                Column {
                    Text("24 HOURS EXPIRED // OPERATOR LOST", style = MaterialTheme.typography.headlineMedium, color = CrimsonAlert)
                    Text("${report.callsign} (${report.roleLabel}) died for good in DeLaine.", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }

            HorizontalDivider(color = OliveBorder)

            // What rival scavengers stole from exposed pack
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF281214), RoundedCornerShape(10.dp))
                    .border(1.dp, CrimsonAlert.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("LOOTED BY RIVAL SCAVENGERS (${report.stolenItemNames.size}):", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFCA5A5))
                if (report.stolenItemNames.isEmpty()) {
                    Text("None of their exposed pack items were taken.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD5E3DA))
                } else {
                    report.stolenItemNames.forEach { name ->
                        Text("• $name (Stolen from exposed backpack)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFECACA))
                    }
                }
            }

            // What came back (remaining exposed bag + untouchable safe pockets)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CyanFloor.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CyanFloor, modifier = Modifier.size(16.dp))
                    Text("RECOVERED TO OUTPOST STASH:", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                }
                if (report.recoveredSafeItems.isEmpty() && report.recoveredBagItems.isEmpty()) {
                    Text("No gear remained on the body.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9FB0A6))
                } else {
                    report.recoveredSafeItems.forEach { item ->
                        Text("• [SAFE POCKET] ${item.name}", style = MaterialTheme.typography.bodySmall, color = CyanFloor)
                    }
                    report.recoveredBagItems.forEach { item ->
                        Text("• [BACKPACK] ${item.name}", style = MaterialTheme.typography.bodySmall, color = EmeraldHud)
                    }
                }
                if (report.recoveredCred > 0 || report.recoveredCds > 0) {
                    Text(
                        text = "Banked: +${report.recoveredCred} CRED • +${report.recoveredCds} CD(s) filed",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberBright
                    )
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("permadeath_ack_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = AmberSignal, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ACKNOWLEDGE RECOVERY", fontWeight = FontWeight.Bold)
            }
        }
    }
}
