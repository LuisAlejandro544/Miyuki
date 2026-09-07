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
    primary = MiyukiGold,
    onPrimary = MiyukiBlack,
    primaryContainer = MiyukiGoldDark,
    onPrimaryContainer = MiyukiGoldLight,
    secondary = MiyukiTurquoise,
    onSecondary = Color.White,
    secondaryContainer = MiyukiTurquoiseDark,
    onSecondaryContainer = MiyukiTurquoiseLight,
    tertiary = MiyukiCoral,
    onTertiary = Color.White,
    background = MiyukiIndigoDark,
    onBackground = MiyukiPearl,
    surface = MiyukiIndigoSurface,
    onSurface = MiyukiPearl,
    surfaceVariant = MiyukiIndigoSurfaceVariant,
    onSurfaceVariant = Color(0xFFCCC7DC),
    outline = Color(0xFF4A4660)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MiyukiGoldDark,
    onPrimary = Color.White,
    primaryContainer = MiyukiGoldLight,
    onPrimaryContainer = Color(0xFF3F2E05),
    secondary = MiyukiTurquoise,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3F6F6),
    onSecondaryContainer = MiyukiTurquoiseDark,
    tertiary = MiyukiCoral,
    onTertiary = Color.White,
    background = MiyukiWarmBackground,
    onBackground = MiyukiBlack,
    surface = MiyukiWarmSurface,
    onSurface = MiyukiBlack,
    surfaceVariant = MiyukiWarmSurfaceVariant,
    onSurfaceVariant = Color(0xFF4C463D),
    outline = MiyukiWarmOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // For brand consistency with Miyuki beads, default to our bespoke jewelry scheme
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

