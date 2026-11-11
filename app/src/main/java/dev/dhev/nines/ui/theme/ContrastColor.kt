package dev.dhev.nines.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun contrastingIconColor(background: Color): Color = if (background.luminance() > 0.5f) Color.Black else Color.White
