package dev.dhev.nines.ui.habits
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.scoring.StreakRun
import dev.dhev.nines.ui.theme.HabitPalette
import dev.dhev.nines.ui.theme.Spacing
import dev.dhev.nines.ui.theme.contrastingIconColor
import dev.dhev.nines.ui.theme.rememberHabitColors
import dev.dhev.nines.ui.theme.rememberHabitColorsForPalette
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun InsightsSection(uiState: HabitDetailUiState, accent: Color) {
    val rows = buildList<@Composable () -> Unit> {
        uiState.recordProgressLabel?.let { label ->
            add { RecordProgressRow(label = label, accent = accent) }
        }
        if (uiState.weekdayRates.size == 7) {
            add { WeekdayBreakdownRow(rates = uiState.weekdayRates, accent = accent) }
        }
        if (uiState.type == HabitType.NUMERICAL && uiState.targetKind != null) {
            val target = uiState.targetValue
            val recentDays = uiState.history.takeLast(14)
            if (target != null && target > 0 && recentDays.size >= 7) {
                add {
                    TargetProgressRow(
                        days = recentDays,
                        target = target,
                        targetKind = uiState.targetKind,
                        accent = accent,
                    )
                }
            }
        }
        uiState.momentumDeltaPoints?.let { delta ->
            add { MomentumRow(deltaPoints = delta, accent = accent) }
        }
        uiState.typicalTimeOfDay?.let { timeOfDay ->
            add { TypicalTimeRow(timeOfDay = timeOfDay, accent = accent) }
        }
        if (uiState.numericTotal != null && uiState.numericAverage != null) {
            add {
                NumericTotalsRow(
                    total = uiState.numericTotal,
                    average = uiState.numericAverage,
                    unit = uiState.unit,
                    accent = accent,
                )
            }
        }
        uiState.longestGap?.let { gap ->
            add { LongestGapRow(gap = gap, accent = accent, today = uiState.today) }
        }
    }
    Spacer(Modifier.height(Spacing.extraLarge))
    Text(
        text = "Insights",
        style = MaterialTheme.typography.titleMediumEmphasized,
    )
    Spacer(Modifier.height(Spacing.default))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLargeIncreased,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.default),
            verticalArrangement = Arrangement.spacedBy(Spacing.default),
        ) {
            if (rows.isEmpty()) {
                EmptyInsightsState(accent = accent)
            } else {
                rows.forEachIndexed { index, row ->
                    if (index > 0) HorizontalDivider(color = accent.copy(alpha = 0.12f))
                    row()
                }
            }
        }
    }
}

