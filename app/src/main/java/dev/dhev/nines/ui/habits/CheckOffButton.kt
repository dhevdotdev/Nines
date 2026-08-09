package dev.dhev.nines.ui.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import dev.dhev.nines.ui.theme.contrastingIconColor
import dev.dhev.nines.ui.theme.habitCompleteSpatialSpec
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CheckOffButton(
    completed: Boolean,
    onToggle: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val spatialSpec = MaterialTheme.motionScheme.habitCompleteSpatialSpec<Float>()

    val shape = if (completed) MaterialShapes.Cookie9Sided.toShape() else CircleShape
    val containerColor by animateColorAsState(
        targetValue = if (completed) accentColor else Color.Transparent,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>(),
        label = "checkOffContainerColor",
    )
    val borderColor = if (completed) accentColor else MaterialTheme.colorScheme.outline
    // Draws in on completion (a beat slower than the pop/settle motion around it, so the stroke
    // is still visibly travelling once the shape has already landed) and simply retreats quickly
    // on undo, rather than trying to "undraw" itself in reverse.
    val checkProgress by animateFloatAsState(
        targetValue = if (completed) 1f else 0f,
        animationSpec = if (completed) {
            tween(durationMillis = 380, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 120)
        },
        label = "checkDrawProgress",
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(shape)
            .background(containerColor)
            .border(width = 2.dp, color = borderColor, shape = shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
            ) {
                haptics.performHapticFeedback(
                    if (completed) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn,
                )
                onToggle()
                scope.launch {
                    scale.animateTo(1.2f, animationSpec = spatialSpec)
                    scale.animateTo(1f, animationSpec = spatialSpec)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        DrawnCheckmark(
            color = contrastingIconColor(accentColor),
            progress = checkProgress,
            modifier = Modifier.size(22.dp),
        )
    }
}
