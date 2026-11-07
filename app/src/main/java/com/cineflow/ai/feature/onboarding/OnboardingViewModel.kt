package com.cineflow.ai.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import com.cineflow.ai.R
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
  @DrawableRes val illustrationResId: Int,
  val illustrationTestTag: String
)

/**
 * ViewModel for the 8-Page Onboarding Experience.
 *
 * Strictly contains the 8 approved CineFlow AI features in the exact approved order,
 * mapped 1-to-1 to the 8 approved reference illustrations:
 * 1. Video Edit        -> `02_Onboarding_Master_Reference_Video_Edit.png` (`R.drawable.img_onboarding_01_video_edit`)
 * 2. Text-to-Speech    -> `03_Feature_Text_to_Speech.png` (`R.drawable.img_onboarding_02_tts`)
 * 3. Video-to-Language -> `04_Feature_Video_to_Language.png` (`R.drawable.img_onboarding_03_video_to_language`)
 * 4. Image to Video    -> `05_Feature_Image_to_Video.png` (`R.drawable.img_onboarding_04_image_to_video`)
 * 5. AI Effects        -> `06_Feature_AI_Effects.png` (`R.drawable.img_onboarding_05_ai_effects`)
 * 6. AI Avatar         -> `07_Feature_AI_Avatar.png` (`R.drawable.img_onboarding_06_ai_avatar`)
 * 7. Auto Subtitles    -> `08_Feature_Auto_Subtitles.png` (`R.drawable.img_onboarding_07_auto_subtitles`)
 * 8. Remove BG         -> `09_Feature_Remove_BG.png` (`R.drawable.img_onboarding_08_remove_bg`)
 *
 * More Tools is NOT included as an onboarding feature.
 */
class OnboardingViewModel : ViewModel() {

  val pages: List<OnboardingFeaturePage> = listOf(
    OnboardingFeaturePage(
      index = 0,
      featureType = OnboardingFeatureType.VIDEO_EDIT,
      title = "Video Edit",
      description = "Edit videos using essential and advanced editing tools. Trim, cut, arrange, and customize video clips to create professional videos.",
      illustrationResId = R.drawable.img_onboarding_01_video_edit,
      illustrationTestTag = "onboarding_illustration_0"
    ),
    OnboardingFeaturePage(
      index = 1,
      featureType = OnboardingFeatureType.TEXT_TO_SPEECH,
      title = "Text-to-Speech",
      description = "Convert written text into natural-sounding AI-generated speech and create voiceovers for your videos.",
      illustrationResId = R.drawable.img_onboarding_02_tts,
      illustrationTestTag = "onboarding_illustration_1"
    ),
    OnboardingFeaturePage(
      index = 2,
      featureType = OnboardingFeatureType.VIDEO_TO_LANGUAGE,
      title = "Video-to-Language",
      description = "Translate video content into different languages and make video communication accessible to a wider audience.",
      illustrationResId = R.drawable.img_onboarding_03_video_to_language,
      illustrationTestTag = "onboarding_illustration_2"
    ),
    OnboardingFeaturePage(
      index = 3,
      featureType = OnboardingFeatureType.IMAGE_TO_VIDEO,
      title = "Image to Video",
      description = "Transform static images into engaging videos using AI-powered image-to-video generation.",
      illustrationResId = R.drawable.img_onboarding_04_image_to_video,
      illustrationTestTag = "onboarding_illustration_3"
    ),
    OnboardingFeaturePage(
      index = 4,
      featureType = OnboardingFeatureType.AI_EFFECTS,
      title = "AI Effects",
      description = "Enhance videos with creative AI-powered effects and visual transformations.",
      illustrationResId = R.drawable.img_onboarding_05_ai_effects,
      illustrationTestTag = "onboarding_illustration_4"
    ),
    OnboardingFeaturePage(
      index = 5,
      featureType = OnboardingFeatureType.AI_AVATAR,
      title = "AI Avatar",
      description = "Create and use AI avatars to produce engaging digital content.",
      illustrationResId = R.drawable.img_onboarding_06_ai_avatar,
      illustrationTestTag = "onboarding_illustration_5"
    ),
    OnboardingFeaturePage(
      index = 6,
      featureType = OnboardingFeatureType.AUTO_SUBTITLES,
      title = "Auto Subtitles",
      description = "Automatically generate subtitles from video speech and make your content easier to understand.",
      illustrationResId = R.drawable.img_onboarding_07_auto_subtitles,
      illustrationTestTag = "onboarding_illustration_6"
    ),
    OnboardingFeaturePage(
      index = 7,
      featureType = OnboardingFeatureType.REMOVE_BG,
      title = "Remove BG",
      description = "Remove unwanted backgrounds from images or videos to create clean and professional visual content.",
      illustrationResId = R.drawable.img_onboarding_08_remove_bg,
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
