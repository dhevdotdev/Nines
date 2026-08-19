package dev.dhev.nines.ui.onboarding

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
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
import androidx.compose.material3.MotionScheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope

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
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val context = LocalContext.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
    val first = remember(page) { Animatable(0f) }
    val second = remember(page) { Animatable(0f) }
    val accent = remember(page) { Animatable(0f) }
    val motionScheme = MaterialTheme.motionScheme
    LaunchedEffect(page, animationsEnabled) {
        if (animationsEnabled) {
            playSignalMotion(motionScheme, first, second, accent)
        } else {
            first.snapTo(1f)
            second.snapTo(1f)
            accent.snapTo(1f)
        }
    }

    Box(Modifier.size(280.dp)) {
        when (page) {
            0 -> RoutineArt(primary, secondary, tertiary, first.value, second.value, accent.value)
            1 -> TargetArt(primary, secondary, tertiary, first.value, second.value, accent.value)
            2 -> InsightArt(primary, secondary, tertiary, first.value, second.value, accent.value)
            else -> StartArt(primary, secondary, tertiary, first.value, second.value, accent.value)
        }
    }
}

private suspend fun playSignalMotion(
    scheme: MotionScheme,
    first: Animatable<Float, *>,
    second: Animatable<Float, *>,
    accent: Animatable<Float, *>,
) {
    coroutineScope {
        launch { first.animateTo(1f, scheme.fastSpatialSpec()) }
        launch { second.animateTo(1f, scheme.defaultSpatialSpec()) }
        launch { delay(190); accent.animateTo(1f, scheme.defaultSpatialSpec()) }
    }
}

@Composable
private fun RoutineArt(primary: Color, secondary: Color, tertiary: Color, first: Float, second: Float, accent: Float) {
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.offset((28f - (1f - first) * 50f).dp, (38f - (1f - first) * 72f).dp).graphicsLayer { rotationZ = (1f - first) * -55f }.size(78.dp).background(tertiary, CircleShape))
        Box(Modifier.offset((170f + (1f - second) * 92f).dp, (44f - (1f - second) * 18f).dp).graphicsLayer { rotationZ = (1f - second) * 70f }.size(64.dp).background(secondary, RoundedCornerShape(32.dp)))
        Box(Modifier.offset((54f - (1f - second) * 70f).dp, (120f + (1f - second) * 66f).dp).size(172.dp, 14.dp).rotate(-18f - (1f - second) * 42f).background(primary, RoundedCornerShape(7.dp)))
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 34.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(9) { index ->
                val direction = if (index % 2 == 0) -1f else 1f
                val spread = (index - 4) * (1f - accent) * 8f
                Box(Modifier.offset(x = spread.dp, y = ((1f - accent) * 72f * direction).dp).graphicsLayer { scaleX = accent.coerceAtLeast(.15f); scaleY = accent.coerceAtLeast(.15f); rotationZ = (1f - accent) * index * 9f }.size(12.dp).background(primary, CircleShape))
            }
        }
    }
}

@Composable
private fun TargetArt(primary: Color, secondary: Color, tertiary: Color, first: Float, second: Float, accent: Float) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(Modifier.graphicsLayer { scaleX = first.coerceAtLeast(.05f); scaleY = first.coerceAtLeast(.05f); rotationZ = (1f - first) * -70f }.size(166.dp).border(12.dp, secondary, CircleShape))
        Box(Modifier.graphicsLayer { scaleX = second.coerceAtLeast(.05f); scaleY = second.coerceAtLeast(.05f); rotationZ = (1f - second) * 90f }.size(112.dp).border(8.dp, primary, CircleShape))
        Box(Modifier.offset((-(1f - accent) * 128f).dp, (-(1f - accent) * 138f).dp).graphicsLayer { scaleX = accent.coerceAtLeast(.2f); scaleY = accent.coerceAtLeast(.2f) }.size(30.dp).background(tertiary, CircleShape))
        Box(Modifier.offset(0.dp, (82f + (1f - second) * 74f).dp).size(126.dp, 12.dp).rotate(-12f - (1f - second) * 44f).background(primary, RoundedCornerShape(6.dp)))
    }
}

@Composable
private fun InsightArt(primary: Color, secondary: Color, tertiary: Color, first: Float, second: Float, accent: Float) {
    Row(Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 42.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
        listOf(54, 92, 70, 132, 108, 164).forEachIndexed { index, height ->
            val progress = when {
                index < 2 -> first
                index < 4 -> second
                else -> accent
            }
            Box(
                Modifier
                    .graphicsLayer { scaleY = progress.coerceAtLeast(.05f); transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f) }
                    .size(22.dp, height.dp)
                    .background(if (index % 2 == 0) secondary else tertiary, RoundedCornerShape(11.dp)),
            )
        }
        Box(Modifier.offset((-20f + (1f - accent) * 118f).dp, (-150f - (1f - accent) * 72f).dp).graphicsLayer { scaleX = accent.coerceAtLeast(.2f); scaleY = accent.coerceAtLeast(.2f); rotationZ = (1f - accent) * 80f }.size(28.dp).background(primary, CircleShape))
    }
}

@Composable
private fun StartArt(primary: Color, secondary: Color, tertiary: Color, first: Float, second: Float, accent: Float) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        repeat(3) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(3) { column ->
                    val isCenter = row == 1 && column == 1
                    val progress = when {
                        isCenter -> accent
                        row == 0 -> first
                        row == 1 -> second
                        else -> accent
                    }
                    val fromCenterX = (1 - column) * (1f - progress) * 82f
                    val fromCenterY = (1 - row) * (1f - progress) * 82f
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Box(
                            Modifier
                                .offset(fromCenterX.dp, fromCenterY.dp)
                                .graphicsLayer { scaleX = progress.coerceAtLeast(.12f); scaleY = progress.coerceAtLeast(.12f); rotationZ = (1f - progress) * if (isCenter) 35f else -18f }
                                .size(if (isCenter) 48.dp else 32.dp)
                                .background(if (isCenter) primary else if ((row + column) % 2 == 0) secondary else tertiary, RoundedCornerShape(16.dp)),
                        )
                    }
                }
            }
            if (row < 2) Spacer(Modifier.height(14.dp))
        }
    }
}
