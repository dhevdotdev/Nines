package dev.dhev.nines.appearance

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun Color.toSeedArgb(): Long = toArgb().toLong() and 0xFFFFFFFFL
