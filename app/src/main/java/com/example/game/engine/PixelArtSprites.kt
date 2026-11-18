package com.example.game.engine

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.model.DoorObj
import com.example.game.model.DoorStyle
import com.example.game.model.PropObj
import com.example.game.model.VehicleKind
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

internal object PixelArtSprites {
    val OUTLINE = Color(0xFF06070A)
    val INK_SOFT = Color(0xFF141821)
    val METAL_DARK = Color(0xFF1B222C)
    val METAL_LIGHT = Color(0xFF94A3B8)

    fun DrawScope.drawComicHatch(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        step: Float = 6f,
        color: Color = OUTLINE.copy(alpha = 0.28f),
        strokeWidth: Float = 1.1f
    ) {
        if (w <= 3f || h <= 3f) return
        val span = min(w, h)
        var d = step
        while (d < w + h - 2f) {
            val sx = x + d.coerceAtMost(w)
            val sy = y + (d - w).coerceAtLeast(0f)
            val ex = x + (d - h).coerceAtLeast(0f)
            val ey = y + d.coerceAtMost(h)
            if (sx > ex && ey > sy && span > 2f) {
                drawLine(color, Offset(sx, sy), Offset(ex, ey), strokeWidth = strokeWidth)
            }
            d += step
        }
    }

