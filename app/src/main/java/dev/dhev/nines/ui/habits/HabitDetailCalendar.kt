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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
fun ExpandedMonthDialog(
    month: YearMonth,
    byDate: Map<LocalDate, DayCell>,
    habitType: HabitType,
    unit: String?,
    today: LocalDate,
    accent: Color,
    onDayClick: (DayCell) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLargeIncreased,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(modifier = Modifier.padding(Spacing.large).fillMaxWidth()) {
                Text(
                    text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " ${month.year}",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
                Spacer(Modifier.height(Spacing.default))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                        Box(modifier = Modifier.size(ExpandedDayCellSize), contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.extraSmall))

                val firstOfMonth = month.atDay(1)
                val leadingBlanks = firstOfMonth.dayOfWeek.value % 7
                val cells = buildList {
                    repeat(leadingBlanks) { add(null) }
                    for (day in 1..month.lengthOfMonth()) add(month.atDay(day))
                    while (size % 7 != 0) add(null)
                }

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                    cells.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            week.forEach { date ->
                                val cell = date?.let { byDate[it] }
                                val enabled = date != null && !date.isAfter(today)
                                val (fill, cellAlpha) = when (cell?.state) {
                                    EntryState.COMPLETED -> accent to 1f
                                    EntryState.IN_PROGRESS -> accent to 0.4f
                                    EntryState.SKIPPED, null -> MaterialTheme.colorScheme.surfaceContainerHighest to 0.6f
                                }
                                Box(
                                    modifier = Modifier
                                        .size(ExpandedDayCellSize)
                                        .clip(CircleShape)
                                        .background(fill.copy(alpha = if (date == null) 0f else cellAlpha))
                                        .then(
                                            if (date == today) {
                                                val ringColor = if (cell?.state == EntryState.COMPLETED) {
                                                    contrastingIconColor(accent)
                                                } else {
                                                    accent
                                                }
                                                Modifier.border(1.5.dp, ringColor, CircleShape)
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(),
                                            enabled = enabled,
                                        ) {
                                            date?.let { onDayClick(cell ?: DayCell(date = it, state = null, isToday = it == today)) }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (date != null) {
                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (cell?.state == EntryState.COMPLETED) {
                                                contrastingIconColor(accent)
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.default))
                Text(
                    text = if (habitType == HabitType.NUMERICAL) {
                        "Tap a day to log or edit an entry."
                    } else {
                        "Tap a day to mark it done or not done."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.default))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}
private val ExpandedDayCellSize = 36.dp

/** Nested on top of [ExpandedMonthDialog] when a day is tapped for a numeric habit — the only
 * habit type where "tap to edit" needs more than a toggle. */
@Composable
fun NumericEditDialog(
    day: DayCell,
    unit: String?,
    today: LocalDate,
    accent: Color,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember(day.date) { mutableStateOf(day.numericValue?.let { formatValue(it) } ?: "") }
    val haptics = LocalHapticFeedback.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLargeIncreased,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(modifier = Modifier.padding(Spacing.large).fillMaxWidth()) {
                Text(
                    text = day.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(Spacing.small))
                Text(
                    text = dayStatusLabel(day, unit, today),
                    style = MaterialTheme.typography.bodyMedium,
                    color = accent,
                )
                Spacer(Modifier.height(Spacing.large))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(unit ?: "Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Spacing.large))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(Spacing.small))
                    Button(
                        onClick = {
                            text.toDoubleOrNull()?.let {
                                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                onSave(it)
                            }
                        },
                    ) { Text("Save") }
                }
            }
        }
    }
}

private fun dayStatusLabel(day: DayCell, unit: String?, today: LocalDate): String = when (day.state) {
    EntryState.COMPLETED -> if (day.numericValue != null) {
        "Completed · ${formatValue(day.numericValue)}${unit?.let { " $it" } ?: ""}"
    } else {
        "Completed"
    }
    EntryState.IN_PROGRESS -> "In progress · ${formatValue(day.numericValue ?: 0.0)}${unit?.let { " $it" } ?: ""}"
    EntryState.SKIPPED -> "Skipped"
    null -> if (day.date.isAfter(today)) "Upcoming" else "Not logged"
}

/**
 * "3× per week", "Goal: at least 20 pages", "Every 2 days" — the schedule this habit was
 * actually set up with, which the detail screen previously didn't surface anywhere at all despite
 * the data already being on [HabitDetailUiState]. For numeric habits the target takes priority
 * over the day-frequency wording since the target is the more meaningful number to lead with.
 */
fun scheduleLabel(uiState: HabitDetailUiState): String {
    if (uiState.type == HabitType.NUMERICAL && uiState.targetValue != null) {
        val kindWord = if (uiState.targetKind == TargetKind.AT_MOST) "at most" else "at least"
        val amount = if (uiState.targetValue == uiState.targetValue.toLong().toDouble()) {
            uiState.targetValue.toLong().toString()
        } else {
            "%.1f".format(uiState.targetValue)
        }
        return "Goal: $kindWord $amount${uiState.unit?.let { " $it" } ?: ""}"
    }
    val num = uiState.targetNumerator
    val den = uiState.targetDenominator
    return when {
        num == 1 && den == 1 -> "Every day"
        den == 7 -> "$num× per week"
        else -> "Every $den days"
    }
}

/**
 * How much of the current window's slack is left, e.g. "2/3 this week" — only shown for
 * habits with real slack in their schedule (daily habits have none: the streak already says
 * everything there is to say about a habit with zero allowed misses).
 */
fun cycleProgressLabel(uiState: HabitDetailUiState): String? {
    val completions = uiState.cycleCompletions ?: return null
    val target = uiState.cycleTarget ?: return null
    val windowDays = uiState.cycleWindowDays ?: return null
    val cadence = if (windowDays == 7) "this week" else "this cycle"
    return "$completions/$target $cadence"
}

/**
 * A ranked row for one past streak: a numbered badge, the date range it covers, and its length
 * as a trailing pill — a small ordered list rather than a bar chart, so a habit with a long
 * history reads as a set of concrete achievements instead of a proportional-length visualization.
 */
@Composable
fun StreakRow(rank: Int, streak: StreakRun, fraction: Float, accent: Color, today: LocalDate) {
    val rowHeight = 44.dp
    // Grows in once on appearance rather than snapping straight to its final length - the fill
    // IS the data here, so the reveal is what sells "this row's length means something" instead
    // of reading as a static tinted background that happens to vary between rows.
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateIn = true }
    val animatedFraction by animateFloatAsState(
        targetValue = if (animateIn) fraction else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "streakFill",
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // The fill itself IS the ranking - a #1 row reads as visibly longer than a #5 row
            // at a glance, rather than making someone compare four-digit-day-count text.
            Box(
                modifier = Modifier
                    .width(maxWidth * animatedFraction)
                    .height(rowHeight)
                    .background(accent.copy(alpha = 0.14f)),
            )
            Row(
                modifier = Modifier
                    .height(rowHeight)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.default),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = accent,
                    )
                }
                Spacer(Modifier.width(Spacing.default))
                Text(
                    text = streakRangeLabel(streak, today),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (streak.length == 1) "1 day" else "${streak.length} days",
                    style = MaterialTheme.typography.labelMediumEmphasized,
                    color = accent,
                )
            }
        }
    }
}

