package com.example.game.model

import kotlin.random.Random

enum class CdRarity(val label: String, val colorHex: Long) {
    COMMON("Common", 0xFF9CA3AF),
    UNCOMMON("Uncommon", 0xFF34D399),
    RARE("Rare", 0xFF38BDF8),
    ULTRA_RARE("Grail", 0xFFC084FC)
}

enum class CdGenre(val label: String, val baseMidi: Int) {
    GRUNGE("90s Grunge", 48),
    DAD_ROCK("Classic Dad Rock", 52),
    SYNTH_POP("80s Synth-Pop", 57),
    HIP_HOP("Boom-Bap Tape", 45),
    METAL("Garage Thrash", 43),
    POP_PUNK("Mall Pop-Punk", 55),
    SMOOTH_JAZZ("Late-Night Jazz", 60),
    MIXTAPE("Burned CD-R", 50)
}

data class CdRecord(
    val id: Int,
    val artist: String,
    val album: String,
    val genre: CdGenre,
    val rarity: CdRarity,
    val coverPrimaryHex: Long,
    val coverAccentHex: Long,
    val bpm: Int
) {
    val catalogTag: String get() = "#${id.toString().padStart(4, '0')}"
}

object CdCatalog {
    const val TOTAL_CDS = 2800

    private val artistFirst = arrayOf(
        "Smells Like", "Velvet", "Suburban", "Rustbelt", "Neon", "Chrome", "Nickel", "Pearl",
        "Stone", "Static", "Midnight", "Glass", "Tape", "Dead", "Paper", "Oakridge",
        "Copper", "Asphalt", "Bitter", "Chrome", "Backseat", "Plastic", "Rainy", "Cheap",
        "Stereo", "Analog", "Basement", "Hollow", "Gravel", "Cinder", "Low-Fi", "Silver"
    )

    private val artistSecond = arrayOf(
        "Nirvanna", "Puppets", "Daydream", "Zephyr", "Static", "Sabbath", "Jammer", "Pilots",
        "Garden", "Circuit", "Drivers", "Echoes", "Rebels", "Boys", "Syndicate", "Drifters",
        "Saints", "Fuzz", "Tapes", "Phantoms", "Kings", "Crows", "Riders", "Project",
        "Division", "Club", "Collective", "Crew", "Quartet", "Vipers", "Wires", "Jukebox"
    )

    private val albumPrefixes = arrayOf(
        "Greatest Hits of", "Unplugged at", "Bootleg From", "Midnight on", "Cheap Thrills in",
        "Lost Demos:", "Live at the", "Discount", "Highway", "Static Over",
        "Overdrive", "Suburban", "Neon", "Basement", "Broken", "Silver"
    )

    private val albumNouns = arrayOf(
        "Route 9", "B-Sides", "Apex Parking", "Motel 6", "Cassette Vol.", "Oakridge High",
        "Night Shift", "Thunder Road", "Bargain Bin", "Blue Horizon", "Westside", "Radio",
        "Gold Edition", "Zero Hour", "Backyard", "Sidewalk", "Afterparty", "Sunday Drive"
    )

    private val palettePairs = arrayOf(
        0xFF1F2937L to 0xFFF59E0BL,
        0xFF312E81L to 0xFF38BDF8L,
        0xFF7F1D1DL to 0xFFFCA5A5L,
        0xFF14532DL to 0xFF4ADE80L,
        0xFF4C1D95L to 0xFFE879F9L,
        0xFF78350FL to 0xFFFDE047L,
        0xFF0F172AL to 0xFF2DD4BFL,
        0xFF3F3F46L to 0xFFFB923CL
    )

    fun getById(id: Int): CdRecord {
        val clamped = id.coerceIn(1, TOTAL_CDS)
        val rng = Random(clamped * 7919 + 104729)
        val genre = CdGenre.entries[rng.nextInt(CdGenre.entries.size)]
        val roll = rng.nextInt(100)
        val rarity = when {
            roll >= 96 -> CdRarity.ULTRA_RARE
            roll >= 82 -> CdRarity.RARE
            roll >= 54 -> CdRarity.UNCOMMON
            else -> CdRarity.COMMON
        }
        val a1 = artistFirst[rng.nextInt(artistFirst.size)]
        val a2 = artistSecond[rng.nextInt(artistSecond.size)]
        val p1 = albumPrefixes[rng.nextInt(albumPrefixes.size)]
        val p2 = albumNouns[rng.nextInt(albumNouns.size)]
        val pal = palettePairs[rng.nextInt(palettePairs.size)]
        val suffix = if (clamped % 9 == 0) " Pt.${(clamped % 4) + 1}" else ""
        val rarityTag = if (rarity == CdRarity.ULTRA_RARE) " [PROMO]" else ""
        return CdRecord(
            id = clamped,
            artist = "$a1 $a2",
            album = "$p1 $p2$suffix$rarityTag",
            genre = genre,
            rarity = rarity,
            coverPrimaryHex = pal.first,
            coverAccentHex = pal.second,
            bpm = 84 + rng.nextInt(68)
        )
    }

    fun rollLootCdId(rarityBoost: Int = 0, rng: Random = Random.Default): Int {
        repeat(6) {
            val id = rng.nextInt(1, TOTAL_CDS + 1)
            val rec = getById(id)
            if (rarityBoost <= 0 || rec.rarity.ordinal >= rarityBoost) return id
        }
        return rng.nextInt(1, TOTAL_CDS + 1)
    }
}
