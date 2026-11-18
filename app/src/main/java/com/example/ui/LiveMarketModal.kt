package com.example.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.cloud.MarketListing
import com.example.game.model.BaseStashState
import com.example.game.model.CdCatalog
import com.example.game.model.CraftingMaterial
import com.example.game.model.InventoryItem
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

sealed interface MarketUiState {
    data object Loading : MarketUiState
    data class Success(val listings: List<MarketListing>) : MarketUiState
    data class Error(val message: String) : MarketUiState
}

enum class MarketSellSource {
    STASH_MATERIAL,
    STASH_WEAPON,
    STASH_PANTS,
    BACKPACK_ITEM,
    SAFE_ITEM,
    COLLECTED_CD
}

data class SellableMarketCandidate(
    val id: String,
    val source: MarketSellSource,
    val category: String,
    val itemName: String,
    val itemDetail: String,
    val rarityLabel: String,
    val weaponKey: String = "",
    val materialKey: String = "",
    val cdId: Int = 0,
    val pantsLevel: Int = 0,
    val value: Int = 1,
    val maxQuantity: Int = 1,
    val suggestedPrice: Int = 100,
    val inventoryItemUid: String? = null
)

@Composable
fun LiveMarketModal(
    currentUserId: String,
    stash: BaseStashState,
    operator: OperatorProfile?,
    collectedCds: Set<Int>,
    activeListingsState: MarketUiState,
    myListingsState: MarketUiState,
    marketBusy: Boolean,
    playingCdId: Int?,
    onPlayCd: (Int) -> Unit,
    onBuyListing: (MarketListing) -> Unit,
    onCreateListing: (SellableMarketCandidate, quantity: Int, priceCredits: Int) -> Unit,
    onCancelListing: (MarketListing) -> Unit,
    onClaimPayout: (MarketListing) -> Unit,
    onClose: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) }
    var browseCategory by remember { mutableStateOf("ALL") }
    var inspectedListing by remember { mutableStateOf<MarketListing?>(null) }

    ModalScrim(onDismiss = onClose) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = EmeraldHud)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "LIVE PLAYER MARKET",
                            style = MaterialTheme.typography.headlineMedium,
                            color = EmeraldHud
                        )
                        CredBadge(amount = stash.credits)
                    }
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp).testTag("market_close_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close market", tint = Color.White)
                }
            }

            if (currentUserId.isBlank()) {
                Text(
                    text = "OFFLINE MODE — Sign-in is disabled, so live player trading is paused. Use the Crafting Workbench, Missions, or Favors to earn CRED and gear.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmberBright,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AmberBright.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .border(1.dp, AmberBright.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("market_tab_browse"),
                    label = { Text("BROWSE", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                FilterChip(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("market_tab_sell"),
                    label = { Text("SELL GEAR", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                val myCount = (myListingsState as? MarketUiState.Success)?.listings?.size ?: 0
                FilterChip(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("market_tab_mine"),
                    label = { Text("MINE ($myCount)", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }

            when (activeTab) {
                0 -> {
                    val categories = listOf("ALL", "WEAPON", "ARMOR", "PANTS", "MATERIAL", "SUPPLIES", "CD")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = browseCategory == cat,
                                onClick = { browseCategory = cat },
                                modifier = Modifier.heightIn(min = 48.dp),
                                label = { Text(cat) }
                            )
                        }
                    }

                    when (activeListingsState) {
                        MarketUiState.Loading -> MarketLoadingBox("Connecting to live player exchange...")
                        is MarketUiState.Error -> MarketErrorBox(activeListingsState.message)
                        is MarketUiState.Success -> {
                            val filtered = remember(activeListingsState.listings, browseCategory) {
                                activeListingsState.listings.filter { listing ->
                                    when (browseCategory) {
                                        "ALL" -> true
                                        "SUPPLIES" -> listing.category in setOf("MEDKIT", "FOOD", "WATER")
                                        else -> listing.category == browseCategory
                                    }
                                }
                            }
                            if (filtered.isEmpty()) {
                                MarketEmptyBox("No active listings in this category yet. Every gun & item here is 100% player-made from DeLaine base materials — craft gear at your Workbench to post the first offer.")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f, fill = false).heightIn(max = 350.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filtered, key = { it.id }) { listing ->
                                        val isMine = listing.sellerId == currentUserId
                                        val canAfford = stash.credits >= listing.priceCredits
                                        val rarity = ItemRarityResolver.forRarityLabel(listing.rarityLabel)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(BunkerSurface, RoundedCornerShape(10.dp))
                                                .border(1.dp, rarity.color.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                                .clickable { inspectedListing = listing }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${rarity.label} • ${listing.sellerCallsign}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = rarity.color,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (listing.quantity > 1L) "${listing.itemName} (x${listing.quantity})" else listing.itemName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (listing.itemDetail.isNotBlank()) {
                                                    Text(
                                                        text = listing.itemDetail,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = EmeraldHud,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = { inspectedListing = listing },
                                                modifier = Modifier.size(48.dp).testTag("market_inspect_${listing.id}")
                                            ) {
                                                Icon(Icons.Default.Search, contentDescription = "Inspect ${listing.itemName}", tint = AmberBright)
                                            }
                                            Button(
                                                onClick = { onBuyListing(listing) },
                                                enabled = !marketBusy && !isMine && canAfford,
                                                modifier = Modifier.heightIn(min = 48.dp).testTag("market_buy_${listing.id}"),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = EmeraldHud,
                                                    contentColor = Color.Black,
                                                    disabledContainerColor = Color(0xFF28332C),
                                                    disabledContentColor = Color(0xFF7D9184)
                                                ),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                val label = when {
                                                    isMine -> "YOURS"
                                                    !canAfford -> "${listing.priceCredits} CRED"
                                                    else -> "BUY ${listing.priceCredits} CRED"
                                                }
                                                Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    SellGearPane(
                        stash = stash,
                        operator = operator,
                        collectedCds = collectedCds,
                        marketBusy = marketBusy || currentUserId.isBlank(),
                        onCreateListing = { candidate, qty, price ->
                            onCreateListing(candidate, qty, price)
                            activeTab = 2
                        },
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                else -> {
                    when (myListingsState) {
                        MarketUiState.Loading -> MarketLoadingBox("Loading your market listings...")
                        is MarketUiState.Error -> MarketErrorBox(myListingsState.message)
                        is MarketUiState.Success -> {
                            if (myListingsState.listings.isEmpty()) {
                                MarketEmptyBox("You haven't posted any listings yet. Switch to SELL GEAR to list crafted items or base materials.")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f, fill = false).heightIn(max = 380.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(myListingsState.listings, key = { it.id }) { listing ->
                                        val rarity = ItemRarityResolver.forRarityLabel(listing.rarityLabel)
                                        val statusColor = when (listing.status) {
                                            "SOLD" -> if (listing.payoutClaimed) CyanFloor else EmeraldHud
                                            "CANCELLED" -> CrimsonAlert
                                            else -> AmberSignal
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(BunkerSurface, RoundedCornerShape(10.dp))
                                                .border(1.dp, statusColor.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${listing.status} • ${rarity.label}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = statusColor
                                                )
                                                Text(
                                                    text = if (listing.quantity > 1L) "${listing.itemName} (x${listing.quantity})" else listing.itemName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Price: ${listing.priceCredits} CRED",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AmberBright
                                                )
                                            }

                                            when {
                                                listing.status == "ACTIVE" -> {
                                                    OutlinedButton(
                                                        onClick = { onCancelListing(listing) },
                                                        enabled = !marketBusy,
                                                        modifier = Modifier.heightIn(min = 48.dp).testTag("market_cancel_${listing.id}")
                                                    ) {
                                                        Text("CANCEL", style = MaterialTheme.typography.labelSmall, color = CrimsonAlert)
                                                    }
                                                }
                                                listing.status == "SOLD" && !listing.payoutClaimed -> {
                                                    Button(
                                                        onClick = { onClaimPayout(listing) },
                                                        enabled = !marketBusy,
                                                        modifier = Modifier.heightIn(min = 48.dp).testTag("market_claim_${listing.id}"),
                                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldHud, contentColor = Color.Black)
                                                    ) {
                                                        Text("CLAIM ${listing.priceCredits} CRED", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                                listing.status == "SOLD" && listing.payoutClaimed -> {
                                                    Text("SETTLED", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                                                }
                                                else -> {
                                                    Text("CLOSED", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inspectedListing?.let { listing ->
        ItemInspectorModal(
            target = InspectTarget.MarketItem(listing),
            currentPantsLevel = operator?.pantsLevel ?: stash.maxPantsLevel,
            safeSpaceLeft = operator?.let { it.safePocketCapacity - it.safePocketItems.size } ?: 0,
            playingCdId = playingCdId,
            onPlayCd = onPlayCd,
            onClose = { inspectedListing = null }
        )
    }
}

@Composable
private fun SellGearPane(
    stash: BaseStashState,
    operator: OperatorProfile?,
    collectedCds: Set<Int>,
    marketBusy: Boolean,
    onCreateListing: (SellableMarketCandidate, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val candidates = remember(stash, operator, collectedCds) {
        buildSellableCandidates(stash, operator, collectedCds)
    }
    var selectedId by remember(candidates) { mutableStateOf(candidates.firstOrNull()?.id) }
    val selected = candidates.firstOrNull { it.id == selectedId } ?: candidates.firstOrNull()

    var quantity by remember(selected?.id) { mutableIntStateOf(1) }
    var priceText by remember(selected?.id) { mutableStateOf((selected?.suggestedPrice ?: 50).toString()) }

    if (candidates.isEmpty()) {
        MarketEmptyBox("No sellable gear, materials, or CDs yet. Scavenge DeLaine or craft equipment at the Workbench first.")
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("SELECT ITEM FROM STASH OR BAG", style = MaterialTheme.typography.labelSmall, color = AmberSignal)
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false).heightIn(max = 210.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(candidates, key = { it.id }) { cand ->
                val isSel = cand.id == selected?.id
                val rarity = ItemRarityResolver.forRarityLabel(cand.rarityLabel)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSel) Color(0xFF1E3224) else BunkerSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isSel) EmeraldHud else OliveBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            selectedId = cand.id
                            quantity = 1
                            priceText = cand.suggestedPrice.toString()
                        }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cand.rarityLabel, style = MaterialTheme.typography.labelSmall, color = rarity.color)
                        Text(cand.itemName, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(cand.itemDetail, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFBFD3C7), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (cand.maxQuantity > 1) {
                        Text("OWN: ${cand.maxQuantity}", style = MaterialTheme.typography.labelSmall, color = AmberBright)
                    }
                }
            }
        }

        if (selected != null) {
            HorizontalDivider(color = OliveBorder)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerObsidian, RoundedCornerShape(10.dp))
                    .border(1.dp, OliveBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selected.maxQuantity > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("QUANTITY: $quantity / ${selected.maxQuantity}", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { if (quantity > 1) quantity-- },
                                enabled = quantity > 1,
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) { Text("-1") }
                            OutlinedButton(
                                onClick = { if (quantity < selected.maxQuantity) quantity++ },
                                enabled = quantity < selected.maxQuantity,
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) { Text("+1") }
                            OutlinedButton(
                                onClick = { quantity = selected.maxQuantity.coerceAtMost(50) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) { Text("MAX") }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { raw ->
                            priceText = raw.filter { it.isDigit() }.take(7)
                        },
                        label = { Text("Price (CRED)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("market_sell_price_input")
                    )
                    val parsedPrice = priceText.toIntOrNull() ?: 0
                    Button(
                        onClick = {
                            if (parsedPrice > 0) {
                                onCreateListing(selected, quantity.coerceIn(1, selected.maxQuantity), parsedPrice)
                            }
                        },
                        enabled = !marketBusy && parsedPrice in 1..1_000_000,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("market_post_listing_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberSignal, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("POST LISTING", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun buildSellableCandidates(
    stash: BaseStashState,
    operator: OperatorProfile?,
    collectedCds: Set<Int>
): List<SellableMarketCandidate> {
    val out = mutableListOf<SellableMarketCandidate>()

    stash.unlockedWeaponKeys
        .filter { it != WeaponId.KNIFE.key }
        .forEach { key ->
            val w = WeaponId.entries.firstOrNull { it.key == key } ?: return@forEach
            val rarity = ItemRarityResolver.forWeapon(w)
            val est = w.damage * 4 + 120
            out.add(
                SellableMarketCandidate(
                    id = "stash_wpn_$key",
                    source = MarketSellSource.STASH_WEAPON,
                    category = "WEAPON",
                    itemName = w.displayName,
                    itemDetail = "${w.caliber} • ${w.category.label}",
                    rarityLabel = rarity.label,
                    weaponKey = w.key,
                    value = est,
                    suggestedPrice = est
                )
            )
        }

    if (stash.maxPantsLevel >= 2) {
        val lvl = stash.maxPantsLevel
        val rarity = ItemRarityResolver.forPants(lvl)
        out.add(
            SellableMarketCandidate(
                id = "stash_pants_$lvl",
                source = MarketSellSource.STASH_PANTS,
                category = "PANTS",
                itemName = "Cargo Pants (Lv.$lvl)",
                itemDetail = "$lvl Concealed Safe Pockets",
                rarityLabel = rarity.label,
                pantsLevel = lvl,
                value = lvl * 95,
                suggestedPrice = lvl * 95
            )
        )
    }

    CraftingMaterial.entries.forEach { mat ->
        val qty = stash.materialCount(mat)
        if (qty > 0) {
            out.add(
                SellableMarketCandidate(
                    id = "stash_mat_${mat.key}",
                    source = MarketSellSource.STASH_MATERIAL,
                    category = "MATERIAL",
                    itemName = mat.displayName,
                    itemDetail = "Crafting Resource • ${mat.oakridgeSources}",
                    rarityLabel = mat.rarityLabel,
                    materialKey = mat.key,
                    value = 18,
                    maxQuantity = qty.coerceAtMost(50),
                    suggestedPrice = 25
                )
            )
        }
    }

    fun addBagItems(items: List<InventoryItem>, src: MarketSellSource, prefix: String) {
        items.forEach { item ->
            val cat = when (item.type) {
                LootType.WEAPON -> "WEAPON"
                LootType.PANTS -> "PANTS"
                LootType.ARMOR -> "ARMOR"
                LootType.MEDKIT -> "MEDKIT"
                LootType.FOOD -> "FOOD"
                LootType.WATER -> "WATER"
                LootType.MATERIAL -> "MATERIAL"
                LootType.CD -> "CD"
                LootType.SALVAGE, LootType.ATTACHMENT -> return@forEach
            }
            val rarity = ItemRarityResolver.forInventoryItem(item)
            out.add(
                SellableMarketCandidate(
                    id = "${prefix}_${item.uid}",
                    source = src,
                    category = cat,
                    itemName = item.name,
                    itemDetail = item.detail,
                    rarityLabel = rarity.label,
                    weaponKey = item.weaponKey.orEmpty(),
                    materialKey = item.materialKey.orEmpty(),
                    cdId = item.cdId ?: 0,
                    pantsLevel = item.pantsLevel ?: 0,
                    value = if (item.type == LootType.ARMOR) (item.armorLevel ?: 1) else item.value,
                    maxQuantity = 1,
                    suggestedPrice = (item.value.coerceAtLeast(10) * 2).coerceAtLeast(25),
                    inventoryItemUid = item.uid
                )
            )
        }
    }
    if (operator != null) {
        addBagItems(operator.backpackItems, MarketSellSource.BACKPACK_ITEM, "bag")
        addBagItems(operator.safePocketItems, MarketSellSource.SAFE_ITEM, "safe")
    }

    collectedCds.sorted().forEach { cdId ->
        val cd = CdCatalog.getById(cdId)
        out.add(
            SellableMarketCandidate(
                id = "rack_cd_$cdId",
                source = MarketSellSource.COLLECTED_CD,
                category = "CD",
                itemName = "${cd.catalogTag} ${cd.artist}",
                itemDetail = "${cd.album} • ${cd.genre.label}",
                rarityLabel = cd.rarity.label.uppercase(),
                cdId = cdId,
                value = cd.rarity.ordinal * 75 + 50,
                suggestedPrice = cd.rarity.ordinal * 80 + 60
            )
        )
    }

    return out
}

@Composable
private fun MarketLoadingBox(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BunkerObsidian, RoundedCornerShape(10.dp))
            .border(1.dp, OliveBorder, RoundedCornerShape(10.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = EmeraldHud, strokeWidth = 2.dp)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = EmeraldHud)
    }
}

@Composable
private fun MarketErrorBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CrimsonAlert.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
            .border(1.dp, CrimsonAlert.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = CrimsonAlert)
    }
}

@Composable
private fun MarketEmptyBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BunkerObsidian, RoundedCornerShape(10.dp))
            .border(1.dp, OliveBorder, RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFBFD3C7))
    }
}
