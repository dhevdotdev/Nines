package dev.dhev.nines.ui.habits

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * A Room query on-device resolves well under 200ms almost always, so showing a spinner on every
 * screen open would flash more often than it'd ever actually communicate progress - the M3
 * guidance is explicit that an indicator has no business appearing for waits under 200ms. Gated
 * behind that delay, so it only ever shows for the rare slow load, not the routine fast one.
 * [LoadingIndicator] itself is still experimental upstream (stable promotion was reverted), so
 * this wrapper is the one place that dependency is allowed to leak from.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullScreenLoadingIndicator(modifier: Modifier = Modifier) {
    var showIndicator by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        showIndicator = true
    }
    if (showIndicator) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
    }
}
