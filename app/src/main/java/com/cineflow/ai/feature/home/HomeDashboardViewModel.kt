package com.cineflow.ai.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cineflow.ai.R
import com.cineflow.ai.core.model.VideoProject
import com.cineflow.ai.data.project.LocalProjectRepository
import com.cineflow.ai.ui.theme.ToolAiAvatarBg
import com.cineflow.ai.ui.theme.ToolAiAvatarCircle
import com.cineflow.ai.ui.theme.ToolAiEffectsBg
import com.cineflow.ai.ui.theme.ToolAiEffectsCircle
import com.cineflow.ai.ui.theme.ToolAutoSubtitlesBg
import com.cineflow.ai.ui.theme.ToolAutoSubtitlesCircle
import com.cineflow.ai.ui.theme.ToolImageToVideoBg
import com.cineflow.ai.ui.theme.ToolImageToVideoCircle
import com.cineflow.ai.ui.theme.ToolMoreToolsBg
import com.cineflow.ai.ui.theme.ToolMoreToolsCircle
import com.cineflow.ai.ui.theme.ToolRemoveBgBg
import com.cineflow.ai.ui.theme.ToolRemoveBgCircle
import com.cineflow.ai.ui.theme.ToolTtsBg
import com.cineflow.ai.ui.theme.ToolTtsCircle
import com.cineflow.ai.ui.theme.ToolVideoEditBg
import com.cineflow.ai.ui.theme.ToolVideoEditCircle
import com.cineflow.ai.ui.theme.ToolVideoLangBg
import com.cineflow.ai.ui.theme.ToolVideoLangCircle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

enum class HomeToolId {
  VIDEO_EDIT,
  TEXT_TO_SPEECH,
  VIDEO_TO_LANGUAGE,
  IMAGE_TO_VIDEO,
  AI_EFFECTS,
  AI_AVATAR,
  AUTO_SUBTITLES,
  MORE_TOOLS,
  REMOVE_BG
}

data class HomeToolItem(
  val id: HomeToolId,
  val titleRes: Int,
  val titleText: String,
  @DrawableRes val iconDrawableRes: Int,
  val testTag: String,
  val cardBackground: Color,
  val iconCircleColor: Color,
  val isActiveInInitialScope: Boolean
)

data class TrendingTemplateItem(
  val id: String,
  val emoji: String,
  val titleRes: Int,
  val titleText: String,
  @DrawableRes val imageRes: Int,
  val testTag: String,
  val primaryColor: Color,
  val secondaryColor: Color,
  val accentColor: Color
)

enum class BottomNavTab(val route: String) {
  HOME("home_dashboard"),
  PROJECTS("project_management"),
  NOTIFICATIONS("notifications"),
  SETTINGS("settings")
}

