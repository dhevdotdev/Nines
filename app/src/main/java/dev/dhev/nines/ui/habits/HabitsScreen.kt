package dev.dhev.nines.ui.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import dev.dhev.nines.ui.theme.contrastingIconColor
import dev.dhev.nines.ui.theme.rememberHabitColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HabitsScreen(
    uiState: HabitsUiState,
    onAction: (HabitsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddSheet by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Nines") },
                scrollBehavior = scrollBehavior,
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
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No habits yet — tap + to add one",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.default,
                    end = Spacing.default,
                    top = innerPadding.calculateTopPadding() + Spacing.default,
                    bottom = innerPadding.calculateBottomPadding() + Spacing.default,
                ),
            ) {
                items(uiState.habits, key = { it.id }) { item ->
                    HabitRow(
                        item = item,
                        onToggle = { onAction(HabitsAction.ToggleHabit(item.id)) },
                        onIncrement = { delta -> onAction(HabitsAction.IncrementHabit(item.id, delta)) },
                        modifier = Modifier
                            .animateItem()
                            .padding(bottom = Spacing.medium),
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        AddHabitSheet(
            onConfirm = { action ->
                onAction(action)
                showAddSheet = false
            },
            onDismiss = { showAddSheet = false },
        )
    }
}

private enum class FrequencyType { DAILY, TIMES_PER_WEEK, EVERY_N_DAYS }

@Suppress("DEPRECATION") // rememberModalBottomSheetState: flagged replacement has no such function in 1.5.0-alpha25 yet
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AddHabitSheet(onConfirm: (HabitsAction.CreateHabit) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(HabitPalette.default) }
    var selectedIcon by remember { mutableStateOf(HabitIcons.default) }
    var type by remember { mutableStateOf(HabitType.BOOLEAN) }
    var targetText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var targetKind by remember { mutableStateOf(TargetKind.AT_LEAST) }
    var frequencyType by remember { mutableStateOf(FrequencyType.DAILY) }
    var timesPerWeek by remember { mutableStateOf(3) }
    var everyNDays by remember { mutableStateOf(2) }
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
            Text(text = "New habit", style = MaterialTheme.typography.headlineSmallEmphasized)
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
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = type == HabitType.BOOLEAN,
                    onClick = { type = HabitType.BOOLEAN },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text("Yes / no") }
                SegmentedButton(
                    selected = type == HabitType.NUMERICAL,
                    onClick = { type = HabitType.NUMERICAL },
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
                HabitPalette.seeds.forEach { seed ->
                    val previewColor = rememberHabitColors(seed).accent
                    val selected = seed == selectedColor
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.1f else 1f,
                        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                        label = "colorSwatchScale",
                    )
                    val borderWidth by animateDpAsState(
                        targetValue = if (selected) 3.dp else 0.dp,
                        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                        label = "colorSwatchBorderWidth",
                    )
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
                            ) { selectedColor = seed },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.large))
            Text(text = "Icon", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(Spacing.extraSmall))
            val iconPreviewColor by animateColorAsState(
                targetValue = rememberHabitColors(selectedColor).accent,
                animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                label = "iconPreviewColor",
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                HabitIcons.options.forEach { (key, icon) ->
                    val selected = key == selectedIcon
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.08f else 1f,
                        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                        label = "iconChipScale",
                    )
                    val chipBackground by animateColorAsState(
                        targetValue = if (selected) iconPreviewColor else MaterialTheme.colorScheme.surfaceContainerHigh,
                        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                        label = "iconChipBackground",
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clip(RoundedCornerShape(14.dp))
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
                    onConfirm(
                        HabitsAction.CreateHabit(
                            name = name.trim(),
                            color = selectedColor.toArgb(),
                            iconKey = selectedIcon,
                            type = type,
                            targetNumerator = numerator,
                            targetDenominator = denominator,
                            targetValue = if (type == HabitType.NUMERICAL) targetText.toDoubleOrNull() else null,
                            targetKind = if (type == HabitType.NUMERICAL) targetKind else null,
                            unit = unit.ifBlank { null },
                        ),
                    )
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add habit")
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
