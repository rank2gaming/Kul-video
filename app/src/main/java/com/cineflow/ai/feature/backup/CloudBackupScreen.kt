package com.cineflow.ai.feature.backup

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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R
import java.util.Locale

private val BackupBgDeep = Color(0xFF0A0A26)
private val BackupCardBg = Color(0xFF13173A)
private val BackupCardBorder = Color(0xFF474E88)
private val BackupIconBoxBg = Color(0xFF2B2868)
private val BackupTextWhite = Color(0xFFFFFFFF)
private val BackupTextMuted = Color(0xFFB7BEDE)
private val BackupCheckPurple = Color(0xFF8B38FF)
private val BackupDividerColor = Color(0xFF282F5E)

/**
 * Cloud Backup Screen (`Neon Cloud Backup Dashboard.png` & Master Prompt Section 12).
 */
@Composable
fun CloudBackupScreen(
  viewModel: CloudBackupViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
  val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
  val manualBackupSelected by viewModel.manualBackupSelected.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
  val dialogTitle by viewModel.dialogTitle.collectAsStateWithLifecycle()
  val dialogMessage by viewModel.dialogMessage.collectAsStateWithLifecycle()

  val selectedCount = selectedCategories.size
  val totalCategories = viewModel.totalCategoriesCount
  val totalSizeGb = selectedCategories.sumOf { it.sizeGb }
  val formattedTotalSize = String.format(Locale.US, "%.2f GB", totalSizeGb)

  BackHandler {
    onBack()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(BackupBgDeep)
      .testTag("cloud_backup_screen"),
    containerColor = BackupBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(BackupBgDeep),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Hero Header (`img_cloud_backup_top_hero.png`)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(941f / 354f)
          .testTag("cloud_backup_top_hero")
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_cloud_backup_top_hero),
          contentDescription = "CineFlow AI Cloud Backup — Select Data to Backup",
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
            .testTag("cloud_backup_back_button")
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 2. Card 1: Select Data to Backup Card (`cloud_backup_select_data_card`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(BackupCardBg)
          .border(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
              colors = listOf(Color(0xFF7B4BFF), BackupCardBorder)
            ),
            shape = RoundedCornerShape(18.dp)
          )
          .padding(horizontal = 14.dp, vertical = 10.dp)
          .testTag("cloud_backup_select_data_card")
      ) {
        // Select All Header Row
        val isAllSelected = selectedCount == totalCategories
        val isAnySelected = selectedCount > 0
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { viewModel.toggleSelectAll() }
            .padding(vertical = 4.dp)
            .testTag("cloud_backup_select_all_row"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                  if (isAnySelected) BackupCheckPurple else Color.Transparent
                )
                .border(
                  width = 1.4.dp,
                  color = if (isAnySelected) Color(0xFFD29BFF) else Color(0xFF8C94BE),
                  shape = RoundedCornerShape(6.dp)
                )
                .semantics { selected = isAllSelected }
                .testTag("cloud_backup_select_all_checkbox"),
              contentAlignment = Alignment.Center
            ) {
              if (isAllSelected) {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = "All Selected",
                  tint = Color.White,
                  modifier = Modifier.size(15.dp)
                )
              }
            }
            Text(
              text = "Select All",
              color = BackupTextWhite,
              fontSize = 15.5.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "$selectedCount of $totalCategories selected",
            color = BackupTextWhite,
            fontSize = 12.8.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.testTag("cloud_backup_selected_count_label")
          )
        }

        Spacer(modifier = Modifier.height(6.dp))
        BackupDivider()

        // 4 Data Category Rows
        CloudBackupCategory.entries.forEachIndexed { index, category ->
          val isChecked = category in selectedCategories
          BackupDataCategoryRow(
            category = category,
            isChecked = isChecked,
            onToggleCheck = { viewModel.toggleCategory(category) },
            onOpenDetails = { viewModel.openCategoryDetails(category) }
          )
          if (index < CloudBackupCategory.entries.lastIndex) {
            BackupDivider()
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Card 2: Backup Summary Card (`cloud_backup_summary_card`)
      BackupSummaryCard(
        selectedCount = selectedCount,
        formattedTotalSize = formattedTotalSize,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 4. Card 3: Backup Options Card (`cloud_backup_options_card`)
      BackupOptionsCard(
        autoBackupEnabled = autoBackupEnabled,
        manualBackupSelected = manualBackupSelected,
        onToggleAutoBackup = { viewModel.onAttemptToggleAutoBackup() },
        onSelectManualNow = { viewModel.selectManualBackupNow() },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
      )

      if (!errorMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = errorMessage!!,
          color = Color(0xFFFF6E7E),
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("cloud_backup_error_text")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 5. Primary CTA Pill Button: `☁↑ Start Backup (5.23 GB)`
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .height(52.dp)
          .clip(RoundedCornerShape(26.dp))
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
            shape = RoundedCornerShape(26.dp)
          )
          .clickable { viewModel.startManualBackup() }
          .testTag("cloud_backup_start_button"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.CloudUpload,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
          Text(
            text = "Start Backup ($formattedTotalSize)",
            color = Color.White,
            fontSize = 16.5.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }

  if (dialogTitle != null && dialogMessage != null) {
    AlertDialog(
      onDismissRequest = { viewModel.dismissDialog() },
      containerColor = Color(0xFF15193B),
      titleContentColor = BackupTextWhite,
      textContentColor = BackupTextMuted,
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
          modifier = Modifier.testTag("cloud_backup_dialog_close_button")
        ) {
          Text(
            text = "Close",
            color = Color(0xFF36D6FF),
            fontWeight = FontWeight.Bold
          )
        }
      },
      modifier = Modifier.testTag("cloud_backup_status_dialog")
    )
  }
}

@Composable
private fun BackupDataCategoryRow(
  category: CloudBackupCategory,
  isChecked: Boolean,
  onToggleCheck: () -> Unit,
  onOpenDetails: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
      .testTag(category.rowTestTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Checkbox
    Box(
      modifier = Modifier
        .size(22.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(if (isChecked) BackupCheckPurple else Color.Transparent)
        .border(
          width = 1.4.dp,
          color = if (isChecked) Color(0xFFD29BFF) else Color(0xFF9EA6CE),
          shape = RoundedCornerShape(6.dp)
        )
        .clickable(onClick = onToggleCheck)
        .semantics { selected = isChecked }
        .testTag(category.checkboxTestTag),
      contentAlignment = Alignment.Center
    ) {
      if (isChecked) {
        Icon(
          imageVector = Icons.Filled.Check,
          contentDescription = "${category.title} Selected",
          tint = Color.White,
          modifier = Modifier.size(15.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Category Icon Box
    Box(
      modifier = Modifier
        .size(width = 46.dp, height = 38.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(BackupIconBoxBg)
        .clickable(onClick = onToggleCheck),
      contentAlignment = Alignment.Center
    ) {
      when (category) {
        CloudBackupCategory.PROJECTS -> Icon(
          imageVector = Icons.Outlined.MovieCreation,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(22.dp)
        )
        CloudBackupCategory.MEDIA_ASSETS -> Icon(
          imageVector = Icons.Outlined.Image,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(22.dp)
        )
        CloudBackupCategory.APP_DATA_PRESETS -> Icon(
          imageVector = Icons.Outlined.Layers,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(22.dp)
        )
        CloudBackupCategory.AI_ASSETS -> Text(
          text = "✦AI",
          color = Color.White,
          fontSize = 12.5.sp,
          fontWeight = FontWeight.ExtraBold
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Title & Subtitle
    Column(
      modifier = Modifier
        .weight(1f)
        .clickable(onClick = onToggleCheck)
    ) {
      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = BackupTextWhite,
              fontSize = 14.2.sp,
              fontWeight = FontWeight.Bold
            )
          ) {
            append(category.title)
          }
          if (category.optionalSuffix != null) {
            withStyle(
              SpanStyle(
                color = BackupTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
              )
            ) {
              append(category.optionalSuffix)
            }
          }
        }
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = category.subtitle,
        color = BackupTextMuted,
        fontSize = 11.sp,
        lineHeight = 13.5.sp
      )
    }

    Spacer(modifier = Modifier.width(6.dp))

    // Right Size / Count + Chevron
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .clickable(onClick = onOpenDetails)
        .padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
    ) {
      Column(horizontalAlignment = Alignment.End) {
        if (category.rightTopLabel != null) {
          Text(
            text = category.rightTopLabel,
            color = BackupTextWhite,
            fontSize = 11.8.sp,
            fontWeight = FontWeight.Medium
          )
        }
        Text(
          text = category.rightBottomLabel,
          color = BackupTextWhite,
          fontSize = 11.8.sp,
          fontWeight = FontWeight.Medium
        )
      }
      Spacer(modifier = Modifier.width(4.dp))
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = "Details for ${category.title}",
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
private fun BackupSummaryCard(
  selectedCount: Int,
  formattedTotalSize: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .background(BackupCardBg)
      .border(
        width = 1.2.dp,
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF7B4BFF), BackupCardBorder)
        ),
        shape = RoundedCornerShape(18.dp)
      )
      .padding(horizontal = 14.dp, vertical = 11.dp)
      .testTag("cloud_backup_summary_card")
  ) {
    // Header: Cloud Upload Icon + Backup Summary
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Icon(
        imageVector = Icons.Outlined.CloudUpload,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(24.dp)
      )
      Text(
        text = "Backup Summary",
        color = BackupTextWhite,
        fontSize = 15.8.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(8.dp))
    BackupDivider()

    // Row 1: Selected Data | 3 Items
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 7.dp)
        .testTag("cloud_backup_summary_selected_items"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Description,
          contentDescription = null,
          tint = Color(0xFFD6DEFF),
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = "Selected Data",
          color = BackupTextWhite,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )
      }
      Text(
        text = "$selectedCount Items",
        color = BackupTextWhite,
        fontSize = 13.2.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.testTag("cloud_backup_summary_items_value")
      )
    }

    BackupDivider()

    // Row 2: Total Backup Size | ≈ 5.23 GB
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 7.dp)
        .testTag("cloud_backup_summary_total_size"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Storage,
          contentDescription = null,
          tint = Color(0xFFD6DEFF),
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = "Total Backup Size",
          color = BackupTextWhite,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )
      }
      Text(
        text = "≈ $formattedTotalSize",
        color = BackupTextWhite,
        fontSize = 13.2.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.testTag("cloud_backup_summary_size_value")
      )
    }

    BackupDivider()

    // Row 3: Cloud Storage (Pro Plan) | 68.40 GB of 100 GB used
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 7.dp)
        .testTag("cloud_backup_summary_cloud_storage"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.CloudQueue,
          contentDescription = null,
          tint = Color(0xFFD6DEFF),
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = "Cloud Storage (Pro Plan)",
          color = BackupTextWhite,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )
      }
      Text(
        text = "68.40 GB of 100 GB used",
        color = BackupTextWhite,
        fontSize = 12.8.sp,
        fontWeight = FontWeight.Medium
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Progress Bar: `68.40 GB Used` ... `100 GB`
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(22.dp)
        .clip(RoundedCornerShape(11.dp))
        .background(Color(0xFF232952))
        .border(1.dp, Color(0xFF5660A4), RoundedCornerShape(11.dp))
        .testTag("cloud_backup_storage_progress_bar")
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.684f)
          .fillMaxHeight()
          .clip(RoundedCornerShape(11.dp))
          .background(
            Brush.horizontalGradient(
              colors = listOf(
                Color(0xFFA032FF),
                Color(0xFF4764FF),
                Color(0xFF00E5FF)
              )
            )
          )
      )
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "68.40 GB Used",
          color = Color.White,
          fontSize = 10.5.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "100 GB",
          color = Color.White,
          fontSize = 10.5.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

