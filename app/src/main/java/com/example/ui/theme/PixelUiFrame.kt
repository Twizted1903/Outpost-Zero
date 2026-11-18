package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Option-3-inspired sleek tactical slate-glass HUD frame:
 * translucent dark slate gradient, razor-thin cyan/slate perimeter hairline,
 * luminous cyan top edge sheen, and crisp L-shaped tactical corner brackets.
 */
fun Modifier.pixelBevelFrame(
    fillColor: Color = BunkerPanel,
    borderColor: Color = OliveBorder,
    borderWidth: Dp = 1.5.dp
): Modifier = this
    .clip(RoundedCornerShape(4.dp))
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                fillColor.copy(alpha = (fillColor.alpha * 0.96f).coerceIn(0.72f, 0.96f)),
                BunkerObsidian.copy(alpha = (fillColor.alpha * 0.92f).coerceIn(0.78f, 0.95f))
            )
        ),
        shape = RoundedCornerShape(4.dp)
    )
    .border(1.dp, borderColor.copy(alpha = 0.72f), RoundedCornerShape(4.dp))
    .drawBehind {
        val bw = borderWidth.toPx().coerceAtLeast(1.4f)
        val w = size.width
        val h = size.height
        if (w < 8f || h < 8f) return@drawBehind

        // Subtle tactical horizontal telemetry hairline across top edge
        drawRect(
            color = BevelHighlight.copy(alpha = 0.28f),
            topLeft = Offset(1f, 1f),
            size = Size((w - 2f).coerceAtLeast(0f), 1.4f)
        )

        // Crisp L-shaped tactical reticle brackets on all 4 corners
        val arm = minOf(10.dp.toPx(), minOf(w, h) * 0.24f)
        val bracketCol = if (borderColor == OliveBorder) BevelHighlight.copy(alpha = 0.92f) else borderColor
        val strokeW = bw.coerceIn(1.5f, 2.5f)

        // Top-left bracket
        drawLine(bracketCol, Offset(0f, 0f), Offset(arm, 0f), strokeWidth = strokeW)
        drawLine(bracketCol, Offset(0f, 0f), Offset(0f, arm), strokeWidth = strokeW)
        // Top-right bracket
        drawLine(bracketCol, Offset(w - arm, 0f), Offset(w, 0f), strokeWidth = strokeW)
        drawLine(bracketCol, Offset(w, 0f), Offset(w, arm), strokeWidth = strokeW)
        // Bottom-left bracket
        drawLine(bracketCol, Offset(0f, h - arm), Offset(0f, h), strokeWidth = strokeW)
        drawLine(bracketCol, Offset(0f, h), Offset(arm, h), strokeWidth = strokeW)
        // Bottom-right bracket
        drawLine(bracketCol, Offset(w - arm, h), Offset(w, h), strokeWidth = strokeW)
        drawLine(bracketCol, Offset(w, h - arm), Offset(w, h), strokeWidth = strokeW)
    }
