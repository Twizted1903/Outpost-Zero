package com.example.game.model

enum class ScavAiState {
    PATROL,
    INVESTIGATE,
    TAKE_COVER,
    ENGAGE,
    RETREAT
}

data class ScavEncampment(
    val id: String,
    val name: String,
    val districtName: String,
    val dangerTier: Int,
    val centerX: Float,
    val centerY: Float,
    val radius: Float = 148f,
    val factionTag: String = "SCAV FOOTHOLD",
    val colorHex: Long = 0xFFD97706
)

data class DeadBodyMarker(
    val id: String,
    val scavId: String,
    val displayName: String,
    val campId: String,
    val districtName: String,
    val x: Float,
    val y: Float,
    val elevation: Int,
    val diedAtMs: Long,
    val killedByPlayer: Boolean,
    var discoveredByCamp: Boolean = false
)

data class SlainScavRecord(
    val scavId: String,
    val displayName: String,
    val campId: String,
    val campName: String,
    val districtName: String,
    val killerCallsign: String,
    val weaponKey: String,
    val killedAtMs: Long,
    val witnessedBySquad: Boolean
)

data class ScavCharacterDef(
    val scavId: String,
    val displayName: String,
    val roleLabel: String,
    val campId: String,
    val weapon: WeaponId,
    val shirtColorHex: Long,
    val vestColorHex: Long,
    val skinColorHex: Long,
    val baseHp: Float,
    val patrolOffsetX: Float,
    val patrolOffsetY: Float
)

data class ScavKinRelation(
    val subjectScavId: String,
    val subjectName: String,
    val kinScavId: String,
    val relationOfKinToSubject: String,
    val relationOfSubjectToKin: String
)

data class ResolvedKinInquiry(
    val kinDef: ScavCharacterDef,
    val slainRecord: SlainScavRecord,
    val relationshipToSlain: String
)

object ScavKinshipRoster {

    val camps: List<ScavEncampment> = listOf(
        ScavEncampment(
            id = "camp_citrus_overpass",
            name = "Citrus Overpass Camp",
            districtName = "CITRUS CITY & OVERPASS",
            dangerTier = 1,
            centerX = 440f,
            centerY = 1650f,
            radius = 148f,
            factionTag = "RIDGE SCAVS",
            colorHex = 0xFF22C55E
        ),
        ScavEncampment(
            id = "camp_westside_lot",
            name = "Westside Salvage Foothold",
            districtName = "WESTSIDE NEIGHBORHOOD",
            dangerTier = 1,
            centerX = -1840f,
            centerY = 240f,
            radius = 145f,
            factionTag = "SALVAGE CREW",
            colorHex = 0xFF34D399
        ),
        ScavEncampment(
            id = "camp_stanton_quad",
            name = "Stanton East Gate Foothold",
            districtName = "STANTON UNIVERSITY",
            dangerTier = 2,
            centerX = 980f,
            centerY = -1220f,
            radius = 152f,
            factionTag = "QUAD RAIDERS",
            colorHex = 0xFF38BDF8
        ),
        ScavEncampment(
            id = "camp_downtown_yard",
            name = "Woodbine Loading Yard",
            districtName = "DOWNTOWN WOODBINE",
            dangerTier = 3,
            centerX = -920f,
            centerY = 340f,
            radius = 158f,
            factionTag = "IRON YARD",
            colorHex = 0xFFF59E0B
        ),
        ScavEncampment(
            id = "camp_airfield_fob",
            name = "Airfield Runway Foothold",
            districtName = "DELAINE AIRFIELD & LAKE HELENA",
            dangerTier = 3,
            centerX = 1620f,
            centerY = -1520f,
            radius = 162f,
            factionTag = "TARMAC MILITIA",
            colorHex = 0xFFFB923C
        ),
        ScavEncampment(
            id = "camp_cobalt_ridge",
            name = "Cobalt Springs Redoubt",
            districtName = "COBALT SPRINGS STATE PARK",
            dangerTier = 4,
            centerX = -2275f,
            centerY = -560f,
            radius = 156f,
            factionTag = "BLUFF SYNDICATE",
            colorHex = 0xFFEF4444
        )
    )

