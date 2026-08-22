package dev.dhev.nines.ui.habits

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

enum class MetricGuidanceKey {
    SCORE,
    STREAK,
    TOTAL,
    FREQUENCY,
    TARGET,
    MOMENTUM,
    GAP,
    WEEKDAY,
    RECORD,
    TIME,
    NUMERIC_TOTALS,
}

data class MetricGuidance(
    val key: MetricGuidanceKey,
    val title: String,
    val explanation: String,
)

fun metricGuidance(key: MetricGuidanceKey): MetricGuidance = when (key) {
    MetricGuidanceKey.SCORE -> MetricGuidance(
        key,
        "Consistency score",
        "A percentage showing how reliably this habit has been completed recently. Recent entries matter more, while older misses gradually matter less.",
    )
    MetricGuidanceKey.STREAK -> MetricGuidance(
        key,
        "Streaks",
        "Current streak is the number of consecutive successful days ending at the selected date. Best streak is your longest successful run so far.",
    )
    MetricGuidanceKey.TOTAL -> MetricGuidance(
        key,
        "Total completions",
        "The number of completed entries recorded for this habit across its available history.",
    )
    MetricGuidanceKey.FREQUENCY -> MetricGuidance(
        key,
        "Frequency progress",
        "For a schedule such as 3 times per week, this shows how many successful entries you have made toward the current cycle target.",
    )
    MetricGuidanceKey.TARGET -> MetricGuidance(
        key,
        "Target progress",
        "This compares your recent measurements with the target you set. For an at-most target, staying below the line is success; for an at-least target, reaching it is success.",
    )
    MetricGuidanceKey.MOMENTUM -> MetricGuidance(
        key,
        "Momentum",
        "Momentum compares your recent consistency with the previous 30 days. It helps show whether your routine is improving, holding steady, or slipping.",
    )
    MetricGuidanceKey.GAP -> MetricGuidance(
        key,
        "Longest gap",
        "The longest stretch between successful entries in the available history. A gap is not a judgment; it shows where a routine had the most space between attempts.",
    )
    MetricGuidanceKey.WEEKDAY -> MetricGuidance(
        key,
        "By day of week",
        "These blocks compare your success rate on each weekday, so you can see which days naturally support this habit and which need more attention.",
    )
    MetricGuidanceKey.RECORD -> MetricGuidance(
        key,
        "Record progress",
        "This compares your current streak with your best streak and tells you whether you are matching it or how many days remain to beat it.",
    )
    MetricGuidanceKey.TIME -> MetricGuidance(
        key,
        "Typical time",
        "The time of day when you most often record this habit. It describes your existing pattern; it does not create a reminder.",
    )
    MetricGuidanceKey.NUMERIC_TOTALS -> MetricGuidance(
        key,
        "Numeric totals",
        "Total is the sum of your recorded measurements. Average is the typical amount per entry, calculated from entries with a value.",
    )
}

@Composable
fun MetricGuidanceButton(
    guidance: MetricGuidance,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .semantics { contentDescription = "Explain ${guidance.title}" },
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            modifier = Modifier,
        )
    }
}

@Composable
fun MetricGuidanceDialog(
    guidance: MetricGuidance,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(guidance.title) },
        text = { Text(guidance.explanation) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it") }
        },
    )
}
