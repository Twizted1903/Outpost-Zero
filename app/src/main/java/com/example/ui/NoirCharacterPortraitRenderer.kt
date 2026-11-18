package com.example.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import com.example.game.model.DogBreedMatch
import com.example.game.model.GlassesStyle
import com.example.game.model.GlovesStyle
import com.example.game.model.HatStyle
import com.example.game.model.OperatorArchetype
import com.example.game.model.OperatorSex
import com.example.game.model.PantsStyle
import com.example.game.model.ShirtStyle
import com.example.game.model.ShoeStyle
import com.example.game.model.SkinToneOption
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanFloor
import com.example.ui.theme.EmeraldHud
import kotlin.math.min

/**
 * High-Realism Inked Graphic-Novel renderer for Character Creation portraits
 * and all 8 equipment/appearance pickers, calibrated to match the splash screen's
 * gritty atmospheric lighting, volumetric anatomical shading, and coastal mist tones.
 */
object NoirCharacterPortraitRenderer {

    private val INK = Color(0xFF0A0D12)
    private val INK_CHARCOAL = Color(0xFF141A21)
    private val MIST_TEAL = Color(0xFF28474E)
    private val MIST_SLATE = Color(0xFF121E24)
    private val WARM_LAMP = Color(0xFFF5B749)
    private val COOL_RIM = Color(0xFF5FA3AD)
    private val PAPER_HIGHLIGHT = Color(0xFFEAF3FA)

    private fun shade(color: Color, amount: Float): Color = lerp(color, Color(0xFF070A0E), amount.coerceIn(0f, 1f))
    private fun tint(color: Color, amount: Float): Color = lerp(color, Color(0xFFF6E9D2), amount.coerceIn(0f, 1f))