class HomeDashboardViewModel(
  private val localProjectRepository: LocalProjectRepository
) : ViewModel() {

  val allTools: List<HomeToolItem> = listOf(
    HomeToolItem(
      id = HomeToolId.VIDEO_EDIT,
      titleRes = R.string.tool_video_edit,
      titleText = "Video Edit",
      iconDrawableRes = R.drawable.img_tool_video_edit,
      testTag = "tool_card_video_edit",
      cardBackground = ToolVideoEditBg,
      iconCircleColor = ToolVideoEditCircle,
      isActiveInInitialScope = true
    ),
    HomeToolItem(
      id = HomeToolId.TEXT_TO_SPEECH,
      titleRes = R.string.tool_text_to_speech,
      titleText = "Text-to-Speech",
      iconDrawableRes = R.drawable.img_tool_tts,
      testTag = "tool_card_text_to_speech",
      cardBackground = ToolTtsBg,
      iconCircleColor = ToolTtsCircle,
      isActiveInInitialScope = false
    ),
    HomeToolItem(
      id = HomeToolId.VIDEO_TO_LANGUAGE,
      titleRes = R.string.tool_video_to_language,
      titleText = "Video-to-Language",
      iconDrawableRes = R.drawable.img_tool_video_lang,
      testTag = "tool_card_video_to_language",
      cardBackground = ToolVideoLangBg,
      iconCircleColor = ToolVideoLangCircle,
      isActiveInInitialScope = false
    ),
    HomeToolItem(
      id = HomeToolId.IMAGE_TO_VIDEO,
      titleRes = R.string.tool_image_to_video,
      titleText = "Image to Video",
      iconDrawableRes = R.drawable.img_tool_image_to_video,
      testTag = "tool_card_image_to_video",
      cardBackground = ToolImageToVideoBg,
      iconCircleColor = ToolImageToVideoCircle,
      isActiveInInitialScope = false
    ),
    HomeToolItem(
      id = HomeToolId.AI_EFFECTS,
      titleRes = R.string.tool_ai_effects,
      titleText = "AI Effects",
      iconDrawableRes = R.drawable.img_tool_ai_effects,
      testTag = "tool_card_ai_effects",
      cardBackground = ToolAiEffectsBg,
      iconCircleColor = ToolAiEffectsCircle,
      isActiveInInitialScope = false
    ),
    HomeToolItem(
      id = HomeToolId.AI_AVATAR,
      titleRes = R.string.tool_ai_avatar,
      titleText = "AI Avatar",
      iconDrawableRes = R.drawable.img_tool_ai_avatar,
      testTag = "tool_card_ai_avatar",
      cardBackground = ToolAiAvatarBg,
      iconCircleColor = ToolAiAvatarCircle,
      isActiveInInitialScope = false
    ),
    HomeToolItem(
      id = HomeToolId.AUTO_SUBTITLES,
      titleRes = R.string.tool_auto_subtitles,
      titleText = "Auto Subtitles",
      iconDrawableRes = R.drawable.img_tool_auto_subtitles,
      testTag = "tool_card_auto_subtitles",
      cardBackground = ToolAutoSubtitlesBg,
      iconCircleColor = ToolAutoSubtitlesCircle,
      isActiveInInitialScope = false
    ),
    HomeToolItem(
      id = HomeToolId.MORE_TOOLS,
      titleRes = R.string.tool_more_tools,
      titleText = "More Tools",
      iconDrawableRes = R.drawable.img_tool_more_tools,
      testTag = "tool_card_more_tools",
      cardBackground = ToolMoreToolsBg,
      iconCircleColor = ToolMoreToolsCircle,
      isActiveInInitialScope = false
    ),
    HomeToolItem(
      id = HomeToolId.REMOVE_BG,
      titleRes = R.string.tool_remove_bg,
      titleText = "Remove BG",
      iconDrawableRes = R.drawable.img_tool_remove_bg,
      testTag = "tool_card_remove_bg",
      cardBackground = ToolRemoveBgBg,
      iconCircleColor = ToolRemoveBgCircle,
      isActiveInInitialScope = false
    )
  )

  val allTemplates: List<TrendingTemplateItem> = listOf(
    TrendingTemplateItem(
      id = "trending",
      emoji = "\uD83D\uDD25",
      titleRes = R.string.template_trending,
      titleText = "Trending",
      imageRes = R.drawable.img_template_trending,
      testTag = "template_card_trending",
      primaryColor = Color(0xFF4A154B),
      secondaryColor = Color(0xFFC83E7B),
      accentColor = Color(0xFFFFB347)
    ),
    TrendingTemplateItem(
      id = "love",
      emoji = "\u2764\uFE0F",
      titleRes = R.string.template_love,
      titleText = "Love",
      imageRes = R.drawable.img_template_love,
      testTag = "template_card_love",
      primaryColor = Color(0xFF5C2018),
      secondaryColor = Color(0xFFD96B43),
      accentColor = Color(0xFFFFD194)
    ),
    TrendingTemplateItem(
      id = "travel",
      emoji = "\uD83C\uDFD4\uFE0F",
      titleRes = R.string.template_travel,
      titleText = "Travel",
      imageRes = R.drawable.img_template_travel,
      testTag = "template_card_travel",
      primaryColor = Color(0xFF0F4C75),
      secondaryColor = Color(0xFF3282B8),
      accentColor = Color(0xFFBBE1FA)
    ),
    TrendingTemplateItem(
      id = "birthday",
      emoji = "\uD83C\uDF82",
      titleRes = R.string.template_birthday,
      titleText = "Birthday",
      imageRes = R.drawable.img_template_birthday,
      testTag = "template_card_birthday",
      primaryColor = Color(0xFF5A2A18),
      secondaryColor = Color(0xFFC06C3E),
      accentColor = Color(0xFFFFE082)
    ),
    TrendingTemplateItem(
      id = "festival",
      emoji = "\uD83C\uDF89",
      titleRes = R.string.template_festival,
      titleText = "Festival",
      imageRes = R.drawable.img_template_festival,
      testTag = "template_card_festival",
      primaryColor = Color(0xFF6B1111),
      secondaryColor = Color(0xFFD43F3F),
      accentColor = Color(0xFFFFD54F)
    )
  )

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _heroBannerPage = MutableStateFlow(0)
  val heroBannerPage: StateFlow<Int> = _heroBannerPage.asStateFlow()

  private val _comingSoonFeatureName = MutableStateFlow<String?>(null)
  val comingSoonFeatureName: StateFlow<String?> = _comingSoonFeatureName.asStateFlow()

  private val _showAccountSheet = MutableStateFlow(false)
  val showAccountSheet: StateFlow<Boolean> = _showAccountSheet.asStateFlow()

  val localProjects: StateFlow<List<VideoProject>> = localProjectRepository.projectsFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000L),
      initialValue = emptyList()
    )

  fun onSearchQueryChanged(query: String) {
    _searchQuery.value = query
  }

  fun onHeroPageSelected(page: Int) {
    _heroBannerPage.value = page.coerceIn(0, 3)
  }

  fun showComingSoon(featureName: String) {
    _comingSoonFeatureName.value = featureName
  }

  fun dismissComingSoon() {
    _comingSoonFeatureName.value = null
  }

  fun setAccountSheetVisible(visible: Boolean) {
    _showAccountSheet.value = visible
  }

  companion object {
    fun provideFactory(repository: LocalProjectRepository): ViewModelProvider.Factory {
      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return HomeDashboardViewModel(repository) as T
        }
      }
    }
  }
}
