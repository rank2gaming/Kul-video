package com.cineflow.ai.feature.export

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cineflow.ai.R
import kotlin.math.roundToInt

// Exact Color Palette from `03_Export_Video_Screen_4f00ed49.png`
internal val ExportBgDeep = Color(0xFF090910)
internal val ExportCardSurface = Color(0xFF12121E)
internal val ExportCardBorder = Color(0xFF25243A)
internal val ExportChipUnselectedBg = Color(0xFF1A1A28)
internal val ExportChipUnselectedBorder = Color(0xFF34334E)
internal val ExportTextPrimary = Color(0xFFFFFFFF)
internal val ExportTextSecondary = Color(0xFF9E9EB8)
internal val ExportAccentPurple = Color(0xFF9B42FF)
internal val ExportAccentBlue = Color(0xFF29C5FF)
internal val ExportCyanHighlight = Color(0xFF33C9FF)
internal val ExportGreenBadge = Color(0xFF2EE68A)

internal val ExportSelectedChipBrush = Brush.horizontalGradient(
  colors = listOf(Color(0xFF933DFB), Color(0xFF5D53FF), Color(0xFF27C2FF))
)

internal val ExportEstimatedBannerBrush = Brush.horizontalGradient(
  colors = listOf(Color(0xFF1F193D), Color(0xFF172347), Color(0xFF112D4E))
)

internal val ExportEstimatedBorderBrush = Brush.horizontalGradient(
  colors = listOf(Color(0xFF7B3FE4), Color(0xFF29B6F6))
)

/**
 * 1. Top Header Bar (`←` | CineFlow AI | `Export Video` | `Device: High-End ✔ / Max: 8K / 240 FPS`)
 */
@Composable
fun ExportTopHeaderBar(
  deviceCapability: DeviceExportCapability,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .clickable(onClick = onBackClick)
          .testTag("export_back_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back to Editor",
          tint = ExportTextPrimary,
          modifier = Modifier.size(22.dp)
        )
      }

      // Brand Icon + CineFlow AI
      Row(
        modifier = Modifier.testTag("export_brand_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        ExportBrandPlayBadge(modifier = Modifier.size(24.dp))
        Text(
          text = buildAnnotatedString {
            withStyle(SpanStyle(color = ExportTextPrimary, fontWeight = FontWeight.ExtraBold)) {
              append("CineFlow ")
            }
            withStyle(SpanStyle(color = ExportAccentPurple, fontWeight = FontWeight.ExtraBold)) {
              append("AI")
            }
          },
          fontSize = 16.sp
        )
      }

      // Vertical Divider
      Box(
        modifier = Modifier
          .padding(horizontal = 2.dp)
          .width(1.dp)
          .height(20.dp)
          .background(Color(0xFF3E3D58))
      )

      Text(
        text = "Export Video",
        color = ExportTextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    // Right Device Capability Pill (`Device: High-End ✔` / `Max: 8K / 240 FPS`)
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xFF141524))
        .border(1.dp, Color(0xFF323450), RoundedCornerShape(10.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .testTag("export_device_capability_badge"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Smartphone,
        contentDescription = null,
        tint = Color(0xFFD0D2E8),
        modifier = Modifier.size(16.dp)
      )
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Text(
            text = deviceCapability.tierTitle,
            color = ExportCyanHighlight,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold
          )
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Verified Device Capability",
            tint = ExportGreenBadge,
            modifier = Modifier.size(11.dp)
          )
        }
        Text(
          text = buildAnnotatedString {
            withStyle(SpanStyle(color = ExportTextSecondary, fontWeight = FontWeight.Medium)) {
              append("Max: ")
            }
            withStyle(SpanStyle(color = ExportGreenBadge, fontWeight = FontWeight.Bold)) {
              append(deviceCapability.maxSpecSubtitle.removePrefix("Max: "))
            }
          },
          fontSize = 9.sp
        )
      }
    }
  }
}

@Composable
private fun ExportBrandPlayBadge(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
      moveTo(w * 0.16f, h * 0.10f)
      lineTo(w * 0.90f, h * 0.50f)
      lineTo(w * 0.16f, h * 0.90f)
      close()
    }
    drawPath(
      path = path,
      brush = Brush.linearGradient(
        colors = listOf(Color(0xFFA239FF), Color(0xFF5D5FEF), Color(0xFF25D0FF))
      ),
      style = Stroke(width = w * 0.20f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
  }
}

