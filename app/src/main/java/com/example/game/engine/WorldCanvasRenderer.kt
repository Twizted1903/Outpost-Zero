package com.example.game.engine

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import com.example.game.model.ArmorTier
import com.example.game.model.CameraViewBounds
import com.example.game.model.GameMapId
import com.example.game.model.GlassesStyle
import com.example.game.model.GlovesStyle
import com.example.game.model.HatStyle
import com.example.game.model.OperatorProfile
import com.example.game.model.ScavAiState
import com.example.game.model.StationKind
import com.example.game.model.SurvivorCatalog
import com.example.game.model.TerrainKind
import com.example.game.model.WeaponCategory
import com.example.game.model.WeaponId
import com.example.game.model.ZombieVariant
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object WorldCanvasRenderer {
    const val OBLIQUE_Y = -34f

    private data class DepthDrawable(
        val sortY: Float,
        val draw: DrawScope.() -> Unit
    )

    fun DrawScope.renderWorld(sim: GameSimulation) {
        val w = sim.world ?: return
        val now = System.currentTimeMillis()
        val inBlackout = now < sim.blackoutUntilMs
        val inRareFlicker = now < sim.rareFlickerUntilMs
        val atmosphere = DayNightCycle.computeAtmosphere(
            hours = sim.worldTimeHours,
            mapId = w.mapId,
            insideBuilding = sim.insideBuilding,
            inBlackout = inBlackout,
            inFlicker = inRareFlicker,
            highVisibilityNight = sim.highVisibilityNight
        )
        val zoom = CameraViewBounds.clampZoom(sim.cameraZoom)
        val voxelDepth = CameraViewBounds.clampVoxelDepth(sim.voxelDepthScale)
        val oblY = OBLIQUE_Y * voxelDepth
        val halfW = (size.width * 0.5f) / zoom
        val halfH = (size.height * 0.5f) / zoom
        val camL = sim.playerX - halfW
        val camT = sim.playerY - halfH
        val camR = sim.playerX + halfW
        val camB = sim.playerY + halfH

        // Base splash-matched smoky pine & coastal slate ground
        drawRect(color = Color(0xFF16231F), size = size)

        withTransform({
            translate(left = size.width * 0.5f, top = size.height * 0.5f)
            scale(scaleX = zoom, scaleY = zoom, pivot = Offset.Zero)
            translate(left = -sim.playerX, top = -sim.playerY)
        }) {
            // Subtle deterministic pine needles, moss & graphic-novel ink grass tufts
            val gxStart = ((camL / 110f).toInt() - 1) * 110f
            val gyStart = ((camT / 110f).toInt() - 1) * 110f
            var ty = gyStart
            while (ty <= camB + 110f) {
                var tx = gxStart
                while (tx <= camR + 110f) {
                    val h = ((tx.toInt() * 73856093) xor (ty.toInt() * 19349663)) and 0x7FFF
                    if (h % 4 == 0) {
                        val jx = tx + (h % 68)
                        val jy = ty + ((h shr 4) % 68)
                        drawLine(Color(0xFF0A120E), Offset(jx, jy), Offset(jx - 3.5f, jy - 6.5f), strokeWidth = 1.4f, cap = StrokeCap.Round)
                        drawLine(Color(0xFF0A120E), Offset(jx + 2f, jy), Offset(jx + 4.5f, jy - 5.5f), strokeWidth = 1.3f, cap = StrokeCap.Round)
                        drawLine(Color(0xFF233830).copy(alpha = 0.55f), Offset(jx + 1f, jy - 1f), Offset(jx + 0.5f, jy - 8f), strokeWidth = 1.1f, cap = StrokeCap.Round)
                    }
                    tx += 110f
                }
                ty += 110f
            }

            // 1. Ground patches, Helipad apron & Cantine deck
            for (gp in w.groundPatches) {
                if (!inView(gp.x, gp.y, gp.w, gp.h, camL, camT, camR, camB)) continue
                drawRect(
                    color = Color(gp.colorHex),
                    topLeft = Offset(gp.x, gp.y),
                    size = Size(gp.w, gp.h)
                )
                if (gp.label == "HELIPAD_APRON") {
                    drawHelipadMarkings(gp.x, gp.y, gp.w, gp.h, now)
                } else if (gp.label == "CANTINE_PATIO" || gp.label == "HQ_COMMAND_DECK") {
                    drawCantinePatioDeck(gp.x, gp.y, gp.w, gp.h)
                } else if (gp.label == "CAMP_TRAIL") {
                    // Packed earth & crushed limestone footpath with natural aggregate stones (no street lines)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF3E352B).copy(alpha = 0.35f), Color.Transparent, Color(0xFF28221B).copy(alpha = 0.35f)),
                            startY = gp.y,
                            endY = gp.y + gp.h
                        ),
                        topLeft = Offset(gp.x, gp.y),
                        size = Size(gp.w, gp.h)
                    )
                    var sx = gp.x + 28f
                    while (sx < gp.x + gp.w - 18f) {
                        var sy = gp.y + 24f
                        while (sy < gp.y + gp.h - 16f) {
                            val h = ((sx.toInt() * 19349663) xor (sy.toInt() * 83492791)) and 0xFF
                            if (h < 52) {
                                drawCircle(Color(0xFF211B15).copy(alpha = 0.55f), radius = 2.6f, center = Offset(sx + (h % 9), sy + ((h shr 3) % 9)))
                            } else if (h in 60..72) {
                                drawCircle(Color(0xFF5D5345).copy(alpha = 0.45f), radius = 2.0f, center = Offset(sx, sy))
                            }
                            sy += 44f
                        }
                        sx += 48f
                    }
                } else if (gp.label == "OUTPOST ZERO COMPOUND") {
                    // Natural mottled hillside earth, gravel aggregate & weathered rock patches
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF3A342C).copy(alpha = 0.42f), Color(0xFF212B29).copy(alpha = 0.25f), Color.Transparent),
                            center = Offset(gp.x + gp.w * 0.5f, gp.y + gp.h * 0.48f),
                            radius = gp.w * 0.52f
                        ),
                        topLeft = Offset(gp.x, gp.y),
                        size = Size(gp.w, gp.h)
                    )
                    var sx = ((camL / 96f).toInt() - 1) * 96f
                    while (sx <= camR + 96f) {
                        if (sx in gp.x..(gp.x + gp.w)) {
                            var sy = ((camT / 96f).toInt() - 1) * 96f
                            while (sy <= camB + 96f) {
                                if (sy in gp.y..(gp.y + gp.h)) {
                                    val h = ((sx.toInt() * 73856093) xor (sy.toInt() * 19349663)) and 0xFF
                                    if (h < 42) {
                                        drawOval(
                                            color = Color(0xFF1A2220).copy(alpha = 0.36f),
                                            topLeft = Offset(sx + (h % 32), sy + ((h shr 2) % 32)),
                                            size = Size(24f, 12f)
                                        )
                                    } else if (h in 110..136) {
                                        drawCircle(
                                            color = Color(0xFF4B453C).copy(alpha = 0.34f),
                                            radius = 2.2f,
                                            center = Offset(sx + (h % 45), sy + ((h shr 3) % 45))
                                        )
                                    }
                                }
                                sy += 96f
                            }
                        }
                        sx += 96f
                    }
                }
            }

            // 1B. Non-Flat Outdoor Terrain Elevation (Cross-Hatched Cliffs, Cobalt Spring Water, Terraces & Ridges)
            val waterWave = sin((now % 2400L) / 2400f * 2f * PI.toFloat())
            for (tz in w.terrainZones) {
                if (!inView(tz.x, tz.y, tz.w, tz.h, camL, camT, camR, camB)) continue
                drawRect(
                    color = Color(tz.colorHex),
                    topLeft = Offset(tz.x, tz.y),
                    size = Size(tz.w, tz.h)
                )
                when (tz.kind) {
                    TerrainKind.BASIN_WATER -> {
                        // Deep sunken limestone bank shadow + vibrant multi-tone cobalt spring shimmer & ink ripples
                        drawRect(
                            color = Color(0xFF041724),
                            topLeft = Offset(tz.x, tz.y),
                            size = Size(tz.w, 14f)
                        )
                        drawRect(
                            color = Color(0xFF14748B).copy(alpha = 0.35f),
                            topLeft = Offset(tz.x + 18f, tz.y + 22f),
                            size = Size((tz.w - 36f).coerceAtLeast(0f), (tz.h - 44f).coerceAtLeast(0f))
                        )
                        drawRect(
                            color = PixelArtSprites.OUTLINE,
                            topLeft = Offset(tz.x, tz.y),
                            size = Size(tz.w, tz.h),
                            style = Stroke(width = 3.2f)
                        )
                        var wy = tz.y + 42f
                        while (wy < tz.y + tz.h - 24f) {
                            var wx = tz.x + 32f + ((wy.toInt() % 64)) * 0.5f
                            while (wx < tz.x + tz.w - 42f) {
                                drawLine(
                                    color = Color(0xFF67E8F9).copy(alpha = 0.34f + 0.14f * waterWave),
                                    start = Offset(wx + waterWave * 5f, wy),
                                    end = Offset(wx + 24f + waterWave * 5f, wy),
                                    strokeWidth = 2.2f,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = PixelArtSprites.OUTLINE.copy(alpha = 0.35f),
                                    start = Offset(wx + 4f + waterWave * 5f, wy + 3f),
                                    end = Offset(wx + 20f + waterWave * 5f, wy + 3f),
                                    strokeWidth = 1f
                                )
                                wx += 108f
                            }
                            wy += 80f
                        }
                    }
                    TerrainKind.LIMESTONE_BANK, TerrainKind.DITCH -> {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.38f),
                            topLeft = Offset(tz.x, tz.y),
                            size = Size(tz.w, 12f)
                        )
                        with(PixelArtSprites) {
                            drawComicHatch(tz.x + 4f, tz.y + 2f, tz.w - 8f, 12f, step = 7f, color = PixelArtSprites.OUTLINE.copy(alpha = 0.4f))
                        }
                        drawRect(
                            color = PixelArtSprites.OUTLINE,
                            topLeft = Offset(tz.x, tz.y),
                            size = Size(tz.w, tz.h),
                            style = Stroke(width = 2.8f)
                        )
                    }
                    TerrainKind.TERRACE_PLAZA, TerrainKind.SANDY_RIDGE -> {
                        val cliffH = if (tz.kind == TerrainKind.TERRACE_PLAZA) 16f else 11f
                        drawRect(
                            color = Color.Black.copy(alpha = 0.48f),
                            topLeft = Offset(tz.x + 5f, tz.y + tz.h),
                            size = Size(tz.w, cliffH + 5f)
                        )
                        drawRect(
                            color = Color(if (tz.kind == TerrainKind.TERRACE_PLAZA) 0xFF282530 else 0xFF2D251C),
                            topLeft = Offset(tz.x, tz.y + tz.h),
                            size = Size(tz.w, cliffH)
                        )
                        with(PixelArtSprites) {
                            drawComicHatch(tz.x + 2f, tz.y + tz.h + 1f, tz.w - 4f, cliffH - 2f, step = 8f, color = PixelArtSprites.OUTLINE.copy(alpha = 0.45f))
                        }
                        drawRect(
                            color = Color(0xFFFFF7E6).copy(alpha = 0.16f),
                            topLeft = Offset(tz.x, tz.y),
                            size = Size(tz.w, 3f)
                        )
                        drawRect(
                            color = PixelArtSprites.OUTLINE,
                            topLeft = Offset(tz.x, tz.y),
                            size = Size(tz.w, tz.h),
                            style = Stroke(width = 2.8f)
                        )
                    }
                    else -> Unit
                }
            }

            // 1C. Stone Stairways & Graded Ramps with graphic-novel ink tread contours
            for (rm in w.terrainRamps) {
                if (!inView(rm.x, rm.y, rm.w, rm.h, camL, camT, camR, camB)) continue
                drawRect(
                    color = Color(if (rm.isStairs) 0xFF534E5C else 0xFF423C33),
                    topLeft = Offset(rm.x, rm.y),
                    size = Size(rm.w, rm.h)
                )
                if (rm.isStairs) {
                    if (rm.w >= rm.h) {
                        var sy = rm.y + 6f
                        while (sy < rm.y + rm.h - 4f) {
                            drawLine(Color.White.copy(alpha = 0.14f), Offset(rm.x + 2f, sy - 1.5f), Offset(rm.x + rm.w - 2f, sy - 1.5f), strokeWidth = 1.2f)
                            drawLine(PixelArtSprites.OUTLINE, Offset(rm.x + 2f, sy), Offset(rm.x + rm.w - 2f, sy), strokeWidth = 2.2f)
                            sy += 8f
                        }
                    } else {
                        var sx = rm.x + 6f
                        while (sx < rm.x + rm.w - 4f) {
                            drawLine(Color.White.copy(alpha = 0.14f), Offset(sx - 1.5f, rm.y + 2f), Offset(sx - 1.5f, rm.y + rm.h - 2f), strokeWidth = 1.2f)
                            drawLine(PixelArtSprites.OUTLINE, Offset(sx, rm.y + 2f), Offset(sx, rm.y + rm.h - 2f), strokeWidth = 2.2f)
                            sx += 8f
                        }
                    }
                }
                drawRect(
                    color = PixelArtSprites.OUTLINE,
                    topLeft = Offset(rm.x, rm.y),
                    size = Size(rm.w, rm.h),
                    style = Stroke(width = 2.4f)
                )
            }

            // 2. Curved & straight asphalt roads with ink curbs, asphalt cracks, storm grates & rainwater sheen
            for (cr in w.curvedRoads) {
                if (cr.points.size < 2) continue
                val path = Path().apply {
                    moveTo(cr.points[0].first, cr.points[0].second)
                    for (i in 1 until cr.points.size) {
                        lineTo(cr.points[i].first, cr.points[i].second)
                    }
                }
                drawPath(
                    path = path,
                    color = PixelArtSprites.OUTLINE,
                    style = Stroke(width = cr.width + 20f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = path,
                    color = Color(0xFF565C63),
                    style = Stroke(width = cr.width + 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = path,
                    color = Color(0xFF282F39),
                    style = Stroke(width = cr.width, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = path,
                    color = Color(0xFFD99B16).copy(alpha = 0.42f),
                    style = Stroke(
                        width = 2.4f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 18f))
                    )
                )
            }

            for (r in w.roads) {
                if (!inView(r.x, r.y, r.w, r.h, camL, camT, camR, camB)) continue
                drawRect(color = Color(0xFF2A313C), topLeft = Offset(r.x, r.y), size = Size(r.w, r.h))
                // Deterministic inked asphalt fractures, manhole grates & wet rainwater puddles
                var rx = r.x + 56f
                while (rx < r.x + r.w - 40f) {
                    var ry = r.y + 42f
                    while (ry < r.y + r.h - 40f) {
                        val h = ((rx.toInt() * 31337) xor (ry.toInt() * 65537)) and 0xFF
                        if (h < 38) {
                            // Jagged comic asphalt crack
                            drawLine(PixelArtSprites.OUTLINE.copy(alpha = 0.72f), Offset(rx, ry), Offset(rx + 14f, ry + 8f), strokeWidth = 1.6f)
                            drawLine(PixelArtSprites.OUTLINE.copy(alpha = 0.72f), Offset(rx + 14f, ry + 8f), Offset(rx + 28f, ry + 3f), strokeWidth = 1.3f)
                        } else if (h in 40..54) {
                            // Rainwater puddle reflecting slate-cyan sky
                            drawOval(Color(0xFF0E1B26).copy(alpha = 0.85f), Offset(rx - 14f, ry - 7f), Size(32f, 14f))
                            drawOval(Color(0xFF38BDF8).copy(alpha = 0.18f), Offset(rx - 10f, ry - 5f), Size(20f, 7f))
                            drawOval(PixelArtSprites.OUTLINE.copy(alpha = 0.65f), Offset(rx - 14f, ry - 7f), Size(32f, 14f), style = Stroke(width = 1.2f))
                        } else if (h in 120..128) {
                            // Curbside cast-iron storm grate
                            drawRect(Color(0xFF111419), Offset(rx, ry), Size(18f, 12f))
                            drawRect(PixelArtSprites.OUTLINE, Offset(rx, ry), Size(18f, 12f), style = Stroke(width = 1.6f))
                            for (bar in 1..3) {
                                drawLine(Color(0xFF475569), Offset(rx + bar * 4.5f, ry + 1f), Offset(rx + bar * 4.5f, ry + 11f), strokeWidth = 1.5f)
                            }
                        }
                        ry += 140f
                    }
                    rx += 150f
                }
                drawRect(
                    color = PixelArtSprites.OUTLINE,
                    topLeft = Offset(r.x, r.y),
                    size = Size(r.w, r.h),
                    style = Stroke(width = 2.6f)
                )
            }

            for (m in w.roadMarkings) {
                if (!inView(m.x, m.y, m.w, m.h, camL, camT, camR, camB, 30f)) continue
                drawRect(color = Color(0xFFD99B16), topLeft = Offset(m.x, m.y), size = Size(m.w, m.h))
                drawRect(color = PixelArtSprites.OUTLINE.copy(alpha = 0.45f), topLeft = Offset(m.x, m.y), size = Size(m.w, m.h), style = Stroke(width = 1f))
            }

            for (sw in w.sidewalks) {
                if (!inView(sw.x, sw.y, sw.w, sw.h, camL, camT, camR, camB)) continue
                drawRect(color = Color(0xFF6B727A), topLeft = Offset(sw.x, sw.y), size = Size(sw.w, sw.h))
                // Heavy comic ink slab expansion joints + curb bevel highlight
                var sx = sw.x + 48f
                while (sx < sw.x + sw.w) {
                    drawLine(
                        color = PixelArtSprites.OUTLINE.copy(alpha = 0.65f),
                        start = Offset(sx, sw.y),
                        end = Offset(sx, sw.y + sw.h),
                        strokeWidth = 1.4f
                    )
                    sx += 48f
                }
                var sy = sw.y + 48f
                while (sy < sw.y + sw.h) {
                    drawLine(
                        color = PixelArtSprites.OUTLINE.copy(alpha = 0.65f),
                        start = Offset(sw.x, sy),
                        end = Offset(sw.x + sw.w, sy),
                        strokeWidth = 1.4f
                    )
                    sy += 48f
                }
                drawRect(
                    color = Color(0xFF9BA3AC).copy(alpha = 0.45f),
                    topLeft = Offset(sw.x + 2f, sw.y + 2f),
                    size = Size(max(0f, sw.w - 4f), 2.2f)
                )
                drawRect(
                    color = PixelArtSprites.OUTLINE,
                    topLeft = Offset(sw.x, sw.y),
                    size = Size(sw.w, sw.h),
                    style = Stroke(width = 2.2f)
                )
            }

            // 2B. Elevated Cypress Boardwalks & Concrete Highway Overpasses (above water & ditches)
            for (ew in w.elevatedWalkways) {
                if (!inView(ew.x, ew.y, ew.w, ew.h, camL, camT, camR, camB)) continue
                val isOverpass = !ew.isBoardwalk
                // Deep drop shadow onto river or canal below
                drawRect(
                    color = Color.Black.copy(alpha = 0.48f),
                    topLeft = Offset(ew.x + 8f, ew.y + 12f),
                    size = Size(ew.w, ew.h)
                )
                drawRect(
                    color = Color(if (isOverpass) 0xFF282D36 else 0xFF4F3826),
                    topLeft = Offset(ew.x, ew.y),
                    size = Size(ew.w, ew.h)
                )
                if (!isOverpass) {
                    // Cypress plank grooves + wooden support pilings
                    if (ew.w >= ew.h) {
                        var px = ew.x + 10f
                        while (px < ew.x + ew.w) {
                            drawLine(Color(0xFF2A1D13), Offset(px, ew.y), Offset(px, ew.y + ew.h), strokeWidth = 1.5f)
                            px += 12f
                        }
                    } else {
                        var py = ew.y + 10f
                        while (py < ew.y + ew.h) {
                            drawLine(Color(0xFF2A1D13), Offset(ew.x, py), Offset(ew.x + ew.w, py), strokeWidth = 1.5f)
                            py += 12f
                        }
                    }
                    // Timber pilings
                    drawRect(Color(0xFF291D14), Offset(ew.x + 4f, ew.y + ew.h), Size(8f, 12f))
                    drawRect(Color(0xFF291D14), Offset(ew.x + ew.w - 12f, ew.y + ew.h), Size(8f, 12f))
                } else {
                    // Highway overpass dashed lane lines & steel jersey guardrails
                    drawLine(Color(0xFFD99B16), Offset(ew.x + ew.w * 0.5f, ew.y + 10f), Offset(ew.x + ew.w * 0.5f, ew.y + ew.h - 10f), strokeWidth = 3f)
                    drawRect(Color(0xFF64748B), Offset(ew.x, ew.y), Size(6f, ew.h))
                    drawRect(Color(0xFF64748B), Offset(ew.x + ew.w - 6f, ew.y), Size(6f, ew.h))
                }
                drawRect(
                    color = Color(0xFF060508),
                    topLeft = Offset(ew.x, ew.y),
                    size = Size(ew.w, ew.h),
                    style = Stroke(width = 2.6f)
                )
            }

            // 3. Multi-floor interior floors with cross-fade
            val curElev = sim.elevation
            val prevElev = sim.prevElevation
            val fade = sim.floorFadeAlpha
            val isFading = fade < 1f && prevElev != curElev

            for (rf in w.roomFloors) {
                if (!inView(rf.x, rf.y, rf.w, rf.h, camL, camT, camR, camB)) continue
                if (rf.floor == curElev || (isFading && rf.floor == prevElev)) {
                    val alpha = if (rf.floor == curElev) fade else (1f - fade)
                    drawRect(
                        color = Color(rf.colorHex).copy(alpha = alpha),
                        topLeft = Offset(rf.x, rf.y),
                        size = Size(rf.w, rf.h)
                    )
                    val step = 28f
                    var gx = rf.x + step
                    while (gx < rf.x + rf.w) {
                        drawLine(
                            color = Color(rf.stripeHex).copy(alpha = alpha * 0.52f),
                            start = Offset(gx, rf.y),
                            end = Offset(gx, rf.y + rf.h),
                            strokeWidth = 1f
                        )
                        gx += step
                    }
                    var gy = rf.y + step
                    while (gy < rf.y + rf.h) {
                        drawLine(
                            color = Color(rf.stripeHex).copy(alpha = alpha * 0.52f),
                            start = Offset(rf.x, gy),
                            end = Offset(rf.x + rf.w, gy),
                            strokeWidth = 1f
                        )
                        gy += step
                    }
                }
            }

            // 4. Stairwells (Walk-on Directional UP / DOWN Treads with Smooth Cross-Fade)
            for (sw in w.stairwells) {
                val drawFloors = buildList {
                    if (curElev in sw.accessibleFloors) add(curElev to fade)
                    if (isFading && prevElev in sw.accessibleFloors) add(prevElev to (1f - fade))
                }
                if (drawFloors.isEmpty()) continue
                if (!inView(sw.x, sw.y, sw.w, sw.h, camL, camT, camR, camB)) continue

                for ((floorElev, alpha) in drawFloors) {
                    val sorted = sw.accessibleFloors.sorted()
                    val idx = sorted.indexOf(floorElev)
                    val hasUp = idx in 0 until sorted.lastIndex
                    val hasDown = idx > 0
                    val splitHoriz = sw.w >= sw.h

                    drawRect(
                        color = Color(0xFF0C2333).copy(alpha = 0.92f * alpha),
                        topLeft = Offset(sw.x, sw.y),
                        size = Size(sw.w, sw.h)
                    )

                    if (hasUp && hasDown) {
                        val upTopLeft = Offset(sw.x, sw.y)
                        val upSize = if (splitHoriz) Size(sw.w * 0.5f, sw.h) else Size(sw.w, sw.h * 0.5f)
                        val dnTopLeft = if (splitHoriz) Offset(sw.x + sw.w * 0.5f, sw.y) else Offset(sw.x, sw.y + sw.h * 0.5f)
                        val dnSize = upSize

                        drawRect(color = Color(0xFF065F46).copy(alpha = 0.55f * alpha), topLeft = upTopLeft, size = upSize)
                        drawRect(color = Color(0xFF78350F).copy(alpha = 0.55f * alpha), topLeft = dnTopLeft, size = dnSize)

                        // Treads inside UP & DOWN halves
                        var step = 6f
                        while (step < upSize.height - 4f) {
                            drawRect(Color(0xFF34D399).copy(alpha = 0.72f * alpha), Offset(upTopLeft.x + 3f, upTopLeft.y + step), Size(upSize.width - 6f, 2f))
                            drawRect(Color(0xFFFBBF24).copy(alpha = 0.72f * alpha), Offset(dnTopLeft.x + 3f, dnTopLeft.y + step), Size(dnSize.width - 6f, 2f))
                            step += 6f
                        }

                        // Up arrow chevron (left/top half) & Down arrow chevron (right/bottom half)
                        val ucx = upTopLeft.x + upSize.width * 0.5f
                        val ucy = upTopLeft.y + upSize.height * 0.5f
                        val upArrow = Path().apply {
                            moveTo(ucx, ucy - 6f)
                            lineTo(ucx + 6f, ucy + 4f)
                            lineTo(ucx - 6f, ucy + 4f)
                            close()
                        }
                        drawPath(upArrow, color = Color(0xFF6EE7B7).copy(alpha = alpha))

                        val dcx = dnTopLeft.x + dnSize.width * 0.5f
                        val dcy = dnTopLeft.y + dnSize.height * 0.5f
                        val dnArrow = Path().apply {
                            moveTo(dcx, dcy + 6f)
                            lineTo(dcx + 6f, dcy - 4f)
                            lineTo(dcx - 6f, dcy - 4f)
                            close()
                        }
                        drawPath(dnArrow, color = Color(0xFFFDE047).copy(alpha = alpha))
                    } else {
                        val accent = if (hasUp) Color(0xFF38BDF8) else Color(0xFFF59E0B)
                        val tintBg = if (hasUp) Color(0xFF0C4A6E) else Color(0xFF78350F)
                        drawRect(color = tintBg.copy(alpha = 0.68f * alpha), topLeft = Offset(sw.x, sw.y), size = Size(sw.w, sw.h))
                        var sy = sw.y + 5f
                        while (sy < sw.y + sw.h - 4f) {
                            drawRect(color = accent.copy(alpha = 0.62f * alpha), topLeft = Offset(sw.x + 4f, sy), size = Size(sw.w - 8f, 2f))
                            sy += 6f
                        }
                        val cx = sw.x + sw.w * 0.5f
                        val cy = sw.y + sw.h * 0.5f
                        val arrow = Path().apply {
                            if (hasUp) {
                                moveTo(cx, cy - 7f); lineTo(cx + 7f, cy + 5f); lineTo(cx - 7f, cy + 5f); close()
                            } else {
                                moveTo(cx, cy + 7f); lineTo(cx + 7f, cy - 5f); lineTo(cx - 7f, cy - 5f); close()
                            }
                        }
                        drawPath(arrow, color = accent.copy(alpha = alpha))
                    }

                    drawRect(
                        color = Color(0xFF38BDF8).copy(alpha = 0.85f * alpha),
                        topLeft = Offset(sw.x, sw.y),
                        size = Size(sw.w, sw.h),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // 5. Outpost Zero Stations & Extraction Zone Beacons
            if (curElev == 0) {
                for (st in w.stations) {
                    if (!inView(st.x - 40f, st.y - 40f, 80f, 80f, camL, camT, camR, camB)) continue
                    val pulse = 0.72f + 0.25f * sin((now % 2200L) / 2200f * 2f * PI.toFloat())
                    val ringRadius = if (st.kind == StationKind.HELIPAD) 42f else 28f
                    drawCircle(
                        color = Color(st.colorHex).copy(alpha = 0.14f * pulse),
                        radius = ringRadius,
                        center = Offset(st.x, st.y)
                    )
                    drawCircle(
                        color = Color(st.colorHex).copy(alpha = 0.68f),
                        radius = ringRadius - 2f,
                        center = Offset(st.x, st.y),
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                        )
                    )
                }
                for (ez in w.extractionZones) {
                    if (!ez.isOpen) continue
                    if (!inView(ez.x - ez.radius, ez.y - ez.radius, ez.radius * 2f, ez.radius * 2f, camL, camT, camR, camB)) continue
                    val pulse = 0.55f + 0.35f * sin((now % 2000L) / 2000f * 2f * PI.toFloat())
                    drawCircle(
                        color = Color(0xFF10B981).copy(alpha = 0.18f * pulse),
                        radius = ez.radius,
                        center = Offset(ez.x, ez.y)
                    )
                    drawCircle(
                        color = Color(0xFF34D399).copy(alpha = 0.85f),
                        radius = ez.radius,
                        center = Offset(ez.x, ez.y),
                        style = Stroke(width = 3f)
                    )
                }
                if (w.mapId == GameMapId.OAKRIDGE_TOWN) {
                    for (camp in w.scavEncampments) {
                        val r = camp.radius
                        if (!inView(camp.centerX - r, camp.centerY - r, r * 2f, r * 2f, camL, camT, camR, camB)) continue
                        val tierCol = when (camp.dangerTier) {
                            1 -> Color(0xFFF59E0B)
                            2 -> Color(0xFFFB923C)
                            3 -> Color(0xFFEF4444)
                            else -> Color(0xFFA855F7)
                        }
                        drawCircle(
                            color = tierCol.copy(alpha = 0.08f),
                            radius = r,
                            center = Offset(camp.centerX, camp.centerY)
                        )
                        drawCircle(
                            color = tierCol.copy(alpha = 0.45f),
                            radius = r,
                            center = Offset(camp.centerX, camp.centerY),
                            style = Stroke(
                                width = 2.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                            )
                        )
                    }
                    for (downed in sim.downedFriendlyBeacons) {
                        if (downed.downedElevation != curElev) continue
                        if (!inView(downed.downedAtX - 55f, downed.downedAtY - 55f, 110f, 110f, camL, camT, camR, camB)) continue
                        val sosPulse = 0.5f + 0.5f * sin((now % 1100L) / 1100f * 2f * PI.toFloat())
                        drawCircle(
                            color = Color(0xFFEF4444).copy(alpha = 0.25f * sosPulse),
                            radius = 38f + sosPulse * 10f,
                            center = Offset(downed.downedAtX, downed.downedAtY)
                        )
                        drawCircle(
                            color = Color(0xFFFCA5A5).copy(alpha = 0.9f),
                            radius = 22f,
                            center = Offset(downed.downedAtX, downed.downedAtY),
                            style = Stroke(width = 2.5f)
                        )
                        drawCircle(
                            color = Color(downed.vestHex),
                            radius = 10f,
                            center = Offset(downed.downedAtX, downed.downedAtY - 4f)
                        )
                    }
                }
            }

            // Downed Scav / Body Markers (discoverable by patrolling squadmates)
            if (w.mapId == GameMapId.OAKRIDGE_TOWN) {
                for (body in sim.deadBodyMarkers) {
                    if (body.elevation != curElev) continue
                    if (!inView(body.x - 24f, body.y - 24f, 48f, 48f, camL, camT, camR, camB)) continue
                    drawOval(
                        color = Color(0xFF7F1D1D).copy(alpha = 0.52f),
                        topLeft = Offset(body.x - 14f, body.y - 6f),
                        size = Size(28f, 14f)
                    )
                    drawLine(
                        color = Color(0xFFDC2626).copy(alpha = 0.85f),
                        start = Offset(body.x - 7f, body.y - 5f),
                        end = Offset(body.x + 7f, body.y + 5f),
                        strokeWidth = 2.4f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFDC2626).copy(alpha = 0.85f),
                        start = Offset(body.x + 7f, body.y - 5f),
                        end = Offset(body.x - 7f, body.y + 5f),
                        strokeWidth = 2.4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 6. Y-Sorted True Vertical 2.5D Entities, Walls, Doors, Vehicles & Detailed Props
            val queue = ArrayList<DepthDrawable>(220)

            // Loot Containers & Dropped Scav Tactical Packs (with smooth floor cross-fade)
            for (lc in w.lootContainers) {
                if (lc.searched) continue
                if (lc.floor != curElev && !(isFading && lc.floor == prevElev)) continue
                if (!inView(lc.x - 18f, lc.y - 22f, 36f, 40f, camL, camT, camR, camB)) continue
                val alpha = if (lc.floor == curElev) fade else (1f - fade)
                queue.add(DepthDrawable(lc.y) {
                    drawOval(color = Color.Black.copy(alpha = 0.35f * alpha), topLeft = Offset(lc.x - 12f, lc.y - 3f), size = Size(24f, 8f))
                    drawRect(color = Color(0xFF92400E).copy(alpha = alpha), topLeft = Offset(lc.x - 11f, lc.y - 16f), size = Size(22f, 14f))
                    drawRect(color = Color(0xFFF59E0B).copy(alpha = alpha), topLeft = Offset(lc.x - 11f, lc.y - 21f), size = Size(22f, 7f))
                    drawRect(color = Color(0xFFFDE68A).copy(alpha = alpha), topLeft = Offset(lc.x - 3f, lc.y - 16f), size = Size(6f, 4f))
                    drawRect(
                        color = Color(0xFFFDE68A).copy(alpha = 0.7f * alpha),
                        topLeft = Offset(lc.x - 11f, lc.y - 21f),
                        size = Size(22f, 19f),
                        style = Stroke(width = 1.4f)
                    )
                })
            }
            for (pack in sim.dynamicLootDrops) {
                if (pack.searched) continue
                if (pack.floor != curElev && !(isFading && pack.floor == prevElev)) continue
                if (!inView(pack.x - 18f, pack.y - 22f, 36f, 40f, camL, camT, camR, camB)) continue
                val alpha = if (pack.floor == curElev) fade else (1f - fade)
                queue.add(DepthDrawable(pack.y) {
                    drawOval(color = Color.Black.copy(alpha = 0.35f * alpha), topLeft = Offset(pack.x - 10f, pack.y - 2f), size = Size(20f, 7f))
                    drawRoundRect(
                        color = Color(0xFF3F6212).copy(alpha = alpha),
                        topLeft = Offset(pack.x - 9f, pack.y - 14f),
                        size = Size(18f, 13f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    drawRoundRect(
                        color = Color(0xFFBEF264).copy(alpha = 0.85f * alpha),
                        topLeft = Offset(pack.x - 9f, pack.y - 14f),
                        size = Size(18f, 13f),
                        cornerRadius = CornerRadius(3f, 3f),
                        style = Stroke(width = 1.5f)
                    )
                })
            }

            // Detailed 16-bit Pixel Props, Trees, Vehicles, TVs, Stoves, Steve's Music, Planes & Canoes
            for (pr in w.props) {
                if (pr.cleared) continue
                if (!inView(pr.x, pr.y, pr.w, pr.h, camL, camT, camR, camB)) continue
                if (pr.floor == curElev || (isFading && pr.floor == prevElev)) {
                    val alpha = if (pr.floor == curElev) fade else (1f - fade)
                    val powerOnNow = now >= sim.blackoutUntilMs && now >= sim.rareFlickerUntilMs
                    queue.add(DepthDrawable(pr.y + pr.h) {
                        with(PixelArtSprites) {
                            drawPixelProp(pr, alpha, now, powerOnNow, oblY = -oblY)
                        }
                    })
                }
            }

            // Walls (True Vertical 2.5D Extrusion with timber palisade, brick, concrete & storefront textures)
            for (wall in w.walls) {
                if (!inView(wall.x, wall.y, wall.w, wall.h, camL, camT, camR, camB)) continue
                if (wall.floor == curElev || (isFading && wall.floor == prevElev)) {
                    val alpha = if (wall.floor == curElev) fade else (1f - fade)
                    queue.add(DepthDrawable(wall.y + wall.h) {
                        if (wall.isFence && wall.colorHex == 0xFF4A3728L) {
                            // Splash-authentic Fortified Timber Palisade (notched split logs & iron cross-beams)
                            val fh = -oblY * 0.86f
                            drawRect(
                                color = Color.Black.copy(alpha = 0.42f * alpha),
                                topLeft = Offset(wall.x + 4f, wall.y + 4f),
                                size = Size(wall.w, wall.h)
                            )
                            val logDark = Color(0xFF2B1E16).copy(alpha = alpha)
                            val logMid = Color(0xFF4A3728).copy(alpha = alpha)
                            val logTop = Color(wall.topColorHex).copy(alpha = alpha)
                            if (wall.w >= wall.h) {
                                // Horizontal run of upright sharpened timber stakes
                                drawRect(color = logMid, topLeft = Offset(wall.x, wall.y + wall.h - fh), size = Size(wall.w, fh))
                                var lx = wall.x
                                while (lx < wall.x + wall.w) {
                                    val sw = minOf(12f, wall.x + wall.w - lx)
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(logDark, logTop, logDark),
                                            startX = lx,
                                            endX = lx + sw
                                        ),
                                        topLeft = Offset(lx, wall.y - fh),
                                        size = Size(sw, fh + wall.h)
                                    )
                                    drawLine(
                                        color = PixelArtSprites.OUTLINE.copy(alpha = 0.85f * alpha),
                                        start = Offset(lx, wall.y - fh),
                                        end = Offset(lx, wall.y + wall.h),
                                        strokeWidth = 1.4f
                                    )
                                    lx += 12f
                                }
                                // Heavy horizontal cross-beam & iron bolt rivets
                                drawRect(
                                    color = Color(0xFF31241B).copy(alpha = alpha),
                                    topLeft = Offset(wall.x, wall.y + wall.h - fh * 0.42f),
                                    size = Size(wall.w, 6f)
                                )
                            } else {
                                // Vertical run of staggered timber logs
                                drawRect(color = logMid, topLeft = Offset(wall.x, wall.y + wall.h - fh), size = Size(wall.w, fh))
                                var ly = wall.y
                                while (ly < wall.y + wall.h) {
                                    val sh = minOf(12f, wall.y + wall.h - ly)
                                    drawRect(
                                        color = logTop,
                                        topLeft = Offset(wall.x, ly - fh),
                                        size = Size(wall.w, sh + fh * 0.15f)
                                    )
                                    drawLine(
                                        color = PixelArtSprites.OUTLINE.copy(alpha = 0.78f * alpha),
                                        start = Offset(wall.x, ly - fh),
                                        end = Offset(wall.x + wall.w, ly - fh),
                                        strokeWidth = 1.4f
                                    )
                                    ly += 12f
                                }
                                drawRect(
                                    color = Color(0xFF31241B).copy(alpha = alpha),
                                    topLeft = Offset(wall.x, wall.y + wall.h - fh),
                                    size = Size(wall.w, fh)
                                )
                            }
                            drawRect(
                                color = PixelArtSprites.OUTLINE.copy(alpha = 0.90f * alpha),
                                topLeft = Offset(wall.x, wall.y - fh),
                                size = Size(wall.w, fh + wall.h),
                                style = Stroke(width = 1.8f)
                            )
                        } else if (wall.isFence) {
                            val fh = -oblY * 0.72f
                            drawRect(
                                color = Color(wall.colorHex).copy(alpha = 0.42f * alpha),
                                topLeft = Offset(wall.x, wall.y + wall.h - fh),
                                size = Size(wall.w, fh)
                            )
                            drawRect(
                                color = Color(wall.topColorHex).copy(alpha = alpha),
                                topLeft = Offset(wall.x, wall.y - fh),
                                size = Size(wall.w, wall.h)
                            )
                            val postStep = 44f
                            var px = wall.x
                            while (px <= wall.x + wall.w) {
                                drawRect(
                                    color = Color(0xFF94A3B8).copy(alpha = 0.85f * alpha),
                                    topLeft = Offset(px, wall.y - fh),
                                    size = Size(4f, fh + wall.h)
                                )
                                px += postStep
                            }
                        } else if (wall.isWindow) {
                            drawRect(
                                color = Color(wall.colorHex).copy(alpha = alpha),
                                topLeft = Offset(wall.x, wall.y + wall.h + oblY),
                                size = Size(wall.w, 8f)
                            )
                            drawRect(
                                color = Color(wall.colorHex).copy(alpha = alpha),
                                topLeft = Offset(wall.x, wall.y + wall.h - 8f),
                                size = Size(wall.w, 8f)
                            )
                            drawRect(
                                color = Color(0xFF38BDF8).copy(alpha = 0.34f * alpha),
                                topLeft = Offset(wall.x, wall.y + wall.h + oblY + 8f),
                                size = Size(wall.w, -oblY - 16f)
                            )
                            if (wall.w >= 20f) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.38f * alpha),
                                    start = Offset(wall.x + 5f, wall.y + wall.h + oblY + 9f),
                                    end = Offset(wall.x + 15f, wall.y + wall.h - 9f),
                                    strokeWidth = 1.6f
                                )
                            }
                            drawRect(
                                color = Color(wall.topColorHex).copy(alpha = alpha),
                                topLeft = Offset(wall.x, wall.y + oblY),
                                size = Size(wall.w, wall.h)
                            )
                            drawRect(
                                color = PixelArtSprites.OUTLINE.copy(alpha = alpha),
                                topLeft = Offset(wall.x, wall.y + oblY),
                                size = Size(wall.w, wall.h - oblY),
                                style = Stroke(width = 2f)
                            )
                        } else {
                            drawRect(
                                color = Color.Black.copy(alpha = 0.44f * alpha),
                                topLeft = Offset(wall.x + 5f, wall.y + 5f),
                                size = Size(wall.w, wall.h)
                            )
                            drawRect(
                                color = Color(wall.colorHex).copy(alpha = alpha),
                                topLeft = Offset(wall.x, wall.y + wall.h + oblY),
                                size = Size(wall.w, -oblY)
                            )
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.34f * alpha)),
                                    startY = wall.y + wall.h + oblY,
                                    endY = wall.y + wall.h
                                ),
                                topLeft = Offset(wall.x, wall.y + wall.h + oblY),
                                size = Size(wall.w, -oblY)
                            )
                            drawLine(
                                color = PixelArtSprites.OUTLINE.copy(alpha = 0.45f * alpha),
                                start = Offset(wall.x, wall.y + wall.h + oblY * 0.66f),
                                end = Offset(wall.x + wall.w, wall.y + wall.h + oblY * 0.66f),
                                strokeWidth = 1.2f
                            )
                            drawLine(
                                color = PixelArtSprites.OUTLINE.copy(alpha = 0.45f * alpha),
                                start = Offset(wall.x, wall.y + wall.h + oblY * 0.33f),
                                end = Offset(wall.x + wall.w, wall.y + wall.h + oblY * 0.33f),
                                strokeWidth = 1.2f
                            )
                            drawRect(
                                color = Color(wall.topColorHex).copy(alpha = alpha),
                                topLeft = Offset(wall.x, wall.y + oblY),
                                size = Size(wall.w, wall.h)
                            )
                            drawRect(
                                color = Color(0xFFFFF7E6).copy(alpha = 0.14f * alpha),
                                topLeft = Offset(wall.x + 1f, wall.y + oblY + 1f),
                                size = Size(max(0f, wall.w - 2f), 2.2f)
                            )
                            drawRect(
                                color = PixelArtSprites.OUTLINE.copy(alpha = alpha),
                                topLeft = Offset(wall.x, wall.y + oblY),
                                size = Size(wall.w, wall.h),
                                style = Stroke(width = 2.2f)
                            )
                            drawRect(
                                color = PixelArtSprites.OUTLINE.copy(alpha = 0.88f * alpha),
                                topLeft = Offset(wall.x, wall.y + wall.h + oblY),
                                size = Size(wall.w, -oblY),
                                style = Stroke(width = 1.8f)
                            )
                        }
                    })
                }
            }

            // Building-Matched Pixel Doors (Wood, Double Sliding Glass, Roll-Up Garage, Steel Security)
            for (d in w.doors) {
                if (!inView(d.hingeX - d.length, d.hingeY - d.length, d.length * 2f, d.length * 2f, camL, camT, camR, camB)) continue
                if (d.floor == curElev || (isFading && d.floor == prevElev)) {
                    val alpha = if (d.floor == curElev) fade else (1f - fade)
                    val tipY = d.hingeY + sin(d.angle) * d.length
                    queue.add(DepthDrawable(max(d.hingeY, tipY)) {
                        val jammed = d.jammedByPropId != null && w.props.any { it.id == d.jammedByPropId && !it.cleared }
                        with(PixelArtSprites) {
                            drawPixelDoor(d, alpha, jammed, oblY = -oblY)
                        }
                    })
                }
            }

            // Infected Survivors (Splash-matched gaunt necrotic anatomy, stained clothing & Frenzy crimson flare)
            val frenzyGlowActive = sim.isFrenzyActive
            for (z in sim.zombies) {
                if (z.elevation != curElev && !(isFading && z.elevation == prevElev)) continue
                if (!inView(z.x - 24f, z.y - 42f, 48f, 58f, camL, camT, camR, camB)) continue
                val alpha = if (z.elevation == curElev) fade else (1f - fade)
                val hitFlash = now < z.hitFlashUntilMs
                val bob = sin((now % 420L) / 420f * 2f * PI.toFloat() + z.id) * 1.3f
                val isFrenzied = z.isFrenzy || frenzyGlowActive
                queue.add(DepthDrawable(z.y) {
                    val scale = when (z.variant) {
                        ZombieVariant.ARMORED -> 1.16f
                        ZombieVariant.SPRINTER -> 0.94f
                        ZombieVariant.WALKER -> 1.0f
                    }
                    if (isFrenzied) {
                        val pulse = 0.48f + 0.42f * sin((now % 650L) / 650f * 2f * PI.toFloat() + z.id)
                        drawCircle(
                            color = Color(0xFFDC2626).copy(alpha = 0.24f * pulse * alpha),
                            radius = 20f * scale,
                            center = Offset(z.x, z.y - 8f)
                        )
                        drawCircle(
                            color = Color(0xFFEF4444).copy(alpha = 0.65f * pulse * alpha),
                            radius = 16f * scale,
                            center = Offset(z.x, z.y + 1f),
                            style = Stroke(width = 1.6f)
                        )
                    }
                    // Soft cast shadow & ragged trouser stride
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.56f * alpha), Color.Transparent),
                            center = Offset(z.x, z.y + 3f),
                            radius = 15f * scale
                        ),
                        topLeft = Offset(z.x - 13f * scale, z.y - 2f),
                        size = Size(26f * scale, 12f * scale)
                    )
                    val stride = sin((now % 380L) / 380f * 2f * PI.toFloat() + z.id * 1.7f) * 3.0f * scale
                    val pantsCol = if (hitFlash) Color.White else Color(0xFF1F242A)
                    drawRoundRect(
                        color = pantsCol.copy(alpha = alpha),
                        topLeft = Offset(z.x - 5.2f * scale, z.y - 8f + stride),
                        size = Size(4.4f * scale, 8.5f * scale),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    drawRoundRect(
                        color = pantsCol.copy(alpha = alpha),
                        topLeft = Offset(z.x + 0.8f * scale, z.y - 8f - stride),
                        size = Size(4.4f * scale, 8.5f * scale),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    rotate(degrees = z.angle * 180f / PI.toFloat(), pivot = Offset(z.x, z.y - 14f + bob)) {
                        val coatDark = if (hitFlash) Color.White else when (z.variant) {
                            ZombieVariant.ARMORED -> Color(0xFF1E262E)
                            ZombieVariant.SPRINTER -> Color(0xFF3A1D1B)
                            ZombieVariant.WALKER -> Color(0xFF282D26)
                        }
                        val coatLight = if (hitFlash) Color.White else when (z.variant) {
                            ZombieVariant.ARMORED -> Color(0xFF3E4A56)
                            ZombieVariant.SPRINTER -> Color(0xFF5D342E)
                            ZombieVariant.WALKER -> Color(0xFF444A3D)
                        }
                        val fleshDark = if (hitFlash) Color.White else when (z.variant) {
                            ZombieVariant.ARMORED -> Color(0xFF474E49)
                            ZombieVariant.SPRINTER -> Color(0xFF5D534B)
                            ZombieVariant.WALKER -> Color(0xFF4F574A)
                        }
                        val fleshLight = if (hitFlash) Color.White else when (z.variant) {
                            ZombieVariant.ARMORED -> Color(0xFF6B736C)
                            ZombieVariant.SPRINTER -> Color(0xFF7D7066)
                            ZombieVariant.WALKER -> Color(0xFF6D7667)
                        }

                        // Asymmetrical lunging gaunt arms with torn sleeves
                        drawRoundRect(
                            color = coatDark.copy(alpha = alpha),
                            topLeft = Offset(z.x + 1f, z.y - 23.5f + bob),
                            size = Size(7f * scale, 4.2f * scale),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                        drawRoundRect(
                            color = fleshLight.copy(alpha = alpha),
                            topLeft = Offset(z.x + 6.5f * scale, z.y - 23.2f + bob),
                            size = Size(7.5f * scale, 3.2f * scale),
                            cornerRadius = CornerRadius(1.5f, 1.5f)
                        )
                        drawRoundRect(
                            color = coatDark.copy(alpha = alpha),
                            topLeft = Offset(z.x + 1f, z.y - 8.5f + bob),
                            size = Size(6.8f * scale, 4.2f * scale),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                        drawRoundRect(
                            color = fleshLight.copy(alpha = alpha),
                            topLeft = Offset(z.x + 6.2f * scale, z.y - 8.2f + bob),
                            size = Size(7.8f * scale, 3.2f * scale),
                            cornerRadius = CornerRadius(1.5f, 1.5f)
                        )

                        // Volumetric hunched coat/torso + tatters
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(coatDark.copy(alpha = alpha), coatLight.copy(alpha = alpha)),
                                startX = z.x - 8f * scale,
                                endX = z.x + 8f * scale
                            ),
                            topLeft = Offset(z.x - 7.5f * scale, z.y - 23.5f + bob),
                            size = Size(14.5f * scale, 19.5f * scale),
                            cornerRadius = CornerRadius(5f * scale, 5f * scale)
                        )
                        drawRoundRect(
                            color = PixelArtSprites.OUTLINE.copy(alpha = 0.85f * alpha),
                            topLeft = Offset(z.x - 7.5f * scale, z.y - 23.5f + bob),
                            size = Size(14.5f * scale, 19.5f * scale),
                            cornerRadius = CornerRadius(5f * scale, 5f * scale),
                            style = Stroke(width = 1.6f)
                        )
                        if (z.variant == ZombieVariant.ARMORED) {
                            drawRoundRect(
                                color = Color(0xFF475569).copy(alpha = alpha),
                                topLeft = Offset(z.x - 5.5f * scale, z.y - 22f + bob),
                                size = Size(11f * scale, 16.5f * scale),
                                cornerRadius = CornerRadius(3f, 3f),
                                style = Stroke(width = 2.0f)
                            )
                        }

                        // Sculpted gaunt cranium with shadowed eye hollows & sparse hair
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(fleshLight.copy(alpha = alpha), fleshDark.copy(alpha = alpha)),
                                center = Offset(z.x + 2.5f * scale, z.y - 14.5f + bob),
                                radius = 9f * scale
                            ),
                            topLeft = Offset(z.x - 6.2f * scale, z.y - 21f * scale + bob),
                            size = Size(13.8f * scale, 13.6f * scale)
                        )
                        drawOval(
                            color = PixelArtSprites.OUTLINE.copy(alpha = 0.90f * alpha),
                            topLeft = Offset(z.x - 6.2f * scale, z.y - 21f * scale + bob),
                            size = Size(13.8f * scale, 13.6f * scale),
                            style = Stroke(width = 1.5f)
                        )
                        drawArc(
                            color = Color(0xFF1A1715).copy(alpha = 0.88f * alpha),
                            startAngle = 100f,
                            sweepAngle = 160f,
                            useCenter = true,
                            topLeft = Offset(z.x - 6.2f * scale, z.y - 21f * scale + bob),
                            size = Size(13.6f * scale, 13.4f * scale)
                        )
                        drawOval(
                            color = Color(0xFF140E0D).copy(alpha = 0.85f * alpha),
                            topLeft = Offset(z.x + 2.8f * scale, z.y - 17.5f + bob),
                            size = Size(2.8f * scale, 2.2f * scale)
                        )
                        drawOval(
                            color = Color(0xFF140E0D).copy(alpha = 0.85f * alpha),
                            topLeft = Offset(z.x + 2.8f * scale, z.y - 13.2f + bob),
                            size = Size(2.8f * scale, 2.2f * scale)
                        )
                        if (isFrenzied) {
                            drawCircle(
                                color = Color(0xFFFF2E2E).copy(alpha = 0.95f * alpha),
                                radius = 1.4f * scale,
                                center = Offset(z.x + 4.1f * scale, z.y - 16.4f + bob)
                            )
                            drawCircle(
                                color = Color(0xFFFF2E2E).copy(alpha = 0.95f * alpha),
                                radius = 1.4f * scale,
                                center = Offset(z.x + 4.1f * scale, z.y - 12.1f + bob)
                            )
                        }
                    }
                    if (z.hp < z.maxHp) {
                        val frac = (z.hp / z.maxHp).coerceIn(0f, 1f)
                        drawRect(color = Color(0xFF060508).copy(alpha = alpha), topLeft = Offset(z.x - 13f, z.y - 34f), size = Size(26f, 4.5f))
                        drawRect(color = Color(0xFFB91C1C).copy(alpha = alpha), topLeft = Offset(z.x - 12f, z.y - 33f), size = Size(24f * frac, 2.5f))
                    }
                })
            }

            // Hostile Scav Squads (Volumetric combatants, tactical cover crouch, kinship & AI badges)
            for (r in sim.rivalOperators) {
                if (r.elevation != curElev && !(isFading && r.elevation == prevElev)) continue
                if (!inView(r.x - 32f, r.y - 52f, 64f, 70f, camL, camT, camR, camB)) continue
                val alpha = if (r.elevation == curElev) fade else (1f - fade)
                val hitFlash = now < r.hitFlashUntilMs
                val isCoverCrouch = r.inCover || r.aiState == ScavAiState.TAKE_COVER
                queue.add(DepthDrawable(r.y) {
                    val oy = oblY * 0.52f + (if (isCoverCrouch) 4f else 0f)
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.55f * alpha), Color.Transparent),
                            center = Offset(r.x, r.y + 3f),
                            radius = 16f
                        ),
                        topLeft = Offset(r.x - 14f, r.y - 3f),
                        size = Size(28f, 14f)
                    )
                    val ringCol = when {
                        r.isVengefulKin -> Color(0xFFC084FC)
                        r.aiState == ScavAiState.ENGAGE -> Color(0xFFDC2626)
                        r.aiState == ScavAiState.TAKE_COVER -> Color(0xFF38BDF8)
                        r.aiState == ScavAiState.RETREAT -> Color(0xFFFB923C)
                        r.aiState == ScavAiState.INVESTIGATE -> Color(0xFFFBBF24)
                        else -> Color(0xFF94A3B8)
                    }
                    drawCircle(
                        color = ringCol.copy(alpha = 0.72f * alpha),
                        radius = if (isCoverCrouch) 17.5f else 15.5f,
                        center = Offset(r.x, r.y + 2f),
                        style = Stroke(width = if (isCoverCrouch) 2.4f else 1.7f)
                    )
                    // Boots & trousers
                    drawRoundRect(color = Color(0xFF1F242B).copy(alpha = alpha), topLeft = Offset(r.x - 5.5f, r.y - 8f), size = Size(4.6f, 8.5f), cornerRadius = CornerRadius(2f, 2f))
                    drawRoundRect(color = Color(0xFF1F242B).copy(alpha = alpha), topLeft = Offset(r.x + 0.9f, r.y - 8f), size = Size(4.6f, 8.5f), cornerRadius = CornerRadius(2f, 2f))
                    val shirtTop = if (hitFlash) Color.White else Color(r.shirtColorHex)
                    val vestTop = if (hitFlash) Color.White else Color(r.vestColorHex)
                    val skinCol = if (hitFlash) Color.White else Color(r.skinColorHex)
                    translate(left = r.x, top = r.y + oy * 0.62f) {
                        rotate(degrees = r.angle * 180f / PI.toFloat(), pivot = Offset.Zero) {
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(shirtTop.copy(alpha = alpha * 0.72f), shirtTop.copy(alpha = alpha)),
                                    startX = -7f,
                                    endX = 7f
                                ),
                                topLeft = Offset(-6.5f, -12.5f),
                                size = Size(13f, 25f),
                                cornerRadius = CornerRadius(5f, 5f)
                            )
                            drawRoundRect(
                                color = vestTop.copy(alpha = alpha),
                                topLeft = Offset(-5.5f, -10.5f),
                                size = Size(11f, 21f),
                                cornerRadius = CornerRadius(3.5f, 3.5f)
                            )
                            drawRoundRect(
                                color = PixelArtSprites.OUTLINE.copy(alpha = 0.9f * alpha),
                                topLeft = Offset(-6.5f, -12.5f),
                                size = Size(13f, 25f),
                                cornerRadius = CornerRadius(5f, 5f),
                                style = Stroke(width = 1.8f)
                            )
                            drawHeldWeapon(r.weapon, 1f)
                            drawCircle(color = PixelArtSprites.OUTLINE.copy(alpha = alpha), radius = 8.4f, center = Offset.Zero)
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(skinCol.copy(alpha = alpha), Color(0xFF3E2A22).copy(alpha = alpha)),
                                    center = Offset(1.5f, -0.5f),
                                    radius = 8.5f
                                ),
                                topLeft = Offset(-7.4f, -7.2f),
                                size = Size(14.8f, 14.4f)
                            )
                            drawArc(
                                color = Color(if (r.isVengefulKin) 0xFF6B21A8 else 0xFF7F1D1D).copy(alpha = alpha),
                                startAngle = 95f,
                                sweepAngle = 170f,
                                useCenter = true,
                                topLeft = Offset(-7.6f, -7.6f),
                                size = Size(15.2f, 15.2f)
                            )
                        }
                    }
                    val frac = (r.hp / r.maxHp).coerceIn(0f, 1f)
                    drawRect(color = Color(0xFF060508).copy(alpha = alpha), topLeft = Offset(r.x - 16f, r.y - 39f), size = Size(32f, 5.5f))
                    drawRect(color = ringCol.copy(alpha = alpha), topLeft = Offset(r.x - 15f, r.y - 38f), size = Size(30f * frac, 3.5f))
                })
            }

            // Outpost Zero Talkable Survivor NPCs & Autonomous Crew + Solo K-9 in BOTH Maps
            if (w.mapId == GameMapId.OUTPOST_ZERO) {
                for (placed in w.survivorNpcs) {
                    if (!inView(placed.x - 24f, placed.y - 44f, 48f, 60f, camL, camT, camR, camB)) continue
                    val def = SurvivorCatalog.npcById(placed.npcId)
                    val breath = sin((now % 2600L) / 2600f * 2f * PI.toFloat()) * 0.9f
                    queue.add(DepthDrawable(placed.y) {
                        drawSurvivorNpcRig(
                            px = placed.x,
                            py = placed.y,
                            shirtHex = def.shirtHex,
                            vestHex = def.vestHex,
                            skinHex = def.skinHex,
                            accentHex = def.accentHex,
                            oblY = oblY,
                            breath = breath,
                            now = now
                        )
                    })
                }

                for (npc in sim.baseNpcs) {
                    val op = sim.rosterOperators.firstOrNull { it.id == npc.operatorId } ?: continue
                    if (!inView(npc.x - 24f, npc.y - 36f, 48f, 52f, camL, camT, camR, camB)) continue
                    queue.add(DepthDrawable(npc.y) {
                        drawOperatorRig(
                            px = npc.x,
                            py = npc.y,
                            angle = npc.angle,
                            profile = op,
                            weapon = sim.unlockedWeapons.lastOrNull() ?: WeaponId.KNIFE,
                            stepPhase = npc.stepTimer,
                            scale = 0.96f,
                            oblY = oblY
                        )
                        val badgeColor = when (npc.statusTag) {
                            "NO WATER!", "NO FOOD!" -> Color(0xFFEF4444)
                            "GET WATER", "DRANK" -> Color(0xFF38BDF8)
                            "GET FOOD", "ATE" -> Color(0xFF34D399)
                            "REST", "RESTING" -> Color(0xFFF59E0B)
                            else -> null
                        }
                        if (badgeColor != null) {
                            drawCircle(color = Color(0xFF0B0F0C), radius = 4.6f, center = Offset(npc.x, npc.y - 38f))
                            drawCircle(color = badgeColor, radius = 3.0f, center = Offset(npc.x, npc.y - 38f))
                        }
                    })
                }
            }

            // Solo Operator's K-9 Companion (Deploys in BOTH Outpost Zero AND DeLaine!)
            val activeOp = sim.activeOperator
            if (activeOp != null) {
                queue.add(DepthDrawable(sim.dogY) {
                    drawDogCompanion(
                        dx = sim.dogX,
                        dy = sim.dogY,
                        angle = sim.dogAngle,
                        coatHex = activeOp.dogCoatColorHex,
                        stepPhase = sim.dogStepTimer,
                        barking = now < sim.dogBarkUntilMs
                    )
                })

                // Active Player Operator (with Armor Plates + Sweeping Melee Animation)
                val swingOffsetRad = if (sim.isSwingingMelee) {
                    val p = sim.meleeSwingProgress
                    val sweep = -1.25f + p * 2.50f
                    sweep * sim.meleeSwingDir
                } else 0f

                queue.add(DepthDrawable(sim.playerY) {
                    if (sim.isSwingingMelee) {
                        drawMeleeSlashArc(
                            px = sim.playerX,
                            py = sim.playerY - 12f,
                            playerAngle = sim.playerAngle,
                            weapon = sim.meleeSwingWeapon,
                            progress = sim.meleeSwingProgress,
                            dir = sim.meleeSwingDir
                        )
                    }
                    drawOperatorRig(
                        px = sim.playerX,
                        py = sim.playerY,
                        angle = sim.playerAngle,
                        profile = activeOp,
                        weapon = sim.currentWeapon,
                        stepPhase = sim.stepTimer,
                        scale = 1f,
                        armorTier = sim.armorTier,
                        weaponSwingOffsetRad = swingOffsetRad,
                        equippedAttachments = sim.weaponAttachments[sim.currentWeapon.key].orEmpty(),
                        oblY = oblY
                    )
                })
            }

            queue.sortBy { it.sortY }
            for (item in queue) item.draw(this)

            // 7. Splash-Matched Weathered Roofs with shingle/corrugated seams, soft gradient vignette & HVAC (Fade smoothly on entry)
            for (b in w.buildings) {
                if (b.roofAlpha <= 0.02f) continue
                if (!inView(b.bounds.x, b.bounds.y, b.bounds.w, b.bounds.h, camL, camT, camR, camB)) continue
                val topLeft = Offset(b.bounds.x, b.bounds.y + oblY)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(b.roofColorHex).copy(alpha = b.roofAlpha),
                            Color(0xFF14181E).copy(alpha = 0.86f * b.roofAlpha)
                        ),
                        startY = b.bounds.y + oblY,
                        endY = b.bounds.y + oblY + b.bounds.h
                    ),
                    topLeft = topLeft,
                    size = Size(b.bounds.w, b.bounds.h)
                )
                var rx = b.bounds.x + 26f
                while (rx < b.bounds.x + b.bounds.w - 8f) {
                    drawLine(
                        color = PixelArtSprites.OUTLINE.copy(alpha = 0.36f * b.roofAlpha),
                        start = Offset(rx, b.bounds.y + oblY),
                        end = Offset(rx, b.bounds.y + oblY + b.bounds.h),
                        strokeWidth = 1.4f
                    )
                    rx += 26f
                }
                // Parapet lip border + rooftop HVAC unit & pipe vent
                drawRect(
                    color = Color(0xFF3E4752).copy(alpha = 0.88f * b.roofAlpha),
                    topLeft = Offset(b.bounds.x + 22f, b.bounds.y + oblY + 20f),
                    size = Size(34f, 24f)
                )
                drawRect(
                    color = PixelArtSprites.OUTLINE.copy(alpha = b.roofAlpha),
                    topLeft = Offset(b.bounds.x + 22f, b.bounds.y + oblY + 20f),
                    size = Size(34f, 24f),
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = PixelArtSprites.OUTLINE.copy(alpha = 0.72f * b.roofAlpha),
                    radius = 7f,
                    center = Offset(b.bounds.x + 39f, b.bounds.y + oblY + 32f),
                    style = Stroke(width = 1.6f)
                )
                drawRect(
                    color = Color(0xFFFFF7E6).copy(alpha = 0.14f * b.roofAlpha),
                    topLeft = Offset(b.bounds.x + 2f, b.bounds.y + oblY + 2f),
                    size = Size(max(0f, b.bounds.w - 4f), 3f)
                )
                drawRect(
                    color = PixelArtSprites.OUTLINE.copy(alpha = b.roofAlpha),
                    topLeft = topLeft,
                    size = Size(b.bounds.w, b.bounds.h),
                    style = Stroke(width = 3.0f)
                )
            }

            // 8. Streetlight Poles (glow from dusk through dawn when power grid is online)
            if (curElev == 0) {
                for (sl in w.streetLights) {
                    if (!inView(sl.x - 14f, sl.y - 46f, 28f, 52f, camL, camT, camR, camB)) continue
                    drawRect(color = Color(0xFF47505A), topLeft = Offset(sl.x - 2f, sl.y - 36f), size = Size(4f, 36f))
                    val bulbOn = atmosphere.streetLightsActive && !inBlackout && (!sl.isBrokenBulb || (now / 420L) % 5L != 0L)
                    drawRect(
                        color = if (bulbOn) Color(0xFFFDE68A) else Color(0xFF39404A),
                        topLeft = Offset(sl.x - 4f, sl.y - 38f),
                        size = Size(8f, 4f)
                    )
                    if (bulbOn) {
                        drawCircle(
                            color = Color(0xFFFDE68A).copy(alpha = 0.24f),
                            radius = 12f,
                            center = Offset(sl.x, sl.y - 36f)
                        )
                    }
                }
            }

            // 9. Muzzle Flashes, Tracers & Ambient Wind Particles
            for (mf in sim.muzzleFlashes) {
                drawCircle(color = Color(mf.colorHex), radius = mf.size, center = Offset(mf.x, mf.y - 14f))
            }
            for (b in sim.bullets) {
                if (b.elevation != curElev) continue
                drawLine(
                    color = Color(b.colorHex),
                    start = Offset(b.x, b.y - 14f),
                    end = Offset(b.x - b.vx * 1.4f, b.y - 14f - b.vy * 1.4f),
                    strokeWidth = 2.5f
                )
            }
            for (p in sim.particles) {
                if (!inView(p.x - 12f, p.y - 12f, 24f, 24f, camL, camT, camR, camB)) continue
                val a = (p.life / p.maxLife).coerceIn(0f, 1f)
                if (p.isPaper) {
                    rotate(degrees = p.rot * 57.3f, pivot = Offset(p.x, p.y)) {
                        drawRect(
                            color = Color(p.colorHex).copy(alpha = 0.85f * a),
                            topLeft = Offset(p.x - p.size * 0.6f, p.y - p.size * 0.4f),
                            size = Size(p.size * 1.2f, p.size * 0.85f)
                        )
                    }
                } else if (p.isLeaf) {
                    rotate(degrees = p.rot * 57.3f, pivot = Offset(p.x, p.y)) {
                        drawOval(
                            color = Color(p.colorHex).copy(alpha = 0.9f * a),
                            topLeft = Offset(p.x - p.size, p.y - p.size * 0.5f),
                            size = Size(p.size * 2f, p.size)
                        )
                    }
                } else {
                    drawCircle(
                        color = Color(p.colorHex).copy(alpha = a),
                        radius = p.size,
                        center = Offset(p.x, p.y - 10f)
                    )
                }
            }

            // Comic-Book Graphic-Novel Impact Starbursts & Bold Outlined Damage Popups
            if (sim.damagePopups.isNotEmpty()) {
                val paintStroke = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 5f
                    textSize = 20f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                val paintText = android.graphics.Paint().apply {
                    textSize = 20f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                for (pop in sim.damagePopups) {
                    val frac = (pop.life / pop.maxLife).coerceIn(0f, 1f)
                    val aInt = (frac * 255).toInt()
                    val label = if (pop.amount >= 9999) "CRIT!" else "${pop.amount}"
                    if (pop.isCrit || pop.amount >= 9999) {
                        val sp = Path().apply {
                            val rOuter = 20f * (0.75f + 0.35f * frac)
                            val rInner = 9f
                            for (k in 0..9) {
                                val rad = k * PI.toFloat() / 5f
                                val r = if (k % 2 == 0) rOuter else rInner
                                val px = pop.x + cos(rad) * r
                                val py = pop.y - 6f + sin(rad) * r
                                if (k == 0) moveTo(px, py) else lineTo(px, py)
                            }
                            close()
                        }
                        drawPath(sp, Color(0xFFF59E0B).copy(alpha = 0.85f * frac))
                        drawPath(sp, PixelArtSprites.OUTLINE.copy(alpha = frac), style = Stroke(width = 1.8f))
                    }
                    paintStroke.alpha = aInt
                    paintText.color = if (pop.isCrit) {
                        android.graphics.Color.rgb(248, 113, 113)
                    } else {
                        android.graphics.Color.rgb(253, 224, 71)
                    }
                    paintText.alpha = aInt
                    drawContext.canvas.nativeCanvas.drawText(label, pop.x, pop.y, paintStroke)
                    drawContext.canvas.nativeCanvas.drawText(label, pop.x, pop.y, paintText)
                }
            }

            // Scav Nameplates, AI State Badges & Tactical Callouts (above roofs/actors for clarity)
            if (w.mapId == GameMapId.OAKRIDGE_TOWN && sim.rivalOperators.isNotEmpty()) {
                val tagStroke = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(235, 6, 8, 12)
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 4.2f
                    textSize = 11.5f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                val tagFill = android.graphics.Paint().apply {
                    textSize = 11.5f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                val calloutFill = android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(254, 243, 199)
                    textSize = 11f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                for (r in sim.rivalOperators) {
                    if (r.elevation != curElev) continue
                    if (!inView(r.x - 80f, r.y - 75f, 160f, 100f, camL, camT, camR, camB)) continue
                    val stateSuffix = when {
                        r.isVengefulKin -> " [KIN]"
                        r.aiState == ScavAiState.ENGAGE -> " [!]"
                        r.aiState == ScavAiState.TAKE_COVER -> " [COVER]"
                        r.aiState == ScavAiState.RETREAT -> " [FALLBACK]"
                        r.aiState == ScavAiState.INVESTIGATE -> " [?]"
                        else -> ""
                    }
                    val labelText = "${r.displayName}$stateSuffix"
                    tagFill.color = when {
                        r.isVengefulKin -> android.graphics.Color.rgb(216, 180, 254)
                        r.aiState == ScavAiState.ENGAGE -> android.graphics.Color.rgb(248, 113, 113)
                        r.aiState == ScavAiState.TAKE_COVER -> android.graphics.Color.rgb(103, 232, 249)
                        r.aiState == ScavAiState.INVESTIGATE -> android.graphics.Color.rgb(252, 211, 77)
                        else -> android.graphics.Color.rgb(203, 213, 225)
                    }
                    drawContext.canvas.nativeCanvas.drawText(labelText, r.x, r.y - 45f, tagStroke)
                    drawContext.canvas.nativeCanvas.drawText(labelText, r.x, r.y - 45f, tagFill)

                    if (r.calloutText.isNotBlank() && now < r.calloutUntilMs) {
                        val bubbleText = "\"${r.calloutText.take(36)}\""
                        drawContext.canvas.nativeCanvas.drawText(bubbleText, r.x, r.y - 59f, tagStroke)
                        drawContext.canvas.nativeCanvas.drawText(bubbleText, r.x, r.y - 59f, calloutFill)
                    }
                }
            }
            // 9B. Base Builder Blueprint Grid & Movable Object Outlines
            if (sim.buildModeActive && w.mapId == GameMapId.OUTPOST_ZERO) {
                var gx = -960f
                while (gx <= 960f) {
                    if (gx in (camL - 40f)..(camR + 40f)) {
                        drawLine(
                            color = Color(0xFF38BDF8).copy(alpha = 0.10f),
                            start = Offset(gx, -740f),
                            end = Offset(gx, 740f),
                            strokeWidth = 1f
                        )
                    }
                    gx += 60f
                }
                var gy = -720f
                while (gy <= 720f) {
                    if (gy in (camT - 40f)..(camB + 40f)) {
                        drawLine(
                            color = Color(0xFF38BDF8).copy(alpha = 0.10f),
                            start = Offset(-960f, gy),
                            end = Offset(960f, gy),
                            strokeWidth = 1f
                        )
                    }
                    gy += 60f
                }
                val pulse = 0.55f + 0.35f * sin((now % 1100L) / 1100f * 2f * PI.toFloat())
                for (piece in sim.buildLayoutPieces) {
                    val rot = ((piece.rotationDeg % 360) + 360) % 360
                    val swap = rot == 90 || rot == 270
                    val pw = if (swap) piece.kind.height else piece.kind.width
                    val ph = if (swap) piece.kind.width else piece.kind.height
                    val lx = piece.x - pw / 2f
                    val ly = piece.y - ph / 2f
                    if (!inView(lx, ly, pw, ph, camL, camT, camR, camB)) continue
                    val isSel = piece.id == sim.selectedBuildPieceId
                    val outlineColor = when {
                        isSel -> Color(0xFF34D399)
                        piece.kind.category == "BUILDING" -> Color(0xFFF59E0B).copy(alpha = 0.55f)
                        piece.kind.category == "BUNK" -> Color(0xFFA7F3D0).copy(alpha = 0.85f)
                        piece.kind.category == "STATION" -> Color(0xFF38BDF8).copy(alpha = 0.8f)
                        else -> Color(0xFFC084FC).copy(alpha = 0.65f)
                    }
                    if (isSel) {
                        drawRect(
                            color = Color(0xFF34D399).copy(alpha = 0.16f * pulse),
                            topLeft = Offset(lx, ly),
                            size = Size(pw, ph)
                        )
                    }
                    drawRect(
                        color = outlineColor,
                        topLeft = Offset(lx, ly),
                        size = Size(pw, ph),
                        style = Stroke(
                            width = if (isSel) 3.5f else 1.8f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                        )
                    )
                    drawCircle(
                        color = outlineColor,
                        radius = if (isSel) 6f else 3.5f,
                        center = Offset(piece.x, piece.y)
                    )
                }
            }
            // 9C. Smoky Coastal Mist Wisps along outer perimeter & river valleys (splash key-art atmosphere)
            if (curElev == 0) {
                val mistDrift = sin((now % 14000L) / 14000f * 2f * PI.toFloat()) * 18f
                val mxStart = ((camL / 260f).toInt() - 1) * 260f
                val myStart = ((camT / 260f).toInt() - 1) * 260f
                var my = myStart
                while (my <= camB + 260f) {
                    var mx = mxStart
                    while (mx <= camR + 260f) {
                        val mh = ((mx.toInt() * 48611) xor (my.toInt() * 97213)) and 0xFF
                        if (mh < 54) {
                            val px = mx + (mh % 40) + mistDrift
                            val py = my + ((mh shr 3) % 40)
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF7CA49C).copy(alpha = 0.11f), Color.Transparent),
                                    center = Offset(px, py),
                                    radius = 125f
                                ),
                                topLeft = Offset(px - 125f, py - 68f),
                                size = Size(250f, 136f)
                            )
                        }
                        mx += 260f
                    }
                    my += 260f
                }
            }
        }

        // 10. Day/Night Atmospheric Tint, Fog-of-War, Dynamic Aim Cone & Streetlight Pools
        renderLightingLayer(sim, atmosphere, camL, camT, camR, camB, zoom, inBlackout, inRareFlicker, now)

        // 11. Tactical Aim-Cone Reticle & Virtual Twin-Stick Rings
        if (!sim.buildModeActive) {
            renderAimReticle(sim, zoom)
        }
        renderTouchSticks(sim)
    }

    private fun DrawScope.drawHelipadMarkings(x: Float, y: Float, w: Float, h: Float, now: Long) {
        val cx = x + w * 0.52f
        val cy = y + h * 0.52f
        val r = (minOf(w, h) * 0.31f).coerceIn(78f, 128f)
        // Helipad outer concrete apron border
        drawRect(
            color = Color(0xFF334155),
            topLeft = Offset(x, y),
            size = Size(w, h),
            style = Stroke(width = 4f)
        )
        // Outer dashed yellow safety perimeter ring
        drawCircle(
            color = Color(0xFFF59E0B).copy(alpha = 0.8f),
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
        )
        drawCircle(
            color = Color(0xFF1E293B),
            radius = r - 8f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFFE2E8F0).copy(alpha = 0.65f),
            radius = r - 12f,
            center = Offset(cx, cy),
            style = Stroke(width = 3f)
        )
        // Bold crisp "H" stencil
        drawRect(color = Color(0xFFF8FAFC), topLeft = Offset(cx - 24f, cy - 30f), size = Size(10f, 60f))
        drawRect(color = Color(0xFFF8FAFC), topLeft = Offset(cx + 14f, cy - 30f), size = Size(10f, 60f))
        drawRect(color = Color(0xFFF8FAFC), topLeft = Offset(cx - 24f, cy - 5f), size = Size(48f, 10f))

        // Corner animated emerald/amber taxiway strobe beacons
        val blink = (now / 450L) % 2L == 0L
        val beaconCol = if (blink) Color(0xFF34D399) else Color(0xFF065F46)
        listOf(
            Offset(cx - r - 8f, cy - r - 8f),
            Offset(cx + r + 8f, cy - r - 8f),
            Offset(cx - r - 8f, cy + r + 8f),
            Offset(cx + r + 8f, cy + r + 8f)
        ).forEach { pos ->
            drawCircle(color = beaconCol.copy(alpha = 0.4f), radius = 9f, center = pos)
            drawCircle(color = beaconCol, radius = 4f, center = pos)
        }
    }

    private fun DrawScope.drawCantinePatioDeck(x: Float, y: Float, w: Float, h: Float) {
        // Timber slat planks for Open-Air Cantine
        var py = y + 10f
        while (py < y + h) {
            drawLine(
                color = Color(0xFF2B2219).copy(alpha = 0.45f),
                start = Offset(x, py),
                end = Offset(x + w, py),
                strokeWidth = 1.4f
            )
            py += 14f
        }
        drawRect(
            color = Color(0xFF785A3C).copy(alpha = 0.65f),
            topLeft = Offset(x, y),
            size = Size(w, h),
            style = Stroke(width = 3f)
        )
    }

    // Legacy drawDetailedProp removed — replaced by PixelArtSprites.drawPixelProp

    private fun DrawScope.drawMeleeSlashArc(
        px: Float,
        py: Float,
        playerAngle: Float,
        weapon: WeaponId,
        progress: Float,
        dir: Float
    ) {
        val reach = weapon.meleeReach
        val sweepTotal = weapon.meleeArcRad * 1.9f
        val startRad = playerAngle - (sweepTotal * 0.5f) * dir
        val sweepDeg = (sweepTotal * progress) * dir * 180f / PI.toFloat()
        val startDeg = startRad * 180f / PI.toFloat()
        val alpha = (1f - progress * 0.65f).coerceIn(0.1f, 0.9f)

        drawArc(
            color = Color(weapon.tracerColorHex).copy(alpha = 0.22f * alpha),
            startAngle = startDeg,
            sweepAngle = sweepDeg,
            useCenter = true,
            topLeft = Offset(px - reach, py - reach),
            size = Size(reach * 2f, reach * 2f)
        )
        drawArc(
            color = Color(weapon.tracerColorHex).copy(alpha = 0.92f * alpha),
            startAngle = startDeg,
            sweepAngle = sweepDeg,
            useCenter = false,
            topLeft = Offset(px - reach, py - reach),
            size = Size(reach * 2f, reach * 2f),
            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
        )
    }

    private fun DrawScope.renderLightingLayer(
        sim: GameSimulation,
        atmosphere: DayNightAtmosphere,
        camL: Float,
        camT: Float,
        camR: Float,
        camB: Float,
        zoom: Float,
        inBlackout: Boolean,
        inRareFlicker: Boolean,
        now: Long
    ) {
        val w = sim.world ?: return

        // Sunlight / Golden-Hour / Dusk / Night painterly ambient wash
        if (atmosphere.ambientTintAlpha > 0.005f) {
            drawRect(
                color = atmosphere.ambientTintColor.copy(alpha = atmosphere.ambientTintAlpha),
                size = size
            )
        }

        val cx = size.width / 2f
        val cy = size.height / 2f

        val startDeg = (sim.playerAngle - sim.currentHalfFov) * 180f / PI.toFloat()
        val sweepDeg = sim.currentHalfFov * 2f * 180f / PI.toFloat()
        val worldConePath = Path().apply {
            moveTo(0f, 0f)
            arcTo(
                rect = Rect(
                    -sim.currentRange,
                    -sim.currentRange,
                    sim.currentRange,
                    sim.currentRange
                ),
                startAngleDegrees = startDeg,
                sweepAngleDegrees = sweepDeg,
                forceMoveTo = false
            )
            close()
        }

        // Apply night/dusk/interior darkness cutout only when lighting warrants it (daytime outdoors = crystal clear!)
        val showDarknessCutout = !sim.buildModeActive && atmosphere.overlayAlpha > 0.01f
        if (showDarknessCutout) {
            drawContext.canvas.saveLayer(Rect(0f, 0f, size.width, size.height), Paint())
            drawRect(color = atmosphere.overlayColor.copy(alpha = atmosphere.overlayAlpha), size = size)

            withTransform({
                translate(left = cx, top = cy)
                scale(scaleX = zoom, scaleY = zoom, pivot = Offset.Zero)
            }) {
                // Personal 360 awareness circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        center = Offset.Zero,
                        radius = sim.ambientRadius
                    ),
                    radius = sim.ambientRadius,
                    center = Offset.Zero,
                    blendMode = BlendMode.DstOut
                )

                // Weapon Dynamic Aim Cone
                drawPath(
                    path = worldConePath,
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Black, Color.Black.copy(alpha = 0.88f), Color.Transparent),
                        center = Offset.Zero,
                        radius = sim.currentRange
                    ),
                    blendMode = BlendMode.DstOut
                )

                // Streetlight & Watchtower Floodlight pools
                if (sim.elevation == 0 && atmosphere.streetLightsActive && !inBlackout && !inRareFlicker) {
                    for (sl in w.streetLights) {
                        if (sl.isBrokenBulb && (now / 420L) % 5L == 0L) continue
                        if (!inView(sl.x - 160f, sl.y - 160f, 320f, 320f, camL, camT, camR, camB, 0f)) continue
                        val sx = sl.x - sim.playerX
                        val sy = sl.y - sim.playerY
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.86f), Color.Transparent),
                                center = Offset(sx, sy),
                                radius = 152f
                            ),
                            radius = 152f,
                            center = Offset(sx, sy),
                            blendMode = BlendMode.DstOut
                        )
                    }
                }

                // Active CRT TV glow pools inside houses & lounges when grid power is on
                if (!inBlackout && !inRareFlicker) {
                    for (pr in w.props) {
                        if (pr.propType != "tv" || !pr.tvPoweredOn || pr.floor != sim.elevation) continue
                        val tcx = pr.x + pr.w * 0.5f
                        val tcy = pr.y + pr.h * 0.5f
                        if (!inView(tcx - 105f, tcy - 105f, 210f, 210f, camL, camT, camR, camB, 0f)) continue
                        val sx = tcx - sim.playerX
                        val sy = tcy - sim.playerY
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent),
                                center = Offset(sx, sy),
                                radius = 102f
                            ),
                            radius = 102f,
                            center = Offset(sx, sy),
                            blendMode = BlendMode.DstOut
                        )
                    }
                }
            }

            drawContext.canvas.restore()
        }

        withTransform({
            translate(left = cx, top = cy)
            scale(scaleX = zoom, scaleY = zoom, pivot = Offset.Zero)
        }) {
            // Warm sodium/amber streetlight pool tint on street surface at dusk & night
            if (sim.elevation == 0 && atmosphere.streetLightsActive && !inBlackout && !inRareFlicker) {
                for (sl in w.streetLights) {
                    if (sl.isBrokenBulb && (now / 420L) % 5L == 0L) continue
                    if (!inView(sl.x - 125f, sl.y - 125f, 250f, 250f, camL, camT, camR, camB, 0f)) continue
                    val sx = sl.x - sim.playerX
                    val sy = sl.y - sim.playerY
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFDE68A).copy(alpha = 0.14f),
                                Color(0xFFF59E0B).copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            center = Offset(sx, sy),
                            radius = 116f
                        ),
                        radius = 116f,
                        center = Offset(sx, sy)
                    )
                }
            }

            // Warm splash-style flashlight beam spill tint along aim cone
            if (atmosphere.flashlightStrength > 0.05f && !sim.buildModeActive) {
                val strength = atmosphere.flashlightStrength
                drawPath(
                    path = worldConePath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFDE68A).copy(alpha = 0.14f * strength),
                            Color(0xFFF59E0B).copy(alpha = 0.05f * strength),
                            Color.Transparent
                        ),
                        center = Offset.Zero,
                        radius = sim.currentRange
                    )
                )
            }
        }
    }

    private fun DrawScope.renderAimReticle(sim: GameSimulation, zoom: Float) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val col = if (sim.hasTargetLock) Color(0xFFEF4444).copy(alpha = 0.65f) else Color(0xFF6EE7B7).copy(alpha = 0.25f)
        val range = (if (sim.currentWeapon.isMelee) sim.currentWeapon.meleeReach else sim.currentRange) * zoom
        val halfFov = if (sim.currentWeapon.isMelee) sim.currentWeapon.meleeArcRad * 0.85f else sim.currentHalfFov
        val a1 = sim.playerAngle - halfFov
        val a2 = sim.playerAngle + halfFov
        drawLine(col, Offset(cx, cy), Offset(cx + cos(a1) * range, cy + sin(a1) * range), strokeWidth = 1.5f)
        drawLine(col, Offset(cx, cy), Offset(cx + cos(a2) * range, cy + sin(a2) * range), strokeWidth = 1.5f)
        drawArc(
            color = col,
            startAngle = a1 * 180f / PI.toFloat(),
            sweepAngle = (a2 - a1) * 180f / PI.toFloat(),
            useCenter = false,
            topLeft = Offset(cx - range, cy - range),
            size = Size(range * 2f, range * 2f),
            style = Stroke(width = 1.5f)
        )
    }

    private fun DrawScope.renderTouchSticks(sim: GameSimulation) {
        val sticks = listOf(sim.leftStick to Color(0xFF34D399), sim.rightStick to Color(0xFFF59E0B))
        for ((stick, color) in sticks) {
            if (!stick.active) continue
            drawCircle(color.copy(alpha = 0.35f), radius = 56f, center = Offset(stick.centerX, stick.centerY), style = Stroke(width = 2.5f))
            drawCircle(color.copy(alpha = 0.45f), radius = 21f, center = Offset(stick.knobX, stick.knobY))
        }
    }

    private fun DrawScope.drawSurvivorNpcRig(
        px: Float,
        py: Float,
        shirtHex: Long,
        vestHex: Long,
        skinHex: Long,
        accentHex: Long,
        oblY: Float,
        breath: Float,
        now: Long
    ) {
        val oy = oblY * 0.52f
        val shirtCol = Color(shirtHex)
        val vestCol = Color(vestHex)
        val skinCol = Color(skinHex)
        translate(left = px, top = py) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.52f), Color.Transparent),
                    center = Offset(0f, 3f),
                    radius = 14f
                ),
                topLeft = Offset(-13f, -2f),
                size = Size(26f, 12f)
            )
            // Contoured cargo trousers & lace-up boots
            drawRoundRect(color = Color(0xFF28241E), topLeft = Offset(-5.4f, -8.5f), size = Size(4.6f, 8.2f), cornerRadius = CornerRadius(2f, 2f))
            drawRoundRect(color = Color(0xFF28241E), topLeft = Offset(0.8f, -8.5f), size = Size(4.6f, 8.2f), cornerRadius = CornerRadius(2f, 2f))
            drawRoundRect(color = Color(0xFF171310), topLeft = Offset(-5.6f, -1.8f), size = Size(5f, 3.6f), cornerRadius = CornerRadius(1.5f, 1.5f))
            drawRoundRect(color = Color(0xFF171310), topLeft = Offset(0.6f, -1.8f), size = Size(5f, 3.6f), cornerRadius = CornerRadius(1.5f, 1.5f))

            // Layered jacket + tactical utility vest
            translate(left = 0f, top = oy * 0.62f + breath * 0.45f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(shirtCol.copy(alpha = 0.78f), shirtCol, shirtCol.copy(alpha = 0.72f)),
                        startX = -8f,
                        endX = 8f
                    ),
                    topLeft = Offset(-7.8f, -11f),
                    size = Size(15.6f, 19f),
                    cornerRadius = CornerRadius(4.5f, 4.5f)
                )
                drawRoundRect(
                    color = vestCol,
                    topLeft = Offset(-6.2f, -10f),
                    size = Size(12.4f, 14.5f),
                    cornerRadius = CornerRadius(3.5f, 3.5f)
                )
                drawRoundRect(
                    color = PixelArtSprites.OUTLINE.copy(alpha = 0.85f),
                    topLeft = Offset(-7.8f, -11f),
                    size = Size(15.6f, 19f),
                    cornerRadius = CornerRadius(4.5f, 4.5f),
                    style = Stroke(width = 1.6f)
                )
                // Neck & sculpted head with hair
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(skinCol, Color(0xFF422C22)),
                        center = Offset(-1f, -13f),
                        radius = 8.5f
                    ),
                    topLeft = Offset(-6.8f, -19.5f),
                    size = Size(13.6f, 13.6f)
                )
                drawOval(
                    color = PixelArtSprites.OUTLINE.copy(alpha = 0.9f),
                    topLeft = Offset(-6.8f, -19.5f),
                    size = Size(13.6f, 13.6f),
                    style = Stroke(width = 1.5f)
                )
                drawArc(
                    color = Color(0xFF231D1A),
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = true,
                    topLeft = Offset(-6.8f, -19.8f),
                    size = Size(13.6f, 12f)
                )
                // Subtle diegetic talk indicator ring
                val haloPulse = 0.55f + 0.35f * sin((now % 1800L) / 1800f * 2f * PI.toFloat())
                drawCircle(
                    color = Color(accentHex).copy(alpha = 0.85f * haloPulse),
                    radius = 3.2f,
                    center = Offset(0f, -25.5f + breath * 0.4f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f * haloPulse),
                    radius = 3.2f,
                    center = Offset(0f, -25.5f + breath * 0.4f),
                    style = Stroke(width = 1.1f)
                )
            }
        }
    }

    fun DrawScope.drawDogCompanion(
        dx: Float,
        dy: Float,
        angle: Float,
        coatHex: Long,
        stepPhase: Float = 0f,
        barking: Boolean = false
    ) {
        val coat = Color(coatHex)
        val coatDark = Color(
            red = (coat.red * 0.62f).coerceIn(0f, 1f),
            green = (coat.green * 0.62f).coerceIn(0f, 1f),
            blue = (coat.blue * 0.62f).coerceIn(0f, 1f),
            alpha = 1f
        )
        val isShepherd = coatHex == 0xFFA66C36L || coatHex == 0xFF9E6B3CL
        translate(left = dx, top = dy) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.48f), Color.Transparent),
                    center = Offset(0f, 3f),
                    radius = 14f
                ),
                topLeft = Offset(-12f, -2f),
                size = Size(24f, 11f)
            )
            if (barking) {
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.55f),
                    radius = 17f,
                    center = Offset(0f, -6f),
                    style = Stroke(width = 2f)
                )
            }
            val wag = sin(stepPhase * 1.4f) * 3.0f
            rotate(degrees = angle * 180f / PI.toFloat(), pivot = Offset(0f, -6f)) {
                // Feathered wagging tail
                val tailPath = Path().apply {
                    moveTo(-8.5f, -6f)
                    quadraticTo(-13.5f, -6f + wag * 0.45f, -16.5f, -5.5f + wag * 0.7f)
                }
                drawPath(tailPath, color = coatDark, style = Stroke(width = 3.0f, cap = StrokeCap.Round))

                // Tapered canine ribcage & hindquarters
                val bodyPath = Path().apply {
                    moveTo(-10f, -9.8f)
                    quadraticTo(-2f, -12.4f, 5.5f, -10.8f)
                    lineTo(5.5f, -1.2f)
                    quadraticTo(-2f, 0.4f, -10f, -2.2f)
                    close()
                }
                drawPath(
                    bodyPath,
                    brush = Brush.verticalGradient(colors = listOf(coat, coatDark), startY = -12f, endY = 0f)
                )
                if (isShepherd) {
                    drawOval(
                        color = Color(0xFF1F1A17).copy(alpha = 0.85f),
                        topLeft = Offset(-7.5f, -10.4f),
                        size = Size(9.5f, 8.8f)
                    )
                }
                drawPath(bodyPath, color = PixelArtSprites.OUTLINE.copy(alpha = 0.88f), style = Stroke(width = 1.4f))

                // Leather collar & brass tag
                drawRect(color = Color(0xFF7F1D1D), topLeft = Offset(3.8f, -10.6f), size = Size(2.4f, 9.2f))
                drawCircle(color = Color(0xFFD97706), radius = 1.2f, center = Offset(5.5f, -2.2f))

                // Sculpted cranium + tapered muzzle + wet black nose
                drawOval(
                    brush = Brush.radialGradient(colors = listOf(coat, coatDark), center = Offset(8.5f, -6f), radius = 6f),
                    topLeft = Offset(4.8f, -10.8f),
                    size = Size(8.8f, 9.6f)
                )
                drawOval(
                    color = if (isShepherd) Color(0xFF1F1A17) else coatDark,
                    topLeft = Offset(10.8f, -8.5f),
                    size = Size(5.0f, 5.0f)
                )
                drawCircle(color = Color(0xFF0A0A0C), radius = 1.3f, center = Offset(15.2f, -6f))

                // Angled pointed German Shepherd / hound ear pinnae
                val leftEar = Path().apply {
                    moveTo(5.6f, -9.8f); lineTo(3.6f, -13.6f); lineTo(8.2f, -10.4f); close()
                }
                val rightEar = Path().apply {
                    moveTo(5.6f, -2.2f); lineTo(3.6f, 1.6f); lineTo(8.2f, -1.6f); close()
                }
                drawPath(leftEar, color = coatDark)
                drawPath(leftEar, color = PixelArtSprites.OUTLINE, style = Stroke(width = 1.1f))
                drawPath(rightEar, color = coatDark)
                drawPath(rightEar, color = PixelArtSprites.OUTLINE, style = Stroke(width = 1.1f))
            }
        }
    }

    fun DrawScope.drawOperatorRig(
        px: Float,
        py: Float,
        angle: Float,
        profile: OperatorProfile,
        weapon: WeaponId,
        stepPhase: Float,
        scale: Float,
        armorTier: ArmorTier = profile.armorTier,
        weaponSwingOffsetRad: Float = 0f,
        equippedAttachments: Set<String> = emptySet(),
        oblY: Float = OBLIQUE_Y
    ) {
        val torsoScale = scale * profile.sex.shoulderScale
        val shirtCol = Color(profile.shirtStyle.primaryHex)
        val shirtShade = Color(
            red = (shirtCol.red * 0.65f).coerceIn(0f, 1f),
            green = (shirtCol.green * 0.65f).coerceIn(0f, 1f),
            blue = (shirtCol.blue * 0.65f).coerceIn(0f, 1f),
            alpha = 1f
        )
        val patternCol = Color(profile.shirtStyle.patternHex)
        val pantsCol = Color(profile.pantsStyle.colorHex)
        val pantsShade = Color(
            red = (pantsCol.red * 0.68f).coerceIn(0f, 1f),
            green = (pantsCol.green * 0.68f).coerceIn(0f, 1f),
            blue = (pantsCol.blue * 0.68f).coerceIn(0f, 1f),
            alpha = 1f
        )
        val shoeCol = Color(profile.shoeStyle.colorHex)
        val soleCol = Color(profile.shoeStyle.soleHex)
        val skinCol = Color(profile.skinTone.colorHex)
        val skinShade = Color(
            red = (skinCol.red * 0.68f).coerceIn(0f, 1f),
            green = (skinCol.green * 0.66f).coerceIn(0f, 1f),
            blue = (skinCol.blue * 0.64f).coerceIn(0f, 1f),
            alpha = 1f
        )

        translate(left = px, top = py) {
            val oy = oblY * 0.52f * scale
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.54f), Color.Transparent),
                    center = Offset(0f, 3f * scale),
                    radius = 16f * scale
                ),
                topLeft = Offset(-14f * scale, -3f * scale),
                size = Size(28f * scale, 14f * scale)
            )
            // Contoured trousers & stitched boots
            val stride = sin(stepPhase) * 3.5f * scale
            drawRoundRect(
                brush = Brush.verticalGradient(colors = listOf(pantsCol, pantsShade), startY = (-8f + stride) * scale, endY = (-1f + stride) * scale),
                topLeft = Offset(-6.0f * scale, (-8f + stride) * scale),
                size = Size(5.0f * scale, 7.2f * scale),
                cornerRadius = CornerRadius(1.8f * scale, 1.8f * scale)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(colors = listOf(pantsCol, pantsShade), startY = (-8f - stride) * scale, endY = (-1f - stride) * scale),
                topLeft = Offset(1.0f * scale, (-8f - stride) * scale),
                size = Size(5.0f * scale, 7.2f * scale),
                cornerRadius = CornerRadius(1.8f * scale, 1.8f * scale)
            )
            drawRoundRect(color = shoeCol, topLeft = Offset(-6.2f * scale, (-2.2f + stride) * scale), size = Size(5.4f * scale, 4.0f * scale), cornerRadius = CornerRadius(1.6f * scale, 1.6f * scale))
            drawRoundRect(color = shoeCol, topLeft = Offset(0.8f * scale, (-2.2f - stride) * scale), size = Size(5.4f * scale, 4.0f * scale), cornerRadius = CornerRadius(1.6f * scale, 1.6f * scale))
            drawRect(color = soleCol, topLeft = Offset(-6.2f * scale, (1.1f + stride) * scale), size = Size(5.4f * scale, 1.2f * scale))
            drawRect(color = soleCol, topLeft = Offset(0.8f * scale, (1.1f - stride) * scale), size = Size(5.4f * scale, 1.2f * scale))

            // Volumetric torso lower oblique drop
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(shirtShade, shirtCol, shirtShade),
                    startX = -8f * torsoScale,
                    endX = 8f * torsoScale
                ),
                topLeft = Offset(-8f * torsoScale, oy * 0.65f),
                size = Size(16f * torsoScale, -oy * 0.65f),
                cornerRadius = CornerRadius(3f * scale, 3f * scale)
            )
            if (profile.shirtStyle.isPlaid) {
                drawLine(patternCol, Offset(-4f * torsoScale, oy * 0.60f), Offset(-4f * torsoScale, 0f), strokeWidth = 1.4f * scale)
                drawLine(patternCol, Offset(4f * torsoScale, oy * 0.60f), Offset(4f * torsoScale, 0f), strokeWidth = 1.4f * scale)
                drawLine(patternCol, Offset(-8f * torsoScale, oy * 0.32f), Offset(8f * torsoScale, oy * 0.32f), strokeWidth = 1.4f * scale)
            }
            if (armorTier != ArmorTier.NONE) {
                drawRoundRect(
                    color = Color(profile.vestHex),
                    topLeft = Offset(-7f * torsoScale, oy * 0.6f),
                    size = Size(14f * torsoScale, -oy * 0.48f),
                    cornerRadius = CornerRadius(2.5f * scale, 2.5f * scale)
                )
            }
            drawRoundRect(
                color = PixelArtSprites.OUTLINE.copy(alpha = 0.88f),
                topLeft = Offset(-8f * torsoScale, oy * 0.65f),
                size = Size(16f * torsoScale, -oy * 0.65f),
                cornerRadius = CornerRadius(3f * scale, 3f * scale),
                style = Stroke(width = 1.6f * scale)
            )

            translate(left = 0f, top = oy * 0.62f) {
                rotate(degrees = angle * 180f / PI.toFloat(), pivot = Offset.Zero) {
                    // Field pack with bedroll & leather buckle straps
                    drawRoundRect(
                        color = Color(profile.packHex),
                        topLeft = Offset(-15.2f * scale, -9f * scale),
                        size = Size(9.2f * scale, 18f * scale),
                        cornerRadius = CornerRadius(3f * scale, 3f * scale)
                    )
                    drawRect(
                        color = Color(0xFF1F1A17).copy(alpha = 0.65f),
                        topLeft = Offset(-13.8f * scale, -7f * scale),
                        size = Size(2.2f * scale, 14f * scale)
                    )
                    drawRoundRect(
                        color = PixelArtSprites.OUTLINE,
                        topLeft = Offset(-15.2f * scale, -9f * scale),
                        size = Size(9.2f * scale, 18f * scale),
                        cornerRadius = CornerRadius(3f * scale, 3f * scale),
                        style = Stroke(width = 1.6f * scale)
                    )
                    if (profile.shirtStyle.hasHood) {
                        drawRoundRect(
                            color = patternCol,
                            topLeft = Offset(-9.5f * torsoScale, -7f * torsoScale),
                            size = Size(5f * torsoScale, 14f * torsoScale),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                    // Contoured jacket shoulders + flannel seam + collar
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(shirtShade, shirtCol, shirtShade),
                            startY = -12.5f * torsoScale,
                            endY = 12.5f * torsoScale
                        ),
                        topLeft = Offset(-6.2f * torsoScale, -12.2f * torsoScale),
                        size = Size(12.4f * torsoScale, 24.4f * torsoScale),
                        cornerRadius = CornerRadius(5f * torsoScale, 5f * torsoScale)
                    )
                    drawRoundRect(
                        color = PixelArtSprites.OUTLINE,
                        topLeft = Offset(-6.2f * torsoScale, -12.2f * torsoScale),
                        size = Size(12.4f * torsoScale, 24.4f * torsoScale),
                        cornerRadius = CornerRadius(5f * torsoScale, 5f * torsoScale),
                        style = Stroke(width = 1.8f * scale)
                    )
                    if (profile.shirtStyle.isPlaid) {
                        drawLine(patternCol, Offset(0f, -12f * torsoScale), Offset(0f, 12f * torsoScale), strokeWidth = 1.8f * scale)
                        drawLine(patternCol, Offset(-6f * torsoScale, 0f), Offset(6f * torsoScale, 0f), strokeWidth = 1.8f * scale)
                    }
                    if (armorTier != ArmorTier.NONE) {
                        drawRoundRect(
                            color = Color(profile.vestHex),
                            topLeft = Offset(-5.5f * torsoScale, -10.5f * torsoScale),
                            size = Size(11f * torsoScale, 21f * torsoScale),
                            cornerRadius = CornerRadius(3.5f * scale, 3.5f * scale)
                        )
                        drawRoundRect(
                            color = Color(armorTier.vestTrimHex).copy(alpha = 0.92f),
                            topLeft = Offset(-5f * torsoScale, -10f * torsoScale),
                            size = Size(10f * torsoScale, 20f * torsoScale),
                            cornerRadius = CornerRadius(3f * scale, 3f * scale),
                            style = Stroke(width = 1.8f * scale)
                        )
                    }
                    // Tapered sleeves, forearm cuffs & gloved/bare hands
                    drawRoundRect(color = shirtCol, topLeft = Offset(2f * torsoScale, -10.2f * torsoScale), size = Size(8.5f * scale, 4.6f * scale), cornerRadius = CornerRadius(2.2f, 2.2f))
                    drawRoundRect(color = shirtCol, topLeft = Offset(3f * torsoScale, 5.4f * torsoScale), size = Size(9.5f * scale, 4.6f * scale), cornerRadius = CornerRadius(2.2f, 2.2f))
                    val gloveCol = if (profile.glovesStyle == GlovesStyle.NONE) skinCol else Color(profile.glovesStyle.colorHex)
                    drawCircle(color = gloveCol, radius = 2.4f * scale, center = Offset(11.4f * scale, -7.8f * scale))
                    drawCircle(color = gloveCol, radius = 2.4f * scale, center = Offset(13.2f * scale, 7.6f * scale))
                    if (profile.glovesStyle.bareTips && profile.glovesStyle != GlovesStyle.NONE) {
                        drawCircle(color = skinCol, radius = 1.0f * scale, center = Offset(12.5f * scale, -7.8f * scale))
                        drawCircle(color = skinCol, radius = 1.0f * scale, center = Offset(14.3f * scale, 7.6f * scale))
                    }

                    rotate(degrees = weaponSwingOffsetRad * 180f / PI.toFloat(), pivot = Offset(4f * scale, 4f * scale)) {
                        drawHeldWeapon(weapon, scale, equippedAttachments)
                    }

                    // Volumetric sculpted cranium + facial bridge shading + eyewear
                    drawCircle(color = PixelArtSprites.OUTLINE, radius = 8.4f * scale, center = Offset.Zero)
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(skinCol, skinShade),
                            center = Offset(2f * scale, -1f * scale),
                            radius = 8.5f * scale
                        ),
                        topLeft = Offset(-7.4f * scale, -7.2f * scale),
                        size = Size(14.8f * scale, 14.4f * scale)
                    )
                    if (profile.glassesStyle != GlassesStyle.NONE) {
                        drawRoundRect(
                            color = Color(profile.glassesStyle.lensHex),
                            topLeft = Offset(4.4f * scale, -5.6f * scale),
                            size = Size(3.2f * scale, 4.6f * scale),
                            cornerRadius = CornerRadius(1f, 1f)
                        )
                        drawRoundRect(
                            color = Color(profile.glassesStyle.lensHex),
                            topLeft = Offset(4.4f * scale, 1.0f * scale),
                            size = Size(3.2f * scale, 4.6f * scale),
                            cornerRadius = CornerRadius(1f, 1f)
                        )
                        drawLine(
                            color = Color(profile.glassesStyle.frameHex),
                            start = Offset(5.6f * scale, -1.0f * scale),
                            end = Offset(5.6f * scale, 1.0f * scale),
                            strokeWidth = 1.4f * scale
                        )
                    }
                    when (profile.hatStyle) {
                        HatStyle.NONE -> {
                            drawArc(
                                color = Color(0xFF231D1A),
                                startAngle = 95f,
                                sweepAngle = 170f,
                                useCenter = true,
                                topLeft = Offset(-7.6f * scale, -7.6f * scale),
                                size = Size(15.2f * scale, 15.2f * scale)
                            )
                        }
                        HatStyle.BALL_CAP -> {
                            drawCircle(color = Color(profile.hatStyle.colorHex), radius = 7.8f * scale, center = Offset(-0.4f * scale, 0f))
                            drawRoundRect(
                                color = Color(profile.hatStyle.colorHex),
                                topLeft = Offset(3.5f * scale, -5.8f * scale),
                                size = Size(7.2f * scale, 11.6f * scale),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                            drawCircle(color = PixelArtSprites.OUTLINE, radius = 7.8f * scale, center = Offset(-0.4f * scale, 0f), style = Stroke(width = 1.2f * scale))
                            drawCircle(color = Color(profile.hatStyle.accentHex), radius = 1.4f * scale, center = Offset(-1f * scale, 0f))
                        }
                        HatStyle.KNIT_BEANIE -> {
                            drawCircle(color = Color(profile.hatStyle.colorHex), radius = 8.1f * scale, center = Offset(-0.6f * scale, 0f))
                            drawCircle(
                                color = Color(profile.hatStyle.accentHex),
                                radius = 7.8f * scale,
                                center = Offset(-0.4f * scale, 0f),
                                style = Stroke(width = 1.6f * scale)
                            )
                        }
                        HatStyle.BOONIE_HAT -> {
                            drawCircle(color = Color(profile.hatStyle.colorHex), radius = 10.6f * scale, center = Offset.Zero)
                            drawCircle(color = PixelArtSprites.OUTLINE, radius = 10.6f * scale, center = Offset.Zero, style = Stroke(width = 1.2f * scale))
                            drawCircle(color = Color(profile.hatStyle.accentHex), radius = 6.6f * scale, center = Offset.Zero)
                        }
                        HatStyle.RANCHER_HAT -> {
                            drawOval(
                                color = Color(profile.hatStyle.colorHex),
                                topLeft = Offset(-11.2f * scale, -10.2f * scale),
                                size = Size(22.4f * scale, 20.4f * scale)
                            )
                            drawOval(
                                color = PixelArtSprites.OUTLINE,
                                topLeft = Offset(-11.2f * scale, -10.2f * scale),
                                size = Size(22.4f * scale, 20.4f * scale),
                                style = Stroke(width = 1.2f * scale)
                            )
                            drawCircle(color = Color(profile.hatStyle.accentHex), radius = 6.0f * scale, center = Offset.Zero)
                        }
                        HatStyle.FLAT_CAP -> {
                            drawOval(
                                color = Color(profile.hatStyle.colorHex),
                                topLeft = Offset(-8.2f * scale, -7.2f * scale),
                                size = Size(17.5f * scale, 14.4f * scale)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun DrawScope.drawHeldWeapon(
        weapon: WeaponId,
        scale: Float,
        equippedAttachments: Set<String> = emptySet()
    ) {
        val len = weapon.gunLength * scale
        when {
            weapon.category == WeaponCategory.MELEE -> {
                drawRect(color = Color(0xFF4B3624), topLeft = Offset(4.5f * scale, 3.6f * scale), size = Size(len, 3.2f * scale))
                if (weapon == WeaponId.FIRE_AXE || weapon == WeaponId.TOMAHAWK || weapon == WeaponId.SLEDGEHAMMER) {
                    drawRect(color = Color(0xFF94A3B8), topLeft = Offset(len - 1f, -0.8f * scale), size = Size(5.5f * scale, 10.5f * scale))
                    drawLine(Color.White.copy(alpha = 0.65f), Offset(len + 4.2f * scale, -0.5f * scale), Offset(len + 4.2f * scale, 9.5f * scale), strokeWidth = 1.1f * scale)
                } else {
                    drawRect(color = Color(0xFFCBD5E1), topLeft = Offset(9f * scale, 3.6f * scale), size = Size(len - 2f, 2.8f * scale))
                }
            }
            weapon == WeaponId.CROSSBOW -> {
                drawRect(color = Color(0xFF3E2D20), topLeft = Offset(3f * scale, 2.6f * scale), size = Size(len, 3.8f * scale))
                drawLine(Color(0xFF64748B), Offset(len, -5f * scale), Offset(len, 12f * scale), strokeWidth = 2.4f * scale)
            }
            weapon.category == WeaponCategory.DMR_SNIPER -> {
                drawRect(color = Color(0xFF2B2219), topLeft = Offset(2f * scale, 2.5f * scale), size = Size(len * 0.45f, 4.2f * scale))
                drawRect(color = Color(weapon.accentColorHex), topLeft = Offset(2f * scale + len * 0.40f, 2.8f * scale), size = Size(len * 0.60f, 3.2f * scale))
                drawRect(color = Color(0xFF0F172A), topLeft = Offset(8f * scale, 1.3f * scale), size = Size(10f * scale, 5.4f * scale))
            }
            weapon.category == WeaponCategory.LMG_SPECIAL -> {
                drawRect(color = Color(weapon.accentColorHex), topLeft = Offset(2f * scale, 1.6f * scale), size = Size(len, 5.2f * scale))
                drawRect(color = Color(0xFF1E3A24), topLeft = Offset(9f * scale, 5.5f * scale), size = Size(8f * scale, 6f * scale))
            }
            weapon.category == WeaponCategory.SHOTGUN -> {
                drawRect(color = Color(weapon.accentColorHex), topLeft = Offset(2f * scale, 2.0f * scale), size = Size(len, 4.4f * scale))
                drawRect(color = Color(0xFF5C341D), topLeft = Offset(9f * scale, 1.8f * scale), size = Size(9f * scale, 4.8f * scale))
            }
            weapon.category == WeaponCategory.PISTOL -> {
                drawRect(color = Color(weapon.accentColorHex), topLeft = Offset(5f * scale, 2.5f * scale), size = Size(len, 3.6f * scale))
                if (weapon.suppressed) {
                    drawRect(color = Color(0xFF0F172A), topLeft = Offset(5f * scale + len - 5f, 2f * scale), size = Size(6f * scale, 4.6f * scale))
                }
            }
            else -> {
                drawRect(color = Color(weapon.accentColorHex), topLeft = Offset(2f * scale, 2.1f * scale), size = Size(len, 4.2f * scale))
                drawRect(color = Color(0xFF1E293B), topLeft = Offset(8f * scale, 5.2f * scale), size = Size(3.6f * scale, 4.2f * scale))
                drawRect(color = Color(weapon.tracerColorHex), topLeft = Offset(len + 1f, 3f * scale), size = Size(2.0f * scale, 2.0f * scale))
            }
        }
        if (!weapon.isMelee && equippedAttachments.isNotEmpty()) {
            val activeMods = equippedAttachments.mapNotNull { com.example.game.model.WeaponAttachmentCatalog.fromKey(it) }
            val startX = if (weapon.category == WeaponCategory.PISTOL) 5f * scale else 2f * scale
            for (mod in activeMods) {
                when (mod.slot) {
                    com.example.game.model.AttachmentSlot.MUZZLE -> {
                        val canLen = if (mod.forcesSuppressed) 6.5f * scale else 4.0f * scale
                        drawRect(
                            color = Color(if (mod.visualCode == "suppressor" && mod.key == "att_maxim_1910_can") 0xFFB45309 else 0xFF0F172A),
                            topLeft = Offset(startX + len, 2.1f * scale),
                            size = Size(canLen, 4.2f * scale)
                        )
                    }
                    com.example.game.model.AttachmentSlot.OPTIC -> {
                        val optCol = if (mod.visualCode == "brass_long_scope") Color(0xFFD97706) else Color(0xFF1E293B)
                        val optW = if (mod.visualCode.contains("scope") || mod.visualCode == "lpvo" || mod.visualCode == "acog") 8.5f * scale else 4.5f * scale
                        drawRect(color = optCol, topLeft = Offset(startX + 4f * scale, 0.8f * scale), size = Size(optW, 3.2f * scale))
                    }
                    com.example.game.model.AttachmentSlot.MAGAZINE -> {
                        if (mod.visualCode == "drum_mag") {
                            drawCircle(color = Color(0xFF111827), radius = 4.5f * scale, center = Offset(startX + 9f * scale, 8.0f * scale))
                        } else if (mod.visualCode == "ext_mag") {
                            drawRect(color = Color(0xFF1F2937), topLeft = Offset(startX + 8f * scale, 6f * scale), size = Size(3.5f * scale, 5.2f * scale))
                        }
                    }
                    com.example.game.model.AttachmentSlot.UNDERBARREL -> {
                        if (mod.visualCode == "bayonet") {
                            drawRect(color = Color(0xFFCBD5E1), topLeft = Offset(startX + len - 2f * scale, 6.2f * scale), size = Size(7.5f * scale, 1.8f * scale))
                        } else if (mod.visualCode == "bipod") {
                            drawLine(Color(0xFF334155), Offset(startX + len - 4f * scale, 6f * scale), Offset(startX + len, 10f * scale), strokeWidth = 1.6f * scale)
                        }
                    }
                    com.example.game.model.AttachmentSlot.TACTICAL -> {
                        if (mod.visualCode == "laser") {
                            drawLine(
                                color = Color(0xFFEF4444).copy(alpha = 0.55f),
                                start = Offset(startX + len, 3.6f * scale),
                                end = Offset(startX + len + 46f * scale, 3.6f * scale),
                                strokeWidth = 1.2f * scale
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun inView(x: Float, y: Float, w: Float, h: Float, camL: Float, camT: Float, camR: Float, camB: Float, pad: Float = 110f): Boolean {
        return x + w >= camL - pad && x <= camR + pad && y + h >= camT - pad && y <= camB + pad
    }
}
