package dev.dhev.nines.ui.nav

sealed interface NinesRoute

data object HabitsRoute : NinesRoute

data class HabitDetailRoute(val habitId: Long) : NinesRoute
