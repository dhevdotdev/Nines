package dev.dhev.nines.ui.habits

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.ui.theme.Spacing
import dev.dhev.nines.ui.theme.contrastingIconColor
import dev.dhev.nines.ui.theme.rememberHabitColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HabitDetailScreen(
    uiState: HabitDetailUiState,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onIncrement: (Double) -> Unit,
    onDecrement: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = rememberHabitColors(Color(uiState.color))

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
                                onIncrement = { onIncrement(defaultIncrement(uiState.targetValue)) },
                                onDecrement = { onDecrement(defaultIncrement(uiState.targetValue)) },
                                accentColor = colors.accent,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(Spacing.default),
                            )
                        } else {
                            CheckOffButton(
                                completed = uiState.completedToday,
                                onToggle = onToggle,
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
            HistoryCalendar(days = uiState.history, accent = colors.accent)
            Spacer(Modifier.height(Spacing.large))
        }
    }
}

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
private fun HistoryCalendar(days: List<DayCell>, accent: Color) {
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
                MonthCalendar(month = month, byDate = byDate, accent = accent)
            }
        }
    }
}

private val MonthCardWidth = 84.dp
private val DayCellSize = 9.dp

@Composable
private fun MonthCalendar(month: YearMonth, byDate: Map<LocalDate, DayCell>, accent: Color) {
    Column(modifier = Modifier.width(MonthCardWidth)) {
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
                        Box(
                            modifier = Modifier
                                .size(DayCellSize)
                                .clip(RoundedCornerShape(2.dp))
                                .background(fill.copy(alpha = cellAlpha)),
                        )
                    }
                }
            }
        }
    }
}
