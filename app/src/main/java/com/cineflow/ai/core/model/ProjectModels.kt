package com.cineflow.ai.core.model

enum class TrackType {
  MAIN_VIDEO,
  SECONDARY_VIDEO,
  OVERLAY,
  AUDIO,
  VOICE_OVER,
  TEXT,
  SUBTITLE,
  STICKER,
  EFFECTS,
  ADJUSTMENT
}

enum class ProjectFilterTab {
  RECENT,
  ALL_PROJECTS,
  FAVORITES
}

enum class ProjectSortOption(val label: String) {
  UPDATED_DESC("Recently Updated"),
  CREATED_DESC("Newest Created"),
  NAME_ASC("Name (A–Z)"),
  DURATION_DESC("Duration (Longest)")
}

data class VideoProject(
  val projectId: String,
  val name: String,
  val aspectRatio: String = "16:9",
  val durationMicros: Long = 0L,
  val frameRateNum: Int = 30,
  val frameRateDen: Int = 1,
  val resolutionLabel: String = "1080p",
  val statusTag: String = "Draft",
  val isFavorite: Boolean = false,
  val visualThemeIndex: Int = 0,
  val trackCount: Int = 1,
  val clipCount: Int = 0,
  val missingMediaCount: Int = 0,
  val schemaVersion: Int = 1,
  val createdAtEpochMillis: Long,
  val updatedAtEpochMillis: Long
) {
  val displayFps: Int
    get() = if (frameRateDen > 0) (frameRateNum / frameRateDen).coerceAtLeast(1) else 30
}

enum class ExportResolutionCap(
  val label: String,
  val maxShortEdgePx: Int,
  val requiresAuthentication: Boolean
) {
  SD_480P("480p", 480, false),
  HD_720P("720p", 720, false),
  FHD_1080P("1080p", 1080, true),
  QHD_2K("2K (1440p)", 1440, true),
  UHD_4K("4K (2160p)", 2160, true),
  UHD_8K("8K (4320p)", 4320, true);

  companion object {
    fun isAllowedForSession(resolution: ExportResolutionCap, isAuthenticated: Boolean): Boolean {
      return isAuthenticated || !resolution.requiresAuthentication
    }
  }
}

/**
 * Canonical timeline clip/item model using 64-bit microsecond timebase (Sections 15 & 16).
 */
data class TimelineClipItem(
  val clipId: String,
  val projectId: String,
  val trackType: TrackType = TrackType.MAIN_VIDEO,
  val trackIndex: Int = 0,
  val label: String,
  val mediaUri: String? = null,
  val mimeType: String = "video/mp4",
  val sourceDurationMicros: Long = 5_000_000L,
  val sourceInMicros: Long = 0L,
  val sourceOutMicros: Long = 5_000_000L,
  val timelineStartMicros: Long = 0L,
  val speedMultiplier: Float = 1.0f,
  val volume: Float = 1.0f,
  val isMuted: Boolean = false,
  val rotationDegrees: Int = 0,
  val isMirrored: Boolean = false,
  val overlayText: String? = null,
  val overlayNormalizedX: Float = 0.5f,
  val overlayNormalizedY: Float = 0.5f,
  val overlayScale: Float = 1.0f,
  val transitionAfter: String? = null,
  val effectPreset: String? = null,
  val filterPreset: String? = null,
  val orderIndex: Int = 0
) {
  val timelineDurationMicros: Long
    get() {
      val trimmedSource = (sourceOutMicros - sourceInMicros).coerceAtLeast(100_000L)
      val safeSpeed = speedMultiplier.coerceIn(0.1f, 100f)
      return (trimmedSource / safeSpeed).toLong().coerceAtLeast(100_000L)
    }

  val timelineEndMicros: Long
    get() = timelineStartMicros + timelineDurationMicros
}

/**
 * Approved Main Categories for the Video Editor bottom navigation bar (Section 13).
 */
enum class EditorMainCategory(val id: String, val label: String) {
  EDIT("edit", "Edit"),
  AUDIO("audio", "Audio"),
  TEXT("text", "Text"),
  OVERLAY("overlay", "Overlay"),
  EFFECTS("effects", "Effects"),
  CAPTIONS("captions", "Captions"),
  ASPECT_RATIO("aspect_ratio", "Aspect ratio"),
  FILTERS("filters", "Filters"),
  TRANSITIONS("transitions", "Transitions"),
  STICKERS("stickers", "Stickers"),
  AI_TOOLS("ai_tools", "AI Tools"),
  MORE("more", "More")
}

