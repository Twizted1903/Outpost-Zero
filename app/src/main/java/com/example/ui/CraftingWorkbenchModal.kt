package com.example.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.game.model.BaseStashState
import com.example.game.model.BaseUpgradeSpecs
import com.example.game.model.CraftOutputKind
import com.example.game.model.CraftingCatalog
import com.example.game.model.CraftingMaterial
import com.example.game.model.CraftingMaterialCategory
import com.example.game.model.CraftingRecipe
import com.example.game.model.CraftingTier
import com.example.game.model.LootType
import com.example.game.model.OperatorProfile
import com.example.game.model.WeaponId
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BunkerObsidian
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanFloor
import com.example.ui.theme.EmeraldHud
import com.example.ui.theme.OliveBorder

private enum class CraftCategoryFilter(val label: String) {
    ALL("ALL"),
    MELEE("MELEE"),
    GUNS("GUNS"),
    MODS("MODS"),
    ARMOR("ARMOR"),
    AMMO("AMMO"),
    GEAR("GEAR")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CraftingWorkbenchModal(
    stash: BaseStashState,
    operator: OperatorProfile?,
    playingCdId: Int?,
    onPlayCd: (Int) -> Unit,
    onDepositMaterials: () -> Unit,
    onCraft: (CraftingRecipe) -> Unit,
    onOpenUpgrades: () -> Unit = {},
    onClose: () -> Unit
) {
    var selectedTier by remember { mutableStateOf(CraftingTier.TIER_1) }
    var categoryFilter by remember { mutableStateOf(CraftCategoryFilter.ALL) }
    var matCategoryFilter by remember { mutableStateOf<CraftingMaterialCategory?>(null) }
    var inspectTarget by remember { mutableStateOf<InspectTarget?>(null) }

    val carriedMaterialUnits = remember(operator) {
        if (operator == null) 0
        else (operator.backpackItems + operator.safePocketItems)
            .filter { it.type == LootType.MATERIAL }
            .sumOf { it.quantity.coerceAtLeast(1) }
    }

    val visibleMaterials = remember(matCategoryFilter) {
        if (matCategoryFilter == null) CraftingMaterial.entries
        else CraftingMaterial.entries.filter { it.matCategory == matCategoryFilter }
    }

    val tierRecipes = remember(selectedTier, categoryFilter) {
        CraftingCatalog.recipes.filter { r ->
            if (r.tier != selectedTier) return@filter false
            when (categoryFilter) {
                CraftCategoryFilter.ALL -> true
                CraftCategoryFilter.MELEE -> r.outputKind == CraftOutputKind.WEAPON && WeaponId.fromKey(r.weaponKey ?: "knife").isMelee
                CraftCategoryFilter.GUNS -> r.outputKind == CraftOutputKind.WEAPON && !WeaponId.fromKey(r.weaponKey ?: "knife").isMelee
                CraftCategoryFilter.MODS -> r.outputKind == CraftOutputKind.ATTACHMENT
                CraftCategoryFilter.ARMOR -> r.outputKind == CraftOutputKind.ARMOR
                CraftCategoryFilter.AMMO -> r.outputKind == CraftOutputKind.AMMO
                CraftCategoryFilter.GEAR -> r.outputKind in setOf(CraftOutputKind.PANTS, CraftOutputKind.CONSUMABLE)
            }
        }
    }

    ModalScrim(onDismiss = onClose) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                    Icon(Icons.Default.Build, contentDescription = null, tint = AmberSignal)
                    Column {
                        Text(
                            text = "OUTPOST WORKBENCH // CRAFTING",
                            style = MaterialTheme.typography.headlineMedium,
                            color = AmberSignal
                        )
                        Text(
                            text = "Depot Cap ${stash.storageMaterialCap}/mat • ${CraftingMaterial.entries.size} Base Materials • ${BaseUpgradeSpecs.reloadingGearTitle(stash.reloadingGearLevel)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldHud,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp).testTag("workbench_close_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close workbench", tint = Color.White)
                }
            }

            // Base Materials Category Filter + Tap-to-Inspect 24-Material Depot Strip
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = matCategoryFilter == null,
                        onClick = { matCategoryFilter = null },
                        modifier = Modifier.heightIn(min = 40.dp),
                        label = { Text("ALL MATS (${CraftingMaterial.entries.size})", style = MaterialTheme.typography.labelSmall) }
                    )
                }
                items(CraftingMaterialCategory.entries) { mcat ->
                    FilterChip(
                        selected = matCategoryFilter == mcat,
                        onClick = { matCategoryFilter = if (matCategoryFilter == mcat) null else mcat },
                        modifier = Modifier.heightIn(min = 40.dp),
                        label = { Text(mcat.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(visibleMaterials, key = { it.key }) { mat ->
                    val count = stash.materialCount(mat)
                    val accent = Color(mat.colorHex)
                    Column(
                        modifier = Modifier
                            .background(BunkerObsidian, RoundedCornerShape(8.dp))
                            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable {
                                inspectTarget = InspectTarget.MaterialPreview(
                                    material = mat,
                                    ownedCount = count,
                                    storageCap = stash.storageMaterialCap
                                )
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("workbench_mat_${mat.key}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(mat.shortName, style = MaterialTheme.typography.labelSmall, color = accent)
                        Text(
                            text = "$count/${stash.storageMaterialCap}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (carriedMaterialUnits > 0) {
                OutlinedButton(
                    onClick = onDepositMaterials,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("deposit_materials_btn"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = EmeraldHud, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("DEPOSIT CARRIED MATERIALS (+$carriedMaterialUnits TO STASH)", color = EmeraldHud, style = MaterialTheme.typography.labelSmall)
                }
            }

            // Complexity Tier Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CraftingTier.entries.forEach { tier ->
                    FilterChip(
                        selected = selectedTier == tier,
                        onClick = { selectedTier = tier },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("craft_tier_${tier.tierNum}"),
                        label = {
                            Text(
                                text = tier.badge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            // Category filter strip
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CraftCategoryFilter.entries) { cat ->
                    FilterChip(
                        selected = categoryFilter == cat,
                        onClick = { categoryFilter = cat },
                        modifier = Modifier.heightIn(min = 44.dp).testTag("craft_cat_${cat.name}"),
                        label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            if (categoryFilter == CraftCategoryFilter.AMMO) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BunkerObsidian, RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            if (stash.reloadingGearLevel > 0) CyanFloor.copy(alpha = 0.6f) else AmberSignal.copy(alpha = 0.7f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "HQ RELOADING BENCH: LV.${stash.reloadingGearLevel}/${BaseUpgradeSpecs.MAX_RELOADING_GEAR_LEVEL}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (stash.reloadingGearLevel > 0) CyanFloor else AmberBright
                        )
                        Text(
                            text = BaseUpgradeSpecs.reloadingGearDescription(stash.reloadingGearLevel),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD5E3DA),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (stash.reloadingGearLevel < BaseUpgradeSpecs.MAX_RELOADING_GEAR_LEVEL) {
                        OutlinedButton(
                            onClick = onOpenUpgrades,
                            modifier = Modifier.heightIn(min = 44.dp).testTag("workbench_open_reloading_upgrade_btn"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("UPGRADE HQ", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tierRecipes, key = { it.id }) { recipe ->
                    val gearUnlocked = stash.reloadingGearLevel >= recipe.requiredReloadingTier
                    val canAfford = stash.canAfford(recipe)
                    val canCraftNow = gearUnlocked && canAfford
                    val rarityColor = Color(recipe.rarityColorHex)
                    val statusTag = when (recipe.outputKind) {
                        CraftOutputKind.WEAPON -> {
                            val wk = recipe.weaponKey
                            if (wk != null && wk in stash.unlockedWeaponKeys && wk != "knife") "UNLOCKED • EXTRA TO BAG" else null
                        }
                        CraftOutputKind.PANTS -> {
                            val lvl = recipe.pantsLevel ?: 1
                            if (lvl <= stash.maxPantsLevel) "OWNED LV.${stash.maxPantsLevel} • EXTRA TO BAG" else null
                        }
                        CraftOutputKind.ARMOR -> {
                            val lvl = recipe.armorLevel ?: 1
                            if (lvl <= (operator?.armorLevel ?: stash.maxArmorLevel)) "EQUIPPED LV.$lvl • EXTRA TO BAG" else null
                        }
                        CraftOutputKind.AMMO -> {
                            if (!gearUnlocked) "LOCKED // REQ RELOADING GEAR LV.${recipe.requiredReloadingTier}" else "PRESS LV.${recipe.requiredReloadingTier} READY"
                        }
                        CraftOutputKind.ATTACHMENT -> {
                            val attKey = recipe.attachmentKey
                            if (attKey != null && attKey in stash.ownedAttachmentKeys) "OWNED IN STASH" else "ERA GUNSMITH PART"
                        }
                        else -> null
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BunkerSurface, RoundedCornerShape(10.dp))
                            .border(1.dp, rarityColor.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            .clickable { inspectTarget = InspectTarget.CraftRecipePreview(recipe) }
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = recipe.rarityLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = rarityColor
                                    )
                                    if (statusTag != null) {
                                        Text(
                                            text = statusTag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (!gearUnlocked) CrimsonAlert else EmeraldHud
                                        )
                                    }
                                }
                                Text(
                                    text = recipe.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = recipe.detail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AmberBright,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = recipe.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFBFD3C7),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            recipe.costs.forEach { (mat, needed) ->
                                val have = stash.materialCount(mat)
                                val ok = have >= needed
                                val chipColor = if (ok) EmeraldHud else CrimsonAlert
                                Text(
                                    text = "${mat.shortName} $have/$needed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = chipColor,
                                    modifier = Modifier
                                        .background(chipColor.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                                        .border(1.dp, chipColor.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = OliveBorder.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { inspectTarget = InspectTarget.CraftRecipePreview(recipe) },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("inspect_recipe_${recipe.id}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = AmberBright, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("INSPECT 3D", style = MaterialTheme.typography.labelSmall, color = AmberBright)
                            }
                            if (!gearUnlocked) {
                                Button(
                                    onClick = onOpenUpgrades,
                                    modifier = Modifier
                                        .weight(1.25f)
                                        .heightIn(min = 48.dp)
                                        .testTag("craft_btn_${recipe.id}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E3A5F),
                                        contentColor = CyanFloor
                                    )
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("REQ PRESS LV.${recipe.requiredReloadingTier}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { onCraft(recipe) },
                                    enabled = canCraftNow,
                                    modifier = Modifier
                                        .weight(1.25f)
                                        .heightIn(min = 48.dp)
                                        .testTag("craft_btn_${recipe.id}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberSignal,
                                        contentColor = Color.Black,
                                        disabledContainerColor = Color(0xFF28332C),
                                        disabledContentColor = Color(0xFF6B7F73)
                                    )
                                ) {
                                    Text(if (canCraftNow) "CRAFT" else "NEED MATS", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inspectTarget?.let { target ->
        ItemInspectorModal(
            target = target,
            currentPantsLevel = operator?.pantsLevel ?: stash.maxPantsLevel,
            safeSpaceLeft = operator?.let { it.safePocketCapacity - it.safePocketItems.size } ?: 0,
            playingCdId = playingCdId,
            stash = stash,
            onPlayCd = onPlayCd,
            onClose = { inspectTarget = null }
        )
    }
}
