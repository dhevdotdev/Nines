package dev.dhev.nines.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import dev.dhev.nines.R

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val googleSansFlex = GoogleFont("Google Sans Flex")

private fun sansFlex(weight: FontWeight) = FontFamily(
    Font(googleFont = googleSansFlex, fontProvider = googleFontProvider, weight = weight),
)

private val defaultType = Typography()

val NinesTypography = Typography(
    displayLarge = defaultType.displayLarge.copy(fontFamily = sansFlex(FontWeight.Bold)),
    displayMedium = defaultType.displayMedium.copy(fontFamily = sansFlex(FontWeight.Bold)),
    displaySmall = defaultType.displaySmall.copy(fontFamily = sansFlex(FontWeight.Bold)),
    headlineLarge = defaultType.headlineLarge.copy(fontFamily = sansFlex(FontWeight.SemiBold)),
    headlineMedium = defaultType.headlineMedium.copy(fontFamily = sansFlex(FontWeight.SemiBold)),
    headlineSmall = defaultType.headlineSmall.copy(fontFamily = sansFlex(FontWeight.SemiBold)),
    titleLarge = defaultType.titleLarge.copy(fontFamily = sansFlex(FontWeight.Medium)),
    titleMedium = defaultType.titleMedium.copy(fontFamily = sansFlex(FontWeight.Medium)),
    titleSmall = defaultType.titleSmall.copy(fontFamily = sansFlex(FontWeight.Medium)),
    bodyLarge = defaultType.bodyLarge.copy(fontFamily = sansFlex(FontWeight.Normal)),
    bodyMedium = defaultType.bodyMedium.copy(fontFamily = sansFlex(FontWeight.Normal)),
    bodySmall = defaultType.bodySmall.copy(fontFamily = sansFlex(FontWeight.Normal)),
    labelLarge = defaultType.labelLarge.copy(fontFamily = sansFlex(FontWeight.Medium)),
    labelMedium = defaultType.labelMedium.copy(fontFamily = sansFlex(FontWeight.Medium)),
    labelSmall = defaultType.labelSmall.copy(fontFamily = sansFlex(FontWeight.Medium)),
)