    val namedScavs: List<ScavCharacterDef> = listOf(
        // Citrus Overpass Camp (Tier 1)
        ScavCharacterDef(
            scavId = "scav_james_vance",
            displayName = "James Vance",
            roleLabel = "Supply Runner",
            campId = "camp_citrus_overpass",
            weapon = WeaponId.GLOCK_19,
            shirtColorHex = 0xFF6B4226,
            vestColorHex = 0xFF334155,
            skinColorHex = 0xFFD9A37C,
            baseHp = 95f,
            patrolOffsetX = -56f,
            patrolOffsetY = -28f
        ),
        ScavCharacterDef(
            scavId = "scav_sienna_mercer",
            displayName = "Sienna Mercer",
            roleLabel = "Lookout",
            campId = "camp_citrus_overpass",
            weapon = WeaponId.WINCHESTER_1873,
            shirtColorHex = 0xFF3F5138,
            vestColorHex = 0xFF475569,
            skinColorHex = 0xFFE2B493,
            baseHp = 90f,
            patrolOffsetX = 58f,
            patrolOffsetY = 34f
        ),

        // Westside Salvage Foothold (Tier 1)
        ScavCharacterDef(
            scavId = "scav_elena_vance",
            displayName = "Elena Vance",
            roleLabel = "Spotter",
            campId = "camp_westside_lot",
            weapon = WeaponId.MP5,
            shirtColorHex = 0xFF4C394E,
            vestColorHex = 0xFF334155,
            skinColorHex = 0xFFE4B796,
            baseHp = 100f,
            patrolOffsetX = -52f,
            patrolOffsetY = 36f
        ),
        ScavCharacterDef(
            scavId = "scav_marcus_bell",
            displayName = "Marcus Bell",
            roleLabel = "Breacher",
            campId = "camp_westside_lot",
            weapon = WeaponId.REMINGTON_870,
            shirtColorHex = 0xFF374151,
            vestColorHex = 0xFF78350F,
            skinColorHex = 0xFF9C6644,
            baseHp = 110f,
            patrolOffsetX = 56f,
            patrolOffsetY = -30f
        ),

        // Stanton East Gate Foothold (Tier 2)
        ScavCharacterDef(
            scavId = "scav_maya_vance",
            displayName = "Maya Vance",
            roleLabel = "Field Medic",
            campId = "camp_stanton_quad",
            weapon = WeaponId.CZ_SCORPION,
            shirtColorHex = 0xFF1E3A5F,
            vestColorHex = 0xFF475569,
            skinColorHex = 0xFFD9A37C,
            baseHp = 105f,
            patrolOffsetX = -58f,
            patrolOffsetY = -36f
        ),
        ScavCharacterDef(
            scavId = "scav_tessa_cole",
            displayName = "Tessa Cole",
            roleLabel = "Flanker",
            campId = "camp_stanton_quad",
            weapon = WeaponId.M4,
            shirtColorHex = 0xFF43382D,
            vestColorHex = 0xFF1F2937,
            skinColorHex = 0xFFC79570,
            baseHp = 115f,
            patrolOffsetX = 60f,
            patrolOffsetY = 32f
        ),

        // Woodbine Loading Yard (Tier 3)
        ScavCharacterDef(
            scavId = "scav_devon_bell",
            displayName = "Devon Bell",
            roleLabel = "Yard Enforcer",
            campId = "camp_downtown_yard",
            weapon = WeaponId.AK47,
            shirtColorHex = 0xFF7F1D1D,
            vestColorHex = 0xFF1E293B,
            skinColorHex = 0xFF8C5A3C,
            baseHp = 125f,
            patrolOffsetX = -62f,
            patrolOffsetY = 38f
        ),
        ScavCharacterDef(
            scavId = "scav_hector_diaz",
            displayName = "Hector Diaz",
            roleLabel = "Squad Lead",
            campId = "camp_downtown_yard",
            weapon = WeaponId.SCAR_H,
            shirtColorHex = 0xFF52525B,
            vestColorHex = 0xFF364A33,
            skinColorHex = 0xFFC68E64,
            baseHp = 135f,
            patrolOffsetX = 58f,
            patrolOffsetY = -34f
        ),

        // Airfield Runway Foothold (Tier 3)
        ScavCharacterDef(
            scavId = "scav_caleb_cole",
            displayName = "Caleb Cole",
            roleLabel = "Runway Gunner",
            campId = "camp_airfield_fob",
            weapon = WeaponId.MK18,
            shirtColorHex = 0xFF334155,
            vestColorHex = 0xFF78350F,
            skinColorHex = 0xFFD9A37C,
            baseHp = 130f,
            patrolOffsetX = -62f,
            patrolOffsetY = -32f
        ),
        ScavCharacterDef(
            scavId = "scav_tomas_kowalski",
            displayName = "Tomas Kowalski",
            roleLabel = "Tech Specialist",
            campId = "camp_airfield_fob",
            weapon = WeaponId.P90,
            shirtColorHex = 0xFF3F3F46,
            vestColorHex = 0xFF1E3A5F,
            skinColorHex = 0xFFE8C1A0,
            baseHp = 125f,
            patrolOffsetX = 62f,
            patrolOffsetY = 36f
        ),

        // Cobalt Springs Redoubt (Tier 4)
        ScavCharacterDef(
            scavId = "scav_nadia_diaz",
            displayName = "Nadia Diaz",
            roleLabel = "Overwatch",
            campId = "camp_cobalt_ridge",
            weapon = WeaponId.M14_EBR,
            shirtColorHex = 0xFF3B2F2F,
            vestColorHex = 0xFF18181B,
            skinColorHex = 0xFFC68E64,
            baseHp = 140f,
            patrolOffsetX = -46f,
            patrolOffsetY = -42f
        ),
        ScavCharacterDef(
            scavId = "scav_ania_kowalski",
            displayName = "Ania Kowalski",
            roleLabel = "Shock Lead",
            campId = "camp_cobalt_ridge",
            weapon = WeaponId.VAL_AS,
            shirtColorHex = 0xFF7C2D12,
            vestColorHex = 0xFF1F2937,
            skinColorHex = 0xFFE5BEA0,
            baseHp = 145f,
            patrolOffsetX = 48f,
            patrolOffsetY = -20f
        ),
        ScavCharacterDef(
            scavId = "scav_gabe_mercer",
            displayName = "Gabe Mercer",
            roleLabel = "Redoubt Commander",
            campId = "camp_cobalt_ridge",
            weapon = WeaponId.M249_SAW,
            shirtColorHex = 0xFF27272A,
            vestColorHex = 0xFF7F1D1D,
            skinColorHex = 0xFFD9A37C,
            baseHp = 165f,
            patrolOffsetX = 0f,
            patrolOffsetY = 48f
        )
    )

