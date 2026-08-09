package dev.dhev.nines.ui.habits

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
    SPIRAL("spiral", "Spiral"),
    STARS("stars", "Stars"),
    CONFETTI("confetti", "Confetti"),
    STRIPES("stripes", "Stripes"),
    HEXAGON("hexagon", "Hexagon"),
    LEAVES("leaves", "Leaves"),
    ;

    companion object {
        fun fromKey(key: String?): CardBackgroundStyle = entries.find { it.key == key } ?: NONE
    }
}

@Composable
fun HabitCardBackground(style: CardBackgroundStyle, color: Color, modifier: Modifier = Modifier, alpha: Float = 0.14f) {
    if (style == CardBackgroundStyle.NONE) return
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val tint = color.copy(alpha = alpha)
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
            CardBackgroundStyle.SPIRAL -> {
                val cx = w * 0.85f
                val cy = h * 0.38f
                val maxR = w * 0.30f
                val turns = 2.2f
                val steps = 64
                val path = Path()
                for (i in 0..steps) {
                    val t = i / steps.toFloat()
                    val angle = t * turns * 2f * PI.toFloat()
                    val r = t * maxR
                    val x = cx + r * cos(angle)
                    val y = cy + r * sin(angle)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, tint, style = stroke)
            }
            CardBackgroundStyle.STARS -> {
                val stars = listOf(Triple(0.68f, 0.24f, 1.0f), Triple(0.90f, 0.42f, 0.65f), Triple(0.80f, 0.65f, 0.5f))
                stars.forEach { (fx, fy, scale) ->
                    drawStar(Offset(w * fx, h * fy), w * 0.11f * scale, w * 0.045f * scale, tint, strokeWidth * 0.7f)
                }
            }
            CardBackgroundStyle.CONFETTI -> {
                val pieces = listOf(
                    Triple(0.62f, 0.22f, 20f),
                    Triple(0.80f, 0.35f, -15f),
                    Triple(0.70f, 0.55f, 40f),
                    Triple(0.92f, 0.60f, -25f),
                    Triple(0.88f, 0.20f, 10f),
                )
                pieces.forEach { (fx, fy, angle) ->
                    rotate(degrees = angle, pivot = Offset(w * fx, h * fy)) {
                        drawRoundRect(
                            color = tint,
                            topLeft = Offset(w * fx - strokeWidth, h * fy - strokeWidth * 1.6f),
                            size = androidx.compose.ui.geometry.Size(strokeWidth * 2f, strokeWidth * 3.2f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(strokeWidth * 0.4f),
                            style = stroke,
                        )
                    }
                }
            }
            CardBackgroundStyle.STRIPES -> {
                repeat(4) { i ->
                    val offset = w * (0.02f + i * 0.10f)
                    drawLine(
                        color = tint,
                        start = Offset(w * 0.55f + offset, h * 1.05f),
                        end = Offset(w * 0.55f + offset + w * 0.25f, -h * 0.05f),
                        strokeWidth = strokeWidth * 0.8f,
                        cap = StrokeCap.Round,
                    )
                }
            }
            CardBackgroundStyle.HEXAGON -> {
                val center = Offset(w * 0.90f, h * 0.35f)
                repeat(2) { i ->
                    drawPolygon(center, w * (0.14f + i * 0.11f), sides = 6, color = tint, strokeWidth = strokeWidth)
                }
            }
            CardBackgroundStyle.LEAVES -> {
                val leaf1 = Path().apply {
                    moveTo(w * 0.60f, h * 0.70f)
                    cubicTo(w * 0.68f, h * 0.55f, w * 0.82f, h * 0.50f, w * 0.94f, h * 0.28f)
                    cubicTo(w * 0.86f, h * 0.48f, w * 0.78f, h * 0.62f, w * 0.60f, h * 0.70f)
                    close()
                }
                drawPath(leaf1, tint, style = stroke)
                val leaf2 = Path().apply {
                    moveTo(w * 0.72f, h * 0.85f)
                    cubicTo(w * 0.80f, h * 0.72f, w * 0.92f, h * 0.68f, w * 1.00f, h * 0.50f)
                    cubicTo(w * 0.94f, h * 0.68f, w * 0.86f, h * 0.80f, w * 0.72f, h * 0.85f)
                    close()
                }
                drawPath(leaf2, tint, style = stroke)
            }
            CardBackgroundStyle.NONE -> Unit
        }
    }
}

private fun DrawScope.drawStar(center: Offset, outerRadius: Float, innerRadius: Float, color: Color, strokeWidth: Float) {
    val path = Path()
    val points = 5
    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (PI.toFloat() / points) * i - PI.toFloat() / 2f
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
}

private fun DrawScope.drawPolygon(center: Offset, radius: Float, sides: Int, color: Color, strokeWidth: Float) {
    val path = Path()
    for (i in 0 until sides) {
        val angle = (2 * PI.toFloat() / sides) * i - PI.toFloat() / 2f
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
}
