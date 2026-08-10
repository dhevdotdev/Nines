package dev.dhev.nines.ui.habits

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HabitDetailScreen(
    uiState: HabitDetailUiState,
    onBack: () -> Unit,
    onAction: (HabitDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = rememberHabitColors(Color(uiState.color))
    var showEditSheet by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf<YearMonth?>(null) }
    var editingDay by remember { mutableStateOf<DayCell?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    // The icon rides along with the name here instead of floating alone above
                    // the ring in the body — it reads as "whose page is this" attached to the
                    // title, rather than an orphaned circle with nothing anchoring it in the
                    // empty space between the app bar and the ring.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.accent),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = HabitIcons.iconFor(uiState.iconKey),
                                contentDescription = null,
                                tint = contrastingIconColor(colors.accent),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Text(
                            text = uiState.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit habit")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) return@Scaffold

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.large),
        ) {
            Spacer(Modifier.height(Spacing.default))

            Text(
                text = scheduleLabel(uiState),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.small))

            // One hero surface start to finish — illustration, ring, and stats all live inside
            // the same panel rather than a card-then-three-more-boxes stack. Stacking several
            // separately-outlined rectangles read as "boxy"; folding the stats into the same
            // surface (divided only by a hairline, not another edge) keeps it to a single shape.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLargeIncreased,
                color = lerp(MaterialTheme.colorScheme.surfaceContainer, colors.accent, 0.08f),
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                        HabitCardBackground(
                            style = CardBackgroundStyle.fromKey(uiState.backgroundStyleKey),
                            color = colors.accent,
                            alpha = 0.35f,
                            modifier = Modifier.matchParentSize(),
                        )
                        // The one thing missing from this page before now: you could see today's
                        // status here but not actually change it, so marking a habit done meant
                        // leaving the detail screen and going back to the list. Floats in the
                        // corner rather than its own row so it doesn't add a fourth boxy section.
                        if (uiState.type == HabitType.NUMERICAL) {
                            NumericProgressButton(
                                metTarget = uiState.completedToday,
                                onIncrement = { onAction(HabitDetailAction.Increment(LocalDate.now(), defaultIncrement(uiState.targetValue))) },
                                onDecrement = { onAction(HabitDetailAction.Increment(LocalDate.now(), -defaultIncrement(uiState.targetValue))) },
                                accentColor = colors.accent,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(Spacing.default),
                            )
                        } else {
                            CheckOffButton(
                                completed = uiState.completedToday,
                                onToggle = { onAction(HabitDetailAction.Toggle(LocalDate.now())) },
                                accentColor = colors.accent,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(Spacing.default),
                            )
                        }
                        // M3 Expressive leans into bold, unmissable primary indicators rather than
                        // a modest little dial — this is the single most important number on the
                        // page, so it gets to be the biggest thing on it.
                        Box(
                            modifier = Modifier.size(184.dp).align(Alignment.Center),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularWavyProgressIndicator(
                                progress = { uiState.currentScore.toFloat() },
                                modifier = Modifier.size(184.dp),
                                color = colors.accent,
                                trackColor = colors.container,
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(uiState.currentScore * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.displayMediumEmphasized,
                                    color = colors.accent,
                                )
                                // "reliability"/"uptime" was the SRE framing this app's name riffs
                                // on, but that whole narrative layer is on hold for now (see the
                                // project's own notes on this) — plain, common wording instead of
                                // jargon until/unless that layer comes back deliberately.
                                Text(
                                    text = "consistency",
                                    style = MaterialTheme.typography.labelMediumEmphasized,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                cycleProgressLabel(uiState)?.let { label ->
                                    Spacer(Modifier.height(Spacing.extraSmall))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = colors.accent.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.default),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatColumn(
                            icon = Icons.Filled.LocalFireDepartment,
                            value = "${uiState.currentStreak}",
                            label = "current streak",
                            accent = colors.accent,
                        )
                        StatColumn(
                            icon = null,
                            value = "${uiState.bestStreak}",
                            label = "best streak",
                            accent = colors.accent,
                        )
                        StatColumn(
                            icon = null,
                            value = "${uiState.totalCompletions}",
                            label = "total",
                            accent = colors.accent,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.extraLarge))

            Text(
                text = "History",
                style = MaterialTheme.typography.titleMediumEmphasized,
            )
            Spacer(Modifier.height(Spacing.default))
            HistoryCalendar(
                days = uiState.history,
                accent = colors.accent,
                onMonthClick = { month -> expandedMonth = month },
            )

            // Skipped when the only streak on record is the one still running today - that's
            // already fully represented by the "current streak" stat above, so a one-row list
            // here would just repeat it instead of adding anything.
            val showStreaks = uiState.topStreaks.size > 1 ||
                uiState.topStreaks.firstOrNull()?.end != LocalDate.now()
            if (uiState.topStreaks.isNotEmpty() && showStreaks) {
                Spacer(Modifier.height(Spacing.extraLarge))
                Text(
                    text = "Best streaks",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
                Spacer(Modifier.height(Spacing.default))
                val longestStreak = uiState.topStreaks.first().length
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    uiState.topStreaks.forEachIndexed { index, streak ->
                        StreakRow(
                            rank = index + 1,
                            streak = streak,
                            fraction = (streak.length.toFloat() / longestStreak).coerceIn(0.12f, 1f),
                            accent = colors.accent,
                        )
                    }
                }
            }

            InsightsSection(uiState = uiState, accent = colors.accent)

            Spacer(Modifier.height(Spacing.large))
        }
    }

    // Reuses the exact same sheet the list screen edits habits with, rather than a second
    // parallel edit UI — one form to keep in sync instead of two.
    if (showEditSheet) {
        AddHabitSheet(
            paletteColors = rememberHabitColorsForPalette(HabitPalette.seeds),
            editing = HabitListItem(
                id = uiState.habitId,
                name = uiState.name,
                color = uiState.color,
                iconKey = uiState.iconKey,
                backgroundStyleKey = uiState.backgroundStyleKey,
                type = uiState.type,
                completedToday = uiState.completedToday,
                currentScore = uiState.currentScore,
                currentStreak = uiState.currentStreak,
                currentValue = uiState.currentValue,
                targetValue = uiState.targetValue,
                targetKind = uiState.targetKind,
                unit = uiState.unit,
                targetNumerator = uiState.targetNumerator,
                targetDenominator = uiState.targetDenominator,
            ),
            onConfirm = { action ->
                if (action is HabitsAction.UpdateHabit) {
                    onAction(
                        HabitDetailAction.Update(
                            name = action.name,
                            color = action.color,
                            iconKey = action.iconKey,
                            backgroundStyleKey = action.backgroundStyleKey,
                            targetNumerator = action.targetNumerator,
                            targetDenominator = action.targetDenominator,
                            targetValue = action.targetValue,
                            targetKind = action.targetKind,
                            unit = action.unit,
                        ),
                    )
                }
                showEditSheet = false
            },
            onDelete = {
                onAction(HabitDetailAction.Delete)
                showEditSheet = false
                onBack()
            },
            onDismiss = { showEditSheet = false },
        )
    }

    // Small compact grid cells are too fiddly to tap accurately, so the calendar is a two-step
    // reveal: tap a whole month card to open it full-size, then tap a day inside that bigger
    // grid to edit it directly — done/not-done for boolean habits, or the logged measurement
    // for numeric ones. This is real retroactive editing, not just a read-only heatmap.
    expandedMonth?.let { month ->
        ExpandedMonthDialog(
            month = month,
            byDate = remember(uiState.history) { uiState.history.associateBy { it.date } },
            habitType = uiState.type,
            unit = uiState.unit,
            accent = colors.accent,
            onDayClick = { day ->
                if (uiState.type == HabitType.NUMERICAL) {
                    editingDay = day
                } else {
                    onAction(HabitDetailAction.Toggle(day.date))
                }
            },
            onDismiss = { expandedMonth = null },
        )
    }

    editingDay?.let { day ->
        NumericEditDialog(
            day = day,
            unit = uiState.unit,
            accent = colors.accent,
            onSave = { value ->
                onAction(HabitDetailAction.SetValue(day.date, value))
                editingDay = null
            },
            onDismiss = { editingDay = null },
        )
    }
}

