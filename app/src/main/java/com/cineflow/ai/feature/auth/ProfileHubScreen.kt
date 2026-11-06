package com.cineflow.ai.feature.auth

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R
import java.util.Locale

private val HubBgDeep = Color(0xFF0A081E)
private val HubCardDark = Color(0xFF121638)
private val HubCardBorder = Color(0xFF32396B)
private val HubTextWhite = Color(0xFFFFFFFF)
private val HubTextMuted = Color(0xFFB3B8DA)
private val HubGoldCrown = Color(0xFFFACC15)
private val HubEmeraldDot = Color(0xFF22C55E)
private val HubCrimsonLogout = Color(0xFFFF4D6D)

/**
 * Account / Profile Hub Screen (`CineFlow AI Neon Profile Dashboard.png` & Master Prompt Section 6 & Section 10).
 */
@Composable
fun ProfileHubScreen(
  viewModel: ProfileHubViewModel,
  onBack: () -> Unit,
  onLogOut: () -> Unit,
  onNavigateToSection: (ProfileHubSection) -> Unit = { viewModel.openSection(it) },
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current

  val displayName by viewModel.displayName.collectAsStateWithLifecycle()
  val username by viewModel.username.collectAsStateWithLifecycle()
  val publicUid by viewModel.publicUid.collectAsStateWithLifecycle()
  val planBadgeText by viewModel.planBadgeText.collectAsStateWithLifecycle()
  val statusBadgeText by viewModel.statusBadgeText.collectAsStateWithLifecycle()
  val projectsCount by viewModel.projectsCount.collectAsStateWithLifecycle()
  val backupsCount by viewModel.backupsCount.collectAsStateWithLifecycle()
  val usedStorageGb by viewModel.usedStorageGb.collectAsStateWithLifecycle()
  val totalStorageGb by viewModel.totalStorageGb.collectAsStateWithLifecycle()
  val copiedUidFeedback by viewModel.copiedUidFeedback.collectAsStateWithLifecycle()
  val activeSectionModal by viewModel.activeSectionModal.collectAsStateWithLifecycle()

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      viewModel.onAvatarSelected(uri.toString())
    }
  }

  BackHandler {
    onBack()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(HubBgDeep)
      .testTag("profile_hub_screen"),
    containerColor = HubBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(HubBgDeep),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Header & Cinema Wave Banner (`img_profile_hub_top_banner.png`)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(941f / 195f)
          .testTag("profile_hub_top_banner")
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_profile_hub_top_banner),
          contentDescription = "CineFlow AI — My Account",
          contentScale = ContentScale.FillWidth,
          modifier = Modifier.fillMaxSize()
        )

        // Left Back arrow hotspot (`←`)
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 10.dp, top = 8.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onBack)
            .testTag("profile_hub_back_button")
        )

        // Right Settings Gear hotspot (`⚙`)
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(end = 10.dp, top = 8.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onNavigateToSection(ProfileHubSection.ACCOUNT_SETTINGS) }
            .testTag("profile_hub_settings_button")
        )
      }

      // 2. Profile Identity Hero Section (`profile_hub_identity_section`)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .testTag("profile_hub_identity_section"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Left: Creator Circular Avatar + Bottom-Right Camera Picker Badge
        Box(
          modifier = Modifier
            .size(94.dp)
            .clickable {
              runCatching {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              }.onFailure {
                viewModel.onAvatarSelected("avatar://creator_updated")
              }
            }
            .testTag("profile_hub_avatar_camera_button")
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_profile_hub_avatar),
            contentDescription = "$displayName Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        // Right: Display Name + Verified Badge, @username + Public User ID Pill, Subscriber + Active Pills
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Row 1: Display Name + Purple Verified Checkmark Starburst
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = displayName,
              color = HubTextWhite,
              fontSize = 21.sp,
              fontWeight = FontWeight.ExtraBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("profile_hub_display_name")
            )
            Box(
              modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    colors = listOf(Color(0xFF9333EA), Color(0xFF6366F1))
                  )
                )
                .border(1.dp, Color(0xFFC084FC), CircleShape)
                .testTag("profile_hub_verified_badge"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Verified Account",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
            }
          }

          // Row 2: @username + Public User ID Pill (`CF...` with copy button)
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "@$username",
              color = HubTextMuted,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Medium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .weight(1f, fill = false)
                .testTag("profile_hub_username")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Public User ID Pill (`CF` + 8 digits + copy icon)
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A204C))
                .border(1.dp, Color(0xFF6366F1), RoundedCornerShape(16.dp))
                .clickable {
                  val uid = viewModel.copyPublicUid()
                  runCatching {
                    clipboardManager.setText(AnnotatedString(uid))
                  }
                }
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .testTag("profile_hub_public_uid_badge"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = publicUid,
                color = HubTextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
              Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "Copy Public User ID",
                tint = Color(0xFFD8DCFF),
                modifier = Modifier
                  .size(14.dp)
                  .testTag("profile_hub_copy_uid_button")
              )
            }
          }

          // Row 3: `[👑 Subscriber]` + `[● Active]` Status Pills
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Subscriber Pill
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(Color(0xFF3E1E68), Color(0xFF2B1C54))
                  )
                )
                .border(1.dp, Color(0xFF9333EA), RoundedCornerShape(16.dp))
                .padding(horizontal = 11.dp, vertical = 4.dp)
                .testTag("profile_hub_subscriber_badge"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.WorkspacePremium,
                contentDescription = null,
                tint = HubGoldCrown,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = planBadgeText,
                color = HubTextWhite,
                fontSize = 11.8.sp,
                fontWeight = FontWeight.SemiBold
              )
            }

            // Active Pill
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0B3B2E))
                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
                .padding(horizontal = 11.dp, vertical = 4.dp)
                .testTag("profile_hub_active_badge"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(HubEmeraldDot)
              )
              Text(
                text = statusBadgeText,
                color = HubTextWhite,
                fontSize = 11.8.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      if (!copiedUidFeedback.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = copiedUidFeedback!!,
          color = Color(0xFF47E5BC),
          fontSize = 11.5.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.testTag("profile_hub_copied_feedback")
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. 3-Column Stats Bar Card (`48 Projects` | `12 Backups` | `2.6 GB of 5 GB Used`)
      val usedGbFormatted = String.format(Locale.US, "%.1f GB", usedStorageGb)
      val totalGbFormatted = String.format(Locale.US, "%.0f GB", totalStorageGb)

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
          .height(IntrinsicSize.Min)
          .clip(RoundedCornerShape(18.dp))
          .background(HubCardDark)
          .border(1.dp, HubCardBorder, RoundedCornerShape(18.dp))
          .padding(horizontal = 12.dp, vertical = 12.dp)
          .testTag("profile_hub_stats_card"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        ProfileStatColumn(
          icon = Icons.Outlined.SmartDisplay,
          iconTint = Color(0xFF818CF8),
          iconBg = Color(0xFF1E2252),
          valueText = projectsCount.toString(),
          labelText = "Projects",
          testTag = "profile_hub_stat_projects",
          modifier = Modifier.weight(1f)
        )

        Box(
          modifier = Modifier
            .width(1.dp)
            .fillMaxHeight(0.75f)
            .background(Color(0xFF282F5E))
        )

        ProfileStatColumn(
          icon = Icons.Outlined.CloudUpload,
          iconTint = Color(0xFF38BDF8),
          iconBg = Color(0xFF162C52),
          valueText = backupsCount.toString(),
          labelText = "Backups",
          testTag = "profile_hub_stat_backups",
          modifier = Modifier.weight(1f)
        )

        Box(
          modifier = Modifier
            .width(1.dp)
            .fillMaxHeight(0.75f)
            .background(Color(0xFF282F5E))
        )

        ProfileStatColumn(
          icon = Icons.Outlined.Storage,
          iconTint = Color(0xFFC084FC),
          iconBg = Color(0xFF291D54),
          valueText = usedGbFormatted,
          labelText = "of $totalGbFormatted Used",
          testTag = "profile_hub_stat_storage",
          modifier = Modifier.weight(1.15f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 4. Cloud Storage Highlight Card (`profile_hub_cloud_storage_card`)
      val storageFraction = (usedStorageGb / totalStorageGb.coerceAtLeast(0.1f)).coerceIn(0f, 1f)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(
            Brush.horizontalGradient(
              colors = listOf(
                Color(0xFF2A1B6E),
                Color(0xFF1B1D52),
                Color(0xFF1D1A4E)
              )
            )
          )
          .border(
            width = 1.4.dp,
            brush = Brush.horizontalGradient(
              colors = listOf(Color(0xFF9333EA), Color(0xFF4F46E5), Color(0xFF6366F1))
            ),
            shape = RoundedCornerShape(18.dp)
          )
          .padding(horizontal = 14.dp, vertical = 13.dp)
          .testTag("profile_hub_cloud_storage_card"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                colors = listOf(Color(0xFF6D28D9), Color(0xFF4338CA))
              )
            )
            .border(1.dp, Color(0xFFA78BFA), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.CloudQueue,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "Cloud Storage",
            color = HubTextWhite,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "$usedGbFormatted / $totalGbFormatted",
            color = Color(0xFFD6DAFA),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.testTag("profile_hub_storage_usage_text")
          )
          Spacer(modifier = Modifier.height(2.dp))
          // Progress Bar (`profile_hub_storage_progress_bar`)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFF353B6E))
              .testTag("profile_hub_storage_progress_bar")
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(storageFraction)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(Color(0xFFD946EF), Color(0xFF38BDF8))
                  )
                )
            )
          }
        }

        // `Manage` Pill Button (`profile_hub_storage_manage_button`)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
              Brush.horizontalGradient(
                colors = listOf(Color(0xFF4C2E9B), Color(0xFF39277A))
              )
            )
            .border(1.dp, Color(0xFF8B5CF6), RoundedCornerShape(16.dp))
            .clickable { onNavigateToSection(ProfileHubSection.CLOUD_STORAGE_MANAGE) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("profile_hub_storage_manage_button"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Manage",
            color = HubTextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 5. Five Action Navigation Rows
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
      ) {
        ProfileActionRowCard(
          icon = Icons.Outlined.Person,
          iconTint = Color(0xFFE0E7FF),
          title = "Personal Information",
          subtitle = "Name, username, email, mobile",
          testTag = "profile_hub_row_personal_info",
          onClick = { onNavigateToSection(ProfileHubSection.PERSONAL_INFORMATION) }
        )

        ProfileActionRowCard(
          icon = Icons.Outlined.WorkspacePremium,
          iconTint = HubGoldCrown,
          title = "Subscription & Plan",
          subtitle = "Manage your subscription, storage and benefits",
          badgeText = "5 GB Plan",
          testTag = "profile_hub_row_subscription_plan",
          onClick = { onNavigateToSection(ProfileHubSection.SUBSCRIPTION_PLAN) }
        )

        ProfileActionRowCard(
          icon = Icons.Outlined.CloudSync,
          iconTint = Color(0xFF22D3EE),
          title = "Cloud Backup & Restore",
          subtitle = "Backup projects to cloud and restore on any device",
          testTag = "profile_hub_row_cloud_backup",
          onClick = { onNavigateToSection(ProfileHubSection.CLOUD_BACKUP_RESTORE) }
        )

        ProfileActionRowCard(
          icon = Icons.Outlined.VerifiedUser,
          iconTint = Color(0xFFD946EF),
          title = "Account Security",
          subtitle = "Password, account recovery and login activity",
          testTag = "profile_hub_row_account_security",
          onClick = { onNavigateToSection(ProfileHubSection.ACCOUNT_SECURITY) }
        )

        ProfileActionRowCard(
          icon = Icons.Outlined.Link,
          iconTint = Color(0xFFE0E7FF),
          title = "Linked Accounts",
          subtitle = "Google and other sign-in methods",
          testTag = "profile_hub_row_linked_accounts",
          onClick = { onNavigateToSection(ProfileHubSection.LINKED_ACCOUNTS) }
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 6. Bottom `Log Out` Pill Button (`profile_hub_logout_button`)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
          .height(52.dp)
          .clip(RoundedCornerShape(26.dp))
          .background(
            Brush.horizontalGradient(
              colors = listOf(Color(0xFF1F102B), Color(0xFF29112E), Color(0xFF1F102B))
            )
          )
          .border(
            width = 1.3.dp,
            brush = Brush.horizontalGradient(
              colors = listOf(Color(0xFFE11D48), Color(0xFFFF4D6D), Color(0xFFBE185D))
            ),
            shape = RoundedCornerShape(26.dp)
          )
          .clickable(onClick = onLogOut)
          .testTag("profile_hub_logout_button"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = null,
            tint = HubCrimsonLogout,
            modifier = Modifier.size(22.dp)
          )
          Text(
            text = "Log Out",
            color = HubCrimsonLogout,
            fontSize = 16.5.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))
    }
  }

  // Section Details Modal (when a row or Manage button is tapped)
  activeSectionModal?.let { section ->
    AlertDialog(
      onDismissRequest = { viewModel.dismissSection() },
      containerColor = Color(0xFF15193B),
      title = {
        Text(
          text = section.title,
          color = HubTextWhite,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier.testTag("profile_hub_section_dialog"),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = section.subtitle,
            color = HubTextMuted,
            fontSize = 13.5.sp
          )
          Text(
            text = "Account: $displayName (@$username) • Public ID: $publicUid",
            color = Color(0xFF38BDF8),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.dismissSection() },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
          modifier = Modifier.testTag("profile_hub_section_dialog_close_button")
        ) {
          Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

@Composable
private fun ProfileStatColumn(
  icon: ImageVector,
  iconTint: Color,
  iconBg: Color,
  valueText: String,
  labelText: String,
  testTag: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .padding(horizontal = 4.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(iconBg)
        .border(1.dp, iconTint.copy(alpha = 0.45f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(20.dp)
      )
    }

    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
      Text(
        text = valueText,
        color = HubTextWhite,
        fontSize = 16.5.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1
      )
      Text(
        text = labelText,
        color = HubTextMuted,
        fontSize = 10.8.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun ProfileActionRowCard(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  testTag: String,
  badgeText: String? = null,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(HubCardDark)
      .border(1.dp, HubCardBorder, RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 12.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(13.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(25.dp)
    )

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = title,
        color = HubTextWhite,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = subtitle,
        color = HubTextMuted,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    if (!badgeText.isNullOrBlank()) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF0D4A38))
          .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
          .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = badgeText,
          color = HubTextWhite,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
      contentDescription = null,
      tint = Color(0xFFD1D5F0),
      modifier = Modifier.size(21.dp)
    )
  }
}
