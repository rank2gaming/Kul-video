package com.cineflow.ai.feature.export

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Export Video Screen (`03_Export_Video_Screen.png` & Master Prompt Section 19).
 *
 * Implements:
 * 1. Top Header Bar (`←`, `CineFlow AI`, `|`, `Export Video`, and `Device: High-End ✔ / Max: 8K / 240 FPS`).
 * 2. Video Preview Card with center play overlay, bottom seek bar (`00:00 / 00:30`), resolution badge (`1080p`),
 *    aspect ratio badge (`9:16`), and fullscreen toggle.
 * 3. Resolution Card (`480p`, `720p`, `1080p ✓`, `2K`, `4K`, `6K`, `8K`).
 * 4. Frame Rate (FPS) Card (`24 FPS`, `25 FPS`, `30 FPS ✓`, `48 FPS`, `50 FPS`, `60 FPS`, `120 FPS`, `240 FPS`).
 * 5. Split Video Format (`MP4 ✓`, `MOV`) & Video Codec (`H.264 ✓`, `H.265`) Card.
 * 6. Video Quality Card (`LOW`, `MEDIUM`, `HIGH ✓`).
 * 7. Video Bitrate Card (`5 Mbps` .. `100 Mbps`, `-` / `+` buttons, `25.0 Mbps` badge).
 * 8. Estimated File Size Banner (`245 MB` live-calculated).
 * 9. Audio Settings Card (`Audio Codec: AAC ⌄`, `Audio Bitrate: 128 kbps, 192 kbps, 256 kbps ✓, 320 kbps`).
 * 10. Export Summary Card (2-column summary table).
 * 11. Bottom Gradient `EXPORT VIDEO` button with Section 19 auth cap (`720p` unsigned max / `8K` authenticated),
 *     progress, cancel, retry, and output validation.
 */
