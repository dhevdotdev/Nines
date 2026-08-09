package dev.dhev.nines.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.ui.theme.Spacing
import dev.dhev.nines.ui.theme.contrastingIconColor
import dev.dhev.nines.ui.theme.rememberHabitColors
import kotlin.math.roundToInt

@Composable
fun HabitRow(
    item: HabitListItem,
    onToggle: () -> Unit,
    onIncrement: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = rememberHabitColors(Color(item.color))
    val cardContainer = lerp(MaterialTheme.colorScheme.surfaceContainer, colors.accent, 0.10f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        colors = CardDefaults.cardColors(
            containerColor = cardContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large, vertical = Spacing.default),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.default),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = HabitIcons.iconFor(item.iconKey),
                    contentDescription = null,
                    tint = contrastingIconColor(colors.accent),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleLargeEmphasized)
                Spacer(Modifier.height(Spacing.extraSmall))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                    if (item.type == HabitType.NUMERICAL && item.targetValue != null) {
                        ValuePill(
                            text = "${formatValue(item.currentValue ?: 0.0)}/${formatValue(item.targetValue)}${item.unit?.let { " $it" } ?: ""}",
                            container = colors.container,
                            onContainer = colors.onContainer,
                        )
                    }
                    ValuePill(
                        text = "${(item.currentScore * 100).roundToInt()}% reliable",
                        container = colors.container,
                        onContainer = colors.onContainer,
                    )
                    frequencyLabel(item.targetNumerator, item.targetDenominator)?.let { label ->
                        ValuePill(text = label, container = colors.container, onContainer = colors.onContainer)
                    }
                }
            }

            if (item.type == HabitType.NUMERICAL) {
                NumericProgressButton(
                    metTarget = item.completedToday,
                    onIncrement = { onIncrement(defaultIncrement(item.targetValue)) },
                    accentColor = colors.accent,
                )
            } else {
                CheckOffButton(
                    completed = item.completedToday,
                    onToggle = onToggle,
                    accentColor = colors.accent,
                )
            }
        }
    }
}

@Composable
private fun ValuePill(text: String, container: Color, onContainer: Color) {
    Surface(shape = MaterialTheme.shapes.small, color = container) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = onContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

private fun formatValue(value: Double): String =
    if (value == value.roundToInt().toDouble()) value.roundToInt().toString() else "%.1f".format(value)

private fun defaultIncrement(target: Double?): Double {
    if (target == null || target <= 0) return 1.0
    val raw = target / 10.0
    return if (raw < 1.0) 1.0 else raw.roundToInt().toDouble()
}

/** null for daily (the common case, not worth a chip); otherwise a short "3x/week" style label. */
private fun frequencyLabel(numerator: Int, denominator: Int): String? = when {
    numerator == 1 && denominator == 1 -> null
    denominator == 7 -> "${numerator}x/week"
    numerator == 1 -> "every ${denominator}d"
    else -> "$numerator/${denominator}d"
}
