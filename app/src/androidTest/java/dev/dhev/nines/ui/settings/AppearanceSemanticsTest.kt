package dev.dhev.nines.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dhev.nines.appearance.AppColorSource
import dev.dhev.nines.ui.theme.NinesTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.pow

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

    @Test
    fun namedPalettesKeepDistinctAccentsAndReadablePairsInBothModes() {
        var selectedSource by mutableStateOf(AppColorSource.CATPPUCCIN)
        var selectedDarkTheme by mutableStateOf(false)
        var scheme: ColorScheme? = null
        composeRule.setContent {
            NinesTheme(darkTheme = selectedDarkTheme, colorSource = selectedSource, dynamicColor = false) {
                scheme = MaterialTheme.colorScheme
            }
        }
        listOf(false, true).forEach { darkTheme ->
            listOf(AppColorSource.CATPPUCCIN, AppColorSource.TOKYO_NIGHT, AppColorSource.DRACULA).forEach { source ->
                selectedDarkTheme = darkTheme
                selectedSource = source
                composeRule.waitForIdle()
                val current = checkNotNull(scheme)
                assertNotEquals("$source primary/secondary", current.primary, current.secondary)
                assertNotEquals("$source primary/tertiary", current.primary, current.tertiary)
                assertNotEquals("$source secondary/tertiary", current.secondary, current.tertiary)
                assertTrue("$source primary pair", contrastRatio(current.primary, current.onPrimary) >= 3.0)
                assertTrue("$source secondary pair", contrastRatio(current.secondary, current.onSecondary) >= 3.0)
                assertTrue("$source tertiary pair", contrastRatio(current.tertiary, current.onTertiary) >= 3.0)
            }
        }
    }

    @Test
    fun wallpaperFallbackAndCustomRemainSingleRootSources() {
        var source by mutableStateOf(AppColorSource.WALLPAPER)
        var darkTheme by mutableStateOf(false)
        var customSeedArgb by mutableStateOf(0xFFCBA6F7L)
        var scheme: ColorScheme? = null
        composeRule.setContent {
            NinesTheme(darkTheme = darkTheme, colorSource = source, customSeedArgb = customSeedArgb, dynamicColor = false) {
                scheme = MaterialTheme.colorScheme
            }
        }
        listOf(false, true).forEach { isDark ->
            darkTheme = isDark
            source = AppColorSource.WALLPAPER
            composeRule.waitForIdle()
            val wallpaper = checkNotNull(scheme)
            source = AppColorSource.NINES
            composeRule.waitForIdle()
            val nines = checkNotNull(scheme)
            source = AppColorSource.CUSTOM
            customSeedArgb = 0xFF00A896L
            composeRule.waitForIdle()
            val custom = checkNotNull(scheme)
            assertEquals("Wallpaper fallback in ${if (isDark) "dark" else "light"} mode", nines, wallpaper)
            assertTrue(contrastRatio(custom.primary, custom.onPrimary) >= 3.0)
        }
    }

    private fun contrastRatio(foreground: androidx.compose.ui.graphics.Color, background: androidx.compose.ui.graphics.Color): Double {
        fun channel(value: Float): Double = if (value <= 0.04045f) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        fun luminance(color: androidx.compose.ui.graphics.Color): Double =
            0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
        val lighter = maxOf(luminance(foreground), luminance(background))
        val darker = minOf(luminance(foreground), luminance(background))
        return (lighter + 0.05) / (darker + 0.05)
    }
}