/**
 * 2. Video Preview Card (`img_export_preview_banner`, Center Play Overlay, Bottom Transport Bar)
 */
@Composable
fun ExportVideoPreviewCard(
  positionMicros: Long,
  durationMicros: Long,
  isPlaying: Boolean,
  resolutionLabel: String,
  aspectRatioLabel: String,
  onTogglePlayPause: () -> Unit,
  onSeekFraction: (Float) -> Unit,
  onToggleFullscreen: () -> Unit,
  modifier: Modifier = Modifier
) {
  val safeDuration = durationMicros.coerceAtLeast(1_000_000L)
  val progressFraction = (positionMicros.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
  val displayAspectRatio = if (aspectRatioLabel == "16:9") "9:16" else aspectRatioLabel

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(Color(0xFF10101A))
      .border(1.dp, Color(0xFF2D2C46), RoundedCornerShape(14.dp))
      .testTag("export_video_preview_card")
  ) {
    // Top Visual Frame
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(148.dp)
        .clickable(onClick = onTogglePlayPause),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_export_preview_banner),
        contentDescription = "Export Video Preview",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    }

    // Bottom Transport & Badges Strip (`▶` `00:00 / 00:30` [slider] `1080p` `9:16` `⛶`)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF12121D))
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .clickable(onClick = onTogglePlayPause)
          .testTag("export_preview_play_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = if (isPlaying) "Pause preview" else "Play preview",
          tint = ExportTextPrimary,
          modifier = Modifier.size(18.dp)
        )
      }

      Text(
        text = "${ExportVideoViewModel.formatTimeMmSs(positionMicros)} / ${ExportVideoViewModel.formatTimeMmSs(safeDuration)}",
        color = ExportTextPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.testTag("export_preview_time_readout")
      )

      // Gradient Progress Slider Bar
      BoxWithConstraints(
        modifier = Modifier
          .weight(1f)
          .height(20.dp)
          .pointerInput(Unit) {
            detectTapGestures { offset ->
              val frac = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
              onSeekFraction(frac)
            }
          }
          .pointerInput(Unit) {
            detectHorizontalDragGestures { change, _ ->
              change.consume()
              val frac = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
              onSeekFraction(frac)
            }
          }
          .testTag("export_preview_seek_bar"),
        contentAlignment = Alignment.CenterStart
      ) {
        val density = LocalDensity.current
        val trackW = maxWidth
        val displayFraction = if (positionMicros == 0L) 0.28f else progressFraction
        val thumbOffsetPx = with(density) { ((trackW - 12.dp).toPx() * displayFraction).roundToInt() }

        // Inactive Track
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF393A4E))
        )
        // Active Gradient Track
        Box(
          modifier = Modifier
            .fillMaxWidth(displayFraction.coerceIn(0.05f, 1f))
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(ExportSelectedChipBrush)
        )
        // White Circular Thumb
        Box(
          modifier = Modifier
            .offset { IntOffset(thumbOffsetPx, 0) }
            .size(12.dp)
            .clip(CircleShape)
            .background(Color.White)
        )
      }

      // Resolution Badge (`1080p`)
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(5.dp))
          .background(Color(0xFF1E1E2E))
          .border(1.dp, Color(0xFF3A3A54), RoundedCornerShape(5.dp))
          .padding(horizontal = 6.dp, vertical = 2.dp)
          .testTag("export_preview_res_badge")
      ) {
        Text(
          text = resolutionLabel,
          color = ExportTextPrimary,
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      // Aspect Ratio Badge (`9:16`)
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(5.dp))
          .background(Color(0xFF1E1E2E))
          .border(1.dp, Color(0xFF3A3A54), RoundedCornerShape(5.dp))
          .padding(horizontal = 6.dp, vertical = 2.dp)
          .testTag("export_preview_ratio_badge")
      ) {
        Text(
          text = displayAspectRatio,
          color = ExportTextPrimary,
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      Box(
        modifier = Modifier
          .size(22.dp)
          .clip(RoundedCornerShape(4.dp))
          .clickable(onClick = onToggleFullscreen)
          .testTag("export_preview_fullscreen_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Fullscreen,
          contentDescription = "Fullscreen Preview",
          tint = ExportTextPrimary,
          modifier = Modifier.size(17.dp)
        )
      }
    }
  }
}

/**
 * Reusable Selectable Option Chip with Purple-to-Cyan Gradient + Checkmark when selected.
 */
