package com.cineflow.ai.feature.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R

private val SubBgDeep = Color(0xFF0A0A26)
private val SubFreeCardBg = Color(0xFF121636)
private val SubFreeCardBorder = Color(0xFF3E4678)
private val SubProCardBgTop = Color(0xFF281860)
private val SubProCardBgBottom = Color(0xFF0E1842)
private val SubRowCardBg = Color(0xFF13183A)
private val SubRowCardBorder = Color(0xFF3C4478)
private val SubTextWhite = Color(0xFFFFFFFF)
private val SubTextMuted = Color(0xFFB5BCDE)
private val SubCheckGreen = Color(0xFF1BE860)
private val SubCrossSlate = Color(0xFF434C7A)

/**
 * Subscription / Plans Screen (`CineFlow AI Neon Subscription Plans.png` & Section 21).
 */
@Composable
fun SubscriptionPlansScreen(
  viewModel: SubscriptionPlansViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val selectedCycle by viewModel.selectedBillingCycle.collectAsStateWithLifecycle()
  val activeDialog by viewModel.activeDialog.collectAsStateWithLifecycle()

  BackHandler {
    onBack()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(SubBgDeep)
      .testTag("subscription_plans_screen"),
    containerColor = SubBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(SubBgDeep),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Hero Header (`img_subscription_plans_top_hero.png`)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(941f / 376f)
          .testTag("subscription_top_hero")
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_subscription_plans_top_hero),
          contentDescription = "CineFlow AI Subscription & Plan — Choose Your Plan",
          contentScale = ContentScale.FillWidth,
          modifier = Modifier.fillMaxSize()
        )

        // Top-left Back arrow hotspot (`←`)
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 10.dp, top = 8.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onBack)
            .testTag("subscription_back_button")
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 2. Monthly / Yearly Toggle Pill with floating `Save 40%` pink badge
      BillingCycleToggleBar(
        selectedCycle = selectedCycle,
        onSelectCycle = { viewModel.selectBillingCycle(it) }
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Side-by-Side Free Plan & Pro Plan Comparison Cards
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp)
          .height(IntrinsicSize.Max)
          .testTag("subscription_plans_row"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Left Column: Free Plan Card
        FreePlanComparisonCard(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        )

        // Right Column: Pro Plan Card (with `★ Most Popular` floating badge)
        ProPlanComparisonCard(
          onUpgradeClick = { viewModel.requestUpgradeToPro() },
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 4. 4-Column Feature Highlights Bar
      SubscriptionFeatureHighlightsBar(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      // 5. Bottom Management Action Card (`Manage Subscription` & `Subscription History`)
      SubscriptionManagementCard(
        onManageSubscriptionClick = { viewModel.openManageSubscription() },
        onSubscriptionHistoryClick = { viewModel.openSubscriptionHistory() },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp)
      )

      Spacer(modifier = Modifier.height(20.dp))
    }
  }

  // Section 21 Honest Prepared Subscription Status Dialog (Payment integration disabled)
  if (activeDialog != null) {
    AlertDialog(
      onDismissRequest = { viewModel.dismissDialog() },
      containerColor = Color(0xFF15193B),
      titleContentColor = SubTextWhite,
      textContentColor = SubTextMuted,
      title = {
        Text(
          text = activeDialog!!.title,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = activeDialog!!.body,
          fontSize = 13.5.sp,
          lineHeight = 19.sp
        )
      },
      confirmButton = {
        TextButton(
          onClick = { viewModel.dismissDialog() },
          modifier = Modifier.testTag("subscription_dialog_close_button")
        ) {
          Text(
            text = "Close",
            color = Color(0xFF36D6FF),
            fontWeight = FontWeight.Bold
          )
        }
      },
      modifier = Modifier.testTag("subscription_status_dialog")
    )
  }
}

