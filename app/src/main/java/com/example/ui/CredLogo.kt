package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BunkerObsidian
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom vector emblem for Outpost Zero's Credits currency ("CRED").
 */
@Composable
fun CredLogo(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r = this.size.minDimension * 0.46f

        val hexPath = Path().apply {
            for (i in 0 until 6) {
                val a = (PI / 3.0 * i - PI / 6.0).toFloat()
                val px = cx + cos(a) * r
                val py = cy + sin(a) * r
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }

        // Outer metallic gold hex chit
        drawPath(
            path = hexPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFDE68A), Color(0xFFD97706)),
                start = Offset(0f, 0f),
                end = Offset(this.size.width, this.size.height)
            )
        )
        drawPath(
            path = hexPath,
            color = Color(0xFF78350F),
            style = Stroke(width = 1.4f, join = StrokeJoin.Round)
        )

        // Inner obsidian core plate
        val innerR = r * 0.74f
        val innerHex = Path().apply {
            for (i in 0 until 6) {
                val a = (PI / 3.0 * i - PI / 6.0).toFloat()
                val px = cx + cos(a) * innerR
                val py = cy + sin(a) * innerR
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(path = innerHex, color = Color(0xFF141914))

        // Vertical currency bar notch
        drawLine(
            color = AmberSignal,
            start = Offset(cx, cy - r * 0.50f),
            end = Offset(cx, cy + r * 0.50f),
            strokeWidth = (r * 0.16f).coerceAtLeast(1.4f),
            cap = StrokeCap.Round
        )

        // Crisp tactical "C" emblem arc
        val cR = r * 0.42f
        drawArc(
            color = AmberBright,
            startAngle = 42f,
            sweepAngle = 276f,
            useCenter = false,
            topLeft = Offset(cx - cR, cy - cR),
            size = androidx.compose.ui.geometry.Size(cR * 2f, cR * 2f),
            style = Stroke(width = (r * 0.24f).coerceAtLeast(2f), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun CredBadge(
    amount: Int,
    modifier: Modifier = Modifier,
    showUnit: Boolean = true,
    accent: Color = AmberBright
) {
    Row(
        modifier = modifier
            .background(BunkerObsidian, RoundedCornerShape(8.dp))
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        CredLogo(size = 16.dp)
        Text(
            text = if (showUnit) "$amount CRED" else "$amount",
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Bold
        )
    }
}