@Composable
fun ExportSelectableChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
  showCheckmarkWhenSelected: Boolean = true
) {
  val shape = RoundedCornerShape(8.dp)
  Box(
    modifier = modifier
      .height(32.dp)
      .clip(shape)
      .then(
        if (selected) {
          Modifier.background(ExportSelectedChipBrush)
        } else {
          Modifier
            .background(ExportChipUnselectedBg)
            .border(1.dp, ExportChipUnselectedBorder, shape)
        }
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        text = label,
        color = ExportTextPrimary,
        fontSize = 11.5.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
        maxLines = 1
      )
      if (selected && showCheckmarkWhenSelected) {
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = ExportTextPrimary,
          modifier = Modifier.size(13.dp)
        )
      }
    }
  }
}

/**
 * 3. Resolution Card (`480p`, `720p`, `1080p ✓`, `2K`, `4K`, `6K`, `8K`)
 */
@Composable
fun ExportResolutionSectionCard(
  selectedResolution: ExportResolutionOption,
  onSelectResolution: (ExportResolutionOption) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(ExportCardSurface)
      .border(1.dp, ExportCardBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 12.dp, vertical = 9.dp)
      .testTag("export_resolution_card"),
    verticalArrangement = Arrangement.spacedBy(7.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Tv,
        contentDescription = null,
        tint = ExportTextPrimary,
        modifier = Modifier.size(16.dp)
      )
      Text(
        text = "Resolution",
        color = ExportTextPrimary,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "Select export video resolution",
        color = ExportTextSecondary,
        fontSize = 10.5.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      ExportResolutionOption.entries.forEach { option ->
        ExportSelectableChip(
          label = option.chipLabel,
          selected = selectedResolution == option,
          onClick = { onSelectResolution(option) },
          testTag = "export_res_chip_${option.chipLabel}",
          modifier = Modifier.width(if (selectedResolution == option) 66.dp else 46.dp)
        )
      }
    }
  }
}

/**
 * 4. Frame Rate (FPS) Card (`24 FPS`, `25 FPS`, `30 FPS ✓`, `48 FPS`, `50 FPS`, `60 FPS`, `120 FPS`, `240 FPS`)
 */
@Composable
fun ExportFpsSectionCard(
  selectedFps: Int,
  onSelectFps: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(ExportCardSurface)
      .border(1.dp, ExportCardBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 12.dp, vertical = 9.dp)
      .testTag("export_fps_card"),
    verticalArrangement = Arrangement.spacedBy(7.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Movie,
        contentDescription = null,
        tint = ExportTextPrimary,
        modifier = Modifier.size(16.dp)
      )
      Text(
        text = "Frame Rate (FPS)",
        color = ExportTextPrimary,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "Select video frame rate (smoother motion with higher FPS)",
        color = ExportTextSecondary,
        fontSize = 10.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      EXPORT_SCREEN_FPS_CHIPS.forEach { fps ->
        ExportSelectableChip(
          label = "$fps FPS",
          selected = selectedFps == fps,
          onClick = { onSelectFps(fps) },
          testTag = "export_fps_chip_$fps"
        )
      }
    }
  }
}

/**
 * 5. Video Format (`MP4 ✓`, `MOV`) & Video Codec (`H.264 ✓`, `H.265`) Split Card
 */
