package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.cloud.MarketListing
import com.example.game.model.ArmorTier
import com.example.game.model.AttachmentSlot
import com.example.game.model.BaseStashState
import com.example.game.model.CdCatalog
import com.example.game.model.CdRecord
import com.example.game.model.CraftOutputKind
import com.example.game.model.CraftingMaterial
import com.example.game.model.CraftingRecipe
import com.example.game.model.InventoryItem
import com.example.game.model.LootType
import com.example.game.model.WeaponAttachment
import com.example.game.model.WeaponAttachmentCatalog
import com.example.game.model.WeaponCategory
import com.example.game.model.WeaponId
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BunkerObsidian
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanFloor
import com.example.ui.theme.EmeraldHud
import com.example.ui.theme.OliveBorder
import com.example.ui.theme.PurpleRare
import kotlin.math.PI

sealed interface InspectTarget {
    data class BagItem(
        val item: InventoryItem,
        val inSafePocket: Boolean
    ) : InspectTarget

    data class LootItem(
        val item: InventoryItem,
        val containerLabel: String
    ) : InspectTarget

    data class EquippedWeapon(
        val weapon: WeaponId,
        val isActive: Boolean,
        val magAmmo: Int,
        val reserveAmmo: Int
    ) : InspectTarget

    data class EquippedPants(
        val level: Int,
        val usedSlots: Int
    ) : InspectTarget

    data class EquippedArmor(
        val tier: ArmorTier,
        val currentShieldHp: Float
    ) : InspectTarget

    data class BinderCd(
        val cd: CdRecord
    ) : InspectTarget

    data class CraftRecipePreview(
        val recipe: CraftingRecipe
    ) : InspectTarget

    data class MarketItem(
        val listing: MarketListing
    ) : InspectTarget

    data class MaterialPreview(
        val material: CraftingMaterial,
        val ownedCount: Int,
        val storageCap: Int
    ) : InspectTarget

    data class AttachmentItem(
        val attachment: WeaponAttachment,
        val owned: Boolean = false
    ) : InspectTarget
}

private data class InspectStat(
    val label: String,
    val value: String,
    val accent: Color = Color.White
)

