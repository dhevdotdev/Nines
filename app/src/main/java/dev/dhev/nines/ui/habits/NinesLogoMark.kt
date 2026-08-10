package dev.dhev.nines.ui.habits

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * The app's own mark, small enough to sit next to the wordmark: a 3x3 grid of dots echoing the
 * history-strip dots used throughout the app (see HistoryStrip in HabitRow) — eight open rings
 * and a ninth filled solid, the same design used for the launcher icon.
 */
@Composable
fun NinesLogoMark(modifier: Modifier = Modifier, tint: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val step = size.minDimension / 2.7f
        val dotRadius = size.minDimension / 8.3f
        val strokeWidth = size.minDimension / 14f
        val stroke = Stroke(width = strokeWidth)
        val startX = (size.width - step * 2) / 2f
        val startY = (size.height - step * 2) / 2f

        for (row in 0 until 3) {
            for (col in 0 until 3) {
                val center = Offset(startX + col * step, startY + row * step)
                val isLast = row == 2 && col == 2
                if (isLast) {
                    drawCircle(color = tint, radius = dotRadius * 1.15f, center = center)
                } else {
                    drawCircle(color = tint, radius = dotRadius, center = center, style = stroke)
                }
            }
        }
    }
}
