package com.cineflow.ai.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch

// Exact Master Color Palette matching 02_Onboarding_Master_Reference_Video_Edit.png
val OnboardingMainBg = Color(0xFF040717)
val OnboardingSecondaryBg = Color(0xFF080B16)
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
val OnboardingIndicatorInactive = Color(0xFF475569)

/**
 * TASK 2 — Eight-Page Horizontally Swipeable Onboarding Screen Structure & Visual Layout
 *
 * Visual source of truth:
 * - Master layout & Page 1: `02_Onboarding_Master_Reference_Video_Edit.png`
 * - Page 2..8 illustrations: `03_Feature_Text_to_Speech.png` .. `09_Feature_Remove_BG.png`
 *
 * Enforces:
 * - 8-page horizontally swipeable container (`HorizontalPager`).
 * - Circular feature illustration container (`onboarding_illustration_$index`) displaying the
 *   exact 8 reference illustrations in the approved order.
 * - Identical circular illustration layout, size, position, and spacing across all 8 pages.
 * - Stable background (with top-left and bottom-right neon wave arcs), stable 8-dot indicator
 *   area, and stable bottom navigation area (`Previous` / `Next >` / `Get Started`) while pages change.
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
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(OnboardingMainBg)
        .padding(innerPadding)
    ) {
      // 1. Stable Top-Left Neon Wave Arc from 02_Onboarding_Master_Reference_Video_Edit.png
      Image(
        painter = painterResource(id = R.drawable.img_splash_top_wave),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.TopCenter)
          .testTag("onboarding_top_header")
      )

      // 2. Stable Bottom-Right Neon Wave Arc from 02_Onboarding_Master_Reference_Video_Edit.png
      Image(
        painter = painterResource(id = R.drawable.img_splash_bottom_wave),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .testTag("onboarding_bottom_wave")
      )

      // 3. Foreground Content Column: 8-Page HorizontalPager + Stable Indicator Dots + Stable Navigation Buttons
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Spacer(modifier = Modifier.height(12.dp))

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

        // Stable Bottom Section: 8 Circular Page Indicators + Previous / Next / Get Started Controls
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .padding(bottom = 12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Eight Circular Page Indicators matching 02_Onboarding_Master_Reference_Video_Edit.png
          Row(
            modifier = Modifier
              .padding(vertical = 12.dp)
              .testTag("onboarding_page_indicators"),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            pages.forEach { page ->
              val isSelected = page.index == currentPage
              val dotSize by animateDpAsState(
                targetValue = if (isSelected) 13.dp else 9.dp,
                animationSpec = tween(durationMillis = 220),
                label = "indicator_size_${page.index}"
              )
              val inactiveColor by animateColorAsState(
                targetValue = if (isSelected) OnboardingPrimaryViolet else OnboardingIndicatorInactive,
                animationSpec = tween(durationMillis = 220),
                label = "indicator_color_${page.index}"
              )

              Box(
                modifier = Modifier
                  .size(dotSize)
                  .then(
                    if (isSelected) {
                      Modifier
                        .shadow(
                          elevation = 8.dp,
                          shape = CircleShape,
                          ambientColor = OnboardingElectricCyan,
                          spotColor = OnboardingPrimaryViolet
                        )
                        .clip(CircleShape)
                        .background(
                          Brush.linearGradient(
                            colors = listOf(
                              OnboardingPrimaryViolet,
                              OnboardingElectricCyan
                            )
                          )
                        )
                        .border(
                          width = 1.5.dp,
                          brush = Brush.linearGradient(
                            colors = listOf(
                              Color.White,
                              OnboardingElectricCyan
                            )
                          ),
                          shape = CircleShape
                        )
                    } else {
                      Modifier
                        .clip(CircleShape)
                        .background(inactiveColor)
                    }
                  )
                  .semantics { selected = isSelected }
                  .testTag("onboarding_indicator_${page.index}")
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Navigation Buttons Row (Previous + Next > OR Previous + Get Started)
          val isFirstPage = currentPage == 0
          val isLastPage = currentPage == pages.lastIndex

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("onboarding_navigation_row"),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Previous Pill Button (matches 02_Onboarding_Master_Reference_Video_Edit.png)
            Box(
              modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .alpha(if (isFirstPage) 0.55f else 1f)
                .clip(RoundedCornerShape(27.dp))
                .background(OnboardingCardSurface)
                .border(
                  width = 1.dp,
                  color = OnboardingBorderColor,
                  shape = RoundedCornerShape(27.dp)
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
              Text(
                text = "Previous",
                color = if (isFirstPage) OnboardingMutedText else OnboardingSecondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
              )
            }

            if (!isLastPage) {
              // Next > Pill Button (Pages 0..6: matches 02_Onboarding_Master_Reference_Video_Edit.png)
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(54.dp)
                  .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(27.dp),
                    ambientColor = OnboardingPrimaryViolet,
                    spotColor = OnboardingPrimaryViolet
                  )
                  .clip(RoundedCornerShape(27.dp))
                  .background(
                    Brush.horizontalGradient(
                      colors = listOf(
                        Color(0xFF9333EA),
                        OnboardingPrimaryViolet,
                        Color(0xFF7C3AED)
                      )
                    )
                  )
                  .border(
                    width = 1.dp,
                    color = OnboardingBrightViolet.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(27.dp)
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
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "Next",
                    color = Color.White,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
            } else {
              // Get Started Pill Button (Final Page 7: Capsule shape, Violet -> Deep Violet + Cyan highlight)
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(54.dp)
                  .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(27.dp),
                    ambientColor = OnboardingPrimaryViolet,
                    spotColor = OnboardingElectricCyan
                  )
                  .clip(RoundedCornerShape(27.dp))
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
                    shape = RoundedCornerShape(27.dp)
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
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "Get Started",
                    color = Color.White,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = OnboardingElectricCyan,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Single Onboarding Page Content in strict vertical order matching `02_Onboarding_Master_Reference_Video_Edit.png`:
 * 1. Large circular feature illustration (`onboarding_illustration_$index`).
 * 2. Feature title (`onboarding_feature_title_$index`).
 * 3. Feature description (`onboarding_feature_description_$index`).
 */
