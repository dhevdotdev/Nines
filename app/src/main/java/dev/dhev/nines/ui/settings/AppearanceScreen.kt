package dev.dhev.nines.ui.settings

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.dhev.nines.appearance.AppColorSource
import dev.dhev.nines.ui.theme.Spacing
import dev.dhev.nines.ui.theme.rememberNinesColorScheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearanceScreen(
    uiState: AppearanceUiState,
    onAction: (AppearanceAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wallpaperAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(
                start = Spacing.large,
                end = Spacing.large,
                top = Spacing.medium,
                bottom = Spacing.extraLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.default),
        ) {
            item {
                Text("Color source", style = MaterialTheme.typography.headlineSmallEmphasized)
                Spacer(Modifier.height(Spacing.extraSmall))
                Text(
                    "Choose a system-personal palette or one made for Nines.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.small))
            }
            items(AppColorSource.entries, key = { it.storageValue }) { source ->
                val available = source != AppColorSource.WALLPAPER || wallpaperAvailable
                ColorSourceOption(
                    source = source,
                    selected = uiState.colorSource == source,
                    enabled = available,
                    onSelect = { onAction(AppearanceAction.SelectColorSource(source)) },
                )
            }
        }
    }
}

@Composable
private fun ColorSourceOption(
    source: AppColorSource,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
) {
    val scheme = rememberNinesColorScheme(colorSource = source)
    val container by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primaryContainer
            enabled -> MaterialTheme.colorScheme.surfaceContainer
            else -> MaterialTheme.colorScheme.surfaceContainerLowest
        },
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "palette option container",
    )
    val label = source.label()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Select $label" }
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onSelect,
            ),
        shape = MaterialTheme.shapes.largeIncreased,
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.default),
            horizontalArrangement = Arrangement.spacedBy(Spacing.default),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PalettePreview(scheme = scheme)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, style = MaterialTheme.typography.titleMediumEmphasized)
                    if (selected) {
                        Spacer(Modifier.width(Spacing.small))
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = source.description(wallpaperAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PalettePreview(scheme: androidx.compose.material3.ColorScheme) {
    Box(
        modifier = Modifier
            .size(92.dp, 68.dp)
            .background(scheme.surfaceContainerHigh, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.fillMaxSize().padding(10.dp)) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .size(24.dp)
                    .background(scheme.primary, CircleShape),
            )
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .size(30.dp, 18.dp)
                    .background(scheme.secondary, RoundedCornerShape(9.dp)),
            )
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(28.dp)
                    .background(scheme.tertiaryContainer, RoundedCornerShape(14.dp)),
            )
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(22.dp)
                    .background(scheme.primaryContainer, RoundedCornerShape(8.dp)),
            )
        }
    }
}

private fun AppColorSource.label(): String = when (this) {
    AppColorSource.WALLPAPER -> "Wallpaper"
    AppColorSource.NINES -> "Nines"
    AppColorSource.FOREST -> "Forest"
    AppColorSource.OCEAN -> "Ocean"
    AppColorSource.SUNSET -> "Sunset"
}

private fun AppColorSource.description(wallpaperAvailable: Boolean): String = when (this) {
    AppColorSource.WALLPAPER -> if (wallpaperAvailable) "Matches your system wallpaper" else "Available on Android 12 and newer"
    AppColorSource.NINES -> "Nines' expressive violet"
    AppColorSource.FOREST -> "Grounded green"
    AppColorSource.OCEAN -> "Clear blue"
    AppColorSource.SUNSET -> "Warm orange"
}
