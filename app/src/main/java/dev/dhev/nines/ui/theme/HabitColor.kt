package dev.dhev.nines.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.ktx.harmonizeWithPrimary
import com.materialkolor.rememberDynamicColorScheme

data class HabitColors(
    val container: Color,
    val onContainer: Color,
    val accent: Color,
)

/**
 * Derives a full, contrast-guaranteed color pair for a habit from its raw seed color: the
 * seed is first nudged toward the app's current primary hue (so per-habit accents never clash
 * with the active theme), then run through the same tone-curve math dynamic color itself uses.
 */
@Composable
fun rememberHabitColors(seed: Color, isDark: Boolean = isSystemInDarkTheme()): HabitColors {
    val currentScheme = MaterialTheme.colorScheme
    val harmonized = remember(seed, currentScheme.primary) {
        currentScheme.harmonizeWithPrimary(seed)
    }
    // TonalSpot: calm, low-chroma — Material's own "safe default, close to stock Material You."
    // Expressive maximizes chroma by design, which reads as heavy at the small sizes habit
    // accents are used at (icon chips, pills, swatches) rather than a single hero surface.
    val scheme = rememberDynamicColorScheme(
        seedColor = harmonized,
        isDark = isDark,
        style = PaletteStyle.TonalSpot,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
    )
    return remember(scheme) {
        HabitColors(
            container = scheme.primaryContainer,
            onContainer = scheme.onPrimaryContainer,
            accent = scheme.primary,
        )
    }
}
