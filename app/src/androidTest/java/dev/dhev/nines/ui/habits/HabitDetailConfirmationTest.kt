package dev.dhev.nines.ui.habits

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.ui.theme.NinesTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitDetailConfirmationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = LocalDate.of(2026, 8, 22)

    @Test
    fun pastBinaryToggleShowsDateAndResultBeforeDispatching() {
        val actions = mutableListOf<HabitDetailAction>()
        composeRule.setContent {
            NinesTheme(dynamicColor = false) {
                HabitDetailScreen(
                    uiState = state(today.minusDays(1)),
                    onBack = {},
                    onAction = actions::add,
                )
            }
        }

        composeRule.onNodeWithContentDescription("Mark complete").performClick()

        composeRule.onNodeWithText("Mark habit done?").assertIsDisplayed()
        composeRule.onNodeWithText("Mark done").performClick()

        assertTrue(actions.any { it is HabitDetailAction.SelectDate })
        assertTrue(actions.any { it is HabitDetailAction.Toggle })
    }

    @Test
    fun todayBinaryToggleStillDispatchesImmediately() {
        val actions = mutableListOf<HabitDetailAction>()
        composeRule.setContent {
            NinesTheme(dynamicColor = false) {
                HabitDetailScreen(
                    uiState = state(today),
                    onBack = {},
                    onAction = actions::add,
                )
            }
        }

        composeRule.onNodeWithContentDescription("Mark complete").performClick()

        assertEquals(1, actions.count { it is HabitDetailAction.Toggle })
    }

    private fun state(selectedDate: LocalDate) = HabitDetailUiState(
        habitId = 1,
        today = today,
        selectedDate = selectedDate,
        name = "Meditate",
        iconKey = "meditation",
        type = HabitType.BOOLEAN,
        isLoading = false,
    )
}
