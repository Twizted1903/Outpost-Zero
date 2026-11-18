package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.game.model.AttachmentSlot
import com.example.game.model.BaseStashState
import com.example.game.model.HeritageVaultCatalog
import com.example.game.model.WeaponEra
import com.example.game.model.WeaponId
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BunkerObsidian
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CyanFloor
import com.example.ui.theme.EmeraldHud
import com.example.ui.theme.OliveBorder

@Composable
fun HeritageVaultPane(
    stash: BaseStashState,
    onClaimEraMilestone: (WeaponEra) -> Unit,
    onEquipAttachment: ((WeaponId, String) -> Unit)?,
    onUnequipSlot: ((WeaponId, AttachmentSlot) -> Unit)?,
    modifier: Modifier = Modifier
) {
    var selectedEra by remember { mutableStateOf<WeaponEra?>(null) }
    var inspectedWeapon by remember { mutableStateOf<WeaponId?>(null) }

    val totalHistoric = WeaponId.historicalCollectibles.size
    val collectedTotal = stash.collectedHistoricKeys.size
    val erasToShow = remember(selectedEra) {
        if (selectedEra == null) WeaponEra.historicalEras else listOf(selectedEra!!)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Overall Vault Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BunkerObsidian, RoundedCornerShape(10.dp))
                .border(1.dp, AmberSignal.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AmberBright, modifier = Modifier.size(18.dp))
                    Text(
                        text = "HERITAGE GUN VAULT",
                        style = MaterialTheme.typography.titleMedium,
                        color = AmberSignal
                    )
                }
                Text(
                    text = "$collectedTotal / $totalHistoric COLLECTED",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldHud,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { (collectedTotal.toFloat() / totalHistoric.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = AmberSignal,
                trackColor = Color(0xFF1E293B)
            )
            Text(
                text = "100% Player-Built Arsenal: Forge frontier, WWI, WWII & Cold War firearms at your Workbench from DeLaine base materials, or buy player-crafted relics on the Live Market.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB5C7BD)
            )
        }

        // Era Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = selectedEra == null,
                    onClick = { selectedEra = null },
                    modifier = Modifier.heightIn(min = 44.dp),
                    label = { Text("ALL ($collectedTotal/$totalHistoric)", style = MaterialTheme.typography.labelSmall) }
                )
            }
            items(WeaponEra.historicalEras) { era ->
                val eraGuns = HeritageVaultCatalog.collectiblesFor(era)
                val eraOwned = eraGuns.count { it.key in stash.collectedHistoricKeys }
                FilterChip(
                    selected = selectedEra == era,
                    onClick = { selectedEra = era },
                    modifier = Modifier.heightIn(min = 44.dp).testTag("vault_era_${era.name}"),
                    label = { Text("${era.label} ($eraOwned/${eraGuns.size})", style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false)
                .heightIn(max = 310.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            erasToShow.forEach { era ->
                val guns = HeritageVaultCatalog.collectiblesFor(era)
                val collectedInEra = guns.count { it.key in stash.collectedHistoricKeys }
                val milestone = HeritageVaultCatalog.milestoneFor(era)
                val alreadyClaimed = era.name in stash.claimedEraMilestones
                val canClaim = collectedInEra >= guns.size && !alreadyClaimed && milestone != null
                val eraColor = Color(era.accentHex)

                item(key = "era_hdr_${era.name}") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BunkerSurface, RoundedCornerShape(10.dp))
                            .border(1.dp, eraColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(era.badge, style = MaterialTheme.typography.titleMedium, color = eraColor)
                                if (milestone != null) {
                                    Text(
                                        text = "${milestone.plaqueTitle} • +${milestone.rewardCred} CRED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberBright
                                    )
                                }
                            }
                            Text(
                                text = "$collectedInEra / ${guns.size}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (collectedInEra >= guns.size) EmeraldHud else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (collectedInEra.toFloat() / guns.size.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                            color = eraColor,
                            trackColor = BunkerObsidian
                        )
                        if (milestone != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val matSummary = milestone.rewardMats.entries.joinToString(" • ") { "+${it.value} ${it.key.shortName}" }
                                Text(
                                    text = matSummary,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanFloor,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Button(
                                    onClick = { onClaimEraMilestone(era) },
                                    enabled = canClaim,
                                    modifier = Modifier.heightIn(min = 44.dp).testTag("claim_era_${era.name}"),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (alreadyClaimed) Color(0xFF1E3224) else EmeraldHud,
                                        contentColor = if (alreadyClaimed) EmeraldHud else Color.Black,
                                        disabledContainerColor = Color(0xFF28332C),
                                        disabledContentColor = Color(0xFF83968A)
                                    )
                                ) {
                                    Text(
                                        text = when {
                                            alreadyClaimed -> "CLAIMED"
                                            canClaim -> "CLAIM REWARD"
                                            else -> "NEED ${guns.size - collectedInEra} MORE"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                items(guns, key = { "vault_wpn_${it.key}" }) { wpn ->
                    val collected = wpn.key in stash.collectedHistoricKeys
                    val modCount = stash.equippedAttachmentsFor(wpn).size
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (collected) Color(0xFF192920) else BunkerObsidian, RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                if (collected) eraColor.copy(alpha = 0.65f) else OliveBorder.copy(alpha = 0.45f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = collected) { inspectedWeapon = wpn }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (collected) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = if (collected) "Collected" else "Missing relic",
                            tint = if (collected) EmeraldHud else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${wpn.displayName} (${wpn.yearIntroduced})",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (collected) Color.White else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (collected) {
                                    "${wpn.caliber} • ${wpn.countryOrigin} • $modCount Mods Installed"
                                } else {
                                    "${wpn.caliber} • Player-Made Only: Craft at Outpost Workbench from DeLaine parts or buy player-made on Live Market (${wpn.era.yearSpan})"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (collected) CyanFloor else Color(0xFF6E8077),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (collected) {
                            OutlinedButton(
                                onClick = { inspectedWeapon = wpn },
                                modifier = Modifier.heightIn(min = 48.dp).testTag("vault_inspect_${wpn.key}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = AmberBright, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("3D / MOD", style = MaterialTheme.typography.labelSmall, color = AmberBright)
                            }
                        }
                    }
                }
            }
        }
    }

    inspectedWeapon?.let { wpn ->
        ItemInspectorModal(
            target = InspectTarget.EquippedWeapon(
                weapon = wpn,
                isActive = false,
                magAmmo = wpn.magSize,
                reserveAmmo = stash.ammoForWeapon(wpn)
            ),
            currentPantsLevel = stash.maxPantsLevel,
            safeSpaceLeft = 0,
            playingCdId = null,
            stash = stash,
            onPlayCd = {},
            onEquipAttachment = onEquipAttachment,
            onUnequipSlot = onUnequipSlot,
            onClose = { inspectedWeapon = null }
        )
    }
}
