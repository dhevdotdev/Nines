package dev.dhev.nines.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.ktx.harmonizeWithPrimary
import com.materialkolor.rememberDynamicColorScheme

data class HabitColors(
    val container: Color,
    val onContainer: Color,
    val accent: Color,
)

/**
 * The actual color-derivation math (harmonize seed toward the app's primary hue, then run
 * through the same tone-curve generation dynamic color itself uses). Plain, non-composable, so
 * it can be batch-computed for many seeds inside one `remember` block — see
 * [rememberHabitColorsForPalette] — rather than paying real HCT/CAM16 math per item, per call.
 */
private fun deriveHabitColors(seed: Color, currentScheme: ColorScheme, isDark: Boolean): HabitColors {
    val harmonized = currentScheme.harmonizeWithPrimary(seed)
    // TonalSpot: calm, low-chroma — Material's own "safe default, close to stock Material You."
    // Expressive maximizes chroma by design, which reads as heavy at the small sizes habit
    // accents are used at (icon chips, pills, swatches) rather than a single hero surface.
    val scheme = dynamicColorScheme(
        seedColor = harmonized,
        isDark = isDark,
        style = PaletteStyle.TonalSpot,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
    )
    return HabitColors(
        container = scheme.primaryContainer,
        onContainer = scheme.onPrimaryContainer,
        accent = scheme.primary,
    )
}

/** Single-habit convenience wrapper — memoized, but recomputes independently per call site. */
@Composable
fun rememberHabitColors(seed: Color, isDark: Boolean = isSystemInDarkTheme()): HabitColors {
    val currentScheme = MaterialTheme.colorScheme
    return remember(seed, currentScheme.primary, isDark) {
        deriveHabitColors(seed, currentScheme, isDark)
    }
}

/**
 * Batch derivation for a fixed palette (e.g. the habit-creation color picker): computes every
 * seed's colors in one remember block, keyed on the palette + theme, not on composition
 * lifetime. Call this from a composable that stays alive across the picker opening and closing
 * (the screen, not the sheet) so the real HCT math for ~20 swatches runs once per theme change,
 * not once per sheet open.
 */
@Composable
fun rememberHabitColorsForPalette(seeds: List<Color>, isDark: Boolean = isSystemInDarkTheme()): List<HabitColors> {
    val currentScheme = MaterialTheme.colorScheme
    return remember(seeds, currentScheme.primary, isDark) {
        seeds.map { seed -> deriveHabitColors(seed, currentScheme, isDark) }
    }
}
