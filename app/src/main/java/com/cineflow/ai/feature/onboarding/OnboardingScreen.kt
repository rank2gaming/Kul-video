package com.cineflow.ai.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

// Exact Master Color Palette (Section 3 & Section 8)
val OnboardingMainBg = Color(0xFF080B16)
val OnboardingSecondaryBg = Color(0xFF0D1222)
val OnboardingCardSurface = Color(0xFF151C30)
val OnboardingSecondarySurface = Color(0xFF1B2540)
val OnboardingPrimaryViolet = Color(0xFF8B5CF6)
val OnboardingDeepViolet = Color(0xFF6D28D9)
val OnboardingBrightViolet = Color(0xFFA78BFA)
val OnboardingElectricCyan = Color(0xFF22D3EE)
val OnboardingDeepCyan = Color(0xFF0891B2)
val OnboardingMainText = Color(0xFFF8FAFC)
val OnboardingSecondaryText = Color(0xFFB8C2D6)
val OnboardingMutedText = Color(0xFF8491A8)
val OnboardingBorderColor = Color(0xFF28334B)
val OnboardingIndicatorInactive = Color(0xFF35415B)
val OnboardingSuccessAccent = Color(0xFF34D399)
val IllustrationGradientStart = Color(0xFF30205C)
val IllustrationGradientEnd = Color(0xFF123B58)

/**
 * Eight-Page Horizontally Swipeable Onboarding Screen
 * (Master Task Instructions Sections 3, 5, 6, 7, 8, 9, 10, 11).
 */