@Composable
fun ItemInspectorModal(
    target: InspectTarget,
    currentPantsLevel: Int,
    safeSpaceLeft: Int,
    playingCdId: Int?,
    onPlayCd: (Int) -> Unit,
    stash: BaseStashState = BaseStashState(),
    onEquipAttachment: ((WeaponId, String) -> Unit)? = null,
    onUnequipSlot: ((WeaponId, AttachmentSlot) -> Unit)? = null,
    onUseItem: ((InventoryItem, Boolean) -> Unit)? = null,
    onMoveToSafe: ((InventoryItem) -> Unit)? = null,
    onMoveToBag: ((InventoryItem) -> Unit)? = null,
    onDropItem: ((InventoryItem, Boolean) -> Unit)? = null,
    onTakeLoot: ((InventoryItem, Boolean) -> Unit)? = null,
    onSelectWeapon: ((WeaponId) -> Unit)? = null,
    onClose: () -> Unit
) {
    val rarity = resolveTargetRarity(target)
    val previewMods = if (target is InspectTarget.EquippedWeapon) stash.equippedAttachmentsFor(target.weapon) else emptySet()

    ModalScrim(onDismiss = onClose) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = inspectCategoryLabel(target),
                            style = MaterialTheme.typography.labelSmall,
                            color = inspectAccentColor(target, rarity)
                        )
                        Text(
                            text = rarity.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = rarity.color,
                            modifier = Modifier
                                .background(rarity.color.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                                .border(1.dp, rarity.color.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                .testTag("inspect_rarity_badge")
                        )
                    }
                    Text(
                        text = inspectTitle(target),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp).testTag("inspect_close_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close inspector", tint = Color.White)
                }
            }

            ItemSprite3DViewport(
                target = target,
                rarity = rarity,
                equippedAttachmentKeys = previewMods
            )

            when (target) {
                is InspectTarget.BinderCd -> {
                    CdInspectBody(
                        cd = target.cd,
                        rarity = rarity,
                        playingCdId = playingCdId,
                        onPlayCd = onPlayCd
                    )
                }
                is InspectTarget.EquippedWeapon -> {
                    val effective = stash.effectiveStatsFor(target.weapon)
                    WeaponInspectBody(
                        weapon = target.weapon,
                        rarity = rarity,
                        statusLine = if (target.weapon.isMelee) "Melee Hitbox • Silent"
                        else "Loaded ${target.magAmmo}/${effective.magSize} • Res ${target.reserveAmmo}",
                        stash = stash,
                        onEquipAttachment = onEquipAttachment,
                        onUnequipSlot = onUnequipSlot
                    )
                }
                is InspectTarget.EquippedPants -> {
                    PantsInspectBody(
                        level = target.level,
                        currentLevel = currentPantsLevel,
                        usedSlots = target.usedSlots,
                        isEquipped = true,
                        rarity = rarity
                    )
                }
                is InspectTarget.EquippedArmor -> {
                    ArmorInspectBody(
                        tier = target.tier,
                        currentShieldHp = target.currentShieldHp,
                        rarity = rarity
                    )
                }
                is InspectTarget.BagItem -> {
                    InventoryItemInspectBody(
                        item = target.item,
                        rarity = rarity,
                        locationNote = if (target.inSafePocket) "Safe Pocket (Untouchable)" else "Exposed Backpack",
                        currentPantsLevel = currentPantsLevel,
                        playingCdId = playingCdId,
                        onPlayCd = onPlayCd,
                        stash = stash
                    )
                }
                is InspectTarget.LootItem -> {
                    InventoryItemInspectBody(
                        item = target.item,
                        rarity = rarity,
                        locationNote = "Container: ${target.containerLabel}",
                        currentPantsLevel = currentPantsLevel,
                        playingCdId = playingCdId,
                        onPlayCd = onPlayCd,
                        stash = stash
                    )
                }
                is InspectTarget.CraftRecipePreview -> {
                    CraftRecipeInspectBody(
                        recipe = target.recipe,
                        rarity = rarity,
                        currentPantsLevel = currentPantsLevel,
                        stash = stash
                    )
                }
                is InspectTarget.MarketItem -> {
                    InventoryItemInspectBody(
                        item = target.listing.toInventoryItemPreview(),
                        rarity = rarity,
                        locationNote = "Seller: ${target.listing.sellerCallsign} • ${target.listing.priceCredits} CRED",
                        currentPantsLevel = currentPantsLevel,
                        playingCdId = playingCdId,
                        onPlayCd = onPlayCd,
                        stash = stash
                    )
                }
                is InspectTarget.MaterialPreview -> {
                    MaterialInspectBody(
                        material = target.material,
                        ownedCount = target.ownedCount,
                        storageCap = target.storageCap,
                        rarity = rarity
                    )
                }
                is InspectTarget.AttachmentItem -> {
                    AttachmentInspectBody(
                        attachment = target.attachment,
                        owned = target.owned || target.attachment.key in stash.ownedAttachmentKeys
                    )
                }
            }

            HorizontalDivider(color = OliveBorder)

            InspectorActionRow(
                target = target,
                safeSpaceLeft = safeSpaceLeft,
                onUseItem = onUseItem,
                onMoveToSafe = onMoveToSafe,
                onMoveToBag = onMoveToBag,
                onDropItem = onDropItem,
                onTakeLoot = onTakeLoot,
                onSelectWeapon = onSelectWeapon,
                onClose = onClose
            )
        }
    }
}

@Composable
private fun ArmorInspectBody(
    tier: ArmorTier,
    currentShieldHp: Float,
    rarity: ItemRarityInfo
) {
    InspectStatGrid(
        stats = listOf(
            InspectStat("ARMOR CLASS", tier.shortName, rarity.color),
            InspectStat("SHIELD CAPACITY", "${currentShieldHp.toInt()} / ${tier.maxShieldHp.toInt()}", CyanFloor),
            InspectStat("DAMAGE REDUCTION", "${(tier.mitigation * 100).toInt()}% Mitigated", EmeraldHud),
            InspectStat("REPAIR", "Auto-Repairs at Base", AmberBright)
        )
    )
    Text(tier.description, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFD5E3DA))
}

