package com.example.game.world

import com.example.game.model.CraftingMaterial
import com.example.game.model.GroundPatch
import com.example.game.model.InventoryItem
import com.example.game.model.LootContainer
import com.example.game.model.PropObj
import com.example.game.model.RivalOperatorEntity
import com.example.game.model.ScavAiState
import com.example.game.model.ScavEncampment
import com.example.game.model.ScavKinshipRoster
import com.example.game.model.StreetLightObj
import com.example.game.model.TerrainKind
import com.example.game.model.WallSegment
import com.example.game.model.WeaponId
import com.example.game.model.ZombieEntity
import com.example.game.model.ZombieVariant
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

internal data class DeLaineHostilePopulation(
    val encampments: List<ScavEncampment>,
    val initialScavs: List<RivalOperatorEntity>,
    val initialZombies: List<ZombieEntity>
)

internal object DeLaineEnemyAndCampBuilder {

    private fun matItem(
        uid: (String) -> String,
        mat: CraftingMaterial,
        qty: Int
    ): InventoryItem = DeLaineTownBuilder.matItem(uid, mat, qty)

    fun buildCampsAndHostiles(
        acc: MutableWorldAccum,
        rng: Random,
        dropX: Float,
        dropY: Float,
        uid: (String) -> String
    ): DeLaineHostilePopulation {
        val camps = ScavKinshipRoster.camps
        val scavs = mutableListOf<RivalOperatorEntity>()
        val zombies = mutableListOf<ZombieEntity>()
        var zombieIdSeq = 1

        // 1. Construct Physical Scav Foothold Encampments (sandbags, crates, firepits, floodlights, stash chests)
        camps.forEachIndexed { cIdx, camp ->
            buildPhysicalCampProps(acc, camp, uid)

            val namedRoster = ScavKinshipRoster.namedByCamp(camp.id)
            namedRoster.forEachIndexed { sIdx, def ->
                val spawnPos = findClearPointNear(
                    acc = acc,
                    targetX = camp.centerX + def.patrolOffsetX,
                    targetY = camp.centerY + def.patrolOffsetY,
                    elev = 0,
                    dropX = dropX,
                    dropY = dropY,
                    minDropDist = 265f
                )
                scavs.add(
                    RivalOperatorEntity(
                        id = def.scavId,
                        uid = "${def.scavId}_uid",
                        callsign = def.displayName.uppercase(),
                        x = spawnPos.first,
                        y = spawnPos.second,
                        angle = (sIdx * 1.9f) % (2f * PI.toFloat()),
                        elevation = 0,
                        hp = def.baseHp,
                        maxHp = def.baseHp,
                        weapon = def.weapon,
                        shirtColorHex = def.shirtColorHex,
                        vestColorHex = def.vestColorHex,
                        skinColorHex = def.skinColorHex,
                        patrolTargetX = camp.centerX - def.patrolOffsetX * 0.65f,
                        patrolTargetY = camp.centerY - def.patrolOffsetY * 0.65f,
                        scavId = def.scavId,
                        displayName = def.displayName,
                        campId = camp.id,
                        campName = camp.name,
                        roleLabel = def.roleLabel,
                        dangerTier = camp.dangerTier,
                        aiState = ScavAiState.PATROL,
                        homeX = camp.centerX,
                        homeY = camp.centerY
                    )
                )
            }

            // Tier-scaled camp sentries (Tier 2 adds 1, Tier 3 adds 2, Tier 4 adds 2 heavy)
            val extraGuards = (camp.dangerTier - 1).coerceIn(0, 2)
            val sentryWeapons = when (camp.dangerTier) {
                1 -> listOf(WeaponId.MOSSBERG_500, WeaponId.GLOCK_19)
                2 -> listOf(WeaponId.UMP45, WeaponId.SKS)
                3 -> listOf(WeaponId.AK47, WeaponId.REMINGTON_870)
                else -> listOf(WeaponId.MK18, WeaponId.SCAR_H)
            }
            for (g in 0 until extraGuards) {
                val angle = (cIdx * 1.1f + g * 2.4f)
                val gx = camp.centerX + cos(angle) * 86f
                val gy = camp.centerY + sin(angle) * 86f
                val pos = findClearPointNear(acc, gx, gy, 0, dropX, dropY, minDropDist = 265f)
                val sentryId = "${camp.id}_sentry_$g"
                val callsign = "${camp.factionTag.substringBefore(" ")}-${g + 1}"
                val hp = 88f + camp.dangerTier * 14f
                scavs.add(
                    RivalOperatorEntity(
                        id = sentryId,
                        uid = "${sentryId}_uid",
                        callsign = callsign,
                        x = pos.first,
                        y = pos.second,
                        angle = angle + PI.toFloat(),
                        elevation = 0,
                        hp = hp,
                        maxHp = hp,
                        weapon = sentryWeapons[(g + cIdx) % sentryWeapons.size],
                        shirtColorHex = 0xFF374151,
                        vestColorHex = if (camp.dangerTier >= 3) 0xFF7F1D1D else 0xFF334155,
                        skinColorHex = 0xFFC68E64,
                        patrolTargetX = camp.centerX + cos(angle + 1.5f) * 74f,
                        patrolTargetY = camp.centerY + sin(angle + 1.5f) * 74f,
                        scavId = sentryId,
                        displayName = callsign,
                        campId = camp.id,
                        campName = camp.name,
                        roleLabel = "Camp Sentry",
                        dangerTier = camp.dangerTier,
                        aiState = ScavAiState.PATROL,
                        homeX = camp.centerX,
                        homeY = camp.centerY
                    )
                )
            }
        }

        // 2. District-Scaled Infected Patrols Across DeLaine Metro (Tier 1..4)
        for (dz in acc.districtZones) {
            val count = when (dz.dangerTier) {
                1 -> 10
                2 -> 13
                3 -> 16
                else -> 18
            }
            var placed = 0
            var attempts = 0
            while (placed < count && attempts < count * 10) {
                attempts++
                val sx = dz.bounds.x + 80f + rng.nextFloat() * max(40f, dz.bounds.w - 160f)
                val sy = dz.bounds.y + 80f + rng.nextFloat() * max(40f, dz.bounds.h - 160f)
                if (hypot(sx - dropX, sy - dropY) < 275f) continue
                // Keep zombies outside immediate Scav Camp interiors so they don't instantly wipe camps at T=0
                if (camps.any { hypot(sx - it.centerX, sy - it.centerY) < it.radius + 95f }) continue
                if (!isOutdoorZombieSpotClear(acc, sx, sy)) continue

                val variant = pickVariantForTier(dz.dangerTier, rng)
                zombies.add(
                    createZombie(
                        id = zombieIdSeq++,
                        x = sx,
                        y = sy,
                        elevation = 0,
                        variant = variant,
                        tier = dz.dangerTier,
                        angle = rng.nextFloat() * 2f * PI.toFloat()
                    )
                )
                placed++
            }
        }

        // 3. Key Landmark Interior Infected (Courthouse, SunCoast Mart, Precinct, Airfield Hangar, Rail Yard)
        for (b in acc.buildings) {
            val tier = districtTierAt(acc, b.bounds.x + b.bounds.w * 0.5f, b.bounds.y + b.bounds.h * 0.5f)
            val interiorFloors = b.accessibleFloors.filter { it != b.roofFloorIndex }
            for (fl in interiorFloors) {
                val perFloor = if (tier >= 3 && b.bounds.w >= 450f) 2 else 1
                var added = 0
                var tries = 0
                while (added < perFloor && tries < 12) {
                    tries++
                    val zx = b.bounds.x + 48f + rng.nextFloat() * max(20f, b.bounds.w - 96f)
                    val zy = b.bounds.y + 48f + rng.nextFloat() * max(20f, b.bounds.h - 96f)
                    if (hypot(zx - dropX, zy - dropY) < 275f) continue
                    if (isBlockedAtElevation(acc, zx, zy, 12f, fl)) continue
                    val variant = if (fl == -1 && tier >= 2 && rng.nextFloat() < 0.45f) {
                        ZombieVariant.ARMORED
                    } else {
                        pickVariantForTier(tier, rng)
                    }
                    zombies.add(
                        createZombie(
                            id = zombieIdSeq++,
                            x = zx,
                            y = zy,
                            elevation = fl,
                            variant = variant,
                            tier = tier,
                            angle = rng.nextFloat() * 2f * PI.toFloat()
                        )
                    )
                    added++
                }
            }
        }

        return DeLaineHostilePopulation(
            encampments = camps,
            initialScavs = scavs,
            initialZombies = zombies
        )
    }

