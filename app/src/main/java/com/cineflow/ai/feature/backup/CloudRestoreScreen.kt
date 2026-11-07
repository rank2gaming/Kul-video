package com.cineflow.ai.feature.backup

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tune
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

private val RestoreBgDeep = Color(0xFF0A0A26)
private val RestoreCardBg = Color(0xFF13173A)
private val RestoreCardBorder = Color(0xFF474E88)
private val RestoreIconBoxBg = Color(0xFF282564)
private val RestoreTextWhite = Color(0xFFFFFFFF)
private val RestoreTextMuted = Color(0xFFB7BEDE)
private val RestoreCheckPurple = Color(0xFF8B38FF)
private val RestoreDividerColor = Color(0xFF282F5E)
private val RestoreStorageGreen = Color(0xFF2EDB58)

/**
 * Cloud Restore — Select Backup Screen (`CineFlow AI Cloud Restore Interface.png` & Master Prompt Section 12).
 */
@Composable
fun CloudRestoreScreen(
  viewModel: CloudRestoreViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val selectedSnapshotId by viewModel.selectedSnapshotId.collectAsStateWithLifecycle()
  val selectedContentCategories by viewModel.selectedContentCategories.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
  val dialogTitle by viewModel.dialogTitle.collectAsStateWithLifecycle()
  val dialogMessage by viewModel.dialogMessage.collectAsStateWithLifecycle()

  val selectedSnapshot = viewModel.availableSnapshots.firstOrNull { it.id == selectedSnapshotId }
    ?: viewModel.availableSnapshots.first()
  val selectedContentCount = selectedContentCategories.size
  val totalContentCategories = viewModel.totalContentCategoriesCount
  val snapshotScale = selectedSnapshot.baseSizeGb / 5.23
  val totalSizeGb = if (selectedContentCategories.isEmpty()) {
    0.0
  } else {
    selectedContentCategories.sumOf { it.baseSizeGb } * snapshotScale
  }
  val formattedTotalSize = String.format(Locale.US, "%.2f GB", totalSizeGb)

  BackHandler {
    onBack()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(RestoreBgDeep)
      .testTag("cloud_restore_screen"),
    containerColor = RestoreBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(RestoreBgDeep),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Hero Header (`img_cloud_restore_top_hero.png`)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(941f / 364f)
          .testTag("cloud_restore_top_hero")
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_cloud_restore_top_hero),
          contentDescription = "CineFlow AI Cloud Restore — Restore from Cloud Backup",
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
            .testTag("cloud_restore_back_button")
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 2. Card 1: Available Backups Card (`cloud_restore_available_backups_card`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(RestoreCardBg)
          .border(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
              colors = listOf(Color(0xFF7B4BFF), RestoreCardBorder)
            ),
            shape = RoundedCornerShape(18.dp)
          )
          .padding(horizontal = 12.dp, vertical = 10.dp)
          .testTag("cloud_restore_available_backups_card")
      ) {
        // Header Row: Cloud Icon + Available Backups | 4 Backups Found
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.CloudUpload,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
            Text(
              text = "Available Backups",
              color = RestoreTextWhite,
              fontSize = 15.5.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "${viewModel.availableSnapshots.size} Backups Found",
            color = RestoreTextWhite,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.testTag("cloud_restore_backups_count_label")
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        viewModel.availableSnapshots.forEachIndexed { index, snapshot ->
          val isSelected = snapshot.id == selectedSnapshotId
          AvailableBackupSnapshotRow(
            snapshot = snapshot,
            isSelected = isSelected,
            onSelect = { viewModel.selectSnapshot(snapshot.id) },
            onOpenMenu = { viewModel.openSnapshotOptions(snapshot) }
          )
          if (index < viewModel.availableSnapshots.lastIndex) {
            Spacer(modifier = Modifier.height(6.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Card 2: Restore Content Card (`cloud_restore_content_card`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(RestoreCardBg)
          .border(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
              colors = listOf(Color(0xFF7B4BFF), RestoreCardBorder)
            ),
            shape = RoundedCornerShape(18.dp)
          )
          .padding(horizontal = 14.dp, vertical = 11.dp)
          .testTag("cloud_restore_content_card")
      ) {
        // Header Row: Layers icon + Restore Content + Subtitle
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Layers,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
          Column {
            Text(
              text = "Restore Content",
              color = RestoreTextWhite,
              fontSize = 15.8.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Select what you want to restore from the chosen backup.",
              color = RestoreTextMuted,
              fontSize = 11.5.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        RestoreDivider()

        CloudRestoreContentCategory.entries.forEachIndexed { index, category ->
          val isChecked = category in selectedContentCategories
          RestoreContentCategoryRow(
            category = category,
            isChecked = isChecked,
            onToggle = { viewModel.toggleContentCategory(category) }
          )
          if (index < CloudRestoreContentCategory.entries.lastIndex) {
            RestoreDivider()
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4. Card 3: Restore Summary Card (`cloud_restore_summary_card`)
      RestoreSummaryCard(
        selectedCount = selectedContentCount,
        totalCategories = totalContentCategories,
        formattedTotalSize = formattedTotalSize,
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
            .testTag("cloud_restore_error_text")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 5. Primary CTA Pill Button: `☁↑ Restore Now (5.23 GB)`
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
          .clickable { viewModel.startManualRestore() }
          .testTag("cloud_restore_now_button"),
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
            text = "Restore Now ($formattedTotalSize)",
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
      titleContentColor = RestoreTextWhite,
      textContentColor = RestoreTextMuted,
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
          modifier = Modifier.testTag("cloud_restore_dialog_close_button")
        ) {
          Text(
            text = "Close",
            color = Color(0xFF36D6FF),
            fontWeight = FontWeight.Bold
          )
        }
      },
      modifier = Modifier.testTag("cloud_restore_status_dialog")
    )
  }
}

@Composable
private fun AvailableBackupSnapshotRow(
  snapshot: CloudRestoreSnapshot,
  isSelected: Boolean,
  onSelect: () -> Unit,
  onOpenMenu: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(
        if (isSelected) {
          Brush.horizontalGradient(
            colors = listOf(Color(0xFF331E6E), Color(0xFF231C54))
          )
        } else {
          Brush.horizontalGradient(
            colors = listOf(Color(0xFF14193E), Color(0xFF14193E))
          )
        }
      )
      .border(
        width = if (isSelected) 1.4.dp else 1.dp,
        color = if (isSelected) Color(0xFFB44CFF) else Color(0xFF282F5E),
        shape = RoundedCornerShape(14.dp)
      )
      .clickable(onClick = onSelect)
      .padding(horizontal = 10.dp, vertical = 9.dp)
      .testTag(snapshot.rowTestTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Radio circle
    Box(
      modifier = Modifier
        .size(22.dp)
        .clip(CircleShape)
        .background(if (isSelected) Color(0xFF8B38FF) else Color.Transparent)
        .border(
          width = 1.6.dp,
          color = if (isSelected) Color(0xFFE2A8FF) else Color(0xFF8C94BE),
          shape = CircleShape
        )
        .semantics { selected = isSelected }
        .testTag(snapshot.radioTestTag),
      contentAlignment = Alignment.Center
    ) {
      if (isSelected) {
        Box(
          modifier = Modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(Color.White)
        )
      }
    }

    Spacer(modifier = Modifier.width(10.dp))

    // Rounded square cloud icon box
    Box(
      modifier = Modifier
        .size(width = 42.dp, height = 36.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(if (isSelected) Color(0xFF422C88) else Color(0xFF23285C)),
      contentAlignment = Alignment.Center
    ) {
      when (snapshot.iconType) {
        RestoreSnapshotIconType.CLOUD_CLOCK -> Icon(
          imageVector = Icons.Outlined.CloudDone,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
        RestoreSnapshotIconType.CLOUD_DOWNLOAD -> Icon(
          imageVector = Icons.Outlined.CloudDownload,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
        RestoreSnapshotIconType.CLOUD_UPLOAD -> Icon(
          imageVector = Icons.Outlined.CloudUpload,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(10.dp))

    // Title + Optional `Recommended` Pill + Timestamp
    Column(modifier = Modifier.weight(1f)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = snapshot.title,
          color = RestoreTextWhite,
          fontSize = 13.8.sp,
          fontWeight = FontWeight.Bold
        )
        if (snapshot.isRecommended) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(
                Brush.horizontalGradient(
                  colors = listOf(Color(0xFFAE32FF), Color(0xFFD938FF))
                )
              )
              .padding(horizontal = 7.dp, vertical = 2.dp)
              .testTag("cloud_restore_recommended_badge")
          ) {
            Text(
              text = "Recommended",
              color = Color.White,
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = snapshot.timestampLabel,
        color = RestoreTextMuted,
        fontSize = 11.2.sp
      )
    }

    Spacer(modifier = Modifier.width(6.dp))

    // Right Column: Size + Projects count
    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = snapshot.sizeLabel,
        color = RestoreTextWhite,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = snapshot.projectsCountLabel,
        color = RestoreTextMuted,
        fontSize = 11.sp
      )
    }

    Spacer(modifier = Modifier.width(4.dp))

    // 3-Dot Vertical Menu (`⋮`)
    Box(
      modifier = Modifier
        .size(26.dp)
        .clip(CircleShape)
        .clickable(onClick = onOpenMenu)
        .testTag(snapshot.menuTestTag),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.MoreVert,
        contentDescription = "Options for ${snapshot.title}",
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
private fun RestoreContentCategoryRow(
  category: CloudRestoreContentCategory,
  isChecked: Boolean,
  onToggle: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onToggle)
      .padding(vertical = 8.dp)
      .testTag(category.rowTestTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Checkbox
    Box(
      modifier = Modifier
        .size(22.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(if (isChecked) RestoreCheckPurple else Color.Transparent)
        .border(
          width = 1.4.dp,
          color = if (isChecked) Color(0xFFD29BFF) else Color(0xFF9EA6CE),
          shape = RoundedCornerShape(6.dp)
        )
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
        .size(width = 44.dp, height = 36.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(RestoreIconBoxBg),
      contentAlignment = Alignment.Center
    ) {
      when (category) {
        CloudRestoreContentCategory.PROJECTS -> Icon(
          imageVector = Icons.Outlined.MovieCreation,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(21.dp)
        )
        CloudRestoreContentCategory.MEDIA_ASSETS -> Icon(
          imageVector = Icons.Outlined.Image,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(21.dp)
        )
        CloudRestoreContentCategory.APP_DATA_PRESETS -> Icon(
          imageVector = Icons.Outlined.Tune,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(21.dp)
        )
        CloudRestoreContentCategory.AI_ASSETS -> Text(
          text = "✦AI",
          color = Color.White,
          fontSize = 12.2.sp,
          fontWeight = FontWeight.ExtraBold
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Title + Subtitle
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = RestoreTextWhite,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          ) {
            append(category.title)
          }
          if (category.optionalSuffix != null) {
            withStyle(
              SpanStyle(
                color = RestoreTextMuted,
                fontSize = 12.8.sp,
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
        color = RestoreTextMuted,
        fontSize = 11.sp
      )
    }

    Spacer(modifier = Modifier.width(6.dp))

    Text(
      text = category.rightLabel,
      color = RestoreTextWhite,
      fontSize = 11.8.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
private fun RestoreSummaryCard(
  selectedCount: Int,
  totalCategories: Int,
  formattedTotalSize: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .background(RestoreCardBg)
      .border(
        width = 1.2.dp,
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF7B4BFF), RestoreCardBorder)
        ),
        shape = RoundedCornerShape(18.dp)
      )
      .padding(horizontal = 14.dp, vertical = 11.dp)
      .testTag("cloud_restore_summary_card")
  ) {
    // Header: Info Circle Icon + Restore Summary
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Icon(
        imageVector = Icons.Outlined.Info,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(22.dp)
      )
      Text(
        text = "Restore Summary",
        color = RestoreTextWhite,
        fontSize = 15.8.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(8.dp))
    RestoreDivider()
    Spacer(modifier = Modifier.height(8.dp))

    // 3-Column Metrics Row: Selected Items | Total Size | Device Storage
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Column 1: Selected Items (3 of 4)
      Row(
        modifier = Modifier
          .weight(0.95f)
          .testTag("cloud_restore_summary_selected_items"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Folder,
          contentDescription = null,
          tint = Color(0xFFD6DEFF),
          modifier = Modifier.size(20.dp)
        )
        Column {
          Text(
            text = "Selected Items",
            color = RestoreTextMuted,
            fontSize = 10.5.sp
          )
          Text(
            text = "$selectedCount of $totalCategories",
            color = RestoreTextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("cloud_restore_summary_items_value")
          )
        }
      }

      Box(
        modifier = Modifier
          .width(1.dp)
          .height(30.dp)
          .background(RestoreDividerColor)
      )

      // Column 2: Total Size (≈ 5.23 GB)
      Row(
        modifier = Modifier
          .weight(1.05f)
          .padding(start = 8.dp)
          .testTag("cloud_restore_summary_total_size"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Storage,
          contentDescription = null,
          tint = Color(0xFFD6DEFF),
          modifier = Modifier.size(20.dp)
        )
        Column {
          Text(
            text = "Total Size",
            color = RestoreTextMuted,
            fontSize = 10.5.sp
          )
          Text(
            text = "≈ $formattedTotalSize",
            color = RestoreTextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("cloud_restore_summary_size_value")
          )
        }
      }

      Box(
        modifier = Modifier
          .width(1.dp)
          .height(30.dp)
          .background(RestoreDividerColor)
      )

      // Column 3: Device Storage (68.40 GB Available)
      Row(
        modifier = Modifier
          .weight(1.25f)
          .padding(start = 8.dp)
          .testTag("cloud_restore_summary_device_storage"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.SdStorage,
          contentDescription = null,
          tint = Color(0xFFD6DEFF),
          modifier = Modifier.size(20.dp)
        )
        Column {
          Text(
            text = "Device Storage",
            color = RestoreTextMuted,
            fontSize = 10.5.sp
          )
          Text(
            text = "68.40 GB Available",
            color = RestoreStorageGreen,
            fontSize = 11.8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("cloud_restore_summary_storage_value")
          )
        }
      }
    }
  }
}

@Composable
private fun RestoreDivider() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(1.dp)
      .background(RestoreDividerColor)
  )
}
