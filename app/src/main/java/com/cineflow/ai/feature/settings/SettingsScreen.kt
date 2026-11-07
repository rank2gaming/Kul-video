package com.cineflow.ai.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.VideoFile
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R

private val SettingsBgDeep = Color(0xFF0A0A26)
private val SettingsCardBg = Color(0xFF13173A)
private val SettingsCardBorder = Color(0xFF474E88)
private val SettingsTextWhite = Color(0xFFFFFFFF)
private val SettingsTextMuted = Color(0xFFB7BEDE)
private val SettingsDividerColor = Color(0xFF282F5E)
private val SettingsToggleActive = Color(0xFFA032FF)
private val SettingsToggleInactive = Color(0xFF3E4574)
private val SettingsPlanBadgeBg = Color(0xFF0E3D34)
private val SettingsPlanBadgeBorder = Color(0xFF1E7E62)

/**
 * Settings Screen (`CineFlow AI Neon Settings Dashboard.png` & Master Prompt Section 6 Item 9).
 */
@Composable
fun SettingsScreen(
  viewModel: SettingsViewModel,
  onBack: () -> Unit,
  onNavigateToCloudBackup: () -> Unit = {},
  onNavigateToSubscriptionPlan: () -> Unit = {},
  onNavigateToNotifications: (() -> Unit)? = null,
  onNavigateToAccountSecurity: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
  val appearance by viewModel.appearance.collectAsStateWithLifecycle()
  val hapticFeedbackEnabled by viewModel.hapticFeedbackEnabled.collectAsStateWithLifecycle()
  val previewQuality by viewModel.previewQuality.collectAsStateWithLifecycle()
  val defaultResolution by viewModel.defaultExportResolution.collectAsStateWithLifecycle()
  val defaultFps by viewModel.defaultFps.collectAsStateWithLifecycle()
  val defaultCodec by viewModel.defaultVideoCodec.collectAsStateWithLifecycle()
  val defaultBitrate by viewModel.defaultBitrate.collectAsStateWithLifecycle()
  val autoSaveEnabled by viewModel.autoSaveEnabled.collectAsStateWithLifecycle()
  val cacheSizeLabel by viewModel.cacheSizeLabel.collectAsStateWithLifecycle()
  val storageLocation by viewModel.storageLocation.collectAsStateWithLifecycle()
  val planBadgeLabel by viewModel.planBadgeLabel.collectAsStateWithLifecycle()
  val dialogTitle by viewModel.dialogTitle.collectAsStateWithLifecycle()
  val dialogMessage by viewModel.dialogMessage.collectAsStateWithLifecycle()

  BackHandler {
    onBack()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(SettingsBgDeep)
      .testTag("settings_screen"),
    containerColor = SettingsBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(SettingsBgDeep),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Hero Header (`img_settings_top_hero.png`)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(941f / 136f)
          .testTag("settings_top_hero")
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_settings_top_hero),
          contentDescription = "CineFlow AI Settings",
          contentScale = ContentScale.FillWidth,
          modifier = Modifier.fillMaxSize()
        )

        // Top-left Back arrow hotspot (`←`)
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 10.dp, top = 4.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onBack)
            .testTag("settings_back_button")
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // 2. Section 1: `⚙ General`
      SettingsSectionHeader(
        icon = Icons.Outlined.Settings,
        iconTint = Color(0xFFB7C4FF),
        title = "General",
        testTag = "settings_section_general"
      )

      SettingsGroupCard(testTag = "settings_card_general") {
        // Row 1: App Language | English >
        SettingsValueRow(
          icon = Icons.Outlined.Language,
          iconTint = Color(0xFF9AC5FF),
          title = "App Language",
          valueText = appLanguage,
          onClick = { viewModel.cycleAppLanguage() },
          testTag = "settings_row_app_language"
        )
        SettingsDivider()

        // Row 2: Appearance | [Light | Dark | System]
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
            .testTag("settings_row_appearance"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Contrast,
              contentDescription = null,
              tint = Color(0xFF9AC5FF),
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "Appearance",
              color = SettingsTextWhite,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // 3-segment pill selector (Light / Dark / System)
          Row(
            modifier = Modifier
              .height(28.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF1A1E46))
              .border(1.dp, Color(0xFF424A82), RoundedCornerShape(14.dp))
              .testTag("settings_appearance_selector"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            SettingsAppearance.entries.forEach { mode ->
              val isSelected = appearance == mode
              Box(
                modifier = Modifier
                  .height(26.dp)
                  .clip(RoundedCornerShape(13.dp))
                  .background(
                    if (isSelected) {
                      Brush.horizontalGradient(
                        colors = listOf(Color(0xFFB438FF), Color(0xFF7B38FF))
                      )
                    } else {
                      Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.Transparent)
                      )
                    }
                  )
                  .clickable { viewModel.selectAppearance(mode) }
                  .semantics { selected = isSelected }
                  .padding(horizontal = 11.dp)
                  .testTag(mode.testTag),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = mode.label,
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              }
            }
          }
        }
        SettingsDivider()

        // Row 3: Haptic Feedback | Toggle Switch
        SettingsToggleRow(
          icon = Icons.Outlined.Vibration,
          iconTint = Color(0xFFE052FF),
          title = "Haptic Feedback",
          subtitle = null,
          checked = hapticFeedbackEnabled,
          onToggle = { viewModel.toggleHapticFeedback() },
          rowTestTag = "settings_row_haptic_feedback",
          switchTestTag = "settings_switch_haptic"
        )
        SettingsDivider()

        // Row 4: Notifications | >
        SettingsValueRow(
          icon = Icons.Outlined.Notifications,
          iconTint = Color(0xFFD6DEFF),
          title = "Notifications",
          valueText = null,
          onClick = {
            if (onNavigateToNotifications != null) {
              onNavigateToNotifications()
            } else {
              viewModel.openNotificationsModal()
            }
          },
          testTag = "settings_row_notifications"
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Section 2: `🎞 Editing`
      SettingsSectionHeader(
        icon = Icons.Outlined.Movie,
        iconTint = Color(0xFFB7C4FF),
        title = "Editing",
        testTag = "settings_section_editing"
      )

      SettingsGroupCard(testTag = "settings_card_editing") {
        SettingsValueRow(
          icon = Icons.Outlined.SmartDisplay,
          iconTint = Color(0xFFD6DEFF),
          title = "Preview Quality",
          valueText = previewQuality,
          onClick = { viewModel.cyclePreviewQuality() },
          testTag = "settings_row_preview_quality"
        )
        SettingsDivider()

        SettingsValueRow(
          icon = Icons.Outlined.Hd,
          iconTint = Color(0xFFD6DEFF),
          title = "Default Export Resolution",
          valueText = defaultResolution,
          onClick = { viewModel.cycleDefaultResolution() },
          testTag = "settings_row_default_resolution"
        )
        SettingsDivider()

        SettingsValueRow(
          icon = Icons.Outlined.Layers,
          iconTint = Color(0xFFD6DEFF),
          title = "Default Frame Rate (FPS)",
          valueText = defaultFps,
          onClick = { viewModel.cycleDefaultFps() },
          testTag = "settings_row_default_fps"
        )
        SettingsDivider()

        SettingsValueRow(
          icon = Icons.Outlined.VideoFile,
          iconTint = Color(0xFFD6DEFF),
          title = "Default Video Codec",
          valueText = defaultCodec,
          onClick = { viewModel.cycleDefaultCodec() },
          testTag = "settings_row_default_codec"
        )
        SettingsDivider()

        SettingsValueRow(
          icon = Icons.Outlined.Tune,
          iconTint = Color(0xFFD6DEFF),
          title = "Default Bitrate",
          valueText = defaultBitrate,
          onClick = { viewModel.cycleDefaultBitrate() },
          testTag = "settings_row_default_bitrate"
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 4. Section 3: `🗄 Project & Storage`
      SettingsSectionHeader(
        icon = Icons.Outlined.Storage,
        iconTint = Color(0xFFB7C4FF),
        title = "Project & Storage",
        testTag = "settings_section_project_storage"
      )

      SettingsGroupCard(testTag = "settings_card_project_storage") {
        // Row 1: Auto Save | Automatically save your progress | Toggle Switch
        SettingsToggleRow(
          icon = Icons.Outlined.Sync,
          iconTint = Color(0xFFFF42D8),
          title = "Auto Save",
          subtitle = "Automatically save your progress",
          checked = autoSaveEnabled,
          onToggle = { viewModel.toggleAutoSave() },
          rowTestTag = "settings_row_auto_save",
          switchTestTag = "settings_switch_auto_save"
        )
        SettingsDivider()

        // Row 2: Project Recovery | Recover unsaved or crashed projects | >
        SettingsSubtitleNavRow(
          icon = Icons.Outlined.History,
          iconTint = Color(0xFF00E5FF),
          title = "Project Recovery",
          subtitle = "Recover unsaved or crashed projects",
          badgeText = null,
          onClick = { viewModel.openProjectRecoveryModal() },
          testTag = "settings_row_project_recovery"
        )
        SettingsDivider()

        // Row 3: Cache Management | 2.1 GB >
        SettingsValueRow(
          icon = Icons.Outlined.CleaningServices,
          iconTint = Color(0xFFD6DEFF),
          title = "Cache Management",
          valueText = cacheSizeLabel,
          valueTestTag = "settings_cache_size_label",
          onClick = { viewModel.openCacheManagementModal() },
          testTag = "settings_row_cache_management"
        )
        SettingsDivider()

        // Row 4: Storage Location | Internal Storage >
        SettingsValueRow(
          icon = Icons.Outlined.PhoneAndroid,
          iconTint = Color(0xFFD6DEFF),
          title = "Storage Location",
          valueText = storageLocation,
          onClick = { viewModel.cycleStorageLocation() },
          testTag = "settings_row_storage_location"
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 5. Section 4: `☁↑ Cloud & Account`
      SettingsSectionHeader(
        icon = Icons.Outlined.CloudUpload,
        iconTint = Color(0xFFB7C4FF),
        title = "Cloud & Account",
        testTag = "settings_section_cloud_account"
      )

      SettingsGroupCard(testTag = "settings_card_cloud_account") {
        // Row 1: Cloud Backup | Backup projects to your cloud storage | >
        SettingsSubtitleNavRow(
          icon = Icons.Outlined.CloudUpload,
          iconTint = Color(0xFF7BE8FF),
          title = "Cloud Backup",
          subtitle = "Backup projects to your cloud storage",
          badgeText = null,
          onClick = onNavigateToCloudBackup,
          testTag = "settings_row_cloud_backup"
        )
        SettingsDivider()

        // Row 2: Subscription & Plan | Manage your plan and storage | [5 GB Plan] >
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToSubscriptionPlan)
            .padding(vertical = 8.dp)
            .testTag("settings_row_subscription_plan"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          SettingsGoldCrownIcon(modifier = Modifier.size(21.dp))
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Subscription & Plan",
              color = SettingsTextWhite,
              fontSize = 13.8.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Manage your plan and storage",
              color = SettingsTextMuted,
              fontSize = 11.2.sp
            )
          }

          Box(
            modifier = Modifier
              .height(22.dp)
              .clip(RoundedCornerShape(11.dp))
              .background(SettingsPlanBadgeBg)
              .border(1.dp, SettingsPlanBadgeBorder, RoundedCornerShape(11.dp))
              .padding(horizontal = 9.dp)
              .testTag("settings_plan_badge"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = planBadgeLabel,
              color = Color.White,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
        SettingsDivider()

        // Row 3: Account Security | Password, recovery and linked accounts | >
        SettingsSubtitleNavRow(
          icon = Icons.Outlined.VerifiedUser,
          iconTint = Color(0xFFE052FF),
          title = "Account Security",
          subtitle = "Password, recovery and linked accounts",
          badgeText = null,
          onClick = {
            if (onNavigateToAccountSecurity != null) {
              onNavigateToAccountSecurity()
            } else {
              viewModel.openAccountSecurityModal()
            }
          },
          testTag = "settings_row_account_security"
        )
      }

      Spacer(modifier = Modifier.height(9.dp))

      // 6. Standalone Card 5: `✦ AI Settings`
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(SettingsCardBg)
          .border(
            width = 1.2.dp,
            brush = Brush.horizontalGradient(
              colors = listOf(Color(0xFF7B4BFF), Color(0xFFB438FF))
            ),
            shape = RoundedCornerShape(16.dp)
          )
          .clickable { viewModel.openAiSettingsModal() }
          .padding(horizontal = 14.dp, vertical = 11.dp)
          .testTag("settings_card_ai_settings"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Outlined.AutoAwesome,
          contentDescription = null,
          tint = Color(0xFFE052FF),
          modifier = Modifier.size(23.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "AI Settings",
            color = SettingsTextWhite,
            fontSize = 14.2.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Gemini AI features, captions, translation and more",
            color = SettingsTextMuted,
            fontSize = 11.2.sp
          )
        }
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(19.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 7. Standalone Card 6: `ⓘ About`
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(SettingsCardBg)
          .border(
            width = 1.1.dp,
            color = SettingsCardBorder,
            shape = RoundedCornerShape(16.dp)
          )
          .clickable { viewModel.openAboutModal() }
          .padding(horizontal = 14.dp, vertical = 11.dp)
          .testTag("settings_card_about"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Outlined.Info,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(23.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "About",
            color = SettingsTextWhite,
            fontSize = 14.2.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "App version, terms, privacy and support",
            color = SettingsTextMuted,
            fontSize = 11.2.sp
          )
        }
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(19.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }

  if (dialogTitle != null && dialogMessage != null) {
    val isCacheModal = dialogTitle!!.startsWith("Cache Management")
    AlertDialog(
      onDismissRequest = { viewModel.dismissDialog() },
      containerColor = Color(0xFF15193B),
      titleContentColor = SettingsTextWhite,
      textContentColor = SettingsTextMuted,
      title = {
        Text(
          text = dialogTitle!!,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = dialogMessage!!,
          fontSize = 13.5.sp,
          lineHeight = 19.sp
        )
      },
      dismissButton = if (isCacheModal) {
        {
          TextButton(
            onClick = { viewModel.clearCache() },
            modifier = Modifier.testTag("settings_clear_cache_confirm_button")
          ) {
            Text(
              text = "Clear Cache",
              color = Color(0xFFE052FF),
              fontWeight = FontWeight.Bold
            )
          }
        }
      } else null,
      confirmButton = {
        TextButton(
          onClick = { viewModel.dismissDialog() },
          modifier = Modifier.testTag("settings_dialog_close_button")
        ) {
          Text(
            text = "Close",
            color = Color(0xFF36D6FF),
            fontWeight = FontWeight.Bold
          )
        }
      },
      modifier = Modifier.testTag("settings_status_dialog")
    )
  }
}

@Composable
private fun SettingsSectionHeader(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  testTag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(18.dp)
    )
    Text(
      text = title,
      color = SettingsTextWhite,
      fontSize = 14.5.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
private fun SettingsGroupCard(
  testTag: String,
  content: @Composable () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(SettingsCardBg)
      .border(
        width = 1.1.dp,
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF6B4BFF), SettingsCardBorder)
        ),
        shape = RoundedCornerShape(16.dp)
      )
      .padding(horizontal = 14.dp, vertical = 4.dp)
      .testTag(testTag)
  ) {
    content()
  }
}

@Composable
private fun SettingsValueRow(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  valueText: String?,
  valueTestTag: String? = null,
  onClick: () -> Unit,
  testTag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.weight(1f)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(20.dp)
      )
      Text(
        text = title,
        color = SettingsTextWhite,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.SemiBold
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      if (valueText != null) {
        Text(
          text = valueText,
          color = SettingsTextWhite,
          fontSize = 12.2.sp,
          fontWeight = FontWeight.Medium,
          modifier = if (valueTestTag != null) Modifier.testTag(valueTestTag) else Modifier
        )
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
private fun SettingsSubtitleNavRow(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  badgeText: String?,
  onClick: () -> Unit,
  testTag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(21.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        color = SettingsTextWhite,
        fontSize = 13.8.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = subtitle,
        color = SettingsTextMuted,
        fontSize = 11.2.sp
      )
    }

    if (badgeText != null) {
      Text(
        text = badgeText,
        color = SettingsTextWhite,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.width(4.dp))
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(18.dp)
    )
  }
}

@Composable
private fun SettingsToggleRow(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String?,
  checked: Boolean,
  onToggle: () -> Unit,
  rowTestTag: String,
  switchTestTag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onToggle)
      .padding(vertical = 7.dp)
      .testTag(rowTestTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.weight(1f)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(20.dp)
      )
      Column {
        Text(
          text = title,
          color = SettingsTextWhite,
          fontSize = 13.5.sp,
          fontWeight = if (subtitle != null) FontWeight.Bold else FontWeight.SemiBold
        )
        if (subtitle != null) {
          Text(
            text = subtitle,
            color = SettingsTextMuted,
            fontSize = 11.2.sp
          )
        }
      }
    }

    // Purple Toggle Switch
    Box(
      modifier = Modifier
        .width(42.dp)
        .height(23.dp)
        .clip(RoundedCornerShape(11.5.dp))
        .background(if (checked) SettingsToggleActive else SettingsToggleInactive)
        .padding(3.dp)
        .semantics { selected = checked }
        .testTag(switchTestTag),
      contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
      Box(
        modifier = Modifier
          .size(17.dp)
          .clip(CircleShape)
          .background(Color.White)
      )
    }
  }
}

@Composable
private fun SettingsDivider() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(1.dp)
      .background(SettingsDividerColor)
  )
}

@Composable
private fun SettingsGoldCrownIcon(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
      moveTo(w * 0.10f, h * 0.80f)
      lineTo(w * 0.05f, h * 0.28f)
      lineTo(w * 0.32f, h * 0.52f)
      lineTo(w * 0.50f, h * 0.16f)
      lineTo(w * 0.68f, h * 0.52f)
      lineTo(w * 0.95f, h * 0.28f)
      lineTo(w * 0.90f, h * 0.80f)
      close()
    }
    drawPath(
      path = path,
      color = Color(0xFFFFCA28),
      style = Stroke(
        width = 1.6.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
      )
    )
  }
}
