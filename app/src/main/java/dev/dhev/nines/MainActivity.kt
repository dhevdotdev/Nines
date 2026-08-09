package dev.dhev.nines

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.dhev.nines.ui.habits.HabitsScreen
import dev.dhev.nines.ui.habits.HabitsViewModel
import dev.dhev.nines.ui.theme.NinesTheme
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NinesTheme {
                val viewModel = koinViewModel<HabitsViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                HabitsScreen(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