@Composable
fun OnboardingScreen(
  viewModel: OnboardingViewModel,
  onGetStarted: () -> Unit,
  modifier: Modifier = Modifier
) {
  val pages = viewModel.pages
  val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
  val coroutineScope = rememberCoroutineScope()

  val pagerState = rememberPagerState(
    initialPage = currentPage,
    pageCount = { pages.size }
  )

  // Synchronize finger/swipe changes from HorizontalPager -> ViewModel
  LaunchedEffect(pagerState.currentPage, pagerState.settledPage) {
    viewModel.onPageChanged(pagerState.currentPage)
  }

  // Synchronize button/ViewModel changes -> HorizontalPager
  LaunchedEffect(currentPage) {
    if (pagerState.currentPage != currentPage) {
      pagerState.scrollToPage(currentPage)
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(OnboardingMainBg)
      .testTag("onboarding_screen"),
    containerColor = OnboardingMainBg,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              OnboardingMainBg,
              OnboardingSecondaryBg,
              OnboardingMainBg
            )
          )
        )
        .padding(innerPadding)
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Compact Brand Header (Existing CineFlow AI Logo + Step Counter)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 520.dp)
          .padding(top = 4.dp)
          .testTag("onboarding_top_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_editor_cineflow_logo_mark),
            contentDescription = "CineFlow AI Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
              .size(28.dp)
              .aspectRatio(1f)
              .testTag("onboarding_header_logo")
          )
          Text(
            text = "CineFlow AI",
            color = OnboardingMainText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(OnboardingCardSurface)
            .border(1.dp, OnboardingBorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("onboarding_page_counter_badge")
        ) {
          Text(
            text = "${currentPage + 1} / ${pages.size}",
            color = OnboardingSecondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      // Horizontally Swipeable 8-Page Carousel (One normal swipe moves one page at a time)
      HorizontalPager(
        state = pagerState,
        flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .widthIn(max = 520.dp)
          .testTag("onboarding_horizontal_pager")
      ) { pageIndex ->
        val page = pages[pageIndex]
        OnboardingFeaturePageContent(
          page = page,
          modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_page_$pageIndex")
        )
      }

      // Bottom Section: 8 Page Indicators + Previous / Next / Get Started Controls
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 520.dp)
          .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Eight Page Indicators (Inactive #35415B, Active #8B5CF6 -> #22D3EE)
        Row(
          modifier = Modifier
            .padding(vertical = 12.dp)
            .testTag("onboarding_page_indicators"),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          pages.forEach { page ->
            val isSelected = page.index == currentPage
            val indicatorWidth by animateDpAsState(
              targetValue = if (isSelected) 26.dp else 8.dp,
              animationSpec = tween(durationMillis = 220),
              label = "indicator_width_${page.index}"
            )
            val indicatorColor by animateColorAsState(
              targetValue = if (isSelected) OnboardingPrimaryViolet else OnboardingIndicatorInactive,
              animationSpec = tween(durationMillis = 220),
              label = "indicator_color_${page.index}"
            )
            Box(
              modifier = Modifier
                .width(indicatorWidth)
                .height(8.dp)
                .clip(CircleShape)
                .background(
                  if (isSelected) {
                    Brush.horizontalGradient(
                      colors = listOf(
                        OnboardingPrimaryViolet,
                        OnboardingElectricCyan
                      )
                    )
                  } else {
                    Brush.horizontalGradient(
                      colors = listOf(indicatorColor, indicatorColor)
                    )
                  }
                )
                .semantics { selected = isSelected }
                .testTag("onboarding_indicator_${page.index}")
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Buttons Row (Previous + Next OR Previous + Get Started)
        val isFirstPage = currentPage == 0
        val isLastPage = currentPage == pages.lastIndex

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_navigation_row"),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Previous Button (Disabled on first page, Background #151C30, Border #35415B, Text/Icon #B8C2D6)
          Box(
            modifier = Modifier
              .weight(1f)
              .height(52.dp)
              .alpha(if (isFirstPage) 0.38f else 1f)
              .clip(RoundedCornerShape(16.dp))
              .background(OnboardingCardSurface)
              .border(
                width = 1.2.dp,
                color = OnboardingIndicatorInactive,
                shape = RoundedCornerShape(16.dp)
              )
              .clickable(
                enabled = !isFirstPage,
                role = Role.Button,
                onClick = {
                  val target = viewModel.previousPage()
                  coroutineScope.launch {
                    pagerState.animateScrollToPage(target)
                  }
                }
              )
              .testTag("onboarding_previous_button"),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = OnboardingSecondaryText,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = "Previous",
                color = OnboardingSecondaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          if (!isLastPage) {
            // Next Button (Pages 0..6: Background #8B5CF6, Text/Icon #FFFFFF, Rounded corners)
            Box(
              modifier = Modifier
                .weight(1.35f)
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(OnboardingPrimaryViolet)
                .border(
                  width = 1.dp,
                  color = OnboardingBrightViolet.copy(alpha = 0.45f),
                  shape = RoundedCornerShape(16.dp)
                )
                .clickable(
                  role = Role.Button,
                  onClick = {
                    val target = viewModel.nextPage()
                    coroutineScope.launch {
                      pagerState.animateScrollToPage(target)
                    }
                  }
                )
                .testTag("onboarding_next_button"),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "Next",
                  color = Color.White,
                  fontSize = 15.5.sp,
                  fontWeight = FontWeight.Bold
                )
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          } else {
            // Get Started Button (Final Page 7: Gradient #8B5CF6 -> #6D28D9, Cyan highlight #22D3EE, Text #FFFFFF, Capsule shape)
            Box(
              modifier = Modifier
                .weight(1.55f)
                .height(52.dp)
                .shadow(
                  elevation = 10.dp,
                  shape = RoundedCornerShape(26.dp),
                  ambientColor = OnboardingPrimaryViolet,
                  spotColor = OnboardingElectricCyan
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(
                      OnboardingPrimaryViolet,
                      OnboardingDeepViolet
                    )
                  )
                )
                .border(
                  width = 1.3.dp,
                  brush = Brush.horizontalGradient(
                    colors = listOf(
                      OnboardingElectricCyan,
                      OnboardingPrimaryViolet
                    )
                  ),
                  shape = RoundedCornerShape(26.dp)
                )
                .clickable(
                  role = Role.Button,
                  onClick = {
                    viewModel.completeOnboarding()
                    onGetStarted()
                  }
                )
                .testTag("onboarding_get_started_button"),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "Get Started",
                  color = Color.White,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  tint = OnboardingElectricCyan,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Single Onboarding Page Content in strict order (Section 5):
 * 1. Large circular feature illustration.
 * 2. Feature title.
 * 3. Feature description.
 * (4. Page indicator and navigation controls rendered below in the parent column.)
 */
@Composable
private fun OnboardingFeaturePageContent(
  page: OnboardingFeaturePage,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 8.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // 1. Large Circular Feature Illustration (Strictly 1:1 CircleShape, #30205C -> #123B58, border #8B5CF6, cyan #22D3EE)
    CircularFeatureIllustration(
      featureType = page.featureType,
      testTag = page.illustrationTestTag
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Feature Title (#F8FAFC, bold typography)
    Text(
      text = page.title,
      color = OnboardingMainText,
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      letterSpacing = 0.2.sp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("onboarding_feature_title_${page.index}")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 3. Feature Description (#B8C2D6, high contrast and readable)
    Text(
      text = page.description,
      color = OnboardingSecondaryText,
      fontSize = 14.5.sp,
      fontWeight = FontWeight.Normal,
      lineHeight = 21.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .testTag("onboarding_feature_description_${page.index}")
    )
  }
}

/**
 * Mandatory Circular Feature Illustration (Section 6).
 *
 * - Strictly circular container with 1:1 aspect ratio (`CircleShape`).
 * - Main gradient: `#30205C` to `#123B58`.
 * - Outer circular border: `#8B5CF6`.
 * - Secondary cyan highlight: `#22D3EE`.
 * - Subtle violet glow/shadow.
 * - All artwork clipped strictly within the circular boundary.
 */
@Composable
fun CircularFeatureIllustration(
  featureType: OnboardingFeatureType,
  testTag: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(192.dp)
      .aspectRatio(1f)
      .shadow(
        elevation = 18.dp,
        shape = CircleShape,
        ambientColor = OnboardingPrimaryViolet,
        spotColor = OnboardingPrimaryViolet
      )
      .clip(CircleShape)
      .background(
        Brush.linearGradient(
          colors = listOf(
            IllustrationGradientStart,
            IllustrationGradientEnd
          ),
          start = Offset.Zero,
          end = Offset(600f, 600f)
        )
      )
      .border(
        width = 2.2.dp,
        color = OnboardingPrimaryViolet,
        shape = CircleShape
      )
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
    ) {
      val w = size.width
      val h = size.height
      val cx = w * 0.5f
      val cy = h * 0.5f
      val r = size.minDimension * 0.5f

      // Subtle inner violet/cyan ambient glow & secondary cyan highlight ring (#22D3EE)
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            OnboardingPrimaryViolet.copy(alpha = 0.22f),
            OnboardingElectricCyan.copy(alpha = 0.10f),
            Color.Transparent
          ),
          center = Offset(cx, cy),
          radius = r * 0.88f
        ),
        radius = r * 0.88f,
        center = Offset(cx, cy)
      )

      // Inner secondary cyan highlight arc (#22D3EE)
      drawCircle(
        color = OnboardingElectricCyan.copy(alpha = 0.45f),
        radius = r * 0.91f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.4.dp.toPx())
      )

      // Subtle dashed orbital ring
      drawCircle(
        color = OnboardingBrightViolet.copy(alpha = 0.25f),
        radius = r * 0.78f,
        center = Offset(cx, cy),
        style = Stroke(
          width = 1.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
      )

      when (featureType) {
        OnboardingFeatureType.VIDEO_EDIT -> drawVideoEditIllustration(w, h, cx, cy)
        OnboardingFeatureType.TEXT_TO_SPEECH -> drawTextToSpeechIllustration(w, h, cx, cy)
        OnboardingFeatureType.VIDEO_TO_LANGUAGE -> drawVideoToLanguageIllustration(w, h, cx, cy)
        OnboardingFeatureType.IMAGE_TO_VIDEO -> drawImageToVideoIllustration(w, h, cx, cy)
        OnboardingFeatureType.AI_EFFECTS -> drawAiEffectsIllustration(w, h, cx, cy)
        OnboardingFeatureType.AI_AVATAR -> drawAiAvatarIllustration(w, h, cx, cy)
        OnboardingFeatureType.AUTO_SUBTITLES -> drawAutoSubtitlesIllustration(w, h, cx, cy)
        OnboardingFeatureType.REMOVE_BG -> drawRemoveBgIllustration(w, h, cx, cy)
      }
    }
  }
}

/**
 * 1. Video Edit Illustration:
 * Video preview monitor frame, play button, multi-track timeline, trim handles, and cyan playhead.
 */
private fun DrawScope.drawVideoEditIllustration(w: Float, h: Float, cx: Float, cy: Float) {
  // Video Preview Frame
  val screenLeft = w * 0.23f
  val screenTop = h * 0.22f
  val screenW = w * 0.54f
  val screenH = h * 0.31f
  drawRoundRect(
    color = OnboardingCardSurface,
    topLeft = Offset(screenLeft, screenTop),
    size = Size(screenW, screenH),
    cornerRadius = CornerRadius(10.dp.toPx())
  )
  drawRoundRect(
    color = OnboardingPrimaryViolet,
    topLeft = Offset(screenLeft, screenTop),
    size = Size(screenW, screenH),
    cornerRadius = CornerRadius(10.dp.toPx()),
    style = Stroke(width = 2.dp.toPx())
  )

  // Play Triangle inside Preview Frame
  val playPath = Path().apply {
    moveTo(cx - w * 0.04f, screenTop + screenH * 0.30f)
    lineTo(cx - w * 0.04f, screenTop + screenH * 0.70f)
    lineTo(cx + w * 0.06f, screenTop + screenH * 0.50f)
    close()
  }
  drawPath(playPath, color = OnboardingElectricCyan)

  // Multitrack Timeline Tracks below preview
  val track1Y = h * 0.60f
  val track2Y = h * 0.69f
  val trackH = h * 0.06f

  // Track 1 (Video Track with Violet Clip & White Trim Handles)
  drawRoundRect(
    color = OnboardingSecondarySurface,
    topLeft = Offset(w * 0.18f, track1Y),
    size = Size(w * 0.64f, trackH),
    cornerRadius = CornerRadius(5.dp.toPx())
  )
  drawRoundRect(
    brush = Brush.horizontalGradient(
      colors = listOf(OnboardingPrimaryViolet, OnboardingDeepViolet)
    ),
    topLeft = Offset(w * 0.26f, track1Y),
    size = Size(w * 0.44f, trackH),
    cornerRadius = CornerRadius(5.dp.toPx())
  )
  // Left & Right White Trim Handles
  drawRoundRect(
    color = OnboardingMainText,
    topLeft = Offset(w * 0.26f, track1Y),
    size = Size(w * 0.025f, trackH),
    cornerRadius = CornerRadius(3.dp.toPx())
  )
  drawRoundRect(
    color = OnboardingMainText,
    topLeft = Offset(w * 0.675f, track1Y),
    size = Size(w * 0.025f, trackH),
    cornerRadius = CornerRadius(3.dp.toPx())
  )

  // Track 2 (Audio/FX Track with Cyan Clip)
  drawRoundRect(
    color = OnboardingSecondarySurface,
    topLeft = Offset(w * 0.21f, track2Y),
    size = Size(w * 0.58f, trackH),
    cornerRadius = CornerRadius(5.dp.toPx())
  )
  drawRoundRect(
    brush = Brush.horizontalGradient(
      colors = listOf(OnboardingDeepCyan, OnboardingElectricCyan)
    ),
    topLeft = Offset(w * 0.30f, track2Y),
    size = Size(w * 0.38f, trackH),
    cornerRadius = CornerRadius(5.dp.toPx())
  )

  // Vertical Electric Cyan Playhead line + top diamond knob
  drawLine(
    color = OnboardingElectricCyan,
    start = Offset(cx, h * 0.56f),
    end = Offset(cx, h * 0.79f),
    strokeWidth = 2.5.dp.toPx(),
    cap = StrokeCap.Round
  )
  drawCircle(
    color = OnboardingElectricCyan,
    radius = 4.5.dp.toPx(),
    center = Offset(cx, h * 0.56f)
  )
}

/**
 * 2. Text-to-Speech Illustration:
 * Script document lines transforming into AI voice waves and a glowing microphone emblem.
 */
private fun DrawScope.drawTextToSpeechIllustration(w: Float, h: Float, cx: Float, cy: Float) {
  // Left Text Script Card
  val docLeft = w * 0.18f
  val docTop = h * 0.30f
  val docW = w * 0.28f
  val docH = h * 0.38f
  drawRoundRect(
    color = OnboardingCardSurface,
    topLeft = Offset(docLeft, docTop),
    size = Size(docW, docH),
    cornerRadius = CornerRadius(8.dp.toPx())
  )
  drawRoundRect(
    color = OnboardingPrimaryViolet,
    topLeft = Offset(docLeft, docTop),
    size = Size(docW, docH),
    cornerRadius = CornerRadius(8.dp.toPx()),
    style = Stroke(width = 1.8.dp.toPx())
  )
  // Text lines inside script card
  for (i in 0..3) {
    val lineY = docTop + docH * (0.24f + i * 0.18f)
    val lineW = if (i == 3) docW * 0.45f else docW * 0.68f
    drawLine(
      color = if (i == 0) OnboardingElectricCyan else OnboardingSecondaryText,
      start = Offset(docLeft + docW * 0.16f, lineY),
      end = Offset(docLeft + docW * 0.16f + lineW, lineY),
      strokeWidth = 2.4.dp.toPx(),
      cap = StrokeCap.Round
    )
  }

  // Right Soundwave Bars (Violet -> Electric Cyan)
  val barHeights = listOf(0.10f, 0.22f, 0.34f, 0.18f, 0.30f, 0.14f)
  val startX = w * 0.53f
  val spacing = w * 0.052f
  barHeights.forEachIndexed { idx, bh ->
    val bx = startX + idx * spacing
    val barH = h * bh
    drawLine(
      brush = Brush.verticalGradient(
        colors = listOf(OnboardingElectricCyan, OnboardingPrimaryViolet)
      ),
      start = Offset(bx, cy - barH * 0.5f),
      end = Offset(bx, cy + barH * 0.5f),
      strokeWidth = 4.2.dp.toPx(),
      cap = StrokeCap.Round
    )
  }

  // Center-Bottom AI Voice Badge Circle
  drawCircle(
    color = OnboardingSecondarySurface,
    radius = w * 0.085f,
    center = Offset(cx, h * 0.74f)
  )
  drawCircle(
    color = OnboardingElectricCyan,
    radius = w * 0.085f,
    center = Offset(cx, h * 0.74f),
    style = Stroke(width = 1.6.dp.toPx())
  )
  drawCircle(
    color = OnboardingMainText,
    radius = w * 0.028f,
    center = Offset(cx, h * 0.74f)
  )
}

/**
 * 3. Video-to-Language Illustration:
 * Video screen with global translation sphere and multi-language speech bubbles.
 */
private fun DrawScope.drawVideoToLanguageIllustration(w: Float, h: Float, cx: Float, cy: Float) {
  // Central Globe Circle
  val globeR = w * 0.21f
  drawCircle(
    color = OnboardingCardSurface,
    radius = globeR,
    center = Offset(cx, cy - h * 0.03f)
  )
  drawCircle(
    color = OnboardingElectricCyan,
    radius = globeR,
    center = Offset(cx, cy - h * 0.03f),
    style = Stroke(width = 2.dp.toPx())
  )
  // Globe latitude & meridian lines
  val gCy = cy - h * 0.03f
  drawLine(
    color = OnboardingPrimaryViolet.copy(alpha = 0.8f),
    start = Offset(cx - globeR, gCy),
    end = Offset(cx + globeR, gCy),
    strokeWidth = 1.5.dp.toPx()
  )
  drawLine(
    color = OnboardingPrimaryViolet.copy(alpha = 0.8f),
    start = Offset(cx, gCy - globeR),
    end = Offset(cx, gCy + globeR),
    strokeWidth = 1.5.dp.toPx()
  )
  drawOval(
    color = OnboardingBrightViolet.copy(alpha = 0.75f),
    topLeft = Offset(cx - globeR * 0.52f, gCy - globeR),
    size = Size(globeR * 1.04f, globeR * 2f),
    style = Stroke(width = 1.5.dp.toPx())
  )

  // Left Speech Pill (Violet)
  drawRoundRect(
    color = OnboardingPrimaryViolet,
    topLeft = Offset(w * 0.16f, h * 0.24f),
    size = Size(w * 0.22f, h * 0.11f),
    cornerRadius = CornerRadius(8.dp.toPx())
  )
  drawLine(
    color = OnboardingMainText,
    start = Offset(w * 0.21f, h * 0.295f),
    end = Offset(w * 0.33f, h * 0.295f),
    strokeWidth = 2.4.dp.toPx(),
    cap = StrokeCap.Round
  )

  // Right Speech Pill (Electric Cyan)
  drawRoundRect(
    color = OnboardingDeepCyan,
    topLeft = Offset(w * 0.62f, h * 0.58f),
    size = Size(w * 0.22f, h * 0.11f),
    cornerRadius = CornerRadius(8.dp.toPx())
  )
  drawRoundRect(
    color = OnboardingElectricCyan,
    topLeft = Offset(w * 0.62f, h * 0.58f),
    size = Size(w * 0.22f, h * 0.11f),
    cornerRadius = CornerRadius(8.dp.toPx()),
    style = Stroke(width = 1.5.dp.toPx())
  )
  drawLine(
    color = OnboardingMainText,
    start = Offset(w * 0.67f, h * 0.635f),
    end = Offset(w * 0.79f, h * 0.635f),
    strokeWidth = 2.4.dp.toPx(),
    cap = StrokeCap.Round
  )

  // Bottom Audio Dubbing Wave
  for (i in -3..3) {
    val x = cx + i * (w * 0.045f)
    val amp = if (i % 2 == 0) h * 0.045f else h * 0.025f
    drawLine(
      color = OnboardingElectricCyan,
      start = Offset(x, h * 0.76f - amp),
      end = Offset(x, h * 0.76f + amp),
      strokeWidth = 2.6.dp.toPx(),
      cap = StrokeCap.Round
    )
  }
}

/**
 * 4. Image to Video Illustration:
 * Static image card transforming into a dynamic video scene with play badge and motion trails.
 */
private fun DrawScope.drawImageToVideoIllustration(w: Float, h: Float, cx: Float, cy: Float) {
  // Back/Left Static Photo Card
  val photoLeft = w * 0.19f
  val photoTop = h * 0.26f
  val cardW = w * 0.34f
  val cardH = h * 0.30f
  drawRoundRect(
    color = OnboardingCardSurface,
    topLeft = Offset(photoLeft, photoTop),
    size = Size(cardW, cardH),
    cornerRadius = CornerRadius(8.dp.toPx())
  )
  drawRoundRect(
    color = OnboardingBrightViolet,
    topLeft = Offset(photoLeft, photoTop),
    size = Size(cardW, cardH),
    cornerRadius = CornerRadius(8.dp.toPx()),
    style = Stroke(width = 1.8.dp.toPx())
  )
  // Sun + Mountain inside photo card
  drawCircle(
    color = OnboardingElectricCyan,
    radius = w * 0.032f,
    center = Offset(photoLeft + cardW * 0.32f, photoTop + cardH * 0.34f)
  )
  val mountainPath = Path().apply {
    moveTo(photoLeft + cardW * 0.15f, photoTop + cardH * 0.82f)
    lineTo(photoLeft + cardW * 0.48f, photoTop + cardH * 0.45f)
    lineTo(photoLeft + cardW * 0.68f, photoTop + cardH * 0.68f)
    lineTo(photoLeft + cardW * 0.85f, photoTop + cardH * 0.52f)
    lineTo(photoLeft + cardW * 0.90f, photoTop + cardH * 0.82f)
    close()
  }
  drawPath(mountainPath, color = OnboardingPrimaryViolet.copy(alpha = 0.75f))

  // Front/Right Moving Video Card
  val videoLeft = w * 0.45f
  val videoTop = h * 0.42f
  drawRoundRect(
    color = OnboardingSecondarySurface,
    topLeft = Offset(videoLeft, videoTop),
    size = Size(cardW, cardH),
    cornerRadius = CornerRadius(8.dp.toPx())
  )
  drawRoundRect(
    color = OnboardingElectricCyan,
    topLeft = Offset(videoLeft, videoTop),
    size = Size(cardW, cardH),
    cornerRadius = CornerRadius(8.dp.toPx()),
    style = Stroke(width = 2.dp.toPx())
  )
  // Play button inside Video Card
  val playPath = Path().apply {
    val pcx = videoLeft + cardW * 0.5f
    val pcy = videoTop + cardH * 0.5f
    moveTo(pcx - w * 0.035f, pcy - h * 0.045f)
    lineTo(pcx - w * 0.035f, pcy + h * 0.045f)
    lineTo(pcx + w * 0.045f, pcy)
    close()
  }
  drawPath(playPath, color = OnboardingMainText)

  // AI Motion Sparkle Nodes connecting Image -> Video
  drawCircle(color = OnboardingElectricCyan, radius = 4.dp.toPx(), center = Offset(cx, h * 0.34f))
  drawCircle(color = OnboardingPrimaryViolet, radius = 3.dp.toPx(), center = Offset(w * 0.36f, h * 0.66f))
}

/**
 * 5. AI Effects Illustration:
 * Cinematic visual transformation prism rings and 4-point AI star sparkles.
 */
private fun DrawScope.drawAiEffectsIllustration(w: Float, h: Float, cx: Float, cy: Float) {
  // Concentric rotated glowing frames
  val boxSize = w * 0.38f
  drawRoundRect(
    color = OnboardingCardSurface,
    topLeft = Offset(cx - boxSize * 0.5f, cy - boxSize * 0.5f),
    size = Size(boxSize, boxSize),
    cornerRadius = CornerRadius(14.dp.toPx())
  )
  drawRoundRect(
    brush = Brush.linearGradient(
      colors = listOf(OnboardingPrimaryViolet, OnboardingElectricCyan)
    ),
    topLeft = Offset(cx - boxSize * 0.5f, cy - boxSize * 0.5f),
    size = Size(boxSize, boxSize),
    cornerRadius = CornerRadius(14.dp.toPx()),
    style = Stroke(width = 2.2.dp.toPx())
  )

  // Central 4-point AI Star Sparkle
  drawFourPointStar(
    center = Offset(cx, cy),
    outerRadius = w * 0.14f,
    innerRadius = w * 0.042f,
    color = OnboardingElectricCyan
  )
  // Secondary Violet 4-point Stars
  drawFourPointStar(
    center = Offset(w * 0.28f, h * 0.30f),
    outerRadius = w * 0.06f,
    innerRadius = w * 0.02f,
    color = OnboardingBrightViolet
  )
  drawFourPointStar(
    center = Offset(w * 0.72f, h * 0.68f),
    outerRadius = w * 0.055f,
    innerRadius = w * 0.018f,
    color = OnboardingPrimaryViolet
  )
}

/**
 * 6. AI Avatar Illustration:
 * Modern digital AI human character silhouette inside a holographic neural scan ring.
 */
private fun DrawScope.drawAiAvatarIllustration(w: Float, h: Float, cx: Float, cy: Float) {
  // Outer Holographic Neural Ring
  val ringR = w * 0.26f
  drawCircle(
    color = OnboardingCardSurface,
    radius = ringR,
    center = Offset(cx, cy)
  )
  drawCircle(
    brush = Brush.sweepGradient(
      colors = listOf(
        OnboardingElectricCyan,
        OnboardingPrimaryViolet,
        OnboardingElectricCyan
      )
    ),
    radius = ringR,
    center = Offset(cx, cy),
    style = Stroke(width = 2.4.dp.toPx())
  )

  // Avatar Head Circle
  drawCircle(
    color = OnboardingBrightViolet,
    radius = w * 0.085f,
    center = Offset(cx, cy - h * 0.06f)
  )
  drawCircle(
    color = OnboardingElectricCyan,
    radius = w * 0.085f,
    center = Offset(cx, cy - h * 0.06f),
    style = Stroke(width = 1.6.dp.toPx())
  )

  // Avatar Shoulders Arc/Bust
  val bustPath = Path().apply {
    moveTo(cx - w * 0.15f, cy + h * 0.16f)
    quadraticTo(
      cx,
      cy + h * 0.01f,
      cx + w * 0.15f,
      cy + h * 0.16f
    )
    close()
  }
  drawPath(
    path = bustPath,
    brush = Brush.horizontalGradient(
      colors = listOf(OnboardingPrimaryViolet, OnboardingElectricCyan)
    )
  )

  // AI Neural Nodes around the avatar
  for (angleDeg in listOf(35.0, 145.0, 215.0, 325.0)) {
    val rad = Math.toRadians(angleDeg)
    val nx = cx + (ringR * 1.18f * cos(rad)).toFloat()
    val ny = cy + (ringR * 1.18f * sin(rad)).toFloat()
    drawCircle(
      color = OnboardingElectricCyan,
      radius = 3.5.dp.toPx(),
      center = Offset(nx, ny)
    )
  }
}

/**
 * 7. Auto Subtitles Illustration:
 * Video frame with speech waveform recognition and synchronized subtitle lines.
 */
private fun DrawScope.drawAutoSubtitlesIllustration(w: Float, h: Float, cx: Float, cy: Float) {
  val frameLeft = w * 0.20f
  val frameTop = h * 0.25f
  val frameW = w * 0.60f
  val frameH = h * 0.50f

  drawRoundRect(
    color = OnboardingCardSurface,
    topLeft = Offset(frameLeft, frameTop),
    size = Size(frameW, frameH),
    cornerRadius = CornerRadius(12.dp.toPx())
  )
  drawRoundRect(
    color = OnboardingPrimaryViolet,
    topLeft = Offset(frameLeft, frameTop),
    size = Size(frameW, frameH),
    cornerRadius = CornerRadius(12.dp.toPx()),
    style = Stroke(width = 2.dp.toPx())
  )

  // Speech waveform inside upper half of video frame
  val waveY = frameTop + frameH * 0.36f
  val amps = listOf(0.04f, 0.09f, 0.14f, 0.07f, 0.15f, 0.10f, 0.05f)
  amps.forEachIndexed { idx, amp ->
    val wx = frameLeft + frameW * (0.20f + idx * 0.10f)
    val wh = h * amp
    drawLine(
      color = OnboardingElectricCyan,
      start = Offset(wx, waveY - wh * 0.5f),
      end = Offset(wx, waveY + wh * 0.5f),
      strokeWidth = 3.dp.toPx(),
      cap = StrokeCap.Round
    )
  }

  // Highlighted Subtitle Caption Pill 1
  drawRoundRect(
    color = OnboardingPrimaryViolet,
    topLeft = Offset(frameLeft + frameW * 0.14f, frameTop + frameH * 0.62f),
    size = Size(frameW * 0.72f, frameH * 0.12f),
    cornerRadius = CornerRadius(4.dp.toPx())
  )
  // Subtitle Caption Pill 2
  drawRoundRect(
    color = OnboardingElectricCyan.copy(alpha = 0.85f),
    topLeft = Offset(frameLeft + frameW * 0.24f, frameTop + frameH * 0.79f),
    size = Size(frameW * 0.52f, frameH * 0.10f),
    cornerRadius = CornerRadius(4.dp.toPx())
  )
}

/**
 * 8. Remove BG Illustration:
 * Foreground subject separated from background grid with an electric cyan AI scan line.
 */
private fun DrawScope.drawRemoveBgIllustration(w: Float, h: Float, cx: Float, cy: Float) {
  val frameLeft = w * 0.22f
  val frameTop = h * 0.24f
  val frameW = w * 0.56f
  val frameH = h * 0.52f

  drawRoundRect(
    color = OnboardingCardSurface,
    topLeft = Offset(frameLeft, frameTop),
    size = Size(frameW, frameH),
    cornerRadius = CornerRadius(12.dp.toPx())
  )
  drawRoundRect(
    color = OnboardingPrimaryViolet,
    topLeft = Offset(frameLeft, frameTop),
    size = Size(frameW, frameH),
    cornerRadius = CornerRadius(12.dp.toPx()),
    style = Stroke(width = 2.dp.toPx())
  )

  // Right-half transparency checkerboard dots (representing removed background)
  for (row in 0..3) {
    for (col in 0..2) {
      val dotX = cx + w * 0.04f + col * (w * 0.065f)
      val dotY = frameTop + h * 0.09f + row * (h * 0.10f)
      drawRect(
        color = OnboardingSecondarySurface,
        topLeft = Offset(dotX, dotY),
        size = Size(w * 0.035f, w * 0.035f)
      )
    }
  }

  // Foreground Subject Silhouette in center
  drawCircle(
    color = OnboardingMainText,
    radius = w * 0.065f,
    center = Offset(cx, cy - h * 0.05f)
  )
  val subjectBody = Path().apply {
    moveTo(cx - w * 0.12f, cy + h * 0.15f)
    quadraticTo(cx, cy + h * 0.01f, cx + w * 0.12f, cy + h * 0.15f)
    close()
  }
  drawPath(subjectBody, color = OnboardingPrimaryViolet)

  // Vertical Electric Cyan AI Cutout Scan Line
  drawLine(
    color = OnboardingElectricCyan,
    start = Offset(cx, frameTop - 6.dp.toPx()),
    end = Offset(cx, frameTop + frameH + 6.dp.toPx()),
    strokeWidth = 2.6.dp.toPx(),
    cap = StrokeCap.Round
  )
}

private fun DrawScope.drawFourPointStar(
  center: Offset,
  outerRadius: Float,
  innerRadius: Float,
  color: Color
) {
  val path = Path().apply {
    moveTo(center.x, center.y - outerRadius)
    lineTo(center.x + innerRadius, center.y - innerRadius)
    lineTo(center.x + outerRadius, center.y)
    lineTo(center.x + innerRadius, center.y + innerRadius)
    lineTo(center.x, center.y + outerRadius)
    lineTo(center.x - innerRadius, center.y + innerRadius)
    lineTo(center.x - outerRadius, center.y)
    lineTo(center.x - innerRadius, center.y - innerRadius)
    close()
  }
  drawPath(path = path, color = color)
}
