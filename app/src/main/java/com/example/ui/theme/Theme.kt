package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = NewsCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF881337),
    onPrimaryContainer = Color(0xFFFFE4E6),
    secondary = NewsBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = NewsEmerald,
    onTertiary = Color.White,
    background = NewsBackgroundDark,
    onBackground = NewsTextPrimaryDark,
    surface = NewsSurfaceDark,
    onSurface = NewsTextPrimaryDark,
    surfaceVariant = NewsSurfaceVariantDark,
    onSurfaceVariant = NewsTextSecondaryDark,
    outline = NewsCardBorderDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NewsCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4E6),
    onPrimaryContainer = Color(0xFF881337),
    secondary = NewsNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = NewsNavyDark,
    tertiary = NewsEmerald,
    onTertiary = Color.White,
    background = NewsBackgroundLight,
    onBackground = NewsTextPrimaryLight,
    surface = NewsSurfaceLight,
    onSurface = NewsTextPrimaryLight,
    surfaceVariant = NewsSurfaceVariantLight,
    onSurfaceVariant = NewsTextSecondaryLight,
    outline = Color(0xFFE2E8F0)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default so editorial identity is consistently stunning
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