@Composable
private fun CraftRecipeInspectBody(
    recipe: CraftingRecipe,
    rarity: ItemRarityInfo,
    currentPantsLevel: Int,
    stash: BaseStashState = BaseStashState()
) {
    Text(
        text = "Complexity: ${recipe.tier.title}",
        style = MaterialTheme.typography.labelSmall,
        color = CyanFloor
    )
    when (recipe.outputKind) {
        CraftOutputKind.WEAPON -> {
            WeaponInspectBody(
                weapon = WeaponId.fromKey(recipe.weaponKey ?: "knife"),
                rarity = rarity,
                statusLine = if (recipe.ammoAmount > 0) "Includes +${recipe.ammoAmount} starter rounds" else "Blueprint Ready"
            )
        }
        CraftOutputKind.PANTS -> {
            PantsInspectBody(
                level = recipe.pantsLevel ?: 2,
                currentLevel = currentPantsLevel,
                usedSlots = 0,
                isEquipped = false,
                rarity = rarity,
                subtitle = "Equips immediately & raises max crafted Cargo Pants tier"
            )
        }
        CraftOutputKind.ARMOR -> {
            val tier = ArmorTier.fromLevel(recipe.armorLevel ?: 1)
            ArmorInspectBody(tier = tier, currentShieldHp = tier.maxShieldHp, rarity = rarity)
        }
        CraftOutputKind.AMMO -> {
            val wpn = WeaponId.fromKey(recipe.weaponKey ?: "m4")
            InspectStatGrid(
                stats = listOf(
                    InspectStat("CALIBER", wpn.caliber, AmberBright),
                    InspectStat("OUTPUT", "+${recipe.ammoAmount} Rounds", EmeraldHud),
                    InspectStat("FOR WEAPON", wpn.displayName, Color.White),
                    InspectStat("RELOADING GEAR REQ", "HQ Bench Lv.${recipe.requiredReloadingTier}", AmberSignal)
                )
            )
            Text(
                recipe.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFD5E3DA)
            )
        }
        CraftOutputKind.ATTACHMENT -> {
            val att = WeaponAttachmentCatalog.fromKey(recipe.attachmentKey)
            if (att != null) {
                AttachmentInspectBody(
                    attachment = att,
                    owned = att.key in stash.ownedAttachmentKeys
                )
            } else {
                Text(recipe.description, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFD5E3DA))
            }
        }
        CraftOutputKind.CONSUMABLE -> {
            InspectStatGrid(
                stats = listOf(
                    InspectStat("CATEGORY", recipe.consumableType?.name ?: "SUPPLY", rarity.color),
                    InspectStat("EFFECT", recipe.detail, EmeraldHud),
                    InspectStat("DESTINATION", "Operator Backpack", CyanFloor),
                    InspectStat("COMPLEXITY", recipe.tier.badge, AmberSignal)
                )
            )
            Text(
                recipe.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFD5E3DA)
            )
        }
    }
}

@Composable
private fun MaterialInspectBody(
    material: CraftingMaterial,
    ownedCount: Int,
    storageCap: Int,
    rarity: ItemRarityInfo
) {
    InspectStatGrid(
        stats = listOf(
            InspectStat("CATEGORY", material.matCategory.label, CyanFloor),
            InspectStat("STOCKPILE", "$ownedCount / $storageCap", EmeraldHud),
            InspectStat("RARITY", rarity.label, rarity.color),
            InspectStat("SHORT TAG", material.shortName, AmberBright),
            InspectStat("DELAINE SOURCES", material.oakridgeSources, Color.White),
            InspectStat("USE AT", "Workbench & HQ Upgrades", AmberSignal)
        )
    )
    Text(
        text = material.description,
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFFD5E3DA)
    )
}

