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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cineflow.ai.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// Exact Master Color Palette (Section 3 & Section 4)
val SplashMainBackground = Color(0xFF080B16)
val SplashRadialCenter = Color(0xFF182044)
val SplashPrimaryViolet = Color(0xFF8B5CF6)
val SplashElectricCyan = Color(0xFF22D3EE)
val SplashMainText = Color(0xFFF8FAFC)
val SplashSecondaryText = Color(0xFFB8C2D6)
val SplashProgressTrack = Color(0xFF263149)

/**
 * Professional Animated Splash Screen (Master Task Instructions Section 3 & Section 4).
 *
 * Displays the existing CineFlow AI logo (`R.drawable.img_editor_cineflow_logo_mark`)
 * over `#080B16` with a `#182044` -> `#080B16` radial gradient, subtle `#8B5CF6` violet glow,
 * `#22D3EE` cyan light-sweep, and a `#263149` capsule progress bar filled with `#8B5CF6` -> `#22D3EE`.
 */
@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit,
  autoAdvance: Boolean = true,
  durationMillis: Int = 950,
  modifier: Modifier = Modifier
) {
  val logoAlpha = remember { Animatable(if (autoAdvance) 0f else 1f) }
  val logoScale = remember { Animatable(if (autoAdvance) 0.85f else 1f) }
  val glowPulse = remember { Animatable(if (autoAdvance) 0.35f else 1f) }
  val cyanSweep = remember { Animatable(if (autoAdvance) -0.4f else 0.5f) }
  val progressFraction = remember { Animatable(if (autoAdvance) 0.08f else 0.75f) }

  LaunchedEffect(autoAdvance, durationMillis) {
    if (autoAdvance) {
      coroutineScope {
        launch {
          logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = (durationMillis * 0.45f).toInt().coerceAtLeast(180),
              easing = LinearOutSlowInEasing
            )
          )
        }
        launch {
          logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = (durationMillis * 0.55f).toInt().coerceAtLeast(220),
              easing = FastOutSlowInEasing
            )
          )
        }
        launch {
          glowPulse.animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = (durationMillis * 0.65f).toInt().coerceAtLeast(260),
              easing = FastOutSlowInEasing
            )
          )
        }
        launch {
          cyanSweep.animateTo(
            targetValue = 1.35f,
            animationSpec = tween(
              durationMillis = (durationMillis * 0.85f).toInt().coerceAtLeast(320),
              easing = FastOutSlowInEasing
            )
          )
        }
        launch {
          progressFraction.animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = durationMillis.coerceAtLeast(350),
              easing = FastOutSlowInEasing
            )
          )
        }
      }
      onSplashFinished()
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
      // Subtle radial gradient behind the logo (#182044 center -> #080B16 outer edge)
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .testTag("splash_glow_canvas")
      ) {
        val center = Offset(size.width * 0.5f, size.height * 0.44f)
        val mainRadius = size.minDimension * 0.72f
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              SplashRadialCenter,
              SplashMainBackground
            ),
            center = center,
            radius = mainRadius.coerceAtLeast(1f)
          ),
          radius = mainRadius,
          center = center
        )

        // Subtle violet glow (#8B5CF6)
        val violetGlowRadius = size.minDimension * 0.36f * (0.85f + 0.15f * glowPulse.value)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              SplashPrimaryViolet.copy(alpha = 0.28f * glowPulse.value),
              SplashPrimaryViolet.copy(alpha = 0.08f * glowPulse.value),
              Color.Transparent
            ),
            center = center,
            radius = violetGlowRadius.coerceAtLeast(1f)
          ),
          radius = violetGlowRadius,
          center = center
        )

        // Subtle cyan light accent ring (#22D3EE)
        val cyanAccentRadius = size.minDimension * 0.22f
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              SplashElectricCyan.copy(alpha = 0.18f * glowPulse.value),
              Color.Transparent
            ),
            center = Offset(center.x + 18.dp.toPx(), center.y - 12.dp.toPx()),
            radius = cyanAccentRadius.coerceAtLeast(1f)
          ),
          radius = cyanAccentRadius,
          center = Offset(center.x + 18.dp.toPx(), center.y - 12.dp.toPx())
        )
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Logo Container with Violet Glow, Existing CineFlow AI Logo, and Soft Cyan Light-Sweep
        Box(
          modifier = Modifier
            .size(136.dp)
            .scale(logoScale.value)
            .alpha(logoAlpha.value)
            .testTag("splash_logo_container"),
          contentAlignment = Alignment.Center
        ) {
          // Outer controlled violet/cyan halo ring
          Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension * 0.47f
            drawCircle(
              brush = Brush.sweepGradient(
                colors = listOf(
                  SplashPrimaryViolet.copy(alpha = 0.55f),
                  SplashElectricCyan.copy(alpha = 0.50f),
                  SplashPrimaryViolet.copy(alpha = 0.25f),
                  SplashPrimaryViolet.copy(alpha = 0.55f)
                )
              ),
              radius = radius,
              style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )
          }

          // Existing CineFlow AI logo asset preserving original proportions (1:1 ContentScale.Fit)
          Image(
            painter = painterResource(id = R.drawable.img_editor_cineflow_logo_mark),
            contentDescription = "CineFlow AI Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
              .size(92.dp)
              .aspectRatio(1f)
              .testTag("splash_logo_image")
          )

          // Soft cyan light-sweep effect (#22D3EE) across the logo area
          Canvas(
            modifier = Modifier
              .size(116.dp)
              .clip(CircleShape)
              .testTag("splash_cyan_light_sweep")
          ) {
            val sweepCenterX = size.width * cyanSweep.value
            val sweepWidth = size.width * 0.42f
            drawRect(
              brush = Brush.linearGradient(
                colors = listOf(
                  Color.Transparent,
                  SplashElectricCyan.copy(alpha = 0.24f),
                  SplashPrimaryViolet.copy(alpha = 0.14f),
                  Color.Transparent
                ),
                start = Offset(sweepCenterX - sweepWidth, 0f),
                end = Offset(sweepCenterX + sweepWidth, size.height)
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Brand Name: CineFlow AI (#F8FAFC)
        Text(
          text = "CineFlow AI",
          color = SplashMainText,
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.6.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .alpha(logoAlpha.value)
            .testTag("splash_brand_title")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Professional AI Video Studio",
          color = SplashSecondaryText,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Medium,
          letterSpacing = 0.3.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .alpha(logoAlpha.value)
            .testTag("splash_brand_subtitle")
        )

        Spacer(modifier = Modifier.height(44.dp))

        // Capsule Progress Indicator (Track #263149, Fill gradient #8B5CF6 -> #22D3EE)
        Box(
          modifier = Modifier
            .width(180.dp)
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(SplashProgressTrack)
            .testTag("splash_progress_track")
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(progressFraction.value.coerceIn(0.05f, 1f))
              .fillMaxHeight()
              .clip(RoundedCornerShape(3.dp))
              .background(
                Brush.horizontalGradient(
                  colors = listOf(
                    SplashPrimaryViolet,
                    SplashElectricCyan
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
