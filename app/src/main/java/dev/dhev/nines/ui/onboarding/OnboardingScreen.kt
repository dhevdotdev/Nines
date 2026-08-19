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
import androidx.compose.foundation.shape.CircleShape
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
    val drift = remember(page) { Animatable(12f) }
    LaunchedEffect(page, animationsEnabled) {
        if (animationsEnabled) {
            drift.animateTo(0f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium))
        } else {
            drift.snapTo(0f)
        }
    }

    Box(Modifier.size(260.dp).background(container.copy(alpha = .65f), RoundedCornerShape(72.dp)).border(5.dp, primary, RoundedCornerShape(72.dp))) {
        when (page) {
            0 -> RoutineArt(primary, secondary, tertiary, drift.value)
            1 -> TargetArt(primary, secondary, tertiary, drift.value)
            2 -> InsightArt(primary, secondary, tertiary, drift.value)
            else -> StartArt(primary, secondary, tertiary, drift.value)
        }
    }
}

@Composable
private fun RoutineArt(primary: Color, secondary: Color, tertiary: Color, motion: Float) {
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.offset(28.dp, (38f + motion).dp).size(78.dp).background(tertiary, CircleShape))
        Box(Modifier.offset((150f - motion).dp, 44.dp).size(64.dp).background(secondary, RoundedCornerShape(32.dp)))
        Box(Modifier.offset(44.dp, 112.dp).size(172.dp, 14.dp).rotate(-18f + motion / 3).background(primary, RoundedCornerShape(7.dp)))
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 34.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(9) { index ->
                Box(Modifier.offset(y = if (index % 2 == 0) motion.dp else (-motion / 2).dp).size(12.dp).background(primary, CircleShape))
            }
        }
    }
}

@Composable
private fun TargetArt(primary: Color, secondary: Color, tertiary: Color, motion: Float) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(Modifier.size(166.dp).border(12.dp, secondary, CircleShape))
        Box(Modifier.size(112.dp).border(8.dp, primary, CircleShape))
        Box(Modifier.offset((motion / 2).dp, (-motion).dp).size(30.dp).background(tertiary, CircleShape))
        Box(Modifier.offset(0.dp, 82.dp).size(126.dp, 12.dp).rotate(-12f).background(primary, RoundedCornerShape(6.dp)))
    }
}

@Composable
private fun InsightArt(primary: Color, secondary: Color, tertiary: Color, motion: Float) {
    Row(Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 42.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
        listOf(54, 92, 70, 132, 108, 164).forEachIndexed { index, height ->
            Box(
                Modifier
                    .offset(y = if (index == 3) (-motion).dp else (motion / 3).dp)
                    .size(22.dp, height.dp)
                    .background(if (index % 2 == 0) secondary else tertiary, RoundedCornerShape(11.dp)),
            )
        }
        Box(Modifier.offset((-20f + motion).dp, (-150f).dp).size(28.dp).background(primary, CircleShape))
    }
}

@Composable
private fun StartArt(primary: Color, secondary: Color, tertiary: Color, motion: Float) {
    Column(Modifier.fillMaxSize().padding(46.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(3) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(3) { column ->
                    val isCenter = row == 1 && column == 1
                    Box(
                        Modifier
                            .offset(y = if (isCenter) (-motion).dp else (motion / 3).dp)
                            .size(if (isCenter) 48.dp else 32.dp)
                            .background(if (isCenter) primary else if ((row + column) % 2 == 0) secondary else tertiary, RoundedCornerShape(16.dp)),
                    )
                }
            }
        }
    }
}
