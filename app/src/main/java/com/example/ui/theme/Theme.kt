package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FFChatDarkColorScheme = darkColorScheme(
  primary = FFChatCyan,
  onPrimary = DarkBg,
  primaryContainer = FFChatViolet,
  onPrimaryContainer = DarkTextPrimary,
  secondary = FFChatVioletLight,
  onSecondary = DarkBg,
  tertiary = FFChatGreen,
  background = DarkBg,
  surface = DarkSurface,
  surfaceVariant = DarkSurfaceVariant,
  onBackground = DarkTextPrimary,
  onSurface = DarkTextPrimary,
  onSurfaceVariant = DarkTextSecondary
)

private val FFChatLightColorScheme = lightColorScheme(
  primary = FFChatCyanVariant,
  onPrimary = LightSurface,
  primaryContainer = FFChatVioletLight,
  onPrimaryContainer = LightTextPrimary,
  secondary = FFChatViolet,
  onSecondary = LightSurface,
  tertiary = FFChatGreen,
  background = LightBg,
  surface = LightSurface,
  surfaceVariant = LightSurfaceVariant,
  onBackground = LightTextPrimary,
  onSurface = LightTextPrimary,
  onSurfaceVariant = LightTextSecondary
)

@Composable
fun FFChatTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use intentional FF Chat branding colors
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> FFChatDarkColorScheme
    else -> FFChatLightColorScheme
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
