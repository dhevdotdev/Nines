package dev.dhev.nines.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dhev.nines.appearance.AppColorSource
import dev.dhev.nines.ui.theme.NinesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppearanceSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun colorSourcesAreVisibleAndSelectionDispatchesAction() {
        var selected: AppColorSource? = null
        composeRule.setContent {
            NinesTheme(dynamicColor = false) {
                AppearanceScreen(
                    uiState = AppearanceUiState(colorSource = AppColorSource.NINES),
                    onAction = { action ->
                        if (action is AppearanceAction.SelectColorSource) selected = action.source
                    },
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        listOf("Wallpaper", "Nines", "Forest", "Ocean", "Sunset").forEach {
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }
        composeRule.onNodeWithContentDescription("Select Forest").performClick()

        assertEquals(AppColorSource.FOREST, selected)
    }
}
