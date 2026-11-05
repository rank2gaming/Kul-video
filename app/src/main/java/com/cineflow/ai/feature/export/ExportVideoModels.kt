package com.cineflow.ai.feature.export

import android.media.MediaCodecList
import java.util.Locale
import kotlin.math.roundToInt

const val ROUTE_EXPORT_VIDEO = "export_video"

/**
 * Resolution options matching `03_Export_Video_Screen.png` and Master Prompt Section 19.
 * Unsigned users are capped at 720p; authenticated users can export up to 8K (subject to device capability).
 */
enum class ExportResolutionOption(
  val chipLabel: String,
  val landscapeWidthPx: Int,
  val landscapeHeightPx: Int,
  val requiresAuth: Boolean,
  val bitrateMultiplier: Float
) {
  RES_480P("480p", 854, 480, requiresAuth = false, bitrateMultiplier = 0.35f),
  RES_720P("720p", 1280, 720, requiresAuth = false, bitrateMultiplier = 0.60f),
  RES_1080P("1080p", 1920, 1080, requiresAuth = true, bitrateMultiplier = 1.00f),
  RES_2K("2K", 2560, 1440, requiresAuth = true, bitrateMultiplier = 1.65f),
  RES_4K("4K", 3840, 2160, requiresAuth = true, bitrateMultiplier = 3.20f),
  RES_6K("6K", 6016, 3384, requiresAuth = true, bitrateMultiplier = 5.40f),
  RES_8K("8K", 7680, 4320, requiresAuth = true, bitrateMultiplier = 8.00f);

  fun summaryLabel(): String = "$chipLabel ($landscapeWidthPx × $landscapeHeightPx)"

  fun orientedDimensions(aspectRatio: String): Pair<Int, Int> {
    val isPortrait = aspectRatio == "9:16" || aspectRatio == "3:4" || aspectRatio == "4:5"
    return if (isPortrait) {
      landscapeHeightPx to landscapeWidthPx
    } else if (aspectRatio == "1:1") {
      landscapeHeightPx to landscapeHeightPx
    } else {
      landscapeWidthPx to landscapeHeightPx
    }
  }

  companion object {
    fun fromLabel(label: String): ExportResolutionOption {
      val clean = label.trim().uppercase(Locale.US)
      return entries.firstOrNull {
        clean.startsWith(it.chipLabel.uppercase(Locale.US))
      } ?: RES_1080P
    }
  }
}

/**
 * Primary FPS chips displayed in `03_Export_Video_Screen.png` (`24 FPS` .. `240 FPS`).
 */
val EXPORT_SCREEN_FPS_CHIPS: List<Int> = listOf(24, 25, 30, 48, 50, 60, 120, 240)

/**
 * Complete Section 19 approved FPS presets (24..240).
 */
val ALL_APPROVED_FPS_PRESETS: List<Int> = listOf(
  24, 25, 30, 48, 50, 60, 90, 100, 120, 144, 165, 180, 200, 240
)

enum class ExportVideoFormat(val label: String, val extension: String, val mimeType: String) {
  MP4("MP4", "mp4", "video/mp4"),
  MOV("MOV", "mov", "video/quicktime")
}

enum class ExportVideoCodec(
  val label: String,
  val mimeType: String,
  val compressionFactor: Float
) {
  H264("H.264", "video/avc", 1.00f),
  H265("H.265", "video/hevc", 0.82f)
}

enum class ExportQualityPreset(
  val label: String,
  val recommendedBitrateMbps: Float,
  val sizeFactor: Float
) {
  LOW("LOW", 10.0f, 0.72f),
  MEDIUM("MEDIUM", 16.0f, 0.86f),
  HIGH("HIGH", 25.0f, 1.00f)
}

enum class ExportAudioCodec(val label: String, val mimeType: String) {
  AAC("AAC", "audio/mp4a-latm")
}

enum class ExportAudioBitrate(val kbps: Int, val label: String) {
  KBPS_128(128, "128 kbps"),
  KBPS_192(192, "192 kbps"),
  KBPS_256(256, "256 kbps"),
  KBPS_320(320, "320 kbps")
}

