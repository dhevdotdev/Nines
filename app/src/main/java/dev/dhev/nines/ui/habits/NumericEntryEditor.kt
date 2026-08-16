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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.scoring.FrequencyTargetKind
import dev.dhev.nines.ui.theme.HabitColors
import dev.dhev.nines.ui.theme.Spacing
import dev.dhev.nines.ui.theme.contrastingIconColor
import dev.dhev.nines.ui.theme.rememberHabitColors
import kotlin.math.roundToInt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NumericEntryEditor(
    habitName: String,
    iconKey: String,
    colors: HabitColors,
    initialValue: Double?,
    targetValue: Double?,
    targetKind: TargetKind?,
    unit: String?,
    selectedDate: LocalDate,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableDoubleStateOf(initialValue ?: 0.0) }
    var text by remember { mutableStateOf(initialValue?.let { formatValue(it) } ?: "") }

    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "editValueNumber",
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateLabel = selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    val targetLabel = targetValue?.let {
        val kind = if (targetKind == TargetKind.AT_MOST) "at most" else "at least"
        "Target: $kind ${formatValue(it)}${unit?.let { value -> " $value" } ?: ""}"
    } ?: "No target set"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.large)
                .padding(bottom = Spacing.large),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.default),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialShapes.Cookie9Sided.toShape())
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
                Column {
                    Text(text = "Edit value", style = MaterialTheme.typography.titleLargeEmphasized)
                    Text(text = habitName, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.large))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatValue(animatedValue.toDouble()),
                    style = MaterialTheme.typography.displaySmallEmphasized,
                    color = colors.accent,
                )
                unit?.let {
                    Spacer(Modifier.width(Spacing.small))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
            Text(
                text = targetLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.default))
            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    val numericText = newText.filter { it.isDigit() || it == '.' }
                    if (numericText.count { it == '.' } <= 1) {
                        text = numericText
                        numericText.toDoubleOrNull()?.let { value = it }
                    }
                },
                singleLine = true,
                label = { Text("Value for $dateLabel") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                isError = text.isNotEmpty() && text.toDoubleOrNull() == null,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.default))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(Spacing.small))
                Button(
                    onClick = { text.toDoubleOrNull()?.let(onConfirm) },
                    enabled = text.isNotEmpty() && text.toDoubleOrNull() != null,
                ) { Text("Save") }
            }
        }
    }
}

/** Shared with the detail screen (same package) - "12" for a whole number, "12.5" otherwise. */