@Composable
fun ExportFormatAndCodecCard(
  selectedFormat: ExportVideoFormat,
  selectedCodec: ExportVideoCodec,
  onSelectFormat: (ExportVideoFormat) -> Unit,
  onSelectCodec: (ExportVideoCodec) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
      .clip(RoundedCornerShape(12.dp))
      .background(ExportCardSurface)
      .border(1.dp, ExportCardBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 12.dp, vertical = 9.dp)
      .testTag("export_format_codec_card"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left Half: Video Format
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Description,
          contentDescription = null,
          tint = ExportTextPrimary,
          modifier = Modifier.size(15.dp)
        )
        Text(
          text = "Video Format",
          color = ExportTextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        ExportVideoFormat.entries.forEach { format ->
          ExportSelectableChip(
            label = format.label,
            selected = selectedFormat == format,
            onClick = { onSelectFormat(format) },
            testTag = "export_format_chip_${format.label}",
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Center Vertical Divider
    Box(
      modifier = Modifier
        .padding(horizontal = 10.dp)
        .width(1.dp)
        .fillMaxHeight()
        .background(Color(0xFF2B2B44))
    )

    // Right Half: Video Codec
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Code,
          contentDescription = null,
          tint = ExportTextPrimary,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = "Video Codec",
          color = ExportTextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        ExportVideoCodec.entries.forEach { codec ->
          ExportSelectableChip(
            label = codec.label,
            selected = selectedCodec == codec,
            onClick = { onSelectCodec(codec) },
            testTag = "export_codec_chip_${codec.label}",
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

/**
 * 6. Video Quality Card (`LOW`, `MEDIUM`, `HIGH ✓`)
 */
@Composable
fun ExportVideoQualityCard(
  selectedQuality: ExportQualityPreset,
  onSelectQuality: (ExportQualityPreset) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(ExportCardSurface)
      .border(1.dp, ExportCardBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 12.dp, vertical = 9.dp)
      .testTag("export_quality_card"),
    verticalArrangement = Arrangement.spacedBy(7.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Layers,
        contentDescription = null,
        tint = ExportTextPrimary,
        modifier = Modifier.size(16.dp)
      )
      Text(
        text = "Video Quality",
        color = ExportTextPrimary,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "Overall export quality (affects clarity and file size)",
        color = ExportTextSecondary,
        fontSize = 10.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ExportQualityPreset.entries.forEach { quality ->
        ExportSelectableChip(
          label = quality.label,
          selected = selectedQuality == quality,
          onClick = { onSelectQuality(quality) },
          testTag = "export_quality_chip_${quality.label}",
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

/**
 * 7. Video Bitrate Card (`5 Mbps` .. `100 Mbps`, `-` / `+` buttons, `25.0 Mbps` pill)
 */
@Composable
fun ExportVideoBitrateCard(
  bitrateMbps: Float,
  onBitrateChange: (Float) -> Unit,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
  modifier: Modifier = Modifier
) {
  val fraction = ((bitrateMbps - 5.0f) / 95.0f).coerceIn(0f, 1f)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(ExportCardSurface)
      .border(1.dp, ExportCardBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 12.dp, vertical = 9.dp)
      .testTag("export_bitrate_card"),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Speed,
          contentDescription = null,
          tint = ExportTextPrimary,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = "Video Bitrate",
          color = ExportTextPrimary,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Controls video data rate (higher bitrate = better quality)",
          color = ExportTextSecondary,
          fontSize = 9.5.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(7.dp))
          .background(Color(0xFF1A1B2E))
          .border(1.dp, Color(0xFF383958), RoundedCornerShape(7.dp))
          .padding(horizontal = 10.dp, vertical = 4.dp)
          .testTag("export_bitrate_value_badge")
      ) {
        Text(
          text = ExportVideoViewModel.formatBitrateMbps(bitrateMbps),
          color = ExportTextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Minus Button
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(Color(0xFF1A1A2B))
          .border(1.dp, Color(0xFF363652), CircleShape)
          .clickable(onClick = onDecrement)
          .testTag("export_bitrate_minus_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Remove,
          contentDescription = "Decrease Bitrate",
          tint = ExportTextPrimary,
          modifier = Modifier.size(16.dp)
        )
      }

      // Slider + 5 Mbps / 100 Mbps labels
      Column(modifier = Modifier.weight(1f)) {
        BoxWithConstraints(
          modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .pointerInput(Unit) {
              detectTapGestures { offset ->
                val newFrac = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                onBitrateChange(5.0f + newFrac * 95.0f)
              }
            }
            .pointerInput(Unit) {
              detectHorizontalDragGestures { change, _ ->
                change.consume()
                val newFrac = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                onBitrateChange(5.0f + newFrac * 95.0f)
              }
            }
            .testTag("export_bitrate_slider"),
          contentAlignment = Alignment.CenterStart
        ) {
          val density = LocalDensity.current
          val thumbOffsetPx = with(density) { ((maxWidth - 16.dp).toPx() * fraction).roundToInt() }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(5.dp)
              .clip(RoundedCornerShape(3.dp))
              .background(Color(0xFF33344A))
          )
          Box(
            modifier = Modifier
              .fillMaxWidth(fraction.coerceIn(0.03f, 1f))
              .height(5.dp)
              .clip(RoundedCornerShape(3.dp))
              .background(ExportSelectedChipBrush)
          )
          Box(
            modifier = Modifier
              .offset { IntOffset(thumbOffsetPx, 0) }
              .size(16.dp)
              .clip(CircleShape)
              .background(Color.White)
              .border(2.5.dp, Color(0xFFB388FF), CircleShape)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "5 Mbps",
            color = ExportTextSecondary,
            fontSize = 10.sp
          )
          Text(
            text = "100 Mbps",
            color = ExportTextSecondary,
            fontSize = 10.sp
          )
        }
      }

      // Plus Button
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(Color(0xFF1A1A2B))
          .border(1.dp, Color(0xFF363652), CircleShape)
          .clickable(onClick = onIncrement)
          .testTag("export_bitrate_plus_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Increase Bitrate",
          tint = ExportTextPrimary,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

/**
 * 8. Estimated File Size Banner Card (`245 MB`)
 */
@Composable
fun ExportEstimatedFileSizeCard(
  estimatedSizeMb: Int,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(ExportEstimatedBannerBrush)
      .border(1.dp, ExportEstimatedBorderBrush, RoundedCornerShape(12.dp))
      .padding(horizontal = 12.dp, vertical = 9.dp)
      .testTag("export_estimated_size_card"),
    verticalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Storage,
          contentDescription = null,
          tint = Color(0xFFA76BFF),
          modifier = Modifier.size(24.dp)
        )
        Column {
          Text(
            text = "Estimated File Size",
            color = ExportTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Approximate final size of the exported video",
            color = Color(0xFFC5C6E0),
            fontSize = 10.5.sp
          )
        }
      }

      Box(
        modifier = Modifier
          .padding(horizontal = 10.dp)
          .width(1.dp)
          .fillMaxHeight()
          .background(Color(0xFF424A70))
      )

      Text(
        text = "$estimatedSizeMb MB",
        color = ExportCyanHighlight,
        fontSize = 23.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.testTag("export_estimated_size_text")
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Info,
        contentDescription = null,
        tint = Color(0xFFB5B8D6),
        modifier = Modifier.size(13.dp)
      )
      Text(
        text = "The estimated size changes based on the selected resolution, frame rate, codec, quality and bitrate.",
        color = Color(0xFFC4C6E0),
        fontSize = 9.8.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

/**
 * 9. Audio Settings Card (`Audio Codec: AAC ⌄` | `Audio Bitrate: 128 kbps, 192 kbps, 256 kbps ✓, 320 kbps`)
 */
@Composable
fun ExportAudioSettingsCard(
  selectedAudioCodec: ExportAudioCodec,
  selectedAudioBitrate: ExportAudioBitrate,
  onSelectAudioBitrate: (ExportAudioBitrate) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(ExportCardSurface)
      .border(1.dp, ExportCardBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 12.dp, vertical = 9.dp)
      .testTag("export_audio_settings_card"),
    verticalArrangement = Arrangement.spacedBy(7.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Equalizer,
        contentDescription = null,
        tint = ExportTextPrimary,
        modifier = Modifier.size(16.dp)
      )
      Text(
        text = "Audio Settings",
        color = ExportTextPrimary,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left: Audio Codec Dropdown Box
      Column(
        modifier = Modifier.width(105.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        Text(
          text = "Audio Codec",
          color = ExportTextSecondary,
          fontSize = 10.5.sp,
          fontWeight = FontWeight.SemiBold
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ExportChipUnselectedBg)
            .border(1.dp, ExportChipUnselectedBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp)
            .testTag("export_audio_codec_selector"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = selectedAudioCodec.label,
            color = ExportTextPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
          )
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = ExportTextSecondary,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Box(
        modifier = Modifier
          .padding(horizontal = 10.dp)
          .width(1.dp)
          .fillMaxHeight()
          .background(Color(0xFF2B2B44))
      )

      // Right: Audio Bitrate Chips
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        Text(
          text = "Audio Bitrate",
          color = ExportTextSecondary,
          fontSize = 10.5.sp,
          fontWeight = FontWeight.SemiBold
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          ExportAudioBitrate.entries.forEach { option ->
            ExportSelectableChip(
              label = option.label,
              selected = selectedAudioBitrate == option,
              onClick = { onSelectAudioBitrate(option) },
              testTag = "export_audio_bitrate_chip_${option.kbps}"
            )
          }
        }
      }
    }
  }
}

/**
 * 10. Export Summary Card (2-column structured summary table)
 */
@Composable
fun ExportSummaryTableCard(
  resolution: ExportResolutionOption,
  fps: Int,
  format: ExportVideoFormat,
  videoCodec: ExportVideoCodec,
  quality: ExportQualityPreset,
  videoBitrateMbps: Float,
  audioCodec: ExportAudioCodec,
  audioBitrate: ExportAudioBitrate,
  estimatedSizeMb: Int,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(ExportCardSurface)
      .border(1.dp, ExportCardBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 12.dp, vertical = 9.dp)
      .testTag("export_summary_card"),
    verticalArrangement = Arrangement.spacedBy(7.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Description,
        contentDescription = null,
        tint = ExportTextPrimary,
        modifier = Modifier.size(16.dp)
      )
      Text(
        text = "Export Summary",
        color = ExportTextPrimary,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xFF161625))
        .border(1.dp, Color(0xFF2B2B44), RoundedCornerShape(10.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left Summary Column
      Column(
        modifier = Modifier.weight(1.05f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SummaryRowItem(
          icon = Icons.Default.Tv,
          label = "Resolution",
          value = resolution.summaryLabel()
        )
        SummaryRowItem(
          icon = Icons.Default.Movie,
          label = "Frame Rate",
          value = "$fps FPS"
        )
        SummaryRowItem(
          icon = Icons.Default.Description,
          label = "Format",
          value = format.label
        )
        SummaryRowItem(
          icon = Icons.Default.Code,
          label = "Video Codec",
          value = videoCodec.label
        )
      }

      Box(
        modifier = Modifier
          .padding(horizontal = 8.dp)
          .width(1.dp)
          .fillMaxHeight()
          .background(Color(0xFF2E2E48))
      )

      // Right Summary Column
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SummaryRowItem(
          icon = Icons.Default.Layers,
          label = "Quality",
          value = quality.label
        )
        SummaryRowItem(
          icon = Icons.Default.Speed,
          label = "Video Bitrate",
          value = ExportVideoViewModel.formatBitrateMbps(videoBitrateMbps)
        )
        SummaryRowItem(
          icon = Icons.Default.Equalizer,
          label = "Audio Bitrate",
          value = "${audioBitrate.label} (${audioCodec.label})"
        )
        SummaryRowItem(
          icon = Icons.Default.Storage,
          label = "Estimated Size",
          value = "$estimatedSizeMb MB"
        )
      }
    }
  }
}

@Composable
private fun SummaryRowItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = ExportTextSecondary,
      modifier = Modifier.size(13.dp)
    )
    Spacer(modifier = Modifier.width(5.dp))
    Text(
      text = label,
      color = ExportTextSecondary,
      fontSize = 10.sp,
      modifier = Modifier.width(68.dp),
      maxLines = 1
    )
    Text(
      text = ":  ",
      color = ExportTextSecondary,
      fontSize = 10.sp
    )
    Text(
      text = value,
      color = ExportTextPrimary,
      fontSize = 10.5.sp,
      fontWeight = FontWeight.SemiBold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

/**
 * 11. Bottom Primary Gradient CTA Button (`[↑] EXPORT VIDEO`)
 */
@Composable
fun ExportPrimaryGradientButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(50.dp)
      .clip(RoundedCornerShape(25.dp))
      .background(
        Brush.horizontalGradient(
          colors = listOf(Color(0xFFA63AFF), Color(0xFF6B4EFF), Color(0xFF1BB6FF))
        )
      )
      .border(
        width = 1.dp,
        brush = Brush.horizontalGradient(
          colors = listOf(Color(0xFFD28CFF), Color(0xFF7BE0FF))
        ),
        shape = RoundedCornerShape(25.dp)
      )
      .clickable(onClick = onClick)
      .testTag("export_video_submit_button"),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Custom Export Tray Upload Icon `[↑]`
      Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // Bottom/side tray bracket
        val trayPath = Path().apply {
          moveTo(w * 0.15f, h * 0.55f)
          lineTo(w * 0.15f, h * 0.85f)
          lineTo(w * 0.85f, h * 0.85f)
          lineTo(w * 0.85f, h * 0.55f)
        }
        drawPath(trayPath, color = Color.White, style = stroke)
        // Upward arrow shaft
        drawLine(
          color = Color.White,
          start = Offset(w * 0.50f, h * 0.65f),
          end = Offset(w * 0.50f, h * 0.16f),
          strokeWidth = w * 0.11f,
          cap = StrokeCap.Round
        )
        // Upward arrow head
        val headPath = Path().apply {
          moveTo(w * 0.28f, h * 0.36f)
          lineTo(w * 0.50f, h * 0.14f)
          lineTo(w * 0.72f, h * 0.36f)
        }
        drawPath(headPath, color = Color.White, style = stroke)
      }

      Text(
        text = "EXPORT VIDEO",
        color = Color.White,
        fontSize = 15.5.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.8.sp
      )
    }
  }
}
