package com.cineflow.ai.feature.projects

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cineflow.ai.R
import com.cineflow.ai.ui.theme.PmPillBackedUpBg
import com.cineflow.ai.ui.theme.PmPillBackedUpBorder
import com.cineflow.ai.ui.theme.PmPillDraftBg
import com.cineflow.ai.ui.theme.PmPillDraftBorder
import com.cineflow.ai.ui.theme.PmPillEditedBg
import com.cineflow.ai.ui.theme.PmPillEditedBorder
import com.cineflow.ai.ui.theme.PmPillExportedBg
import com.cineflow.ai.ui.theme.PmPillExportedBorder
import com.cineflow.ai.ui.theme.PmPillProjectBg
import com.cineflow.ai.ui.theme.PmPillProjectBorder

/**
 * Top-right glowing 3D Neon Clapperboard & Filmstrip hero artwork matching
 * CineFlow AI Project Dashboard(1).png.
 */
@Composable
fun ProjectHeroClapperboardArtwork(modifier: Modifier = Modifier) {
  Image(
    painter = painterResource(id = R.drawable.img_pm_hero_clapperboard),
    contentDescription = null,
    contentScale = ContentScale.Fit,
    modifier = modifier.size(width = 148.dp, height = 84.dp)
  )
}

/**
 * Scenic project thumbnail with centered Play overlay and bottom-right duration pill
 * matching CineFlow AI Project Dashboard(1).png.
 */
@Composable
fun ProjectThumbnailPreview(
  visualThemeIndex: Int,
  durationText: String,
  modifier: Modifier = Modifier
) {
  val thumbRes = when (visualThemeIndex % 5) {
    0 -> R.drawable.img_pm_thumb_0
    1 -> R.drawable.img_pm_thumb_1
    2 -> R.drawable.img_pm_thumb_2
    3 -> R.drawable.img_pm_thumb_3
    else -> R.drawable.img_pm_thumb_4
  }

  Box(
    modifier = modifier
      .size(width = 126.dp, height = 82.dp)
      .clip(RoundedCornerShape(12.dp))
      .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
  ) {
    Image(
      painter = painterResource(id = thumbRes),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.matchParentSize()
    )

    // Center circular dark-translucent play button overlay
    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .size(30.dp)
        .clip(CircleShape)
        .background(Color(0xAA090A1F))
        .border(1.2.dp, Color.White.copy(alpha = 0.75f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.PlayArrow,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
    }

    // Bottom-right duration pill ("02:15")
    Box(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 6.dp, bottom = 5.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(Color(0xE6090A1A))
        .padding(horizontal = 6.dp, vertical = 2.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = durationText,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

/**
 * Status Pill Badge ("Project", "Edited", "Draft", "Exported", "Backed Up")
 * matching CineFlow AI Project Dashboard(1).png.
 */
@Composable
fun ProjectStatusPill(
  text: String,
  modifier: Modifier = Modifier
) {
  val (bgColor, borderColor) = when (text.lowercase()) {
    "project" -> PmPillProjectBg to PmPillProjectBorder
    "edited" -> PmPillEditedBg to PmPillEditedBorder
    "exported" -> PmPillExportedBg to PmPillExportedBorder
    "backed up" -> PmPillBackedUpBg to PmPillBackedUpBorder
    else -> PmPillDraftBg to PmPillDraftBorder
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(50))
      .background(bgColor)
      .border(1.dp, borderColor, RoundedCornerShape(50))
      .padding(horizontal = 10.dp, vertical = 2.5.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = Color.White,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}
