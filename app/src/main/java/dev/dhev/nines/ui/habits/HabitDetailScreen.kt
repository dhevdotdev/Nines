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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HabitDetailScreen(
    uiState: HabitDetailUiState,
    onBack: () -> Unit,
    onAction: (HabitDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = rememberHabitColors(Color(uiState.color))
    val haptics = LocalHapticFeedback.current
    var showEditSheet by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf<YearMonth?>(null) }
    var editingDay by remember { mutableStateOf<DayCell?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    fun clearNumericEntry(value: Double, date: LocalDate) {
        onAction(HabitDetailAction.ClearNumericEntry(date, value))
        snackbarScope.launch {
            if (snackbarHostState.showSnackbar(
                    message = "Entry cleared",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Long,
                ) == SnackbarResult.ActionPerformed
            ) {
                onAction(HabitDetailAction.UndoClearNumericEntry(date, value))
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        if (uiState.isLoading) {
            FullScreenLoadingIndicator(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }

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
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(Spacing.default),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CheckOffButton(
                                    completed = uiState.currentValue != null,
                                    icon = Icons.Outlined.Edit,
                                    contentDescription = "Edit ${uiState.name} value for ${uiState.selectedDate}",
                                    onToggle = {
                                        editingDay = DayCell(
                                            date = uiState.selectedDate,
                                            state = if (uiState.completed) EntryState.COMPLETED else EntryState.IN_PROGRESS,
                                            isToday = uiState.selectedDate == uiState.today,
                                            numericValue = uiState.currentValue,
                                        )
                                    },
                                    accentColor = colors.accent,
                                )
                                Spacer(Modifier.height(Spacing.extraSmall))
                                // Every tap on that button silently changes a number nowhere else
                                // visible on this screen - this is the only feedback confirming
                                // the tap landed and showing what it actually did to today's value.
                                Text(
                                    text = "${formatValue(uiState.currentValue ?: 0.0)}${uiState.targetValue?.let { "/${formatValue(it)}" } ?: ""}",
                                    style = MaterialTheme.typography.labelMediumEmphasized,
                                    color = colors.accent,
                                )
                            }
                        } else {
                            CheckOffButton(
                                completed = uiState.completed,
                                onToggle = { onAction(HabitDetailAction.Toggle(uiState.selectedDate)) },
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
                today = uiState.today,
                onMonthClick = { month ->
                    haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    expandedMonth = month
                },
            )

            // Skipped when the only streak on record is the one still running today - that's
            // already fully represented by the "current streak" stat above, so a one-row list
            // here would just repeat it instead of adding anything.
            val showStreaks = uiState.topStreaks.size > 1 ||
                uiState.topStreaks.firstOrNull()?.end != uiState.today
            if (uiState.topStreaks.isNotEmpty() && showStreaks) {
                Spacer(Modifier.height(Spacing.extraLarge))
                Text(
                    text = "Best streaks",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
                Spacer(Modifier.height(Spacing.default))
                val longestStreak = uiState.topStreaks.first().length
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                    uiState.topStreaks.forEachIndexed { index, streak ->
                        StreakRow(
                            rank = index + 1,
                            streak = streak,
                            fraction = (streak.length.toFloat() / longestStreak).coerceIn(0.12f, 1f),
                            accent = colors.accent,
                            today = uiState.today,
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
                completed = uiState.completed,
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
            today = uiState.today,
            accent = colors.accent,
            onDayClick = { day ->
                onAction(HabitDetailAction.SelectDate(day.date))
                if (uiState.type == HabitType.NUMERICAL) {
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    editingDay = day
                } else {
                    haptics.performHapticFeedback(
                        if (day.state == EntryState.COMPLETED) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn,
                    )
                    onAction(HabitDetailAction.Toggle(day.date))
                }
            },
            onDismiss = { expandedMonth = null },
        )
    }

    editingDay?.let { day ->
        NumericEntryEditor(
            habitName = uiState.name,
            iconKey = uiState.iconKey,
            colors = colors,
            initialValue = day.numericValue,
            targetValue = uiState.targetValue,
            targetKind = uiState.targetKind,
            unit = uiState.unit,
            selectedDate = day.date,
            onConfirm = { value ->
                onAction(HabitDetailAction.SetValue(day.date, value))
                editingDay = null
            },
            onClear = day.numericValue?.let { value ->
                {
                    clearNumericEntry(value, day.date)
                    editingDay = null
                }
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
