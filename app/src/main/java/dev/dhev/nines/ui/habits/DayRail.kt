package dev.dhev.nines.ui.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import dev.dhev.nines.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val RAIL_WINDOW_DAYS = 7
private val NON_TODAY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

/** The calendar week containing today (Sunday-Saturday, matching the S/M/T/W/T/F/S labels) -
 * not a rolling "last 7 days" window. A rolling window always pins today to the trailing edge,
 * which reads as cramped with no breathing room on that side; anchoring to the calendar week
 * instead lets today sit wherever it naturally falls, the same way most calendar/fitness apps'
 * weekly strips do. Days after today are still shown (so the week doesn't visually truncate
 * mid-row) but rendered muted and non-interactive - browsing into them is never possible, only
 * seeing that they exist. */
@Composable
fun DayRail(
    today: LocalDate,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCalendar by remember { mutableStateOf(false) }
    val weekStart =
        remember(today) {
            today.minusDays(today.dayOfWeek.value.toLong() % 7) // dayOfWeek.value: Mon=1..Sun=7, so Sun=7%7=0 back
        }
    val days = remember(weekStart) { (0 until RAIL_WINDOW_DAYS).map { weekStart.plusDays(it.toLong()) } }
    val haptics = LocalHapticFeedback.current
    // A plain Row with equal weights, not a LazyRow - exactly 7 days always exist, so dividing
    // the full available width evenly guarantees every chip renders whole. A LazyRow sized to
    // each chip's natural width overflows on most screens, clipping the last day right at the
    // edge - looks broken for a component whose entire point is showing the complete week.
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        days.forEachIndexed { index, date ->
            val isFuture = date.isAfter(today)
            DayChip(
                date = date,
                index = index,
                isSelected = date == selectedDate,
                isToday = date == today,
                isFuture = isFuture,
                onClick =
                if (isFuture) {
                    null
                } else {
                    {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onSelectDate(date)
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
        IconButton(
            onClick = { showCalendar = true },
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "Choose a date",
            )
        }
    }

    if (showCalendar) {
        CalendarDialog(
            today = today,
            selectedDate = selectedDate,
            onSelectDate = onSelectDate,
            onDismiss = { showCalendar = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarDialog(
    today: LocalDate,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val todayMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val pickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates =
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis

                override fun isSelectableYear(year: Int): Boolean = year <= today.year
            },
        )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onSelectDate(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    onDismiss()
                },
                enabled = pickerState.selectedDateMillis != null,
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = pickerState, showModeToggle = false)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DayChip(
    date: LocalDate,
    index: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val locale = remember { Locale.getDefault() }
    // A day chip is a small component - per M3's own motion guidance, small components use the
    // fast tier of a spec, not the default speed (that's reserved for larger elements).
    val spatialSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val scope = rememberCoroutineScope()

    // A small staggered pop-in the first time the rail composes, rather than the whole week
    // appearing flat-footed all at once - each chip trails the previous by 40ms. (No M3-specified
    // stagger figure exists for this; chosen empirically, not cited as spec.)
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 40L)
        entrance.animateTo(1f, animationSpec = spatialSpec)
    }

    // Selecting a day gets the same overshoot-pop treatment as the habit check-off control
    // (see CheckOffButton) - the gesture IS the animation driver there too, so reusing it here
    // keeps every "you just committed to something" moment in the app feeling like one language.
    val selectionPop = remember { Animatable(1f) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            selectionPop.snapTo(0.8f)
            selectionPop.animateTo(1f, animationSpec = spatialSpec)
        }
    }

    // Circle-to-Cookie is a real animated Morph, not an instant swap - M3's shape guidance is
    // explicit that shape morph is the named mechanism for communicating an interaction state
    // like selection, not just a nice-to-have.
    val morphProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spatialSpec,
        label = "dayChipShapeMorph",
    )
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }
    val shape = remember(morph) { MorphShape(morph) { morphProgress } }
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "dayChipContainer",
    )
    val numberColor =
        when {
            isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isToday -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        }
    val weekdayLetterColor =
        if (isFuture) {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    var chipModifier =
        modifier
            .graphicsLayer {
                scaleX = entrance.value
                scaleY = entrance.value
                alpha = entrance.value
            }.clip(MaterialTheme.shapes.large)
            .semantics {
                contentDescription = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
                role = Role.Button
                stateDescription = if (isSelected) "Selected" else "Not selected"
                if (isFuture) disabled()
            }
    if (onClick != null) {
        chipModifier =
            chipModifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = {
                    scope.launch {
                        selectionPop.snapTo(0.8f)
                        selectionPop.animateTo(1f, animationSpec = spatialSpec)
                    }
                    onClick()
                },
            )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = chipModifier.padding(vertical = Spacing.extraSmall),
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).take(1),
            style = MaterialTheme.typography.labelSmall,
            color = weekdayLetterColor,
        )
        Spacer(Modifier.height(4.dp))
        var circleModifier =
            Modifier
                .size(38.dp)
                .graphicsLayer {
                    scaleX = selectionPop.value
                    scaleY = selectionPop.value
                }.clip(shape)
                .background(containerColor)
        if (isToday && !isSelected) {
            circleModifier = circleModifier.border(1.5.dp, MaterialTheme.colorScheme.primary, shape)
        }
        Box(
            modifier = circleModifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = numberColor,
            )
        }
    }
}

/** Renders [morph] at whatever progress [progress] currently reports, letting a single Shape
 * instance drive a live-animating outline instead of swapping between two fixed shapes.
 * [RoundedPolygon]-based shapes don't live in a fixed, predictable coordinate space (their
 * radius/bounds vary per shape), so the transform maps [Morph.calculateMaxBounds] - the bounding
 * box across the whole morph range, not just the current frame, to avoid the shape's apparent
 * size jittering as it morphs - onto the actual pixel size directly, point by point, rather than
 * guessing a fixed normalization. */
private class MorphShape(
    private val morph: Morph,
    private val progress: () -> Float,
) : Shape {
    private val bounds = morph.calculateMaxBounds()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val left = bounds[0]
        val top = bounds[1]
        val boundsWidth = bounds[2] - bounds[0]
        val boundsHeight = bounds[3] - bounds[1]
        val scaleX = size.width / boundsWidth
        val scaleY = size.height / boundsHeight

        fun tx(x: Float) = (x - left) * scaleX

        fun ty(y: Float) = (y - top) * scaleY

        val path = Path()
        morph.forEachCubic(progress()) { cubic ->
            if (path.isEmpty) {
                path.moveTo(tx(cubic.anchor0X), ty(cubic.anchor0Y))
            }
            path.cubicTo(
                tx(cubic.control0X),
                ty(cubic.control0Y),
                tx(cubic.control1X),
                ty(cubic.control1Y),
                tx(cubic.anchor1X),
                ty(cubic.anchor1Y),
            )
        }
        path.close()
        return Outline.Generic(path)
    }
}

/** Shown only while browsing a date other than today, so a retroactive edit can never get
 * silently mistaken for logging today's entry. */
@Composable
fun NonTodayIndicator(
    selectedDate: LocalDate,
    onJumpToToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large, vertical = Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Viewing ${selectedDate.format(NON_TODAY_DATE_FORMAT)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        TextButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onJumpToToday()
            },
        ) {
            Text("Jump to today")
        }
    }
}
