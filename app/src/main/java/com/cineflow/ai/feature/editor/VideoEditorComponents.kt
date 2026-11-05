package com.cineflow.ai.feature.editor

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Rotate90DegreesCcw
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cineflow.ai.R
import com.cineflow.ai.core.model.EditorMainCategory
import com.cineflow.ai.core.model.TimelineClipItem
import com.cineflow.ai.core.model.TrackType
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val EditorBgBlack = Color(0xFF0E0E0E)
val EditorSurfaceDark = Color(0xFF18181B)
val EditorPillDark = Color(0xFF232326)
val EditorExportCyan = Color(0xFF16DCE0)
val EditorTextWhite = Color(0xFFF5F5F7)
val EditorTextMuted = Color(0xFF8E8E93)

/**
 * Top Toolbar matching `17_Video_Editor_Dashboard.jpg`:
 * - Left: Close (`✕`) button + CineFlow AI Logo (replacing `?` and flame icons).
 * - Right: Resolution dropdown pill (`720P ▾`) + Cyan `Export` button.
 */
@Composable
fun EditorTopToolbar(
  resolutionLabel: String,
  onCloseEditor: () -> Unit,
  onOpenResolutionSheet: () -> Unit,
  onOpenExportSheet: () -> Unit,
  modifier: Modifier = Modifier
) {
  val closeDesc = stringResource(R.string.ve_cd_close)
  val logoDesc = stringResource(R.string.ve_cd_brand_logo)
  val resDesc = stringResource(R.string.ve_cd_resolution)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(48.dp)
      .background(EditorBgBlack)
      .padding(horizontal = 12.dp)
      .testTag("editor_top_toolbar"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .clickable(onClick = onCloseEditor)
          .semantics { contentDescription = closeDesc }
          .testTag("editor_close_button"),
        contentAlignment = Alignment.CenterStart
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = closeDesc,
          tint = EditorTextWhite,
          modifier = Modifier.size(25.dp)
        )
      }

      Row(
        modifier = Modifier
          .padding(start = 2.dp)
          .semantics { contentDescription = logoDesc }
          .testTag("editor_brand_logo"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_editor_cineflow_logo_mark),
          contentDescription = logoDesc,
          contentScale = ContentScale.Fit,
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = buildAnnotatedString {
            withStyle(
              SpanStyle(
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
              )
            ) {
              append("CineFlow ")
            }
            withStyle(
              SpanStyle(
                brush = Brush.horizontalGradient(
                  listOf(Color(0xFFA855F7), Color(0xFF38BDF8))
                ),
                fontWeight = FontWeight.ExtraBold
              )
            ) {
              append("AI")
            }
          },
          fontSize = 16.5.sp
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier
          .height(32.dp)
          .clip(RoundedCornerShape(7.dp))
          .background(EditorPillDark)
          .clickable(onClick = onOpenResolutionSheet)
          .padding(horizontal = 11.dp)
          .semantics { contentDescription = resDesc }
          .testTag("editor_resolution_button"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = resolutionLabel.uppercase(),
          color = EditorTextWhite,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(3.dp))
        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = null,
          tint = EditorTextWhite,
          modifier = Modifier.size(16.dp)
        )
      }

      Box(
        modifier = Modifier
          .height(32.dp)
          .clip(RoundedCornerShape(7.dp))
          .background(EditorExportCyan)
          .clickable(onClick = onOpenExportSheet)
          .padding(horizontal = 15.dp)
          .testTag("editor_export_button"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = stringResource(R.string.ve_action_export),
          color = Color(0xFF061618),
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

/**
 * Preview Canvas Stage positioned directly between the Top Header and the Play-Pause Transport Bar.
 *
 * - In `9:16` Portrait mode (`Screenshot_2026-11-03-22-02-04-985_com.lemon.lvoverseas.jpg`),
 *   the preview canvas fills the ENTIRE vertical height from right below the Top Header to right
 *   above the Play-Pause bar (`height = stageHeight`, `width = stageHeight * 9/16`) with zero vertical gap.
 * - In `16:9` Landscape mode (`17_Video_Editor_Dashboard.jpg`, and default empty mode),
 *   the preview canvas spans the full horizontal width (`width = stageWidth`, `height = stageWidth * 9/16`)
 *   within that exact same upper-half stage so the Play-Pause bar and Timeline remain in the middle of the screen.
 */
@Composable
fun EditorPreviewStage(
  aspectRatio: String,
  activeVideoClip: TimelineClipItem?,
  activeOverlays: List<TimelineClipItem>,
  selectedClipId: String?,
  playheadMicros: Long,
  onSelectOverlay: (String) -> Unit,
  onMoveOverlay: (String, Float, Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val targetRatio = VideoEditorViewModel.parseAspectRatioValue(aspectRatio)
  val density = LocalDensity.current

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .background(EditorBgBlack)
      .testTag("editor_preview_stage"),
    contentAlignment = Alignment.Center
  ) {
    val stageWidth = maxWidth
    val stageHeight = maxHeight
    val stageRatio = if (stageHeight.value > 0f) stageWidth.value / stageHeight.value else 1f

    val (canvasWidth, canvasHeight) = if (targetRatio >= stageRatio) {
      // 16:9 Landscape / Wide: spans full screen width inside the upper stage
      val w = stageWidth
      val h = (stageWidth.value / targetRatio).dp
      w to h
    } else {
      // 9:16 Portrait / Tall: stretches from the bottom of the Top Header all the way to the top of Play-Pause
      val h = stageHeight
      val w = (stageHeight.value * targetRatio).dp
      w to h
    }

    Box(
      modifier = Modifier
        .size(width = canvasWidth, height = canvasHeight)
        .background(Color(0xFF141417))
        .testTag("editor_preview_canvas_$aspectRatio"),
      contentAlignment = Alignment.Center
    ) {
      if (activeVideoClip != null) {
        val demoDrawableRes = resolveDemoDrawableRes(activeVideoClip.mediaUri)
        val decodedFrame = if (demoDrawableRes == null) {
          rememberMediaFrameBitmap(
            uriString = activeVideoClip.mediaUri,
            offsetMicros = (playheadMicros - activeVideoClip.timelineStartMicros)
              .coerceAtLeast(0L) + activeVideoClip.sourceInMicros
          )
        } else {
          null
        }

        Box(
          modifier = Modifier
            .fillMaxSize()
            .rotate(activeVideoClip.rotationDegrees.toFloat())
            .background(Color(0xFF141417)),
          contentAlignment = Alignment.Center
        ) {
          when {
            demoDrawableRes != null -> {
              Image(
                painter = painterResource(id = demoDrawableRes),
                contentDescription = activeVideoClip.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            }
            decodedFrame != null -> {
              Image(
                bitmap = decodedFrame,
                contentDescription = activeVideoClip.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
              )
            }
          }
        }
      } else {
        // Default empty 16:9 horizontal preview surface (completely clean, no buttons or badges)
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141417))
            .border(0.8.dp, Color(0xFF232329))
            .testTag("editor_empty_preview_state")
        )
      }

      // Floating Overlay & Text Items
      val canvasWidthPx = with(density) { canvasWidth.toPx().coerceAtLeast(1f) }
      val canvasHeightPx = with(density) { canvasHeight.toPx().coerceAtLeast(1f) }

      activeOverlays.forEach { overlay ->
        val isSelected = overlay.clipId == selectedClipId
        val crossesBoundary = overlay.overlayNormalizedX < 0.05f ||
          overlay.overlayNormalizedX > 0.95f ||
          overlay.overlayNormalizedY < 0.05f ||
          overlay.overlayNormalizedY > 0.95f

        val offsetX = ((overlay.overlayNormalizedX - 0.5f) * canvasWidthPx).roundToInt()
        val offsetY = ((overlay.overlayNormalizedY - 0.5f) * canvasHeightPx).roundToInt()

        Box(
          modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .clip(RoundedCornerShape(6.dp))
            .background(
              if (overlay.trackType == TrackType.TEXT) Color(0x99000000) else Color(0xCC1E1E28)
            )
            .then(
              if (crossesBoundary) {
                Modifier.border(1.5.dp, Color(0xFFFF453A), RoundedCornerShape(6.dp))
              } else if (isSelected) {
                Modifier.border(1.5.dp, Color.White, RoundedCornerShape(6.dp))
              } else {
                Modifier
              }
            )
            .clickable { onSelectOverlay(overlay.clipId) }
            .pointerInput(overlay.clipId, canvasWidthPx, canvasHeightPx) {
              detectDragGestures { change, dragAmount ->
                change.consume()
                val nextNormX = overlay.overlayNormalizedX + (dragAmount.x / canvasWidthPx)
                val nextNormY = overlay.overlayNormalizedY + (dragAmount.y / canvasHeightPx)
                onMoveOverlay(overlay.clipId, nextNormX, nextNormY)
              }
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("editor_canvas_overlay_${overlay.clipId}")
        ) {
          Text(
            text = overlay.overlayText ?: overlay.label,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

/**
 * Playback & Transport Control Row positioned right in the middle of the screen
 * immediately below the Preview Stage (`17_Video_Editor_Dashboard.jpg`).
 */
@Composable
fun EditorTransportBar(
  isPlaying: Boolean,
  isFullscreen: Boolean,
  isClipConfinementOn: Boolean,
  canUndo: Boolean,
  canRedo: Boolean,
  onToggleFullscreen: () -> Unit,
  onTogglePlayPause: () -> Unit,
  onToggleClipConfinement: () -> Unit,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
  modifier: Modifier = Modifier
) {
  val fullscreenDesc = stringResource(R.string.ve_cd_fullscreen)
  val playPauseDesc = stringResource(R.string.ve_cd_play_pause)
  val confineDesc = stringResource(R.string.ve_cd_clip_confinement)
  val undoDesc = stringResource(R.string.ve_cd_undo)
  val redoDesc = stringResource(R.string.ve_cd_redo)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(42.dp)
      .background(EditorBgBlack)
      .padding(horizontal = 12.dp)
      .testTag("editor_transport_bar")
  ) {
    Box(
      modifier = Modifier
        .align(Alignment.CenterStart)
        .size(42.dp)
        .clip(CircleShape)
        .clickable(onClick = onToggleFullscreen)
        .semantics { contentDescription = fullscreenDesc }
        .testTag("editor_fullscreen_button"),
      contentAlignment = Alignment.CenterStart
    ) {
      Icon(
        imageVector = if (isFullscreen) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen,
        contentDescription = fullscreenDesc,
        tint = EditorTextWhite,
        modifier = Modifier.size(22.dp)
      )
    }

    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .size(42.dp)
        .clip(CircleShape)
        .clickable(onClick = onTogglePlayPause)
        .semantics { contentDescription = playPauseDesc }
        .testTag("editor_play_pause_button"),
      contentAlignment = Alignment.Center
    ) {
      if (isPlaying) {
        Icon(
          imageVector = Icons.Outlined.Pause,
          contentDescription = playPauseDesc,
          tint = EditorTextWhite,
          modifier = Modifier.size(25.dp)
        )
      } else {
        Canvas(modifier = Modifier.size(19.dp)) {
          val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.18f, size.height * 0.12f)
            lineTo(size.width * 0.88f, size.height * 0.5f)
            lineTo(size.width * 0.18f, size.height * 0.88f)
            close()
          }
          drawPath(
            path = path,
            color = EditorTextWhite,
            style = Stroke(width = 2.dp.toPx())
          )
        }
      }
    }

    Row(
      modifier = Modifier.align(Alignment.CenterEnd),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .clickable(onClick = onToggleClipConfinement)
          .semantics { contentDescription = confineDesc }
          .testTag("editor_confinement_toggle"),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Outlined.Layers,
            contentDescription = null,
            tint = if (isClipConfinementOn) EditorTextWhite else EditorTextMuted,
            modifier = Modifier.size(15.dp)
          )
          Text(
            text = if (isClipConfinementOn) "ON" else "OFF",
            color = if (isClipConfinementOn) EditorTextWhite else EditorTextMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 8.5.sp
          )
        }
      }

      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .clickable(enabled = canUndo, onClick = onUndo)
          .semantics { contentDescription = undoDesc }
          .testTag("editor_undo_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Outlined.Undo,
          contentDescription = undoDesc,
          tint = if (canUndo) EditorTextWhite else Color(0xFF48484C),
          modifier = Modifier.size(20.dp)
        )
      }

      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .clickable(enabled = canRedo, onClick = onRedo)
          .semantics { contentDescription = redoDesc }
          .testTag("editor_redo_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Outlined.Redo,
          contentDescription = redoDesc,
          tint = if (canRedo) EditorTextWhite else Color(0xFF48484C),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

/**
 * Persistent Main Category Navigation Bar at the bottom of the Video Editor
 * with both smooth horizontal touch scrolling AND an interactive synchronized
 * horizontal slider bar (`editor_nav_slider_track`).
 */
@Composable
fun EditorMainCategoryBar(
  activeCategory: EditorMainCategory?,
  onSelectCategory: (EditorMainCategory) -> Unit,
  modifier: Modifier = Modifier
) {
  val navScrollState = rememberScrollState()
  val coroutineScope = rememberCoroutineScope()
  val density = LocalDensity.current

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(EditorBgBlack)
      .padding(top = 4.dp, bottom = 6.dp)
      .testTag("editor_main_category_bar")
  ) {
    // 1. Horizontally Scrollable Category Icons Row (Edit, Audio, Text, Overlay, Effects, Captions, Aspect ratio, Filters, Transitions, Stickers, AI Tools, More)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .horizontalScroll(navScrollState)
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      EditorMainCategory.entries.forEach { category ->
        val isSelected = activeCategory == category
        val icon = iconForEditorCategory(category)
        Column(
          modifier = Modifier
            .width(64.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelectCategory(category) }
            .padding(vertical = 4.dp)
            .testTag("editor_category_${category.id}"),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = category.label,
            tint = if (isSelected) EditorExportCyan else EditorTextWhite,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = category.label,
            color = if (isSelected) EditorExportCyan else Color(0xFFD4D4D8),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 2. Synchronized Interactive Horizontal Scroll Slider Bar for the Bottom Navigation Bar
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .height(14.dp)
        .pointerInput(navScrollState.maxValue) {
          detectHorizontalDragGestures { change, dragAmount ->
            change.consume()
            val maxScroll = navScrollState.maxValue.coerceAtLeast(1)
            val trackPx = size.width.toFloat().coerceAtLeast(1f)
            val deltaScroll = (dragAmount / trackPx) * maxScroll * 1.35f
            coroutineScope.launch {
              navScrollState.scrollTo(
                (navScrollState.value + deltaScroll.roundToInt()).coerceIn(0, maxScroll)
              )
            }
          }
        }
        .testTag("editor_nav_slider_track"),
      contentAlignment = Alignment.CenterStart
    ) {
      val trackWidthDp = maxWidth
      val thumbWidthDp = (trackWidthDp * 0.32f).coerceAtLeast(48.dp)
      val maxThumbOffsetDp = (trackWidthDp - thumbWidthDp).coerceAtLeast(0.dp)
      val scrollFraction = if (navScrollState.maxValue > 0) {
        (navScrollState.value.toFloat() / navScrollState.maxValue.toFloat()).coerceIn(0f, 1f)
      } else {
        0f
      }
      val thumbOffsetPx = with(density) { (maxThumbOffsetDp * scrollFraction).roundToPx() }

      // Background slider rail
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(3.dp)
          .clip(RoundedCornerShape(50))
          .background(Color(0xFF26262B))
      )

      // Draggable slider thumb pill
      Box(
        modifier = Modifier
          .offset { IntOffset(thumbOffsetPx, 0) }
          .width(thumbWidthDp)
          .height(4.dp)
          .clip(RoundedCornerShape(50))
          .background(Color(0xFF787882))
          .testTag("editor_nav_slider_thumb")
      )
    }
  }
}

private fun iconForEditorCategory(category: EditorMainCategory): ImageVector {
  return when (category) {
    EditorMainCategory.EDIT -> Icons.Outlined.ContentCut
    EditorMainCategory.AUDIO -> Icons.Outlined.MusicNote
    EditorMainCategory.TEXT -> Icons.Outlined.TextFields
    EditorMainCategory.OVERLAY -> Icons.Outlined.PictureInPictureAlt
    EditorMainCategory.EFFECTS -> Icons.Outlined.StarOutline
    EditorMainCategory.CAPTIONS -> Icons.Outlined.ClosedCaption
    EditorMainCategory.ASPECT_RATIO -> Icons.Outlined.AspectRatio
    EditorMainCategory.FILTERS -> Icons.Outlined.FilterVintage
    EditorMainCategory.TRANSITIONS -> Icons.Outlined.SwapHoriz
    EditorMainCategory.STICKERS -> Icons.Outlined.EmojiEmotions
    EditorMainCategory.AI_TOOLS -> Icons.Outlined.AutoAwesome
    EditorMainCategory.MORE -> Icons.Outlined.MoreHoriz
  }
}

/**
 * Separate Horizontally Scrollable Contextual Tools Strip (Section 13).
 */
@Composable
fun EditorContextualToolsStrip(
  activeCategory: EditorMainCategory,
  currentAspectRatio: String,
  hasSelectedClip: Boolean,
  onSelectAspectRatio: (String) -> Unit,
  onImportVideoOrPhoto: () -> Unit,
  onSplitClip: () -> Unit,
  onTrimLeftMinusHalfSec: () -> Unit,
  onTrimRightMinusHalfSec: () -> Unit,
  onDuplicateClip: () -> Unit,
  onDeleteClip: () -> Unit,
  onSpeedChange: (Float) -> Unit,
  onRotateClip: () -> Unit,
  onAddTextClick: () -> Unit,
  onAddAudioClick: () -> Unit,
  onAddOverlayClick: () -> Unit,
  onCloseContextualStrip: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(48.dp)
      .background(Color(0xFF16161A))
      .border(width = 0.5.dp, color = Color(0xFF2A2A30))
      .padding(horizontal = 10.dp)
      .testTag("editor_contextual_tools_strip"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = Modifier
        .weight(1f)
        .horizontalScroll(rememberScrollState()),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      when (activeCategory) {
        EditorMainCategory.ASPECT_RATIO -> {
          VideoEditorViewModel.APPROVED_ASPECT_RATIOS.forEach { ratio ->
            val selected = currentAspectRatio == ratio
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (selected) EditorExportCyan else EditorPillDark)
                .clickable { onSelectAspectRatio(ratio) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("contextual_aspect_$ratio"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = ratio,
                color = if (selected) Color(0xFF061618) else EditorTextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        EditorMainCategory.EDIT -> {
          ContextualActionChip(
            icon = Icons.Default.Add,
            label = "Import Media",
            onClick = onImportVideoOrPhoto,
            testTag = "ctx_edit_import"
          )
          ContextualActionChip(
            icon = Icons.Outlined.ContentCut,
            label = "Split",
            onClick = onSplitClip,
            enabled = hasSelectedClip,
            testTag = "ctx_edit_split"
          )
          ContextualActionChip(
            icon = Icons.Outlined.ContentCut,
            label = "Trim -0.5s",
            onClick = onTrimRightMinusHalfSec,
            enabled = hasSelectedClip,
            testTag = "ctx_edit_trim_end"
          )
          ContextualActionChip(
            icon = Icons.Outlined.ContentCopy,
            label = "Duplicate",
            onClick = onDuplicateClip,
            enabled = hasSelectedClip,
            testTag = "ctx_edit_duplicate"
          )
          ContextualActionChip(
            icon = Icons.Outlined.Rotate90DegreesCcw,
            label = "Rotate 90°",
            onClick = onRotateClip,
            enabled = hasSelectedClip,
            testTag = "ctx_edit_rotate"
          )
          ContextualActionChip(
            icon = Icons.Outlined.Speed,
            label = "Speed 1.5x",
            onClick = { onSpeedChange(1.5f) },
            enabled = hasSelectedClip,
            testTag = "ctx_edit_speed_1_5x"
          )
          ContextualActionChip(
            icon = Icons.Outlined.Speed,
            label = "Speed 1.0x",
            onClick = { onSpeedChange(1.0f) },
            enabled = hasSelectedClip,
            testTag = "ctx_edit_speed_1x"
          )
          ContextualActionChip(
            icon = Icons.Outlined.DeleteOutline,
            label = "Delete",
            onClick = onDeleteClip,
            enabled = hasSelectedClip,
            testTag = "ctx_edit_delete"
          )
        }

        EditorMainCategory.TEXT, EditorMainCategory.CAPTIONS -> {
          ContextualActionChip(
            icon = Icons.Outlined.TextFields,
            label = "+ Add Text Layer",
            onClick = onAddTextClick,
            testTag = "ctx_add_text_layer"
          )
        }

        EditorMainCategory.AUDIO -> {
          ContextualActionChip(
            icon = Icons.Outlined.MusicNote,
            label = "+ Add Audio Track",
            onClick = onAddAudioClick,
            testTag = "ctx_add_audio_track"
          )
        }

        EditorMainCategory.OVERLAY, EditorMainCategory.STICKERS -> {
          ContextualActionChip(
            icon = Icons.Outlined.PictureInPictureAlt,
            label = "+ Add Overlay Media",
            onClick = onAddOverlayClick,
            testTag = "ctx_add_overlay_media"
          )
        }

        else -> {
          Text(
            text = "${activeCategory.label}: Select or import a clip to apply ${activeCategory.label.lowercase()}.",
            color = EditorTextMuted,
            fontSize = 12.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.width(6.dp))
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .clickable(onClick = onCloseContextualStrip)
        .testTag("editor_close_contextual_strip"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close contextual tools",
        tint = EditorTextMuted,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
private fun ContextualActionChip(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  testTag: String,
  enabled: Boolean = true
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(7.dp))
      .background(if (enabled) EditorPillDark else Color(0xFF1A1A1D))
      .clickable(enabled = enabled, onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = if (enabled) EditorTextWhite else Color(0xFF55555A),
      modifier = Modifier.size(16.dp)
    )
    Text(
      text = label,
      color = if (enabled) EditorTextWhite else Color(0xFF55555A),
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

/**
 * Resolves built-in 16:9 and 9:16 demo drawable resources synchronously for instant rendering.
 */
@DrawableRes
fun resolveDemoDrawableRes(uriString: String?): Int? {
  if (uriString.isNullOrBlank()) return null
  return when {
    uriString.endsWith("img_demo_video_16_9") -> R.drawable.img_demo_video_16_9
    uriString.endsWith("img_demo_video_9_16") -> R.drawable.img_demo_video_9_16
    else -> null
  }
}

/**
 * Decodes a real video frame or image bitmap from a content/file URI off the main thread.
 */
@Composable
fun rememberMediaFrameBitmap(
  uriString: String?,
  offsetMicros: Long = 0L
): ImageBitmap? {
  val context = LocalContext.current
  val bitmapState = produceState<ImageBitmap?>(initialValue = null, uriString, offsetMicros / 1_000_000L) {
    if (uriString.isNullOrBlank() || resolveDemoDrawableRes(uriString) != null) {
      value = null
      return@produceState
    }
    value = withContext(Dispatchers.IO) {
      runCatching {
        val uri = Uri.parse(uriString)
        val retriever = MediaMetadataRetriever()
        try {
          retriever.setDataSource(context, uri)
          val frame = retriever.getFrameAtTime(
            offsetMicros.coerceAtLeast(0L),
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
          )
          if (frame != null) {
            return@runCatching frame.asImageBitmap()
          }
        } catch (_: Throwable) {
        } finally {
          runCatching { retriever.release() }
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
      }.getOrNull()
    }
  }
  return bitmapState.value
}