@Composable
private fun BillingCycleToggleBar(
  selectedCycle: SubscriptionBillingCycle,
  onSelectCycle: (SubscriptionBillingCycle) -> Unit
) {
  Box(
    modifier = Modifier
      .width(248.dp)
      .padding(top = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(42.dp)
        .clip(RoundedCornerShape(21.dp))
        .background(Color(0xFF151A3E))
        .border(1.dp, Color(0xFF47508A), RoundedCornerShape(21.dp))
        .padding(3.dp)
        .testTag("subscription_billing_toggle_bar"),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Monthly Pill
      val isMonthly = selectedCycle == SubscriptionBillingCycle.MONTHLY
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clip(RoundedCornerShape(18.dp))
          .background(
            if (isMonthly) {
              Brush.horizontalGradient(
                colors = listOf(Color(0xFFA52EFF), Color(0xFF4C5BFF))
              )
            } else {
              Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Transparent)
              )
            }
          )
          .clickable { onSelectCycle(SubscriptionBillingCycle.MONTHLY) }
          .testTag("subscription_toggle_monthly"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Monthly",
          color = SubTextWhite,
          fontSize = 13.5.sp,
          fontWeight = if (isMonthly) FontWeight.Bold else FontWeight.Medium
        )
      }

      // Yearly Pill
      val isYearly = selectedCycle == SubscriptionBillingCycle.YEARLY
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clip(RoundedCornerShape(18.dp))
          .background(
            if (isYearly) {
              Brush.horizontalGradient(
                colors = listOf(Color(0xFFA52EFF), Color(0xFF4C5BFF))
              )
            } else {
              Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Transparent)
              )
            }
          )
          .clickable { onSelectCycle(SubscriptionBillingCycle.YEARLY) }
          .testTag("subscription_toggle_yearly"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Yearly",
          color = SubTextWhite,
          fontSize = 13.5.sp,
          fontWeight = if (isYearly) FontWeight.Bold else FontWeight.Medium
        )
      }
    }

    // Floating `Save 40%` Pink Pill Badge on top-right
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .offset(x = 12.dp, y = (-8).dp)
        .clip(RoundedCornerShape(10.dp))
        .background(
          Brush.horizontalGradient(
            colors = listOf(Color(0xFFFF3A88), Color(0xFFFF2B67))
          )
        )
        .padding(horizontal = 7.dp, vertical = 2.dp)
        .testTag("subscription_save_40_badge"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "Save 40%",
        color = Color.White,
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
private fun FreePlanComparisonCard(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .padding(top = 6.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(SubFreeCardBg)
      .border(1.dp, SubFreeCardBorder, RoundedCornerShape(20.dp))
      .padding(horizontal = 11.dp, vertical = 12.dp)
      .testTag("subscription_free_plan_card"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      // Header: Green Leaf + Free Plan + Subtitle
      Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        GreenLeafVectorIcon(
          modifier = Modifier
            .padding(top = 2.dp)
            .size(22.dp)
        )
        Column {
          Text(
            text = "Free Plan",
            color = SubTextWhite,
            fontSize = 16.5.sp,
            fontWeight = FontWeight.ExtraBold
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Get started with essential editing tools.",
            color = SubTextMuted,
            fontSize = 10.2.sp,
            lineHeight = 13.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Price: `₹0 / Forever`
      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = SubTextWhite,
              fontSize = 24.sp,
              fontWeight = FontWeight.ExtraBold
            )
          ) {
            append("₹0")
          }
          withStyle(
            SpanStyle(
              color = SubTextWhite,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          ) {
            append(" / Forever")
          }
        },
        modifier = Modifier.testTag("subscription_free_price_text")
      )

      Spacer(modifier = Modifier.height(8.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(Color(0xFF2B325B))
      )
      Spacer(modifier = Modifier.height(10.dp))

      // 8 Feature Items (5 Included ✓, 3 Excluded ✕)
      Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        PlanFeatureItemRow(text = "Basic Video Editing", included = true)
        PlanFeatureItemRow(text = "Standard Effects & Filters", included = true)
        PlanFeatureItemRow(text = "Export up to 1080p", included = true)
        PlanFeatureItemRow(text = "1 GB Cloud Storage", included = true)
        PlanFeatureItemRow(text = "Up to 5 Projects", included = true)
        PlanFeatureItemRow(text = "AI Tools (Limited)", included = false)
        PlanFeatureItemRow(text = "Premium Assets", included = false)
        PlanFeatureItemRow(text = "Priority Support", included = false)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Bottom Button: `✓ Current Plan`
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(40.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0xFF141838))
        .border(1.2.dp, Color(0xFF8C49E8), RoundedCornerShape(20.dp))
        .testTag("subscription_free_plan_button"),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Box(
          modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(SubCheckGreen),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = Color(0xFF072B12),
            modifier = Modifier.size(11.dp)
          )
        }
        Text(
          text = "Current Plan",
          color = SubTextWhite,
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
private fun ProPlanComparisonCard(
  onUpgradeClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 6.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(SubProCardBgTop, SubProCardBgBottom)
          )
        )
        .border(
          width = 1.8.dp,
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFE03BFF),
              Color(0xFF7B4BFF),
              Color(0xFF00E5FF),
              Color(0xFFD538FF)
            )
          ),
          shape = RoundedCornerShape(20.dp)
        )
        .padding(horizontal = 11.dp, vertical = 12.dp)
        .testTag("subscription_pro_plan_card"),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        // Header: Gold Crown + Pro Plan + Subtitle
        Row(
          verticalAlignment = Alignment.Top,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.padding(top = 4.dp)
        ) {
          GoldCrownVectorIcon(
            modifier = Modifier
              .padding(top = 2.dp)
              .size(23.dp)
          )
          Column {
            Text(
              text = "Pro Plan",
              color = SubTextWhite,
              fontSize = 16.5.sp,
              fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Unlock the full power of CineFlow AI.",
              color = SubTextMuted,
              fontSize = 10.2.sp,
              lineHeight = 13.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Price: `₹299 / Month`
        Text(
          text = buildAnnotatedString {
            withStyle(
              SpanStyle(
                color = SubTextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
              )
            ) {
              append("₹299")
            }
            withStyle(
              SpanStyle(
                color = SubTextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            ) {
              append(" / Month")
            }
          },
          modifier = Modifier.testTag("subscription_pro_price_text")
        )

        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF393E78))
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 8 Included Pro Feature Items
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
          PlanFeatureItemRow(text = "All Video Editing Features", included = true)
          PlanFeatureItemRow(text = "Full AI Tools (Text to Video, AI Edit, etc.)", included = true)
          PlanFeatureItemRow(text = "Premium Effects, Filters & Assets", included = true)
          PlanFeatureItemRow(text = "Export up to 4K (Ultra HD)", included = true)
          PlanFeatureItemRow(text = "100 GB Cloud Storage", included = true)
          PlanFeatureItemRow(text = "Unlimited Projects", included = true)
          PlanFeatureItemRow(text = "Priority Cloud Backup & Restore", included = true)
          PlanFeatureItemRow(text = "Priority Support", included = true)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Bottom CTA Pill Button: `⚡ Upgrade to Pro`
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(40.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(
            Brush.horizontalGradient(
              colors = listOf(
                Color(0xFFD532FF),
                Color(0xFF7B4EFF),
                Color(0xFF00DCFF)
              )
            )
          )
          .border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
              colors = listOf(Color(0xFFF09CFF), Color(0xFF8CF2FF))
            ),
            shape = RoundedCornerShape(20.dp)
          )
          .clickable(onClick = onUpgradeClick)
          .testTag("subscription_upgrade_pro_button"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(17.dp)
          )
          Text(
            text = "Upgrade to Pro",
            color = Color.White,
            fontSize = 12.8.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Floating `★ Most Popular` Lavender Pill Badge at Top-Right of Pro Plan Card
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(end = 8.dp)
        .clip(RoundedCornerShape(11.dp))
        .background(
          Brush.horizontalGradient(
            colors = listOf(Color(0xFFE7B8FF), Color(0xFFF6C6FF))
          )
        )
        .padding(horizontal = 8.dp, vertical = 2.5.dp)
        .testTag("subscription_most_popular_badge"),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
      ) {
        Icon(
          imageVector = Icons.Filled.Star,
          contentDescription = null,
          tint = Color(0xFF39186E),
          modifier = Modifier.size(11.dp)
        )
        Text(
          text = "Most Popular",
          color = Color(0xFF281052),
          fontSize = 9.2.sp,
          fontWeight = FontWeight.ExtraBold
        )
      }
    }
  }
}

