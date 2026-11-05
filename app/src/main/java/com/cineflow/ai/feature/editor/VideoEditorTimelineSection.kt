package com.cineflow.ai.feature.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cineflow.ai.R
import com.cineflow.ai.core.model.TimelineClipItem
import com.cineflow.ai.core.model.TrackType

/**
 * Universal Multitrack Timeline positioned in the middle-to-lower half of the screen
 * matching `17_Video_Editor_Dashboard.jpg` and
 * `Screenshot_2026-11-03-22-02-04-985_com.lemon.lvoverseas.jpg`.
 */
@Composable
fun EditorTimelineWorkspace(
  clips: List<TimelineClipItem>,
  selectedClipId: String?,
  playheadMicros: Long,
  totalDurationMicros: Long,
  isMainTrackMuted: Boolean,
  coverUri: String?,
  zoomScale: Float,
  onZoomChange: (Float) -> Unit,
  onSeekToMicros: (Long) -> Unit,
  onSelectClip: (String?) -> Unit,
  onToggleMuteClip: () -> Unit,
  onPickCoverClick: () -> Unit,
  onAddMainClipClick: () -> Unit,
  onAddAudioClick: () -> Unit,
  onInsertTransitionClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val mainClips = remember(clips) {
    clips.filter { it.trackType == TrackType.MAIN_VIDEO }.sortedBy { it.orderIndex }
  }
  val audioClips = remember(clips) {
    clips.filter { it.trackType == TrackType.AUDIO }
  }
  val overlayAndTextClips = remember(clips) {
    clips.filter { it.trackType != TrackType.MAIN_VIDEO && it.trackType != TrackType.AUDIO }
  }

  val currentFormatted = remember(playheadMicros) {
    VideoEditorViewModel.formatTimeMmSs(playheadMicros)
  }
  val totalFormatted = remember(totalDurationMicros) {
    VideoEditorViewModel.formatTimeMmSs(totalDurationMicros)
  }

  val scrollState = rememberScrollState()
  val density = LocalDensity.current
  val dpPerSecond = (18f * zoomScale).coerceIn(12f, 80f)
  val pxPerSecond = with(density) { dpPerSecond.dp.toPx().coerceAtLeast(1f) }

  // Sync horizontal scroll offset when user scrubs timeline manually
  LaunchedEffect(scrollState.isScrollInProgress, scrollState.value) {
    if (scrollState.isScrollInProgress && totalDurationMicros > 0L) {
      val scrubbedSeconds = scrollState.value / pxPerSecond
      val targetMicros = (scrubbedSeconds * 1_000_000f).toLong().coerceIn(0L, totalDurationMicros)
      onSeekToMicros(targetMicros)
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(EditorBgBlack)
      .testTag("editor_timeline_workspace")
  ) {
    HorizontalDivider(color = Color(0xFF1F1F23), thickness = 1.dp)

    // 1. Time Readout ("00:00 / 00:17") + Time Ruler Row ("00:00  •  00:02  •")
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(26.dp)
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = EditorTextWhite,
              fontWeight = FontWeight.Medium
            )
          ) {
            append(currentFormatted)
          }
          withStyle(
            SpanStyle(
              color = Color(0xFF6E6E73),
              fontWeight = FontWeight.Normal
            )
          ) {
            append(" / $totalFormatted")
          }
        },
        fontSize = 11.5.sp,
        modifier = Modifier
          .width(124.dp)
          .testTag("editor_time_readout")
      )

      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "00:00",
          color = Color(0xFF787880),
          fontSize = 11.sp
        )
        Text(
          text = "•",
          color = Color(0xFF787880),
          fontSize = 11.sp
        )
        Text(
          text = if (totalDurationMicros >= 25_000_000L) "00:20" else "00:02",
          color = Color(0xFF787880),
          fontSize = 11.sp
        )
        Text(
          text = "•",
          color = Color(0xFF787880),
          fontSize = 11.sp
        )
        Text(
          text = if (totalDurationMicros >= 25_000_000L) "00:30" else "00:04",
          color = Color(0xFF787880),
          fontSize = 11.sp
        )
      }
    }

    // 2. Main Timeline Stage (Occupies the remaining middle-to-lower half of the screen)
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      val halfWidth = maxWidth / 2

      // Far-left vertical scroll indicator bar matching reference screenshots
      Box(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(start = 5.dp, top = 2.dp)
          .width(3.dp)
          .height(64.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(Color(0xFF636368))
      )

      // Left-of-Playhead Controls ("Mute clip" + "Cover" box) positioned just left of center playhead
      Row(
        modifier = Modifier
          .align(Alignment.TopStart)
          .offset(x = (halfWidth - 118.dp).coerceAtLeast(14.dp), y = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        val muteDesc = if (isMainTrackMuted) {
          stringResource(R.string.ve_unmute_clip)
        } else {
          stringResource(R.string.ve_mute_clip)
        }
        Column(
          modifier = Modifier
            .width(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onToggleMuteClip)
            .padding(vertical = 4.dp)
            .semantics { contentDescription = muteDesc }
            .testTag("editor_mute_clip_button"),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = if (isMainTrackMuted) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp,
            contentDescription = null,
            tint = if (isMainTrackMuted) EditorExportCyan else Color(0xFFD4D4D8),
            modifier = Modifier.size(19.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = stringResource(R.string.ve_mute_clip),
            color = if (isMainTrackMuted) EditorExportCyan else Color(0xFFB8B8BE),
            fontSize = 10.sp,
            lineHeight = 11.5.sp,
            textAlign = TextAlign.Center
          )
        }

        // Cover box matching reference screenshots
        val effectiveCoverUri = coverUri ?: mainClips.firstOrNull()?.mediaUri
        val coverDemoRes = resolveDemoDrawableRes(effectiveCoverUri)
        val coverBitmap = if (coverDemoRes == null) {
          rememberMediaFrameBitmap(uriString = effectiveCoverUri, offsetMicros = 0L)
        } else {
          null
        }

        Box(
          modifier = Modifier
            .size(width = 50.dp, height = 52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1F1F24))
            .border(1.dp, Color(0xFF33333A), RoundedCornerShape(8.dp))
            .clickable(onClick = onPickCoverClick)
            .testTag("editor_cover_button"),
          contentAlignment = Alignment.Center
        ) {
          when {
            coverDemoRes != null -> {
              Image(
                painter = painterResource(id = coverDemoRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(Color(0x66000000))
              )
            }
            coverBitmap != null -> {
              Image(
                bitmap = coverBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(Color(0x66000000))
              )
            }
          }
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Edit,
              contentDescription = null,
              tint = EditorTextWhite,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = stringResource(R.string.ve_cover),
              color = EditorTextWhite,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      // Right-of-Playhead Scrollable Multitrack Lanes (starting at `halfWidth` where the playhead line sits)
      Row(
        modifier = Modifier
          .fillMaxSize()
          .horizontalScroll(scrollState)
      ) {
        Spacer(modifier = Modifier.width(halfWidth))

        Column(
          modifier = Modifier
            .padding(top = 48.dp)
            .width(maxOf(halfWidth + 160.dp, ((totalDurationMicros / 1_000_000f) * dpPerSecond).dp + 240.dp))
        ) {
          // A. Main Video Track Row (Height = 52.dp) — Multiple Clips with Filmstrip & White `[|]` Transition Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("editor_main_video_track"),
            contentAlignment = Alignment.CenterStart
          ) {
            if (mainClips.isNotEmpty()) {
              Row(
                verticalAlignment = Alignment.CenterVertically
              ) {
                mainClips.forEachIndexed { index, clip ->
                  val clipWidthDp = ((clip.timelineDurationMicros / 1_000_000f) * dpPerSecond)
                    .coerceAtLeast(88f)
                    .dp
                  val isSelected = clip.clipId == selectedClipId
                  val demoRes = resolveDemoDrawableRes(clip.mediaUri)
                  val clipFrame = if (demoRes == null) {
                    rememberMediaFrameBitmap(
                      uriString = clip.mediaUri,
                      offsetMicros = clip.sourceInMicros
                    )
                  } else {
                    null
                  }

                  Box(
                    modifier = Modifier
                      .width(clipWidthDp)
                      .height(52.dp)
                      .clip(RoundedCornerShape(3.dp))
                      .background(Color(0xFF24242C))
                      .then(
                        if (isSelected) {
                          Modifier.border(1.8.dp, Color.White, RoundedCornerShape(3.dp))
                        } else {
                          Modifier
                        }
                      )
                      .clickable { onSelectClip(clip.clipId) }
                      .testTag("timeline_clip_${clip.clipId}")
                  ) {
                    when {
                      demoRes != null -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                          repeat(3) {
                            Image(
                              painter = painterResource(id = demoRes),
                              contentDescription = null,
                              contentScale = ContentScale.Crop,
                              modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                            )
                          }
                        }
                      }
                      clipFrame != null -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                          repeat(3) {
                            Image(
                              bitmap = clipFrame,
                              contentDescription = null,
                              contentScale = ContentScale.Crop,
                              modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                            )
                          }
                        }
                      }
                    }

                    // Visible White Left & Right Trim Handles when selected
                    if (isSelected) {
                      Box(
                        modifier = Modifier
                          .align(Alignment.CenterStart)
                          .width(10.dp)
                          .fillMaxHeight()
                          .background(Color.White)
                          .testTag("trim_handle_left_${clip.clipId}"),
                        contentAlignment = Alignment.Center
                      ) {
                        Box(
                          modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .background(Color.Black)
                        )
                      }
                      Box(
                        modifier = Modifier
                          .align(Alignment.CenterEnd)
                          .width(10.dp)
                          .fillMaxHeight()
                          .background(Color.White)
                          .testTag("trim_handle_right_${clip.clipId}"),
                        contentAlignment = Alignment.Center
                      ) {
                        Box(
                          modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .background(Color.Black)
                        )
                      }
                    }
                  }

                  // White Rounded Square `[ | ]` Transition Button between adjacent Main Video clips (matching Reference 2)
                  if (index < mainClips.lastIndex) {
                    Box(
                      modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .size(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .clickable { onInsertTransitionClick(clip.clipId) }
                        .zIndex(2f)
                        .testTag("timeline_transition_button_${clip.clipId}"),
                      contentAlignment = Alignment.Center
                    ) {
                      Box(
                        modifier = Modifier
                          .width(2.dp)
                          .height(11.dp)
                          .background(Color(0xFF141417))
                      )
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // B. "+ Add audio" Track Row directly below Main Video Track matching reference screenshots
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(34.dp)
              .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
              .background(Color(0xFF222225))
              .clickable(onClick = onAddAudioClick)
              .padding(horizontal = 10.dp)
              .testTag("editor_add_audio_button"),
            contentAlignment = Alignment.CenterStart
          ) {
            if (audioClips.isEmpty()) {
              Text(
                text = stringResource(R.string.ve_add_audio),
                color = Color(0xFFDADADE),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal
              )
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Outlined.MusicNote,
                  contentDescription = null,
                  tint = EditorExportCyan,
                  modifier = Modifier.size(15.dp)
                )
                audioClips.forEach { audioClip ->
                  Text(
                    text = "${audioClip.label} (${String.format("%.1fs", audioClip.timelineDurationMicros / 1_000_000f)})",
                    color = EditorTextWhite,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(Color(0xFF1A3A40))
                      .clickable { onSelectClip(audioClip.clipId) }
                      .padding(horizontal = 8.dp, vertical = 3.dp)
                      .testTag("timeline_audio_clip_${audioClip.clipId}")
                  )
                }
              }
            }
          }

          // C. Separate Track Rows for Independently Positioned Visual Overlays / Text
          if (overlayAndTextClips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(5.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              overlayAndTextClips.forEach { ovl ->
                val isSelected = ovl.clipId == selectedClipId
                Box(
                  modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF311F47))
                    .border(
                      width = if (isSelected) 1.5.dp else 0.8.dp,
                      color = if (isSelected) Color.White else Color(0xFF7E22CE),
                      shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onSelectClip(ovl.clipId) }
                    .padding(horizontal = 8.dp)
                    .testTag("timeline_overlay_clip_${ovl.clipId}"),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "T: ${ovl.overlayText ?: ovl.label}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }
        }
      }

      // 3. Center Fixed Vertical White Playhead Line (matching reference screenshots)
      Box(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 14.dp)
          .width(2.dp)
          .height(136.dp)
          .background(Color.White)
          .testTag("editor_playhead_line")
      )

      // 4. FIXED Right-Side White Square "+" Media Upload Button (Never scrolls with timeline)
      WhitePlusAddClipButton(
        onClick = onAddMainClipClick,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 57.dp, end = 12.dp)
          .shadow(elevation = 6.dp, shape = RoundedCornerShape(6.dp))
      )
    }
  }
}

@Composable
private fun WhitePlusAddClipButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val addClipDesc = stringResource(R.string.ve_cd_add_clip)
  Box(
    modifier = modifier
      .size(34.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(Color.White)
      .clickable(onClick = onClick)
      .semantics { contentDescription = addClipDesc }
      .testTag("editor_add_clip_button"),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = Icons.Default.Add,
      contentDescription = addClipDesc,
      tint = Color.Black,
      modifier = Modifier.size(22.dp)
    )
  }
}
