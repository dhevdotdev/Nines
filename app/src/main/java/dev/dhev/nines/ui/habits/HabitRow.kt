package dev.dhev.nines.ui.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
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
    onEdit: () -> Unit,
    onClick: () -> Unit,
    actionsEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    dragHandle: (@Composable () -> Unit)? = null,
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
                    .padding(horizontal = Spacing.large, vertical = Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.default),
            ) {
                // Leads the row rather than trailing it - the handle stays reachable without
                // competing with the action group.
                dragHandle?.invoke()
                // The wavy ring communicates reliability (Score) and gives every habit a
                // persistent expressive identity.
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CircularWavyProgressIndicator(
                        progress = { item.currentScore.toFloat() },
                        modifier = Modifier.size(48.dp),
                        color = colors.accent,
                        trackColor = colors.container,
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = HabitIcons.iconFor(item.iconKey),
                            contentDescription = null,
                            tint = contrastingIconColor(colors.accent),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    SelectedDateStatus(
                        item = item,
                        accent = colors.accent,
                    )
                }

                HabitActionGroup(
                    item = item,
                    accent = colors.accent,
                    enabled = actionsEnabled,
                    onPrimaryAction = if (item.type == HabitType.NUMERICAL) {
                        { onIncrement(defaultIncrement(item.targetValue)) }
                    } else {
                        onToggle
                    },
                    onDecrement = if (item.type == HabitType.NUMERICAL) {
                        { onDecrement(defaultIncrement(item.targetValue)) }
                    } else {
                        null
                    },
                    onSecondaryAction = if (item.type == HabitType.NUMERICAL) {
                        { showValueDialog = true }
                    } else {
                        onEdit
                    },
                    secondaryDescription = if (item.type == HabitType.NUMERICAL) {
                        "Set ${item.name} value"
                    } else {
                        "Edit ${item.name}"
                    },
                )
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SelectedDateStatus(
    item: HabitListItem,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val hasEntry = item.type == HabitType.NUMERICAL && item.currentValue != null
    val isMeaningfullyComplete = item.completed || hasEntry
    val markerColor by animateColorAsState(
        targetValue = if (isMeaningfullyComplete) accent else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "selectedDateStatusMarker",
    )
    val statusText = when (item.type) {
        HabitType.BOOLEAN -> if (item.completed) "Done" else "Not logged"
        HabitType.NUMERICAL -> when {
            item.targetValue != null && item.currentValue != null -> {
                "${formatValue(item.currentValue)}/${formatValue(item.targetValue)}${item.unit?.let { " $it" } ?: ""}"
            }
            item.targetValue != null -> "Not logged · target ${formatValue(item.targetValue)}${item.unit?.let { " $it" } ?: ""}"
            item.currentValue != null -> "${formatValue(item.currentValue)} · No target"
            else -> "Not logged"
        }
    }
    val markerShape = if (isMeaningfullyComplete) {
        MaterialShapes.Cookie9Sided.toShape()
    } else {
        CircleShape
    }

    Column(
        modifier = modifier.heightIn(min = 44.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(markerShape)
                    .background(markerColor),
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMeaningfullyComplete) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (item.currentStreak > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "${item.currentStreak} day streak",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HabitActionGroup(
    item: HabitListItem,
    accent: Color,
    enabled: Boolean,
    onPrimaryAction: () -> Unit,
    onDecrement: (() -> Unit)?,
    onSecondaryAction: () -> Unit,
    secondaryDescription: String,
) {
    val primaryInteractionSource = remember { MutableInteractionSource() }
    val secondaryInteractionSource = remember { MutableInteractionSource() }
    val primaryDescription = if (item.type == HabitType.NUMERICAL) {
        "Add ${formatValue(defaultIncrement(item.targetValue))} to ${item.name}; hold to subtract"
    } else if (item.completed) {
        "Mark ${item.name} incomplete"
    } else {
        "Mark ${item.name} complete"
    }
    ButtonGroup(
        overflowIndicator = { },
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        customItem(
            buttonGroupContent = {
                if (item.type == HabitType.NUMERICAL) {
                    Box(
                        modifier = Modifier
                            .size(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        NumericProgressButton(
                            metTarget = item.completed,
                            onIncrement = onPrimaryAction,
                            onDecrement = onDecrement ?: {},
                            accentColor = accent,
                            contentDescription = primaryDescription,
                            enabled = enabled,
                            interactionSource = primaryInteractionSource,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CheckOffButton(
                            completed = item.completed,
                            onToggle = onPrimaryAction,
                            accentColor = accent,
                            contentDescription = if (item.completed) {
                                "Mark ${item.name} incomplete"
                            } else {
                                "Mark ${item.name} complete"
                            },
                            enabled = enabled,
                            interactionSource = primaryInteractionSource,
                        )
                    }
                }
            },
            menuContent = {},
        )
        customItem(
            buttonGroupContent = {
                OutlinedIconButton(
                    onClick = onSecondaryAction,
                    enabled = enabled,
                    interactionSource = secondaryInteractionSource,
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = secondaryDescription,
                    )
                }
            },
            menuContent = {},
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

/** Shared with the detail screen (same package) - "12" for a whole number, "12.5" otherwise. */
fun formatValue(value: Double): String =
    if (value == value.roundToInt().toDouble()) value.roundToInt().toString() else "%.1f".format(value)

fun defaultIncrement(target: Double?): Double {
    if (target == null || target <= 0) return 1.0
    val raw = target / 10.0
    return if (raw < 1.0) 1.0 else raw.roundToInt().toDouble()
}
