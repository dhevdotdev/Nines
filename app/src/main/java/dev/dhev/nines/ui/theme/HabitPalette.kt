package dev.dhev.nines.ui.theme

import androidx.compose.ui.graphics.Color

/** Curated seed colors for habits to pick from — spans the hue wheel at a consistent chroma. */
object HabitPalette {
    val seeds: List<Color> = listOf(
        Color(0xFFE53935), // red
        Color(0xFFF4511E), // deep orange
        Color(0xFFFB8C00), // orange
        Color(0xFFFFB300), // amber
        Color(0xFFFDD835), // yellow
        Color(0xFFC0CA33), // lime
        Color(0xFF7CB342), // light green
        Color(0xFF43A047), // green
        Color(0xFF2E7D32), // forest
        Color(0xFF00897B), // teal
        Color(0xFF00ACC1), // cyan
        Color(0xFF039BE5), // light blue
        Color(0xFF1E88E5), // blue
        Color(0xFF3949AB), // indigo
        Color(0xFF5E35B1), // deep purple
        Color(0xFF8E24AA), // purple
        Color(0xFFD81B60), // pink
        Color(0xFFAD1457), // maroon
        Color(0xFF6D4C41), // brown
        Color(0xFF546E7A), // blue grey
    )

    val default = seeds.first()
}
