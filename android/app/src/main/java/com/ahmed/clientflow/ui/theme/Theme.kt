package com.ahmed.clientflow.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.GoogleFont.Provider
import androidx.compose.ui.text.googlefonts.Font as GoogleFontEntry
import com.ahmed.clientflow.R
import com.ahmed.clientflow.data.DarkThemeMode
import com.ahmed.clientflow.data.AppTheme

private val LightColors = lightColorScheme(
    primary = blue500,
    onPrimary = Color.White,
    primaryContainer = blue100,
    onPrimaryContainer = blue800,
    secondary = teal400,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = teal700,
    tertiary = purple400,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE9FE),
    onTertiaryContainer = purple700,
    error = red400,
    onError = Color.White,
    errorContainer = red50,
    onErrorContainer = red600,
    background = slate50,
    onBackground = slate900,
    surface = Color.White,
    onSurface = slate900,
    surfaceVariant = gray100,
    onSurfaceVariant = gray500,
    outline = gray200,
    outlineVariant = gray200
)

private val DarkColors = darkColorScheme(
    primary = blue300,
    onPrimary = blue900,
    primaryContainer = blue800,
    onPrimaryContainer = blue100,
    secondary = teal200,
    onSecondary = teal700,
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = teal200,
    tertiary = purple200,
    onTertiary = purple700,
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = purple200,
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    background = slate900,
    onBackground = slate100,
    surface = Color(0xFF1E293B),
    onSurface = slate200,
    surfaceVariant = slate800,
    onSurfaceVariant = slate400,
    outline = slate600,
    outlineVariant = slate700
)

private val GreenColors = lightColorScheme(
    primary = themeGreenPrimary,
    onPrimary = Color.White,
    background = themeGreenBackground,
    surface = Color.White,
    onSurface = themeGreenPrimary,
    surfaceVariant = themeGreenPrimary.copy(alpha = 0.05f)
)

private val OrangeColors = lightColorScheme(
    primary = themeOrangePrimary,
    onPrimary = Color.White,
    background = themeOrangeBackground,
    surface = Color.White,
    onSurface = themeOrangePrimary,
    surfaceVariant = themeOrangePrimary.copy(alpha = 0.05f)
)

private val BlueColors = lightColorScheme(
    primary = themeBluePrimary,
    onPrimary = Color.White,
    background = themeBlueBackground,
    surface = Color.White,
    onSurface = themeBluePrimary,
    surfaceVariant = themeBluePrimary.copy(alpha = 0.05f)
)

private val MidnightColors = darkColorScheme(
    primary = themeMidnightPrimary,
    onPrimary = themeMidnightBackground,
    background = themeMidnightBackground,
    surface = themeMidnightSurface,
    onSurface = themeMidnightPrimary,
    surfaceVariant = themeMidnightPrimary.copy(alpha = 0.1f)
)

private val TealColors = lightColorScheme(
    primary = themeTealPrimary,
    onPrimary = Color.White,
    background = themeTealBackground,
    surface = Color.White,
    onSurface = themeTealPrimary,
    surfaceVariant = themeTealPrimary.copy(alpha = 0.05f)
)

private val provider = Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val interFont = GoogleFont("Inter")

private val interFamily = FontFamily(
    GoogleFontEntry(googleFont = interFont, fontProvider = provider)
)

private val InterTypography = AppTypography.run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = interFamily),
        headlineLarge = headlineLarge.copy(fontFamily = interFamily),
        titleLarge = titleLarge.copy(fontFamily = interFamily),
        titleMedium = titleMedium.copy(fontFamily = interFamily),
        bodyLarge = bodyLarge.copy(fontFamily = interFamily),
        bodyMedium = bodyMedium.copy(fontFamily = interFamily),
        bodySmall = bodySmall.copy(fontFamily = interFamily),
        labelLarge = labelLarge.copy(fontFamily = interFamily),
        labelMedium = labelMedium.copy(fontFamily = interFamily),
        labelSmall = labelSmall.copy(fontFamily = interFamily)
    )
}

@Composable
fun ClientFlowTheme(
    darkThemeMode: DarkThemeMode = DarkThemeMode.System,
    appTheme: AppTheme = AppTheme.Default,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = when (darkThemeMode) {
        DarkThemeMode.System -> isSystemInDarkTheme()
        DarkThemeMode.Light -> false
        DarkThemeMode.Dark -> true
    }

    val colorScheme = when (appTheme) {
        AppTheme.Default -> {
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColors
                else -> LightColors
            }
        }
        AppTheme.Green -> GreenColors
        AppTheme.Orange -> OrangeColors
        AppTheme.Blue -> BlueColors
        AppTheme.Midnight -> MidnightColors
        AppTheme.Teal -> TealColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = InterTypography,
        shapes = AppShapes,
        content = content
    )
}
