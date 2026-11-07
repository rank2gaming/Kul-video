package com.cineflow.ai.feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cineflow.ai.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Approved Dark Cinematic Master Palette
val SplashMainBackground = Color(0xFF040717)
val SplashRadialCenter = Color(0xFF182044)
val SplashPrimaryViolet = Color(0xFF8B5CF6)
val SplashBrightViolet = Color(0xFFA855F7)
val SplashElectricCyan = Color(0xFF22D3EE)
val SplashBrightCyan = Color(0xFF00F0FF)
val SplashMainText = Color(0xFFF8FAFC)
val SplashSecondaryText = Color(0xFFB8C2D6)
val SplashProgressTrack = Color(0xFF1C2541)

/**
 * TASK 1 — Professional Animated Splash Screen
 *
 * Visual source of truth: `01_Splash_Screen_Reference.png`
 *
 * Requirements enforced:
 * - Follows `01_Splash_Screen_Reference.png` closely (top-left & bottom-right neon wave arcs,
 *   3D spherical filmstrip "C" + play emblem, "CineFlow AI", "CREATE BEYOND LIMITS",
 *   glowing horizontal light flare, and bottom capsule progress bar).
 * - Progress animation begins strictly at `0f` (`0%`) and advances naturally.
 * - Minimum visible Splash duration: 2 seconds (`2000` ms by default).
 * - If application initialization (`isAppInitialized`) takes longer than 2 seconds,
 *   keeps the Splash visible and caps progress below 100% (`0.92f`) until initialization completes.
 * - Never falsely shows 100% completion (`1.0f`) before initialization is actually complete.
 */
