package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.game.model.BaseModuleKind
import com.example.game.model.BaseStashState
import com.example.game.model.BaseUpgradeSpecs
import com.example.game.model.PlacedBasePiece
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

@Composable
fun BaseBuilderHudBar(
    selectedPiece: PlacedBasePiece?,
    pendingKind: BaseModuleKind?,
    pendingRot: Int,
    cameraZoom: Float = 1.0f,
    modifier: Modifier = Modifier,
    onOpenCatalog: () -> Unit,
    onRotate: () -> Unit,
    onDelete: () -> Unit,
    onAdjustZoom: (Float) -> Unit = {},
    onDone: () -> Unit
) {
    val statusText = when {
        pendingKind != null -> "TAP MAP TO PLACE: ${pendingKind.label.uppercase()} (${pendingRot}°)"
        selectedPiece != null -> {
            val lbl = if (selectedPiece.kind == BaseModuleKind.BUNK) {
                "BOUGHT BUNK #${selectedPiece.slotNumber}"
            } else {
                selectedPiece.kind.label.uppercase()
            }
            "MOVING: $lbl (${selectedPiece.rotationDeg}°) — TAP DESTINATION"
        }
        else -> "BUILD MODE // TAP ANY BUILDING, BUNK OR LAPTOP TO MOVE"
    }

    val canDelete = selectedPiece != null &&
        selectedPiece.kind != BaseModuleKind.LAPTOP &&
        selectedPiece.kind != BaseModuleKind.HELIPAD &&
        selectedPiece.kind != BaseModuleKind.BUNK

    Surface(
        modifier = modifier
            .widthIn(max = 600.dp)
            .border(1.5.dp, CyanFloor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = BunkerPanel.copy(alpha = 0.94f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Construction, contentDescription = null, tint = CyanFloor, modifier = Modifier.size(16.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberBright,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenCatalog,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("builder_catalog_btn"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanFloor, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Open Build Catalog", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("CATALOG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRotate,
                    enabled = selectedPiece != null || pendingKind != null,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("builder_rotate_btn"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate 90 degrees", modifier = Modifier.size(18.dp), tint = AmberSignal)
                    Spacer(Modifier.width(4.dp))
                    Text("ROT 90°", style = MaterialTheme.typography.labelSmall, color = AmberBright)
                }

                OutlinedButton(
                    onClick = onDelete,
                    enabled = canDelete,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("builder_delete_btn"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove selected piece",
                        modifier = Modifier.size(18.dp),
                        tint = if (canDelete) CrimsonAlert else Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("REMOVE", style = MaterialTheme.typography.labelSmall, color = if (canDelete) CrimsonAlert else Color.Gray)
                }

                OutlinedButton(
                    onClick = { onAdjustZoom(-0.10f) },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("builder_zoom_out_btn"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text("ZOOM - (${(cameraZoom * 100f).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                }

                OutlinedButton(
                    onClick = { onAdjustZoom(0.10f) },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("builder_zoom_in_btn"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text("ZOOM +", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                }

                Button(
                    onClick = onDone,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("builder_done_btn"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldHud, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Finish Building", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("DONE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BaseBuilderCatalogModal(
    stash: BaseStashState,
    onMoveExistingPiece: (String) -> Unit,
    onSelectKindToPlaceOrMove: (BaseModuleKind) -> Unit,
    onUpgradeBunk: () -> Unit,
    onUpgradeHelipad: () -> Unit = {},
    onClearToScratch: () -> Unit,
    onResetDefault: () -> Unit,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val ensuredLayout = remember(stash.baseLayout, stash.bunkLevel, stash.helipadLevel) {
        stash.baseLayout.ensureInfrastructure(stash.bunkLevel, stash.helipadLevel)
    }
    val placedBunks = remember(ensuredLayout) {
        ensuredLayout.byKind(BaseModuleKind.BUNK).sortedBy { it.slotNumber }
    }

    val stationKinds = remember {
        listOf(
            BaseModuleKind.LAPTOP,
            BaseModuleKind.WORKBENCH,
            BaseModuleKind.STORAGE_DEPOT,
            BaseModuleKind.MARKET,
            BaseModuleKind.CD_RACK,
            BaseModuleKind.MESS_FOOD,
            BaseModuleKind.WATER_COOLER,
            BaseModuleKind.INFIRMARY_BED
        )
    }
    val buildingKinds = remember {
        BaseModuleKind.entries.filter { it.category == "BUILDING" }
    }
    val buildPropKinds = remember {
        BaseModuleKind.entries.filter { it.category == "BUILD" }
    }

    ModalScrim(onDismiss = onClose) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("OUTPOST ARCHITECT // BUILD BASE", style = MaterialTheme.typography.headlineMedium, color = CyanFloor)
                    Text("Relocate existing stations, barracks, walls, or pour new structures anywhere inside the fence.", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        CredBadge(amount = stash.credits)
                        Text(
                            text = "SCRAP: ${stash.scrapMetal}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberBright
                        )
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(48.dp).testTag("builder_catalog_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close build catalog", tint = Color.White)
                }
            }

            // Whole-compound reset / scratch controls
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onClearToScratch,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("builder_scratch_btn"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = AmberSignal, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("BUILD FROM SCRATCH", style = MaterialTheme.typography.labelSmall, color = AmberBright, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = onResetDefault,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("builder_reset_btn"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = CyanFloor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("DEFAULT LAYOUT", style = MaterialTheme.typography.labelSmall, color = CyanFloor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("STATIONS & BUNKS", "BUILDINGS", "WALLS & PROPS").forEachIndexed { idx, title ->
                    FilterChip(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        label = { Text(title, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.weight(1f).testTag("builder_tab_$idx")
                    )
                }
            }

            HorizontalDivider(color = OliveBorder)

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 390.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            Text(
                                text = "BOUGHT BARRACKS BUNKS (${placedBunks.size}/${BaseUpgradeSpecs.MAX_LEVEL})",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldHud,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(placedBunks, key = { it.id }) { bunk ->
                            BuilderItemRow(
                                title = "Bought Bunk #${bunk.slotNumber}",
                                subtitle = "Placed at (${bunk.x.toInt()}, ${bunk.y.toInt()}) • Rot ${bunk.rotationDeg}°",
                                actionLabel = "MOVE",
                                accent = EmeraldHud,
                                canAfford = true,
                                tag = "builder_move_${bunk.id}",
                                icon = Icons.Default.Bed,
                                onClick = { onMoveExistingPiece(bunk.id) }
                            )
                        }
                        if (stash.bunkLevel < BaseUpgradeSpecs.MAX_LEVEL) {
                            item {
                                val nextCred = BaseUpgradeSpecs.bunkUpgradeCredCost(stash.bunkLevel)
                                val nextMats = BaseUpgradeSpecs.bunkUpgradeMats(stash.bunkLevel)
                                val canBuy = stash.credits >= nextCred && stash.canAffordMats(nextMats)
                                val matStr = nextMats.entries.joinToString(" • ") { "${it.value} ${it.key.shortName}" }
                                BuilderItemRow(
                                    title = "Buy New Bunk #${stash.bunkLevel + 1} (+1 Operator Spot)",
                                    subtitle = "$nextCred CRED • $matStr",
                                    actionLabel = "BUY BUNK",
                                    accent = AmberSignal,
                                    canAfford = canBuy,
                                    tag = "builder_buy_bunk_btn",
                                    icon = Icons.Default.Add,
                                    onClick = onUpgradeBunk
                                )
                            }
                        }
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "HQ LAPTOP & BASE STATIONS",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanFloor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(stationKinds, key = { it.name }) { kind ->
                            val existing = ensuredLayout.firstByKind(kind)
                            val cred = kind.credCost
                            val scrap = kind.scrapCost
                            val canAfford = existing != null || (stash.credits >= cred && stash.scrapMetal >= scrap)
                            BuilderItemRow(
                                title = kind.label,
                                subtitle = if (existing != null) {
                                    "Placed at (${existing.x.toInt()}, ${existing.y.toInt()}) • Tap to relocate"
                                } else {
                                    "Build cost: $cred CRED • $scrap SCRAP"
                                },
                                actionLabel = if (existing != null) "MOVE" else "PLACE",
                                accent = if (existing != null) CyanFloor else AmberSignal,
                                canAfford = canAfford,
                                tag = "builder_kind_${kind.name.lowercase()}",
                                icon = Icons.Default.OpenWith,
                                onClick = { onSelectKindToPlaceOrMove(kind) }
                            )
                        }
                    }

                    1 -> {
                        items(buildingKinds, key = { it.name }) { kind ->
                            val existing = ensuredLayout.firstByKind(kind)
                            if (kind == BaseModuleKind.HELIPAD && !stash.hasHelipad) {
                                val upgradeCred = BaseUpgradeSpecs.helipadUpgradeCredCost(stash.helipadLevel)
                                val upgradeMats = BaseUpgradeSpecs.helipadUpgradeMats(stash.helipadLevel)
                                val canBuy = stash.credits >= upgradeCred && stash.canAffordMats(upgradeMats)
                                val matStr = upgradeMats.entries.joinToString(" • ") { "${it.value} ${it.key.shortName}" }
                                BuilderItemRow(
                                    title = "${kind.label} (Upgrade Req)",
                                    subtitle = "Unlocks drop anywhere • $upgradeCred CRED • $matStr",
                                    actionLabel = "UPGRADE",
                                    accent = CyanFloor,
                                    canAfford = canBuy,
                                    tag = "builder_upgrade_helipad_btn",
                                    icon = Icons.Default.Apartment,
                                    onClick = onUpgradeHelipad
                                )
                            } else {
                                val cred = kind.credCost
                                val scrap = kind.scrapCost
                                val canAfford = existing != null || (stash.credits >= cred && stash.scrapMetal >= scrap)
                                BuilderItemRow(
                                    title = kind.label,
                                    subtitle = if (existing != null) {
                                        "${kind.width.toInt()}×${kind.height.toInt()} • At (${existing.x.toInt()}, ${existing.y.toInt()})"
                                    } else {
                                        "${kind.width.toInt()}×${kind.height.toInt()} • $cred CRED • $scrap SCRAP"
                                    },
                                    actionLabel = if (existing != null) "MOVE" else "BUILD",
                                    accent = if (existing != null) CyanFloor else AmberSignal,
                                    canAfford = canAfford,
                                    tag = "builder_kind_${kind.name.lowercase()}",
                                    icon = Icons.Default.Apartment,
                                    onClick = { onSelectKindToPlaceOrMove(kind) }
                                )
                            }
                        }
                    }

                    else -> {
                        items(buildPropKinds, key = { it.name }) { kind ->
                            val cred = kind.credCost
                            val scrap = kind.scrapCost
                            val canAfford = stash.credits >= cred && stash.scrapMetal >= scrap
                            val count = ensuredLayout.byKind(kind).size
                            BuilderItemRow(
                                title = "${kind.label}${if (count > 0) " ($count)" else ""}",
                                subtitle = "Place anywhere • $cred CRED • $scrap SCRAP",
                                actionLabel = "BUILD",
                                accent = PurpleRare,
                                canAfford = canAfford,
                                tag = "builder_kind_${kind.name.lowercase()}",
                                icon = Icons.Default.Construction,
                                onClick = { onSelectKindToPlaceOrMove(kind) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuilderItemRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    accent: Color,
    canAfford: Boolean,
    tag: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = BunkerSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = if (canAfford) Color(0xFFB6C7BD) else CrimsonAlert, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Button(
                onClick = onClick,
                enabled = canAfford,
                modifier = Modifier.heightIn(min = 48.dp).testTag(tag),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}
