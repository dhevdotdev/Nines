package dev.dhev.nines.data

import dev.dhev.nines.appearance.AppColorSource
import org.junit.Assert.assertEquals
import org.junit.Test

class AppColorSourceTest {
    @Test
    fun wallpaperFallsBackToNinesWhenSystemDynamicColorIsUnavailable() {
        assertEquals(AppColorSource.NINES, AppColorSource.WALLPAPER.effectiveSource(dynamicColorAvailable = false))
    }

    @Test
    fun curatedSourceRemainsSelectedWhenSystemDynamicColorIsUnavailable() {
        assertEquals(AppColorSource.OCEAN, AppColorSource.OCEAN.effectiveSource(dynamicColorAvailable = false))
    }
}
