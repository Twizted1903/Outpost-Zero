package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operators")
data class OperatorEntity(
    @PrimaryKey val id: String,
    val slotIndex: Int,
    val callsign: String,
    val fullName: String,
    val archetypeName: String,
    val headgearName: String,
    val skinHex: Long,
    val shirtHex: Long,
    val vestHex: Long,
    val packHex: Long,
    val pantsLevel: Int,
    val armorLevel: Int = 0,
    val dogFound: Boolean = true,
    val kiaCount: Int,
    val isKia: Boolean,
    val kiaTimestamp: Long,
    val infirmaryUntil: Long,
    val safePocketJson: String,
    val backpackJson: String,
    val createdAt: Long,
    val downedAtX: Float = 0f,
    val downedAtY: Float = 0f,
    val downedZoneId: String = "lz_highway",
    val downedElevation: Int = 0,
    val stolenItemsJson: String = "[]",
    val scavengeChecksDone: Int = 0,
    val isPermadead: Boolean = false,
    val sexName: String = "MALE",
    val skinToneName: String = "LIGHT_TAN",
    val shirtStyleName: String = "RED_FLANNEL",
    val pantsStyleName: String = "BLUE_JEANS",
    val shoeStyleName: String = "WORK_BOOTS",
    val hatStyleName: String = "BALL_CAP",
    val glassesStyleName: String = "NONE",
    val glovesStyleName: String = "NONE",
    val pairedDogBreed: String = "German Shepherd",
    val pairedDogCoatHex: Long = 0xFF7A5A3A
)

@Entity(tableName = "base_stash")
data class BaseStashEntity(
    @PrimaryKey val id: Int = 1,
    val credits: Int,
    val foodStock: Int,
    val waterStock: Int,
    val medsStock: Int,
    val activeOperatorId: String,
    val scrapMetal: Int = 6,
    val gunpowder: Int = 2,
    val tacticalCloth: Int = 5,
    val electronicParts: Int = 0,
    val medicalHerbs: Int = 3,
    val materialsJson: String = "{}",
    val ammoM4: Int = 0,
    val ammoSmg: Int = 0,
    val ammoShotgun: Int = 0,
    val weaponAmmoJson: String = "{}",
    val maxPantsLevel: Int = 1,
    val maxArmorLevel: Int = 0,
    val bunkLevel: Int = 1,
    val storageLevel: Int = 1,
    val helipadLevel: Int = 0,
    val reloadingGearLevel: Int = 0,
    val unlockedWeaponKeysJson: String = """["knife"]""",
    val activeMissionId: String = "hq_1",
    val activeMissionProgress: Int = 0,
    val completedMissionsJson: String = "[]",
    val activeFavorId: String = "",
    val activeFavorProgress: Int = 0,
    val completedFavorsJson: String = "[]",
    val baseLayoutJson: String = "",
    val ownedAttachmentsJson: String = "[]",
    val weaponAttachmentsJson: String = "{}",
    val collectedHistoricWeaponsJson: String = "[]",
    val claimedEraMilestonesJson: String = "[]"
)

@Entity(tableName = "collected_cds")
data class CollectedCdEntity(
    @PrimaryKey val cdId: Int,
    val foundAt: Long,
    val foundLocation: String
)
