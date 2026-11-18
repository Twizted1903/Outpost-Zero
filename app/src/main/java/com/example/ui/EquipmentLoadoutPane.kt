package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.game.engine.WorldCanvasRenderer.drawDogCompanion
import com.example.game.engine.WorldCanvasRenderer.drawOperatorRig
import com.example.game.model.BaseStashState
import com.example.game.model.OperatorProfile
import com.example.game.model.WeaponCategory
import com.example.game.model.WeaponId
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BunkerObsidian
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CyanFloor
import com.example.ui.theme.EmeraldHud
import com.example.ui.theme.OliveBorder

@Composable
fun EquipmentLoadoutPane(
    operator: OperatorProfile,
    availableWeapons: List<WeaponId>,
    activeWeapon: WeaponId,
    armorHp: Float,
    magAmmoMap: Map<WeaponId, Int>,
    reserveAmmoMap: Map<WeaponId, Int>,
    onSelectWeapon: (WeaponId) -> Unit,
    onInspectPants: () -> Unit,
    onInspectArmor: () -> Unit,
    onInspectWeapon: (WeaponId) -> Unit,
    stash: BaseStashState = BaseStashState(),
    modifier: Modifier = Modifier
) {
    val pantsRarity = ItemRarityResolver.forPants(operator.pantsLevel)
    val armorTier = operator.armorTier
    val armorRarity = ItemRarityResolver.forArmor(armorTier.level)

    LazyColumn(
        modifier = modifier.heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Operator Tactical Rig Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerObsidian, RoundedCornerShape(10.dp))
                    .border(1.dp, OliveBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFF141C16), RoundedCornerShape(8.dp))
                ) {
                    drawOperatorRig(size.width * 0.44f, size.height * 0.68f, -0.35f, operator, activeWeapon, 0f, 1.25f)
                    drawDogCompanion(size.width * 0.78f, size.height * 0.74f, -0.45f, operator.dogCoatColorHex)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "${operator.callsign} // ${operator.sex.label.uppercase()} ${operator.archetype.label.uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AmberSignal
                    )
                    Text(
                        text = "${operator.fullName} • ${operator.shirtStyle.label} • ${operator.hatStyle.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "HP ${operator.maxHp} • SPD ${(operator.speedMul * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldHud
                        )
                        Icon(Icons.Default.Pets, contentDescription = null, tint = EmeraldHud, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Solo K-9: ${operator.dogBreed}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberBright,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Equipped Ballistic Armor
        item {
            Text("BALLISTIC PROTECTION // ARMOR RIG", style = MaterialTheme.typography.labelSmall, color = armorRarity.color)
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, armorRarity.color.copy(alpha = 0.75f), RoundedCornerShape(10.dp))
                    .clickable(onClick = onInspectArmor)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(armorRarity.color.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                        .border(1.dp, armorRarity.color, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = armorRarity.color, modifier = Modifier.size(26.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(armorRarity.label, style = MaterialTheme.typography.labelSmall, color = armorRarity.color)
                    Text(armorTier.displayName, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = if (armorTier.level == 0) "0 Armor • Craft Lv.1 Vest at Workbench"
                        else "Shield ${armorHp.toInt()}/${armorTier.maxShieldHp.toInt()} • ${(armorTier.mitigation * 100).toInt()}% Mitigated",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanFloor
                    )
                }
                OutlinedButton(
                    onClick = onInspectArmor,
                    modifier = Modifier.height(48.dp).testTag("inspect_equip_armor_btn"),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AmberBright, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("INSPECT", style = MaterialTheme.typography.labelSmall, color = AmberBright)
                }
            }
        }

        // Equipped Pants & Safe Pockets Slot
        item {
            Text("LOWER GEAR // SAFE POCKETS", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, pantsRarity.color.copy(alpha = 0.75f), RoundedCornerShape(10.dp))
                    .clickable(onClick = onInspectPants)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(pantsRarity.color.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                            .border(1.dp, pantsRarity.color, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Checkroom, contentDescription = null, tint = pantsRarity.color, modifier = Modifier.size(26.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pantsRarity.label, style = MaterialTheme.typography.labelSmall, color = pantsRarity.color)
                        Text("Cargo Pants (Lv.${operator.pantsLevel})", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text("${operator.safePocketItems.size}/${operator.safePocketCapacity} Safe Pockets occupied", style = MaterialTheme.typography.bodyMedium, color = CyanFloor)
                    }
                    OutlinedButton(
                        onClick = onInspectPants,
                        modifier = Modifier.height(48.dp).testTag("inspect_equip_pants_btn"),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AmberBright, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("INSPECT", style = MaterialTheme.typography.labelSmall, color = AmberBright)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    repeat(operator.safePocketCapacity) { idx ->
                        val filled = idx < operator.safePocketItems.size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .background(if (filled) Color(0xFF0E3A4D) else BunkerObsidian, RoundedCornerShape(5.dp))
                                .border(1.dp, CyanFloor.copy(alpha = 0.7f), RoundedCornerShape(5.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (filled) Icons.Default.Lock else Icons.Default.Security,
                                contentDescription = null,
                                tint = CyanFloor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Equipped & Unlocked Weapons
        item {
            Text("ARSENAL // EQUIPPED WEAPONS (${availableWeapons.size})", style = MaterialTheme.typography.labelSmall, color = AmberSignal)
        }
        items(availableWeapons, key = { it.key }) { wpn ->
            val isEquipped = wpn == activeWeapon
            val rarity = ItemRarityResolver.forWeapon(wpn)
            val mag = magAmmoMap[wpn] ?: 0
            val reserve = reserveAmmoMap[wpn] ?: 0
            val equippedMods = stash.equippedAttachmentsFor(wpn)
            val effective = stash.effectiveStatsFor(wpn)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isEquipped) Color(0xFF1E3224) else BunkerSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, if (isEquipped) AmberSignal else rarity.color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable { onInspectWeapon(wpn) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(64.dp)
                        .height(38.dp)
                        .background(BunkerObsidian, RoundedCornerShape(6.dp))
                ) {
                    val cy = size.height * 0.5f
                    if (wpn.isMelee) {
                        drawRect(Color(wpn.accentColorHex), topLeft = Offset(10f, cy - 3f), size = Size(size.width - 20f, 6f))
                        drawRect(Color(wpn.tracerColorHex), topLeft = Offset(size.width * 0.52f, cy - 5f), size = Size(16f, 10f))
                    } else {
                        val bw = when (wpn.category) {
                            WeaponCategory.PISTOL -> size.width * 0.36f
                            WeaponCategory.SMG -> size.width * 0.46f
                            else -> size.width * 0.60f
                        }
                        drawRect(Color(wpn.accentColorHex), topLeft = Offset(8f, cy - 5f), size = Size(bw, 9f))
                        drawRect(Color(0xFF64748B), topLeft = Offset(8f + bw, cy - 3f), size = Size(14f, 4f))
                        drawCircle(Color(wpn.tracerColorHex), radius = 3f, center = Offset(8f + bw + 16f, cy - 1f))
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEquipped) "EQUIPPED • ${wpn.era.label.uppercase()}" else wpn.era.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEquipped) EmeraldHud else Color(wpn.era.accentHex),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (equippedMods.isNotEmpty()) {
                            Text(
                                text = "${equippedMods.size} MODS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(EmeraldHud, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = wpn.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (wpn.isMelee) {
                            "DMG ${wpn.damage} • Reach ${wpn.meleeReach.toInt()}m Melee"
                        } else {
                            "${wpn.caliber} • $mag/${effective.magSize} (+$reserve)${if (effective.suppressed) " • SUPP" else ""}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmberBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isEquipped) {
                    Button(
                        onClick = { onSelectWeapon(wpn) },
                        modifier = Modifier.height(48.dp).testTag("equip_weapon_btn_${wpn.key}"),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberSignal, contentColor = Color.Black)
                    ) {
                        Text("EQUIP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { onInspectWeapon(wpn) },
                    modifier = Modifier.height(48.dp).testTag("inspect_equip_weapon_${wpn.key}"),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Inspect ${wpn.displayName}", tint = AmberBright, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
