package dev.dhev.nines

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import dev.dhev.nines.ui.nav.ArchivedHabitsRoute
import dev.dhev.nines.ui.nav.HabitDetailRoute
import dev.dhev.nines.ui.nav.HabitsRoute
import dev.dhev.nines.ui.nav.SettingsRoute
import dev.dhev.nines.ui.nav.AppearanceRoute
import dev.dhev.nines.ui.nav.OnboardingRoute
import dev.dhev.nines.data.OnboardingCompletionStore
import dev.dhev.nines.data.AppearancePreferencesStore
import dev.dhev.nines.data.AppearancePreferences
import dev.dhev.nines.ui.onboarding.OnboardingScreen
import dev.dhev.nines.ui.settings.ArchivedHabitsScreen
import dev.dhev.nines.ui.settings.ArchivedHabitsViewModel
import dev.dhev.nines.ui.settings.SettingsScreen
import dev.dhev.nines.ui.settings.AppearanceScreen
import dev.dhev.nines.ui.settings.AppearanceViewModel
import dev.dhev.nines.ui.theme.NinesTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appearanceStore = koinInject<AppearancePreferencesStore>()
            val appearance by appearanceStore.appearance.collectAsStateWithLifecycle(initialValue = AppearancePreferences())
            NinesTheme(colorSource = appearance.colorSource, customSeedArgb = appearance.customSeedArgb) {
                val onboardingStore = koinInject<OnboardingCompletionStore>()
                val onboardingCompleted by onboardingStore.completed.collectAsStateWithLifecycle(initialValue = null)
                if (onboardingCompleted == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    NinesNavRoot(onboardingStore, onboardingCompleted == true)
                }
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
private fun NinesNavRoot(
    onboardingStore: OnboardingCompletionStore,
    onboardingCompleted: Boolean,
) {
    val scope = rememberCoroutineScope()
    val backStack = remember {
        mutableStateListOf<Any>(if (onboardingCompleted) HabitsRoute else OnboardingRoute(replay = false))
    }

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
                    onSettingsClick = { backStack.add(SettingsRoute) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            entry<SettingsRoute> {
                SettingsScreen(
                    onBack = dropUnlessResumed { backStack.removeLastOrNull() },
                    onArchivedHabitsClick = { backStack.add(ArchivedHabitsRoute) },
                    onAppearanceClick = { backStack.add(AppearanceRoute) },
                    onHowNinesWorksClick = { backStack.add(OnboardingRoute(replay = true)) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            entry<AppearanceRoute> {
                val viewModel = koinViewModel<AppearanceViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                AppearanceScreen(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                    onBack = dropUnlessResumed { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            entry<OnboardingRoute> { route ->
                OnboardingScreen(
                    onComplete = {
                        if (route.replay) backStack.removeLastOrNull()
                        else {
                            scope.launch { onboardingStore.markCompleted() }
                            backStack.clear()
                            backStack.add(HabitsRoute)
                        }
                    },
                    onExit = {
                        if (route.replay) backStack.removeLastOrNull()
                        else {
                            scope.launch { onboardingStore.markCompleted() }
                            backStack.clear()
                            backStack.add(HabitsRoute)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            entry<ArchivedHabitsRoute> {
                val viewModel = koinViewModel<ArchivedHabitsViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                ArchivedHabitsScreen(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                    onBack = dropUnlessResumed { backStack.removeLastOrNull() },
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