@Composable
private fun PlanFeatureItemRow(
  text: String,
  included: Boolean
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Box(
      modifier = Modifier
        .size(14.dp)
        .clip(CircleShape)
        .background(if (included) SubCheckGreen else SubCrossSlate),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (included) Icons.Filled.Check else Icons.Filled.Close,
        contentDescription = null,
        tint = if (included) Color(0xFF062910) else Color(0xFFD0D5F2),
        modifier = Modifier.size(9.5.dp)
      )
    }
    Text(
      text = text,
      color = if (included) SubTextWhite else SubTextMuted,
      fontSize = 10.2.sp,
      fontWeight = if (included) FontWeight.Medium else FontWeight.Normal,
      lineHeight = 12.5.sp
    )
  }
}

@Composable
private fun SubscriptionFeatureHighlightsBar(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(SubRowCardBg)
      .border(1.dp, SubRowCardBorder, RoundedCornerShape(16.dp))
      .padding(vertical = 10.dp, horizontal = 6.dp)
      .testTag("subscription_feature_highlights_bar"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // 1. Cloud Storage (Projects & Assets)
    FeatureHighlightColumn(
      title = "Cloud Storage",
      subtitle = "(Projects & Assets)",
      icon = {
        Icon(
          imageVector = Icons.Outlined.CloudQueue,
          contentDescription = null,
          tint = Color(0xFFC8D2FF),
          modifier = Modifier.size(22.dp)
        )
      },
      testTag = "subscription_highlight_cloud_storage",
      modifier = Modifier.weight(1f)
    )

    HighlightVerticalDivider()

    // 2. AI Editing Tools (Premium)
    FeatureHighlightColumn(
      title = "AI Editing Tools",
      subtitle = "(Premium)",
      icon = {
        AiChipBadgeIcon(modifier = Modifier.size(22.dp))
      },
      testTag = "subscription_highlight_ai_tools",
      modifier = Modifier.weight(1f)
    )

    HighlightVerticalDivider()

    // 3. 4K Export (Ultra HD)
    FeatureHighlightColumn(
      title = "4K Export",
      subtitle = "(Ultra HD)",
      icon = {
        FourKFrameBadgeIcon(modifier = Modifier.size(width = 26.dp, height = 20.dp))
      },
      testTag = "subscription_highlight_4k_export",
      modifier = Modifier.weight(1f)
    )

    HighlightVerticalDivider()

    // 4. Priority Support
    FeatureHighlightColumn(
      title = "Priority",
      subtitle = "Support",
      icon = {
        Icon(
          imageVector = Icons.Outlined.HeadsetMic,
          contentDescription = null,
          tint = Color(0xFFC8D2FF),
          modifier = Modifier.size(22.dp)
        )
      },
      testTag = "subscription_highlight_priority_support",
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun FeatureHighlightColumn(
  title: String,
  subtitle: String,
  icon: @Composable () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    Box(
      modifier = Modifier.height(24.dp),
      contentAlignment = Alignment.Center
    ) {
      icon()
    }
    Text(
      text = title,
      color = SubTextWhite,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )
    Text(
      text = subtitle,
      color = SubTextMuted,
      fontSize = 9.sp,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun HighlightVerticalDivider() {
  Box(
    modifier = Modifier
      .width(1.dp)
      .height(42.dp)
      .background(Color(0xFF2D3566))
  )
}

@Composable
private fun SubscriptionManagementCard(
  onManageSubscriptionClick: () -> Unit,
  onSubscriptionHistoryClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .background(SubRowCardBg)
      .border(1.dp, SubRowCardBorder, RoundedCornerShape(18.dp))
      .padding(horizontal = 14.dp, vertical = 6.dp)
      .testTag("subscription_management_card")
  ) {
    // Row 1: Manage Subscription
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onManageSubscriptionClick)
        .padding(vertical = 10.dp)
        .testTag("subscription_manage_row"),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Outlined.CreditCard,
        contentDescription = null,
        tint = Color(0xFFD6DEFF),
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Manage Subscription",
          color = SubTextWhite,
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "View plan details, billing and renewal",
          color = SubTextMuted,
          fontSize = 11.5.sp
        )
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = "Open Manage Subscription",
        tint = Color.White,
        modifier = Modifier.size(22.dp)
      )
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(Color(0xFF28305E))
    )

    // Row 2: Subscription History
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onSubscriptionHistoryClick)
        .padding(vertical = 10.dp)
        .testTag("subscription_history_row"),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Outlined.Schedule,
        contentDescription = null,
        tint = Color(0xFFD6DEFF),
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Subscription History",
          color = SubTextWhite,
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "View past payments and invoices",
          color = SubTextMuted,
          fontSize = 11.5.sp
        )
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = "Open Subscription History",
        tint = Color.White,
        modifier = Modifier.size(22.dp)
      )
    }
  }
}