    private fun buildPhysicalCampProps(
        acc: MutableWorldAccum,
        camp: ScavEncampment,
        uid: (String) -> String
    ) {
        val cx = camp.centerX
        val cy = camp.centerY
        val pid = camp.id

        // Packed earth clearing & subtle perimeter timber stakes/barricades
        acc.groundPatches.add(GroundPatch(cx - 115f, cy - 92f, 230f, 184f, 0xFF332B23))
        acc.groundPatches.add(GroundPatch(cx - 88f, cy - 66f, 176f, 132f, 0xFF3D3328))

        // Central campfire + night floodlight
        acc.props.add(PropObj("${pid}_fire", cx - 23f, cy - 23f, 46f, 46f, 0, 0xFF4B5563, 0.28f, "camp_firepit"))
        acc.streetLights.add(StreetLightObj(cx - 98f, cy - 78f))

        // Four-corner sandbag emplacements + crate cover positions (real tactical cover for both AI and player!)
        acc.props.add(PropObj("${pid}_sb_n", cx - 44f, cy - 98f, 88f, 18f, 0, 0xFF7D6B51, 0.44f, "sandbag"))
        acc.props.add(PropObj("${pid}_sb_s", cx - 44f, cy + 80f, 88f, 18f, 0, 0xFF7D6B51, 0.44f, "sandbag"))
        acc.props.add(PropObj("${pid}_sb_w", cx - 108f, cy - 44f, 18f, 88f, 0, 0xFF7D6B51, 0.44f, "sandbag"))
        acc.props.add(PropObj("${pid}_sb_e", cx + 90f, cy - 44f, 18f, 88f, 0, 0xFF7D6B51, 0.44f, "sandbag"))

        // Low solid timber barricade corners for hard LOS break
        acc.walls.add(WallSegment(cx - 106f, cy - 88f, 52f, 10f, 0, 0xFF4A3728, 0xFF6B503B))
        acc.walls.add(WallSegment(cx + 54f, cy + 78f, 52f, 10f, 0, 0xFF4A3728, 0xFF6B503B))

        // Supply crates inside camp perimeter
        acc.props.add(PropObj("${pid}_crate_1", cx + 42f, cy - 64f, 44f, 30f, 0, 0xFF475569, 0.62f, "storage_crate_extra"))
        acc.props.add(PropObj("${pid}_crate_2", cx - 84f, cy + 36f, 44f, 30f, 0, 0xFF5A432D, 0.62f, "storage_crate_heavy"))

        // Tier-scaled loot chest strictly stocked with crafting materials
        val campLoot = when (camp.dangerTier) {
            1 -> listOf(
                matItem(uid, CraftingMaterial.SCRAP_METAL, 4),
                matItem(uid, CraftingMaterial.TACTICAL_CLOTH, 3),
                matItem(uid, CraftingMaterial.GUNPOWDER, 3),
                matItem(uid, CraftingMaterial.BRASS_CASINGS, 3)
            )
            2 -> listOf(
                matItem(uid, CraftingMaterial.GUNPOWDER, 4),
                matItem(uid, CraftingMaterial.ELECTRONIC_PARTS, 3),
                matItem(uid, CraftingMaterial.FORGED_RECEIVER, 2),
                matItem(uid, CraftingMaterial.KEVLAR_WEAVE, 2)
            )
            3 -> listOf(
                matItem(uid, CraftingMaterial.CERAMIC_PLATES, 3),
                matItem(uid, CraftingMaterial.OPTICAL_GLASS, 3),
                matItem(uid, CraftingMaterial.SUPPRESSOR_BAFFLES, 2),
                matItem(uid, CraftingMaterial.RIFLED_BARREL, 2),
                matItem(uid, CraftingMaterial.PERCUSSION_PRIMERS, 4)
            )
            else -> listOf(
                matItem(uid, CraftingMaterial.CERAMIC_PLATES, 4),
                matItem(uid, CraftingMaterial.TRIGGER_ASSEMBLY, 3),
                matItem(uid, CraftingMaterial.LITHIUM_CELLS, 4),
                matItem(uid, CraftingMaterial.SUPPRESSOR_BAFFLES, 3),
                matItem(uid, CraftingMaterial.RIFLED_BARREL, 3)
            )
        }
        acc.lootContainers.add(
            LootContainer(
                id = "${pid}_locker",
                x = cx + 48f,
                y = cy - 26f,
                floor = 0,
                label = "${camp.name} Supply Cache",
                items = campLoot.toMutableList()
            )
        )
    }