/**
 * The full-size month view opened from tapping a compact card in [HistoryCalendar]. Cells here
 * are big enough (36dp, with the day-of-month number) to tap confidently, unlike the 9dp cells
 * in the scrollable overview row.
 */
@Composable
private fun ExpandedMonthDialog(
    month: YearMonth,
    byDate: Map<LocalDate, DayCell>,
    habitType: HabitType,
    unit: String?,
    accent: Color,
    onDayClick: (DayCell) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = LocalDate.now()
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
private fun NumericEditDialog(
    day: DayCell,
    unit: String?,
    accent: Color,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember(day.date) { mutableStateOf(day.numericValue?.let { formatDetailValue(it) } ?: "") }

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
                    text = dayStatusLabel(day, unit),
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
                    Button(onClick = { text.toDoubleOrNull()?.let(onSave) }) { Text("Save") }
                }
            }
        }
    }
}

private fun dayStatusLabel(day: DayCell, unit: String?): String = when (day.state) {
    EntryState.COMPLETED -> if (day.numericValue != null) {
        "Completed · ${formatDetailValue(day.numericValue)}${unit?.let { " $it" } ?: ""}"
    } else {
        "Completed"
    }
    EntryState.IN_PROGRESS -> "In progress · ${formatDetailValue(day.numericValue ?: 0.0)}${unit?.let { " $it" } ?: ""}"
    EntryState.SKIPPED -> "Skipped"
    null -> if (day.date.isAfter(LocalDate.now())) "Upcoming" else "Not logged"
}