@Composable
fun ExportVideoScreen(
  viewModel: ExportVideoViewModel,
  isSignedIn: Boolean,
  onBackToEditor: () -> Unit,
  modifier: Modifier = Modifier
) {
  val project by viewModel.project.collectAsStateWithLifecycle()
  val selectedResolution by viewModel.selectedResolution.collectAsStateWithLifecycle()
  val selectedFps by viewModel.selectedFps.collectAsStateWithLifecycle()
  val selectedFormat by viewModel.selectedFormat.collectAsStateWithLifecycle()
  val selectedVideoCodec by viewModel.selectedVideoCodec.collectAsStateWithLifecycle()
  val selectedQuality by viewModel.selectedQuality.collectAsStateWithLifecycle()
  val videoBitrateMbps by viewModel.videoBitrateMbps.collectAsStateWithLifecycle()
  val selectedAudioCodec by viewModel.selectedAudioCodec.collectAsStateWithLifecycle()
  val selectedAudioBitrate by viewModel.selectedAudioBitrate.collectAsStateWithLifecycle()
  val estimatedSizeMb by viewModel.estimatedSizeMb.collectAsStateWithLifecycle()
  val previewPositionMicros by viewModel.previewPositionMicros.collectAsStateWithLifecycle()
  val isPreviewPlaying by viewModel.isPreviewPlaying.collectAsStateWithLifecycle()
  val exportJobState by viewModel.exportJobState.collectAsStateWithLifecycle()

  val scrollState = rememberScrollState()

  BackHandler {
    viewModel.pausePreview()
    onBackToEditor()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(ExportBgDeep)
      .testTag("export_video_screen"),
    containerColor = ExportBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(ExportBgDeep)
    ) {
      // 1. Top Header Bar
      ExportTopHeaderBar(
        deviceCapability = viewModel.deviceCapability,
        onBackClick = {
          viewModel.pausePreview()
          onBackToEditor()
        }
      )

      // Scrollable Export Configuration Body
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(scrollState)
          .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // 2. Video Preview Card
        ExportVideoPreviewCard(
          positionMicros = previewPositionMicros,
          durationMicros = viewModel.effectiveDurationMicros,
          isPlaying = isPreviewPlaying,
          resolutionLabel = selectedResolution.chipLabel,
          aspectRatioLabel = project.aspectRatio,
          onTogglePlayPause = { viewModel.togglePreviewPlayPause() },
          onSeekFraction = { frac -> viewModel.seekPreviewFraction(frac) },
          onToggleFullscreen = { viewModel.togglePreviewFullscreen() }
        )

        // 3. Resolution Card
        ExportResolutionSectionCard(
          selectedResolution = selectedResolution,
          onSelectResolution = { res -> viewModel.selectResolution(res) }
        )

        // 4. Frame Rate (FPS) Card
        ExportFpsSectionCard(
          selectedFps = selectedFps,
          onSelectFps = { fps -> viewModel.selectFps(fps) }
        )

        // 5. Video Format & Video Codec Split Card
        ExportFormatAndCodecCard(
          selectedFormat = selectedFormat,
          selectedCodec = selectedVideoCodec,
          onSelectFormat = { fmt -> viewModel.selectFormat(fmt) },
          onSelectCodec = { codec -> viewModel.selectVideoCodec(codec) }
        )

        // 6. Video Quality Card
        ExportVideoQualityCard(
          selectedQuality = selectedQuality,
          onSelectQuality = { quality -> viewModel.selectQuality(quality) }
        )

        // 7. Video Bitrate Card
        ExportVideoBitrateCard(
          bitrateMbps = videoBitrateMbps,
          onBitrateChange = { mbps -> viewModel.setVideoBitrateMbps(mbps) },
          onDecrement = { viewModel.adjustVideoBitrateBy(-1.0f) },
          onIncrement = { viewModel.adjustVideoBitrateBy(1.0f) }
        )

        // 8. Estimated File Size Banner Card
        ExportEstimatedFileSizeCard(
          estimatedSizeMb = estimatedSizeMb
        )

        // 9. Audio Settings Card
        ExportAudioSettingsCard(
          selectedAudioCodec = selectedAudioCodec,
          selectedAudioBitrate = selectedAudioBitrate,
          onSelectAudioBitrate = { ab -> viewModel.selectAudioBitrate(ab) }
        )

        // 10. Export Summary Card
        ExportSummaryTableCard(
          resolution = selectedResolution,
          fps = selectedFps,
          format = selectedFormat,
          videoCodec = selectedVideoCodec,
          quality = selectedQuality,
          videoBitrateMbps = videoBitrateMbps,
          audioCodec = selectedAudioCodec,
          audioBitrate = selectedAudioBitrate,
          estimatedSizeMb = estimatedSizeMb
        )

        // 11. Primary Gradient CTA Button (`EXPORT VIDEO`)
        Spacer(modifier = Modifier.height(2.dp))
        ExportPrimaryGradientButton(
          onClick = { viewModel.startExport(isSignedIn = isSignedIn) }
        )
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }

  // Section 19 Dialogs: Auth Gate for >720p, Exporting Progress, Completed, Failed, Cancelled
  when (val state = exportJobState) {
    is ExportJobState.Idle -> Unit

    is ExportJobState.RequiresAuthForHighRes -> {
      AlertDialog(
        onDismissRequest = { viewModel.dismissExportDialog() },
        containerColor = Color(0xFF171726),
        title = {
          Text(
            text = "Sign In Required for ${state.requestedResolution.chipLabel}",
            color = ExportTextPrimary,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column(
            modifier = Modifier.testTag("export_auth_required_dialog"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Unsigned sessions support up to 720p export. To export in ${state.requestedResolution.chipLabel} (up to 8K), please sign in with Google on the Home Dashboard, or export immediately at 720p.",
              color = ExportTextSecondary,
              fontSize = 13.sp
            )
          }
        },
        confirmButton = {
          Button(
            onClick = { viewModel.downgradeTo720pAndExport() },
            colors = ButtonDefaults.buttonColors(containerColor = ExportCyanHighlight),
            modifier = Modifier.testTag("export_downgrade_720p_button")
          ) {
            Text("Export at 720p", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          OutlinedButton(
            onClick = { viewModel.dismissExportDialog() },
            modifier = Modifier.testTag("export_auth_dialog_cancel_button")
          ) {
            Text("Close", color = ExportTextPrimary)
          }
        }
      )
    }

    is ExportJobState.Exporting -> {
      AlertDialog(
        onDismissRequest = { /* Prevent accidental dismiss while exporting */ },
        containerColor = Color(0xFF171726),
        title = {
          Text(
            text = "Exporting Video (${state.progressPercent}%)",
            color = ExportTextPrimary,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column(
            modifier = Modifier.testTag("export_progress_dialog"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            LinearProgressIndicator(
              progress = { (state.progressPercent / 100f).coerceIn(0f, 1f) },
              modifier = Modifier.fillMaxWidth(),
              color = ExportCyanHighlight,
              trackColor = Color(0xFF2E2E46)
            )
            Text(
              text = state.stageLabel,
              color = ExportTextSecondary,
              fontSize = 12.5.sp
            )
            if (!state.frameDuplicationNote.isNullOrBlank()) {
              Text(
                text = state.frameDuplicationNote,
                color = Color(0xFFB4B6D8),
                fontSize = 11.sp
              )
            }
          }
        },
        confirmButton = {
          OutlinedButton(
            onClick = { viewModel.cancelExport() },
            modifier = Modifier.testTag("export_cancel_job_button")
          ) {
            Text("Cancel Export", color = Color(0xFFFF6E6E), fontWeight = FontWeight.Bold)
          }
        }
      )
    }

    is ExportJobState.Completed -> {
      val rec = state.record
      AlertDialog(
        onDismissRequest = { viewModel.dismissExportDialog() },
        containerColor = Color(0xFF171726),
        title = {
          Text(
            text = "Export Complete ✔",
            color = ExportGreenBadge,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column(
            modifier = Modifier.testTag("export_completed_dialog"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "Output validated and saved:",
              color = ExportTextPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "• File: ${rec.outputFilePath}",
              color = ExportCyanHighlight,
              fontSize = 12.sp
            )
            Text(
              text = "• Dimensions: ${rec.widthPx} × ${rec.heightPx} (${rec.resolution.chipLabel}) @ ${rec.fps} FPS",
              color = ExportTextSecondary,
              fontSize = 12.sp
            )
            Text(
              text = "• Codec & Container: ${rec.videoCodec.label} + ${rec.audioCodec.label} (${rec.audioBitrate.label}) in ${rec.format.label}",
              color = ExportTextSecondary,
              fontSize = 12.sp
            )
            Text(
              text = "• Validated Frame Count: ${rec.totalFrameCount} frames • Est. Size: ${rec.estimatedSizeMb} MB",
              color = ExportTextSecondary,
              fontSize = 12.sp
            )
            if (!rec.frameRatePolicyNote.isNullOrBlank()) {
              Text(
                text = "• Frame Policy: ${rec.frameRatePolicyNote}",
                color = Color(0xFFB4B6D8),
                fontSize = 11.sp
              )
            }
          }
        },
        confirmButton = {
          Button(
            onClick = { viewModel.dismissExportDialog() },
            colors = ButtonDefaults.buttonColors(containerColor = ExportCyanHighlight),
            modifier = Modifier.testTag("export_completed_done_button")
          ) {
            Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      )
    }

    is ExportJobState.Failed -> {
      AlertDialog(
        onDismissRequest = { viewModel.dismissExportDialog() },
        containerColor = Color(0xFF171726),
        title = {
          Text(
            text = "Export Failed",
            color = Color(0xFFFF6E6E),
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column(
            modifier = Modifier.testTag("export_failed_dialog"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = state.message,
              color = ExportTextSecondary,
              fontSize = 13.sp
            )
          }
        },
        confirmButton = {
          if (state.canRetry) {
            Button(
              onClick = { viewModel.retryExport(isSignedIn = isSignedIn) },
              colors = ButtonDefaults.buttonColors(containerColor = ExportCyanHighlight),
              modifier = Modifier.testTag("export_retry_button")
            ) {
              Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        },
        dismissButton = {
          OutlinedButton(
            onClick = { viewModel.dismissExportDialog() },
            modifier = Modifier.testTag("export_failed_close_button")
          ) {
            Text("Close", color = ExportTextPrimary)
          }
        }
      )
    }

    is ExportJobState.Cancelled -> {
      AlertDialog(
        onDismissRequest = { viewModel.dismissExportDialog() },
        containerColor = Color(0xFF171726),
        title = {
          Text(
            text = "Export Cancelled",
            color = ExportTextPrimary,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column(
            modifier = Modifier.testTag("export_cancelled_dialog"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "The export operation was cancelled safely and temporary buffers were cleaned up.",
              color = ExportTextSecondary,
              fontSize = 12.5.sp
            )
          }
        },
        confirmButton = {
          Button(
            onClick = { viewModel.retryExport(isSignedIn = isSignedIn) },
            colors = ButtonDefaults.buttonColors(containerColor = ExportCyanHighlight),
            modifier = Modifier.testTag("export_cancelled_retry_button")
          ) {
            Text("Retry Export", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          OutlinedButton(
            onClick = { viewModel.dismissExportDialog() },
            modifier = Modifier.testTag("export_cancelled_close_button")
          ) {
            Text("Close", color = ExportTextPrimary)
          }
        }
      )
    }
  }
}
