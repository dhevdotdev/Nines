package dev.dhev.nines.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable

/** The spring behind the habit check-off bounce: a larger, rarer change, so overshoot is wanted. */
@Composable
fun <T> MotionScheme.habitCompleteSpatialSpec(): FiniteAnimationSpec<T> = fastSpatialSpec()
