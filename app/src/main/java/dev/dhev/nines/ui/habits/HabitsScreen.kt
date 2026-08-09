package dev.dhev.nines.ui.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.widthIn
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.ui.theme.HabitPalette
import dev.dhev.nines.ui.theme.Spacing
import dev.dhev.nines.ui.theme.HabitColors
import dev.dhev.nines.ui.theme.contrastingIconColor
import dev.dhev.nines.ui.theme.rememberHabitColorsForPalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HabitsScreen(
    uiState: HabitsUiState,
    onAction: (HabitsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitListItem?>(null) }
    // Computed here (persists across sheet open/close), not inside AddHabitSheet (torn down and
    // rebuilt every time the sheet opens): the real HCT/CAM16 math for ~20 swatches should run
    // once per theme change, not once per sheet open.
    val paletteColors = rememberHabitColorsForPalette(HabitPalette.seeds)

    Scaffold(
        modifier = modifier,
        // Plain, fixed-height, no scrollBehavior attached — fully sticky/pinned rather than the
        // collapsing-on-scroll LargeTopAppBar, which reserved a lot of empty vertical space at
        // rest and only gave it back once you scrolled. Consistent space from the start instead.
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nines",
                        style = MaterialTheme.typography.displaySmall,
                    )
                },
            )
        },
        floatingActionButton = {
            // Medium is M3 Expressive's recommended default size, not the plain (small-tier) FAB.
            MediumFloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add habit",
                    modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                )
            }
        },
    ) { innerPadding ->
        if (uiState.habits.isEmpty() && !uiState.isLoading) {
            EmptyHabitsState(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                onAddHabit = { showAddSheet = true },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.default,
                    end = Spacing.default,
                    top = innerPadding.calculateTopPadding() + Spacing.default,
                    // Scaffold's innerPadding doesn't reserve room for the FAB — without this,
                    // the FAB sits on top of (and eats touches meant for) the last row's own
                    // trailing button.
                    bottom = innerPadding.calculateBottomPadding() + Spacing.default + 96.dp,
                ),
            ) {
                item {
                    // Shown only once every habit is checked off — a small reward for actually
                    // finishing, not just a status readout that's visible the rest of the time.
                    AnimatedVisibility(
                        visible = uiState.habits.all { it.completedToday },
                        enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                            expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                        exit = fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                            shrinkVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                    ) {
                        AllDoneBanner(modifier = Modifier.padding(bottom = Spacing.medium))
                    }
                }
                items(uiState.habits, key = { it.id }) { item ->
                    // Swiping in either direction opens edit — the only way to delete is the
                    // trash icon inside the edit sheet, which is immediate and final (no undo).
                    // The swipe-to-delete-with-undo version of this kept breaking in ways that
                    // were never fully pinned down, so it's gone rather than patched further:
                    // neither direction here ever actually commits (confirmValueChange always
                    // returns false), it only ever springs back to Settled on its own built-in
                    // animation after opening the sheet as a side effect.
                    val dismissState = rememberSwipeToDismissBoxState(
                        // 90% rather than the library's 50% default, so a normal drag has to go
                        // nearly all the way across before opening edit — a half-hearted swipe
                        // (or an incidental diagonal touch while scrolling) shouldn't trigger it.
                        positionalThreshold = { totalDistance -> totalDistance * 0.9f },
                        confirmValueChange = { value ->
                            if (value != SwipeToDismissBoxValue.Settled) {
                                editingHabit = item
                            }
                            false
                        },
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier
                            .animateItem()
                            .padding(bottom = Spacing.medium),
                        backgroundContent = { SwipeBackground(direction = dismissState.dismissDirection) },
                    ) {
                        HabitRow(
                            item = item,
                            onToggle = { onAction(HabitsAction.ToggleHabit(item.id)) },
                            onIncrement = { delta -> onAction(HabitsAction.IncrementHabit(item.id, delta)) },
                            onDecrement = { delta -> onAction(HabitsAction.IncrementHabit(item.id, -delta)) },
                            onSetValue = { value -> onAction(HabitsAction.SetNumericValue(item.id, value)) },
                        )
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
private fun SwipeBackground(direction: SwipeToDismissBoxValue) {
    // Settled must render as genuinely nothing, not fall through to the edit look — this
    // background sits behind every row at all times, normally just hidden by the opaque card on
    // top of it. Both swipe directions mean the same thing now (open edit), so only alignment
    // differs — the icon sits on whichever side your thumb is dragging away from.
    if (direction == SwipeToDismissBoxValue.Settled) return

    val isStartToEnd = direction == SwipeToDismissBoxValue.StartToEnd
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.largeIncreased)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = if (isStartToEnd) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Edit",
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = Spacing.large),
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

private enum class FrequencyType { DAILY, TIMES_PER_WEEK, EVERY_N_DAYS }

private data class FrequencyState(val type: FrequencyType, val timesPerWeek: Int, val everyNDays: Int)

/** Inverts the numerator/denominator encoding the submit button builds, to prefill an edit. */
private fun frequencyStateFor(editing: HabitListItem?): FrequencyState {
    if (editing == null) return FrequencyState(FrequencyType.DAILY, 3, 2)
    val n = editing.targetNumerator
    val d = editing.targetDenominator
    return when {
        n == 1 && d == 1 -> FrequencyState(FrequencyType.DAILY, 3, 2)
        d == 7 -> FrequencyState(FrequencyType.TIMES_PER_WEEK, n.coerceIn(1, 7), 2)
        n == 1 -> FrequencyState(FrequencyType.EVERY_N_DAYS, 3, d.coerceIn(2, 30))
        else -> FrequencyState(FrequencyType.DAILY, 3, 2)
    }
}

private fun formatEditableValue(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

@Suppress("DEPRECATION") // rememberModalBottomSheetState: flagged replacement has no such function in 1.5.0-alpha25 yet
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AddHabitSheet(
    paletteColors: List<HabitColors>,
    editing: HabitListItem?,
    onConfirm: (HabitsAction) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(editing?.name ?: "") }
    var selectedColorIndex by remember {
        mutableStateOf(
            editing?.let { target -> HabitPalette.seeds.indexOfFirst { it.toArgb() == target.color } }
                ?.takeIf { it >= 0 } ?: 0,
        )
    }
    val selectedColor = HabitPalette.seeds[selectedColorIndex]
    var selectedIcon by remember { mutableStateOf(editing?.iconKey ?: HabitIcons.default) }
    var selectedBackground by remember {
        mutableStateOf(CardBackgroundStyle.fromKey(editing?.backgroundStyleKey))
    }
    var type by remember { mutableStateOf(editing?.type ?: HabitType.BOOLEAN) }
    var targetText by remember { mutableStateOf(editing?.targetValue?.let(::formatEditableValue) ?: "") }
    var unit by remember { mutableStateOf(editing?.unit ?: "") }
    var targetKind by remember { mutableStateOf(editing?.targetKind ?: TargetKind.AT_LEAST) }
    val initialFrequency = remember { frequencyStateFor(editing) }
    var frequencyType by remember { mutableStateOf(initialFrequency.type) }
    var timesPerWeek by remember { mutableStateOf(initialFrequency.timesPerWeek) }
    var everyNDays by remember { mutableStateOf(initialFrequency.everyNDays) }
    // Skip the partially-expanded resting state: this sheet is a form, and a peeked-height
    // sheet doesn't grow when the IME appears — it just gets covered by the keyboard.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current

    val canSubmit = name.isNotBlank() && (type == HabitType.BOOLEAN || targetText.toDoubleOrNull() != null)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.large)
                .padding(bottom = Spacing.large),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (editing != null) "Edit habit" else "New habit",
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    modifier = Modifier.weight(1f),
                )
                if (editing != null && onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete habit")
                    }
                }
            }
            Spacer(Modifier.height(Spacing.default))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Name") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.large))
            Text(text = "Type", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(Spacing.extraSmall))
            // Locked once a habit exists: type decides which entry fields (boolean vs numeric)
            // its history already carries, so changing it after entries exist would make that
            // history ambiguous — see HabitRepository.updateHabit.
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = type == HabitType.BOOLEAN,
                    onClick = { if (editing == null) type = HabitType.BOOLEAN },
                    enabled = editing == null,
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text("Yes / no") }
                SegmentedButton(
                    selected = type == HabitType.NUMERICAL,
                    onClick = { if (editing == null) type = HabitType.NUMERICAL },
                    enabled = editing == null,
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text("Measurable") }
            }

            Spacer(Modifier.height(Spacing.large))
            Text(text = "Frequency", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(Spacing.extraSmall))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = frequencyType == FrequencyType.DAILY,
                    onClick = { frequencyType = FrequencyType.DAILY },
                    shape = SegmentedButtonDefaults.itemShape(0, 3),
                ) { Text("Daily") }
                SegmentedButton(
                    selected = frequencyType == FrequencyType.TIMES_PER_WEEK,
                    onClick = { frequencyType = FrequencyType.TIMES_PER_WEEK },
                    shape = SegmentedButtonDefaults.itemShape(1, 3),
                ) { Text("Per week") }
                SegmentedButton(
                    selected = frequencyType == FrequencyType.EVERY_N_DAYS,
                    onClick = { frequencyType = FrequencyType.EVERY_N_DAYS },
                    shape = SegmentedButtonDefaults.itemShape(2, 3),
                ) { Text("Custom") }
            }
            // Revealing/collapsing a content region within the sheet: a size change, moderate
            // extent — the same "partially covers the screen" category as the sheet itself, so
            // default spatial/effects speed (not fast — these aren't small controls).
            val revealEnter = expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
                fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
            val revealExit = shrinkVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
                fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())

            AnimatedVisibility(visible = frequencyType == FrequencyType.TIMES_PER_WEEK, enter = revealEnter, exit = revealExit) {
                Column {
                    Spacer(Modifier.height(Spacing.default))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.default)) {
                        Text(text = "Times per week", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        NumberStepper(value = timesPerWeek, onValueChange = { timesPerWeek = it }, range = 1..7)
                    }
                }
            }
            AnimatedVisibility(visible = frequencyType == FrequencyType.EVERY_N_DAYS, enter = revealEnter, exit = revealExit) {
                Column {
                    Spacer(Modifier.height(Spacing.default))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.default)) {
                        Text(text = "Days between", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        NumberStepper(value = everyNDays, onValueChange = { everyNDays = it }, range = 2..30)
                    }
                }
            }

            AnimatedVisibility(visible = type == HabitType.NUMERICAL, enter = revealEnter, exit = revealExit) {
                Column {
                    Spacer(Modifier.height(Spacing.default))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.default)) {
                        OutlinedTextField(
                            value = targetText,
                            onValueChange = { targetText = it },
                            singleLine = true,
                            label = { Text("Target") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            singleLine = true,
                            label = { Text("Unit") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(Spacing.default))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = targetKind == TargetKind.AT_LEAST,
                            onClick = { targetKind = TargetKind.AT_LEAST },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                        ) { Text("At least") }
                        SegmentedButton(
                            selected = targetKind == TargetKind.AT_MOST,
                            onClick = { targetKind = TargetKind.AT_MOST },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                        ) { Text("At most") }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.large))
            Text(text = "Color", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(Spacing.extraSmall))
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.extraSmall, vertical = Spacing.extraSmall),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                HabitPalette.seeds.forEachIndexed { index, seed ->
                    val previewColor = paletteColors[index].accent
                    val selected = index == selectedColorIndex
                    val scale = if (selected) 1.1f else 1f
                    val borderWidth = if (selected) 3.dp else 0.dp
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clip(CircleShape)
                            .background(previewColor)
                            .border(
                                width = borderWidth,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape,
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { selectedColorIndex = index },
                    )
                }
            }

            val iconPreviewColor by animateColorAsState(
                targetValue = paletteColors[selectedColorIndex].accent,
                animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                label = "iconPreviewColor",
            )
            // The real M3 Expressive squircle — a smooth-cornered RoundedPolygon, not a plain
            // circular-arc RoundedCornerShape, which is what was reading as "rough"/inconsistent
            // around the edges on these swatches.
            val squircle = MaterialShapes.Square.toShape()

            Spacer(Modifier.height(Spacing.large))
            Text(text = "Background", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(Spacing.extraSmall))
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.extraSmall, vertical = Spacing.extraSmall),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                CardBackgroundStyle.entries.forEach { style ->
                    val selected = style == selectedBackground
                    val scale = if (selected) 1.08f else 1f
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clip(squircle)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(
                                width = if (selected) 2.dp else 0.dp,
                                color = iconPreviewColor,
                                shape = squircle,
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { selectedBackground = style },
                        contentAlignment = Alignment.Center,
                    ) {
                        // Inset from the true edge so the pattern's own strokes never reach the
                        // selection border — at this small preview size they bled right up to
                        // it, visually blending with the border and reading as uneven thickness.
                        HabitCardBackground(
                            style = style,
                            color = iconPreviewColor,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxSize(),
                        )
                        if (style == CardBackgroundStyle.NONE) {
                            Text(text = "None", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.large))
            Text(text = "Icon", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(Spacing.extraSmall))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                HabitIcons.options.forEach { (key, icon) ->
                    val selected = key == selectedIcon
                    val scale = if (selected) 1.08f else 1f
                    val chipBackground = if (selected) iconPreviewColor else MaterialTheme.colorScheme.surfaceContainerHigh
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clip(squircle)
                            .background(chipBackground)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { selectedIcon = key },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selected) {
                                contrastingIconColor(iconPreviewColor)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.large))
            Button(
                onClick = {
                    val (numerator, denominator) = when (frequencyType) {
                        FrequencyType.DAILY -> 1 to 1
                        FrequencyType.TIMES_PER_WEEK -> timesPerWeek to 7
                        FrequencyType.EVERY_N_DAYS -> 1 to everyNDays
                    }
                    val targetValue = if (type == HabitType.NUMERICAL) targetText.toDoubleOrNull() else null
                    val effectiveTargetKind = if (type == HabitType.NUMERICAL) targetKind else null
                    onConfirm(
                        if (editing != null) {
                            HabitsAction.UpdateHabit(
                                habitId = editing.id,
                                name = name.trim(),
                                color = selectedColor.toArgb(),
                                iconKey = selectedIcon,
                                backgroundStyleKey = selectedBackground.key.takeIf { selectedBackground != CardBackgroundStyle.NONE },
                                targetNumerator = numerator,
                                targetDenominator = denominator,
                                targetValue = targetValue,
                                targetKind = effectiveTargetKind,
                                unit = unit.ifBlank { null },
                            )
                        } else {
                            HabitsAction.CreateHabit(
                                name = name.trim(),
                                color = selectedColor.toArgb(),
                                iconKey = selectedIcon,
                                backgroundStyleKey = selectedBackground.key.takeIf { selectedBackground != CardBackgroundStyle.NONE },
                                type = type,
                                targetNumerator = numerator,
                                targetDenominator = denominator,
                                targetValue = targetValue,
                                targetKind = effectiveTargetKind,
                                unit = unit.ifBlank { null },
                            )
                        },
                    )
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (editing != null) "Save changes" else "Add habit")
            }
        }
    }
}

@Composable
private fun NumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        FilledTonalIconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            enabled = value > range.first,
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMediumEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 32.dp),
        )
        FilledTonalIconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            enabled = value < range.last,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Increase")
        }
    }
}
