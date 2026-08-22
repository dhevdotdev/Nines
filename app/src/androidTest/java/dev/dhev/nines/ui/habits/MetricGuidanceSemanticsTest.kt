package dev.dhev.nines.ui.habits

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.ui.theme.NinesTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetricGuidanceSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun booleanDetailExposesConsistencyExplanation() {
        composeRule.setContent {
            NinesTheme(dynamicColor = false) {
                HabitDetailScreen(
                    uiState = HabitDetailUiState(
                        habitId = 1,
                        today = LocalDate.of(2026, 8, 22),
                        selectedDate = LocalDate.of(2026, 8, 22),
                        name = "Meditate",
                        iconKey = "meditation",
                        type = HabitType.BOOLEAN,
                        currentScore = 0.8,
                        isLoading = false,
                    ),
                    onBack = {},
                    onAction = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Explain Consistency score").performClick()

        composeRule.onNodeWithText("Consistency score").assertIsDisplayed()
        composeRule.onNodeWithText(metricGuidance(MetricGuidanceKey.SCORE).explanation).assertIsDisplayed()
    }

    @Test
    fun numericalTargetProgressUsesTheSameGuidanceContract() {
        var requested: MetricGuidance? = null
        val today = LocalDate.of(2026, 8, 22)
        composeRule.setContent {
            NinesTheme(dynamicColor = false) {
                InsightsSection(
                    uiState = HabitDetailUiState(
                        type = HabitType.NUMERICAL,
                        targetValue = 20.0,
                        targetKind = TargetKind.AT_LEAST,
                        history = (0 until 7).map { offset ->
                            DayCell(
                                date = today.minusDays(offset.toLong()),
                                state = null,
                                isToday = offset == 0,
                                numericValue = 20.0,
                            )
                        },
                        isLoading = false,
                    ),
                    accent = Color(0xFF6750A4),
                    onGuidanceRequested = { requested = it },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Explain Target progress").performClick()

        assertEquals(MetricGuidanceKey.TARGET, requested?.key)
    }
}
