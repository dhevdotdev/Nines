package dev.dhev.nines.ui.habits

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * A handful of abstract, procedurally-drawn motifs a habit's card background can carry — the
 * decorative-illustration flavor Material's own marketing/empty-state art uses, without pulling
 * in external art assets of uncertain quality or license. Deliberately abstract (rings, waves,
 * scattered dots) rather than representational, so restraint does the work: low-alpha, tinted to
 * the habit's own accent, bled off one corner rather than covering the card.
 */
enum class CardBackgroundStyle(val key: String, val label: String) {
    NONE("none", "None"),
    RINGS("rings", "Rings"),
    WAVES("waves", "Waves"),
    DOTS("dots", "Dots"),
    PEAKS("peaks", "Peaks"),
    SPARKLE("sparkle", "Sparkle"),
    ARC("arc", "Arc"),
    ZIGZAG("zigzag", "Zigzag"),
    ;

    companion object {
        fun fromKey(key: String?): CardBackgroundStyle = entries.find { it.key == key } ?: NONE
    }
}

@Composable
fun HabitCardBackground(style: CardBackgroundStyle, color: Color, modifier: Modifier = Modifier) {
    if (style == CardBackgroundStyle.NONE) return
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val tint = color.copy(alpha = 0.14f)
        val strokeWidth = size.minDimension * 0.025f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (style) {
            CardBackgroundStyle.RINGS -> {
                val center = Offset(w * 0.92f, h * 0.18f)
                repeat(3) { i ->
                    drawCircle(
                        color = tint,
                        radius = w * (0.12f + i * 0.10f),
                        center = center,
                        style = stroke,
                    )
                }
            }
            CardBackgroundStyle.WAVES -> {
                repeat(3) { i ->
                    val yBase = h * (0.15f + i * 0.32f)
                    val path = Path().apply {
                        moveTo(w * 0.55f, yBase)
                        cubicTo(w * 0.68f, yBase - h * 0.12f, w * 0.80f, yBase + h * 0.12f, w * 0.98f, yBase)
                    }
                    drawPath(path, tint, style = stroke)
                }
            }
            CardBackgroundStyle.DOTS -> {
                val points = listOf(
                    0.62f to 0.20f,
                    0.78f to 0.45f,
                    0.90f to 0.15f,
                    0.70f to 0.75f,
                    0.95f to 0.65f,
                )
                points.forEachIndexed { index, (fx, fy) ->
                    val radius = strokeWidth * (0.8f + (index % 3) * 0.35f)
                    drawCircle(color = tint, radius = radius, center = Offset(w * fx, h * fy))
                }
            }
            CardBackgroundStyle.PEAKS -> {
                val path = Path().apply {
                    moveTo(w * 0.55f, h * 0.75f)
                    lineTo(w * 0.68f, h * 0.35f)
                    lineTo(w * 0.78f, h * 0.55f)
                    lineTo(w * 0.90f, h * 0.20f)
                    lineTo(w * 1.02f, h * 0.75f)
                }
                drawPath(path, tint, style = stroke)
            }
            CardBackgroundStyle.SPARKLE -> {
                val sparkles = listOf(Triple(0.65f, 0.22f, 1.0f), Triple(0.85f, 0.48f, 0.7f), Triple(0.94f, 0.18f, 0.55f))
                sparkles.forEach { (fx, fy, scale) ->
                    val cx = w * fx
                    val cy = h * fy
                    val arm = strokeWidth * 2.4f * scale
                    val armStroke = Stroke(width = strokeWidth * 0.6f, cap = StrokeCap.Round)
                    drawLine(tint, Offset(cx - arm, cy), Offset(cx + arm, cy), strokeWidth = armStroke.width, cap = StrokeCap.Round)
                    drawLine(tint, Offset(cx, cy - arm), Offset(cx, cy + arm), strokeWidth = armStroke.width, cap = StrokeCap.Round)
                }
            }
            CardBackgroundStyle.ARC -> {
                drawArc(
                    color = tint,
                    startAngle = 90f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(w * 0.35f, -h * 0.35f),
                    size = androidx.compose.ui.geometry.Size(w * 0.9f, w * 0.9f),
                    style = stroke,
                )
            }
            CardBackgroundStyle.ZIGZAG -> {
                val path = Path().apply {
                    moveTo(w * 0.55f, h * 0.5f)
                    var x = w * 0.55f
                    val step = w * 0.09f
                    var up = true
                    while (x < w) {
                        val nextX = (x + step).coerceAtMost(w)
                        lineTo(nextX, if (up) h * 0.3f else h * 0.7f)
                        x = nextX
                        up = !up
                    }
                }
                drawPath(path, tint, style = stroke)
            }
            CardBackgroundStyle.NONE -> Unit
        }
    }
}
