package dev.dhev.nines

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.dhev.nines.ui.habits.HabitDetailScreen
import dev.dhev.nines.ui.habits.HabitDetailViewModel
import dev.dhev.nines.ui.habits.HabitsScreen
import dev.dhev.nines.ui.habits.HabitsViewModel
import dev.dhev.nines.ui.nav.HabitDetailRoute
import dev.dhev.nines.ui.nav.HabitsRoute
import dev.dhev.nines.ui.theme.NinesTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NinesTheme {
                NinesNavRoot()
            }
        }
    }
}

/**
 * Screen-to-screen motion is intentionally left as Nav3's own built-in default transition here
 * (no custom transitionSpec) rather than a hand-rolled one — two different custom attempts (a
 * cross-screen SharedTransitionLayout container transform, then a self-contained expanding-card
 * overlay) both introduced more jank/flakiness than they were worth. Custom navigation motion is
 * deferred until later; the library's own default is the simplest thing that reliably works.
 */
@Composable
private fun NinesNavRoot() {
    val backStack = remember { mutableStateListOf<Any>(HabitsRoute) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HabitsRoute> {
                val viewModel = koinViewModel<HabitsViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                HabitsScreen(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                    onHabitClick = { habitId ->
                        backStack.add(HabitDetailRoute(habitId))
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            entry<HabitDetailRoute> { route ->
                val viewModel = koinViewModel<HabitDetailViewModel> {
                    parametersOf(route.habitId)
                }
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                HabitDetailScreen(
                    uiState = uiState,
                    onBack = dropUnlessResumed { backStack.removeLastOrNull() },
                    onAction = viewModel::onAction,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
    )
}