private fun formatDetailValue(value: Double): String =
    if (value == value.roundToInt().toDouble()) value.roundToInt().toString() else "%.1f".format(value)

/**
 * "3× per week", "Goal: at least 20 pages", "Every 2 days" — the schedule this habit was
 * actually set up with, which the detail screen previously didn't surface anywhere at all despite
 * the data already being on [HabitDetailUiState]. For numeric habits the target takes priority
 * over the day-frequency wording since the target is the more meaningful number to lead with.
 */
private fun scheduleLabel(uiState: HabitDetailUiState): String {
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
private fun cycleProgressLabel(uiState: HabitDetailUiState): String? {
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
private fun StreakRow(rank: Int, streak: StreakRun, fraction: Float, accent: Color) {
    val rowHeight = 56.dp
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
                    .width(maxWidth * fraction)
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
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.labelMediumEmphasized,
                        color = accent,
                    )
                }
                Spacer(Modifier.width(Spacing.default))
                Text(
                    text = streakRangeLabel(streak),
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

private fun streakRangeLabel(streak: StreakRun): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d")
    val startText = if (streak.start.year != LocalDate.now().year) {
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
private fun InsightsSection(uiState: HabitDetailUiState, accent: Color) {
    val rows = buildList<@Composable () -> Unit> {
        uiState.recordProgressLabel?.let { label ->
            add { RecordProgressRow(label = label, accent = accent) }
        }
        if (uiState.weekdayRates.size == 7) {
            add { WeekdayBreakdownRow(rates = uiState.weekdayRates, accent = accent) }
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
            add { LongestGapRow(gap = gap, accent = accent) }
        }
    }
    if (rows.isEmpty()) return

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
            rows.forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider(color = accent.copy(alpha = 0.12f))
                row()
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
            text = "${formatDetailValue(total)}$unitSuffix total · averaging ${formatDetailValue(average)}$unitSuffix per entry",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LongestGapRow(gap: StreakRun, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.EventBusy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = "Longest gap: ${gap.length} days (${streakRangeLabel(gap)})",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun WeekdayBreakdownRow(rates: List<Double>, accent: Color) {
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
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accent.copy(alpha = (0.12f + rate.toFloat() * 0.75f).coerceAtMost(1f))),
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

private fun weekdayName(index: Int): String =
    java.time.DayOfWeek.of(index + 1).getDisplayName(TextStyle.FULL, Locale.getDefault())

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
@Composable
private fun StatColumn(
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
private fun HistoryCalendar(days: List<DayCell>, accent: Color, onMonthClick: (YearMonth) -> Unit) {
    if (days.isEmpty()) return
    val byDate = remember(days) { days.associateBy { it.date } }
    val months = remember(days) { days.map { YearMonth.from(it.date) }.distinct().sortedDescending() }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val cardStepPx = remember(density) { with(density) { (MonthCardWidth + Spacing.small).toPx() } }
    var viewportWidthPx by remember { mutableFloatStateOf(0f) }

    // Which year is showing sits above the row and tracks scroll position directly — no need to
    // scroll all the way to a January/December boundary to tell which year you've landed on.
    // Anchored to the CENTER of the visible viewport, not the left edge: at the scroll extremes
    // the leftmost card can be reduced to a sliver just a few pixels wide while the rest of the
    // viewport is dominated by the next month along — using the edge card's year there reads as
    // wrong the moment more than half the screen is visibly a different month/year.
    val visibleYear by remember(months) {
        derivedStateOf {
            val centerPx = scrollState.value + viewportWidthPx / 2f
            val index = (centerPx / cardStepPx).roundToInt().coerceIn(0, months.lastIndex)
            months[index].year
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
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .onSizeChanged { viewportWidthPx = it.width.toFloat() },
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            months.forEach { month ->
                MonthCalendar(
                    month = month,
                    byDate = byDate,
                    accent = accent,
                    onClick = { onMonthClick(month) },
                )
            }
        }
    }
}

private val MonthCardWidth = 84.dp
private val DayCellSize = 9.dp

@Composable
private fun MonthCalendar(
    month: YearMonth,
    byDate: Map<LocalDate, DayCell>,
    accent: Color,
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
                                    if (date == LocalDate.now()) {
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
