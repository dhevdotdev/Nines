package dev.dhev.nines.ui.habits

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * A stem and two leaves, each drawing themselves in on their own stroke, staggered stem-then-
 * leaves like a hand-drawn illustration rather than fading or scaling in as a static icon would
 * — the "growth" a habit tracker's empty state is actually about. Starts automatically on first
 * composition; there's nothing for the caller to drive.
 */
@Composable
fun DrawnSproutIllustration(color: Color, modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val stemProgress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "sproutStem",
    )
    val leftLeafProgress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 550, delayMillis = 450, easing = FastOutSlowInEasing),
        label = "sproutLeftLeaf",
    )
    val rightLeafProgress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 550, delayMillis = 650, easing = FastOutSlowInEasing),
        label = "sproutRightLeaf",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = size.minDimension * 0.05f

        val stem = Path().apply {
            moveTo(w * 0.5f, h * 0.85f)
            cubicTo(w * 0.47f, h * 0.65f, w * 0.53f, h * 0.45f, w * 0.5f, h * 0.22f)
        }
        drawTrimmedStroke(stem, stemProgress, color, strokeWidth)

        val leftLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.58f)
            cubicTo(w * 0.30f, h * 0.52f, w * 0.18f, h * 0.38f, w * 0.26f, h * 0.22f)
            cubicTo(w * 0.40f, h * 0.30f, w * 0.49f, h * 0.44f, w * 0.50f, h * 0.58f)
            close()
        }
        drawTrimmedStroke(leftLeaf, leftLeafProgress, color, strokeWidth)

        val rightLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.46f)
            cubicTo(w * 0.68f, h * 0.41f, w * 0.80f, h * 0.28f, w * 0.73f, h * 0.13f)
            cubicTo(w * 0.60f, h * 0.20f, w * 0.51f, h * 0.32f, w * 0.50f, h * 0.46f)
            close()
        }
        drawTrimmedStroke(rightLeaf, rightLeafProgress, color, strokeWidth)
    }
}

private fun DrawScope.drawTrimmedStroke(path: Path, progress: Float, color: Color, strokeWidth: Float) {
    if (progress <= 0f) return
    val measure = PathMeasure().apply { setPath(path, forceClosed = false) }
    val trimmed = Path()
    measure.getSegment(0f, measure.length * progress, trimmed, startWithMoveTo = true)
    drawPath(
        path = trimmed,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}