@Composable
private fun OnboardingFeaturePageContent(
  page: OnboardingFeaturePage,
  modifier: Modifier = Modifier
) {
  androidx.compose.foundation.layout.BoxWithConstraints(
    modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    val illustrationSize = minOf(maxWidth * 0.72f, maxHeight * 0.48f, 272.dp).coerceAtLeast(136.dp)
    val titleTopSpacing = (maxHeight * 0.04f).coerceIn(10.dp, 26.dp)
    val descTopSpacing = (maxHeight * 0.02f).coerceIn(6.dp, 14.dp)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // 1. Large Circular Feature Illustration (Strictly 1:1 CircleShape, exact reference illustration)
      CircularFeatureIllustration(
        illustrationResId = page.illustrationResId,
        contentDescription = page.title,
        testTag = page.illustrationTestTag,
        modifier = Modifier.size(illustrationSize)
      )

      Spacer(modifier = Modifier.height(titleTopSpacing))

      // 2. Feature Title (#F8FAFC, bold typography matching 02_Onboarding_Master_Reference_Video_Edit.png)
      Text(
        text = page.title,
        color = OnboardingMainText,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        letterSpacing = 0.2.sp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("onboarding_feature_title_${page.index}")
      )

      Spacer(modifier = Modifier.height(descTopSpacing))

      // 3. Feature Description (#B8C2D6, centered, readable multi-line copy)
      Text(
        text = page.description,
        color = OnboardingSecondaryText,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp)
          .testTag("onboarding_feature_description_${page.index}")
      )
    }
  }
}

/**
 * Mandatory Circular Feature Illustration Container (Task 2).
 *
 * Displays the exact reference illustration (`02`..`09`) inside a strictly circular 1:1
 * container with consistent size, position, and spacing across all 8 pages.
 */
@Composable
fun CircularFeatureIllustration(
  @DrawableRes illustrationResId: Int,
  contentDescription: String,
  testTag: String,
  modifier: Modifier = Modifier.size(240.dp)
) {
  Box(
    modifier = modifier
      .aspectRatio(1f)
      .shadow(
        elevation = 20.dp,
        shape = CircleShape,
        ambientColor = OnboardingPrimaryViolet,
        spotColor = OnboardingElectricCyan
      )
      .clip(CircleShape)
      .background(OnboardingMainBg)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(id = illustrationResId),
      contentDescription = contentDescription,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
        .testTag("${testTag}_image")
    )
  }
}
