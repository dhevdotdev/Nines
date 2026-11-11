package dev.dhev.nines.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.dhev.nines.ui.habits.HabitIcons
import dev.dhev.nines.ui.theme.Spacing
import dev.dhev.nines.ui.theme.contrastingIconColor
import dev.dhev.nines.ui.theme.rememberHabitColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArchivedHabitsScreen(
    uiState: ArchivedHabitsUiState,
    onAction: (ArchivedHabitsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Archived habits") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.archivedHabits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.large),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(Spacing.extraLarge * 2))
                        Box(
                            modifier =
                            Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                        Spacer(Modifier.height(Spacing.default))
                        Text(
                            text = "Habits you delete from the list end up here, not gone for good.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding =
                    PaddingValues(
                        start = Spacing.large,
                        end = Spacing.large,
                        top = Spacing.default,
                        bottom = Spacing.large,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    items(uiState.archivedHabits, key = { it.id }) { habit ->
                        ArchivedHabitRow(
                            habit = habit,
                            onRestore = { onAction(ArchivedHabitsAction.RestoreHabit(habit.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivedHabitRow(
    habit: ArchivedHabitItem,
    onRestore: () -> Unit,
) {
    val colors = rememberHabitColors(Color(habit.color))
    val haptics = LocalHapticFeedback.current
    var restored by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.default, vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.default),
        ) {
            Box(
                modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = HabitIcons.iconFor(habit.iconKey),
                    contentDescription = null,
                    tint = contrastingIconColor(colors.accent),
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            // Disabled the instant it's tapped rather than waiting on the restore to actually
            // land - the row is about to disappear from this list anyway (it moves back to the
            // main list), so a double-tap only risks a duplicate restore call, never a useful one.
            IconButton(
                onClick = {
                    if (!restored) {
                        restored = true
                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        onRestore()
                    }
                },
                enabled = !restored,
            ) {
                Icon(
                    imageVector = Icons.Filled.Unarchive,
                    contentDescription = "Restore ${habit.name}",
                    tint = colors.accent,
                )
            }
        }
    }
}
