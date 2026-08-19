package dev.dhev.nines.ui.onboarding

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class OnboardingPage(val title: String, val body: String)

private val pages = listOf(
    OnboardingPage("Build a reliable routine", "Nines helps you turn small actions into a pattern you can trust."),
    OnboardingPage("Log entries against targets", "Mark a habit done or record a value. Nines compares each entry with its target."),
    OnboardingPage("See the pattern, not just the streak", "Score, streaks, and insights show how your routine is holding up over time."),
    OnboardingPage("Ready when you are", "Create your first habit and start building your own signal."),
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
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) { TextButton(onClick = onExit) { Text("Skip") } }
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${currentPage + 1} of ${pages.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(pages.size) { page ->
                        Box(
                            Modifier
                                .size(width = if (page == currentPage) 24.dp else 8.dp, height = 8.dp)
                                .background(
                                    if (page == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
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
                        onClick = { scope.launch { pagerState.animateScrollToPage(currentPage + 1) } },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Next") }
                }
            }
        },
    ) { padding ->
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize().padding(padding)) { page ->
            val content = pages[page]
            Column(
                Modifier.fillMaxSize().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                OnboardingIllustration(page)
                Spacer(Modifier.height(32.dp))
                Text(content.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(content.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun OnboardingIllustration(page: Int) {
    val primary = MaterialTheme.colorScheme.primary
    val container = MaterialTheme.colorScheme.primaryContainer
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val context = LocalContext.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
    val drift = remember(page) { Animatable(-4f) }
    LaunchedEffect(page, animationsEnabled) {
        if (animationsEnabled) {
            drift.animateTo(0f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
        } else {
            drift.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .size(260.dp)
            .background(container.copy(alpha = .65f), RoundedCornerShape(72.dp))
            .border(5.dp, primary, RoundedCornerShape(72.dp)),
    ) {
        Box(Modifier.offset(24.dp, (28f + drift.value).dp).size(94.dp).background(secondary, RoundedCornerShape(48.dp)))
        Box(Modifier.offset((142f - drift.value).dp, (142f - drift.value / 2).dp).size(78.dp).background(tertiary, RoundedCornerShape(40.dp)))
        Box(
            Modifier
                .offset(54.dp, 92.dp)
                .size(160.dp, 14.dp)
                .rotate(-24f + drift.value / 2)
                .background(primary, RoundedCornerShape(7.dp)),
        )
        Box(Modifier.offset((92f + drift.value / 2).dp, 76.dp).size(32.dp).background(container, RoundedCornerShape(16.dp)))
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(if (page == 3) 9 else 6) { index ->
                Box(
                    Modifier
                        .offset(y = if (index % 2 == 0) drift.value.dp else (-drift.value / 2).dp)
                        .size((10 + index % 3 * 3).dp)
                        .background(primary.copy(alpha = .8f), RoundedCornerShape(8.dp)),
                )
            }
        }
    }
}
