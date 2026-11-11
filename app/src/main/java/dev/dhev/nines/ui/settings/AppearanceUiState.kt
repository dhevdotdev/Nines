package dev.dhev.nines.ui.settings

import dev.dhev.nines.appearance.AppColorSource

data class AppearanceUiState(
    val colorSource: AppColorSource = AppColorSource.WALLPAPER,
    val customSeedArgb: Long = 0xFFCBA6F7L,
)

sealed interface AppearanceAction {
    data class SelectColorSource(
        val source: AppColorSource,
    ) : AppearanceAction

    data class SelectCustomColor(
        val seedArgb: Long,
    ) : AppearanceAction
}