    private fun DrawScope.drawNoirBackdrop(accent: Color, drawHorizon: Boolean = false) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.verticalGradient(listOf(MIST_TEAL.copy(alpha = 0.72f), MIST_SLATE, Color(0xFF070B0E))),
            size = size
        )
        // Diagonal warm amber watchtower backlight + misty cyan horizon haze
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(WARM_LAMP.copy(alpha = 0.22f), accent.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(w * 0.72f, h * 0.28f),
                radius = maxOf(w, h) * 0.75f
            ),
            radius = maxOf(w, h) * 0.75f,
            center = Offset(w * 0.72f, h * 0.28f)
        )
        if (drawHorizon) {
            val hy = h * 0.87f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF111B20), Color(0xFF070A0E)),
                    startY = hy,
                    endY = h
                ),
                topLeft = Offset(0f, hy),
                size = Size(w, h - hy)
            )
            drawLine(WARM_LAMP.copy(alpha = 0.28f), Offset(0f, hy), Offset(w, hy), strokeWidth = 1.1f)
        }
        drawNoirHatch(0f, 0f, w * 0.24f, h * 0.22f, step = 5.5f, color = Color.Black.copy(alpha = 0.30f))
        drawNoirHatch(w * 0.76f, h * 0.78f, w * 0.24f, h * 0.22f, step = 5.5f, color = Color.Black.copy(alpha = 0.30f))
    }

    private fun DrawScope.drawNoirHatch(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        step: Float = 5.0f,
        color: Color = INK.copy(alpha = 0.28f),
        strokeWidth: Float = 0.9f
    ) {
        if (w <= 3f || h <= 3f) return
        var d = step
        while (d < w + h - 2f) {
            val sx = x + d.coerceAtMost(w)
            val sy = y + (d - w).coerceAtLeast(0f)
            val ex = x + (d - h).coerceAtLeast(0f)
            val ey = y + d.coerceAtMost(h)
            if (sx > ex && ey > sy) {
                drawLine(color, Offset(sx, sy), Offset(ex, ey), strokeWidth = strokeWidth)
            }
            d += step
        }
    }

    // =========================================================================
    // REALISTIC GRAPHIC-NOVEL SURVIVOR + PAIRED K-9 STUDIO PORTRAIT
    // =========================================================================

    fun DrawScope.drawNoirOperatorPortrait(
        sex: OperatorSex,
        skinTone: SkinToneOption,
        archetype: OperatorArchetype,
        shirtStyle: ShirtStyle,
        pantsStyle: PantsStyle,
        shoeStyle: ShoeStyle,
        hatStyle: HatStyle,
        glassesStyle: GlassesStyle,
        glovesStyle: GlovesStyle,
        dogMatch: DogBreedMatch,
        drawOpaqueBackdrop: Boolean = true
    ) {
        val w = size.width
        val h = size.height
        val skin = Color(skinTone.colorHex)
        val skinShadow = shade(skin, 0.44f)
        val skinMid = shade(skin, 0.20f)
        val skinHighlight = tint(skin, 0.26f)

        val shirtCol = Color(shirtStyle.primaryHex)
        val shirtDark = shade(shirtCol, 0.50f)
        val shirtLight = tint(shirtCol, 0.20f)
        val shirtPat = Color(shirtStyle.patternHex)

        val pantsCol = Color(pantsStyle.colorHex)
        val pantsDark = shade(pantsCol, 0.52f)
        val pantsLight = tint(pantsCol, 0.18f)
        val pantsTrim = Color(pantsStyle.trimHex)

        val vestCol = Color(archetype.defaultVestHex)
        val vestDark = shade(vestCol, 0.52f)
        val vestLight = tint(vestCol, 0.16f)
        val packCol = Color(archetype.defaultPackHex)

        if (drawOpaqueBackdrop) {
            drawNoirBackdrop(accent = AmberSignal, drawHorizon = true)
        }

        // Proportioned anatomical staging on the wet lit courtyard
        val groundY = h * 0.90f
        val scale = min(w / 240f, h / 215f).coerceIn(0.68f, 1.95f)
        val cx = w * 0.38f
        val inkW = (1.35f * scale).coerceAtLeast(1.1f)
        val fineInk = (0.95f * scale).coerceAtLeast(0.8f)

        // Realistic cast ground contact shadows & warm wet amber reflection
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.72f), Color.Black.copy(alpha = 0.34f), Color.Transparent),
                center = Offset(cx + 8f * scale, groundY),
                radius = 58f * scale
            ),
            topLeft = Offset(cx - 48f * scale, groundY - 9f * scale),
            size = Size(116f * scale, 22f * scale)
        )
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.72f), Color.Black.copy(alpha = 0.34f), Color.Transparent),
                center = Offset(w * 0.69f, groundY + 1f * scale),
                radius = 44f * scale
            ),
            topLeft = Offset(w * 0.69f - 40f * scale, groundY - 7f * scale),
            size = Size(84f * scale, 18f * scale)
        )

        // 1. Tactical Assault Rucksack behind shoulder (volumetric straps + sleep roll)
        val packPath = Path().apply {
            moveTo(cx - 29f * scale, h * 0.28f)
            quadraticTo(cx - 34f * scale, h * 0.41f, cx - 30f * scale, h * 0.56f)
            lineTo(cx - 10f * scale, h * 0.57f)
            lineTo(cx - 10f * scale, h * 0.27f)
            close()
        }
        drawPath(
            packPath,
            Brush.horizontalGradient(
                colors = listOf(shade(packCol, 0.56f), shade(packCol, 0.16f)),
                startX = cx - 34f * scale,
                endX = cx - 10f * scale
            )
        )
        drawLine(tint(packCol, 0.25f), Offset(cx - 28f * scale, h * 0.34f), Offset(cx - 14f * scale, h * 0.34f), strokeWidth = 1.6f * scale)
        drawLine(INK, Offset(cx - 28f * scale, h * 0.44f), Offset(cx - 14f * scale, h * 0.44f), strokeWidth = fineInk)
        drawPath(packPath, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        // 2. Anatomical Legs & Detailed Trousers (realistic stance, quads/knees/shins, fabric creases)
        val hipY = h * 0.535f
        val kneeY = h * 0.71f
        val ankleY = groundY - 14f * scale
        val isFemale = sex == OperatorSex.FEMALE
        val hipHalfW = (if (isFemale) 15.8f else 17.6f) * scale

        // Left / back leg (planted slightly back in shadow)
        val leftLeg = Path().apply {
            moveTo(cx - hipHalfW, hipY)
            lineTo(cx - 1.5f * scale, hipY + 3f * scale)
            quadraticTo(cx - 2f * scale, (hipY + kneeY) * 0.5f, cx - 4.5f * scale, kneeY)
            lineTo(cx - 6.5f * scale, ankleY)
            lineTo(cx - 20f * scale, ankleY)
            quadraticTo(cx - 20f * scale, (kneeY + ankleY) * 0.5f, cx - 19f * scale, kneeY)
            quadraticTo(cx - 20f * scale, (hipY + kneeY) * 0.5f, cx - hipHalfW, hipY)
            close()
        }
        drawPath(
            leftLeg,
            Brush.horizontalGradient(
                colors = listOf(pantsDark, shade(pantsCol, 0.28f), pantsDark),
                startX = cx - 21f * scale,
                endX = cx - 2f * scale
            )
        )
        // Fabric tension folds behind left knee
        drawLine(INK.copy(alpha = 0.5f), Offset(cx - 17f * scale, kneeY - 2f * scale), Offset(cx - 6f * scale, kneeY + 1f * scale), strokeWidth = fineInk)
        drawLine(INK.copy(alpha = 0.4f), Offset(cx - 16f * scale, kneeY + 4f * scale), Offset(cx - 7f * scale, kneeY + 2f * scale), strokeWidth = fineInk)
        drawPath(leftLeg, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        // Right / lead leg (lit from upper-right watchtower)
        val rightLeg = Path().apply {
            moveTo(cx - 2f * scale, hipY + 1f * scale)
            lineTo(cx + hipHalfW, hipY)
            quadraticTo(cx + hipHalfW + 3f * scale, (hipY + kneeY) * 0.5f, cx + 19.5f * scale, kneeY)
            quadraticTo(cx + 20f * scale, (kneeY + ankleY) * 0.5f, cx + 17.5f * scale, ankleY)
            lineTo(cx + 5f * scale, ankleY)
            quadraticTo(cx + 3f * scale, (kneeY + ankleY) * 0.5f, cx + 3.5f * scale, kneeY)
            close()
        }
        drawPath(
            rightLeg,
            Brush.horizontalGradient(
                colors = listOf(pantsDark, pantsCol, pantsLight),
                startX = cx + 2f * scale,
                endX = cx + 21f * scale
            )
        )
        // Knee crease, seam line & cargo pockets
        drawLine(INK.copy(alpha = 0.55f), Offset(cx + 6f * scale, kneeY - 1f * scale), Offset(cx + 17f * scale, kneeY - 3f * scale), strokeWidth = fineInk)
        drawLine(INK.copy(alpha = 0.35f), Offset(cx + 7f * scale, kneeY + 5f * scale), Offset(cx + 15f * scale, kneeY + 3f * scale), strokeWidth = fineInk)
        if (pantsStyle == PantsStyle.CHARCOAL_CARGO || pantsStyle == PantsStyle.WORK_CANVAS || pantsStyle == PantsStyle.OLIVE_FATIGUES) {
            val pkt = Path().apply {
                moveTo(cx + 8f * scale, kneeY - 18f * scale)
                lineTo(cx + 18f * scale, kneeY - 17f * scale)
                lineTo(cx + 18f * scale, kneeY - 4f * scale)
                lineTo(cx + 8f * scale, kneeY - 5f * scale)
                close()
            }
            drawPath(pkt, shade(pantsCol, 0.14f))
            drawPath(pkt, INK.copy(alpha = 0.72f), style = Stroke(fineInk))
            drawLine(pantsTrim.copy(alpha = 0.75f), Offset(cx + 8f * scale, kneeY - 13f * scale), Offset(cx + 18f * scale, kneeY - 12f * scale), strokeWidth = fineInk)
        } else {
            // Denim outseam + subtle knee whiskering
            drawLine(pantsTrim.copy(alpha = 0.70f), Offset(cx + 16.5f * scale, hipY + 5f * scale), Offset(cx + 16.5f * scale, ankleY - 2f * scale), strokeWidth = 1.0f * scale)
        }
        drawPath(rightLeg, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        // 3. Sculpted Lace-Up Boots / Trail Shoes
        drawRealisticBoot(cx - 22f * scale, ankleY, 18f * scale, 14f * scale, shoeStyle, isShadowSide = true, inkW = inkW)
        drawRealisticBoot(cx + 3.5f * scale, ankleY, 21f * scale, 15f * scale, shoeStyle, isShadowSide = false, inkW = inkW)

        // Heavy tactical duty belt + holster drop strap + steel buckle
        drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF292524), Color(0xFF0C0A09))),
            topLeft = Offset(cx - hipHalfW, hipY - 4f * scale),
            size = Size(hipHalfW * 2f, 6.5f * scale)
        )
        drawRect(INK, Offset(cx - hipHalfW, hipY - 4f * scale), Size(hipHalfW * 2f, 6.5f * scale), style = Stroke(fineInk))
        drawRect(Color(0xFFD97706), Offset(cx - 3f * scale, hipY - 3.5f * scale), Size(7f * scale, 5.5f * scale))
        drawRect(INK, Offset(cx - 3f * scale, hipY - 3.5f * scale), Size(7f * scale, 5.5f * scale), style = Stroke(fineInk * 0.8f))

        // 4. Anatomical Shoulders, Torso & Layered Flannel/Jacket
        val shoulderY = h * 0.275f
        val shoulderHalfW = (if (isFemale) 21.5f else 25.5f) * scale
        val waistHalfW = (if (isFemale) 14f else 17.5f) * scale

        // Back (left) arm in shadow
        val backArm = Path().apply {
            moveTo(cx - shoulderHalfW + 1f * scale, shoulderY + 3f * scale)
            quadraticTo(cx - shoulderHalfW - 8f * scale, h * 0.38f, cx - shoulderHalfW - 5.5f * scale, h * 0.54f)
            lineTo(cx - shoulderHalfW + 4.5f * scale, h * 0.54f)
            quadraticTo(cx - shoulderHalfW + 7f * scale, h * 0.42f, cx - shoulderHalfW + 6f * scale, shoulderY + 12f * scale)
            close()
        }
        drawPath(backArm, Brush.horizontalGradient(listOf(shirtDark, shade(shirtCol, 0.30f)), cx - shoulderHalfW - 7f * scale, cx - shoulderHalfW + 6f * scale))
        drawPath(backArm, INK, style = Stroke(inkW, join = StrokeJoin.Round))
        drawRealisticHand(cx - shoulderHalfW - 0.5f * scale, h * 0.555f, 6.4f * scale, glovesStyle, skin, inkW)

        // Sculpted Torso Jacket / Flannel / Hoodie
        val torsoPath = Path().apply {
            moveTo(cx - shoulderHalfW, shoulderY + 3f * scale)
            lineTo(cx - 8f * scale, h * 0.238f)
            lineTo(cx + 9f * scale, h * 0.238f)
            lineTo(cx + shoulderHalfW, shoulderY + 3f * scale)
            quadraticTo(cx + waistHalfW + 4f * scale, (shoulderY + hipY) * 0.5f, cx + waistHalfW, hipY)
            lineTo(cx - waistHalfW, hipY)
            quadraticTo(cx - waistHalfW - 3f * scale, (shoulderY + hipY) * 0.5f, cx - shoulderHalfW, shoulderY + 3f * scale)
            close()
        }
        drawPath(
            torsoPath,
            Brush.horizontalGradient(
                colors = listOf(shirtDark, shirtCol, shirtLight),
                startX = cx - shoulderHalfW,
                endX = cx + shoulderHalfW
            )
        )
        if (shirtStyle.isPlaid) {
            for (py in 0..4) {
                val yLine = shoulderY + (5f + py * 9.5f) * scale
                drawLine(shirtPat.copy(alpha = 0.55f), Offset(cx - waistHalfW, yLine), Offset(cx + waistHalfW, yLine), strokeWidth = 2.0f * scale)
                drawLine(INK.copy(alpha = 0.18f), Offset(cx - waistHalfW, yLine + 1.5f * scale), Offset(cx + waistHalfW, yLine + 1.5f * scale), strokeWidth = 0.8f * scale)
            }
            for (px in -2..2) {
                val xLine = cx + px * 7.5f * scale
                drawLine(shirtPat.copy(alpha = 0.50f), Offset(xLine, h * 0.245f), Offset(xLine, hipY - 2f * scale), strokeWidth = 1.9f * scale)
            }
        }
        // Drapery folds near waist & armpits
        drawLine(INK.copy(alpha = 0.42f), Offset(cx - waistHalfW * 0.8f, hipY - 12f * scale), Offset(cx - 4f * scale, hipY - 7f * scale), strokeWidth = fineInk)
        drawLine(INK.copy(alpha = 0.42f), Offset(cx + waistHalfW * 0.78f, hipY - 14f * scale), Offset(cx + 3f * scale, hipY - 9f * scale), strokeWidth = fineInk)

        if (shirtStyle.hasHood) {
            drawArc(
                color = shade(shirtPat, 0.15f),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(cx - 12f * scale, h * 0.21f),
                size = Size(24f * scale, 14f * scale),
                style = Stroke(5.2f * scale, cap = StrokeCap.Round)
            )
            drawLine(BoneWhite.copy(alpha = 0.82f), Offset(cx - 3.5f * scale, h * 0.28f), Offset(cx - 4.2f * scale, h * 0.355f), strokeWidth = 1.2f * scale)
            drawLine(BoneWhite.copy(alpha = 0.82f), Offset(cx + 3.5f * scale, h * 0.28f), Offset(cx + 4.2f * scale, h * 0.355f), strokeWidth = 1.2f * scale)
        } else {
            val lCollar = Path().apply {
                moveTo(cx - 7.5f * scale, h * 0.242f)
                lineTo(cx - 1f * scale, h * 0.295f)
                lineTo(cx - 9f * scale, h * 0.305f)
                close()
            }
            val rCollar = Path().apply {
                moveTo(cx + 8.5f * scale, h * 0.242f)
                lineTo(cx + 2f * scale, h * 0.295f)
                lineTo(cx + 10.5f * scale, h * 0.305f)
                close()
            }
            drawPath(lCollar, shade(shirtPat, 0.20f))
            drawPath(rCollar, shirtPat)
            drawPath(lCollar, INK, style = Stroke(fineInk))
            drawPath(rCollar, INK, style = Stroke(fineInk))
        }
        drawPath(torsoPath, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        // 5. Realistic Tactical Plate Carrier with MOLLE Webbing, Mag Pouches & Archetype Gear
        val vestTop = h * 0.275f
        val vestBot = h * 0.495f
        val vestPath = Path().apply {
            moveTo(cx - 12.5f * scale, vestTop)
            lineTo(cx - 7f * scale, vestTop)
            lineTo(cx - 5.5f * scale, h * 0.312f)
            lineTo(cx + 7f * scale, h * 0.312f)
            lineTo(cx + 8.5f * scale, vestTop)
            lineTo(cx + 14f * scale, vestTop)
            lineTo(cx + 15.5f * scale, vestBot)
            lineTo(cx - 13.5f * scale, vestBot)
            close()
        }
        drawPath(
            vestPath,
            Brush.horizontalGradient(
                colors = listOf(vestDark, vestCol, vestLight),
                startX = cx - 14f * scale,
                endX = cx + 16f * scale
            )
        )
        // PALS MOLLE webbing strips + shoulder fastex buckles
        for (row in 0..2) {
            val my = h * 0.335f + row * 6.5f * scale
            drawLine(vestDark.copy(alpha = 0.85f), Offset(cx - 11.5f * scale, my), Offset(cx + 13f * scale, my), strokeWidth = 1.6f * scale)
        }
        drawRect(Color(0xFF1E252D), Offset(cx - 12f * scale, vestTop + 2f * scale), Size(5f * scale, 4.5f * scale))
        drawRect(Color(0xFF1E252D), Offset(cx + 9f * scale, vestTop + 2f * scale), Size(5f * scale, 4.5f * scale))
        for (m in 0..2) {
            val mx = cx + (-10.5f + m * 8.2f) * scale
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(vestLight, vestDark)),
                topLeft = Offset(mx, h * 0.415f),
                size = Size(6.8f * scale, 11.5f * scale),
                cornerRadius = CornerRadius(1.5f * scale, 1.5f * scale)
            )
            drawRoundRect(INK, Offset(mx, h * 0.415f), Size(6.8f * scale, 11.5f * scale), CornerRadius(1.5f * scale, 1.5f * scale), style = Stroke(fineInk))
            drawLine(WARM_LAMP.copy(alpha = 0.35f), Offset(mx + 1f * scale, h * 0.43f), Offset(mx + 5.8f * scale, h * 0.43f), strokeWidth = fineInk)
        }
        // Role-specific realistic tactical equipment
        when (archetype) {
            OperatorArchetype.RESPONDER -> {
                drawRoundRect(Color(0xFF991B1B), Offset(cx + 3f * scale, h * 0.33f), Size(9f * scale, 9f * scale), CornerRadius(1.5f * scale, 1.5f * scale))
                drawRect(BoneWhite, Offset(cx + 6.7f * scale, h * 0.343f), Size(1.6f * scale, 6.2f * scale))
                drawRect(BoneWhite, Offset(cx + 4.4f * scale, h * 0.366f), Size(6.2f * scale, 1.6f * scale))
                drawRoundRect(INK, Offset(cx + 3f * scale, h * 0.33f), Size(9f * scale, 9f * scale), CornerRadius(1.5f * scale, 1.5f * scale), style = Stroke(fineInk))
            }
            OperatorArchetype.MARSHAL -> {
                drawCircle(Brush.radialGradient(listOf(WARM_LAMP, Color(0xFF92400E))), radius = 4.5f * scale, center = Offset(cx + 8f * scale, h * 0.355f))
                drawCircle(INK, radius = 4.5f * scale, center = Offset(cx + 8f * scale, h * 0.355f), style = Stroke(fineInk))
            }
            OperatorArchetype.BREACHER -> {
                drawLine(Color(0xFF6B3E26), Offset(cx - 22f * scale, h * 0.20f), Offset(cx + 11f * scale, h * 0.43f), strokeWidth = 3.2f * scale, cap = StrokeCap.Round)
                drawRect(Color(0xFF94A3B8), Offset(cx - 25.5f * scale, h * 0.165f), Size(9.5f * scale, 6.5f * scale))
                drawRect(INK, Offset(cx - 25.5f * scale, h * 0.165f), Size(9.5f * scale, 6.5f * scale), style = Stroke(fineInk))
            }
            OperatorArchetype.RECON -> {
                drawLine(Color(0xFF1F2937), Offset(cx - 10f * scale, h * 0.305f), Offset(cx + 12f * scale, h * 0.455f), strokeWidth = 2.2f * scale)
                drawRect(Color(0xFF0F172A), Offset(cx + 5.5f * scale, h * 0.32f), Size(6f * scale, 8.5f * scale))
                drawLine(Color(0xFF475569), Offset(cx + 10f * scale, h * 0.32f), Offset(cx + 10f * scale, h * 0.255f), strokeWidth = 1.2f * scale)
                drawCircle(EmeraldHud, radius = 1.3f * scale, center = Offset(cx + 8f * scale, h * 0.34f))
            }
        }
        drawPath(vestPath, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        // Front (right) bent arm + realistic tactical light & hand
        val frontArm = Path().apply {
            moveTo(cx + shoulderHalfW - 3.5f * scale, shoulderY + 3f * scale)
            quadraticTo(cx + shoulderHalfW + 8f * scale, h * 0.39f, cx + shoulderHalfW + 3.5f * scale, h * 0.51f)
            lineTo(cx + shoulderHalfW - 5.5f * scale, h * 0.50f)
            quadraticTo(cx + shoulderHalfW - 2f * scale, h * 0.40f, cx + shoulderHalfW - 4f * scale, shoulderY + 13f * scale)
            close()
        }
        drawPath(frontArm, Brush.horizontalGradient(listOf(shirtDark, shirtCol, shirtLight), cx + shoulderHalfW - 6f * scale, cx + shoulderHalfW + 8f * scale))
        drawLine(INK.copy(alpha = 0.45f), Offset(cx + shoulderHalfW - 2f * scale, h * 0.40f), Offset(cx + shoulderHalfW + 5f * scale, h * 0.42f), strokeWidth = fineInk)
        drawPath(frontArm, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        val handX = cx + shoulderHalfW - 1f * scale
        val handY = h * 0.52f
        drawLine(Color(0xFF1C232B), Offset(handX - 3f * scale, handY + 1.5f * scale), Offset(handX + 9f * scale, handY - 3.2f * scale), strokeWidth = 4.2f * scale, cap = StrokeCap.Round)
        drawCircle(
            brush = Brush.radialGradient(listOf(WARM_LAMP, Color.Transparent)),
            radius = 7f * scale,
            center = Offset(handX + 9.5f * scale, handY - 3.5f * scale)
        )
        drawCircle(WARM_LAMP, radius = 2.0f * scale, center = Offset(handX + 9.2f * scale, handY - 3.4f * scale))
        drawRealisticHand(handX, handY, 6.4f * scale, glovesStyle, skin, inkW)

        // 6. Realistic Sculpted Neck, Jaw, Cheekbones, Almond Eyes, Nose, Lips & Hair
        val headCx = cx + 1.5f * scale
        val headCy = h * 0.155f
        val headW = (if (isFemale) 11.4f else 12.6f) * scale
        val headH = 15.2f * scale

        // Neck with sternocleidomastoid chiaroscuro shadow
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(skinShadow, skinMid, skin),
                startX = headCx - 6f * scale,
                endX = headCx + 6f * scale
            ),
            topLeft = Offset(headCx - 5.2f * scale, headCy + headH * 0.52f),
            size = Size(10.8f * scale, 14f * scale)
        )
        drawLine(INK.copy(alpha = 0.45f), Offset(headCx - 2f * scale, headCy + headH * 0.72f), Offset(headCx + 2.5f * scale, headCy + headH + 4f * scale), strokeWidth = fineInk)

        // Back hair volume for Female before face contour
        val hairBase = Color(0xFF221813)
        val hairHighlight = Color(0xFF47352A)
        if (isFemale && hatStyle != HatStyle.KNIT_BEANIE) {
            val backHair = Path().apply {
                moveTo(headCx - headW - 2.5f * scale, headCy - 5f * scale)
                quadraticTo(headCx - headW - 7f * scale, headCy + headH * 0.6f, headCx - headW - 4.5f * scale, headCy + headH + 6f * scale)
                lineTo(headCx + headW + 2.5f * scale, headCy + headH + 4f * scale)
                quadraticTo(headCx + headW + 4f * scale, headCy + headH * 0.4f, headCx + headW + 1f * scale, headCy - 4f * scale)
                close()
            }
            drawPath(backHair, Brush.horizontalGradient(listOf(INK, hairBase, hairHighlight), headCx - headW - 6f * scale, headCx + headW + 3f * scale))
            drawPath(backHair, INK, style = Stroke(inkW, join = StrokeJoin.Round))
        }

        // Anatomical head cranium & jaw
        val facePath = Path().apply {
            moveTo(headCx - headW, headCy - 5f * scale)
            quadraticTo(headCx, headCy - headH - 3f * scale, headCx + headW, headCy - 4f * scale)
            lineTo(headCx + headW * 0.96f, headCy + headH * 0.36f)
            quadraticTo(headCx + headW * 0.78f, headCy + headH * 0.78f, headCx + headW * (if (isFemale) 0.24f else 0.40f), headCy + headH)
            lineTo(headCx - headW * (if (isFemale) 0.26f else 0.42f), headCy + headH * 0.95f)
            quadraticTo(headCx - headW * 0.85f, headCy + headH * 0.72f, headCx - headW * 0.98f, headCy + headH * 0.35f)
            close()
        }
        drawPath(
            facePath,
            Brush.linearGradient(
                colors = listOf(skinShadow, skin, skinHighlight),
                start = Offset(headCx - headW, headCy + headH * 0.3f),
                end = Offset(headCx + headW, headCy - headH * 0.4f)
            )
        )
        // Left-side cheekbone hollow + under-chin chiaroscuro
        drawOval(
            color = skinShadow.copy(alpha = 0.45f),
            topLeft = Offset(headCx - headW * 0.85f, headCy + 1.5f * scale),
            size = Size(headW * 0.65f, headH * 0.55f)
        )
        // Ear contour
        drawOval(skinMid, Offset(headCx - headW - 1.8f * scale, headCy - 1.5f * scale), Size(3.6f * scale, 6.4f * scale))
        drawOval(INK, Offset(headCx - headW - 1.8f * scale, headCy - 1.5f * scale), Size(3.6f * scale, 6.4f * scale), style = Stroke(fineInk))
        drawPath(facePath, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        // Realistic Almond Eyes + Iris + Eyelids + Nose Bridge + Shaded Lips (NO cartoon circles!)
        val eyeY = headCy + 0.6f * scale
        drawRealisticFaceFeatures(
            cx = headCx + 1.0f * scale,
            eyeY = eyeY,
            scale = scale,
            skinShadow = skinShadow,
            skinHighlight = skinHighlight,
            isFemale = isFemale,
            fineInk = fineInk
        )

        // Sculpted hair locks with realistic specular sheen
        val hairPath = Path().apply {
            moveTo(headCx - headW * 1.03f, headCy - 2.5f * scale)
            quadraticTo(headCx - headW * 0.65f, headCy - headH - 4f * scale, headCx + headW * 0.25f, headCy - headH - 3f * scale)
            quadraticTo(headCx + headW * 1.06f, headCy - headH * 0.68f, headCx + headW * 0.99f, headCy - 3.5f * scale)
            quadraticTo(headCx + headW * 0.48f, headCy - 8f * scale, headCx - headW * 0.1f, headCy - 6.5f * scale)
            lineTo(headCx - headW * 0.55f, headCy - 5.5f * scale)
            close()
        }
        drawPath(hairPath, Brush.horizontalGradient(listOf(INK, hairBase, hairHighlight), headCx - headW, headCx + headW))
        drawLine(hairHighlight.copy(alpha = 0.7f), Offset(headCx - 2f * scale, headCy - headH + 1f * scale), Offset(headCx + headW * 0.7f, headCy - 8f * scale), strokeWidth = 1.2f * scale)
        drawPath(hairPath, INK, style = Stroke(fineInk))

        // Eyewear & Hat
        drawPortraitGlasses(headCx + 1.0f * scale, eyeY, headW * 1.55f, glassesStyle, scale)
        drawPortraitHat(headCx, headCy - 4.5f * scale, headW, hatStyle, scale, inkW)

        // 7. Realistic Seated Guard K-9 Companion on right side of courtyard
        drawNoirDogProfile(
            cx = w * 0.70f,
            groundY = groundY,
            scale = scale * 1.02f,
            coatColor = Color(dogMatch.coatColorHex),
            breedName = dogMatch.breed,
            inkW = inkW
        )
    }

    private fun DrawScope.drawRealisticFaceFeatures(
        cx: Float,
        eyeY: Float,
        scale: Float,
        skinShadow: Color,
        skinHighlight: Color,
        isFemale: Boolean,
        fineInk: Float
    ) {
        // Deep brow-socket shadow
        drawOval(
            color = skinShadow.copy(alpha = 0.42f),
            topLeft = Offset(cx - 7.8f * scale, eyeY - 3.6f * scale),
            size = Size(6.5f * scale, 4.6f * scale)
        )
        drawOval(
            color = skinShadow.copy(alpha = 0.42f),
            topLeft = Offset(cx + 1.5f * scale, eyeY - 3.6f * scale),
            size = Size(6.5f * scale, 4.6f * scale)
        )

        // Sculpted eyebrows
        drawLine(INK, Offset(cx - 7.2f * scale, eyeY - 3.2f * scale), Offset(cx - 1.5f * scale, eyeY - 2.3f * scale), strokeWidth = (if (isFemale) 1.3f else 1.7f) * scale, cap = StrokeCap.Round)
        drawLine(INK, Offset(cx + 1.8f * scale, eyeY - 2.3f * scale), Offset(cx + 7.5f * scale, eyeY - 3.2f * scale), strokeWidth = (if (isFemale) 1.3f else 1.7f) * scale, cap = StrokeCap.Round)

        // Almond Eyes (off-white shaded sclera + colored iris + pupil + specular catchlight, NEVER flat round dots!)
        val eyeCenters = listOf(cx - 4.1f * scale, cx + 4.3f * scale)
        for (ex in eyeCenters) {
            val sclera = Path().apply {
                moveTo(ex - 2.4f * scale, eyeY)
                quadraticTo(ex, eyeY - 1.7f * scale, ex + 2.4f * scale, eyeY)
                quadraticTo(ex, eyeY + 1.3f * scale, ex - 2.4f * scale, eyeY)
                close()
            }
            drawPath(sclera, Color(0xFFD8DADF))
            drawCircle(Color(0xFF425849), radius = 1.05f * scale, center = Offset(ex + 0.1f * scale, eyeY))
            drawCircle(INK, radius = 0.55f * scale, center = Offset(ex + 0.1f * scale, eyeY))
            drawCircle(Color.White.copy(alpha = 0.90f), radius = 0.35f * scale, center = Offset(ex + 0.55f * scale, eyeY - 0.35f * scale))
            // Crisp upper lash line
            drawLine(INK, Offset(ex - 2.5f * scale, eyeY + 0.1f * scale), Offset(ex + 2.5f * scale, eyeY - 0.1f * scale), strokeWidth = 1.05f * scale, cap = StrokeCap.Round)
        }

        // Sculpted Nose Bridge + Lit Tip + Nostril Shadow
        drawLine(skinHighlight.copy(alpha = 0.72f), Offset(cx + 0.4f * scale, eyeY + 0.4f * scale), Offset(cx + 1.0f * scale, eyeY + 4.5f * scale), strokeWidth = 1.1f * scale, cap = StrokeCap.Round)
        drawLine(INK.copy(alpha = 0.78f), Offset(cx - 0.5f * scale, eyeY + 5.1f * scale), Offset(cx + 1.5f * scale, eyeY + 5.1f * scale), strokeWidth = fineInk, cap = StrokeCap.Round)

        // Realistic Shaded Lips & Jaw Line
        val lipUpper = Path().apply {
            moveTo(cx - 3.0f * scale, eyeY + 8.3f * scale)
            quadraticTo(cx, eyeY + 7.4f * scale, cx + 3.2f * scale, eyeY + 8.3f * scale)
            lineTo(cx, eyeY + 8.9f * scale)
            close()
        }
        drawPath(lipUpper, shade(skinShadow, 0.28f))
        drawLine(INK.copy(alpha = 0.72f), Offset(cx - 2.8f * scale, eyeY + 8.5f * scale), Offset(cx + 3.0f * scale, eyeY + 8.4f * scale), strokeWidth = fineInk * 0.85f)
        drawLine(skinHighlight.copy(alpha = 0.50f), Offset(cx - 1.5f * scale, eyeY + 9.5f * scale), Offset(cx + 1.8f * scale, eyeY + 9.5f * scale), strokeWidth = fineInk * 0.9f)
        if (!isFemale) {
            // Subtle jaw stubble texture
            drawLine(skinShadow.copy(alpha = 0.35f), Offset(cx - 5f * scale, eyeY + 10.5f * scale), Offset(cx + 5f * scale, eyeY + 10.5f * scale), strokeWidth = 1.8f * scale)
        }
    }

    private fun DrawScope.drawRealisticBoot(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        shoe: ShoeStyle,
        isShadowSide: Boolean,
        inkW: Float
    ) {
        val base = if (isShadowSide) shade(Color(shoe.colorHex), 0.38f) else Color(shoe.colorHex)
        val light = tint(base, 0.25f)
        val dark = shade(base, 0.45f)
        val sole = if (isShadowSide) shade(Color(shoe.soleHex), 0.35f) else Color(shoe.soleHex)
        val path = Path().apply {
            moveTo(x + w * 0.12f, y)
            lineTo(x + w * 0.56f, y)
            lineTo(x + w * 0.66f, y + h * 0.44f)
            quadraticTo(x + w * 0.95f, y + h * 0.46f, x + w, y + h * 0.64f)
            lineTo(x + w, y + h * 0.82f)
            lineTo(x, y + h * 0.82f)
            close()
        }
        drawPath(path, Brush.horizontalGradient(listOf(dark, base, light), x, x + w))
        // Toe leather sheen & lace crossings
        drawLine(light.copy(alpha = 0.7f), Offset(x + w * 0.72f, y + h * 0.56f), Offset(x + w * 0.92f, y + h * 0.62f), strokeWidth = inkW * 0.85f, cap = StrokeCap.Round)
        for (l in 0..2) {
            val ly = y + (2.5f + l * 2.5f) * (h / 14f)
            drawLine(Color(0xFFD6D3D1).copy(alpha = 0.75f), Offset(x + w * 0.46f, ly), Offset(x + w * 0.62f, ly - 0.8f), strokeWidth = inkW * 0.55f)
        }
        drawPath(path, INK, style = Stroke(inkW, join = StrokeJoin.Round))
        drawRect(sole, Offset(x, y + h * 0.76f), Size(w, h * 0.24f))
        drawRect(INK, Offset(x, y + h * 0.76f), Size(w, h * 0.24f), style = Stroke(inkW * 0.8f))
    }

    private fun DrawScope.drawRealisticHand(
        cx: Float,
        cy: Float,
        r: Float,
        gloves: GlovesStyle,
        skin: Color,
        inkW: Float
    ) {
        val base = if (gloves == GlovesStyle.NONE) skin else Color(gloves.colorHex)
        val tip = if (gloves.bareTips) skin else base
        val dark = shade(base, 0.42f)
        drawCircle(Brush.radialGradient(listOf(tint(base, 0.20f), dark)), radius = r, center = Offset(cx, cy))
        // Finger knuckle crease & curled fingers
        for (f in -1..1) {
            drawOval(tip, Offset(cx + f * r * 0.38f - r * 0.18f, cy + r * 0.22f), Size(r * 0.36f, r * 0.52f))
        }
        drawCircle(INK, radius = r, center = Offset(cx, cy), style = Stroke(inkW * 0.85f))
    }

    private fun DrawScope.drawPortraitGlasses(
        cx: Float,
        eyeY: Float,
        spanW: Float,
        glasses: GlassesStyle,
        scale: Float
    ) {
        if (glasses == GlassesStyle.NONE) return
        val frame = Color(glasses.frameHex)
        val lens = Color(glasses.lensHex)
        val lensW = spanW * 0.39f
        val lensH = 5.0f * scale
        val gap = 2.0f * scale
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(lens.copy(alpha = 0.82f), shade(lens, 0.35f).copy(alpha = 0.90f))),
            topLeft = Offset(cx - lensW - gap * 0.5f, eyeY - lensH * 0.5f),
            size = Size(lensW, lensH),
            cornerRadius = CornerRadius(2.2f * scale, 2.2f * scale)
        )
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(lens.copy(alpha = 0.82f), shade(lens, 0.35f).copy(alpha = 0.90f))),
            topLeft = Offset(cx + gap * 0.5f, eyeY - lensH * 0.5f),
            size = Size(lensW, lensH),
            cornerRadius = CornerRadius(2.2f * scale, 2.2f * scale)
        )
        drawLine(WARM_LAMP.copy(alpha = 0.50f), Offset(cx + gap + 1f * scale, eyeY - lensH * 0.32f), Offset(cx + gap + lensW * 0.42f, eyeY + lensH * 0.32f), strokeWidth = 1.0f * scale)
        val fStroke = (if (glasses == GlassesStyle.WAYFARERS) 1.55f else 1.05f) * scale
        drawRoundRect(frame, Offset(cx - lensW - gap * 0.5f, eyeY - lensH * 0.5f), Size(lensW, lensH), CornerRadius(2.2f * scale, 2.2f * scale), style = Stroke(fStroke))
        drawRoundRect(frame, Offset(cx + gap * 0.5f, eyeY - lensH * 0.5f), Size(lensW, lensH), CornerRadius(2.2f * scale, 2.2f * scale), style = Stroke(fStroke))
        drawLine(frame, Offset(cx - gap * 0.5f, eyeY - 0.8f * scale), Offset(cx + gap * 0.5f, eyeY - 0.8f * scale), strokeWidth = fStroke)
    }

    private fun DrawScope.drawPortraitHat(
        headCx: Float,
        browY: Float,
        headW: Float,
        hat: HatStyle,
        scale: Float,
        inkW: Float
    ) {
        if (hat == HatStyle.NONE) return
        val main = Color(hat.colorHex)
        val dark = shade(main, 0.46f)
        val light = tint(main, 0.22f)
        val accent = Color(hat.accentHex)
        when (hat) {
            HatStyle.BALL_CAP -> {
                val crown = Path().apply {
                    moveTo(headCx - headW * 1.06f, browY)
                    quadraticTo(headCx, browY - 14.5f * scale, headCx + headW * 1.04f, browY)
                    close()
                }
                drawPath(crown, Brush.horizontalGradient(listOf(dark, main, light), headCx - headW, headCx + headW))
                drawLine(dark, Offset(headCx, browY - 11f * scale), Offset(headCx + 2f * scale, browY), strokeWidth = 1.0f * scale)
                drawPath(crown, INK, style = Stroke(inkW, join = StrokeJoin.Round))
                val brim = Path().apply {
                    moveTo(headCx - headW * 0.3f, browY - 1f * scale)
                    lineTo(headCx + headW * 1.56f, browY + 1.5f * scale)
                    quadraticTo(headCx + headW * 0.9f, browY + 4.5f * scale, headCx - headW * 0.3f, browY + 2f * scale)
                    close()
                }
                drawPath(brim, Brush.horizontalGradient(listOf(shade(accent, 0.35f), accent), headCx - headW * 0.3f, headCx + headW * 1.56f))
                drawPath(brim, INK, style = Stroke(inkW * 0.9f))
            }
            HatStyle.KNIT_BEANIE -> {
                val dome = Path().apply {
                    moveTo(headCx - headW * 1.03f, browY + 1f * scale)
                    quadraticTo(headCx, browY - 18f * scale, headCx + headW * 1.03f, browY + 1f * scale)
                    close()
                }
                drawPath(dome, Brush.horizontalGradient(listOf(dark, main, light), headCx - headW, headCx + headW))
                drawPath(dome, INK, style = Stroke(inkW))
                drawRoundRect(
                    Brush.horizontalGradient(listOf(shade(accent, 0.35f), accent), headCx - headW, headCx + headW),
                    Offset(headCx - headW * 1.08f, browY - 3.5f * scale),
                    Size(headW * 2.16f, 5.5f * scale),
                    CornerRadius(3f * scale, 3f * scale)
                )
                drawRoundRect(INK, Offset(headCx - headW * 1.08f, browY - 3.5f * scale), Size(headW * 2.16f, 5.5f * scale), CornerRadius(3f * scale, 3f * scale), style = Stroke(inkW * 0.85f))
            }
            HatStyle.BOONIE_HAT, HatStyle.RANCHER_HAT -> {
                val crown = Path().apply {
                    moveTo(headCx - headW * 0.92f, browY)
                    lineTo(headCx - headW * 0.65f, browY - 13f * scale)
                    lineTo(headCx + headW * 0.65f, browY - 13f * scale)
                    lineTo(headCx + headW * 0.92f, browY)
                    close()
                }
                drawPath(crown, Brush.horizontalGradient(listOf(dark, main, light), headCx - headW, headCx + headW))
                drawLine(accent, Offset(headCx - headW * 0.88f, browY - 2f * scale), Offset(headCx + headW * 0.88f, browY - 2f * scale), strokeWidth = 2.6f * scale)
                drawPath(crown, INK, style = Stroke(inkW))
                val brim = Path().apply {
                    moveTo(headCx - headW * 1.78f, browY + 1.5f * scale)
                    quadraticTo(headCx, browY - 3.5f * scale, headCx + headW * 1.78f, browY + 1.5f * scale)
                    quadraticTo(headCx, browY + 5f * scale, headCx - headW * 1.78f, browY + 1.5f * scale)
                    close()
                }
                drawPath(brim, shade(main, 0.22f))
                drawPath(brim, INK, style = Stroke(inkW * 0.9f))
            }
            HatStyle.FLAT_CAP -> {
                val cap = Path().apply {
                    moveTo(headCx - headW * 1.08f, browY)
                    quadraticTo(headCx - headW * 0.2f, browY - 11f * scale, headCx + headW * 1.15f, browY - 2f * scale)
                    lineTo(headCx + headW * 1.35f, browY + 2.5f * scale)
                    lineTo(headCx - headW * 0.9f, browY + 1f * scale)
                    close()
                }
                drawPath(cap, Brush.horizontalGradient(listOf(dark, main, light), headCx - headW, headCx + headW))
                drawLine(accent, Offset(headCx - headW * 0.5f, browY - 5f * scale), Offset(headCx + headW * 0.9f, browY - 3f * scale), strokeWidth = 1.5f * scale)
                drawPath(cap, INK, style = Stroke(inkW))
            }
            HatStyle.NONE -> Unit
        }
    }

    private fun DrawScope.drawNoirDogProfile(
        cx: Float,
        groundY: Float,
        scale: Float,
        coatColor: Color,
        breedName: String,
        inkW: Float
    ) {
        val coatShadow = shade(coatColor, 0.52f)
        val coatMid = shade(coatColor, 0.22f)
        val coatLight = tint(coatColor, 0.28f)
        val hasSaddle = breedName.contains("Shepherd", ignoreCase = true) ||
            breedName.contains("Malinois", ignoreCase = true) ||
            breedName.contains("Rottweiler", ignoreCase = true)
        val isFloppyEars = breedName.contains("Labrador", ignoreCase = true) ||
            breedName.contains("Bloodhound", ignoreCase = true) ||
            breedName.contains("Australian", ignoreCase = true) ||
            breedName.contains("Rottweiler", ignoreCase = true) ||
            breedName.contains("Boxer", ignoreCase = true)

        // Feathered bushy K-9 tail curled gently behind
        val tailPath = Path().apply {
            moveTo(cx + 12f * scale, groundY - 11f * scale)
            quadraticTo(cx + 24f * scale, groundY - 17f * scale, cx + 21f * scale, groundY - 32f * scale)
            quadraticTo(cx + 17f * scale, groundY - 20f * scale, cx + 11f * scale, groundY - 14f * scale)
            close()
        }
        drawPath(tailPath, Brush.verticalGradient(listOf(coatLight, coatShadow)))
        drawPath(tailPath, INK, style = Stroke(inkW * 0.85f, join = StrokeJoin.Round))

        // Muscular seated ribcage + haunch silhouette
        val bodyPath = Path().apply {
            moveTo(cx - 11f * scale, groundY - 35f * scale)
            quadraticTo(cx - 2f * scale, groundY - 37f * scale, cx + 5f * scale, groundY - 32f * scale)
            quadraticTo(cx + 18f * scale, groundY - 18f * scale, cx + 14f * scale, groundY - 2f * scale)
            lineTo(cx - 12f * scale, groundY - 2f * scale)
            quadraticTo(cx - 14f * scale, groundY - 19f * scale, cx - 11f * scale, groundY - 35f * scale)
            close()
        }
        drawPath(
            bodyPath,
            Brush.horizontalGradient(
                colors = listOf(coatLight, coatColor, coatShadow),
                startX = cx - 14f * scale,
                endX = cx + 16f * scale
            )
        )
        if (hasSaddle) {
            val saddle = Path().apply {
                moveTo(cx - 2f * scale, groundY - 34f * scale)
                quadraticTo(cx + 12f * scale, groundY - 24f * scale, cx + 13f * scale, groundY - 11f * scale)
                lineTo(cx + 2f * scale, groundY - 13f * scale)
                close()
            }
            drawPath(saddle, Color(0xFF1A1614).copy(alpha = 0.78f))
        }
        // Chestnut ruff fur tufts along lit chest
        for (i in 0..3) {
            val fy = groundY - (28f - i * 4.5f) * scale
            drawLine(coatLight.copy(alpha = 0.85f), Offset(cx - 11f * scale, fy), Offset(cx - 6f * scale, fy + 3.5f * scale), strokeWidth = 1.4f * scale, cap = StrokeCap.Round)
        }
        drawPath(bodyPath, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        // Leather tactical collar + brass ring tag
        drawLine(Color(0xFF7F1D1D), Offset(cx - 10f * scale, groundY - 34.5f * scale), Offset(cx + 4f * scale, groundY - 32f * scale), strokeWidth = 3.2f * scale, cap = StrokeCap.Round)
        drawLine(INK, Offset(cx - 10f * scale, groundY - 33f * scale), Offset(cx + 4f * scale, groundY - 30.5f * scale), strokeWidth = 0.9f * scale)
        drawCircle(WARM_LAMP, radius = 1.7f * scale, center = Offset(cx - 8f * scale, groundY - 31.5f * scale))

        // Front forelegs + grounded paws
        drawLine(coatMid, Offset(cx - 3f * scale, groundY - 22f * scale), Offset(cx - 4f * scale, groundY - 2f * scale), strokeWidth = 4.5f * scale, cap = StrokeCap.Round)
        drawLine(coatLight, Offset(cx - 9f * scale, groundY - 22f * scale), Offset(cx - 10f * scale, groundY - 2f * scale), strokeWidth = 4.8f * scale, cap = StrokeCap.Round)
        drawOval(coatColor, Offset(cx - 14f * scale, groundY - 4f * scale), Size(7f * scale, 4.2f * scale))
        drawOval(coatMid, Offset(cx - 7f * scale, groundY - 4f * scale), Size(7f * scale, 4.2f * scale))
        drawOval(INK, Offset(cx - 14f * scale, groundY - 4f * scale), Size(7f * scale, 4.2f * scale), style = Stroke(inkW * 0.7f))

        // Sculpted K-9 Skull + Tapered Muzzle
        val headY = groundY - 43.5f * scale
        val headPath = Path().apply {
            moveTo(cx - 8f * scale, headY - 7.5f * scale)
            quadraticTo(cx - 1f * scale, headY - 8.5f * scale, cx + 4.5f * scale, headY - 6.5f * scale)
            quadraticTo(cx + 6.5f * scale, headY + 1f * scale, cx + 5f * scale, headY + 6.5f * scale)
            lineTo(cx - 9.5f * scale, headY + 7.5f * scale)
            lineTo(cx - 18.5f * scale, headY + 3.2f * scale)
            lineTo(cx - 17.5f * scale, headY - 1.5f * scale)
            lineTo(cx - 11.5f * scale, headY - 3.5f * scale)
            close()
        }
        drawPath(headPath, Brush.horizontalGradient(listOf(coatLight, coatColor, coatShadow), cx - 18f * scale, cx + 6f * scale))
        if (hasSaddle) {
            // Shepherd/Malinois dark facial mask around muzzle
            val mask = Path().apply {
                moveTo(cx - 18.5f * scale, headY + 3f * scale)
                lineTo(cx - 17.5f * scale, headY - 1.5f * scale)
                lineTo(cx - 10f * scale, headY - 3f * scale)
                lineTo(cx - 9f * scale, headY + 4f * scale)
                close()
            }
            drawPath(mask, Color(0xFF161311).copy(alpha = 0.72f))
        }
        drawPath(headPath, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        // Breed-reactive anatomical ears with inner pinna shadow
        if (isFloppyEars) {
            val ear = Path().apply {
                moveTo(cx + 1f * scale, headY - 5f * scale)
                quadraticTo(cx + 7.5f * scale, headY + 1f * scale, cx + 6f * scale, headY + 6.5f * scale)
                lineTo(cx - 1f * scale, headY + 6f * scale)
                close()
            }
            drawPath(ear, coatShadow)
            drawPath(ear, INK, style = Stroke(inkW * 0.85f))
        } else {
            val earBack = Path().apply {
                moveTo(cx - 5.5f * scale, headY - 6f * scale)
                lineTo(cx - 3.5f * scale, headY - 17f * scale)
                lineTo(cx + 1f * scale, headY - 7f * scale)
                close()
            }
            val earFront = Path().apply {
                moveTo(cx + 0.5f * scale, headY - 6f * scale)
                lineTo(cx + 4.5f * scale, headY - 17.5f * scale)
                lineTo(cx + 5.5f * scale, headY - 5.5f * scale)
                close()
            }
            drawPath(earBack, coatShadow)
            drawPath(earBack, INK, style = Stroke(inkW * 0.8f))
            drawPath(earFront, coatColor)
            drawLine(coatShadow, Offset(cx + 3.8f * scale, headY - 14f * scale), Offset(cx + 2.5f * scale, headY - 7f * scale), strokeWidth = 1.8f * scale)
            drawPath(earFront, INK, style = Stroke(inkW * 0.85f))
        }

        // Textured wet black canine nose + sculpted alert eye under brow
        drawOval(INK, Offset(cx - 18.8f * scale, headY - 1.8f * scale), Size(3.6f * scale, 2.8f * scale))
        drawCircle(Color.White.copy(alpha = 0.45f), radius = 0.5f * scale, center = Offset(cx - 17.6f * scale, headY - 1.1f * scale))
        val dogEye = Offset(cx - 6.8f * scale, headY - 2.3f * scale)
        drawOval(INK, Offset(dogEye.x - 2.2f * scale, dogEye.y - 1.4f * scale), Size(4.4f * scale, 2.8f * scale))
        drawCircle(Color(0xFFB45309), radius = 1.0f * scale, center = dogEye)
        drawCircle(INK, radius = 0.5f * scale, center = dogEye)
        drawCircle(Color.White.copy(alpha = 0.85f), radius = 0.35f * scale, center = Offset(dogEye.x + 0.4f * scale, dogEye.y - 0.3f * scale))
    }

    // =========================================================================
    // 8 REALISTIC GRAPHIC-NOVEL CATEGORY PICKER DRAWERS
    // =========================================================================

    fun DrawScope.drawNoirSkinSexPreview(tone: SkinToneOption, sex: OperatorSex, shirt: ShirtStyle) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val skin = Color(tone.colorHex)
        val skinShadow = shade(skin, 0.42f)
        val skinHighlight = tint(skin, 0.24f)
        val shirtCol = Color(shirt.primaryHex)
        val isFemale = sex == OperatorSex.FEMALE

        drawNoirBackdrop(accent = skin)

        val scale = min(w, h) / 64f
        val inkW = (1.35f * scale).coerceIn(1.0f, 2.2f)
        val fineInk = inkW * 0.68f

        val shW = (if (isFemale) 26f else 31f) * scale
        val shTop = h * 0.68f
        val shoulders = Path().apply {
            moveTo(cx - shW, h)
            lineTo(cx - shW * 0.82f, shTop + 4f * scale)
            lineTo(cx - 8f * scale, shTop)
            lineTo(cx + 8f * scale, shTop)
            lineTo(cx + shW * 0.82f, shTop + 4f * scale)
            lineTo(cx + shW, h)
            close()
        }
        drawPath(shoulders, Brush.horizontalGradient(listOf(shade(shirtCol, 0.48f), shirtCol, tint(shirtCol, 0.18f)), cx - shW, cx + shW))
        drawPath(shoulders, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        drawRect(Brush.horizontalGradient(listOf(skinShadow, skin)), Offset(cx - 5.5f * scale, h * 0.54f), Size(11f * scale, h * 0.16f))

        val headY = h * 0.36f
        val hw = (if (isFemale) 12.0f else 13.4f) * scale
        val hh = 15.8f * scale
        if (isFemale) {
            val backHair = Path().apply {
                moveTo(cx - hw - 3f * scale, headY - 4f * scale)
                lineTo(cx - hw - 5.5f * scale, headY + hh + 3.5f * scale)
                lineTo(cx + hw + 4.5f * scale, headY + hh + 3.5f * scale)
                lineTo(cx + hw + 2f * scale, headY - 4f * scale)
                close()
            }
            drawPath(backHair, Brush.horizontalGradient(listOf(INK, Color(0xFF281C15)), cx - hw - 5f * scale, cx + hw + 4f * scale))
            drawPath(backHair, INK, style = Stroke(inkW))
        }

        val face = Path().apply {
            moveTo(cx - hw, headY - 4f * scale)
            quadraticTo(cx, headY - hh - 3f * scale, cx + hw, headY - 4f * scale)
            lineTo(cx + hw * 0.92f, headY + hh * 0.42f)
            lineTo(cx + hw * (if (isFemale) 0.24f else 0.42f), headY + hh)
            lineTo(cx - hw * (if (isFemale) 0.24f else 0.42f), headY + hh)
            lineTo(cx - hw * 0.92f, headY + hh * 0.42f)
            close()
        }
        drawPath(
            face,
            Brush.linearGradient(
                colors = listOf(skinShadow, skin, skinHighlight),
                start = Offset(cx - hw, headY + hh * 0.4f),
                end = Offset(cx + hw, headY - hh * 0.4f)
            )
        )
        drawPath(face, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        drawRealisticFaceFeatures(cx, headY + 1f * scale, scale, skinShadow, skinHighlight, isFemale, fineInk)

        val hair = Path().apply {
            moveTo(cx - hw * 1.03f, headY - 2f * scale)
            quadraticTo(cx, headY - hh - 4.5f * scale, cx + hw * 1.03f, headY - 2f * scale)
            lineTo(cx + hw * 0.45f, headY - 7f * scale)
            lineTo(cx - hw * 0.45f, headY - 6f * scale)
            close()
        }
        drawPath(hair, Brush.horizontalGradient(listOf(INK, Color(0xFF2B2019)), cx - hw, cx + hw))
        drawPath(hair, INK, style = Stroke(fineInk))
    }

    fun DrawScope.drawNoirArchetypePreview(arch: OperatorArchetype) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.5f
        val vest = Color(arch.defaultVestHex)
        val pack = Color(arch.defaultPackHex)
        val scale = min(w, h) / 64f
        val inkW = (1.45f * scale).coerceIn(1.0f, 2.2f)

        drawNoirBackdrop(accent = AmberBright)

        drawRoundRect(
            Brush.horizontalGradient(listOf(shade(pack, 0.48f), shade(pack, 0.12f))),
            Offset(cx - 21f * scale, cy - 22f * scale),
            Size(42f * scale, 44f * scale),
            CornerRadius(6f * scale, 6f * scale)
        )
        drawRoundRect(INK, Offset(cx - 21f * scale, cy - 22f * scale), Size(42f * scale, 44f * scale), CornerRadius(6f * scale, 6f * scale), style = Stroke(inkW))

        val rig = Path().apply {
            moveTo(cx - 16f * scale, cy - 21f * scale)
            lineTo(cx - 8f * scale, cy - 21f * scale)
            lineTo(cx - 6f * scale, cy - 13f * scale)
            lineTo(cx + 6f * scale, cy - 13f * scale)
            lineTo(cx + 8f * scale, cy - 21f * scale)
            lineTo(cx + 16f * scale, cy - 21f * scale)
            lineTo(cx + 18f * scale, cy + 20f * scale)
            lineTo(cx - 18f * scale, cy + 20f * scale)
            close()
        }
        drawPath(rig, Brush.horizontalGradient(listOf(shade(vest, 0.48f), vest, tint(vest, 0.16f)), cx - 18f * scale, cx + 18f * scale))
        for (r in 0..1) {
            val ry = cy + (5f + r * 7f) * scale
            drawLine(shade(vest, 0.45f), Offset(cx - 14f * scale, ry), Offset(cx + 14f * scale, ry), strokeWidth = 2f * scale)
        }
        drawPath(rig, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        when (arch) {
            OperatorArchetype.BREACHER -> {
                drawLine(Color(0xFF78350F), Offset(cx - 11f * scale, cy + 7f * scale), Offset(cx + 11f * scale, cy - 11f * scale), strokeWidth = 4.2f * scale, cap = StrokeCap.Round)
                drawRect(Color(0xFFCBD5E1), Offset(cx + 3f * scale, cy - 16f * scale), Size(12f * scale, 8f * scale))
                drawRect(INK, Offset(cx + 3f * scale, cy - 16f * scale), Size(12f * scale, 8f * scale), style = Stroke(inkW * 0.8f))
            }
            OperatorArchetype.RECON -> {
                val bolt = Path().apply {
                    moveTo(cx + 3f * scale, cy - 14f * scale)
                    lineTo(cx - 8f * scale, cy + 1f * scale)
                    lineTo(cx, cy + 1f * scale)
                    lineTo(cx - 3f * scale, cy + 14f * scale)
                    lineTo(cx + 9f * scale, cy - 2f * scale)
                    lineTo(cx + 1f * scale, cy - 2f * scale)
                    close()
                }
                drawPath(bolt, EmeraldHud)
                drawPath(bolt, INK, style = Stroke(inkW * 0.8f))
            }
            OperatorArchetype.RESPONDER -> {
                drawCircle(CrimsonAlert, 12f * scale, Offset(cx, cy - 1f * scale))
                drawCircle(INK, 12f * scale, Offset(cx, cy - 1f * scale), style = Stroke(inkW * 0.8f))
                drawRect(BoneWhite, Offset(cx - 2.5f * scale, cy - 9f * scale), Size(5f * scale, 16f * scale))
                drawRect(BoneWhite, Offset(cx - 8f * scale, cy - 3.5f * scale), Size(16f * scale, 5f * scale))
            }
            OperatorArchetype.MARSHAL -> {
                drawCircle(Brush.radialGradient(listOf(WARM_LAMP, Color(0xFF92400E))), 11f * scale, Offset(cx, cy - 1f * scale))
                drawCircle(INK, 11f * scale, Offset(cx, cy - 1f * scale), style = Stroke(inkW * 0.85f))
                drawCircle(Color(0xFF1E3252), 5.5f * scale, Offset(cx, cy - 1f * scale))
            }
        }
    }

    fun DrawScope.drawNoirShirtPreview(shirt: ShirtStyle) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val primary = Color(shirt.primaryHex)
        val pat = Color(shirt.patternHex)
        val scale = min(w, h) / 64f
        val inkW = (1.45f * scale).coerceIn(1.0f, 2.2f)

        drawNoirBackdrop(accent = primary)

        val topY = h * 0.22f
        val botY = h * 0.88f
        val garment = Path().apply {
            moveTo(cx - 10f * scale, topY)
            lineTo(cx + 10f * scale, topY)
            lineTo(cx + 26f * scale, topY + 10f * scale)
            lineTo(cx + 29f * scale, topY + 26f * scale)
            lineTo(cx + 18f * scale, topY + 27f * scale)
            lineTo(cx + 15f * scale, botY)
            lineTo(cx - 15f * scale, botY)
            lineTo(cx - 18f * scale, topY + 27f * scale)
            lineTo(cx - 29f * scale, topY + 26f * scale)
            lineTo(cx - 26f * scale, topY + 10f * scale)
            close()
        }
        drawPath(garment, Brush.horizontalGradient(listOf(shade(primary, 0.45f), primary, tint(primary, 0.18f)), cx - 29f * scale, cx + 29f * scale))

        if (shirt.isPlaid) {
            for (i in 0..3) {
                val py = topY + (8f + i * 9f) * scale
                drawLine(pat.copy(alpha = 0.62f), Offset(cx - 24f * scale, py), Offset(cx + 24f * scale, py), strokeWidth = 2.4f * scale)
            }
            for (j in -2..2) {
                val px = cx + j * 7f * scale
                drawLine(pat.copy(alpha = 0.62f), Offset(px, topY + 2f * scale), Offset(px, botY - 2f * scale), strokeWidth = 2.2f * scale)
            }
        }

        if (shirt.hasHood) {
            drawArc(pat, 180f, 180f, true, Offset(cx - 11f * scale, topY - 8f * scale), Size(22f * scale, 16f * scale))
            drawArc(INK, 180f, 180f, false, Offset(cx - 11f * scale, topY - 8f * scale), Size(22f * scale, 16f * scale), style = Stroke(inkW))
            drawLine(BoneWhite, Offset(cx - 3.5f * scale, topY + 4f * scale), Offset(cx - 4.5f * scale, topY + 18f * scale), strokeWidth = 1.4f * scale)
            drawLine(BoneWhite, Offset(cx + 3.5f * scale, topY + 4f * scale), Offset(cx + 4.5f * scale, topY + 18f * scale), strokeWidth = 1.4f * scale)
        } else {
            drawLine(pat, Offset(cx, topY + 5f * scale), Offset(cx, botY - 3f * scale), strokeWidth = 1.8f * scale)
            drawRect(pat.copy(alpha = 0.72f), Offset(cx - 12f * scale, topY + 14f * scale), Size(8f * scale, 9f * scale), style = Stroke(1.2f * scale))
            drawRect(pat.copy(alpha = 0.72f), Offset(cx + 4f * scale, topY + 14f * scale), Size(8f * scale, 9f * scale), style = Stroke(1.2f * scale))
        }
        drawCircle(INK, radius = 4.8f * scale, center = Offset(cx, topY + 1f * scale))
        drawPath(garment, INK, style = Stroke(inkW, join = StrokeJoin.Round))
    }

    fun DrawScope.drawNoirPantsPreview(pants: PantsStyle) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val main = Color(pants.colorHex)
        val trim = Color(pants.trimHex)
        val scale = min(w, h) / 64f
        val inkW = (1.45f * scale).coerceIn(1.0f, 2.2f)

        drawNoirBackdrop(accent = trim)

        val topY = h * 0.14f
        val botY = h * 0.88f
        val trousers = Path().apply {
            moveTo(cx - 15f * scale, topY)
            lineTo(cx + 15f * scale, topY)
            lineTo(cx + 17f * scale, h * 0.42f)
            lineTo(cx + 15f * scale, botY)
            lineTo(cx + 4f * scale, botY)
            lineTo(cx + 1f * scale, h * 0.45f)
            lineTo(cx - 1f * scale, h * 0.45f)
            lineTo(cx - 4f * scale, botY)
            lineTo(cx - 15f * scale, botY)
            lineTo(cx - 17f * scale, h * 0.42f)
            close()
        }
        drawPath(trousers, Brush.horizontalGradient(listOf(shade(main, 0.45f), main, tint(main, 0.18f)), cx - 17f * scale, cx + 17f * scale))
        drawRect(Color(0xFF1C1917), Offset(cx - 15f * scale, topY), Size(30f * scale, 5f * scale))
        drawRect(AmberSignal, Offset(cx - 2.5f * scale, topY + 0.5f * scale), Size(5f * scale, 4f * scale))
        drawLine(trim, Offset(cx, topY + 6f * scale), Offset(cx, h * 0.42f), strokeWidth = 1.3f * scale)
        if (pants == PantsStyle.CHARCOAL_CARGO || pants == PantsStyle.WORK_CANVAS || pants == PantsStyle.OLIVE_FATIGUES) {
            drawRect(trim.copy(alpha = 0.7f), Offset(cx - 14f * scale, h * 0.48f), Size(8f * scale, 12f * scale), style = Stroke(1.2f * scale))
            drawRect(trim.copy(alpha = 0.7f), Offset(cx + 6f * scale, h * 0.48f), Size(8f * scale, 12f * scale), style = Stroke(1.2f * scale))
        }
        drawPath(trousers, INK, style = Stroke(inkW, join = StrokeJoin.Round))
    }

    fun DrawScope.drawNoirShoePreview(shoe: ShoeStyle) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val upper = Color(shoe.colorHex)
        val sole = Color(shoe.soleHex)
        val scale = min(w, h) / 64f
        val inkW = (1.45f * scale).coerceIn(1.0f, 2.2f)

        drawNoirBackdrop(accent = sole)

        val isHigh = shoe == ShoeStyle.WORK_BOOTS || shoe == ShoeStyle.HIKING_BOOTS
        val topY = if (isHigh) h * 0.24f else h * 0.42f
        val baseY = h * 0.74f

        val bootPath = Path().apply {
            moveTo(cx - 20f * scale, topY)
            lineTo(cx - 2f * scale, topY)
            lineTo(cx + 4f * scale, baseY - 12f * scale)
            lineTo(cx + 22f * scale, baseY - 9f * scale)
            quadraticTo(cx + 25f * scale, baseY - 2f * scale, cx + 22f * scale, baseY)
            lineTo(cx - 20f * scale, baseY)
            close()
        }
        drawPath(bootPath, Brush.horizontalGradient(listOf(shade(upper, 0.45f), upper, tint(upper, 0.25f)), cx - 20f * scale, cx + 24f * scale))
        for (l in 0..2) {
            val ly = baseY - (10f + l * 4.5f) * scale
            drawLine(PAPER_HIGHLIGHT, Offset(cx - 1f * scale, ly), Offset(cx + 4f * scale, ly - 1.5f * scale), strokeWidth = 1.4f * scale)
        }
        drawPath(bootPath, INK, style = Stroke(inkW, join = StrokeJoin.Round))

        drawRoundRect(sole, Offset(cx - 21f * scale, baseY), Size(45f * scale, 6.5f * scale), CornerRadius(2f * scale, 2f * scale))
        drawRoundRect(INK, Offset(cx - 21f * scale, baseY), Size(45f * scale, 6.5f * scale), CornerRadius(2f * scale, 2f * scale), style = Stroke(inkW))
    }

    fun DrawScope.drawNoirHatPreview(hat: HatStyle, skinTone: SkinToneOption) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.58f
        val skin = Color(skinTone.colorHex)
        val skinShadow = shade(skin, 0.42f)
        val skinHighlight = tint(skin, 0.22f)
        val scale = min(w, h) / 64f
        val inkW = (1.45f * scale).coerceIn(1.0f, 2.2f)

        drawNoirBackdrop(accent = if (hat == HatStyle.NONE) CyanFloor else Color(hat.accentHex))

        val hw = 13f * scale
        val hh = 15f * scale
        val head = Path().apply {
            moveTo(cx - hw, cy - 4f * scale)
            quadraticTo(cx, cy - hh - 2f * scale, cx + hw, cy - 4f * scale)
            lineTo(cx + hw * 0.9f, cy + hh * 0.45f)
            lineTo(cx + hw * 0.35f, cy + hh)
            lineTo(cx - hw * 0.35f, cy + hh)
            lineTo(cx - hw * 0.9f, cy + hh * 0.45f)
            close()
        }
        drawPath(head, Brush.linearGradient(listOf(skinShadow, skin, skinHighlight), Offset(cx - hw, cy + hh), Offset(cx + hw, cy - hh)))
        drawPath(head, INK, style = Stroke(inkW))
        drawRealisticFaceFeatures(cx, cy + 1f * scale, scale * 0.92f, skinShadow, skinHighlight, false, inkW * 0.65f)

        if (hat == HatStyle.NONE) {
            val hair = Path().apply {
                moveTo(cx - hw, cy - 3f * scale)
                quadraticTo(cx, cy - hh - 4f * scale, cx + hw, cy - 3f * scale)
                lineTo(cx + 4f * scale, cy - 7f * scale)
                lineTo(cx - 4f * scale, cy - 6f * scale)
                close()
            }
            drawPath(hair, Color(0xFF231A14))
            drawPath(hair, INK, style = Stroke(inkW * 0.7f))
            return
        }
        drawPortraitHat(cx, cy - 4.5f * scale, hw * 1.15f, hat, scale * 1.1f, inkW)
    }

    fun DrawScope.drawNoirGlassesPreview(glasses: GlassesStyle, skinTone: SkinToneOption) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.52f
        val skin = Color(skinTone.colorHex)
        val skinShadow = shade(skin, 0.42f)
        val skinHighlight = tint(skin, 0.22f)
        val scale = min(w, h) / 64f
        val inkW = (1.4f * scale).coerceIn(1.0f, 2.2f)

        drawNoirBackdrop(accent = if (glasses == GlassesStyle.NONE) CyanFloor else Color(glasses.frameHex))

        drawRoundRect(
            Brush.linearGradient(listOf(skinShadow, skin, skinHighlight), Offset(cx - 22f * scale, cy + 16f * scale), Offset(cx + 22f * scale, cy - 16f * scale)),
            Offset(cx - 22f * scale, cy - 17f * scale),
            Size(44f * scale, 34f * scale),
            CornerRadius(12f * scale, 12f * scale)
        )
        drawRoundRect(INK, Offset(cx - 22f * scale, cy - 17f * scale), Size(44f * scale, 34f * scale), CornerRadius(12f * scale, 12f * scale), style = Stroke(inkW))

        drawRealisticFaceFeatures(cx, cy, scale * 1.6f, skinShadow, skinHighlight, false, inkW * 0.65f)

        if (glasses != GlassesStyle.NONE) {
            drawPortraitGlasses(cx, cy, 26f * scale, glasses, scale * 1.45f)
        }
    }

    fun DrawScope.drawNoirGlovesPreview(gloves: GlovesStyle, skinTone: SkinToneOption) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.56f
        val scale = min(w, h) / 64f
        val inkW = (1.4f * scale).coerceIn(1.0f, 2.2f)
        val palmCol = if (gloves == GlovesStyle.NONE) Color(skinTone.colorHex) else Color(gloves.colorHex)
        val tipCol = if (gloves.bareTips) Color(skinTone.colorHex) else palmCol

        drawNoirBackdrop(accent = if (gloves == GlovesStyle.NONE) Color(skinTone.colorHex) else Color(gloves.colorHex))

        drawRoundRect(shade(palmCol, 0.35f), Offset(cx - 9f * scale, cy + 12f * scale), Size(18f * scale, 10f * scale), CornerRadius(2f * scale, 2f * scale))
        drawRoundRect(INK, Offset(cx - 9f * scale, cy + 12f * scale), Size(18f * scale, 10f * scale), CornerRadius(2f * scale, 2f * scale), style = Stroke(inkW))

        drawRoundRect(
            Brush.horizontalGradient(listOf(shade(palmCol, 0.38f), palmCol, tint(palmCol, 0.2f)), cx - 11f * scale, cx + 11f * scale),
            Offset(cx - 11f * scale, cy - 6f * scale),
            Size(22f * scale, 20f * scale),
            CornerRadius(5f * scale, 5f * scale)
        )
        if (gloves == GlovesStyle.MECHANIC || gloves == GlovesStyle.LEATHER_WORK) {
            drawRect(tint(palmCol, 0.22f), Offset(cx - 10f * scale, cy - 6f * scale), Size(20f * scale, 4.5f * scale))
        }
        drawRoundRect(INK, Offset(cx - 11f * scale, cy - 6f * scale), Size(22f * scale, 20f * scale), CornerRadius(5f * scale, 5f * scale), style = Stroke(inkW))

        for (i in 0..3) {
            val fx = cx - 10.5f * scale + i * 5.6f * scale
            val fh = if (i == 1 || i == 2) 15f * scale else 12.5f * scale
            drawRoundRect(palmCol, Offset(fx, cy - 6f * scale - fh * 0.65f), Size(4.4f * scale, fh * 0.65f), CornerRadius(2f * scale, 2f * scale))
            drawRoundRect(tipCol, Offset(fx, cy - 6f * scale - fh), Size(4.4f * scale, fh * 0.40f), CornerRadius(2f * scale, 2f * scale))
            drawRoundRect(INK, Offset(fx, cy - 6f * scale - fh), Size(4.4f * scale, fh), CornerRadius(2f * scale, 2f * scale), style = Stroke(inkW * 0.78f))
        }
        drawLine(INK, Offset(cx - 10f * scale, cy + 5f * scale), Offset(cx - 18f * scale, cy - 4f * scale), strokeWidth = 7.2f * scale, cap = StrokeCap.Round)
        drawLine(tipCol, Offset(cx - 10f * scale, cy + 5f * scale), Offset(cx - 17.5f * scale, cy - 3.5f * scale), strokeWidth = 4.6f * scale, cap = StrokeCap.Round)
    }
}
