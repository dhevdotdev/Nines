package dev.dhev.nines.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import dev.dhev.nines.R
import androidx.compose.ui.text.font.Font as DeviceFont

private val googleFontProvider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs,
    )

private val googleSansFlex = GoogleFont("Google Sans Flex")

/** The downloadable font first, then the platform sans-serif if the provider is unavailable. */
private fun sansFlex(weight: FontWeight) = FontFamily(
    Font(googleFont = googleSansFlex, fontProvider = googleFontProvider, weight = weight),
    DeviceFont(DeviceFontFamilyName("sans-serif"), weight = weight),
)

// One shared FontFamily instance per weight tier, reused across every role that needs it.
private val sansFlexBold = sansFlex(FontWeight.Bold)
private val sansFlexSemiBold = sansFlex(FontWeight.SemiBold)
private val sansFlexMedium = sansFlex(FontWeight.Medium)
private val sansFlexNormal = sansFlex(FontWeight.Normal)

/**
 * Every distinct FontFamily NinesTypography references. Resolving a downloadable font is a
 * real one-time cost (first-touch typeface fetch/rasterization) — see [NinesTheme]'s prewarm,
 * which resolves these at app launch instead of lazily on first use (e.g. opening a sheet).
 */
val NinesFontFamilies: List<FontFamily> = listOf(sansFlexBold, sansFlexSemiBold, sansFlexMedium, sansFlexNormal)

private val defaultType = Typography()

val NinesTypography =
    Typography(
        displayLarge = defaultType.displayLarge.copy(fontFamily = sansFlexBold),
        displayMedium = defaultType.displayMedium.copy(fontFamily = sansFlexBold),
        displaySmall = defaultType.displaySmall.copy(fontFamily = sansFlexBold),
        headlineLarge = defaultType.headlineLarge.copy(fontFamily = sansFlexSemiBold),
        headlineMedium = defaultType.headlineMedium.copy(fontFamily = sansFlexSemiBold),
        headlineSmall = defaultType.headlineSmall.copy(fontFamily = sansFlexSemiBold),
        titleLarge = defaultType.titleLarge.copy(fontFamily = sansFlexMedium),
        titleMedium = defaultType.titleMedium.copy(fontFamily = sansFlexMedium),
        titleSmall = defaultType.titleSmall.copy(fontFamily = sansFlexMedium),
        bodyLarge = defaultType.bodyLarge.copy(fontFamily = sansFlexNormal),
        bodyMedium = defaultType.bodyMedium.copy(fontFamily = sansFlexNormal),
        bodySmall = defaultType.bodySmall.copy(fontFamily = sansFlexNormal),
        labelLarge = defaultType.labelLarge.copy(fontFamily = sansFlexMedium),
        labelMedium = defaultType.labelMedium.copy(fontFamily = sansFlexMedium),
        labelSmall = defaultType.labelSmall.copy(fontFamily = sansFlexMedium),
    )
