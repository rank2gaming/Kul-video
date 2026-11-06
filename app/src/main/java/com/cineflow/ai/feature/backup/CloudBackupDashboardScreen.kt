package com.cineflow.ai.feature.backup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartDisplay
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
import java.util.Locale

private val DashboardBgDeep = Color(0xFF0A0A26)
private val DashboardCardBg = Color(0xFF13173A)
private val DashboardCardBorder = Color(0xFF474E88)
private val DashboardTextWhite = Color(0xFFFFFFFF)
private val DashboardTextMuted = Color(0xFFB7BEDE)
private val DashboardDividerColor = Color(0xFF282F5E)
private val CompletedBadgeBg = Color(0xFF0E3D34)
private val CompletedBadgeBorder = Color(0xFF1E7E62)
private val CompletedBadgeGreen = Color(0xFF2EDB58)

/**
 * Cloud Backup & Restore Main Dashboard (`CineFlow AI Cloud Backup Dashboard.png` & Master Prompt Section 12).
 */
@Composable
fun CloudBackupDashboardScreen(
  viewModel: CloudBackupDashboardViewModel,
  onBack: () -> Unit,
  onNavigateToSelectBackupData: () -> Unit = {},
  onNavigateToRestore: (() -> Unit)? = null,
  onNavigateToUpgrade: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val storageUsedGb by viewModel.storageUsedGb.collectAsStateWithLifecycle()
  val storageQuotaGb by viewModel.storageQuotaGb.collectAsStateWithLifecycle()
  val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
  val recentBackups by viewModel.recentBackups.collectAsStateWithLifecycle()
  val dialogTitle by viewModel.dialogTitle.collectAsStateWithLifecycle()
  val dialogMessage by viewModel.dialogMessage.collectAsStateWithLifecycle()

  val progressFraction = (storageUsedGb / storageQuotaGb.coerceAtLeast(1f)).coerceIn(0f, 1f)
  val formattedUsed = String.format(Locale.US, "%.1f GB", storageUsedGb)
  val formattedQuota = String.format(Locale.US, "%.0f GB", storageQuotaGb)

  BackHandler {
    onBack()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(DashboardBgDeep)
      .testTag("cloud_backup_dashboard_screen"),
    containerColor = DashboardBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(DashboardBgDeep),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Hero Header (`img_cloud_backup_dashboard_top_hero.png`)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(941f / 292f)
          .testTag("cloud_backup_dashboard_top_hero")
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_cloud_backup_dashboard_top_hero),
          contentDescription = "CineFlow AI Cloud Backup & Restore",
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
            .testTag("cloud_backup_dashboard_back_button")
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 2. Card 1: Cloud Storage Card (`cloud_backup_dashboard_storage_card`)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(DashboardCardBg)
          .border(
            width = 1.2.dp,
            brush = Brush.horizontalGradient(
              colors = listOf(Color(0xFF48A5FF), Color(0xFF6B4BFF))
            ),
            shape = RoundedCornerShape(18.dp)
          )
          .padding(horizontal = 14.dp, vertical = 12.dp)
          .testTag("cloud_backup_dashboard_storage_card"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left circular glowing purple-blue cloud icon
        Box(
          modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(Color(0xFF6F56EC), Color(0xFF342A78))
              )
            )
            .border(1.dp, Color(0xFF8C7DFF), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.CloudQueue,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Middle Column: Title, Usage text, Progress bar
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Cloud Storage",
            color = DashboardTextWhite,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$formattedUsed of $formattedQuota Used",
            color = DashboardTextMuted,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.testTag("cloud_backup_dashboard_storage_usage_text")
          )
          Spacer(modifier = Modifier.height(7.dp))

          // Progress Bar
          Box(
            modifier = Modifier
              .fillMaxWidth(0.92f)
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(Color(0xFF393E6A))
              .testTag("cloud_backup_dashboard_storage_progress_bar")
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(progressFraction)
                .fillMaxHeight()
                .clip(RoundedCornerShape(5.dp))
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(
                      Color(0xFFE03BFF),
                      Color(0xFF7B5EFF),
                      Color(0xFF1CF0FF)
                    )
                  )
                )
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Pill Button: `👑 Upgrade`
        Row(
          modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E1A4A))
            .border(
              width = 1.2.dp,
              brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFD03BFF), Color(0xFF8C4DFF))
              ),
              shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onNavigateToUpgrade)
            .padding(horizontal = 12.dp)
            .testTag("cloud_backup_dashboard_upgrade_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          DashboardGoldCrownIcon(modifier = Modifier.size(15.dp))
          Text(
            text = "Upgrade",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Category Filter Pills Row (`All (48)`, `Projects (32)`, `Assets (10)`, `Settings (6)`)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .testTag("cloud_backup_dashboard_filter_row"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        BackupDashboardFilter.entries.forEach { filter ->
          val isSelected = selectedFilter == filter
          Row(
            modifier = Modifier
              .weight(1f)
              .height(38.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (isSelected) {
                  Brush.horizontalGradient(
                    colors = listOf(Color(0xFFC82BFF), Color(0xFF6B38FF))
                  )
                } else {
                  Brush.horizontalGradient(
                    colors = listOf(Color(0xFF15193E), Color(0xFF15193E))
                  )
                }
              )
              .border(
                width = 1.1.dp,
                color = if (isSelected) Color(0xFFE58CFF) else Color(0xFF424A82),
                shape = RoundedCornerShape(12.dp)
              )
              .clickable { viewModel.selectFilter(filter) }
              .semantics { selected = isSelected }
              .padding(horizontal = 6.dp)
              .testTag(filter.testTag),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            when (filter) {
              BackupDashboardFilter.ALL -> Icon(
                imageVector = Icons.Outlined.Layers,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
              BackupDashboardFilter.PROJECTS -> Icon(
                imageVector = Icons.Outlined.SmartDisplay,
                contentDescription = null,
                tint = Color(0xFFB7BEDE),
                modifier = Modifier.size(14.dp)
              )
              BackupDashboardFilter.ASSETS -> Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = Color(0xFFB7BEDE),
                modifier = Modifier.size(14.dp)
              )
              BackupDashboardFilter.SETTINGS -> Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = Color(0xFFB7BEDE),
                modifier = Modifier.size(14.dp)
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = filter.label,
              color = Color.White,
              fontSize = 11.sp,
              maxLines = 1,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4. Card 2: Back Up to Cloud Card (`cloud_backup_dashboard_backup_card`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(DashboardCardBg)
          .border(
            width = 1.2.dp,
            color = DashboardCardBorder,
            shape = RoundedCornerShape(18.dp)
          )
          .padding(horizontal = 14.dp, vertical = 12.dp)
          .testTag("cloud_backup_dashboard_backup_card")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(Color(0xFF1B2B56)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.CloudUpload,
              contentDescription = null,
              tint = Color(0xFF7BE8FF),
              modifier = Modifier.size(28.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Back Up to Cloud",
              color = DashboardTextWhite,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Safely save your projects, assets and settings\nto your CineFlow AI cloud.",
              color = DashboardTextMuted,
              fontSize = 12.sp,
              lineHeight = 15.5.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Full-width Gradient CTA: `☁↑ Back Up Now`
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
              Brush.horizontalGradient(
                colors = listOf(
                  Color(0xFFD82DFF),
                  Color(0xFF6B54FF),
                  Color(0xFF00E0FF)
                )
              )
            )
            .border(
              width = 1.dp,
              brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFF39AFF), Color(0xFF8CF5FF))
              ),
              shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onNavigateToSelectBackupData)
            .testTag("cloud_backup_dashboard_backup_now_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.CloudUpload,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
            Text(
              text = "Back Up Now",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 5. Card 3: Restore from Cloud Card (`cloud_backup_dashboard_restore_card`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(DashboardCardBg)
          .border(
            width = 1.2.dp,
            color = DashboardCardBorder,
            shape = RoundedCornerShape(18.dp)
          )
          .padding(horizontal = 14.dp, vertical = 12.dp)
          .testTag("cloud_backup_dashboard_restore_card")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(Color(0xFF1A224A)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.CloudDownload,
              contentDescription = null,
              tint = Color(0xFFDCE4FF),
              modifier = Modifier.size(28.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Restore from Cloud",
              color = DashboardTextWhite,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Bring back your projects, assets and settings\nto this device.",
              color = DashboardTextMuted,
              fontSize = 12.sp,
              lineHeight = 15.5.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Full-width Outlined Pill CTA: `☁↓ Restore Now`
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF121536))
            .border(
              width = 1.3.dp,
              brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFD438FF), Color(0xFF9B38FF))
              ),
              shape = RoundedCornerShape(22.dp)
            )
            .clickable {
              if (onNavigateToRestore != null) {
                onNavigateToRestore()
              } else {
                viewModel.onOpenRestoreModal()
              }
            }
            .testTag("cloud_backup_dashboard_restore_now_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.CloudDownload,
              contentDescription = null,
              tint = Color(0xFFE58CFF),
              modifier = Modifier.size(22.dp)
            )
            Text(
              text = "Restore Now",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 6. Card 4: Recent Backups Card (`cloud_backup_dashboard_recent_card`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(DashboardCardBg)
          .border(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
              colors = listOf(Color(0xFF6B4BFF), DashboardCardBorder)
            ),
            shape = RoundedCornerShape(18.dp)
          )
          .padding(horizontal = 14.dp, vertical = 11.dp)
          .testTag("cloud_backup_dashboard_recent_card")
      ) {
        // Header Row: History icon + Recent Backups | View All >
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.History,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "Recent Backups",
              color = DashboardTextWhite,
              fontSize = 15.5.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { viewModel.onViewAllRecentBackups() }
              .padding(horizontal = 4.dp, vertical = 2.dp)
              .testTag("cloud_backup_dashboard_view_all_button"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "View All",
              color = Color.White,
              fontSize = 12.2.sp,
              fontWeight = FontWeight.Medium
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = "View All Recent Backups",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        DashboardDivider()

        recentBackups.forEachIndexed { index, entry ->
          RecentBackupRow(
            entry = entry,
            onRowClick = { viewModel.onOpenBackupEntryOptions(entry) },
            onMenuClick = { viewModel.onOpenBackupEntryOptions(entry) }
          )
          if (index < recentBackups.lastIndex) {
            DashboardDivider()
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }

  if (dialogTitle != null && dialogMessage != null) {
    AlertDialog(
      onDismissRequest = { viewModel.dismissDialog() },
      containerColor = Color(0xFF15193B),
      titleContentColor = DashboardTextWhite,
      textContentColor = DashboardTextMuted,
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
      confirmButton = {
        TextButton(
          onClick = { viewModel.dismissDialog() },
          modifier = Modifier.testTag("cloud_backup_dashboard_dialog_close_button")
        ) {
          Text(
            text = "Close",
            color = Color(0xFF36D6FF),
            fontWeight = FontWeight.Bold
          )
        }
      },
      modifier = Modifier.testTag("cloud_backup_dashboard_status_dialog")
    )
  }
}

@Composable
private fun RecentBackupRow(
  entry: RecentBackupEntry,
  onRowClick: () -> Unit,
  onMenuClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onRowClick)
      .padding(vertical = 8.dp)
      .testTag(entry.rowTestTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left circular icon
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(
          when (entry.iconType) {
            RecentBackupIconType.CLAPPERBOARD -> Color(0xFF2E2268)
            RecentBackupIconType.FOLDER -> Color(0xFF1D2452)
            RecentBackupIconType.SETTINGS_GEAR -> Color(0xFF1D2452)
          }
        ),
      contentAlignment = Alignment.Center
    ) {
      when (entry.iconType) {
        RecentBackupIconType.CLAPPERBOARD -> Icon(
          imageVector = Icons.Outlined.MovieCreation,
          contentDescription = null,
          tint = Color(0xFFE0B8FF),
          modifier = Modifier.size(20.dp)
        )
        RecentBackupIconType.FOLDER -> Icon(
          imageVector = Icons.Outlined.Folder,
          contentDescription = null,
          tint = Color(0xFFD6DEFF),
          modifier = Modifier.size(20.dp)
        )
        RecentBackupIconType.SETTINGS_GEAR -> Icon(
          imageVector = Icons.Outlined.Settings,
          contentDescription = null,
          tint = Color(0xFFD6DEFF),
          modifier = Modifier.size(20.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(10.dp))

    // Title & Subtitle
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = entry.title,
        color = DashboardTextWhite,
        fontSize = 13.8.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = entry.subtitle,
        color = DashboardTextMuted,
        fontSize = 11.2.sp
      )
    }

    Spacer(modifier = Modifier.width(6.dp))

    // Green `Completed` Pill Badge
    Row(
      modifier = Modifier
        .height(24.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(CompletedBadgeBg)
        .border(1.dp, CompletedBadgeBorder, RoundedCornerShape(12.dp))
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      Box(
        modifier = Modifier
          .size(13.dp)
          .clip(CircleShape)
          .background(CompletedBadgeGreen),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.Check,
          contentDescription = null,
          tint = Color(0xFF062314),
          modifier = Modifier.size(9.dp)
        )
      }
      Text(
        text = entry.statusLabel,
        color = Color.White,
        fontSize = 10.8.sp,
        fontWeight = FontWeight.SemiBold
      )
    }

    Spacer(modifier = Modifier.width(4.dp))

    // 3-Dot Vertical Menu (`⋮`)
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .clickable(onClick = onMenuClick)
        .testTag(entry.menuTestTag),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.MoreVert,
        contentDescription = "Options for ${entry.title}",
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
private fun DashboardDivider() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(1.dp)
      .background(DashboardDividerColor)
  )
}

@Composable
private fun DashboardGoldCrownIcon(modifier: Modifier = Modifier) {
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
