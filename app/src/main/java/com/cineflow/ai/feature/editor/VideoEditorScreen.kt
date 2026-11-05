package com.cineflow.ai.feature.editor

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.core.model.EditorMainCategory
import com.cineflow.ai.core.model.ExportResolutionCap
import com.cineflow.ai.core.model.TrackType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val ROUTE_VIDEO_EDITOR = "video_editor"

/**
 * Video Editor Workspace Screen (`17_Video_Editor_Dashboard.jpg` &
 * `Screenshot_2026-11-03-22-02-04-985_com.lemon.lvoverseas.jpg`).
 *
 * Implements:
 * 1. Top Toolbar with Close (`✕`) + CineFlow AI Logo (replacing `?` and flame icons),
 *    Resolution selector (`720P ▾`), and Cyan `Export` button.
 * 2. Aspect-ratio accurate Preview Canvas supporting `16:9` Landscape, `9:16` Portrait,
 *    and all 14 approved presets using Fit/Contain math — with ZERO demo videos.
 * 3. Playback & Transport Bar (`Fullscreen`, `Play/Pause`, `Clip Confinement ON/OFF`, `Undo`, `Redo`).
 * 4. Universal Multitrack Timeline (`00:00 / 00:00` time readout, ruler ticks, `Mute clip`,
 *    `Cover`, center fixed white Playhead line, white square `+` media import button,
 *    white clip trim handles, magnetic `+` transition control, and `+ Add audio` track).
 * 5. Contextual Tools Strip + Persistent Main Category Navigation Bar (`Edit`, `Audio`, `Text`,
 *    `Overlay`, `Effects`, `Captions`, `Aspect ratio`, `Filters`, `Transitions`, `Stickers`,
 *    `AI Tools`, `More`).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoEditorScreen(
  viewModel: VideoEditorViewModel,
  isSignedIn: Boolean,
  onBackToProjects: () -> Unit,
  onNavigateToExport: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val project by viewModel.project.collectAsStateWithLifecycle()
  val clips by viewModel.clips.collectAsStateWithLifecycle()
  val selectedClipId by viewModel.selectedClipId.collectAsStateWithLifecycle()
  val activeCategory by viewModel.activeCategory.collectAsStateWithLifecycle()
  val playheadMicros by viewModel.playheadMicros.collectAsStateWithLifecycle()
  val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
  val isClipConfinementOn by viewModel.isClipConfinementOn.collectAsStateWithLifecycle()
  val isMainTrackMuted by viewModel.isMainTrackMuted.collectAsStateWithLifecycle()
  val isFullscreenPreview by viewModel.isFullscreenPreview.collectAsStateWithLifecycle()
  val timelineZoomScale by viewModel.timelineZoomScale.collectAsStateWithLifecycle()
  val coverUri by viewModel.coverUri.collectAsStateWithLifecycle()
  val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
  val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
  val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()

  var showResolutionDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  var showAddTextDialog by remember { mutableStateOf(false) }
  var showAddAudioDialog by remember { mutableStateOf(false) }

  BackHandler {
    if (isFullscreenPreview) {
      viewModel.toggleFullscreenPreview()
    } else if (activeCategory != null) {
      viewModel.selectMainCategory(null)
    } else {
      viewModel.pausePlayback()
      onBackToProjects()
    }
  }

  LaunchedEffect(statusMessage) {
    val msg = statusMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearStatusMessage()
    }
  }

  // Zero-permission Android Photo Picker for importing Main Video/Photo clips (Play Policy compliant)
  // Automatically probes video/image dimensions & rotation to detect 9:16 vs 16:9 aspect ratio.
  val pickMainMediaLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      coroutineScope.launch {
        val probedItems = withContext(Dispatchers.IO) {
          uris.mapIndexed { idx, uri ->
            runCatching {
              context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
              )
            }
            val displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Clip ${idx + 1}"
            probeMediaClipSpec(context, uri, displayName)
          }
        }
        viewModel.importMainVideoClipsWithDimensions(probedItems)
      }
    }
  }

  // Zero-permission Android Photo Picker for Cover Thumbnail
  val pickCoverLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      runCatching {
        context.contentResolver.takePersistableUriPermission(
          uri,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
      }
      viewModel.setCoverUri(uri.toString())
    }
  }

  // Zero-permission Android Photo Picker for Overlay Media
  val pickOverlayLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      runCatching {
        context.contentResolver.takePersistableUriPermission(
          uri,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
      }
      val name = uri.lastPathSegment?.substringAfterLast('/') ?: "Overlay"
      viewModel.addOverlayMediaClip(uri.toString(), name)
    }
  }

  // Determine active main video clip & active overlays at current `playheadMicros`
  val activeVideoClip = remember(clips, playheadMicros) {
    val mainClips = clips.filter { it.trackType == TrackType.MAIN_VIDEO }
    mainClips.firstOrNull {
      playheadMicros >= it.timelineStartMicros && playheadMicros <= it.timelineEndMicros
    } ?: mainClips.firstOrNull()
  }

  val activeOverlays = remember(clips, playheadMicros) {
    clips.filter {
      (it.trackType == TrackType.TEXT || it.trackType == TrackType.OVERLAY || it.trackType == TrackType.STICKER) &&
        playheadMicros >= it.timelineStartMicros && playheadMicros <= it.timelineEndMicros
    }
  }

  val totalDurationMicros = remember(clips) {
    clips.maxOfOrNull { it.timelineEndMicros }?.coerceAtLeast(0L) ?: 0L
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(EditorBgBlack)
      .testTag("video_editor_screen"),
    containerColor = EditorBgBlack,
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(EditorBgBlack)
    ) {
      // 1. Top Toolbar (Close `✕` + CineFlow AI Logo on left, `720P ▾` + Cyan `Export` on right)
      if (!isFullscreenPreview) {
        EditorTopToolbar(
          resolutionLabel = if (!isSignedIn && project.resolutionLabel != "480p") {
            "720P"
          } else {
            project.resolutionLabel.uppercase()
          },
          onCloseEditor = {
            viewModel.pausePlayback()
            onBackToProjects()
          },
          onOpenResolutionSheet = { showResolutionDialog = true },
          onOpenExportSheet = {
            viewModel.pausePlayback()
            if (onNavigateToExport != null) {
              onNavigateToExport()
            } else {
              showExportDialog = true
            }
          }
        )
      }

      // 2. Central Preview Stage (Upper half between Top Header and Play-Pause bar; 9:16 fills 100% height, 16:9 fills 100% width)
      EditorPreviewStage(
        aspectRatio = project.aspectRatio,
        activeVideoClip = activeVideoClip,
        activeOverlays = activeOverlays,
        selectedClipId = selectedClipId,
        playheadMicros = playheadMicros,
        onSelectOverlay = { clipId -> viewModel.selectClip(clipId) },
        onMoveOverlay = { clipId, normX, normY ->
          viewModel.updateOverlayPosition(clipId, normX, normY)
        },
        modifier = Modifier.weight(if (isFullscreenPreview) 1f else 0.46f)
      )

      // 3. Playback & Transport Control Row (Sits right at the vertical middle of the screen)
      EditorTransportBar(
        isPlaying = isPlaying,
        isFullscreen = isFullscreenPreview,
        isClipConfinementOn = isClipConfinementOn,
        canUndo = canUndo,
        canRedo = canRedo,
        onToggleFullscreen = { viewModel.toggleFullscreenPreview() },
        onTogglePlayPause = { viewModel.togglePlayPause() },
        onToggleClipConfinement = { viewModel.toggleClipConfinement() },
        onUndo = { viewModel.undo() },
        onRedo = { viewModel.redo() }
      )

      if (!isFullscreenPreview) {
        // 4. Universal Multitrack Timeline Workspace (Middle-to-lower half of the screen)
        EditorTimelineWorkspace(
          clips = clips,
          selectedClipId = selectedClipId,
          playheadMicros = playheadMicros,
          totalDurationMicros = totalDurationMicros,
          isMainTrackMuted = isMainTrackMuted,
          coverUri = coverUri,
          zoomScale = timelineZoomScale,
          onZoomChange = { scale -> viewModel.setTimelineZoom(scale) },
          onSeekToMicros = { target -> viewModel.seekToMicros(target) },
          onSelectClip = { id -> viewModel.selectClip(id) },
          onToggleMuteClip = { viewModel.toggleMuteClipOrTrack() },
          onPickCoverClick = {
            pickCoverLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          onAddMainClipClick = {
            pickMainMediaLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
          },
          onAddAudioClick = { showAddAudioDialog = true },
          onInsertTransitionClick = { clipId ->
            viewModel.setTransitionAfterClip(clipId, "Crossfade")
          },
          modifier = Modifier.weight(0.54f)
        )

        // 5. Contextual Tools Strip (Separate horizontal panel when a category is selected)
        val currentCategory = activeCategory
        if (currentCategory != null) {
          EditorContextualToolsStrip(
            activeCategory = currentCategory,
            currentAspectRatio = project.aspectRatio,
            hasSelectedClip = selectedClipId != null,
            onSelectAspectRatio = { ratio -> viewModel.setAspectRatio(ratio) },
            onImportVideoOrPhoto = {
              pickMainMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
              )
            },
            onSplitClip = { viewModel.splitClipAtPlayhead() },
            onTrimLeftMinusHalfSec = { viewModel.trimSelectedClip(deltaSourceInMicros = 500_000L) },
            onTrimRightMinusHalfSec = { viewModel.trimSelectedClip(deltaSourceOutMicros = -500_000L) },
            onDuplicateClip = { viewModel.duplicateSelectedClip() },
            onDeleteClip = { viewModel.deleteSelectedClip() },
            onSpeedChange = { speed -> viewModel.setSelectedClipSpeed(speed) },
            onRotateClip = { viewModel.rotateSelectedClip90() },
            onAddTextClick = { showAddTextDialog = true },
            onAddAudioClick = { showAddAudioDialog = true },
            onAddOverlayClick = {
              pickOverlayLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            onCloseContextualStrip = { viewModel.selectMainCategory(null) }
          )
        }

        // 6. Persistent Main Category Navigation Bar at Bottom
        EditorMainCategoryBar(
          activeCategory = activeCategory,
          onSelectCategory = { category ->
            viewModel.selectMainCategory(category)
          }
        )
      }
    }
  }

  // Resolution & FPS Configuration Dialog (Section 19: Unsigned capped at 720p, Signed-in up to 8K)
  if (showResolutionDialog) {
    var selectedFps by remember(project.displayFps) { mutableIntStateOf(project.displayFps) }
    val fpsPresets = listOf(25, 30, 48, 50, 60, 120, 240)

    AlertDialog(
      onDismissRequest = { showResolutionDialog = false },
      containerColor = Color(0xFF1A1A1E),
      title = {
        Text(
          text = "Resolution & Frame Rate",
          color = EditorTextWhite,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier.testTag("editor_resolution_dialog"),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = if (isSignedIn) {
              "Signed-in session: Up to 8K export enabled (subject to device codec capability)."
            } else {
              "Unsigned session: Maximum 720p export. Sign in with Google on Home Dashboard to unlock 1080p–8K."
            },
            color = EditorTextMuted,
            fontSize = 12.sp
          )

          Text(
            text = "Resolution",
            color = EditorTextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ExportResolutionCap.entries.forEach { cap ->
              val allowed = ExportResolutionCap.isAllowedForSession(cap, isSignedIn)
              val isCurrent = project.resolutionLabel.startsWith(cap.label.substringBefore(" "), ignoreCase = true)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(
                    when {
                      isCurrent -> EditorExportCyan
                      allowed -> EditorPillDark
                      else -> Color(0xFF141417)
                    }
                  )
                  .border(
                    width = 1.dp,
                    color = if (isCurrent) EditorExportCyan else Color(0xFF34343C),
                    shape = RoundedCornerShape(6.dp)
                  )
                  .clickable {
                    val ok = viewModel.setResolutionAndFps(cap, selectedFps, isSignedIn)
                    if (ok) showResolutionDialog = false
                  }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
                  .testTag("res_option_${cap.name}"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (allowed) cap.label else "${cap.label} 🔒",
                  color = when {
                    isCurrent -> Color(0xFF061618)
                    allowed -> EditorTextWhite
                    else -> Color(0xFF66666E)
                  },
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Frame Rate (FPS)",
            color = EditorTextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            fpsPresets.forEach { fps ->
              val active = selectedFps == fps
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (active) EditorExportCyan else EditorPillDark)
                  .clickable { selectedFps = fps }
                  .padding(horizontal = 10.dp, vertical = 5.dp)
                  .testTag("fps_option_$fps")
              ) {
                Text(
                  text = "$fps FPS",
                  color = if (active) Color(0xFF061618) else EditorTextWhite,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Text(
          text = "Close",
          color = EditorExportCyan,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .clickable { showResolutionDialog = false }
            .padding(8.dp)
            .testTag("editor_resolution_close_button")
        )
      }
    )
  }

  // Add Text Layer Dialog
  if (showAddTextDialog) {
    var textValue by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showAddTextDialog = false },
      containerColor = Color(0xFF1A1A1E),
      title = {
        Text("Add Text Overlay", color = EditorTextWhite, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(modifier = Modifier.testTag("editor_add_text_dialog")) {
          OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it.take(80) },
            placeholder = { Text("Enter overlay text", color = EditorTextMuted) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("editor_text_overlay_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (textValue.isNotBlank()) {
              viewModel.addTextOverlay(textValue)
              showAddTextDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EditorExportCyan),
          modifier = Modifier.testTag("editor_text_overlay_confirm")
        ) {
          Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAddTextDialog = false }) {
          Text("Cancel", color = EditorTextWhite)
        }
      }
    )
  }

  // Add Audio Track Dialog
  if (showAddAudioDialog) {
    var audioLabel by remember { mutableStateOf("Ambient Track") }
    AlertDialog(
      onDismissRequest = { showAddAudioDialog = false },
      containerColor = Color(0xFF1A1A1E),
      title = {
        Text("Add Audio Track", color = EditorTextWhite, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(
          modifier = Modifier.testTag("editor_add_audio_dialog"),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Add an audio track to the universal multitrack timeline at the current playhead.",
            color = EditorTextMuted,
            fontSize = 12.sp
          )
          OutlinedTextField(
            value = audioLabel,
            onValueChange = { audioLabel = it.take(50) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("editor_audio_label_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (audioLabel.isNotBlank()) {
              viewModel.addAudioClip(audioLabel.trim())
              showAddAudioDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EditorExportCyan),
          modifier = Modifier.testTag("editor_audio_confirm_button")
        ) {
          Text("Add Audio", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAddAudioDialog = false }) {
          Text("Cancel", color = EditorTextWhite)
        }
      }
    )
  }

  // Export Summary & Validation Dialog (Section 19)
  if (showExportDialog) {
    val activeRes = if (!isSignedIn && project.resolutionLabel != "480p") "720p" else project.resolutionLabel
    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      containerColor = Color(0xFF1A1A1E),
      title = {
        Text("Export Project", color = EditorTextWhite, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(
          modifier = Modifier.testTag("editor_export_dialog"),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text("Project: ${project.name}", color = EditorTextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          Text("Aspect Ratio: ${project.aspectRatio}", color = EditorExportCyan, fontSize = 12.5.sp)
          Text("Target Resolution: $activeRes • ${project.displayFps} FPS (MP4 / H.264 + AAC)", color = EditorTextMuted, fontSize = 12.sp)
          Text(
            text = "Timeline Duration: ${VideoEditorViewModel.formatTimeMmSs(totalDurationMicros)} (${clips.size} item(s))",
            color = EditorTextMuted,
            fontSize = 12.sp
          )
          if (clips.isEmpty()) {
            Text(
              text = "Timeline is currently empty. Import at least one video or photo clip (+) before exporting.",
              color = Color(0xFFFF6B6B),
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showExportDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = EditorExportCyan),
          modifier = Modifier.testTag("editor_export_close_button")
        ) {
          Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

private fun probeMediaClipSpec(
  context: android.content.Context,
  uri: Uri,
  displayName: String
): ProbedMediaClip {
  val retriever = MediaMetadataRetriever()
  return try {
    retriever.setDataSource(context, uri)
    val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
    val rawWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
    val rawHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
    val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0

    val (orientedW, orientedH) = if (rawWidth > 0 && rawHeight > 0) {
      if (rotation == 90 || rotation == 270) {
        rawHeight to rawWidth
      } else {
        rawWidth to rawHeight
      }
    } else {
      // Fallback for still image URIs
      val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
      context.contentResolver.openInputStream(uri)?.use { stream ->
        android.graphics.BitmapFactory.decodeStream(stream, null, opts)
      }
      val imgW = if (opts.outWidth > 0) opts.outWidth else 1920
      val imgH = if (opts.outHeight > 0) opts.outHeight else 1080
      imgW to imgH
    }

    val durationMicros = if (durationMs != null && durationMs > 0L) {
      durationMs * 1_000L
    } else {
      5_000_000L
    }

    ProbedMediaClip(
      uri = uri.toString(),
      label = displayName,
      durationMicros = durationMicros,
      widthPx = orientedW,
      heightPx = orientedH
    )
  } catch (_: Throwable) {
    ProbedMediaClip(
      uri = uri.toString(),
      label = displayName,
      durationMicros = 5_000_000L,
      widthPx = 1920,
      heightPx = 1080
    )
  } finally {
    runCatching { retriever.release() }
  }
}

