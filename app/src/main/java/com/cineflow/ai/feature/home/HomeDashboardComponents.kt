package com.cineflow.ai.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cineflow.ai.R

/**
 * 3D Gradient Play Ribbon Logo matching the top-left brand mark in 01_Home_Dashboard.png.
 */
@Composable
fun CineFlowBrandLogo(modifier: Modifier = Modifier) {
  Image(
    painter = painterResource(id = R.drawable.img_home_brand_logo),
    contentDescription = stringResource(R.string.app_name),
    contentScale = ContentScale.Fit,
    modifier = modifier.size(38.dp)
  )
}

/**
 * Circular creator profile avatar matching top-right of 01_Home_Dashboard.png.
 */
@Composable
fun CreatorAvatarButton(
  isSignedIn: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cd = stringResource(R.string.cd_profile_avatar)
  Box(
    modifier = modifier
      .size(44.dp)
      .clip(CircleShape)
      .clickable(onClick = onClick)
      .semantics { contentDescription = cd },
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_home_avatar),
      contentDescription = cd,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
    )
  }
}

/**
 * Hero Banner matching 01_Home_Dashboard.png:
 * Renders the exact Hero Banner visual asset (`R.drawable.img_home_hero_banner`) with
 * interactive hit-targets for "Get Started >" (`home_get_started_button`) and the 4 carousel dots.
 */
@Composable
fun AiPoweredHeroBanner(
  currentPage: Int,
  onPageSelected: (Int) -> Unit,
  onGetStartedClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val getStartedDesc = stringResource(R.string.hero_cta_get_started)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(700f / 284f)
      .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x337B2FF7))
      .clip(RoundedCornerShape(20.dp))
      .clickable(onClick = onGetStartedClick)
      .testTag("home_hero_banner")
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_home_hero_banner),
      contentDescription = stringResource(R.string.hero_title_turn_ideas),
      contentScale = ContentScale.FillBounds,
      modifier = Modifier.fillMaxSize()
    )

    // Interactive hit-target over the "Get Started >" pill CTA button
    Box(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 14.dp, bottom = 18.dp)
        .width(156.dp)
        .height(42.dp)
        .clip(RoundedCornerShape(21.dp))
        .clickable(onClick = onGetStartedClick)
        .semantics { contentDescription = getStartedDesc }
        .testTag("home_get_started_button")
    )

    // Interactive carousel indicator dots at bottom-center
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      for (index in 0..3) {
        val isSelected = index == currentPage
        Box(
          modifier = Modifier
            .size(if (isSelected) 8.dp else 7.dp)
            .clip(CircleShape)
            .background(
              if (isSelected) Color.White else Color.White.copy(alpha = 0.40f)
            )
            .clickable { onPageSelected(index) }
        )
      }
    }
  }
}

/**
 * Portrait Trending Template card matching the 5-card row in 01_Home_Dashboard.png.
 */
@Composable
fun TrendingTemplateCard(
  item: TrendingTemplateItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val title = stringResource(item.titleRes)
  Box(
    modifier = modifier
      .aspectRatio(135f / 200f)
      .shadow(elevation = 3.dp, shape = RoundedCornerShape(14.dp))
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .semantics { contentDescription = title }
      .testTag(item.testTag)
  ) {
    Image(
      painter = painterResource(id = item.imageRes),
      contentDescription = title,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
  }
}
