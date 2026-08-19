package dev.dhev.nines.ui.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dhev.nines.ui.theme.NinesTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun nextReachesTheGetStartedPage() {
        composeRule.setContent {
            NinesTheme(dynamicColor = false) {
                OnboardingScreen(onComplete = {}, onExit = {})
            }
        }

        composeRule.onNodeWithText("Build a reliable routine").assertIsDisplayed()
        repeat(3) { composeRule.onNodeWithText("Next").performClick() }
        composeRule.onNodeWithText("Ready when you are").assertIsDisplayed()
        composeRule.onNodeWithText("Get started").assertIsDisplayed()
    }

    @Test
    fun skipExitsTheFlow() {
        var exited = false
        composeRule.setContent {
            NinesTheme(dynamicColor = false) {
                OnboardingScreen(onComplete = {}, onExit = { exited = true })
            }
        }

        composeRule.onNodeWithText("Skip").performClick()
        assertTrue(exited)
    }
}