internal fun streakRangeLabel(streak: StreakRun, today: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d")
    val startText = if (streak.start.year != today.year) {
        DateTimeFormatter.ofPattern("MMM d, yyyy").format(streak.start)
    } else {
        formatter.format(streak.start)
    }
    return if (streak.start == streak.end) startText else "$startText – ${formatter.format(streak.end)}"
}

/**
 * Three different angles on the same history that none of the other sections answer: which
 * weekdays this habit actually holds up on, whether the last month was better or worse than the
 * one before it, and roughly when in the day it tends to get done. Grouped into one card rather
 * than three separate titled sections since each is a single supporting fact, not its own screen
 * region — three headings in a row would out-weigh what they're each saying.
 */
@Composable
fun StatColumn(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    value: String,
    label: String,
    accent: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
            icon?.let {
                Icon(imageVector = it, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Text(text = value, style = MaterialTheme.typography.titleLargeEmphasized, color = accent)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Small, compact month grids in a horizontally-scrollable row — four fit on screen at once,
 * most recent month first (no need to scroll to see current data), older months revealed by
 * scrolling right. Replaced two earlier attempts: a GitHub-contributions column-per-week layout
 * (leading blank cells shifted the whole grid off-center depending on the start weekday) and a
 * full-width vertically-stacked calendar (cells were huge, and being inside a non-scrolling
 * screen meant three months of them could overflow off the bottom of the screen entirely).
 */
@Composable
fun HistoryCalendar(
    days: List<DayCell>,
    accent: Color,
    today: LocalDate,
    onMonthClick: (YearMonth) -> Unit,
) {
    if (days.isEmpty()) return
    val byDate = remember(days) { days.associateBy { it.date } }
    val months = remember(days) { days.map { YearMonth.from(it.date) }.distinct().sortedDescending() }
    val listState = rememberLazyListState()

    // Which year is showing sits above the row and tracks scroll position directly — no need to
    // scroll all the way to a January/December boundary to tell which year you've landed on.
    // Anchored to the CENTER of the visible viewport, not the left edge: at the scroll extremes
    // the leftmost card can be reduced to a sliver just a few pixels wide while the rest of the
    // viewport is dominated by the next month along — using the edge card's year there reads as
    // wrong the moment more than half the screen is visibly a different month/year. LazyListState
    // already tracks exactly which items are visible and where, so this reads that directly
    // instead of converting scroll-offset pixels back into a month index by hand.
    val visibleYear by remember(months) {
        derivedStateOf {
            val layout = listState.layoutInfo
            val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
            val centerItem = layout.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
            }
            months.getOrElse(centerItem?.index ?: 0) { months.last() }.year
        }
    }

    Column {
        // An odometer-style tick, not a plain crossfade: scrolling further into the past slides
        // the new (older) year up from below as the old one exits upward — like a counter rolling
        // down — and reverses when scrolling back toward the present. Direction is derived from
        // the actual year delta so it stays correct however far you jump in one scroll gesture.
        AnimatedContent(
            targetState = visibleYear,
            transitionSpec = {
                val goingBackInTime = targetState < initialState
                (slideInVertically(tween(220)) { height -> if (goingBackInTime) height else -height } + fadeIn(tween(220)))
                    .togetherWith(slideOutVertically(tween(220)) { height -> if (goingBackInTime) -height else height } + fadeOut(tween(220)))
            },
            label = "historyYear",
        ) { year ->
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(Spacing.extraSmall))
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            items(months, key = { it.toString() }) { month ->
                MonthCalendar(
                    month = month,
                    byDate = byDate,
                    accent = accent,
                    today = today,
                    onClick = { onMonthClick(month) },
                )
            }
        }
    }
}

private val MonthCardWidth = 84.dp
private val DayCellSize = 9.dp

@Composable
fun MonthCalendar(
    month: YearMonth,
    byDate: Map<LocalDate, DayCell>,
    accent: Color,
    today: LocalDate,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(MonthCardWidth)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .padding(4.dp),
    ) {
        Text(
            text = month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            style = MaterialTheme.typography.labelSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.extraSmall))

        val firstOfMonth = month.atDay(1)
        // ISO DayOfWeek is Monday=1..Sunday=7; %7 maps Sunday to 0 so the grid starts on Sunday.
        val leadingBlanks = firstOfMonth.dayOfWeek.value % 7
        val cells = buildList {
            repeat(leadingBlanks) { add(null) }
            for (day in 1..month.lengthOfMonth()) add(month.atDay(day))
            while (size % 7 != 0) add(null)
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            cells.chunked(7).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    week.forEach { date ->
                        val cell = date?.let { byDate[it] }
                        // Every cell in the grid gets a square, including padding days outside
                        // the month and days before the habit existed — no cell is ever fully
                        // blank, so the grid always reads as one solid, intentional rectangle
                        // rather than a patchwork of holes and squares.
                        val (fill, cellAlpha) = when (cell?.state) {
                            EntryState.COMPLETED -> accent to 1f
                            EntryState.IN_PROGRESS -> accent to 0.4f
                            EntryState.SKIPPED, null -> MaterialTheme.colorScheme.surfaceContainerHighest to 0.6f
                        }
                        // Individual cells are no longer separately tappable — at 9dp they're far
                        // too small to hit reliably. The whole card is one tap target (see
                        // MonthCalendar's onClick) that opens a bigger, actually-tappable grid.
                        Box(
                            modifier = Modifier
                                .size(DayCellSize)
                                .clip(RoundedCornerShape(2.dp))
                                .background(fill.copy(alpha = cellAlpha))
                                .then(
                                    if (date == today) {
                                        val ringColor = if (cell?.state == EntryState.COMPLETED) {
                                            contrastingIconColor(accent)
                                        } else {
                                            accent
                                        }
                                        Modifier.border(1.dp, ringColor, RoundedCornerShape(2.dp))
                                    } else {
                                        Modifier
                                    },
                                ),
                        )
                    }
                }
            }
        }
    }
}