    fun DrawScope.drawPixelProp(
        pr: PropObj,
        alpha: Float,
        now: Long,
        powerOnNow: Boolean,
        oblY: Float = -WorldCanvasRenderer.OBLIQUE_Y
    ) {
        when (pr.propType) {
            "tree" -> {
                drawFloridaLiveOak(pr, alpha, now)
                return
            }
            "watchtower" -> {
                drawWatchtower(pr, oblY, alpha, now)
                return
            }
            "canoe" -> {
                drawCanoe(pr, alpha)
                return
            }
            "plane" -> {
                drawAirfieldPlane(pr, alpha)
                return
            }
        }

        val ext = oblY * pr.heightScale
        val topY = pr.y - ext

        // 1. Soft graphic-novel noir offset cast shadow
        drawRect(
            color = Color.Black.copy(alpha = 0.48f * alpha),
            topLeft = Offset(pr.x + 5f, pr.y + 5f),
            size = Size(pr.w, pr.h)
        )
        // 2. Front extruded 2.5D vertical face with ink cross-hatching
        drawRect(
            color = Color(0xFF181B22).copy(alpha = alpha),
            topLeft = Offset(pr.x, pr.y + pr.h - ext),
            size = Size(pr.w, ext)
        )
        drawComicHatch(
            x = pr.x + 1f,
            y = pr.y + pr.h - ext + 1f,
            w = pr.w - 2f,
            h = ext - 2f,
            step = 5.5f,
            color = OUTLINE.copy(alpha = 0.36f * alpha)
        )
        // 3. Top face
        drawRect(
            color = Color(pr.colorHex).copy(alpha = alpha),
            topLeft = Offset(pr.x, topY),
            size = Size(pr.w, pr.h)
        )
        // Warm noir top-left cel-highlight rim + ink contour border
        drawRect(
            color = Color(0xFFFFF7E6).copy(alpha = 0.16f * alpha),
            topLeft = Offset(pr.x + 2f, topY + 2f),
            size = Size(max(0f, pr.w - 4f), 2.2f)
        )
        drawRect(
            color = OUTLINE.copy(alpha = alpha),
            topLeft = Offset(pr.x, topY),
            size = Size(pr.w, pr.h + ext),
            style = Stroke(width = 2.4f)
        )

        when {
            pr.propType == "tv" -> drawDetailedTv(pr, topY, ext, alpha, now, powerOnNow)
            pr.propType == "stove" -> drawDetailedStove(pr, topY, ext, alpha)
            pr.propType == "fridge" -> drawDetailedFridge(pr, topY, ext, alpha)
            pr.propType == "sink" -> drawDetailedSink(pr, topY, alpha)
            pr.propType == "toilet" -> drawDetailedToilet(pr, topY, alpha)
            pr.propType == "bathtub" -> drawDetailedBathtub(pr, topY, alpha)
            pr.propType == "sofa" -> drawDetailedSofa(pr, topY, ext, alpha)
            pr.propType == "bed" -> drawDetailedBed(pr, topY, alpha)
            pr.propType == "desk" || pr.propType == "command_desk" -> drawDetailedDesk(pr, topY, alpha, pr.propType == "command_desk")
            pr.propType == "checkout" -> drawDetailedCheckout(pr, topY, alpha)
            pr.propType == "guitar_wall" -> drawGuitarWall(pr, topY, ext, alpha)
            pr.propType == "drum_kit" -> drawDrumKit(pr, topY, alpha)
            pr.propType == "amp_stack" -> drawAmpStack(pr, topY, ext, alpha)
            pr.propType == "music_counter" -> drawMusicCounter(pr, topY, ext, alpha)
            pr.propType == "garage_bench" -> drawGarageBench(pr, topY, ext, alpha)
            pr.propType == "picnic_table" -> drawPicnicTable(pr, topY, alpha)
            pr.propType == "vehicle" -> drawPixelVehicle(pr, topY, ext, alpha, now)
            pr.propType.startsWith("bunk_") -> {
                drawRect(Color(0xFF1F2937).copy(alpha = alpha), Offset(pr.x + 2f, topY + 2f), Size(pr.w - 4f, pr.h - 4f), style = Stroke(width = 2.5f))
                drawRect(Color(0xFFE5DFD3).copy(alpha = alpha), Offset(pr.x + 5f, topY + 5f), Size(14f, pr.h - 10f))
                drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 5f, topY + 5f), Size(14f, pr.h - 10f), style = Stroke(width = 1.4f))
                drawRect(Color(0xFF3E5622).copy(alpha = alpha), Offset(pr.x + 21f, topY + 4f), Size(pr.w - 25f, pr.h - 8f))
                drawComicHatch(pr.x + 22f, topY + pr.h * 0.5f, pr.w - 27f, pr.h * 0.5f - 5f, step = 6f, color = OUTLINE.copy(alpha = 0.25f * alpha))
            }
            pr.propType.startsWith("locker_") -> {
                drawLine(OUTLINE.copy(alpha = alpha), Offset(pr.x + pr.w * 0.5f, topY), Offset(pr.x + pr.w * 0.5f, topY + pr.h), strokeWidth = 2f)
                for (louver in 0..2) {
                    val ly = topY + 4f + louver * 3.2f
                    drawLine(Color(0xFF94A3B8).copy(alpha = alpha), Offset(pr.x + 4f, ly), Offset(pr.x + 11f, ly), strokeWidth = 1.4f)
                    drawLine(Color(0xFF94A3B8).copy(alpha = alpha), Offset(pr.x + pr.w - 11f, ly), Offset(pr.x + pr.w - 4f, ly), strokeWidth = 1.4f)
                }
            }
            pr.propType == "mess_table" || pr.propType == "counter" -> {
                drawLine(OUTLINE.copy(alpha = 0.45f * alpha), Offset(pr.x + 2f, topY + pr.h * 0.36f), Offset(pr.x + pr.w - 2f, topY + pr.h * 0.36f), strokeWidth = 1.2f)
                drawLine(OUTLINE.copy(alpha = 0.45f * alpha), Offset(pr.x + 2f, topY + pr.h * 0.68f), Offset(pr.x + pr.w - 2f, topY + pr.h * 0.68f), strokeWidth = 1.2f)
                // Inked metal cafeteria trays + coffee mug + lantern candle
                drawRect(Color(0xFFD1D5DB).copy(alpha = alpha), Offset(pr.x + 9f, topY + 6f), Size(15f, 11f))
                drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 9f, topY + 6f), Size(15f, 11f), style = Stroke(width = 1.2f))
                drawRect(Color(0xFFD1D5DB).copy(alpha = alpha), Offset(pr.x + pr.w - 27f, topY + 8f), Size(15f, 11f))
                drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + pr.w - 27f, topY + 8f), Size(15f, 11f), style = Stroke(width = 1.2f))
                drawCircle(Color(0xFFF59E0B).copy(alpha = alpha), radius = 3.2f, center = Offset(pr.x + pr.w * 0.5f, topY + pr.h * 0.5f))
            }
            pr.propType == "workbench" -> {
                // Pegboard blueprint + glowing lamp + wrench + ammo brass boxes
                drawRect(Color(0xFF1E3A8A).copy(alpha = 0.9f * alpha), Offset(pr.x + 8f, topY + 5f), Size(28f, 16f))
                drawRect(Color(0xFF93C5FD).copy(alpha = 0.45f * alpha), Offset(pr.x + 11f, topY + 8f), Size(22f, 10f), style = Stroke(width = 1f))
                drawRect(Color(0xFFEA580C).copy(alpha = alpha), Offset(pr.x + 44f, topY + 7f), Size(16f, 10f))
                drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 44f, topY + 7f), Size(16f, 10f), style = Stroke(width = 1.2f))
                drawRect(Color(0xFF94A3B8).copy(alpha = alpha), Offset(pr.x + pr.w - 20f, topY + 5f), Size(12f, 8f))
            }
            pr.propType == "shelf" -> {
                for (row in 0..2) {
                    val ry = topY + 6f + row * (pr.h / 3.4f)
                    if (ry < topY + pr.h - 6f) {
                        drawLine(OUTLINE.copy(alpha = alpha), Offset(pr.x + 2f, ry), Offset(pr.x + pr.w - 2f, ry), strokeWidth = 2f)
                        drawRect(Color(0xFFB45309).copy(alpha = alpha), Offset(pr.x + 6f, ry - 4f), Size(9f, 4f))
                        drawRect(Color(0xFF047857).copy(alpha = alpha), Offset(pr.x + 18f, ry - 4f), Size(7f, 4f))
                        drawRect(Color(0xFFDC2626).copy(alpha = alpha), Offset(pr.x + 28f, ry - 4f), Size(6f, 4f))
                    }
                }
            }
            pr.propType == "storage_depot" || pr.propType.startsWith("storage_crate") -> {
                drawRect(Color(0xFF1E293B).copy(alpha = alpha), Offset(pr.x + 10f, topY), Size(5f, pr.h))
                drawRect(Color(0xFF1E293B).copy(alpha = alpha), Offset(pr.x + pr.w - 15f, topY), Size(5f, pr.h))
                drawRect(Color(0xFFF59E0B).copy(alpha = alpha), Offset(pr.x + pr.w * 0.5f - 6f, topY + pr.h * 0.5f - 4f), Size(12f, 8f))
                drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + pr.w * 0.5f - 6f, topY + pr.h * 0.5f - 4f), Size(12f, 8f), style = Stroke(width = 1.3f))
            }
            pr.propType == "market_terminal" -> {
                drawRect(Color(0xFF0F172A).copy(alpha = alpha), Offset(pr.x + 4f, topY + 4f), Size(pr.w - 8f, pr.h - 8f))
                drawRect(Color(0xFF38BDF8).copy(alpha = alpha), Offset(pr.x + 7f, topY + 6f), Size(18f, 10f))
                drawRect(Color(0xFFC084FC).copy(alpha = alpha), Offset(pr.x + 29f, topY + 6f), Size(18f, 10f))
                drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 4f, topY + 4f), Size(pr.w - 8f, pr.h - 8f), style = Stroke(width = 1.4f))
            }
            pr.propType == "cd_rack" -> {
                val spineColors = listOf(0xFF38BDF8, 0xFFF43F5E, 0xFFF59E0B, 0xFFC084FC, 0xFF34D399)
                spineColors.forEachIndexed { idx, col ->
                    val sy = topY + 5f + idx * 8f
                    if (sy + 4f < topY + pr.h) {
                        drawRect(Color(col).copy(alpha = alpha), Offset(pr.x + 4f, sy), Size(pr.w - 8f, 4f))
                        drawRect(OUTLINE.copy(alpha = 0.65f * alpha), Offset(pr.x + 4f, sy), Size(pr.w - 8f, 4f), style = Stroke(width = 0.9f))
                    }
                }
            }
            pr.propType.startsWith("med_bed") -> {
                drawRect(Color(0xFFE2E8F0).copy(alpha = alpha), Offset(pr.x + 4f, topY + 4f), Size(pr.w - 8f, pr.h - 8f))
                drawRect(Color(0xFF0EA5E9).copy(alpha = 0.72f * alpha), Offset(pr.x + 18f, topY + 5f), Size(pr.w - 24f, pr.h - 10f))
                drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 4f, topY + 4f), Size(pr.w - 8f, pr.h - 8f), style = Stroke(width = 1.4f))
            }
            pr.propType == "med_cabinet" -> {
                drawRect(Color(0xFFEF4444).copy(alpha = alpha), Offset(pr.x + pr.w * 0.5f - 2f, topY + 6f), Size(4f, 14f))
                drawRect(Color(0xFFEF4444).copy(alpha = alpha), Offset(pr.x + pr.w * 0.5f - 7f, topY + 11f), Size(14f, 4f))
            }
            pr.propType == "water_cooler" -> {
                drawCircle(Color(0xFF38BDF8).copy(alpha = alpha), radius = 9f, center = Offset(pr.x + pr.w * 0.5f, topY + pr.h * 0.5f))
                drawCircle(OUTLINE.copy(alpha = alpha), radius = 9f, center = Offset(pr.x + pr.w * 0.5f, topY + pr.h * 0.5f), style = Stroke(width = 1.5f))
                drawCircle(Color(0xFFBAE6FD).copy(alpha = alpha), radius = 3.8f, center = Offset(pr.x + pr.w * 0.5f - 2f, topY + pr.h * 0.5f - 2f))
            }
            pr.propType == "hvac" -> {
                drawCircle(Color(0xFF1F242D).copy(alpha = alpha), radius = minOf(pr.w, pr.h) * 0.34f, center = Offset(pr.x + pr.w * 0.35f, topY + pr.h * 0.5f))
                drawCircle(Color(0xFF64748B).copy(alpha = alpha), radius = minOf(pr.w, pr.h) * 0.34f, center = Offset(pr.x + pr.w * 0.35f, topY + pr.h * 0.5f), style = Stroke(width = 2f))
                for (slat in 0..3) {
                    val sy = topY + 6f + slat * 5f
                    if (sy < topY + pr.h - 4f) {
                        drawLine(Color(0xFF64748B).copy(alpha = alpha), Offset(pr.x + pr.w * 0.65f, sy), Offset(pr.x + pr.w - 5f, sy), strokeWidth = 1.6f)
                    }
                }
            }
            pr.propType == "sandbag" -> {
                var sx = pr.x + 3f
                while (sx < pr.x + pr.w - 8f) {
                    drawRoundRect(Color(0xFF8C7A5B).copy(alpha = alpha), Offset(sx, topY + 2f), Size(14f, pr.h - 4f), CornerRadius(4f, 4f))
                    drawRoundRect(OUTLINE.copy(alpha = alpha), Offset(sx, topY + 2f), Size(14f, pr.h - 4f), CornerRadius(4f, 4f), style = Stroke(width = 1.6f))
                    sx += 13f
                }
            }
            pr.propType == "camp_firepit" -> {
                val cx = pr.x + pr.w * 0.5f
                val cy = topY + pr.h * 0.5f
                drawCircle(Color(0xFF57534E).copy(alpha = alpha), radius = 19f, center = Offset(cx, cy))
                drawCircle(OUTLINE.copy(alpha = alpha), radius = 19f, center = Offset(cx, cy), style = Stroke(width = 2f))
                drawCircle(Color(0xFF1C1917).copy(alpha = alpha), radius = 13f, center = Offset(cx, cy))
                val emberPulse = 0.7f + 0.3f * sin((now % 850L) / 850f * 2f * PI.toFloat())
                drawCircle(Color(0xFFF97316).copy(alpha = emberPulse * alpha), radius = 9.5f, center = Offset(cx, cy))
                drawCircle(Color(0xFFFDE047).copy(alpha = emberPulse * alpha), radius = 4.8f, center = Offset(cx, cy))
            }
            pr.isClearableBox -> {
                drawLine(Color(0xFF3E2614).copy(alpha = alpha), Offset(pr.x + 2f, topY + 2f), Offset(pr.x + pr.w - 2f, topY + pr.h - 2f), strokeWidth = 2.2f)
                drawLine(Color(0xFF3E2614).copy(alpha = alpha), Offset(pr.x + pr.w - 2f, topY + 2f), Offset(pr.x + 2f, topY + pr.h - 2f), strokeWidth = 2.2f)
                drawComicHatch(pr.x + 3f, topY + 3f, pr.w - 6f, pr.h - 6f, step = 7f, color = OUTLINE.copy(alpha = 0.22f * alpha))
            }
        }
    }

    /**
     * Hyper-detailed Inked Graphic-Novel CRT/Console TV:
     * Wood-grained cabinet, V-antenna rabbit ears, recessed phosphor CRT screen casting ambient glow,
     * rotary tuning & volume KNOBS, tactile BUTTONS, speaker grille dots & power LED.
     */
    private fun DrawScope.drawDetailedTv(
        pr: PropObj,
        topY: Float,
        ext: Float,
        alpha: Float,
        now: Long,
        powerOnNow: Boolean
    ) {
        val tvLive = pr.tvPoweredOn && powerOnNow
        // Classic V-shaped rabbit-ear antenna on top of cabinet
        val antX = pr.x + pr.w * 0.5f
        val antY = topY + 2f
        drawLine(OUTLINE.copy(alpha = alpha), Offset(antX, antY), Offset(antX - 8f, antY - 10f), strokeWidth = 2.2f)
        drawLine(OUTLINE.copy(alpha = alpha), Offset(antX, antY), Offset(antX + 8f, antY - 10f), strokeWidth = 2.2f)
        drawLine(Color(0xFFCBD5E1).copy(alpha = alpha), Offset(antX, antY), Offset(antX - 8f, antY - 10f), strokeWidth = 1.1f)
        drawLine(Color(0xFFCBD5E1).copy(alpha = alpha), Offset(antX, antY), Offset(antX + 8f, antY - 10f), strokeWidth = 1.1f)

        val bezelY = topY + 2f
        val bezelH = pr.h + ext - 4f
        drawRect(Color(0xFF4B2E1E).copy(alpha = alpha), Offset(pr.x + 1f, topY + 1f), Size(pr.w - 2f, pr.h - 2f))
        drawRect(Color(0xFF1A1E26).copy(alpha = alpha), Offset(pr.x + 2f, bezelY), Size(pr.w - 4f, bezelH))

        val hasSidePanel = pr.w >= 32f
        val screenW = if (hasSidePanel) (pr.w - 14f) * 0.72f else pr.w - 10f
        val screenX = pr.x + 5f
        val screenY = bezelY + 3f
        val screenH = max(8f, bezelH - 10f)

        if (tvLive) {
            val flicker = 0.80f + 0.20f * sin((now % 180L) / 180f * 2f * PI.toFloat())
            val shift = ((now / 120L) % 3L).toInt()
            val col = when (shift) {
                0 -> Color(0xFF38BDF8)
                1 -> Color(0xFF67E8F9)
                else -> Color(0xFFBAE6FD)
            }
            drawRect(col.copy(alpha = flicker * alpha), Offset(screenX, screenY), Size(screenW, screenH))
            // Crisp comic scanlines + static noise bar
            var sy = screenY + 2f
            while (sy < screenY + screenH - 1f) {
                drawLine(Color(0xFF0C4A6E).copy(alpha = 0.28f * alpha), Offset(screenX + 1f, sy), Offset(screenX + screenW - 1f, sy), strokeWidth = 0.8f)
                sy += 3f
            }
            drawLine(
                Color.White.copy(alpha = 0.72f * alpha),
                Offset(screenX + 1f, screenY + 2f + shift * 2f),
                Offset(screenX + screenW - 1f, screenY + 2f + shift * 2f),
                strokeWidth = 1.4f
            )
            // Floor phosphor glow pool
            drawCircle(
                Color(0xFF38BDF8).copy(alpha = 0.28f * flicker * alpha),
                radius = 40f,
                center = Offset(pr.x + pr.w * 0.5f, pr.y + pr.h + 12f)
            )
        } else {
            drawRect(Color(0xFF0A0D14).copy(alpha = alpha), Offset(screenX, screenY), Size(screenW, screenH))
            drawLine(
                Color.White.copy(alpha = 0.16f * alpha),
                Offset(screenX + 2f, screenY + 2f),
                Offset(screenX + 10f, screenY + screenH - 2f),
                strokeWidth = 1.4f
            )
        }
        drawRect(OUTLINE.copy(alpha = alpha), Offset(screenX, screenY), Size(screenW, screenH), style = Stroke(width = 1.6f))

        if (hasSidePanel) {
            val panelX = screenX + screenW + 2.5f
            drawCircle(Color(0xFF94A3B8).copy(alpha = alpha), radius = 2.6f, center = Offset(panelX + 3f, screenY + 3.5f))
            drawCircle(OUTLINE.copy(alpha = alpha), radius = 2.6f, center = Offset(panelX + 3f, screenY + 3.5f), style = Stroke(width = 1f))
            drawCircle(OUTLINE.copy(alpha = alpha), radius = 1f, center = Offset(panelX + 3.8f, screenY + 3f))
            drawCircle(Color(0xFF94A3B8).copy(alpha = alpha), radius = 2.6f, center = Offset(panelX + 3f, screenY + 9.5f))
            drawCircle(OUTLINE.copy(alpha = alpha), radius = 2.6f, center = Offset(panelX + 3f, screenY + 9.5f), style = Stroke(width = 1f))
            drawCircle(OUTLINE.copy(alpha = alpha), radius = 1f, center = Offset(panelX + 2.2f, screenY + 9.5f))

            for (r in 0..1) {
                for (c in 0..1) {
                    drawRect(
                        Color(0xFF090B0E).copy(alpha = alpha),
                        Offset(panelX + 1.2f + c * 2.5f, screenY + 13.5f + r * 2.5f),
                        Size(1.5f, 1.5f)
                    )
                }
            }
        }

        val btnY = bezelY + bezelH - 5f
        for (i in 0..3) {
            drawRect(
                color = Color(if (i == 0) 0xFFDC2626 else 0xFFCBD5E1).copy(alpha = alpha),
                topLeft = Offset(screenX + 2f + i * 4.5f, btnY),
                size = Size(3f, 2.5f)
            )
        }
        val ledColor = if (tvLive) Color(0xFF34D399) else Color(0xFFEF4444)
        drawCircle(ledColor.copy(alpha = alpha), radius = 1.6f, center = Offset(pr.x + pr.w - 5f, btnY + 1.2f))
    }

    /**
     * Hyper-detailed Inked Kitchen Stove/Oven:
     * 4 cooktop coiled burners, digital backsplash clock + 5 rotary dials,
     * metallic handle & tinted glass oven door with warm interior glow + rack lines.
     */
    private fun DrawScope.drawDetailedStove(pr: PropObj, topY: Float, ext: Float, alpha: Float) {
        drawRect(Color(0xFFE5DFD3).copy(alpha = 0.94f * alpha), Offset(pr.x + 2f, topY + 2f), Size(pr.w - 4f, pr.h - 4f))
        // Raised rear backsplash w/ digital clock pill
        drawRect(Color(0xFF1F242D).copy(alpha = alpha), Offset(pr.x + 2f, topY + 1f), Size(pr.w - 4f, 5f))
        drawRect(Color(0xFF34D399).copy(alpha = 0.85f * alpha), Offset(pr.x + pr.w * 0.5f - 5f, topY + 2f), Size(10f, 3f))

        val bx = listOf(pr.x + pr.w * 0.28f, pr.x + pr.w * 0.72f)
        val by = listOf(topY + pr.h * 0.36f, topY + pr.h * 0.70f)
        for (cx in bx) {
            for (cy in by) {
                drawCircle(Color(0xFF111827).copy(alpha = alpha), radius = 5.4f, center = Offset(cx, cy))
                drawCircle(Color(0xFF64748B).copy(alpha = alpha), radius = 3.2f, center = Offset(cx, cy), style = Stroke(width = 1.2f))
                drawLine(Color(0xFF374151).copy(alpha = alpha), Offset(cx - 5f, cy), Offset(cx + 5f, cy), strokeWidth = 1f)
                drawLine(Color(0xFF374151).copy(alpha = alpha), Offset(cx, cy - 5f), Offset(cx, cy + 5f), strokeWidth = 1f)
            }
        }
        val dialY = topY + pr.h - 3f
        for (i in 0..4) {
            val dx = pr.x + 6f + i * ((pr.w - 12f) / 4f)
            drawCircle(OUTLINE.copy(alpha = alpha), radius = 2.2f, center = Offset(dx, dialY))
            drawCircle(Color(0xFFE2E8F0).copy(alpha = alpha), radius = 1.1f, center = Offset(dx, dialY))
        }
        val frontTop = pr.y + pr.h - ext
        drawRect(Color(0xFF282E38).copy(alpha = alpha), Offset(pr.x + 2f, frontTop + 1f), Size(pr.w - 4f, ext - 2f))
        drawRect(Color(0xFFE2E8F0).copy(alpha = alpha), Offset(pr.x + 6f, frontTop + 2.5f), Size(pr.w - 12f, 2.2f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 6f, frontTop + 2.5f), Size(pr.w - 12f, 2.2f), style = Stroke(width = 0.8f))

        val winY = frontTop + 6f
        val winH = max(5f, ext - 8f)
        drawRect(Color(0xFF0A0D14).copy(alpha = alpha), Offset(pr.x + 7f, winY), Size(pr.w - 14f, winH))
        drawRect(Color(0xFFEA580C).copy(alpha = 0.25f * alpha), Offset(pr.x + 8f, winY + winH * 0.4f), Size(pr.w - 16f, winH * 0.5f))
        drawLine(Color(0xFF94A3B8).copy(alpha = 0.78f * alpha), Offset(pr.x + 8f, winY + winH * 0.55f), Offset(pr.x + pr.w - 8f, winY + winH * 0.55f), strokeWidth = 1.2f)
        drawLine(Color(0xFF7DD3FC).copy(alpha = 0.45f * alpha), Offset(pr.x + 10f, winY + 1f), Offset(pr.x + 16f, winY + winH - 1f), strokeWidth = 1.5f)
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 7f, winY), Size(pr.w - 14f, winH), style = Stroke(width = 1.4f))
    }

    private fun DrawScope.drawDetailedFridge(pr: PropObj, topY: Float, ext: Float, alpha: Float) {
        drawRect(Color(0xFFCBD5E1).copy(alpha = alpha), Offset(pr.x + 2f, topY + 2f), Size(pr.w - 4f, pr.h - 4f))
        val frontTop = pr.y + pr.h - ext
        drawRect(Color(0xFF94A3B8).copy(alpha = alpha), Offset(pr.x + 2f, frontTop + 1f), Size(pr.w - 4f, ext - 2f))
        drawComicHatch(pr.x + pr.w * 0.62f, frontTop + 2f, pr.w * 0.34f, ext - 4f, step = 5f, color = OUTLINE.copy(alpha = 0.22f * alpha))
        drawLine(OUTLINE.copy(alpha = alpha), Offset(pr.x + 2f, frontTop + ext * 0.36f), Offset(pr.x + pr.w - 2f, frontTop + ext * 0.36f), strokeWidth = 1.8f)
        drawRect(Color(0xFFF8FAFC).copy(alpha = alpha), Offset(pr.x + 5f, frontTop + 3f), Size(2.6f, ext * 0.26f))
        drawRect(Color(0xFFF8FAFC).copy(alpha = alpha), Offset(pr.x + 5f, frontTop + ext * 0.42f), Size(2.6f, ext * 0.42f))
        drawRect(Color(0xFF1E293B).copy(alpha = alpha), Offset(pr.x + pr.w * 0.5f - 4f, frontTop + ext * 0.48f), Size(8f, ext * 0.28f))
    }

    private fun DrawScope.drawDetailedSink(pr: PropObj, topY: Float, alpha: Float) {
        drawRect(Color(0xFFCBD5E1).copy(alpha = alpha), Offset(pr.x + 4f, topY + 5f), Size(pr.w - 8f, pr.h - 8f))
        drawRect(Color(0xFF475569).copy(alpha = alpha), Offset(pr.x + 7f, topY + 8f), Size(pr.w - 14f, pr.h - 14f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 7f, topY + 8f), Size(pr.w - 14f, pr.h - 14f), style = Stroke(width = 1.4f))
        drawCircle(Color(0xFF0F172A).copy(alpha = alpha), radius = 2.4f, center = Offset(pr.x + pr.w * 0.5f, topY + pr.h * 0.55f))
        drawRect(Color(0xFFF8FAFC).copy(alpha = alpha), Offset(pr.x + pr.w * 0.5f - 1.5f, topY + 2f), Size(3f, 8f))
        drawCircle(Color(0xFFEF4444).copy(alpha = alpha), radius = 1.8f, center = Offset(pr.x + pr.w * 0.5f - 6f, topY + 4f))
        drawCircle(Color(0xFF38BDF8).copy(alpha = alpha), radius = 1.8f, center = Offset(pr.x + pr.w * 0.5f + 6f, topY + 4f))
    }

    private fun DrawScope.drawDetailedToilet(pr: PropObj, topY: Float, alpha: Float) {
        drawRect(Color(0xFFF1F5F9).copy(alpha = alpha), Offset(pr.x + 3f, topY + 2f), Size(pr.w - 6f, 8f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 3f, topY + 2f), Size(pr.w - 6f, 8f), style = Stroke(width = 1.3f))
        drawRect(Color(0xFF94A3B8).copy(alpha = alpha), Offset(pr.x + 5f, topY + 4f), Size(4f, 2f))
        drawOval(Color(0xFFE2E8F0).copy(alpha = alpha), Offset(pr.x + 4f, topY + 10f), Size(pr.w - 8f, pr.h - 12f))
        drawOval(OUTLINE.copy(alpha = alpha), Offset(pr.x + 4f, topY + 10f), Size(pr.w - 8f, pr.h - 12f), style = Stroke(width = 1.4f))
        drawOval(Color(0xFF475569).copy(alpha = alpha), Offset(pr.x + 7f, topY + 13f), Size(pr.w - 14f, pr.h - 18f))
    }

    private fun DrawScope.drawDetailedBathtub(pr: PropObj, topY: Float, alpha: Float) {
        drawRoundRect(Color(0xFFF1F5F9).copy(alpha = alpha), Offset(pr.x + 3f, topY + 3f), Size(pr.w - 6f, pr.h - 6f), CornerRadius(6f, 6f))
        drawRoundRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 3f, topY + 3f), Size(pr.w - 6f, pr.h - 6f), CornerRadius(6f, 6f), style = Stroke(width = 1.6f))
        drawRoundRect(Color(0xFF94A3B8).copy(alpha = 0.55f * alpha), Offset(pr.x + 7f, topY + 6f), Size(pr.w - 14f, pr.h - 12f), CornerRadius(4f, 4f))
        drawCircle(Color(0xFF1E293B).copy(alpha = alpha), radius = 2.2f, center = Offset(pr.x + pr.w - 12f, topY + pr.h * 0.5f))
        drawRect(Color(0xFFCBD5E1).copy(alpha = alpha), Offset(pr.x + pr.w - 8f, topY + pr.h * 0.5f - 2f), Size(5f, 4f))
    }

    private fun DrawScope.drawDetailedSofa(pr: PropObj, topY: Float, ext: Float, alpha: Float) {
        drawRect(Color(0xFF2D1E1B).copy(alpha = alpha), Offset(pr.x + 2f, topY + 2f), Size(pr.w - 4f, 9f))
        drawRect(Color(0xFF2D1E1B).copy(alpha = alpha), Offset(pr.x + 2f, topY + 2f), Size(8f, pr.h - 4f))
        drawRect(Color(0xFF2D1E1B).copy(alpha = alpha), Offset(pr.x + pr.w - 10f, topY + 2f), Size(8f, pr.h - 4f))
        val innerW = pr.w - 20f
        val cushions = 3
        val cw = innerW / cushions
        for (i in 0 until cushions) {
            drawRect(
                Color(0xFF7B4942).copy(alpha = alpha),
                Offset(pr.x + 10f + i * cw + 1f, topY + 12f),
                Size(cw - 2f, pr.h - 15f)
            )
            drawRect(
                OUTLINE.copy(alpha = 0.7f * alpha),
                Offset(pr.x + 10f + i * cw + 1f, topY + 12f),
                Size(cw - 2f, pr.h - 15f),
                style = Stroke(width = 1.2f)
            )
        }
        // Accent folded noir ochre throw pillow
        drawRect(Color(0xFFF59E0B).copy(alpha = 0.85f * alpha), Offset(pr.x + 12f, topY + 13f), Size(9f, 9f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 12f, topY + 13f), Size(9f, 9f), style = Stroke(width = 1.2f))
    }

    private fun DrawScope.drawDetailedBed(pr: PropObj, topY: Float, alpha: Float) {
        drawRect(Color(0xFF3A2416).copy(alpha = alpha), Offset(pr.x + 1f, topY + 1f), Size(5f, pr.h - 2f))
        drawRect(Color(0xFFEAE2D3).copy(alpha = alpha), Offset(pr.x + 7f, topY + 4f), Size(11f, pr.h * 0.42f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 7f, topY + 4f), Size(11f, pr.h * 0.42f), style = Stroke(width = 1.2f))
        drawRect(Color(0xFFEAE2D3).copy(alpha = alpha), Offset(pr.x + 7f, topY + pr.h * 0.54f), Size(11f, pr.h * 0.42f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 7f, topY + pr.h * 0.54f), Size(11f, pr.h * 0.42f), style = Stroke(width = 1.2f))
        drawRect(Color(0xFF882924).copy(alpha = alpha), Offset(pr.x + 20f, topY + 3f), Size(pr.w - 23f, pr.h - 6f))
        drawRect(Color(0xFFA63A34).copy(alpha = alpha), Offset(pr.x + 20f, topY + 3f), Size(6f, pr.h - 6f))
        drawComicHatch(pr.x + 27f, topY + pr.h * 0.55f, pr.w - 31f, pr.h * 0.4f - 3f, step = 6f, color = OUTLINE.copy(alpha = 0.25f * alpha))
    }

    private fun DrawScope.drawDetailedDesk(pr: PropObj, topY: Float, alpha: Float, isCommand: Boolean) {
        drawRect(Color(0xFF0F172A).copy(alpha = alpha), Offset(pr.x + 12f, topY + 4f), Size(18f, 10f))
        drawRect(Color(if (isCommand) 0xFF34D399 else 0xFF38BDF8).copy(alpha = 0.85f * alpha), Offset(pr.x + 14f, topY + 5f), Size(14f, 7f))
        drawRect(Color(0xFF334155).copy(alpha = alpha), Offset(pr.x + 14f, topY + 16f), Size(14f, 5f))
        drawRect(Color(0xFFEAE2D3).copy(alpha = alpha), Offset(pr.x + pr.w - 20f, topY + 7f), Size(11f, 14f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + pr.w - 20f, topY + 7f), Size(11f, 14f), style = Stroke(width = 1.1f))
        drawCircle(Color(0xFFB45309).copy(alpha = alpha), radius = 2.6f, center = Offset(pr.x + 6f, topY + 8f))
    }

    private fun DrawScope.drawDetailedCheckout(pr: PropObj, topY: Float, alpha: Float) {
        drawRect(Color(0xFF0F172A).copy(alpha = alpha), Offset(pr.x + 4f, topY + 5f), Size(pr.w * 0.55f, pr.h - 10f))
        drawRect(Color(0xFFCBD5E1).copy(alpha = alpha), Offset(pr.x + pr.w * 0.65f, topY + 4f), Size(16f, 14f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + pr.w * 0.65f, topY + 4f), Size(16f, 14f), style = Stroke(width = 1.3f))
        drawRect(Color(0xFF34D399).copy(alpha = alpha), Offset(pr.x + pr.w * 0.67f, topY + 6f), Size(8f, 4f))
    }

    private fun DrawScope.drawGuitarWall(pr: PropObj, topY: Float, ext: Float, alpha: Float) {
        drawRect(Color(0xFF2B1B12).copy(alpha = alpha), Offset(pr.x + 2f, topY + 2f), Size(pr.w - 4f, pr.h - 4f))
        // Slatwall horizontal rails
        drawLine(OUTLINE.copy(alpha = 0.5f * alpha), Offset(pr.x + 3f, topY + pr.h * 0.25f), Offset(pr.x + pr.w - 3f, topY + pr.h * 0.25f), strokeWidth = 1f)
        drawLine(OUTLINE.copy(alpha = 0.5f * alpha), Offset(pr.x + 3f, topY + pr.h * 0.60f), Offset(pr.x + pr.w - 3f, topY + pr.h * 0.60f), strokeWidth = 1f)

        val bodyColors = listOf(0xFFEF4444, 0xFFF59E0B, 0xFF38BDF8, 0xFFD97706, 0xFF1F2937)
        val count = 5
        val spacing = (pr.w - 16f) / count
        for (i in 0 until count) {
            val gx = pr.x + 10f + i * spacing
            // Headstock & tuning pegs + inked neck
            drawRect(Color(0xFF452C1B).copy(alpha = alpha), Offset(gx + 2f, topY + 2f), Size(5f, 3.5f))
            drawRect(Color(0xFFD6B48C).copy(alpha = alpha), Offset(gx + 3f, topY + 5f), Size(3f, pr.h * 0.43f))
            drawRect(OUTLINE.copy(alpha = alpha), Offset(gx + 3f, topY + 5f), Size(3f, pr.h * 0.43f), style = Stroke(width = 0.9f))
            // Inked guitar body + pickguard / acoustic rosette
            val bodyCenter = Offset(gx + 4.5f, topY + pr.h * 0.68f)
            drawCircle(Color(bodyColors[i]).copy(alpha = alpha), radius = 6.8f, center = bodyCenter)
            drawCircle(OUTLINE.copy(alpha = alpha), radius = 6.8f, center = bodyCenter, style = Stroke(width = 1.6f))
            if (i == 3) {
                drawCircle(OUTLINE.copy(alpha = alpha), radius = 2.2f, center = Offset(gx + 4.5f, topY + pr.h * 0.64f))
            } else {
                drawRect(Color(0xFFF8FAFC).copy(alpha = 0.88f * alpha), Offset(gx + 5f, topY + pr.h * 0.62f), Size(3f, 5f))
            }
        }
    }

    private fun DrawScope.drawDrumKit(pr: PropObj, topY: Float, alpha: Float) {
        val cx = pr.x + pr.w * 0.5f
        val cy = topY + pr.h * 0.52f
        // Heavy inked drum rug underneath
        drawRect(Color(0xFF332924).copy(alpha = 0.72f * alpha), Offset(cx - 24f, cy - 18f), Size(48f, 36f))
        drawRect(OUTLINE.copy(alpha = 0.6f * alpha), Offset(cx - 24f, cy - 18f), Size(48f, 36f), style = Stroke(width = 1.2f))
        // Kick drum + white front resonant head + chrome hoop
        drawRect(Color(0xFF881B1B).copy(alpha = alpha), Offset(cx - 12f, cy - 10f), Size(24f, 16f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(cx - 12f, cy - 10f), Size(24f, 16f), style = Stroke(width = 1.8f))
        drawRect(Color(0xFFE5DFD3).copy(alpha = alpha), Offset(cx - 10f, cy + 4f), Size(20f, 4f))
        // Snare + floor tom + rack toms with dark ink outlines
        drawCircle(Color(0xFFE5DFD3).copy(alpha = alpha), radius = 7f, center = Offset(cx - 17f, cy + 8f))
        drawCircle(OUTLINE.copy(alpha = alpha), radius = 7f, center = Offset(cx - 17f, cy + 8f), style = Stroke(width = 1.6f))
        drawCircle(Color(0xFF991B1B).copy(alpha = alpha), radius = 8f, center = Offset(cx + 18f, cy + 8f))
        drawCircle(OUTLINE.copy(alpha = alpha), radius = 8f, center = Offset(cx + 18f, cy + 8f), style = Stroke(width = 1.6f))
        drawCircle(Color(0xFF991B1B).copy(alpha = alpha), radius = 6f, center = Offset(cx - 7f, cy - 12f))
        drawCircle(OUTLINE.copy(alpha = alpha), radius = 6f, center = Offset(cx - 7f, cy - 12f), style = Stroke(width = 1.4f))
        drawCircle(Color(0xFF991B1B).copy(alpha = alpha), radius = 6f, center = Offset(cx + 7f, cy - 12f))
        drawCircle(OUTLINE.copy(alpha = alpha), radius = 6f, center = Offset(cx + 7f, cy - 12f), style = Stroke(width = 1.4f))
        // Brass cymbals with ink bell ring & outlines
        drawCircle(Color(0xFFFBBF24).copy(alpha = alpha), radius = 8f, center = Offset(cx - 22f, cy - 12f))
        drawCircle(OUTLINE.copy(alpha = alpha), radius = 8f, center = Offset(cx - 22f, cy - 12f), style = Stroke(width = 1.6f))
        drawCircle(OUTLINE.copy(alpha = alpha), radius = 2f, center = Offset(cx - 22f, cy - 12f))
        drawCircle(Color(0xFFFDE047).copy(alpha = alpha), radius = 9f, center = Offset(cx + 22f, cy - 10f))
        drawCircle(OUTLINE.copy(alpha = alpha), radius = 9f, center = Offset(cx + 22f, cy - 10f), style = Stroke(width = 1.6f))
        drawCircle(OUTLINE.copy(alpha = alpha), radius = 2.2f, center = Offset(cx + 22f, cy - 10f))
    }

    private fun DrawScope.drawAmpStack(pr: PropObj, topY: Float, ext: Float, alpha: Float) {
        drawRect(Color(0xFF111827).copy(alpha = alpha), Offset(pr.x + 3f, topY + 3f), Size(pr.w - 6f, pr.h - 6f))
        val frontTop = pr.y + pr.h - ext
        drawRect(Color(0xFFD97706).copy(alpha = alpha), Offset(pr.x + 3f, frontTop + 2f), Size(pr.w - 6f, 5f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 3f, frontTop + 2f), Size(pr.w - 6f, 5f), style = Stroke(width = 1f))
        for (k in 0..4) {
            drawRect(Color.Black.copy(alpha = alpha), Offset(pr.x + 6f + k * 5f, frontTop + 3.5f), Size(2f, 2f))
        }
        drawCircle(Color(0xFFEF4444).copy(alpha = alpha), radius = 1.6f, center = Offset(pr.x + pr.w - 6f, frontTop + 4.5f))
        // 4x12 grille cloth hatching + speaker cones
        drawComicHatch(pr.x + 4f, frontTop + 8f, pr.w - 8f, ext - 10f, step = 4f, color = Color(0xFF475569).copy(alpha = 0.45f * alpha))
        drawCircle(Color(0xFF334155).copy(alpha = alpha), radius = 6f, center = Offset(pr.x + pr.w * 0.5f, frontTop + ext * 0.62f))
        drawCircle(OUTLINE.copy(alpha = alpha), radius = 6f, center = Offset(pr.x + pr.w * 0.5f, frontTop + ext * 0.62f), style = Stroke(width = 1.2f))
    }

    private fun DrawScope.drawMusicCounter(pr: PropObj, topY: Float, ext: Float, alpha: Float) {
        drawRect(Color(0xFF38BDF8).copy(alpha = 0.24f * alpha), Offset(pr.x + 5f, topY + 4f), Size(pr.w - 10f, pr.h - 8f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 5f, topY + 4f), Size(pr.w - 10f, pr.h - 8f), style = Stroke(width = 1.4f))
        val pedalCols = listOf(0xFFEF4444, 0xFF22C55E, 0xFFF59E0B, 0xFFC084FC, 0xFF3B82F6)
        pedalCols.forEachIndexed { idx, col ->
            val px = pr.x + 10f + idx * 15f
            if (px + 10f < pr.x + pr.w - 6f) {
                drawRect(Color(col).copy(alpha = alpha), Offset(px, topY + 8f), Size(10f, 14f))
                drawRect(OUTLINE.copy(alpha = alpha), Offset(px, topY + 8f), Size(10f, 14f), style = Stroke(width = 1.2f))
                drawCircle(Color.Black.copy(alpha = alpha), radius = 1.5f, center = Offset(px + 5f, topY + 11f))
                drawCircle(Color.White.copy(alpha = alpha), radius = 1.2f, center = Offset(px + 5f, topY + 18f))
            }
        }
        // Glass glare diagonal streak
        drawLine(Color.White.copy(alpha = 0.35f * alpha), Offset(pr.x + 9f, topY + 6f), Offset(pr.x + 21f, topY + pr.h - 6f), strokeWidth = 1.5f)
    }

    private fun DrawScope.drawGarageBench(pr: PropObj, topY: Float, ext: Float, alpha: Float) {
        drawRect(Color(0xFF991B1B).copy(alpha = alpha), Offset(pr.x + 2f, topY + 2f), Size(pr.w - 4f, pr.h - 4f))
        drawRect(Color(0xFF475569).copy(alpha = alpha), Offset(pr.x + 8f, topY + 5f), Size(28f, 12f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 8f, topY + 5f), Size(28f, 12f), style = Stroke(width = 1.2f))
        drawRect(Color(0xFF94A3B8).copy(alpha = alpha), Offset(pr.x + pr.w - 22f, topY + 4f), Size(12f, 10f))
        val frontTop = pr.y + pr.h - ext
        drawRect(Color(0xFF7F1D1D).copy(alpha = alpha), Offset(pr.x + 2f, frontTop + 1f), Size(pr.w - 4f, ext - 2f))
        for (r in 0..2) {
            val dy = frontTop + 3f + r * (ext / 3.4f)
            if (dy < frontTop + ext - 2f) {
                drawRect(Color(0xFFE2E8F0).copy(alpha = alpha), Offset(pr.x + 8f, dy), Size(pr.w - 16f, 1.8f))
            }
        }
    }

    private fun DrawScope.drawPicnicTable(pr: PropObj, topY: Float, alpha: Float) {
        drawRect(Color(0xFF5C3E28).copy(alpha = alpha), Offset(pr.x, topY - 6f), Size(pr.w, 5f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x, topY - 6f), Size(pr.w, 5f), style = Stroke(width = 1.4f))
        drawRect(Color(0xFF5C3E28).copy(alpha = alpha), Offset(pr.x, topY + pr.h + 2f), Size(pr.w, 5f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x, topY + pr.h + 2f), Size(pr.w, 5f), style = Stroke(width = 1.4f))
        drawLine(OUTLINE.copy(alpha = 0.65f * alpha), Offset(pr.x + 2f, topY + pr.h * 0.33f), Offset(pr.x + pr.w - 2f, topY + pr.h * 0.33f), strokeWidth = 1.4f)
        drawLine(OUTLINE.copy(alpha = 0.65f * alpha), Offset(pr.x + 2f, topY + pr.h * 0.66f), Offset(pr.x + pr.w - 2f, topY + pr.h * 0.66f), strokeWidth = 1.4f)
    }

    private fun DrawScope.drawCanoe(pr: PropObj, alpha: Float) {
        val cx = pr.x + pr.w * 0.5f
        val cy = pr.y + pr.h * 0.5f
        drawOval(Color.Black.copy(alpha = 0.45f * alpha), Offset(pr.x + 4f, pr.y + 5f), Size(pr.w, pr.h))
        val hull = Path().apply {
            moveTo(cx, pr.y)
            quadraticTo(pr.x + pr.w + 4f, cy, cx, pr.y + pr.h)
            quadraticTo(pr.x - 4f, cy, cx, pr.y)
            close()
        }
        drawPath(hull, Color(pr.colorHex).copy(alpha = alpha))
        drawPath(hull, OUTLINE.copy(alpha = alpha), style = Stroke(width = 2.4f))
        drawLine(Color(0xFF5C4028).copy(alpha = alpha), Offset(pr.x + 4f, pr.y + pr.h * 0.34f), Offset(pr.x + pr.w - 4f, pr.y + pr.h * 0.34f), strokeWidth = 3f)
        drawLine(Color(0xFF5C4028).copy(alpha = alpha), Offset(pr.x + 4f, pr.y + pr.h * 0.66f), Offset(pr.x + pr.w - 4f, pr.y + pr.h * 0.66f), strokeWidth = 3f)
    }

    private fun DrawScope.drawAirfieldPlane(pr: PropObj, alpha: Float) {
        val cy = pr.y + pr.h * 0.5f
        drawOval(Color.Black.copy(alpha = 0.48f * alpha), Offset(pr.x + 10f, pr.y + 10f), Size(pr.w, pr.h))
        drawRect(Color(0xFFCBD5E1).copy(alpha = alpha), Offset(pr.x + 42f, pr.y + 6f), Size(44f, pr.h - 12f))
        drawComicHatch(pr.x + 62f, pr.y + 8f, 22f, pr.h - 16f, step = 6f, color = OUTLINE.copy(alpha = 0.22f * alpha))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 42f, pr.y + 6f), Size(44f, pr.h - 12f), style = Stroke(width = 2.6f))

        drawRect(Color(0xFFE5DFD3).copy(alpha = alpha), Offset(pr.x + 12f, cy - 14f), Size(pr.w - 28f, 28f))
        drawRect(Color(0xFFDC2626).copy(alpha = alpha), Offset(pr.x + 16f, cy - 4f), Size(pr.w - 36f, 8f))
        drawRect(Color(0xFFCBD5E1).copy(alpha = alpha), Offset(pr.x + pr.w - 32f, cy - 28f), Size(20f, 56f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + pr.w - 32f, cy - 28f), Size(20f, 56f), style = Stroke(width = 2.2f))

        drawRect(Color(0xFF38BDF8).copy(alpha = 0.72f * alpha), Offset(pr.x + 34f, cy - 10f), Size(18f, 20f))
        drawLine(Color.White.copy(alpha = 0.5f * alpha), Offset(pr.x + 36f, cy - 8f), Offset(pr.x + 46f, cy + 8f), strokeWidth = 1.6f)
        drawRect(Color(0xFF1E293B).copy(alpha = alpha), Offset(pr.x + 4f, cy - 20f), Size(5f, 40f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 12f, cy - 14f), Size(pr.w - 28f, 28f), style = Stroke(width = 2.4f))
    }

    private fun DrawScope.drawFloridaLiveOak(pr: PropObj, alpha: Float, now: Long) {
        val cx = pr.x + pr.w * 0.5f
        val cy = pr.y + pr.h * 0.5f
        val sway = sin((now % 3800L) / 3800f * 2f * PI.toFloat() + cx * 0.02f) * 1.4f

        // Ground shadow pool + flared gnarled roots & bark grain
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.54f * alpha), Color.Transparent),
                center = Offset(cx + 2f, cy + 8f),
                radius = 40f
            ),
            topLeft = Offset(cx - 38f, cy - 8f),
            size = Size(76f, 34f)
        )
        val trunkPath = Path().apply {
            moveTo(cx - 6.5f, cy - 28f)
            lineTo(cx + 6.5f, cy - 28f)
            quadraticTo(cx + 8f, cy - 10f, cx + 13f, cy + 4f)
            lineTo(cx + 5f, cy + 6f)
            lineTo(cx, cy + 2f)
            lineTo(cx - 6f, cy + 6f)
            lineTo(cx - 13f, cy + 4f)
            quadraticTo(cx - 8f, cy - 10f, cx - 6.5f, cy - 28f)
            close()
        }
        drawPath(trunkPath, Brush.horizontalGradient(listOf(Color(0xFF211711), Color(0xFF422F22), Color(0xFF2B1E16)), cx - 12f, cx + 12f))
        drawLine(OUTLINE.copy(alpha = 0.65f * alpha), Offset(cx - 2f, cy - 24f), Offset(cx - 3f, cy + 1f), strokeWidth = 1.2f)
        drawLine(OUTLINE.copy(alpha = 0.55f * alpha), Offset(cx + 2.5f, cy - 22f), Offset(cx + 4f, cy), strokeWidth = 1.1f)
        drawPath(trunkPath, OUTLINE.copy(alpha = alpha), style = Stroke(width = 1.6f))

        // Organic clustered oak/pine boughs matching the smoky splash-screen foliage
        val baseGreen = Color(pr.colorHex)
        val deepTealShadow = Color(0xFF0E1D19).copy(alpha = alpha)
        val midMoss = baseGreen.copy(alpha = alpha)
        val litOlive = Color(0xFF4F6B46).copy(alpha = 0.90f * alpha)

        val lobes = listOf(
            Triple(-16f, -24f, 17f),
            Triple(16f, -24f, 16.5f),
            Triple(-10f, -38f, 17.5f),
            Triple(12f, -37f, 16f),
            Triple(0f, -30f, 22f),
            Triple(-4f, -43f, 13.5f)
        )
        // Outer ink contour pass behind lobes
        for ((ox, oy, r) in lobes) {
            drawCircle(OUTLINE.copy(alpha = 0.85f * alpha), radius = r + 1.8f, center = Offset(cx + ox + sway, cy + oy))
        }
        for ((idx, lobe) in lobes.withIndex()) {
            val (ox, oy, r) = lobe
            val lx = cx + ox + sway
            val ly = cy + oy
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(if (idx >= 3) litOlive else midMoss, deepTealShadow),
                    center = Offset(lx - r * 0.28f, ly - r * 0.28f),
                    radius = r * 1.15f
                ),
                radius = r,
                center = Offset(lx, ly)
            )
        }
        // Ink foliage scallop detail lines
        drawArc(
            color = Color(0xFF8AA178).copy(alpha = 0.42f * alpha),
            startAngle = 200f,
            sweepAngle = 92f,
            useCenter = false,
            topLeft = Offset(cx - 22f + sway, cy - 52f),
            size = Size(40f, 40f),
            style = Stroke(width = 1.6f, cap = StrokeCap.Round)
        )

        // Delicate swaying Spanish moss wisps (matches splash oaks)
        val mossCol = Color(0xFF6B7D69).copy(alpha = 0.88f * alpha)
        listOf(-20f to 13f, -12f to 17f, -3f to 11f, 10f to 15f, 19f to 11f).forEach { (ox, h) ->
            val mx = cx + ox + sway
            drawLine(OUTLINE.copy(alpha = 0.55f * alpha), Offset(mx, cy - 19f), Offset(mx + sway * 0.5f, cy - 19f + h), strokeWidth = 2.6f, cap = StrokeCap.Round)
            drawLine(mossCol, Offset(mx, cy - 19f), Offset(mx + sway * 0.5f, cy - 19f + h), strokeWidth = 1.6f, cap = StrokeCap.Round)
        }
    }

    private fun DrawScope.drawWatchtower(pr: PropObj, oblY: Float, alpha: Float, now: Long) {
        val towerH = oblY * 2.15f
        val topY = pr.y - towerH
        val cx = pr.x + pr.w * 0.5f

        // Ground shadow beneath tower stilts
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.62f * alpha), Color.Transparent),
                center = Offset(cx + 8f, pr.y + pr.h * 0.6f),
                radius = pr.w * 0.72f
            ),
            topLeft = Offset(pr.x - 12f, pr.y + pr.h * 0.12f),
            size = Size(pr.w + 28f, pr.h * 0.9f)
        )

        // Four Heavy Timber Stilts + X-Bracing Trusses
        val postW = 7f
        val postsX = listOf(pr.x + 8f, pr.x + pr.w - 15f)
        val baseBotY = pr.y + pr.h - 4f
        val deckY = topY + 26f
        for (px in postsX) {
            drawRect(Color(0xFF3A281A).copy(alpha = alpha), Offset(px, deckY), Size(postW, baseBotY - deckY))
            drawRect(OUTLINE.copy(alpha = alpha), Offset(px, deckY), Size(postW, baseBotY - deckY), style = Stroke(width = 1.6f))
        }
        // Diagonal timber cross-bracing
        drawLine(Color(0xFF4F3826).copy(alpha = alpha), Offset(pr.x + 11f, deckY + 6f), Offset(pr.x + pr.w - 11f, baseBotY - 6f), strokeWidth = 3.5f)
        drawLine(Color(0xFF4F3826).copy(alpha = alpha), Offset(pr.x + pr.w - 11f, deckY + 6f), Offset(pr.x + 11f, baseBotY - 6f), strokeWidth = 3.5f)
        drawLine(OUTLINE.copy(alpha = 0.75f * alpha), Offset(pr.x + 11f, deckY + 6f), Offset(pr.x + pr.w - 11f, baseBotY - 6f), strokeWidth = 1.2f)
        drawLine(OUTLINE.copy(alpha = 0.75f * alpha), Offset(pr.x + pr.w - 11f, deckY + 6f), Offset(pr.x + 11f, baseBotY - 6f), strokeWidth = 1.2f)

        // Elevated Timber Walkway Deck & Parapet Guardrail
        drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF6B4F37).copy(alpha = alpha), Color(0xFF3D2B1D).copy(alpha = alpha))),
            topLeft = Offset(pr.x + 2f, deckY - 12f),
            size = Size(pr.w - 4f, 16f)
        )
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 2f, deckY - 12f), Size(pr.w - 4f, 16f), style = Stroke(width = 1.8f))

        // Raised Guard Cabin & Peaked Corrugated Roof
        val cabX = pr.x + 10f
        val cabW = pr.w - 20f
        drawRect(Color(0xFF1F272B).copy(alpha = alpha), Offset(cabX, topY - 4f), Size(cabW, 24f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(cabX, topY - 4f), Size(cabW, 24f), style = Stroke(width = 1.6f))
        val roof = Path().apply {
            moveTo(pr.x - 4f, topY - 4f)
            lineTo(cx, topY - 22f)
            lineTo(pr.x + pr.w + 4f, topY - 4f)
            close()
        }
        drawPath(roof, Brush.verticalGradient(listOf(Color(0xFF3B484D).copy(alpha = alpha), Color(0xFF1E2629).copy(alpha = alpha))))
        drawPath(roof, OUTLINE.copy(alpha = alpha), style = Stroke(width = 2f))

        // Amber Watchtower Searchlight Beacon & Subtle Sweeping Cone
        val sweep = sin((now % 6500L) / 6500f * 2f * PI.toFloat()) * 0.42f
        val lampPos = Offset(cx, deckY - 10f)
        val beamLen = 185f
        val a1 = (PI.toFloat() * 0.5f) + sweep - 0.24f
        val a2 = (PI.toFloat() * 0.5f) + sweep + 0.24f
        val beamPath = Path().apply {
            moveTo(lampPos.x, lampPos.y)
            lineTo(lampPos.x + cos(a1) * beamLen, lampPos.y + sin(a1) * beamLen)
            lineTo(lampPos.x + cos(a2) * beamLen, lampPos.y + sin(a2) * beamLen)
            close()
        }
        drawPath(
            beamPath,
            Brush.radialGradient(
                colors = listOf(Color(0xFFF5B749).copy(alpha = 0.26f * alpha), Color(0xFFF59E0B).copy(alpha = 0.08f * alpha), Color.Transparent),
                center = lampPos,
                radius = beamLen
            )
        )
        drawCircle(Color(0xFFFDE68A).copy(alpha = alpha), radius = 4.2f, center = lampPos)
    }

    private fun DrawScope.drawPixelVehicle(pr: PropObj, topY: Float, ext: Float, alpha: Float, now: Long) {
        // Chunky inked tires
        drawRect(Color(0xFF0A0C10).copy(alpha = alpha), Offset(pr.x - 3f, topY + 8f), Size(5.5f, 13f))
        drawRect(Color(0xFF0A0C10).copy(alpha = alpha), Offset(pr.x + pr.w - 2.5f, topY + 8f), Size(5.5f, 13f))
        drawRect(Color(0xFF0A0C10).copy(alpha = alpha), Offset(pr.x - 3f, topY + pr.h - 21f), Size(5.5f, 13f))
        drawRect(Color(0xFF0A0C10).copy(alpha = alpha), Offset(pr.x + pr.w - 2.5f, topY + pr.h - 21f), Size(5.5f, 13f))

        // Inked hood crease lines + roof frame
        drawLine(OUTLINE.copy(alpha = 0.55f * alpha), Offset(pr.x + 8f, topY + 3f), Offset(pr.x + 10f, topY + 10f), strokeWidth = 1.2f)
        drawLine(OUTLINE.copy(alpha = 0.55f * alpha), Offset(pr.x + pr.w - 8f, topY + 3f), Offset(pr.x + pr.w - 10f, topY + 10f), strokeWidth = 1.2f)

        // Front & rear glass windshields with comic diagonal reflection lines
        val wsH = max(8f, pr.h * 0.20f)
        drawRect(Color(0xFF7DD3FC).copy(alpha = 0.58f * alpha), Offset(pr.x + 5f, topY + 10f), Size(pr.w - 10f, wsH))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 5f, topY + 10f), Size(pr.w - 10f, wsH), style = Stroke(width = 1.5f))
        drawLine(Color.White.copy(alpha = 0.55f * alpha), Offset(pr.x + 8f, topY + 11f), Offset(pr.x + 15f, topY + 10f + wsH - 1f), strokeWidth = 1.4f)

        drawRect(Color(0xFF38BDF8).copy(alpha = 0.40f * alpha), Offset(pr.x + 6f, topY + pr.h - 15f), Size(pr.w - 12f, 8f))
        drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 6f, topY + pr.h - 15f), Size(pr.w - 12f, 8f), style = Stroke(width = 1.4f))

        drawRect(Color(0xFF1F2937).copy(alpha = alpha), Offset(pr.x - 3f, topY + 16f), Size(3f, 4f))
        drawRect(Color(0xFF1F2937).copy(alpha = alpha), Offset(pr.x + pr.w, topY + 16f), Size(3f, 4f))
        drawRect(Color(0xFFFDE047).copy(alpha = 0.88f * alpha), Offset(pr.x + 4f, topY + 1f), Size(6f, 3f))
        drawRect(Color(0xFFFDE047).copy(alpha = 0.88f * alpha), Offset(pr.x + pr.w - 10f, topY + 1f), Size(6f, 3f))
        drawRect(Color(0xFFDC2626).copy(alpha = 0.88f * alpha), Offset(pr.x + 4f, topY + pr.h - 4f), Size(6f, 3f))
        drawRect(Color(0xFFDC2626).copy(alpha = 0.88f * alpha), Offset(pr.x + pr.w - 10f, topY + pr.h - 4f), Size(6f, 3f))

        if (pr.vehicleKind == VehicleKind.POLICE_CRUISER) {
            // Crisp B&W patrol roof bar + subtle pulsing emergency glow
            val lw = (pr.w - 12f) * 0.5f
            drawRect(Color(0xFFEF4444).copy(alpha = alpha), Offset(pr.x + 6f, topY + pr.h * 0.42f), Size(lw, 5.5f))
            drawRect(Color(0xFF3B82F6).copy(alpha = alpha), Offset(pr.x + pr.w * 0.5f, topY + pr.h * 0.42f), Size(lw, 5.5f))
            drawRect(OUTLINE.copy(alpha = alpha), Offset(pr.x + 6f, topY + pr.h * 0.42f), Size(pr.w - 12f, 5.5f), style = Stroke(width = 1.3f))
            val flashLeft = (now / 260L) % 2L == 0L
            drawCircle(
                Color(if (flashLeft) 0xFFEF4444 else 0xFF3B82F6).copy(alpha = 0.24f * alpha),
                radius = 22f,
                center = Offset(pr.x + if (flashLeft) 8f else pr.w - 8f, topY + pr.h * 0.44f)
            )
        } else if (pr.vehicleKind == VehicleKind.AMBULANCE) {
            drawRect(Color(0xFFDC2626).copy(alpha = alpha), Offset(pr.x + pr.w * 0.5f - 2f, topY + pr.h * 0.36f), Size(4f, 14f))
            drawRect(Color(0xFFDC2626).copy(alpha = alpha), Offset(pr.x + pr.w * 0.5f - 7f, topY + pr.h * 0.36f + 5f), Size(14f, 4f))
        }

        if (pr.alarmActiveUntil > now && ((now / 180L) % 2L == 0L)) {
            drawCircle(Color(0xFFFDE047).copy(alpha = 0.72f * alpha), radius = 30f, center = Offset(pr.x + pr.w / 2f, topY))
        }
    }

    fun DrawScope.drawPixelDoor(
        d: DoorObj,
        alpha: Float,
        jammed: Boolean,
        oblY: Float = -WorldCanvasRenderer.OBLIQUE_Y
    ) {
        val dh = oblY * 0.88f
        val dirX = cos(d.baseAngle)
        val dirY = sin(d.baseAngle)

        when (d.doorStyle) {
            DoorStyle.DOUBLE_SLIDING_GLASS -> {
                val endX = d.hingeX + dirX * d.length
                val endY = d.hingeY + dirY * d.length
                drawLine(OUTLINE.copy(alpha = alpha), Offset(d.hingeX, d.hingeY - dh - 4f), Offset(endX, endY - dh - 4f), strokeWidth = 6f)
                drawLine(Color(0xFF28303B).copy(alpha = alpha), Offset(d.hingeX, d.hingeY - dh - 4f), Offset(endX, endY - dh - 4f), strokeWidth = 3.5f)
                if (d.autoSensor) {
                    val sensorCol = if (d.open) Color(0xFF2ED573) else Color(0xFFF59E0B)
                    drawCircle(sensorCol.copy(alpha = alpha), radius = 2.6f, center = Offset((d.hingeX + endX) * 0.5f, (d.hingeY + endY) * 0.5f - dh - 4f))
                }
                val shift = d.slideProgress.coerceIn(0f, 1f) * d.length * 0.88f
                val sX = d.hingeX - dirX * shift
                val sY = d.hingeY - dirY * shift
                val tX = endX - dirX * shift
                val tY = endY - dirY * shift
                val glassPath = Path().apply {
                    moveTo(sX, sY); lineTo(tX, tY); lineTo(tX, tY - dh); lineTo(sX, sY - dh); close()
                }
                drawPath(glassPath, color = Color(0xFF4DD0E1).copy(alpha = 0.34f * alpha))
                drawPath(glassPath, color = OUTLINE.copy(alpha = alpha), style = Stroke(width = 3.0f))
                drawPath(glassPath, color = Color(0xFFCBD5E1).copy(alpha = 0.85f * alpha), style = Stroke(width = 1.4f))
                drawLine(Color.White.copy(alpha = 0.58f * alpha), Offset(sX + dirX * 4f, sY - dh + 4f), Offset(tX - dirX * 4f, tY - 6f), strokeWidth = 1.6f)
            }
            DoorStyle.GARAGE_ROLLUP -> {
                val endX = d.hingeX + dirX * d.length
                val endY = d.hingeY + dirY * d.length
                drawLine(OUTLINE.copy(alpha = alpha), Offset(d.hingeX, d.hingeY), Offset(d.hingeX, d.hingeY - dh), strokeWidth = 4.6f)
                drawLine(OUTLINE.copy(alpha = alpha), Offset(endX, endY), Offset(endX, endY - dh), strokeWidth = 4.6f)
                val remain = (1f - d.slideProgress.coerceIn(0f, 1f))
                if (remain > 0.06f) {
                    val curtainBottomYOffset = dh * (1f - remain)
                    val curtainPath = Path().apply {
                        moveTo(d.hingeX, d.hingeY - curtainBottomYOffset)
                        lineTo(endX, endY - curtainBottomYOffset)
                        lineTo(endX, endY - dh)
                        lineTo(d.hingeX, d.hingeY - dh)
                        close()
                    }
                    drawPath(curtainPath, color = Color(0xFF5A6270).copy(alpha = alpha))
                    drawPath(curtainPath, color = OUTLINE.copy(alpha = alpha), style = Stroke(width = 2.4f))
                    for (rib in 1..3) {
                        val frac = rib * 0.25f * remain
                        val ryOff = dh * (1f - frac)
                        drawLine(OUTLINE.copy(alpha = 0.72f * alpha), Offset(d.hingeX, d.hingeY - ryOff), Offset(endX, endY - ryOff), strokeWidth = 1.8f)
                    }
                    val hX = (d.hingeX + endX) * 0.5f
                    val hY = (d.hingeY + endY) * 0.5f - curtainBottomYOffset - 4f
                    drawRect(Color(0xFFE2E8F0).copy(alpha = alpha), Offset(hX - 4f, hY - 1.5f), Size(8f, 3f))
                }
            }
            DoorStyle.STEEL_SECURITY -> {
                val tipX = d.hingeX + cos(d.angle) * d.length
                val tipY = d.hingeY + sin(d.angle) * d.length
                val path = Path().apply {
                    moveTo(d.hingeX, d.hingeY); lineTo(tipX, tipY); lineTo(tipX, tipY - dh); lineTo(d.hingeX, d.hingeY - dh); close()
                }
                drawPath(path, color = (if (jammed) Color(0xFF7F1D1D) else Color(0xFF334155)).copy(alpha = alpha))
                drawPath(path, color = OUTLINE.copy(alpha = alpha), style = Stroke(width = 2.4f))
                val p1X = d.hingeX + cos(d.angle) * (d.length * 0.18f)
                val p1Y = d.hingeY + sin(d.angle) * (d.length * 0.18f) - dh * 0.45f
                val p2X = d.hingeX + cos(d.angle) * (d.length * 0.82f)
                val p2Y = d.hingeY + sin(d.angle) * (d.length * 0.82f) - dh * 0.45f
                drawLine(Color(0xFFE2E8F0).copy(alpha = alpha), Offset(p1X, p1Y), Offset(p2X, p2Y), strokeWidth = 3f)
            }
            DoorStyle.HINGED_WOOD -> {
                val tipX = d.hingeX + cos(d.angle) * d.length
                val tipY = d.hingeY + sin(d.angle) * d.length
                val path = Path().apply {
                    moveTo(d.hingeX, d.hingeY); lineTo(tipX, tipY); lineTo(tipX, tipY - dh); lineTo(d.hingeX, d.hingeY - dh); close()
                }
                drawPath(path, color = (if (jammed) Color(0xFF7F1D1D) else Color(0xFF7D4825)).copy(alpha = alpha))
                drawPath(path, color = OUTLINE.copy(alpha = alpha), style = Stroke(width = 2.4f))
                drawLine(Color(0xFFB47444).copy(alpha = alpha), Offset(d.hingeX, d.hingeY - dh), Offset(tipX, tipY - dh), strokeWidth = 3f)
                val handleX = d.hingeX + cos(d.angle) * (d.length * 0.78f)
                val handleY = d.hingeY + sin(d.angle) * (d.length * 0.78f) - dh * 0.5f
                drawCircle(Color(0xFFFDE047).copy(alpha = 0.94f * alpha), radius = 2.8f, center = Offset(handleX, handleY))
                drawCircle(OUTLINE.copy(alpha = alpha), radius = 2.8f, center = Offset(handleX, handleY), style = Stroke(width = 1f))
            }
        }
    }
}
