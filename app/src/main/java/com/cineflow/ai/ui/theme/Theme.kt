package com.cineflow.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CineFlowLightColorScheme = lightColorScheme(
  primary = CineFlowPurple,
  onPrimary = Color.White,
  primaryContainer = ToolVideoEditBg,
  onPrimaryContainer = CineFlowDeepNavy,
  secondary = CineFlowCyan,
  onSecondary = Color.White,
  tertiary = CineFlowMagenta,
  background = CineFlowSurfaceBg,
  onBackground = CineFlowDeepNavy,
  surface = Color.White,
  onSurface = CineFlowDeepNavy,
  surfaceVariant = CineFlowSearchBg,
  onSurfaceVariant = CineFlowSlate
)

@Composable
fun CineFlowTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = CineFlowLightColorScheme,
    typography = Typography,
    content = content
  )
}
