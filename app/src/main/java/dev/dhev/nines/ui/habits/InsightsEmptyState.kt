package dev.dhev.nines.ui.habits

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.dhev.nines.ui.theme.Spacing

/** The intentional first-run state for Insights, before any metric has enough data to show. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun EmptyInsightsState(accent: Color) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "insightsDots",
    )

    Row(
        modifier =
        Modifier
            .semantics { contentDescription = "Insights patterns are waiting" }
            .padding(Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.default),
    ) {
        Box(
            modifier =
            Modifier
                .size(56.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(accent.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            PatternDots(progress = progress, accent = accent)
        }
        Column {
            Text("Patterns are waiting", style = MaterialTheme.typography.titleSmallEmphasized)
            Spacer(Modifier.size(Spacing.extraSmall))
            Text(
                "Keep logging and Nines will connect the dots.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PatternDots(
    progress: Float,
    accent: Color,
) {
    Canvas(Modifier.size(40.dp)) {
        // The dots settle into a balanced constellation, not a chart: there is no axis, trend,
        // or implied score before the habit has enough history to produce one.
        val start =
            listOf(
                Offset(2f, 2f),
                Offset(36f, 4f),
                Offset(5f, 38f),
                Offset(38f, 36f),
                Offset(20f, 2f),
                Offset(2f, 20f),
                Offset(38f, 20f),
                Offset(20f, 38f),
            )
        val settled =
            listOf(
                Offset(8f, 9f),
                Offset(20f, 7f),
                Offset(32f, 10f),
                Offset(10f, 21f),
                Offset(24f, 19f),
                Offset(34f, 25f),
                Offset(16f, 33f),
                Offset(29f, 34f),
            )
        start.zip(settled).forEachIndexed { index, (from, to) ->
            val dotProgress = ((progress * 1.15f) - index * 0.035f).coerceIn(0f, 1f)
            val point =
                Offset(
                    x = from.x + (to.x - from.x) * dotProgress,
                    y = from.y + (to.y - from.y) * dotProgress,
                )
            drawCircle(accent, radius = 2.8f, center = point)
        }
    }
}
