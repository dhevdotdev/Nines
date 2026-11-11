package dev.dhev.nines.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import dev.dhev.nines.appearance.AppColorSource

@Composable
fun NinesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorSource: AppColorSource = AppColorSource.WALLPAPER,
    customSeedArgb: Long = 0xFFCBA6F7L,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        rememberNinesColorScheme(
            colorSource = colorSource,
            customSeedArgb = customSeedArgb,
            darkTheme = darkTheme,
            dynamicColor = dynamicColor,
        )

    // Resolving a downloadable font is a real one-time cost per process (typeface fetch +
    // rasterization). Do it now, while the user is looking at the habit list with nothing
    // time-sensitive happening, instead of lazily the first time a sheet using these fonts opens.
    val fontFamilyResolver = LocalFontFamilyResolver.current
    LaunchedEffect(Unit) {
        NinesFontFamilies.forEach { fontFamilyResolver.preload(it) }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = NinesTypography,
        content = content,
    )
}

@Composable
fun rememberNinesColorScheme(
    colorSource: AppColorSource,
    customSeedArgb: Long = 0xFFCBA6F7L,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
): ColorScheme {
    val context = LocalContext.current
    val dynamicColorAvailable = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val effectiveSource = colorSource.effectiveSource(dynamicColorAvailable)
    return when (effectiveSource) {
        AppColorSource.WALLPAPER -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                rememberDynamicColorScheme(
                    seedColor = Color(AppColorSource.NINES.seedArgb!!.toInt()),
                    isDark = darkTheme,
                    style = PaletteStyle.Expressive,
                    specVersion = ColorSpec.SpecVersion.SPEC_2025,
                )
            }
        }

        else -> {
            rememberDynamicColorScheme(
                seedColor = Color((effectiveSource.seedArgb ?: customSeedArgb).toInt()),
                isDark = darkTheme,
                style = PaletteStyle.Expressive,
                specVersion = ColorSpec.SpecVersion.SPEC_2025,
            )
        }
    }
}