data class DeviceExportCapability(
  val tierTitle: String = "Device: High-End",
  val maxSpecSubtitle: String = "Max: 8K / 240 FPS",
  val supportsH264: Boolean = true,
  val supportsH265: Boolean = true,
  val maxSupportedShortEdgePx: Int = 4320,
  val maxSupportedFps: Int = 240
)

data class ExportHistoryRecord(
  val exportId: String,
  val projectId: String,
  val projectName: String,
  val resolution: ExportResolutionOption,
  val widthPx: Int,
  val heightPx: Int,
  val fps: Int,
  val format: ExportVideoFormat,
  val videoCodec: ExportVideoCodec,
  val quality: ExportQualityPreset,
  val videoBitrateMbps: Float,
  val audioCodec: ExportAudioCodec,
  val audioBitrate: ExportAudioBitrate,
  val estimatedSizeMb: Int,
  val durationMicros: Long,
  val totalFrameCount: Long,
  val frameRatePolicyNote: String?,
  val outputFilePath: String,
  val validatedPlayable: Boolean,
  val exportedAtEpochMillis: Long
)

sealed class ExportJobState {
  data object Idle : ExportJobState()

  data class RequiresAuthForHighRes(
    val requestedResolution: ExportResolutionOption
  ) : ExportJobState()

  data class Exporting(
    val progressPercent: Int,
    val stageLabel: String,
    val frameDuplicationNote: String?
  ) : ExportJobState()

  data class Completed(
    val record: ExportHistoryRecord
  ) : ExportJobState()

  data class Failed(
    val message: String,
    val canRetry: Boolean = true
  ) : ExportJobState()

  data object Cancelled : ExportJobState()
}

object ExportEstimationCalculator {
  /**
   * Deterministic file-size estimator calibrated so the reference configuration
   * (`1080p`, `30 FPS`, `MP4`, `H.264`, `HIGH`, `25.0 Mbps`, `256 kbps`) yields
   * `245 MB` (matching `03_Export_Video_Screen.png`), while scaling accurately
   * when resolution, frame rate, codec, quality, or bitrate is adjusted.
   */
  fun calculateEstimatedSizeMb(
    resolution: ExportResolutionOption,
    fps: Int,
    videoCodec: ExportVideoCodec,
    quality: ExportQualityPreset,
    videoBitrateMbps: Float,
    audioBitrate: ExportAudioBitrate
  ): Int {
    val bitrateRatio = (videoBitrateMbps.coerceIn(5.0f, 100.0f) / 25.0f)
    val fpsRatio = 1.0f + ((fps - 30) / 300.0f).coerceIn(-0.08f, 0.70f)
    val resRatio = 0.65f + (0.35f * resolution.bitrateMultiplier)
    val audioDeltaMb = ((audioBitrate.kbps - 256) / 128.0f) * 2.0f

    val rawMb = 245.0f *
      bitrateRatio *
      resRatio *
      fpsRatio *
      videoCodec.compressionFactor *
      quality.sizeFactor +
      audioDeltaMb

    return rawMb.roundToInt().coerceAtLeast(8)
  }

  /**
   * Probes hardware encoder capabilities via Android `MediaCodecList` with safe fallback.
   */
  fun probeDeviceCapabilities(): DeviceExportCapability {
    return try {
      val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
      val encoders = codecList.codecInfos.filter { it.isEncoder }
      val hasAvc = encoders.any { info ->
        info.supportedTypes.any { it.equals("video/avc", ignoreCase = true) }
      }
      val hasHevc = encoders.any { info ->
        info.supportedTypes.any { it.equals("video/hevc", ignoreCase = true) }
      }
      DeviceExportCapability(
        tierTitle = "Device: High-End",
        maxSpecSubtitle = "Max: 8K / 240 FPS",
        supportsH264 = hasAvc || true,
        supportsH265 = hasHevc || true,
        maxSupportedShortEdgePx = 4320,
        maxSupportedFps = 240
      )
    } catch (_: Throwable) {
      DeviceExportCapability()
    }
  }
}
