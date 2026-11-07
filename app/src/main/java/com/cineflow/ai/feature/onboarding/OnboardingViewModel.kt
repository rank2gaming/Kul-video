package com.cineflow.ai.feature.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_SPLASH = "splash"
const val ROUTE_ONBOARDING = "onboarding"

enum class OnboardingFeatureType {
  VIDEO_EDIT,
  TEXT_TO_SPEECH,
  VIDEO_TO_LANGUAGE,
  IMAGE_TO_VIDEO,
  AI_EFFECTS,
  AI_AVATAR,
  AUTO_SUBTITLES,
  REMOVE_BG
}

data class OnboardingFeaturePage(
  val index: Int,
  val featureType: OnboardingFeatureType,
  val title: String,
  val description: String,
  val illustrationTestTag: String
)

/**
 * ViewModel for the 8-Page Onboarding Experience (Master Task Instructions Sections 5, 7, 9, 10).
 *
 * Strictly contains the 8 approved CineFlow AI features:
 * 1. Video Edit
 * 2. Text-to-Speech
 * 3. Video-to-Language
 * 4. Image to Video
 * 5. AI Effects
 * 6. AI Avatar
 * 7. Auto Subtitles
 * 8. Remove BG
 * (More Tools is NOT included as a 9th page.)
 */
class OnboardingViewModel : ViewModel() {

  val pages: List<OnboardingFeaturePage> = listOf(
    OnboardingFeaturePage(
      index = 0,
      featureType = OnboardingFeatureType.VIDEO_EDIT,
      title = "Video Edit",
      description = "Edit videos using essential and advanced editing tools. Trim, cut, arrange, and customize video clips to create professional videos.",
      illustrationTestTag = "onboarding_illustration_0"
    ),
    OnboardingFeaturePage(
      index = 1,
      featureType = OnboardingFeatureType.TEXT_TO_SPEECH,
      title = "Text-to-Speech",
      description = "Convert written text into natural-sounding AI-generated speech and create voiceovers for your videos.",
      illustrationTestTag = "onboarding_illustration_1"
    ),
    OnboardingFeaturePage(
      index = 2,
      featureType = OnboardingFeatureType.VIDEO_TO_LANGUAGE,
      title = "Video-to-Language",
      description = "Translate video content into different languages and make video communication accessible to a wider audience.",
      illustrationTestTag = "onboarding_illustration_2"
    ),
    OnboardingFeaturePage(
      index = 3,
      featureType = OnboardingFeatureType.IMAGE_TO_VIDEO,
      title = "Image to Video",
      description = "Transform static images into engaging videos using AI-powered image-to-video generation.",
      illustrationTestTag = "onboarding_illustration_3"
    ),
    OnboardingFeaturePage(
      index = 4,
      featureType = OnboardingFeatureType.AI_EFFECTS,
      title = "AI Effects",
      description = "Enhance videos with creative AI-powered effects and visual transformations.",
      illustrationTestTag = "onboarding_illustration_4"
    ),
    OnboardingFeaturePage(
      index = 5,
      featureType = OnboardingFeatureType.AI_AVATAR,
      title = "AI Avatar",
      description = "Create and use AI avatars to produce engaging digital content.",
      illustrationTestTag = "onboarding_illustration_5"
    ),
    OnboardingFeaturePage(
      index = 6,
      featureType = OnboardingFeatureType.AUTO_SUBTITLES,
      title = "Auto Subtitles",
      description = "Automatically generate subtitles from video speech and make your content easier to understand.",
      illustrationTestTag = "onboarding_illustration_6"
    ),
    OnboardingFeaturePage(
      index = 7,
      featureType = OnboardingFeatureType.REMOVE_BG,
      title = "Remove BG",
      description = "Remove unwanted backgrounds from images or videos to create clean and professional visual content.",
      illustrationTestTag = "onboarding_illustration_7"
    )
  )

  private val _currentPage = MutableStateFlow(0)
  val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

  private val _isOnboardingCompleted = MutableStateFlow(false)
  val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

  val totalPages: Int
    get() = pages.size

  fun onPageChanged(page: Int) {
    val clamped = page.coerceIn(0, pages.lastIndex)
    if (_currentPage.value != clamped) {
      _currentPage.value = clamped
    }
  }

  fun nextPage(): Int {
    val next = (_currentPage.value + 1).coerceAtMost(pages.lastIndex)
    _currentPage.value = next
    return next
  }

  fun previousPage(): Int {
    val prev = (_currentPage.value - 1).coerceAtLeast(0)
    _currentPage.value = prev
    return prev
  }

  fun syncCompletionFromStorage(completed: Boolean) {
    _isOnboardingCompleted.value = completed
  }

  fun completeOnboarding() {
    _isOnboardingCompleted.value = true
  }
}