    private fun pickVariantForTier(tier: Int, rng: Random): ZombieVariant {
        val roll = rng.nextFloat()
        return when (tier) {
            1 -> if (roll < 0.80f) ZombieVariant.WALKER else ZombieVariant.SPRINTER
            2 -> when {
                roll < 0.56f -> ZombieVariant.WALKER
                roll < 0.90f -> ZombieVariant.SPRINTER
                else -> ZombieVariant.ARMORED
            }
            3 -> when {
                roll < 0.40f -> ZombieVariant.WALKER
                roll < 0.80f -> ZombieVariant.SPRINTER
                else -> ZombieVariant.ARMORED
            }
            else -> when {
                roll < 0.25f -> ZombieVariant.WALKER
                roll < 0.68f -> ZombieVariant.SPRINTER
                else -> ZombieVariant.ARMORED
            }
        }
    }

    fun createZombie(
        id: Int,
        x: Float,
        y: Float,
        elevation: Int,
        variant: ZombieVariant,
        tier: Int,
        angle: Float,
        isFrenzy: Boolean = false
    ): ZombieEntity {
        val tierHpScale = 1f + (tier - 1).coerceAtLeast(0) * 0.14f
        val baseHp = when (variant) {
            ZombieVariant.WALKER -> 56f
            ZombieVariant.SPRINTER -> 44f
            ZombieVariant.ARMORED -> 118f
        }
        val maxHp = (baseHp * tierHpScale * if (isFrenzy) 1.08f else 1f).toInt().toFloat()
        val baseSpeed = when (variant) {
            ZombieVariant.WALKER -> 1.18f
            ZombieVariant.SPRINTER -> 2.18f
            ZombieVariant.ARMORED -> 0.96f
        }
        return ZombieEntity(
            id = id,
            x = x,
            y = y,
            radius = if (variant == ZombieVariant.ARMORED) 12.5f else 11f,
            speed = baseSpeed + (tier - 1).coerceAtLeast(0) * 0.06f,
            hp = maxHp,
            maxHp = maxHp,
            angle = angle,
            elevation = elevation,
            variant = variant,
            isFrenzy = isFrenzy,
            homeDistrictTier = tier
        )
    }