@Composable
private fun RecordProgressRow(label: String, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Spacing.small))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NumericTotalsRow(total: Double, average: Double, unit: String?, accent: Color) {
    val unitSuffix = unit?.let { " $it" } ?: ""
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Functions,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = "${formatValue(total)}$unitSuffix total · averaging ${formatValue(average)}$unitSuffix per entry",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LongestGapRow(gap: StreakRun, accent: Color, today: LocalDate) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.EventBusy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = "Longest gap: ${gap.length} days (${streakRangeLabel(gap, today)})",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun WeekdayBreakdownRow(rates: List<Double>, accent: Color) {
    // Color/alpha, not shape or position, so this fades in rather than springing - the
    // library's own rule is spatial for anything that moves or resizes, effects for anything
    // that's purely a color change, and a fade reads calmer for seven cells appearing together.
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateIn = true }
    Column {
        Text(
            text = "By day of week",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.small))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val labels = listOf("M", "T", "W", "T", "F", "S", "S")
            rates.forEachIndexed { index, rate ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val targetColor = accent.copy(alpha = (0.12f + rate.toFloat() * 0.75f).coerceAtMost(1f))
                    val animatedColor by animateColorAsState(
                        targetValue = if (animateIn) targetColor else accent.copy(alpha = 0f),
                        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                        label = "weekdayCell",
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(animatedColor),
                    )
                    Spacer(Modifier.height(Spacing.extraSmall))
                    Text(
                        text = labels[index],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        val strongest = rates.indices.maxByOrNull { rates[it] }
        val weakest = rates.indices.minByOrNull { rates[it] }
        if (strongest != null && weakest != null && strongest != weakest && rates[strongest] > rates[weakest]) {
            Spacer(Modifier.height(Spacing.small))
            Text(
                text = "Strongest on ${weekdayName(strongest)}s, weakest on ${weekdayName(weakest)}s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun weekdayName(index: Int): String = java.time.DayOfWeek.of(index + 1).getDisplayName(TextStyle.FULL, Locale.getDefault())

private val TargetChartHeight = 56.dp

// Bars are allowed to rise 25% above the target line before clipping, so a day that blew past
// goal reads as visibly taller instead of clipping flush with every day that merely met it.
private const val TARGET_CHART_MAX_SCALE = 1.25f

/**
 * The other half of a numeric habit's story that a plain daily number never shows: not just
 * whether you hit target, but by how much or how little on each of the last two weeks. A
 * reference line at the target height turns "12/20, 18/20, 22/20" into a shape you can read at
 * a glance instead of a column of fractions.
 */
@Composable
private fun TargetProgressRow(days: List<DayCell>, target: Double, targetKind: TargetKind, accent: Color) {
    // AT_MOST inverts what "met" means (staying under a ceiling, not reaching a floor) but not
    // the bar mechanics - a bar that reaches the line still means "landed exactly on target"
    // either way, only which side of it counts as success flips.
    fun isMet(value: Double) = if (targetKind == TargetKind.AT_MOST) value <= target else value >= target
    val metCount = days.count { isMet(it.numericValue ?: 0.0) }
    // One coordinated grow-in for the whole strip, not fourteen individually-timed bars - a
    // bar chart reading in together sells "this is one chart appearing," while staggering each
    // bar reads as list-item motion, which the library itself never applies to repeated rows.
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateIn = true }
    Column {
        Text(
            text = "Recent days vs. target",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.small))
        Box(modifier = Modifier.fillMaxWidth().height(TargetChartHeight)) {
            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(y = -(TargetChartHeight / TARGET_CHART_MAX_SCALE)),
                color = accent.copy(alpha = 0.45f),
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                days.forEach { day ->
                    val ratio = ((day.numericValue ?: 0.0) / target).toFloat().coerceIn(0f, TARGET_CHART_MAX_SCALE)
                    val metTarget = isMet(day.numericValue ?: 0.0)
                    val animatedRatio by animateFloatAsState(
                        targetValue = if (animateIn) ratio else 0f,
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                        label = "targetBar",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            // The spatial spring is underdamped and overshoots by design, but
                            // fillMaxHeight requires its fraction to stay within 0f..1f - clamp
                            // the rare overshoot past 1.0 rather than let it crash mid-animation.
                            .fillMaxHeight((animatedRatio / TARGET_CHART_MAX_SCALE).coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    metTarget -> accent

                                    // Going over a ceiling is a violation, not just a shortfall -
                                    // worth a clearer color than the muted-accent "didn't get
                                    // there yet" used for an AT_LEAST day that fell short.
                                    targetKind == TargetKind.AT_MOST -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)

                                    else -> accent.copy(alpha = 0.35f)
                                },
                            ),
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.small))
        Text(
            text = if (targetKind == TargetKind.AT_MOST) {
                "Stayed within the limit on $metCount of the last ${days.size} days"
            } else {
                "Hit target on $metCount of the last ${days.size} days"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MomentumRow(deltaPoints: Double, accent: Color) {
    val roundedDelta = kotlin.math.abs(deltaPoints).roundToInt()
    // A delta that rounds to zero isn't "0% more consistent" (which reads like a broken
    // template), it's just flat - a distinct third state, not a degenerate case of up or down.
    val direction = when {
        roundedDelta == 0 -> null
        deltaPoints > 0 -> true
        else -> false
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = when (direction) {
                true -> Icons.AutoMirrored.Filled.TrendingUp
                false -> Icons.AutoMirrored.Filled.TrendingDown
                null -> Icons.AutoMirrored.Filled.TrendingFlat
            },
            contentDescription = null,
            tint = when (direction) {
                true -> accent
                false -> MaterialTheme.colorScheme.error
                null -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = when (direction) {
                true -> "$roundedDelta% more consistent than the previous 30 days"
                false -> "$roundedDelta% less consistent than the previous 30 days"
                null -> "About as consistent as the previous 30 days"
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TypicalTimeRow(timeOfDay: String, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = "Usually logged in the $timeOfDay",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Plain text now, no chip background — these live inside the hero surface itself (see the
 * comment above it), so wrapping each one in its own tonal box on top of that would just be
 * another layer of boxes-within-boxes. The surface itself already gives them a home. */
