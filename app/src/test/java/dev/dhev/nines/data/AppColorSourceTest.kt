package dev.dhev.nines.data

import androidx.compose.ui.graphics.Color
import dev.dhev.nines.appearance.AppColorSource
import dev.dhev.nines.appearance.toSeedArgb
import org.junit.Assert.assertEquals
import org.junit.Test

class AppColorSourceTest {
    @Test
    fun customColorConvertsToAnArgbSeedInsteadOfComposePackedColor() {
        assertEquals(0xFF00A896L, Color(0xFF00A896).toSeedArgb())
    }

    @Test
    fun wallpaperFallsBackToNinesWhenSystemDynamicColorIsUnavailable() {
        assertEquals(AppColorSource.NINES, AppColorSource.WALLPAPER.effectiveSource(dynamicColorAvailable = false))
    }

    @Test
    fun curatedSourceRemainsSelectedWhenSystemDynamicColorIsUnavailable() {
        assertEquals(AppColorSource.OCEAN, AppColorSource.OCEAN.effectiveSource(dynamicColorAvailable = false))
    }

    @Test
    fun knownClassicThemeValuesDeserializeToTheirPalette() {
        assertEquals(AppColorSource.CATPPUCCIN, AppColorSource.fromStorage("catppuccin"))
        assertEquals(AppColorSource.TOKYO_NIGHT, AppColorSource.fromStorage("tokyo_night"))
    }

    @Test
    fun themeCollectionContainsTheCuratedDeveloperThemeSet() {
        assertEquals(
            listOf(
                AppColorSource.CATPPUCCIN,
                AppColorSource.TOKYO_NIGHT,
                AppColorSource.DRACULA,
                AppColorSource.NORD,
                AppColorSource.ONE_DARK,
                AppColorSource.GRUVBOX,
                AppColorSource.SOLARIZED,
                AppColorSource.MONOKAI,
                AppColorSource.KANAGAWA,
                AppColorSource.ROSE_PINE,
                AppColorSource.EVERFOREST,
                AppColorSource.NIGHTFOX,
            ),
            AppColorSource.themeCollection,
        )
    }
}