    private fun districtTierAt(acc: MutableWorldAccum, x: Float, y: Float): Int {
        return acc.districtZones.firstOrNull { dz ->
            x in dz.bounds.x..(dz.bounds.x + dz.bounds.w) &&
                y in dz.bounds.y..(dz.bounds.y + dz.bounds.h)
        }?.dangerTier ?: 1
    }

    private fun isOutdoorZombieSpotClear(acc: MutableWorldAccum, x: Float, y: Float): Boolean {
        if (x !in -3120f..3120f || y !in -3120f..3120f) return false
        for (b in acc.buildings) {
            if (x in (b.bounds.x - 16f)..(b.bounds.x + b.bounds.w + 16f) &&
                y in (b.bounds.y - 16f)..(b.bounds.y + b.bounds.h + 16f)
            ) {
                return false
            }
        }
        val onWalkway = acc.elevatedWalkways.any { x in it.x..(it.x + it.w) && y in it.y..(it.y + it.h) }
        if (!onWalkway) {
            val inWater = acc.terrainZones.any {
                it.kind == TerrainKind.BASIN_WATER &&
                    x in (it.x - 10f)..(it.x + it.w + 10f) &&
                    y in (it.y - 10f)..(it.y + it.h + 10f)
            }
            if (inWater) return false
        }
        return !isBlockedAtElevation(acc, x, y, 13f, 0)
    }

    private fun findClearPointNear(
        acc: MutableWorldAccum,
        targetX: Float,
        targetY: Float,
        elev: Int,
        dropX: Float,
        dropY: Float,
        minDropDist: Float
    ): Pair<Float, Float> {
        if (hypot(targetX - dropX, targetY - dropY) >= minDropDist &&
            !isBlockedAtElevation(acc, targetX, targetY, 14f, elev)
        ) {
            return targetX to targetY
        }
        val radii = listOf(22f, 44f, 70f, 105f)
        for (rad in radii) {
            for (deg in 0 until 360 step 30) {
                val a = deg * PI.toFloat() / 180f
                val tx = (targetX + cos(a) * rad).coerceIn(-3100f, 3100f)
                val ty = (targetY + sin(a) * rad).coerceIn(-3100f, 3100f)
                if (hypot(tx - dropX, ty - dropY) >= minDropDist &&
                    !isBlockedAtElevation(acc, tx, ty, 14f, elev)
                ) {
                    return tx to ty
                }
            }
        }
        return targetX to targetY
    }

    private fun isBlockedAtElevation(acc: MutableWorldAccum, x: Float, y: Float, r: Float, elev: Int): Boolean {
        for (w in acc.walls) {
            if (w.floor == elev && MultiFloorBlueprint.circleRectOverlap(x, y, r, w.x, w.y, w.w, w.h)) return true
        }
        for (p in acc.props) {
            if (p.floor == elev && !p.cleared && MultiFloorBlueprint.circleRectOverlap(x, y, r, p.x, p.y, p.w, p.h)) return true
        }
        for (d in acc.doors) {
            if (d.floor == elev) {
                val tipX = d.hingeX + cos(d.angle) * d.length
                val tipY = d.hingeY + sin(d.angle) * d.length
                val minX = min(d.hingeX, tipX) - 6f
                val maxX = max(d.hingeX, tipX) + 6f
                val minY = min(d.hingeY, tipY) - 6f
                val maxY = max(d.hingeY, tipY) + 6f
                if (MultiFloorBlueprint.circleRectOverlap(x, y, r, minX, minY, maxX - minX, maxY - minY)) return true
            }
        }
        return false
    }
}