    val kinLinks: List<ScavKinRelation> = listOf(
        // James Vance <-> Elena Vance (wife) & Maya Vance (sister)
        ScavKinRelation("scav_james_vance", "James Vance", "scav_elena_vance", "Wife", "husband"),
        ScavKinRelation("scav_james_vance", "James Vance", "scav_maya_vance", "Sister", "brother"),
        ScavKinRelation("scav_elena_vance", "Elena Vance", "scav_james_vance", "Husband", "wife"),
        ScavKinRelation("scav_elena_vance", "Elena Vance", "scav_maya_vance", "Sister-in-law", "sister-in-law"),
        ScavKinRelation("scav_maya_vance", "Maya Vance", "scav_james_vance", "Brother", "sister"),
        ScavKinRelation("scav_maya_vance", "Maya Vance", "scav_elena_vance", "Sister-in-law", "sister-in-law"),

        // Bell brothers
        ScavKinRelation("scav_marcus_bell", "Marcus Bell", "scav_devon_bell", "Brother", "brother"),
        ScavKinRelation("scav_devon_bell", "Devon Bell", "scav_marcus_bell", "Brother", "brother"),

        // Cole siblings
        ScavKinRelation("scav_tessa_cole", "Tessa Cole", "scav_caleb_cole", "Brother", "sister"),
        ScavKinRelation("scav_caleb_cole", "Caleb Cole", "scav_tessa_cole", "Sister", "brother"),

        // Diaz siblings
        ScavKinRelation("scav_hector_diaz", "Hector Diaz", "scav_nadia_diaz", "Sister", "brother"),
        ScavKinRelation("scav_nadia_diaz", "Nadia Diaz", "scav_hector_diaz", "Brother", "sister"),

        // Kowalski spouses
        ScavKinRelation("scav_tomas_kowalski", "Tomas Kowalski", "scav_ania_kowalski", "Wife", "husband"),
        ScavKinRelation("scav_ania_kowalski", "Ania Kowalski", "scav_tomas_kowalski", "Husband", "wife"),

        // Mercer father / daughter
        ScavKinRelation("scav_gabe_mercer", "Gabe Mercer", "scav_sienna_mercer", "Daughter", "dad"),
        ScavKinRelation("scav_sienna_mercer", "Sienna Mercer", "scav_gabe_mercer", "Father", "daughter")
    )

