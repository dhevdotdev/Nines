package dev.dhev.nines.ui.habits

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.widthIn
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt
import dev.dhev.nines.BuildConfig
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.ui.theme.HabitPalette
import dev.dhev.nines.ui.theme.Spacing
import dev.dhev.nines.ui.theme.HabitColors
import dev.dhev.nines.ui.theme.contrastingIconColor
import dev.dhev.nines.ui.theme.rememberHabitColors
import dev.dhev.nines.ui.theme.rememberHabitColorsForPalette
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HabitsScreen(
    uiState: HabitsUiState,
    onAction: (HabitsAction) -> Unit,
    onHabitClick: (Long) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitListItem?>(null) }
    // Computed here (persists across sheet open/close), not inside AddHabitSheet (torn down and
    // rebuilt every time the sheet opens): the real HCT/CAM16 math for ~20 swatches should run
    // once per theme change, not once per sheet open.
    val paletteColors = rememberHabitColorsForPalette(HabitPalette.seeds)
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val today = uiState.today
    // Toggled from the top bar rather than a permanent per-row handle, and mutually exclusive
    // with the swipe gestures below - stacking a long-press-drag detector directly on the same
    // element as swipe-to-dismiss is exactly the kind of fragile gesture overlap that broke this
    // the first two times it was attempted. As separate modes, they're never both listening on
    // the same touch at once.
    var reorderMode by remember { mutableStateOf(false) }
    val effectiveReorderMode = isReorderModeActive(reorderMode, uiState.selectedDate, today)
    LaunchedEffect(uiState.selectedDate) {
        if (uiState.selectedDate != today) {
            reorderMode = false
        }
    }

    // Order lives here, not directly on uiState.habits, so a drag can reorder immediately without
    // waiting on a DB write + Flow round-trip, while every other field (score, completed)
    // still comes live from uiState. Re-synced only when the *set* of ids changes (a habit was
    // added or deleted) so it never fights an in-progress drag or a real reorder that just landed.
    var orderIds by remember { mutableStateOf(uiState.habits.map { it.id }) }
    LaunchedEffect(uiState.habits.map { it.id }.toSet()) {
        val newIds = uiState.habits.map { it.id }
        if (orderIds.toSet() != newIds.toSet()) {
            orderIds = orderIds.filter { it in newIds.toSet() } + newIds.filter { it !in orderIds.toSet() }
        }
    }
    val displayedHabits = remember(orderIds, uiState.habits) {
        orderIds.mapNotNull { id -> uiState.habits.find { it.id == id } }
    }

    // Archiving (never a real delete — see HabitRepository.archiveHabit) is safe to commit
    // immediately with no undo prompt: it's already recoverable, just not from this screen.
    // Tracked locally only so the row disappears the instant you swipe, rather than waiting on
    // the DB write + Flow round-trip.
    var archivedLocally by remember { mutableStateOf(setOf<Long>()) }

    fun archiveImmediately(item: HabitListItem) {
        archivedLocally = archivedLocally + item.id
        haptics.performHapticFeedback(HapticFeedbackType.Reject)
        onAction(HabitsAction.DeleteHabit(item.id))
    }

    fun clearNumericEntry(item: HabitListItem, value: Double) {
        val date = uiState.selectedDate
        onAction(HabitsAction.ClearNumericEntry(item.id, date, value))
        snackbarScope.launch {
            if (snackbarHostState.showSnackbar(
                    message = "Entry cleared",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Long,
                ) == SnackbarResult.ActionPerformed
            ) {
                onAction(HabitsAction.UndoClearNumericEntry(item.id, date, value))
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Plain, fixed-height, no scrollBehavior attached — fully sticky/pinned rather than the
        // collapsing-on-scroll LargeTopAppBar, which reserved a lot of empty vertical space at
        // rest and only gave it back once you scrolled. Consistent space from the start instead.
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nines",
                        style = MaterialTheme.typography.displaySmall,
                        // Debug-only: long-press to insert demo habits/history directly through
                        // the repository, in-process — a stand-in for adb-based DB reseeding on
                        // devices (like this one) whose storage permissions block that. No-op,
                        // inert gesture in release builds.
                        modifier = if (BuildConfig.DEBUG) {
                            Modifier.combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                                onLongClick = { onAction(HabitsAction.SeedDemoData) },
                            )
                        } else {
                            Modifier
                        },
                    )
                },
                actions = {
                    // A real mode switch, not just a per-row handle — swipe-to-archive/edit is
                    // disabled while this is on (see the items loop below), so the two gesture
                    // systems are never live on the same touch at once.
                    IconButton(
                        enabled = canEnterReorderMode(uiState.selectedDate, today),
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            reorderMode = !reorderMode
                        },
                    ) {
                        Icon(
                            imageVector = if (effectiveReorderMode) Icons.Filled.Check else Icons.Rounded.DragHandle,
                            contentDescription = when {
                                effectiveReorderMode -> "Done reordering"
                                uiState.selectedDate != today -> "Reorder habits unavailable for past dates"
                                else -> "Reorder habits"
                            },
                        )
                    }
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onSettingsClick()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            // Medium is M3 Expressive's recommended default size, not the plain (small-tier) FAB.
            MediumFloatingActionButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    showAddSheet = true
                },
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add habit",
                    modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                )
            }
        },
    ) { innerPadding ->
        val visibleHabits = remember(displayedHabits, archivedLocally) {
            displayedHabits.filter { it.id !in archivedLocally }
        }
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            DayRail(
                today = today,
                selectedDate = uiState.selectedDate,
                onSelectDate = { date -> onAction(HabitsAction.SelectDate(date)) },
            )
            // Unobtrusive by design: only ever visible while browsing a date other than today,
            // so a retroactive correction can never get silently mistaken for logging today.
            AnimatedVisibility(
                visible = uiState.selectedDate != today,
                enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                    expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                exit = fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                    shrinkVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
            ) {
                NonTodayIndicator(
                    selectedDate = uiState.selectedDate,
                    onJumpToToday = { onAction(HabitsAction.SelectDate(today)) },
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    FullScreenLoadingIndicator(modifier = Modifier.fillMaxSize())
                } else if (visibleHabits.isEmpty()) {
                    EmptyHabitsState(
                        modifier = Modifier.fillMaxSize(),
                        onAddHabit = { showAddSheet = true },
                    )
                } else {
                    val lazyListState = rememberLazyListState()
                    // Index 1 accounts for the AllDoneBanner's own always-present item {} slot (index 0)
                    // ahead of the reorderable habit items - the library's from/to indices are positions
                    // within the whole LazyColumn, not just the reorderable subset.
                    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                        orderIds = orderIds.toMutableList().apply {
                            add(to.index - 1, removeAt(from.index - 1))
                        }
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                    }
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Spacing.default,
                            end = Spacing.default,
                            top = Spacing.default,
                            // No Scaffold innerPadding to account for here (already consumed by the outer
                            // Column) — but the FAB still isn't part of that padding, so its clearance is
                            // still needed or it'll sit on top of (and eat touches meant for) the last
                            // row's own trailing button.
                            bottom = Spacing.default + 96.dp,
                        ),
                    ) {
                        item {
                            // Shown only once every habit is checked off — a small reward for actually
                            // finishing, not just a status readout that's visible the rest of the time.
                            // Fully qualified: the outer Column (added for the day rail) makes
                            // ColumnScope.AnimatedVisibility ambiguous with the plain overload here, even
                            // though the nearest real receiver at this point is LazyItemScope, not Column.
                            androidx.compose.animation.AnimatedVisibility(
                                // isNotEmpty() guards the vacuous-truth case: List<T>.all{} on an empty
                                // list is true, so before the real data loads in this would otherwise
                                // flash on for a frame every launch.
                                // Also gated on today: celebrating "all done" while browsing a past date
                                // would be nonsensical - that congratulation only means anything for the
                                // day you're actually tracking live, not a historical correction session.
                                visible = uiState.selectedDate == today &&
                                visibleHabits.isNotEmpty() && visibleHabits.all { it.completed },
                                enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                                expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                                exit = fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                                shrinkVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                            ) {
                                AllDoneBanner(modifier = Modifier.padding(bottom = Spacing.medium))
                            }
                        }
                        items(visibleHabits, key = { it.id }) { item ->
                            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                                val elevation by animateDpAsState(
                                    targetValue = if (isDragging) 6.dp else 0.dp,
                                    label = "reorderElevation",
                                )
                                if (effectiveReorderMode) {
                                    // Swipe is off entirely in this mode - the handle is the only active
                                    // gesture, so there's nothing for a long-press-drag to conflict with.
                                    Box(
                                        modifier = Modifier
                                        .shadow(elevation, MaterialTheme.shapes.largeIncreased)
                                        .padding(bottom = Spacing.medium),
                                    ) {
                                        HabitRow(
                                            item = item,
                                            onToggle = {},
                                            onSetValue = {},
                                            selectedDate = uiState.selectedDate,
                                            onClick = {},
                                            actionsEnabled = false,
                                            dragHandle = {
                                                Icon(
                                                    imageVector = Icons.Rounded.DragHandle,
                                                    contentDescription = "Reorder ${item.name}",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier
                                                    .draggableHandle(
                                                        onDragStarted = {
                                                            haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                                        },
                                                        onDragStopped = {
                                                            haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                                            onAction(HabitsAction.ReorderHabits(orderIds))
                                                        },
                                                    )
                                                    .size(24.dp),
                                                )
                                            },
                                        )
                                    }
                                } else {
                                    // The two swipe directions mean genuinely different things: edit
                                    // (right) never actually commits - it opens the sheet as a side
                                    // effect and springs back to Settled, since the row itself hasn't
                                    // gone anywhere. Archive (left) commits for real and is immediate -
                                    // no undo prompt, since archiving was never a real delete to begin
                                    // with (see HabitRepository.archiveHabit / the Settings screen).
                                    //
                                    // Hand-rolled on top of AnchoredDraggableState directly, rather than
                                    // M3's SwipeToDismissBox: that composable always anchors a dismissal at
                                    // +/- the row's full width (hundreds of dp), so even after visually
                                    // capping the render, the actual settle animation still had to travel
                                    // that whole real distance before settledValue updated - which read as
                                    // a very real, very noticeable delay between release and the action
                                    // firing. Anchoring at the reveal distance we actually want (revealPx)
                                    // means the settle has almost nothing left to travel, so it resolves
                                    // essentially the instant you let go.
                                    val density = LocalDensity.current
                                    val revealPx = remember(density) { with(density) { 120.dp.toPx() } }
                                    val dismissState = remember {
                                        AnchoredDraggableState(initialValue = SwipeToDismissBoxValue.Settled)
                                    }
                                    LaunchedEffect(dismissState, revealPx) {
                                        dismissState.updateAnchors(
                                            DraggableAnchors {
                                                SwipeToDismissBoxValue.Settled at 0f
                                                SwipeToDismissBoxValue.StartToEnd at revealPx
                                                SwipeToDismissBoxValue.EndToStart at -revealPx
                                            },
                                        )
                                    }
                                    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                                        state = dismissState,
                                        positionalThreshold = { totalDistance -> totalDistance * 0.6f },
                                        animationSpec = tween(150),
                                    )
                                    // Mirrors M3's own settledValue-gated LaunchedEffect(state.settledValue)
                                    // pattern (see SwipeToDismissBox source) - settledValue only updates once
                                    // release-triggered settling has actually finished, so this still can't
                                    // fire mid-drag.
                                    LaunchedEffect(dismissState.settledValue) {
                                        val direction = when (dismissState.settledValue) {
                                            SwipeToDismissBoxValue.StartToEnd -> HabitSwipeDirection.START_TO_END
                                            SwipeToDismissBoxValue.EndToStart -> HabitSwipeDirection.END_TO_START
                                            SwipeToDismissBoxValue.Settled -> null
                                        }
                                        when (habitSwipeAction(
                                            HabitGesturePolicyInput(
                                                selectedDate = uiState.selectedDate,
                                                today = today,
                                                reorderMode = effectiveReorderMode,
                                                swipeDirection = direction,
                                            ),
                                        )) {
                                            HabitSwipeAction.EDIT -> {
                                                editingHabit = item
                                                dismissState.animateTo(SwipeToDismissBoxValue.Settled)
                                            }
                                            HabitSwipeAction.ARCHIVE -> archiveImmediately(item)
                                            HabitSwipeAction.NONE -> Unit
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                        .shadow(elevation, MaterialTheme.shapes.largeIncreased)
                                        .padding(bottom = Spacing.medium)
                                        .anchoredDraggable(
                                            state = dismissState,
                                            orientation = Orientation.Horizontal,
                                            flingBehavior = flingBehavior,
                                        ),
                                    ) {
                                        val offsetPx = runCatching { dismissState.requireOffset() }.getOrDefault(0f)
                                        SwipeBackground(
                                            offsetPx = offsetPx,
                                            revealPx = revealPx,
                                            modifier = Modifier.matchParentSize(),
                                        )
                                        HabitRow(
                                            item = item,
                                            onToggle = { onAction(HabitsAction.ToggleHabit(item.id)) },
                                            onSetValue = { value -> onAction(HabitsAction.SetNumericValue(item.id, value)) },
                                            onClearValue = { value -> clearNumericEntry(item, value) },
                                            selectedDate = uiState.selectedDate,
                                            onClick = { onHabitClick(item.id) },
                                            modifier = Modifier.offset { IntOffset(offsetPx.roundToInt(), 0) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet || editingHabit != null) {
        AddHabitSheet(
            paletteColors = paletteColors,
            editing = editingHabit,
            onConfirm = { action ->
                onAction(action)
                showAddSheet = false
                editingHabit = null
            },
            onDelete = editingHabit?.let { target ->
                {
                    onAction(HabitsAction.DeleteHabit(target.id))
                    editingHabit = null
                }
            },
            onDismiss = {
                showAddSheet = false
                editingHabit = null
            },
        )
    }
}

@Composable
private fun SwipeBackground(offsetPx: Float, revealPx: Float, modifier: Modifier = Modifier) {
    // Settled must render as genuinely nothing — this background sits behind every row at all
    // times, normally just hidden by the opaque card on top of it. Direction now carries real
    // meaning, not just which side your thumb dragged from: right reveals edit, left reveals
    // archive, matching the well-worn convention (Gmail, Inbox) rather than inventing a new one.
    if (offsetPx == 0f) return

    val isStartToEnd = offsetPx > 0f
    val containerColor = if (isStartToEnd) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val onContainerColor = if (isStartToEnd) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.largeIncreased)
            .background(containerColor),
        contentAlignment = if (isStartToEnd) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = if (isStartToEnd) Icons.Outlined.Edit else Icons.Outlined.Archive,
            contentDescription = if (isStartToEnd) "Edit" else "Archive",
            tint = onContainerColor,
            modifier = Modifier
                .padding(horizontal = Spacing.large)
                // Scales up with the drag itself rather than a separate timed animation - the
                // gesture IS the animation driver, so the icon growing in reads as a direct,
                // tactile response to how far your thumb has actually committed to the swipe.
                .graphicsLayer {
                    val progress = (abs(offsetPx) / revealPx).coerceIn(0f, 1f)
                    val scale = 0.6f + 0.4f * progress
                    scaleX = scale
                    scaleY = scale
                },
        )
    }
}

/** A one-time reward for actually finishing, not a status readout — only exists while every
 * habit is checked off, so its own drawn checkmark gets to draw in fresh every time it appears. */
@Composable
private fun AllDoneBanner(modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val checkProgress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 420, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "allDoneCheck",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large, vertical = Spacing.default),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.default),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                DrawnCheckmark(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    progress = checkProgress,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column {
                Text(text = "All done for today", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Every habit checked off. Nice work.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun EmptyHabitsState(onAddHabit: () -> Unit, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // True vertical center of the space below the (now compact, fixed) top bar reads as too low
    // — people perceive "centered" relative to the whole screen including the header, not just
    // the remaining space beneath it. A slight upward bias corrects for that.
    Box(modifier = modifier, contentAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.2f)) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                scaleIn(
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                    initialScale = 0.82f,
                ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(Spacing.large),
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    DrawnSproutIllustration(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(76.dp),
                    )
                }
                Spacer(Modifier.height(Spacing.large))
                Text(
                    text = "Nothing tracked yet",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(Spacing.small))
                Text(
                    text = "Add your first habit and watch it grow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(Spacing.large))
                Button(onClick = onAddHabit) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Add a habit")
                }
            }
        }
    }
}
