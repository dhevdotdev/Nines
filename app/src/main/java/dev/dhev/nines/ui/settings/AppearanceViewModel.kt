package dev.dhev.nines.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhev.nines.data.AppearancePreferencesStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppearanceViewModel(
    private val preferences: AppearancePreferencesStore,
) : ViewModel() {
    val uiState: StateFlow<AppearanceUiState> =
        preferences.appearance
            .map { AppearanceUiState(it.colorSource, it.customSeedArgb) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AppearanceUiState(),
            )

    fun onAction(action: AppearanceAction) {
        when (action) {
            is AppearanceAction.SelectColorSource -> {
                viewModelScope.launch {
                    preferences.select(action.source)
                }
            }

            is AppearanceAction.SelectCustomColor -> {
                viewModelScope.launch {
                    preferences.selectCustom(action.seedArgb)
                }
            }
        }
    }
}
