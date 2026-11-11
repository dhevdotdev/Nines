package dev.dhev.nines.ui.nav

sealed interface NinesRoute

data object HabitsRoute : NinesRoute

data class HabitDetailRoute(
    val habitId: Long,
    val openValueEditor: Boolean = false,
) : NinesRoute

data object SettingsRoute : NinesRoute

data object AppearanceRoute : NinesRoute

data object ArchivedHabitsRoute : NinesRoute

data class OnboardingRoute(
    val replay: Boolean,
) : NinesRoute