@Composable
private fun GreenLeafVectorIcon(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val leafPath = Path().apply {
      moveTo(w * 0.18f, h * 0.82f)
      cubicTo(w * 0.14f, h * 0.35f, w * 0.48f, h * 0.14f, w * 0.86f, h * 0.16f)
      cubicTo(w * 0.86f, h * 0.56f, w * 0.62f, h * 0.86f, w * 0.18f, h * 0.82f)
      close()
    }
    drawPath(
      path = leafPath,
      color = SubCheckGreen,
      style = Stroke(width = w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawLine(
      color = SubCheckGreen,
      start = Offset(w * 0.16f, h * 0.84f),
      end = Offset(w * 0.64f, h * 0.38f),
      strokeWidth = w * 0.09f,
      cap = StrokeCap.Round
    )
  }
}

@Composable
private fun GoldCrownVectorIcon(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val crownPath = Path().apply {
      moveTo(w * 0.16f, h * 0.78f)
      lineTo(w * 0.10f, h * 0.32f)
      lineTo(w * 0.34f, h * 0.52f)
      lineTo(w * 0.50f, h * 0.22f)
      lineTo(w * 0.66f, h * 0.52f)
      lineTo(w * 0.90f, h * 0.32f)
      lineTo(w * 0.84f, h * 0.78f)
      close()
    }
    drawPath(
      path = crownPath,
      color = Color(0xFFFFCA28),
      style = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
  }
}

@Composable
private fun AiChipBadgeIcon(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .border(1.4.dp, Color(0xFFFF59B8), RoundedCornerShape(5.dp)),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "AI",
      color = Color(0xFFFF6EC4),
      fontSize = 9.5.sp,
      fontWeight = FontWeight.ExtraBold
    )
  }
}

@Composable
private fun FourKFrameBadgeIcon(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .border(1.4.dp, Color(0xFFC8D2FF), RoundedCornerShape(4.dp)),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "4K",
      color = Color(0xFFDCE4FF),
      fontSize = 9.5.sp,
      fontWeight = FontWeight.ExtraBold
    )
  }
}
