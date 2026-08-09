package dev.dhev.nines.ui.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ripple
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.ui.theme.HabitColors
import dev.dhev.nines.ui.theme.Spacing
import dev.dhev.nines.ui.theme.contrastingIconColor
import dev.dhev.nines.ui.theme.rememberHabitColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HabitRow(
    item: HabitListItem,
    onToggle: () -> Unit,
    onIncrement: (Double) -> Unit,
    onDecrement: (Double) -> Unit,
    onSetValue: (Double) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = rememberHabitColors(Color(item.color))
    val cardContainer = lerp(MaterialTheme.colorScheme.surfaceContainer, colors.accent, 0.10f)
    var showValueDialog by remember { mutableStateOf(false) }

    // Tapping the card opens the detail screen; edit lives on the swipe-right gesture instead
    // (see HabitsScreen), delete on swipe-left.
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        colors = CardDefaults.cardColors(
            containerColor = cardContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box {
            HabitCardBackground(
                style = CardBackgroundStyle.fromKey(item.backgroundStyleKey),
                color = colors.accent,
                modifier = Modifier.matchParentSize(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.large, vertical = Spacing.default),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.default),
            ) {
            // The wavy ring itself communicates reliability (score) — a distinctly MD3
            // Expressive stand-in for a flat "X% reliable" text pill.
            Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator(
                    progress = { item.currentScore.toFloat() },
                    modifier = Modifier.size(56.dp),
                    color = colors.accent,
                    trackColor = colors.container,
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = HabitIcons.iconFor(item.iconKey),
                        contentDescription = null,
                        tint = contrastingIconColor(colors.accent),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // weight(1f) is what keeps a long name/value from ever pushing the trailing button
            // off-card — every child below must wrap or ellipsize rather than grow unbounded.
            // Deliberately just two lines: name, then one compact status line. Piling on a
            // score ring AND lettered history AND several chip-pills AND an accent stripe read
            // as clutter — real habit trackers keep the row itself minimal and push detail
            // (frequency, exact history) into the edit sheet instead.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Spacing.small))
                // Dots are a graphic element, not text — keeping them on their own line means
                // nothing has to fight a text glyph for baseline alignment. Streak/value below
                // are pure text on their own line for the same reason.
                HistoryStrip(days = item.recentDays, accent = colors.accent)
                val valueText = if (item.type == HabitType.NUMERICAL && item.targetValue != null) {
                    "${formatValue(item.currentValue ?: 0.0)}/${formatValue(item.targetValue)}${item.unit?.let { " $it" } ?: ""}"
                } else {
                    null
                }
                if (item.currentStreak > 0 || valueText != null) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        if (item.currentStreak > 0) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "${item.currentStreak}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        valueText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.accent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(),
                                ) { showValueDialog = true },
                            )
                        }
                    }
                }
            }

            if (item.type == HabitType.NUMERICAL) {
                NumericProgressButton(
                    metTarget = item.completedToday,
                    onIncrement = { onIncrement(defaultIncrement(item.targetValue)) },
                    onDecrement = { onDecrement(defaultIncrement(item.targetValue)) },
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

    if (showValueDialog) {
        EditValueDialog(
            habitName = item.name,
            iconKey = item.iconKey,
            colors = colors,
            initialValue = item.currentValue ?: 0.0,
            targetValue = item.targetValue,
            unit = item.unit,
            onConfirm = { value ->
                onSetValue(value)
                showValueDialog = false
            },
            onDismiss = { showValueDialog = false },
        )
    }
}

/**
 * Manual entry — tapping the value directly to set an exact number, uhabits-style, rather than
 * only being able to nudge it with +/- taps. A slider and quick-add chips do the fun/fast part
 * of the job; the typed field underneath is there for when you want to be exact, not the star.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditValueDialog(
    habitName: String,
    iconKey: String,
    colors: HabitColors,
    initialValue: Double,
    targetValue: Double?,
    unit: String?,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableDoubleStateOf(initialValue) }
    var text by remember { mutableStateOf(formatValue(initialValue)) }

    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "editValueNumber",
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLargeIncreased,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier
                    .padding(Spacing.large)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = HabitIcons.iconFor(iconKey),
                        contentDescription = null,
                        tint = contrastingIconColor(colors.accent),
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.height(Spacing.small))
                Text(text = habitName, style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(Spacing.large))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatValue(animatedValue.toDouble()),
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.accent,
                    )
                    unit?.let {
                        Spacer(Modifier.width(Spacing.small))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.large))
                OutlinedTextField(
                    value = text,
                    // Digits only, filtered as you type — a decimal point can't be entered at
                    // all rather than being accepted then rejected on save.
                    onValueChange = { newText ->
                        val digitsOnly = newText.filter { it.isDigit() }
                        text = digitsOnly
                        digitsOnly.toDoubleOrNull()?.let { value = it }
                    },
                    singleLine = true,
                    label = { Text("Exact value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    isError = text.toDoubleOrNull() == null,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.large))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(Spacing.small))
                    Button(
                        onClick = { text.toDoubleOrNull()?.let(onConfirm) },
                        enabled = text.toDoubleOrNull() != null,
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/** A row of small dots — one per recent day — giving an at-a-glance history, uhabits-style. */
@Composable
private fun HistoryStrip(days: List<DayCell>, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        days.forEach { day ->
            val (fill, alpha) = when (day.state) {
                EntryState.COMPLETED -> accent to 1f
                EntryState.IN_PROGRESS -> accent to 0.4f
                EntryState.SKIPPED, null -> MaterialTheme.colorScheme.outlineVariant to 1f
            }
            val targetColor = fill.copy(alpha = alpha)
            if (day.isToday) {
                // Only today's dot ever changes after first composition (toggling flips its
                // state live), so only it needs an animated state — the other four are static
                // once rendered and don't need the overhead of their own animatable.
                val animatedColor by animateColorAsState(
                    targetValue = targetColor,
                    animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                    label = "historyDotColor",
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(animatedColor),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(targetColor),
                )
            }
        }
    }
}

private fun formatValue(value: Double): String =
    if (value == value.roundToInt().toDouble()) value.roundToInt().toString() else "%.1f".format(value)

private fun defaultIncrement(target: Double?): Double {
    if (target == null || target <= 0) return 1.0
    val raw = target / 10.0
    return if (raw < 1.0) 1.0 else raw.roundToInt().toDouble()
}