@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit,
  isAppInitialized: Boolean = true,
  autoAdvance: Boolean = true,
  durationMillis: Int = 2000,
  onProgressUpdated: ((Float) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val currentInitialized by rememberUpdatedState(isAppInitialized)
  val currentOnFinished by rememberUpdatedState(onSplashFinished)
  val currentOnProgress by rememberUpdatedState(onProgressUpdated)

  // Progress MUST begin strictly at 0f (0%)
  val progressFraction = remember { Animatable(0f) }
  val logoAlpha = remember { Animatable(if (autoAdvance) 0f else 1f) }
  val logoScale = remember { Animatable(if (autoAdvance) 0.86f else 1f) }
  val glowPulse = remember { Animatable(if (autoAdvance) 0.35f else 1f) }
  val cyanSweep = remember { Animatable(if (autoAdvance) -0.4f else 0.5f) }

  LaunchedEffect(progressFraction.value) {
    currentOnProgress?.invoke(progressFraction.value)
  }

  LaunchedEffect(autoAdvance, durationMillis) {
    if (autoAdvance) {
      val effectiveMinDuration = durationMillis.coerceAtLeast(100)
      val stageOneDuration = (effectiveMinDuration * 0.85f).toInt().coerceAtLeast(80)
      val stageTwoDuration = (effectiveMinDuration - stageOneDuration).coerceAtLeast(20)

      coroutineScope {
        launch {
          logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = (effectiveMinDuration * 0.40f).toInt().coerceAtLeast(120),
              easing = LinearOutSlowInEasing
            )
          )
        }
        launch {
          logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = (effectiveMinDuration * 0.50f).toInt().coerceAtLeast(150),
              easing = FastOutSlowInEasing
            )
          )
        }
        launch {
          glowPulse.animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = (effectiveMinDuration * 0.65f).toInt().coerceAtLeast(180),
              easing = FastOutSlowInEasing
            )
          )
        }
        launch {
          cyanSweep.animateTo(
            targetValue = 1.35f,
            animationSpec = tween(
              durationMillis = (effectiveMinDuration * 0.80f).toInt().coerceAtLeast(200),
              easing = FastOutSlowInEasing
            )
          )
        }
        launch {
          // Stage 1: Advance naturally from 0% to 90% over 85% of the minimum splash duration
          progressFraction.animateTo(
            targetValue = 0.90f,
            animationSpec = tween(
              durationMillis = stageOneDuration,
              easing = FastOutSlowInEasing
            )
          )
        }
      }

      // If initialization is still in progress after Stage 1, hold at 92% (NEVER falsely show 100%)
      if (!currentInitialized) {
        progressFraction.animateTo(
          targetValue = 0.92f,
          animationSpec = tween(durationMillis = 120, easing = LinearOutSlowInEasing)
        )
        snapshotFlow { currentInitialized }.first { it }
      }

      // Stage 2: Once BOTH minimum duration and initialization are satisfied, reach 100% (1.0f)
      progressFraction.animateTo(
        targetValue = 1f,
        animationSpec = tween(
          durationMillis = stageTwoDuration,
          easing = LinearOutSlowInEasing
        )
      )

      currentOnFinished()
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(SplashMainBackground)
      .testTag("splash_screen"),
    containerColor = SplashMainBackground,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(SplashMainBackground)
        .padding(innerPadding)
        .testTag("splash_radial_backdrop"),
      contentAlignment = Alignment.Center
    ) {
      // 1. Top-Left Neon Wave Arc from 01_Splash_Screen_Reference.png
      Image(
        painter = painterResource(id = R.drawable.img_splash_top_wave),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.TopCenter)
          .testTag("splash_top_wave")
      )

      // 2. Bottom-Right Neon Wave Arc from 01_Splash_Screen_Reference.png
      Image(
        painter = painterResource(id = R.drawable.img_splash_bottom_wave),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .testTag("splash_bottom_wave")
      )

      // 3. Radial Glow Backdrop behind Center Emblem
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .testTag("splash_glow_canvas")
      ) {
        val center = Offset(size.width * 0.5f, size.height * 0.42f)
        val violetGlowRadius = size.minDimension * 0.46f * (0.85f + 0.15f * glowPulse.value)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              SplashPrimaryViolet.copy(alpha = 0.24f * glowPulse.value),
              SplashRadialCenter.copy(alpha = 0.32f * glowPulse.value),
              Color.Transparent
            ),
            center = center,
            radius = violetGlowRadius.coerceAtLeast(1f)
          ),
          radius = violetGlowRadius,
          center = center
        )
      }

      // 4. Center Logo Emblem + Brand Name + Tagline + Light Flare + Bottom Capsule Progress Bar
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Spacer(modifier = Modifier.height(36.dp))

        // Center Brand Block
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          // 3D Spherical Filmstrip "C" + Play Emblem from 01_Splash_Screen_Reference.png
          Box(
            modifier = Modifier
              .size(216.dp)
              .scale(logoScale.value)
              .alpha(logoAlpha.value)
              .clip(CircleShape)
              .testTag("splash_logo_container"),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_splash_logo_emblem),
              contentDescription = "CineFlow AI Logo",
              contentScale = ContentScale.Fit,
              modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .clip(CircleShape)
                .testTag("splash_logo_image")
            )

            // Soft cyan light-sweep across the emblem
            Canvas(
              modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .testTag("splash_cyan_light_sweep")
            ) {
              val sweepCenterX = size.width * cyanSweep.value
              val sweepWidth = size.width * 0.40f
              drawRect(
                brush = Brush.linearGradient(
                  colors = listOf(
                    Color.Transparent,
                    SplashElectricCyan.copy(alpha = 0.20f),
                    SplashPrimaryViolet.copy(alpha = 0.12f),
                    Color.Transparent
                  ),
                  start = Offset(sweepCenterX - sweepWidth, 0f),
                  end = Offset(sweepCenterX + sweepWidth, size.height)
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Brand Name: CineFlow AI (#F8FAFC)
          Text(
            text = "CineFlow AI",
            color = SplashMainText,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.4.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .alpha(logoAlpha.value)
              .testTag("splash_brand_title")
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Tagline from 01_Splash_Screen_Reference.png: CREATE BEYOND LIMITS
          Text(
            text = "CREATE BEYOND LIMITS",
            color = SplashSecondaryText,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 3.6.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .alpha(logoAlpha.value)
              .testTag("splash_brand_subtitle")
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Glowing Horizontal Light Flare below tagline (matches 01_Splash_Screen_Reference.png)
          Canvas(
            modifier = Modifier
              .width(240.dp)
              .height(10.dp)
              .alpha(logoAlpha.value)
              .testTag("splash_tagline_flare")
          ) {
            val midY = size.height * 0.5f
            drawLine(
              brush = Brush.horizontalGradient(
                colors = listOf(
                  Color.Transparent,
                  SplashPrimaryViolet.copy(alpha = 0.65f),
                  Color.White,
                  SplashElectricCyan.copy(alpha = 0.75f),
                  Color.Transparent
                )
              ),
              start = Offset(0f, midY),
              end = Offset(size.width, midY),
              strokeWidth = 2.dp.toPx()
            )
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(
                  Color.White.copy(alpha = 0.95f),
                  SplashPrimaryViolet.copy(alpha = 0.45f),
                  Color.Transparent
                ),
                center = Offset(size.width * 0.5f, midY),
                radius = 18.dp.toPx()
              ),
              radius = 18.dp.toPx(),
              center = Offset(size.width * 0.5f, midY)
            )
          }
        }

        // Bottom Capsule Progress Bar (Starts strictly at 0f and advances naturally to 1f)
        Column(
          modifier = Modifier.padding(bottom = 64.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          val clampedProgress = progressFraction.value.coerceIn(0f, 1f)
          Box(
            modifier = Modifier
              .width(280.dp)
              .height(11.dp)
              .clip(RoundedCornerShape(50))
              .background(SplashProgressTrack)
              .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                  current = clampedProgress,
                  range = 0f..1f
                )
              }
              .testTag("splash_progress_track"),
            contentAlignment = Alignment.CenterStart
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(clampedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(
                      SplashBrightViolet,
                      SplashPrimaryViolet,
                      SplashElectricCyan,
                      SplashBrightCyan
                    )
                  )
                )
                .testTag("splash_progress_fill")
            )
          }
        }
      }
    }
  }
}