@Composable
private fun InspectorActionRow(
    target: InspectTarget,
    safeSpaceLeft: Int,
    onUseItem: ((InventoryItem, Boolean) -> Unit)?,
    onMoveToSafe: ((InventoryItem) -> Unit)?,
    onMoveToBag: ((InventoryItem) -> Unit)?,
    onDropItem: ((InventoryItem, Boolean) -> Unit)?,
    onTakeLoot: ((InventoryItem, Boolean) -> Unit)?,
    onSelectWeapon: ((WeaponId) -> Unit)?,
    onClose: () -> Unit
) {
    when (target) {
        is InspectTarget.BagItem -> {
            val item = target.item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.type in listOf(LootType.FOOD, LootType.WATER, LootType.MEDKIT, LootType.PANTS, LootType.ARMOR)) {
                    Button(
                        onClick = {
                            onUseItem?.invoke(item, target.inSafePocket)
                            onClose()
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("inspect_use_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldHud, contentColor = Color.Black)
                    ) {
                        Text(if (item.type == LootType.PANTS || item.type == LootType.ARMOR) "EQUIP" else "USE", fontWeight = FontWeight.Bold)
                    }
                }
                if (target.inSafePocket) {
                    OutlinedButton(
                        onClick = {
                            onMoveToBag?.invoke(item)
                            onClose()
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("TO BAG", style = MaterialTheme.typography.labelSmall)
                    }
                } else if (safeSpaceLeft > 0) {
                    OutlinedButton(
                        onClick = {
                            onMoveToSafe?.invoke(item)
                            onClose()
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("TO SAFE", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                    }
                }
                OutlinedButton(
                    onClick = {
                        onDropItem?.invoke(item, target.inSafePocket)
                        onClose()
                    },
                    modifier = Modifier.height(48.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Drop item", tint = CrimsonAlert)
                }
            }
        }
        is InspectTarget.LootItem -> {
            val item = target.item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onTakeLoot?.invoke(item, false)
                        onClose()
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("inspect_take_bag_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberSignal, contentColor = Color.Black)
                ) {
                    val label = if (item.type == LootType.WEAPON || item.type == LootType.PANTS || item.type == LootType.ARMOR) "EQUIP GEAR" else "TAKE TO BAG"
                    Text(label, fontWeight = FontWeight.Bold)
                }
                if (safeSpaceLeft > 0 && item.type != LootType.WEAPON && item.type != LootType.PANTS && item.type != LootType.ARMOR) {
                    OutlinedButton(
                        onClick = {
                            onTakeLoot?.invoke(item, true)
                            onClose()
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("inspect_take_safe_btn")
                    ) {
                        Text("TUCK IN SAFE", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                    }
                }
            }
        }
        is InspectTarget.EquippedWeapon -> {
            if (!target.isActive) {
                Button(
                    onClick = {
                        onSelectWeapon?.invoke(target.weapon)
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("inspect_equip_weapon_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberSignal, contentColor = Color.Black)
                ) {
                    Text("EQUIP ${target.weapon.shortName}", fontWeight = FontWeight.Bold)
                }
            }
        }
        else -> Unit
    }
}

@Composable
private fun InventoryItemInspectBody(
    item: InventoryItem,
    rarity: ItemRarityInfo,
    locationNote: String,
    currentPantsLevel: Int,
    playingCdId: Int?,
    onPlayCd: (Int) -> Unit,
    stash: BaseStashState = BaseStashState()
) {
    when {
        item.type == LootType.CD && item.cdId != null -> {
            val cd = CdCatalog.getById(item.cdId)
            CdInspectBody(cd = cd, rarity = rarity, playingCdId = playingCdId, onPlayCd = onPlayCd, subtitle = locationNote)
        }
        item.type == LootType.WEAPON && item.weaponKey != null -> {
            WeaponInspectBody(weapon = WeaponId.fromKey(item.weaponKey), rarity = rarity, statusLine = locationNote)
        }
        item.type == LootType.ATTACHMENT -> {
            val att = WeaponAttachmentCatalog.fromKey(item.attachmentKey)
            if (att != null) {
                AttachmentInspectBody(
                    attachment = att,
                    owned = att.key in stash.ownedAttachmentKeys,
                    subtitle = locationNote
                )
            } else {
                Text(item.detail.ifBlank { "Weapon modification hardware." }, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFD5E3DA))
            }
        }
        item.type == LootType.PANTS && item.pantsLevel != null -> {
            PantsInspectBody(
                level = item.pantsLevel,
                currentLevel = currentPantsLevel,
                usedSlots = 0,
                isEquipped = false,
                rarity = rarity,
                subtitle = locationNote
            )
        }
        item.type == LootType.ARMOR && item.armorLevel != null -> {
            val tier = ArmorTier.fromLevel(item.armorLevel)
            ArmorInspectBody(tier = tier, currentShieldHp = tier.maxShieldHp, rarity = rarity)
        }
        item.type == LootType.MATERIAL -> {
            val mat = CraftingMaterial.fromKey(item.materialKey)
            InspectStatGrid(
                stats = listOf(
                    InspectStat("RESOURCE", mat?.shortName ?: "MATERIAL", rarity.color),
                    InspectStat("QUANTITY", "x${item.quantity}", EmeraldHud),
                    InspectStat("RARITY", rarity.label, rarity.color),
                    InspectStat("SOURCES", mat?.oakridgeSources ?: "DeLaine", CyanFloor),
                    InspectStat("USE AT", "Workbench / Market", AmberBright),
                    InspectStat("STORAGE", locationNote, Color(0xFFD5E3DA))
                )
            )
            Text(
                text = mat?.description ?: "Base crafting resource scavenged from DeLaine.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFD5E3DA)
            )
        }
        else -> {
            val (effectLabel, baseDestination, lore) = when (item.type) {
                LootType.FOOD -> Triple(
                    "+${item.value} Hunger",
                    "+1 Cantine Food Stock",
                    "Crafted ration pack for Outpost Zero. Eat in the field to stabilize hunger or sell on the Live Market."
                )
                LootType.WATER -> Triple(
                    "+${item.value} Thirst",
                    "+1 Cooler Water",
                    "Purified drinking canteen. Drink on patrol to recover stamina or list on the Live Market."
                )
                LootType.MEDKIT -> Triple(
                    "+${item.value} HP",
                    "+1 Infirmary Med",
                    "Compounded trauma bandages and antiseptic field kit. Patches lacerations instantly on a run."
                )
                else -> Triple(
                    "${item.value} CRED",
                    "Base Stash Credits",
                    "Pre-outbreak parts valued at ${item.value} CRED."
                )
            }
            InspectStatGrid(
                stats = listOf(
                    InspectStat("CATEGORY", item.type.name, rarity.color),
                    InspectStat("RARITY TIER", rarity.label, rarity.color),
                    InspectStat("FIELD EFFECT", effectLabel, EmeraldHud),
                    InspectStat("EXTRACTION", baseDestination, AmberBright),
                    InspectStat("STORAGE", locationNote, CyanFloor),
                    InspectStat("POCKET ELIGIBLE", "Yes (1 Slot)", Color(0xFFD5E3DA))
                )
            )
            if (item.detail.isNotBlank()) {
                Text("Spec: ${item.detail}", style = MaterialTheme.typography.labelSmall, color = AmberSignal)
            }
            Text(lore, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFD5E3DA))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttachmentInspectBody(
    attachment: WeaponAttachment,
    owned: Boolean,
    subtitle: String = ""
) {
    val eraCol = Color(attachment.era.accentHex)
    val compatWeapons = remember(attachment.key) {
        WeaponId.entries.filter { attachment.isCompatibleWith(it) }
    }
    InspectStatGrid(
        stats = listOf(
            InspectStat("MOD SLOT", attachment.slot.label, CyanFloor),
            InspectStat("HISTORICAL ERA", attachment.era.badge, eraCol),
            InspectStat("ERAS ALLOWED", attachment.erasAllowed.joinToString(", ") { it.name.replace('_', ' ') }, AmberBright),
            InspectStat("ARMORY STATUS", if (owned) "OWNED IN STASH" else "FIND / CRAFT AT BENCH", if (owned) EmeraldHud else AmberSignal)
        )
    )
    if (subtitle.isNotBlank()) {
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = CyanFloor)
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for ((label, positive) in attachment.statCallouts()) {
            val col = if (positive) EmeraldHud else CrimsonAlert
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = col,
                modifier = Modifier
                    .background(col.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                    .border(1.dp, col.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BunkerSurface, RoundedCornerShape(8.dp))
            .border(1.dp, OliveBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("HISTORICAL NOTE", style = MaterialTheme.typography.labelSmall, color = AmberBright)
        Text(attachment.historicalNote, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFD5E3DA))
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Compatible Firearms (${compatWeapons.size}): ${compatWeapons.take(6).joinToString(", ") { it.shortName }}${if (compatWeapons.size > 6) " +${compatWeapons.size - 6} more" else ""}",
            style = MaterialTheme.typography.labelSmall,
            color = CyanFloor
        )
    }
}

@Composable
private fun CdInspectBody(
    cd: CdRecord,
    rarity: ItemRarityInfo,
    playingCdId: Int?,
    onPlayCd: (Int) -> Unit,
    subtitle: String = "Filed in Outpost Zero CD Book"
) {
    val isPlaying = playingCdId == cd.id
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BunkerObsidian, RoundedCornerShape(10.dp))
            .border(1.dp, rarity.color.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(cd.artist, style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text("Album: ${cd.album}", style = MaterialTheme.typography.bodyMedium, color = AmberBright)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = CyanFloor)
    }

    InspectStatGrid(
        stats = listOf(
            InspectStat("CATALOG TAG", "${cd.catalogTag} / ${CdCatalog.TOTAL_CDS}", PurpleRare),
            InspectStat("RARITY", rarity.label, rarity.color),
            InspectStat("GENRE", cd.genre.label, EmeraldHud),
            InspectStat("TEMPO", "${cd.bpm} BPM", AmberSignal)
        )
    )

    Text(
        "Scratched plastic jewel case with printed liner notes. Extract safely to file ${cd.catalogTag} in numerical order on Outpost Zero's CD Rack, or trade it on the Live Market.",
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFFD5E3DA)
    )

    OutlinedButton(
        onClick = { onPlayCd(cd.id) },
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("inspect_cd_play_btn")
    ) {
        Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, tint = AmberSignal)
        Spacer(Modifier.width(8.dp))
        Text(if (isPlaying) "STOP STEREO PREVIEW" else "PLAY SYNTH TRACK PREVIEW", color = AmberSignal)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeaponInspectBody(
    weapon: WeaponId,
    rarity: ItemRarityInfo,
    statusLine: String,
    stash: BaseStashState? = null,
    onEquipAttachment: ((WeaponId, String) -> Unit)? = null,
    onUnequipSlot: ((WeaponId, AttachmentSlot) -> Unit)? = null
) {
    val equippedKeys = remember(stash, weapon) { stash?.equippedAttachmentsFor(weapon).orEmpty() }
    val eff = remember(stash, weapon) { WeaponAttachmentCatalog.computeEffectiveStats(weapon, equippedKeys) }
    val hasMods = equippedKeys.isNotEmpty() && !weapon.isMelee

    val coneDeg = (eff.baseHalfFov * 2f * 180f / PI.toFloat()).toInt()
    val focusRange = (eff.baseRange + eff.stillRangeBonus).toInt()
    val rpmText = if (weapon.isMelee) "Swing ${weapon.fireDelayMs}ms" else "${(60_000L / eff.fireDelayMs.coerceAtLeast(1L))} RPM"
    val reloadText = if (weapon.isMelee) "Reach ${weapon.meleeReach.toInt()}m" else "${"%.2f".format(eff.reloadTimeMs / 1000f)}s"
    val dmgText = if (weapon.pelletCount > 1) "${eff.damage} x${weapon.pelletCount} (${eff.damage * weapon.pelletCount})" else "${eff.damage}"
    val noiseText = if (eff.suppressed || weapon.isMelee) "Silent (${eff.noiseRadius.toInt()}m)" else "${eff.noiseRadius.toInt()}m"
    val velocityText = if (weapon.muzzleVelocityFps > 0) "${weapon.muzzleVelocityFps} ft/s" else "Manual Swing"
    val magText = if (weapon.isMelee) "N/A (Melee)" else "${eff.magSize} rds (Max ${weapon.reserveMax})"
    val benchReqText = if (weapon.reloadingTierRequired <= 0) "None" else "HQ Press Lv.${weapon.reloadingTierRequired}"
    val eraColor = Color(weapon.era.accentHex)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = weapon.era.badge,
            style = MaterialTheme.typography.labelSmall,
            color = eraColor,
            modifier = Modifier
                .background(eraColor.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                .border(1.dp, eraColor.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
        if (weapon.isHistoricalCollectible) {
            Text(
                text = "HERITAGE RELIC",
                style = MaterialTheme.typography.labelSmall,
                color = AmberBright,
                modifier = Modifier
                    .background(AmberBright.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                    .border(1.dp, AmberBright.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        if (hasMods) {
            Text(
                text = "${eff.equippedAttachments.size} MODS FITTED",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldHud,
                modifier = Modifier
                    .background(EmeraldHud.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                    .border(1.dp, EmeraldHud.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }

    InspectStatGrid(
        stats = listOf(
            InspectStat("MANUFACTURER", weapon.manufacturer, AmberBright),
            InspectStat("ORIGIN & YEAR", "${weapon.countryOrigin} • ${weapon.yearIntroduced}", CyanFloor),
            InspectStat("CALIBER / STEEL", weapon.caliber, AmberBright),
            InspectStat("ACTION SYSTEM", weapon.actionSystem, Color.White),
            InspectStat("STATUS", statusLine, EmeraldHud),
            InspectStat("DAMAGE", dmgText, if (eff.damage > weapon.damage) EmeraldHud else AmberSignal),
            InspectStat("RATE OF FIRE", rpmText, if (eff.fireDelayMs < weapon.fireDelayMs) EmeraldHud else Color.White),
            InspectStat("MUZZLE VELOCITY", velocityText, CyanFloor),
            InspectStat("EFFECTIVE RANGE", "${weapon.effectiveRangeMeters}m (${focusRange}u focus)", if (eff.baseRange + eff.stillRangeBonus > weapon.baseRange + weapon.stillRangeBonus) EmeraldHud else EmeraldHud),
            InspectStat("BARREL / LENGTH", weapon.barrelLengthIn, Color(0xFFD5E3DA)),
            InspectStat("UNLOADED WEIGHT", weapon.weightKg, Color(0xFFD5E3DA)),
            InspectStat("CAPACITY / MAG", magText, if (eff.magSize > weapon.magSize) EmeraldHud else AmberBright),
            InspectStat("PENETRATION", "${eff.penetrationPct}% Armor Pen", if (eff.penetrationPct > weapon.penetrationPct) EmeraldHud else PurpleRare),
            InspectStat("AIM CONE / SWEEP", "${coneDeg}° arc", CyanFloor),
            InspectStat("SOUND PROFILE", noiseText, if (eff.suppressed || weapon.isMelee) CyanFloor else CrimsonAlert),
            InspectStat(if (weapon.isMelee) "HITBOX REACH" else "RELOAD / PRESS", "$reloadText • $benchReqText", Color(0xFFD5E3DA))
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BunkerObsidian, RoundedCornerShape(10.dp))
            .border(1.dp, rarity.color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WeaponStatBar("STOPPING POWER", (eff.damage * weapon.pelletCount.coerceAtLeast(1)) / 220f, dmgText, AmberSignal)
        WeaponStatBar("EFFECTIVE REACH", focusRange / 920f, "${weapon.effectiveRangeMeters}m • ${focusRange}u aim focus", EmeraldHud)
        WeaponStatBar("ARMOR PENETRATION", eff.penetrationPct / 100f, "${eff.penetrationPct}% penetration", PurpleRare)
        WeaponStatBar("KNOCKBACK FORCE", eff.knockback / 52f, "${eff.knockback.toInt()} impulse", CyanFloor)
    }

    if (stash != null && !weapon.isMelee && onEquipAttachment != null && onUnequipSlot != null) {
        var selectedSlot by remember(weapon) {
            val firstWithOpts = AttachmentSlot.entries.firstOrNull {
                WeaponAttachmentCatalog.compatibleForSlot(weapon, it).isNotEmpty()
            } ?: AttachmentSlot.OPTIC
            mutableStateOf(firstWithOpts)
        }
        val slotOptions = remember(weapon, selectedSlot) {
            WeaponAttachmentCatalog.compatibleForSlot(weapon, selectedSlot)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BunkerObsidian, RoundedCornerShape(10.dp))
                .border(1.5.dp, eraColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Build, contentDescription = null, tint = eraColor, modifier = Modifier.size(16.dp))
                Text(
                    text = "GUNSMITH // ERA-AUTHENTIC ATTACHMENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = eraColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Era Rule (${weapon.era.badge}): Only period-authentic & platform-compatible parts fit ${weapon.shortName}.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFBFD3C7)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(AttachmentSlot.entries) { slot ->
                    val hasSlotEquipped = eff.equippedAttachments.any { it.slot == slot }
                    val compatCount = WeaponAttachmentCatalog.compatibleForSlot(weapon, slot).size
                    FilterChip(
                        selected = selectedSlot == slot,
                        onClick = { selectedSlot = slot },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("gunsmith_slot_${slot.name}"),
                        leadingIcon = if (hasSlotEquipped) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldHud, modifier = Modifier.size(14.dp)) }
                        } else null,
                        label = {
                            Text(
                                text = "${slot.shortLabel} ($compatCount)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            if (slotOptions.isEmpty()) {
                Text(
                    text = "No period-authentic ${selectedSlot.shortLabel.lowercase()} mounts exist for ${weapon.displayName} (${weapon.yearIntroduced}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            } else {
                slotOptions.forEach { att ->
                    val isEquipped = eff.equippedAttachments.any { it.key == att.key }
                    val isOwned = att.key in stash.ownedAttachmentKeys
                    val attAccent = Color(att.rarityColorHex)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isEquipped) Color(0xFF162B24) else BunkerSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, if (isEquipped) EmeraldHud else attAccent.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "${att.era.badge} • ${att.rarityLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = attAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = att.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = att.historicalNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFBFD3C7)
                                )
                            }
                            when {
                                isEquipped -> {
                                    OutlinedButton(
                                        onClick = { onUnequipSlot(weapon, att.slot) },
                                        modifier = Modifier.heightIn(min = 48.dp).testTag("gunsmith_unequip_${att.key}"),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Text("UNEQUIP", style = MaterialTheme.typography.labelSmall, color = CrimsonAlert)
                                    }
                                }
                                isOwned -> {
                                    Button(
                                        onClick = { onEquipAttachment(weapon, att.key) },
                                        modifier = Modifier.heightIn(min = 48.dp).testTag("gunsmith_equip_${att.key}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldHud, contentColor = Color.Black),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("EQUIP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .background(BunkerObsidian, RoundedCornerShape(6.dp))
                                            .border(1.dp, OliveBorder, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = AmberBright, modifier = Modifier.size(14.dp))
                                        Text("FIND / CRAFT", style = MaterialTheme.typography.labelSmall, color = AmberBright)
                                    }
                                }
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for ((statLbl, good) in att.statCallouts()) {
                                val chipCol = if (good) EmeraldHud else CrimsonAlert
                                Text(
                                    text = statLbl,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = chipCol,
                                    modifier = Modifier
                                        .background(chipCol.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BunkerSurface, RoundedCornerShape(8.dp))
            .border(1.dp, OliveBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("BALLISTICS & HISTORICAL BRIEF", style = MaterialTheme.typography.labelSmall, color = AmberBright)
        Text(weapon.gunDescription, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFD5E3DA))
    }
}

@Composable
private fun WeaponStatBar(label: String, ratio: Float, valueText: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            Text(valueText, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0.05f, 1f) },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = BunkerSurface
        )
    }
}

@Composable
private fun PantsInspectBody(
    level: Int,
    currentLevel: Int,
    usedSlots: Int,
    isEquipped: Boolean,
    rarity: ItemRarityInfo,
    subtitle: String = ""
) {
    val diff = level - currentLevel
    val comparisonText = when {
        isEquipped -> "Equipped ($usedSlots/$level used)"
        diff > 0 -> "+$diff Safe Pocket${if (diff > 1) "s" else ""}"
        diff == 0 -> "Matches Lv.$currentLevel"
        else -> "Lower than Lv.$currentLevel"
    }

    InspectStatGrid(
        stats = listOf(
            InspectStat("GEAR TIER", "Level $level Cargo", rarity.color),
            InspectStat("SAFE CAPACITY", "$level Pocket${if (level > 1) "s" else ""}", CyanFloor),
            InspectStat("COMPARISON", comparisonText, if (diff > 0) EmeraldHud else AmberBright),
            InspectStat("KIA PROTECTION", "100% Untouchable", EmeraldHud)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BunkerObsidian, RoundedCornerShape(10.dp))
            .border(1.dp, CyanFloor.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("CONCEALED WAISTBAND SLOTS", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(level.coerceIn(1, 4)) { idx ->
                val filled = isEquipped && idx < usedSlots
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .background(if (filled) Color(0xFF0E3A4D) else BunkerSurface, RoundedCornerShape(6.dp))
                        .border(1.dp, CyanFloor, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (filled) Icons.Default.Lock else Icons.Default.Security,
                        contentDescription = null,
                        tint = CyanFloor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
    if (subtitle.isNotBlank()) {
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = AmberBright)
    }
    Text(
        "Reinforced hidden pockets stitched inside the waistband. Items stored in Safe Pockets cannot be seen or taken if you go down.",
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFFD5E3DA)
    )
}

@Composable
private fun InspectStatGrid(stats: List<InspectStat>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.testTag("inspect_stats_grid")) {
        stats.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pair.forEach { stat ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(BunkerSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, OliveBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stat.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stat.value,
                            style = MaterialTheme.typography.titleSmall,
                            color = stat.accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun resolveTargetRarity(target: InspectTarget): ItemRarityInfo = when (target) {
    is InspectTarget.BinderCd -> ItemRarityResolver.forCdRarity(target.cd.rarity)
    is InspectTarget.EquippedWeapon -> ItemRarityResolver.forWeapon(target.weapon)
    is InspectTarget.EquippedPants -> ItemRarityResolver.forPants(target.level)
    is InspectTarget.EquippedArmor -> ItemRarityResolver.forArmor(target.tier.level)
    is InspectTarget.BagItem -> ItemRarityResolver.forInventoryItem(target.item)
    is InspectTarget.LootItem -> ItemRarityResolver.forInventoryItem(target.item)
    is InspectTarget.CraftRecipePreview -> ItemRarityResolver.forRecipe(target.recipe)
    is InspectTarget.MarketItem -> ItemRarityResolver.forRarityLabel(target.listing.rarityLabel)
    is InspectTarget.MaterialPreview -> ItemRarityResolver.forMaterial(target.material.key)
    is InspectTarget.AttachmentItem -> ItemRarityInfo(target.attachment.rarityLabel, Color(target.attachment.rarityColorHex))
}

private fun inspectCategoryLabel(target: InspectTarget): String = when (target) {
    is InspectTarget.BinderCd -> "CD BINDER"
    is InspectTarget.EquippedWeapon -> "EQUIPPED WEAPON"
    is InspectTarget.EquippedPants -> "EQUIPPED GEAR"
    is InspectTarget.EquippedArmor -> "BALLISTIC ARMOR"
    is InspectTarget.BagItem -> if (target.inSafePocket) "SAFE POCKET" else "BACKPACK"
    is InspectTarget.LootItem -> "LOOT CONTAINER"
    is InspectTarget.CraftRecipePreview -> "CRAFT BLUEPRINT"
    is InspectTarget.MarketItem -> "LIVE MARKET"
    is InspectTarget.MaterialPreview -> "BASE RESOURCE"
    is InspectTarget.AttachmentItem -> "GUNSMITH ATTACHMENT"
}

private fun inspectTitle(target: InspectTarget): String = when (target) {
    is InspectTarget.BinderCd -> "${target.cd.catalogTag} ${target.cd.artist}"
    is InspectTarget.EquippedWeapon -> target.weapon.displayName
    is InspectTarget.EquippedPants -> "Cargo Pants (Lv.${target.level})"
    is InspectTarget.EquippedArmor -> target.tier.displayName
    is InspectTarget.BagItem -> target.item.name
    is InspectTarget.LootItem -> target.item.name
    is InspectTarget.CraftRecipePreview -> target.recipe.name
    is InspectTarget.MarketItem -> target.listing.itemName
    is InspectTarget.MaterialPreview -> target.material.displayName
    is InspectTarget.AttachmentItem -> target.attachment.name
}

private fun inspectAccentColor(target: InspectTarget, rarity: ItemRarityInfo): Color = when (target) {
    is InspectTarget.BinderCd -> rarity.color
    is InspectTarget.EquippedWeapon -> AmberSignal
    is InspectTarget.EquippedPants -> CyanFloor
    is InspectTarget.EquippedArmor -> rarity.color
    is InspectTarget.BagItem -> itemAccent(target.item.type)
    is InspectTarget.LootItem -> itemAccent(target.item.type)
    is InspectTarget.CraftRecipePreview -> rarity.color
    is InspectTarget.MarketItem -> rarity.color
    is InspectTarget.MaterialPreview -> Color(target.material.colorHex)
    is InspectTarget.AttachmentItem -> Color(target.attachment.rarityColorHex)
}

private fun itemAccent(type: LootType): Color = when (type) {
    LootType.FOOD -> EmeraldHud
    LootType.WATER -> CyanFloor
    LootType.MEDKIT -> Color(0xFFFCA5A5)
    LootType.CD -> PurpleRare
    LootType.PANTS, LootType.ARMOR -> CyanFloor
    LootType.MATERIAL -> AmberBright
    LootType.ATTACHMENT -> EmeraldHud
    LootType.WEAPON, LootType.SALVAGE -> AmberSignal
}
