package com.ahmed.clientflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.GoogleFont.Provider
import androidx.compose.ui.text.googlefonts.Font as GoogleFontEntry
import com.ahmed.clientflow.R

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    secondary = Color(0xFF0F766E),
    tertiary = Color(0xFF7C3AED),
    background = Color(0xFFF7F8FC),
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFDC2626)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF93C5FD),
    secondary = Color(0xFF5EEAD4),
    tertiary = Color(0xFFC4B5FD)
)

private val provider = Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val interFont = GoogleFont("Inter")

private val appTypographyFont = FontFamily(
    GoogleFontEntry(googleFont = interFont, fontProvider = provider)
)

@Composable
fun ClientFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography.copy(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = appTypographyFont),
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = appTypographyFont),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = appTypographyFont),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = appTypographyFont),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = appTypographyFont),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = appTypographyFont)
        ),
        content = content
    )
}
