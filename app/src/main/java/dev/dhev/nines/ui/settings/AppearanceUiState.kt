package dev.dhev.nines.ui.settings

import dev.dhev.nines.appearance.AppColorSource

data class AppearanceUiState(
    val colorSource: AppColorSource = AppColorSource.WALLPAPER,
)

sealed interface AppearanceAction {
    data class SelectColorSource(val source: AppColorSource) : AppearanceAction
}
