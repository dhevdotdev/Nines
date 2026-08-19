package dev.dhev.nines.ui.onboarding

import androidx.activity.compose.BackHandler
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val body: String,
)

private val pages = listOf(
    OnboardingPage(
        "Build a reliable routine",
        "Nines helps you turn small actions into a pattern you can trust.",
    ),
    OnboardingPage(
        "Log entries against targets",
        "Mark a habit done or record a value. Nines compares each entry with its target.",
    ),
    OnboardingPage(
        "See the pattern, not just the streak",
        "Score, streaks, and insights show how your routine is holding up over time.",
    ),
    OnboardingPage(
        "Ready when you are",
        "Create your first habit and start building your own signal.",
    ),
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    BackHandler {
        if (currentPage == 0) onExit()
        else scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onExit) { Text("Skip") }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${currentPage + 1} of ${pages.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    repeat(pages.size) { page ->
                        Box(
                            modifier = Modifier
                                .size(width = if (page == currentPage) 24.dp else 8.dp, height = 8.dp)
                                .background(
                                    if (page == currentPage) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(4.dp),
                                )
                                .semantics { contentDescription = "Onboarding page ${page + 1}" },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (currentPage == pages.lastIndex) {
                    Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Get started")
                    }
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Next") }
                }
            }
        },
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { page ->
            val content = pages[page]
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                OnboardingIllustration(page)
                Spacer(Modifier.height(32.dp))
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = content.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun OnboardingIllustration(page: Int) {
    val primary = MaterialTheme.colorScheme.primary
    val container = MaterialTheme.colorScheme.primaryContainer
    val context = LocalContext.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }
    val pulse = if (animationsEnabled) {
        val transition = rememberInfiniteTransition(label = "onboarding illustration")
        val animatedPulse by transition.animateFloat(
            0.94f,
            1.04f,
            infiniteRepeatable(tween(1800), RepeatMode.Reverse),
            label = "illustration pulse",
        )
        animatedPulse
    } else {
        1f
    }
    Box(
        modifier = Modifier.size(220.dp).scale(pulse).background(container, RoundedCornerShape(64.dp)),
        contentAlignment = Alignment.Center,
    ) {
        when (page) {
            0 -> WelcomeIllustration(primary)
            1 -> EntryTargetIllustration(primary)
            2 -> PatternIllustration(primary)
            else -> Icon(Icons.Filled.LocalFireDepartment, null, tint = primary, modifier = Modifier.size(72.dp))
        }
    }
}

@Composable
private fun WelcomeIllustration(color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            Box(
                Modifier.size((32 + index * 12).dp).background(color.copy(alpha = .45f + index * .15f), CircleShape),
            )
        }
    }
}

@Composable
private fun EntryTargetIllustration(color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(Icons.Filled.Flag, null, tint = color, modifier = Modifier.size(58.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { Box(Modifier.size(16.dp).background(color, CircleShape)) }
        }
    }
}

@Composable
private fun PatternIllustration(color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        listOf(30, 52, 42, 76, 62).forEach { height ->
            Box(Modifier.size(width = 18.dp, height = height.dp).background(color, RoundedCornerShape(9.dp)))
        }
    }
}
