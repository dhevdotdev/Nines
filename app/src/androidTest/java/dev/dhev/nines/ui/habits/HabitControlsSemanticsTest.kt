package dev.dhev.nines.ui.habits

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.ui.theme.NinesTheme
import dev.dhev.nines.ui.theme.rememberHabitColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class HabitControlsSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dayRailExposesDateSelectionAndDisablesFutureDays() {
        val today = LocalDate.of(2026, 8, 19)
        var selectedDate: LocalDate? = null

        composeRule.setContent {
            NinesTheme(dynamicColor = false) {
                DayRail(today = today, selectedDate = today, onSelectDate = { selectedDate = it })
            }
        }

        composeRule.onNodeWithContentDescription("Tuesday, August 18")
            .assertIsEnabled()
            .performClick()
        composeRule.onNodeWithContentDescription("Thursday, August 20")
            .assertIsNotEnabled()
        assertEquals(today.minusDays(1), selectedDate)
    }

    @Test
    fun binaryControlExposesActionAndInvokesToggle() {
        var toggled = false

        composeRule.setContent {
            NinesTheme(dynamicColor = false) {
                CheckOffButton(
                    completed = false,
                    onToggle = { toggled = true },
                    accentColor = Color.Blue,
                )
            }
        }

        composeRule.onNodeWithContentDescription("Mark complete")
            .assertIsEnabled()
            .performClick()
        assertTrue(toggled)
    }

    @Test
    fun measurableControlUsesOneAccessibleActionForAddAndLongPressUndo() {
        var increments = 0
        var decrements = 0

        composeRule.setContent {
            NinesTheme(dynamicColor = false) {
                NumericProgressButton(
                    metTarget = false,
                    onIncrement = { increments++ },
                    onDecrement = { decrements++ },
                    accentColor = Color.Blue,
                )
            }
        }

        composeRule.onNodeWithContentDescription("Add progress")
            .assertIsEnabled()
            .performClick()
        composeRule.onNodeWithContentDescription("Add progress")
            .performTouchInput { longClick() }
        assertEquals(1, increments)
        assertEquals(1, decrements)
    }

    @Test
    fun numericEditorExposesClearOnlyForExistingEntry() {
        var cleared = false

        composeRule.setContent {
            NinesTheme(dynamicColor = false) {
                NumericEntryEditor(
                    habitName = "Water",
                    iconKey = "water",
                    colors = rememberHabitColors(Color.Blue, isDark = false),
                    initialValue = 2.5,
                    targetValue = 2.0,
                    targetKind = TargetKind.AT_LEAST,
                    unit = "glasses",
                    selectedDate = LocalDate.of(2026, 8, 19),
                    onConfirm = {},
                    onClear = { cleared = true },
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("Clear entry").assertIsEnabled().performClick()
        assertTrue(cleared)
    }
}
