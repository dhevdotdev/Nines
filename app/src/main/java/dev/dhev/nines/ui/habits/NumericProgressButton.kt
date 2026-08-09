package dev.dhev.nines.ui.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
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

/**
 * Same visual language as [CheckOffButton], but every tap logs progress rather than toggling.
 * The icon always stays "+" — swapping to a checkmark once [metTarget] is true used to read as
 * "done, stop tapping" while a tap still silently kept adding, which is exactly backwards. Target
 * met is instead communicated by the filled container + shape change alone. Long-press undoes the
 * last tap's worth of progress, since there was previously no way to correct an accidental extra tap.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NumericProgressButton(
    metTarget: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val spatialSpec = MaterialTheme.motionScheme.habitCompleteSpatialSpec<Float>()

    val shape = if (metTarget) MaterialShapes.Cookie9Sided.toShape() else CircleShape
    val containerColor by animateColorAsState(
        targetValue = if (metTarget) accentColor else Color.Transparent,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>(),
        label = "numericProgressContainerColor",
    )

    fun bounce(toward: Float) {
        scope.launch {
            scale.animateTo(toward, animationSpec = spatialSpec)
            scale.animateTo(1f, animationSpec = spatialSpec)
        }
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(shape)
            .background(containerColor)
            .border(width = 2.dp, color = accentColor, shape = shape)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    onIncrement()
                    bounce(1.2f)
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDecrement()
                    bounce(0.85f)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = if (metTarget) contrastingIconColor(accentColor) else accentColor,
        )
    }
}
