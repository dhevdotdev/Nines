package dev.dhev.nines.ui.habits

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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.dhev.nines.data.local.entity.EntryState
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
            Spacer(Modifier.height(Spacing.extraLarge))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                    CircularWavyProgressIndicator(
                        progress = { uiState.currentScore.toFloat() },
                        modifier = Modifier.size(96.dp),
                        color = colors.accent,
                        trackColor = colors.container,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(uiState.currentScore * 100).roundToInt()}%",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.accent,
                        )
                        Text(
                            text = "reliability",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(Modifier.height(Spacing.extraLarge))

            Text(
                text = "History",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(Spacing.default))
            HistoryCalendar(days = uiState.history, accent = colors.accent)
            Spacer(Modifier.height(Spacing.large))
        }
    }
}

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
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = accent)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        months.forEach { month ->
            MonthCalendar(month = month, byDate = byDate, accent = accent)
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
            style = MaterialTheme.typography.labelSmall,
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
