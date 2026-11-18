package com.example.data.cloud

import com.example.game.model.BaseStashState
import com.example.game.model.CraftingMaterial
import com.example.game.model.InventoryItem
import com.example.game.model.LootType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserCloudStash(
    val userId: String = "",
    val callsign: String = "SURVIVOR",
    val credits: Long = 350L,
    val matScrap: Long = 0L,
    val matGunpowder: Long = 0L,
    val matCloth: Long = 0L,
    val matElectronics: Long = 0L,
    val matHerbs: Long = 0L,
    val ammoM4: Long = 0L,
    val ammoSmg: Long = 0L,
    val ammoShotgun: Long = 0L,
    val maxPantsLevel: Long = 1L,
    val unlockedWeaponKeys: List<String> = listOf("baton"),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toBaseStashState(activeOperatorId: String, foodStock: Int, waterStock: Int, medsStock: Int): BaseStashState =
        BaseStashState(
            credits = credits.toInt().coerceAtLeast(0),
            foodStock = foodStock,
            waterStock = waterStock,
            medsStock = medsStock,
            activeOperatorId = activeOperatorId,
            scrapMetal = matScrap.toInt().coerceAtLeast(0),
            gunpowder = matGunpowder.toInt().coerceAtLeast(0),
            tacticalCloth = matCloth.toInt().coerceAtLeast(0),
            electronicParts = matElectronics.toInt().coerceAtLeast(0),
            medicalHerbs = matHerbs.toInt().coerceAtLeast(0),
            ammoM4 = ammoM4.toInt().coerceAtLeast(0),
            ammoSmg = ammoSmg.toInt().coerceAtLeast(0),
            ammoShotgun = ammoShotgun.toInt().coerceAtLeast(0),
            maxPantsLevel = maxPantsLevel.toInt().coerceIn(1, 4),
            unlockedWeaponKeys = unlockedWeaponKeys.ifEmpty { listOf("baton") }.toSet()
        )
}

@IgnoreExtraProperties
data class MarketListing(
    @DocumentId val id: String = "",
    val sellerId: String = "",
    val sellerCallsign: String = "",
    val buyerId: String = "",
    val category: String = "",
    val itemName: String = "",
    val itemDetail: String = "",
    val rarityLabel: String = "",
    val weaponKey: String = "",
    val materialKey: String = "",
    val cdId: Long = 0L,
    val pantsLevel: Long = 0L,
    val value: Long = 1L,
    val quantity: Long = 1L,
    val priceCredits: Long = 100L,
    val status: String = "ACTIVE",
    val payoutClaimed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toInventoryItemPreview(): InventoryItem {
        val lootType = runCatching { LootType.valueOf(category) }.getOrDefault(LootType.SALVAGE)
        return InventoryItem(
            uid = "mkt_$id",
            type = lootType,
            name = itemName,
            detail = itemDetail,
            value = value.toInt(),
            cdId = cdId.toInt().takeIf { it > 0 },
            weaponKey = weaponKey.takeIf { it.isNotBlank() },
            pantsLevel = pantsLevel.toInt().takeIf { it > 0 },
            materialKey = materialKey.takeIf { it.isNotBlank() },
            quantity = quantity.toInt().coerceAtLeast(1)
        )
    }

    fun materialFieldKey(): String? = when (CraftingMaterial.fromKey(materialKey)) {
        CraftingMaterial.SCRAP_METAL -> "matScrap"
        CraftingMaterial.GUNPOWDER -> "matGunpowder"
        CraftingMaterial.TACTICAL_CLOTH -> "matCloth"
        CraftingMaterial.ELECTRONIC_PARTS -> "matElectronics"
        CraftingMaterial.MEDICAL_HERBS -> "matHerbs"
        else -> null
    }
}

data class MarketPurchaseOutcome(
    val listing: MarketListing,
    val updatedBuyerCredits: Int
)

@IgnoreExtraProperties
data class KillFeedEvent(
    @DocumentId val id: String = "",
    val killerId: String = "",
    val killerCallsign: String = "",
    val victimId: String = "",
    val victimCallsign: String = "",
    val weaponKey: String = "",
    val weaponName: String = "",
    val zoneName: String = "",
    val feedScope: String = "PUBLIC",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class KillFeedEntry(
    val id: String,
    val killerCallsign: String,
    val victimCallsign: String,
    val weaponName: String,
    val weaponKey: String = "",
    val zoneName: String = "",
    val timestampMs: Long = System.currentTimeMillis()
)