@Composable
private fun BackupOptionsCard(
  autoBackupEnabled: Boolean,
  manualBackupSelected: Boolean,
  onToggleAutoBackup: () -> Unit,
  onSelectManualNow: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .background(BackupCardBg)
      .border(
        width = 1.2.dp,
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF7B4BFF), BackupCardBorder)
        ),
        shape = RoundedCornerShape(18.dp)
      )
      .padding(horizontal = 14.dp, vertical = 10.dp)
      .testTag("cloud_backup_options_card")
  ) {
    // Header: Gear Icon + Backup Options
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Icon(
        imageVector = Icons.Outlined.Settings,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(22.dp)
      )
      Text(
        text = "Backup Options",
        color = BackupTextWhite,
        fontSize = 15.5.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(6.dp))
    BackupDivider()

    // Row 1: Auto Backup (Toggle OFF per Section 12)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onToggleAutoBackup)
        .padding(vertical = 8.dp)
        .testTag("cloud_backup_auto_backup_row"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = Icons.Outlined.CloudQueue,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(22.dp)
        )
        Column {
          Text(
            text = "Auto Backup",
            color = BackupTextWhite,
            fontSize = 13.8.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Automatically backup your selected data",
            color = BackupTextMuted,
            fontSize = 11.2.sp
          )
        }
      }

      // Toggle Switch (Off state matching reference image)
      Box(
        modifier = Modifier
          .width(44.dp)
          .height(24.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(if (autoBackupEnabled) BackupCheckPurple else Color(0xFF474E7C))
          .padding(3.dp)
          .semantics { selected = autoBackupEnabled }
          .testTag("cloud_backup_auto_backup_switch"),
        contentAlignment = if (autoBackupEnabled) Alignment.CenterEnd else Alignment.CenterStart
      ) {
        Box(
          modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(Color.White)
        )
      }
    }

    BackupDivider()

    // Row 2: Backup Now (Manual) — Selected Radio
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onSelectManualNow)
        .padding(vertical = 8.dp)
        .testTag("cloud_backup_manual_now_row"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = Icons.Outlined.CalendarToday,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(21.dp)
        )
        Column {
          Text(
            text = "Backup Now (Manual)",
            color = BackupTextWhite,
            fontSize = 13.8.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Start a backup immediately",
            color = BackupTextMuted,
            fontSize = 11.2.sp
          )
        }
      }

      // Glowing Purple Selected Radio Button (`◉`)
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(if (manualBackupSelected) Color(0xFF5A24C8) else Color.Transparent)
          .border(1.8.dp, Color.White, CircleShape)
          .semantics { selected = manualBackupSelected }
          .testTag("cloud_backup_manual_radio"),
        contentAlignment = Alignment.Center
      ) {
        if (manualBackupSelected) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(Color.White)
          )
        }
      }
    }
  }
}

@Composable
private fun BackupDivider() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(1.dp)
      .background(BackupDividerColor)
  )
}
