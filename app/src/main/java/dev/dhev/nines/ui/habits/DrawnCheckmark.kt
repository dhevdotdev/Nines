package dev.dhev.nines.ui.habits

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * A checkmark that draws itself along its own stroke rather than fading or popping in — the
 * hand-drawn-illustration feel Material's own marketing/empty-state art is known for, applied to
 * a everyday control. [progress] 0f draws nothing, 1f draws the complete mark; animate it
 * yourself (see [CheckOffButton]) rather than animating internally, so callers control timing.
 */
@Composable
fun DrawnCheckmark(color: Color, progress: Float, modifier: Modifier = Modifier) {
    if (progress <= 0f) return
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val fullPath = Path().apply {
            moveTo(w * 0.20f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.72f)
            lineTo(w * 0.80f, h * 0.28f)
        }
        // PathMeasure is Compose's real tool for "draw the first N% of this path" — extract just
        // that leading segment into a fresh path and draw only it, re-cut every frame as
        // progress advances.
        val measure = PathMeasure().apply { setPath(fullPath, forceClosed = false) }
        val trimmedPath = Path()
        measure.getSegment(0f, measure.length * progress, trimmedPath, startWithMoveTo = true)
        drawPath(
            path = trimmedPath,
            color = color,
            style = Stroke(
                width = size.minDimension * 0.16f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}