    fun campById(campId: String): ScavEncampment? = camps.firstOrNull { it.id == campId }

    fun namedByCamp(campId: String): List<ScavCharacterDef> = namedScavs.filter { it.campId == campId }

    fun resolveInquiriesForLivingScav(
        kinScavId: String,
        slainRecords: List<SlainScavRecord>
    ): ResolvedKinInquiry? {
        val kinDef = namedScavs.firstOrNull { it.scavId == kinScavId } ?: return null
        val slainById = slainRecords.associateBy { it.scavId }
        val matchingLink = kinLinks.firstOrNull { link ->
            link.kinScavId == kinScavId && link.subjectScavId in slainById
        } ?: return null
        val record = slainById[matchingLink.subjectScavId] ?: return null
        return ResolvedKinInquiry(
            kinDef = kinDef,
            slainRecord = record,
            relationshipToSlain = matchingLink.relationOfSubjectToKin
        )
    }
}

object ScavKinshipMemoryStore {
    private const val FIELD_SEP = "\u001F"
    private const val RECORD_SEP = "\u001E"

    fun encode(records: List<SlainScavRecord>): String {
        return records.joinToString(RECORD_SEP) { r ->
            listOf(
                sanitize(r.scavId),
                sanitize(r.displayName),
                sanitize(r.campId),
                sanitize(r.campName),
                sanitize(r.districtName),
                sanitize(r.killerCallsign),
                sanitize(r.weaponKey),
                r.killedAtMs.toString(),
                r.witnessedBySquad.toString()
            ).joinToString(FIELD_SEP)
        }
    }

    fun decode(raw: String?): List<SlainScavRecord> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(RECORD_SEP).mapNotNull { entry ->
            if (entry.isBlank()) return@mapNotNull null
            val parts = entry.split(FIELD_SEP)
            if (parts.size < 9) return@mapNotNull null
            SlainScavRecord(
                scavId = parts[0],
                displayName = parts[1],
                campId = parts[2],
                campName = parts[3],
                districtName = parts[4],
                killerCallsign = parts[5],
                weaponKey = parts[6],
                killedAtMs = parts[7].toLongOrNull() ?: 0L,
                witnessedBySquad = parts[8].toBooleanStrictOrNull() ?: false
            )
        }
    }

    fun upsertRecord(
        existing: List<SlainScavRecord>,
        newRecord: SlainScavRecord
    ): List<SlainScavRecord> {
        val filtered = existing.filterNot { it.scavId == newRecord.scavId }
        return (listOf(newRecord) + filtered).take(40)
    }

    private fun sanitize(value: String): String =
        value.replace(FIELD_SEP, " ").replace(RECORD_SEP, " ").trim()
}
