package com.example.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import com.example.game.world.OakridgeMapBuilder
import kotlin.math.hypot
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KingBed
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.game.model.BaseStashState
import com.example.game.model.BaseUpgradeSpecs
import com.example.game.model.CraftingMaterial
import com.example.game.model.DeploymentZone
import com.example.game.model.HqMissionCatalog
import com.example.game.model.OperatorProfile
import com.example.game.model.SurvivorCatalog
import com.example.game.model.SurvivorNpcDef
import com.example.game.model.TerrainKind
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BunkerObsidian
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanFloor
import com.example.ui.theme.EmeraldHud
import com.example.ui.theme.OliveBorder
import com.example.ui.theme.PurpleRare

@Composable
fun HelipadDeploymentModal(
    operator: OperatorProfile?,
    hasHelipad: Boolean,
    selectedZone: DeploymentZone,
    onSelectZone: (DeploymentZone) -> Unit,
    onOpenUpgrades: () -> Unit = {},
    onDeploy: (DeploymentZone, Pair<Float, Float>?) -> Unit,
    onClose: () -> Unit
) {
    var pickedZone by remember(selectedZone, hasHelipad) {
        mutableStateOf(if (hasHelipad) selectedZone else DeploymentZone.OAKRIDGE_HIGHWAY)
    }
    var customDrop by remember(hasHelipad) { mutableStateOf<Pair<Float, Float>?>(null) }

    val oakridgePreview = remember { OakridgeMapBuilder.build(runSeed = 42L) }
    val worldMinX = -3200f
    val worldWidth = 6400f
    val worldMinY = -3200f
    val worldHeight = 6400f

    val activePin = if (hasHelipad) customDrop else null
    val nearestLandmark = remember(activePin) {
        val pin = activePin ?: return@remember ""
        val closest = oakridgePreview.buildings.minByOrNull { b ->
            hypot(b.bounds.x + b.bounds.w / 2f - pin.first, b.bounds.y + b.bounds.h / 2f - pin.second)
        } ?: return@remember "DeLaine Outskirts"
        val dist = hypot(closest.bounds.x + closest.bounds.w / 2f - pin.first, closest.bounds.y + closest.bounds.h / 2f - pin.second)
        if (dist < 420f) "Near ${closest.name}" else {
            oakridgePreview.districtZones.firstOrNull { dz ->
                pin.first in dz.bounds.x..(dz.bounds.x + dz.bounds.w) &&
                    pin.second in dz.bounds.y..(dz.bounds.y + dz.bounds.h)
            }?.name ?: "DeLaine Outskirts"
        }
    }

    ModalScrim(onDismiss = onClose) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (hasHelipad) Icons.Default.FlightTakeoff else Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = if (hasHelipad) CyanFloor else AmberSignal
                    )
                    Column {
                        Text(
                            text = if (hasHelipad) "EAST HELIPAD // DROP ANYWHERE" else "SOUTH GATE // FOOT ENTRY",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (hasHelipad) CyanFloor else AmberSignal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (hasHelipad) "Pilot Reyes ready — tap anywhere on DeLaine Metro or pick LZ" else "Buy Helipad Upgrade to air-drop anywhere on DeLaine Metro",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasHelipad) EmeraldHud else AmberBright,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(48.dp).testTag("helipad_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close deployment", tint = Color.White)
                }
            }

            // Interactive DeLaine Metro Tactical Drop Map (6400 x 6400)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0E1210))
                    .border(
                        width = 1.5.dp,
                        color = if (hasHelipad) CyanFloor.copy(alpha = 0.75f) else AmberSignal.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("helipad_tactical_map")
                        .pointerInput(hasHelipad) {
                            detectTapGestures { tap ->
                                if (hasHelipad && size.width > 0 && size.height > 0) {
                                    val wx = (worldMinX + (tap.x / size.width.toFloat()) * worldWidth).coerceIn(-3050f, 3050f)
                                    val wy = (worldMinY + (tap.y / size.height.toFloat()) * worldHeight).coerceIn(-3050f, 3050f)
                                    customDrop = wx to wy
                                }
                            }
                        }
                ) {
                    val sx = size.width / worldWidth
                    val sy = size.height / worldHeight
                    fun toCanvas(wx: Float, wy: Float) = Offset((wx - worldMinX) * sx, (wy - worldMinY) * sy)

                    for (gp in oakridgePreview.groundPatches) {
                        val tl = toCanvas(gp.x, gp.y)
                        drawRect(
                            color = Color(gp.colorHex).copy(alpha = 0.55f),
                            topLeft = tl,
                            size = Size(gp.w * sx, gp.h * sy)
                        )
                    }
                    for (tz in oakridgePreview.terrainZones) {
                        val tl = toCanvas(tz.x, tz.y)
                        val col = when (tz.kind) {
                            TerrainKind.BASIN_WATER -> Color(0xFF0E7490).copy(alpha = 0.85f)
                            TerrainKind.LIMESTONE_BANK -> Color(0xFF4B5563).copy(alpha = 0.75f)
                            TerrainKind.DITCH -> Color(0xFF1F2D27).copy(alpha = 0.75f)
                            TerrainKind.SANDY_RIDGE -> Color(0xFF6B5D45).copy(alpha = 0.55f)
                            TerrainKind.TERRACE_PLAZA -> Color(0xFF4A475A).copy(alpha = 0.75f)
                            TerrainKind.OVERPASS_DECK -> Color(0xFF64748B).copy(alpha = 0.85f)
                        }
                        drawRect(col, topLeft = tl, size = Size(tz.w * sx, tz.h * sy))
                    }
                    for (r in oakridgePreview.roads) {
                        val tl = toCanvas(r.x, r.y)
                        drawRect(
                            color = Color(0xFF334155),
                            topLeft = tl,
                            size = Size(r.w * sx, r.h * sy)
                        )
                    }
                    for (ew in oakridgePreview.elevatedWalkways) {
                        val tl = toCanvas(ew.x, ew.y)
                        drawRect(
                            color = if (!ew.isBoardwalk) Color(0xFF94A3B8) else Color(0xFFB48148),
                            topLeft = tl,
                            size = Size(ew.w * sx, ew.h * sy)
                        )
                    }
                    for (cr in oakridgePreview.curvedRoads) {
                        if (cr.points.size >= 2) {
                            val path = Path()
                            val p0 = toCanvas(cr.points.first().first, cr.points.first().second)
                            path.moveTo(p0.x, p0.y)
                            for (i in 1 until cr.points.size) {
                                val pi = toCanvas(cr.points[i].first, cr.points[i].second)
                                path.lineTo(pi.x, pi.y)
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFF334155),
                                style = Stroke(width = (cr.width * sx).coerceAtLeast(2.5f))
                            )
                        }
                    }
                    for (b in oakridgePreview.buildings) {
                        val tl = toCanvas(b.bounds.x, b.bounds.y)
                        drawRect(
                            color = Color(b.miniColorHex).copy(alpha = 0.9f),
                            topLeft = tl,
                            size = Size((b.bounds.w * sx).coerceAtLeast(2f), (b.bounds.h * sy).coerceAtLeast(2f))
                        )
                    }

                    // Preset LZs
                    for (z in DeploymentZone.entries) {
                        val p = toCanvas(z.spawnX, z.spawnY)
                        val isAllowed = hasHelipad || z == DeploymentZone.OAKRIDGE_HIGHWAY
                        val col = if (isAllowed) Color(z.accentHex) else Color(0xFF64748B).copy(alpha = 0.55f)
                        drawCircle(col.copy(alpha = 0.25f), radius = 10f, center = p)
                        drawCircle(col, radius = 4.5f, center = p)
                    }

                    // Active drop target crosshair
                    val targetX = activePin?.first ?: pickedZone.spawnX
                    val targetY = activePin?.second ?: pickedZone.spawnY
                    val targetPt = toCanvas(targetX, targetY)
                    val pinColor = if (activePin != null) CyanFloor else AmberBright
                    drawLine(pinColor.copy(alpha = 0.55f), Offset(targetPt.x, 0f), Offset(targetPt.x, size.height), strokeWidth = 1.2f)
                    drawLine(pinColor.copy(alpha = 0.55f), Offset(0f, targetPt.y), Offset(size.width, targetPt.y), strokeWidth = 1.2f)
                    drawCircle(pinColor.copy(alpha = 0.28f), radius = 18f, center = targetPt)
                    drawCircle(pinColor, radius = 11f, center = targetPt, style = Stroke(width = 2.4f))
                    drawCircle(pinColor, radius = 4f, center = targetPt)
                }

                // Map corner caption overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(BunkerObsidian.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                        .border(1.dp, OliveBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = if (hasHelipad) Icons.Default.GpsFixed else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (hasHelipad) CyanFloor else AmberSignal,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (hasHelipad) {
                            if (activePin != null) {
                                "DROP PIN (${activePin.first.toInt()}, ${activePin.second.toInt()}) • $nearestLandmark"
                            } else {
                                "TAP MAP TO SET CUSTOM AIR-DROP PIN"
                            }
                        } else {
                            "GATE ENTRY ONLY // UPGRADE HELIPAD FOR CUSTOM DROP"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasHelipad) CyanFloor else AmberBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (hasHelipad && activePin != null) {
                    OutlinedButton(
                        onClick = { customDrop = null },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .heightIn(min = 48.dp)
                            .testTag("helipad_clear_pin_btn"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("USE PRESET LZ", style = MaterialTheme.typography.labelSmall, color = AmberBright)
                    }
                }
            }

            if (!hasHelipad) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2B1C10), RoundedCornerShape(8.dp))
                        .border(1.dp, AmberSignal.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("EAST HELIPAD LOCKED", style = MaterialTheme.typography.labelSmall, color = AmberBright, fontWeight = FontWeight.Bold)
                        Text(
                            "Walk in via Citrus City Overpass Gate, or buy the Helipad Upgrade at HQ to drop anywhere in DeLaine.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE5D4B3)
                        )
                    }
                    Button(
                        onClick = onOpenUpgrades,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("helipad_open_upgrades_btn"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanFloor, contentColor = Color.Black)
                    ) {
                        Text("UPGRADE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BunkerObsidian, RoundedCornerShape(8.dp))
                        .border(1.dp, EmeraldHud.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = EmeraldHud, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Solo K-9 (${operator?.dogBreed ?: "Tactical K-9"}) deploys automatically on every drop.",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldHud
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false).heightIn(max = 210.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(DeploymentZone.entries, key = { it.id }) { zone ->
                    val unlocked = hasHelipad || zone == DeploymentZone.OAKRIDGE_HIGHWAY
                    val isSel = zone == pickedZone && activePin == null
                    val accent = if (unlocked) Color(zone.accentHex) else Color(0xFF64748B)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                when {
                                    isSel -> Color(0xFF162B24)
                                    unlocked -> BunkerSurface
                                    else -> BunkerObsidian
                                },
                                RoundedCornerShape(10.dp)
                            )
                            .border(if (isSel) 2.dp else 1.dp, if (isSel) accent else OliveBorder, RoundedCornerShape(10.dp))
                            .clickable(enabled = unlocked) {
                                customDrop = null
                                pickedZone = zone
                                onSelectZone(zone)
                            }
                            .padding(10.dp)
                            .testTag("helipad_zone_${zone.id}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = zone.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (unlocked) Color.White else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (unlocked) zone.subtitle else "Requires East Helipad Upgrade",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (unlocked) Color(0xFFBFD3C7) else Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = if (unlocked) zone.threatLabel else "HELIPAD REQ",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            modifier = Modifier
                                .background(accent.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                                .border(1.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Button(
                onClick = { onDeploy(pickedZone, activePin) },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("helipad_deploy_${pickedZone.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activePin != null) CyanFloor else AmberSignal,
                    contentColor = Color.Black
                )
            ) {
                Icon(if (hasHelipad) Icons.Default.FlightTakeoff else Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when {
                        !hasHelipad -> "EXIT SOUTH GATE ON FOOT (HIGHWAY)"
                        activePin != null -> "AIR-DROP AT PIN (${activePin.first.toInt()}, ${activePin.second.toInt()})"
                        else -> "FLY TO ${pickedZone.title.substringAfter("// ").trim()}"
                    },
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SurvivorDialogueModal(
    npc: SurvivorNpcDef,
    stash: BaseStashState,
    onAcceptFavor: (String) -> Unit,
    onClaimFavorReward: () -> Unit,
    onClose: () -> Unit
) {
    val favor = remember(npc.id) { SurvivorCatalog.favorForNpc(npc.id) }
    val accent = Color(npc.accentHex)

    val isActiveFavor = favor != null && stash.activeFavorId == favor.id
    val isCompleted = favor != null && favor.id in stash.completedFavorIds
    val canClaim = isActiveFavor && stash.activeFavorProgress >= favor.targetCount

    ModalScrim(onDismiss = onClose) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Canvas(
                        modifier = Modifier
                            .size(50.dp)
                            .background(BunkerObsidian, RoundedCornerShape(10.dp))
                            .border(1.5.dp, accent, RoundedCornerShape(10.dp))
                    ) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        drawCircle(Color(npc.vestHex), radius = 15f, center = Offset(cx, cy + 4f))
                        drawCircle(Color(npc.skinHex), radius = 10f, center = Offset(cx, cy - 8f))
                        drawCircle(accent, radius = 21f, center = Offset(cx, cy), style = Stroke(width = 2f))
                    }
                    Column {
                        Text(npc.name.uppercase(), style = MaterialTheme.typography.headlineMedium, color = accent)
                        Text("${npc.role} • ${npc.stationArea}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBFD3C7))
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(48.dp).testTag("npc_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close conversation", tint = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerObsidian, RoundedCornerShape(10.dp))
                    .border(1.dp, OliveBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                    Text("SURVIVOR RADIO", style = MaterialTheme.typography.labelSmall, color = accent)
                }
                Text("\"${npc.greeting}\"", style = MaterialTheme.typography.bodyLarge, color = Color.White)
            }

            if (favor != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BunkerSurface, RoundedCornerShape(10.dp))
                        .border(1.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SURVIVOR FAVOR // ${favor.title.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberBright
                        )
                        if (isCompleted) {
                            Text("COMPLETED", style = MaterialTheme.typography.labelSmall, color = EmeraldHud)
                        } else if (isActiveFavor) {
                            Text("${stash.activeFavorProgress}/${favor.targetCount}", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                        }
                    }

                    Text("\"${favor.dialogueHook}\"", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD5E3DA))

                    if (isActiveFavor) {
                        LinearProgressIndicator(
                            progress = { (stash.activeFavorProgress.toFloat() / favor.targetCount.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (canClaim) EmeraldHud else CyanFloor,
                            trackColor = BunkerObsidian
                        )
                    }

                    HorizontalDivider(color = OliveBorder.copy(alpha = 0.6f))

                    Text("FAVOR REWARD:", style = MaterialTheme.typography.labelSmall, color = AmberSignal)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CredBadge(amount = favor.rewardCred)
                        for ((mat, qty) in favor.rewardMats) {
                            val mc = Color(mat.colorHex)
                            Text(
                                text = "+$qty ${mat.shortName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = mc,
                                modifier = Modifier
                                    .background(mc.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                    .border(1.dp, mc.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    when {
                        canClaim -> {
                            Button(
                                onClick = onClaimFavorReward,
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("claim_favor_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldHud, contentColor = Color.Black)
                            ) {
                                Text("CLAIM FAVOR REWARD", fontWeight = FontWeight.Bold)
                            }
                        }
                        isCompleted -> {
                            Text("Favor already repaid. Check back later for more camp work.", style = MaterialTheme.typography.labelSmall, color = EmeraldHud)
                        }
                        isActiveFavor -> {
                            Text("FAVOR TRACKING ON HUD — Complete objective and return to ${npc.name}.", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                        }
                        else -> {
                            Button(
                                onClick = { onAcceptFavor(favor.id) },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("accept_favor_${favor.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black)
                            ) {
                                Text(
                                    text = if (stash.activeFavorId.isNotBlank()) "SWITCH TO THIS FAVOR" else "ACCEPT FAVOR",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HqMissionsPane(
    stash: BaseStashState,
    onClaimMissionReward: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMission = HqMissionCatalog.byId(stash.activeMissionId)
    val allDone = activeMission.id in stash.completedMissionIds
    val readyToClaim = !allDone && stash.activeMissionProgress >= activeMission.targetCount

    LazyColumn(
        modifier = modifier.heightIn(max = 370.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerObsidian, RoundedCornerShape(10.dp))
                    .border(1.5.dp, AmberSignal, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = AmberSignal, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (allDone) "ALL HQ MISSIONS COMPLETE" else "ACTIVE HQ MISSION // ${activeMission.code}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberSignal
                        )
                    }
                    if (!allDone) {
                        Text(
                            text = "${stash.activeMissionProgress.coerceAtMost(activeMission.targetCount)}/${activeMission.targetCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (readyToClaim) EmeraldHud else CyanFloor
                        )
                    }
                }

                if (!allDone) {
                    Text(activeMission.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(activeMission.briefing, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD5E3DA))
                    LinearProgressIndicator(
                        progress = { (stash.activeMissionProgress.toFloat() / activeMission.targetCount.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (readyToClaim) EmeraldHud else AmberSignal,
                        trackColor = BunkerSurface
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CredBadge(amount = activeMission.rewardCred)
                        for ((mat, qty) in activeMission.rewardMats) {
                            val mc = Color(mat.colorHex)
                            Text(
                                text = "+$qty ${mat.shortName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = mc,
                                modifier = Modifier
                                    .background(mc.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                    .border(1.dp, mc.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (readyToClaim) {
                        Button(
                            onClick = onClaimMissionReward,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("claim_mission_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldHud, contentColor = Color.Black)
                        ) {
                            Text("CLAIM HQ MISSION REWARD", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        "You have cleared every HQ Command Mission directive for Outpost Zero.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmeraldHud
                    )
                }
            }
        }

        item {
            Text("HQ DIRECTIVE LOG (${stash.completedMissionIds.size}/${HqMissionCatalog.missions.size})", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
        }

        items(HqMissionCatalog.missions, key = { it.id }) { m ->
            val done = m.id in stash.completedMissionIds
            val isCur = m.id == activeMission.id && !allDone
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isCur) Color(0xFF1E3224) else BunkerSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, if (done) EmeraldHud.copy(alpha = 0.6f) else if (isCur) AmberSignal else OliveBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (done) EmeraldHud else if (isCur) AmberSignal else Color(0xFF475569),
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("${m.code} • ${m.title}", style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(m.briefing, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                CredBadge(amount = m.rewardCred, showUnit = false)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BaseUpgradesPane(
    stash: BaseStashState,
    onUpgradeBunk: () -> Unit,
    onUpgradeStorage: () -> Unit,
    onUpgradeHelipad: () -> Unit = {},
    onUpgradeReloadingGear: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bunkMaxed = stash.bunkLevel >= BaseUpgradeSpecs.MAX_LEVEL
    val bunkCredCost = BaseUpgradeSpecs.bunkUpgradeCredCost(stash.bunkLevel)
    val bunkMats = BaseUpgradeSpecs.bunkUpgradeMats(stash.bunkLevel)
    val canAffordBunk = !bunkMaxed && stash.credits >= bunkCredCost && stash.canAffordMats(bunkMats)

    val storageMaxed = stash.storageLevel >= BaseUpgradeSpecs.MAX_LEVEL
    val storageCredCost = BaseUpgradeSpecs.storageUpgradeCredCost(stash.storageLevel)
    val storageMats = BaseUpgradeSpecs.storageUpgradeMats(stash.storageLevel)
    val canAffordStorage = !storageMaxed && stash.credits >= storageCredCost && stash.canAffordMats(storageMats)

    val helipadMaxed = stash.hasHelipad
    val helipadCredCost = BaseUpgradeSpecs.helipadUpgradeCredCost(stash.helipadLevel)
    val helipadMats = BaseUpgradeSpecs.helipadUpgradeMats(stash.helipadLevel)
    val canAffordHelipad = !helipadMaxed && stash.credits >= helipadCredCost && stash.canAffordMats(helipadMats)

    val reloadingMaxed = stash.reloadingGearLevel >= BaseUpgradeSpecs.MAX_RELOADING_GEAR_LEVEL
    val reloadingCredCost = BaseUpgradeSpecs.reloadingUpgradeCredCost(stash.reloadingGearLevel)
    val reloadingMats = BaseUpgradeSpecs.reloadingUpgradeMats(stash.reloadingGearLevel)
    val canAffordReloading = !reloadingMaxed && stash.credits >= reloadingCredCost && stash.canAffordMats(reloadingMats)

    LazyColumn(
        modifier = modifier.heightIn(max = 370.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerSurface, RoundedCornerShape(10.dp))
                    .border(1.5.dp, if (reloadingMaxed) EmeraldHud.copy(alpha = 0.7f) else AmberSignal.copy(alpha = 0.75f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.HomeRepairService, contentDescription = null, tint = if (reloadingMaxed) EmeraldHud else AmberSignal)
                        Column {
                            Text("HQ RELOADING GEAR & AMMO PRESS", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(
                                text = BaseUpgradeSpecs.reloadingGearTitle(stash.reloadingGearLevel),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (stash.reloadingGearLevel > 0) EmeraldHud else AmberBright
                            )
                        }
                    }
                    Text(
                        text = "LV.${stash.reloadingGearLevel}/${BaseUpgradeSpecs.MAX_RELOADING_GEAR_LEVEL}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (reloadingMaxed) EmeraldHud else AmberSignal
                    )
                }

                Text(
                    text = BaseUpgradeSpecs.reloadingGearDescription(stash.reloadingGearLevel),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD5E3DA)
                )

                if (!reloadingMaxed) {
                    Text(
                        text = "Next Tier (Lv.${stash.reloadingGearLevel + 1}): ${BaseUpgradeSpecs.reloadingGearDescription(stash.reloadingGearLevel + 1)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanFloor
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CredBadge(
                            amount = reloadingCredCost,
                            accent = if (stash.credits >= reloadingCredCost) AmberBright else CrimsonAlert
                        )
                        for ((mat, needed) in reloadingMats) {
                            val have = stash.materialCount(mat)
                            val ok = have >= needed
                            val col = if (ok) EmeraldHud else CrimsonAlert
                            Text(
                                text = "${mat.shortName} $have/$needed",
                                style = MaterialTheme.typography.labelSmall,
                                color = col,
                                modifier = Modifier
                                    .background(col.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                    .border(1.dp, col.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Button(
                        onClick = onUpgradeReloadingGear,
                        enabled = canAffordReloading,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("upgrade_reloading_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberSignal,
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF28332C),
                            disabledContentColor = Color(0xFF6B7F73)
                        )
                    ) {
                        Icon(Icons.Default.HomeRepairService, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (canAffordReloading) {
                                if (stash.reloadingGearLevel == 0) "INSTALL RELOADING BENCH (LV.1)"
                                else "UPGRADE RELOADING GEAR TO LV.${stash.reloadingGearLevel + 1}"
                            } else "NEED CRED OR MATS",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text("ACTIVE // PRECISION RELOADING BENCH MAXED — All pistol, shotgun, rifle, sniper & LMG ammo craftable.", style = MaterialTheme.typography.labelSmall, color = PurpleRare)
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerSurface, RoundedCornerShape(10.dp))
                    .border(1.5.dp, if (helipadMaxed) EmeraldHud.copy(alpha = 0.7f) else CyanFloor.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = if (helipadMaxed) EmeraldHud else CyanFloor)
                        Column {
                            Text("EAST HELIPAD & PRECISION DROP", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(
                                text = if (helipadMaxed) {
                                    "ACTIVE // Custom Map Drop-In Unlocked"
                                } else {
                                    "Unbuilt — Currently South Gate Foot Entry Only"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (helipadMaxed) EmeraldHud else AmberBright
                            )
                        }
                    }
                    Text(if (helipadMaxed) "BUILT" else "LV.0", style = MaterialTheme.typography.labelSmall, color = if (helipadMaxed) EmeraldHud else AmberSignal)
                }

                if (!helipadMaxed) {
                    Text(
                        text = "Constructs the movable East Helipad Tarmac in Outpost Zero and unlocks Precision Air-Drop to drop anywhere you tap on the DeLaine Metro map.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD5E3DA)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CredBadge(
                            amount = helipadCredCost,
                            accent = if (stash.credits >= helipadCredCost) AmberBright else CrimsonAlert
                        )
                        for ((mat, needed) in helipadMats) {
                            val have = stash.materialCount(mat)
                            val ok = have >= needed
                            val col = if (ok) EmeraldHud else CrimsonAlert
                            Text(
                                text = "${mat.shortName} $have/$needed",
                                style = MaterialTheme.typography.labelSmall,
                                color = col,
                                modifier = Modifier
                                    .background(col.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                    .border(1.dp, col.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Button(
                        onClick = onUpgradeHelipad,
                        enabled = canAffordHelipad,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("upgrade_helipad_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanFloor,
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF28332C),
                            disabledContentColor = Color(0xFF6B7F73)
                        )
                    ) {
                        Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (canAffordHelipad) "BUILD EAST HELIPAD & UNLOCK MAP DROP" else "NEED CRED OR MATS", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("ACTIVE // EAST HELIPAD BUILT — Air-drop anywhere in DeLaine or relocate pad in Base Builder.", style = MaterialTheme.typography.labelSmall, color = EmeraldHud)
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, AmberSignal.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.KingBed, contentDescription = null, tint = AmberSignal)
                        Column {
                            Text("WALK-IN BARRACKS BUNKS", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(
                                text = "Current Lv.${stash.bunkLevel}/${BaseUpgradeSpecs.MAX_LEVEL} • ${stash.maxRosterSlots} Operator Spot(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldHud
                            )
                        }
                    }
                    Text("LV.${stash.bunkLevel}", style = MaterialTheme.typography.labelSmall, color = AmberBright)
                }

                if (!bunkMaxed) {
                    val nextSlots = BaseUpgradeSpecs.bunkSlotsForLevel(stash.bunkLevel + 1)
                    Text(
                        text = "Next Upgrade (Lv.${stash.bunkLevel + 1}): Expands Walk-in Barracks to $nextSlots Bunks & $nextSlots Operator Spots.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD5E3DA)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CredBadge(
                            amount = bunkCredCost,
                            accent = if (stash.credits >= bunkCredCost) AmberBright else CrimsonAlert
                        )
                        for ((mat, needed) in bunkMats) {
                            val have = stash.materialCount(mat)
                            val ok = have >= needed
                            val col = if (ok) EmeraldHud else CrimsonAlert
                            Text(
                                text = "${mat.shortName} $have/$needed",
                                style = MaterialTheme.typography.labelSmall,
                                color = col,
                                modifier = Modifier
                                    .background(col.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                    .border(1.dp, col.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Button(
                        onClick = onUpgradeBunk,
                        enabled = canAffordBunk,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("upgrade_bunk_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberSignal,
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF28332C),
                            disabledContentColor = Color(0xFF6B7F73)
                        )
                    ) {
                        Icon(Icons.Default.HomeRepairService, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (canAffordBunk) "UPGRADE BARRACKS TO LV.${stash.bunkLevel + 1}" else "NEED CRED OR MATS", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Barracks maxed at 5 Operator Bunks.", style = MaterialTheme.typography.labelSmall, color = PurpleRare)
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CyanFloor.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = CyanFloor)
                        Column {
                            Text("STORAGE DEPOT & PACK RIG", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(
                                text = "Current Lv.${stash.storageLevel}/${BaseUpgradeSpecs.MAX_LEVEL} • Cap ${stash.storageMaterialCap}/mat • ${stash.backpackSlotCap} Bag Slots",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanFloor
                            )
                        }
                    }
                    Text("LV.${stash.storageLevel}", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                }

                if (!storageMaxed) {
                    val nextCap = BaseUpgradeSpecs.storageCapForLevel(stash.storageLevel + 1)
                    val nextBag = BaseUpgradeSpecs.backpackSlotsForLevel(stash.storageLevel + 1)
                    Text(
                        text = "Next Upgrade (Lv.${stash.storageLevel + 1}): $nextCap Material Storage Cap + $nextBag Field Backpack Slots.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD5E3DA)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CredBadge(
                            amount = storageCredCost,
                            accent = if (stash.credits >= storageCredCost) AmberBright else CrimsonAlert
                        )
                        for ((mat, needed) in storageMats) {
                            val have = stash.materialCount(mat)
                            val ok = have >= needed
                            val col = if (ok) EmeraldHud else CrimsonAlert
                            Text(
                                text = "${mat.shortName} $have/$needed",
                                style = MaterialTheme.typography.labelSmall,
                                color = col,
                                modifier = Modifier
                                    .background(col.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                    .border(1.dp, col.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Button(
                        onClick = onUpgradeStorage,
                        enabled = canAffordStorage,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("upgrade_storage_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanFloor,
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF28332C),
                            disabledContentColor = Color(0xFF6B7F73)
                        )
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (canAffordStorage) "UPGRADE STORAGE TO LV.${stash.storageLevel + 1}" else "NEED CRED OR MATS", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Storage Depot maxed at 600 cap / 16 bag slots.", style = MaterialTheme.typography.labelSmall, color = PurpleRare)
                }
            }
        }
    }
}
