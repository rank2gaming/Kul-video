package com.cineflow.ai.feature.notifications

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R

private val NotifBgTop = Color(0xFF0B0A26)
private val NotifBgMiddle = Color(0xFF090B22)
private val NotifBgBottom = Color(0xFF07061A)
private val NotifCardBg = Color(0xFF14183B)
private val NotifCardBgAlt = Color(0xFF121636)
private val NotifCardBorder = Color(0xFF2B3566)
private val NotifDividerColor = Color(0xFF232B54)
private val NotifTextPrimary = Color(0xFFF8FAFC)
private val NotifTextSecondary = Color(0xFFB4C0DF)
private val NotifUnreadDot = Color(0xFFE040FB)
private val NotifPurpleStart = Color(0xFFA838FF)
private val NotifPurpleEnd = Color(0xFF635BFF)
private val NotifCyanAccent = Color(0xFF22D3EE)
private val NotifGoldCrown = Color(0xFFFACC15)
private val NotifGreenSuccess = Color(0xFF22C55E)

/**
 * Notification Center Screen (`CineFlow AI Notification Center.png` — Reference Index 11, TASK-018).
 */
@Composable
fun NotificationCenterScreen(
  viewModel: NotificationCenterViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)

  val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
  val notifications by viewModel.notifications.collectAsStateWithLifecycle()
  val appNotificationsEnabled by viewModel.appNotificationsEnabled.collectAsStateWithLifecycle()
  val backupRestoreAlertsEnabled by viewModel.backupRestoreAlertsEnabled.collectAsStateWithLifecycle()
  val subscriptionAlertsEnabled by viewModel.subscriptionAlertsEnabled.collectAsStateWithLifecycle()
  val accountAlertsEnabled by viewModel.accountAlertsEnabled.collectAsStateWithLifecycle()
  val dialogTitle by viewModel.statusDialogTitle.collectAsStateWithLifecycle()
  val dialogMessage by viewModel.statusDialogMessage.collectAsStateWithLifecycle()

  val visibleNotifications = if (selectedFilter == NotificationCategoryFilter.ALL) {
    notifications
  } else {
    notifications.filter { it.category == selectedFilter }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(NotifBgBottom)
      .testTag("notification_center_screen"),
    containerColor = NotifBgBottom,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(NotifBgTop, NotifBgMiddle, NotifBgBottom)
          )
        )
        .padding(innerPadding)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Hero Banner (Exact artwork from CineFlow AI Notification Center.png)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 600.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_notification_center_top_hero),
          contentDescription = "CineFlow AI Notifications Hero Header",
          contentScale = ContentScale.FillWidth,
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(941f / 260f)
            .testTag("notification_center_top_hero")
        )

        // Back Arrow Button Overlay
        Box(
          modifier = Modifier
            .padding(start = 12.dp, top = 8.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
              role = Role.Button,
              onClick = onBack
            )
            .testTag("notification_center_back_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.Transparent,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 600.dp)
          .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // 2. 5-Category Filter Pill Bar (All, Account, Projects, Backup, Subscription)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF101336))
            .border(
              width = 1.2.dp,
              brush = Brush.horizontalGradient(
                colors = listOf(
                  NotifPurpleStart.copy(alpha = 0.75f),
                  NotifCyanAccent.copy(alpha = 0.85f)
                )
              ),
              shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .testTag("notification_center_filter_bar")
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            NotificationCategoryFilter.entries.forEach { filter ->
              val isSelected = filter == selectedFilter
              val weight = when (filter) {
                NotificationCategoryFilter.ALL -> 0.85f
                NotificationCategoryFilter.SUBSCRIPTION -> 1.22f
                else -> 1f
              }
              Box(
                modifier = Modifier
                  .weight(weight)
                  .height(36.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(
                    if (isSelected) {
                      Brush.horizontalGradient(
                        colors = listOf(NotifPurpleStart, NotifPurpleEnd)
                      )
                    } else {
                      Brush.horizontalGradient(
                        colors = listOf(Color(0xFF181D45), Color(0xFF181D45))
                      )
                    }
                  )
                  .border(
                    width = 1.dp,
                    color = if (isSelected) Color(0xFFC084FC) else Color(0xFF2A3362),
                    shape = RoundedCornerShape(12.dp)
                  )
                  .clickable(
                    role = Role.Tab,
                    onClick = { viewModel.selectFilter(filter) }
                  )
                  .padding(horizontal = 4.dp)
                  .testTag(filter.testTag),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  FilterCategorySmallIcon(
                    filter = filter,
                    tint = Color.White
                  )
                  Text(
                    text = filter.label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }

        // 3. Recent Notifications Section Header + "✓ Mark All as Read"
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, start = 2.dp, end = 2.dp)
            .testTag("notification_recent_header"),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Notifications",
            color = NotifTextPrimary,
            fontSize = 17.5.sp,
            fontWeight = FontWeight.Bold
          )

          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable(
                role = Role.Button,
                onClick = { viewModel.markAllAsRead() }
              )
              .padding(horizontal = 6.dp, vertical = 4.dp)
              .testTag("notification_mark_all_read_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = NotifTextSecondary,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = "Mark All as Read",
              color = NotifTextSecondary,
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // 4. Recent Notifications Cards List + "View All Notifications" Row
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_recent_list"),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          visibleNotifications.forEachIndexed { index, entry ->
            NotificationItemCard(
              entry = entry,
              index = index,
              onCardClick = { viewModel.openNotificationDetail(entry) },
              onMenuClick = { viewModel.openNotificationDetail(entry) }
            )
          }

          // "View All Notifications >" full-width bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(NotifCardBgAlt)
              .border(
                width = 1.dp,
                color = NotifCardBorder,
                shape = RoundedCornerShape(14.dp)
              )
              .clickable(
                role = Role.Button,
                onClick = { viewModel.openViewAllNotificationsDialog() }
              )
              .padding(horizontal = 16.dp)
              .testTag("notification_view_all_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Spacer(modifier = Modifier.width(18.dp))
            Text(
              text = "View All Notifications",
              color = NotifTextPrimary,
              fontSize = 14.5.sp,
              fontWeight = FontWeight.SemiBold
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = null,
              tint = NotifTextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 5. Notification Settings Card
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color(0xFF171B42),
                  Color(0xFF111536)
                )
              )
            )
            .border(
              width = 1.3.dp,
              brush = Brush.linearGradient(
                colors = listOf(
                  NotifPurpleStart,
                  NotifPurpleEnd,
                  NotifCyanAccent
                )
              ),
              shape = RoundedCornerShape(20.dp)
            )
            .padding(vertical = 14.dp)
            .testTag("notification_settings_card")
        ) {
          // Card Header: Gear Icon + Title + Subtitle
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF261E5A))
                .border(1.dp, Color(0xFF6D4AFF), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = Color(0xFFE9D5FF),
                modifier = Modifier.size(22.dp)
              )
            }

            Column {
              Text(
                text = "Notification Settings",
                color = NotifTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Choose what updates you want to receive.",
                color = NotifTextSecondary,
                fontSize = 12.5.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = NotifDividerColor, thickness = 1.dp)

          // Row 1: App Notifications
          NotificationSettingToggleRow(
            title = "App Notifications",
            subtitle = "General updates, tips and new features",
            checked = appNotificationsEnabled,
            onToggle = { viewModel.toggleAppNotifications() },
            rowTestTag = "notification_setting_app_row",
            switchTestTag = "notification_switch_app",
            iconContent = {
              Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = Color(0xFFE2E8F0),
                modifier = Modifier.size(22.dp)
              )
            }
          )

          HorizontalDivider(
            color = NotifDividerColor,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 14.dp)
          )

          // Row 2: Backup & Restore Alerts
          NotificationSettingToggleRow(
            title = "Backup & Restore Alerts",
            subtitle = "Cloud backup, restore and sync status",
            checked = backupRestoreAlertsEnabled,
            onToggle = { viewModel.toggleBackupRestoreAlerts() },
            rowTestTag = "notification_setting_backup_row",
            switchTestTag = "notification_switch_backup",
            iconContent = {
              CloudDownIcon(
                tint = Color(0xFFE2E8F0),
                modifier = Modifier.size(22.dp)
              )
            }
          )

          HorizontalDivider(
            color = NotifDividerColor,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 14.dp)
          )

          // Row 3: Subscription Alerts
          NotificationSettingToggleRow(
            title = "Subscription Alerts",
            subtitle = "Plan updates, renewals, billing and offers",
            checked = subscriptionAlertsEnabled,
            onToggle = { viewModel.toggleSubscriptionAlerts() },
            rowTestTag = "notification_setting_subscription_row",
            switchTestTag = "notification_switch_subscription",
            iconContent = {
              CrownOutlineIcon(
                tint = NotifGoldCrown,
                modifier = Modifier.size(22.dp)
              )
            }
          )

          HorizontalDivider(
            color = NotifDividerColor,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 14.dp)
          )

          // Row 4: Account Alerts
          NotificationSettingToggleRow(
            title = "Account Alerts",
            subtitle = "Login activity, security and important account updates",
            checked = accountAlertsEnabled,
            onToggle = { viewModel.toggleAccountAlerts() },
            rowTestTag = "notification_setting_account_row",
            switchTestTag = "notification_switch_account",
            iconContent = {
              ShieldAlertIcon(
                tint = Color(0xFFE2E8F0),
                modifier = Modifier.size(22.dp)
              )
            }
          )
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

  // Notification Detail / View All Modal Dialog
  if (dialogTitle != null && dialogMessage != null) {
    AlertDialog(
      onDismissRequest = { viewModel.dismissStatusDialog() },
      containerColor = Color(0xFF15193B),
      titleContentColor = NotifTextPrimary,
      textContentColor = NotifTextSecondary,
      title = {
        Text(
          text = dialogTitle.orEmpty(),
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      },
      text = {
        Text(
          text = dialogMessage.orEmpty(),
          fontSize = 14.sp,
          lineHeight = 20.sp
        )
      },
      confirmButton = {
        Button(
          onClick = { viewModel.dismissStatusDialog() },
          colors = ButtonDefaults.buttonColors(
            containerColor = NotifPurpleEnd,
            contentColor = Color.White
          ),
          modifier = Modifier.testTag("notification_dialog_close_button")
        ) {
          Text("OK", fontWeight = FontWeight.Bold)
        }
      },
      modifier = Modifier.testTag("notification_status_dialog")
    )
  }
}

@Composable
private fun NotificationItemCard(
  entry: NotificationEntry,
  index: Int,
  onCardClick: () -> Unit,
  onMenuClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(
        Brush.horizontalGradient(
          colors = listOf(
            NotifCardBg,
            NotifCardBgAlt
          )
        )
      )
      .border(
        width = 1.dp,
        color = if (entry.isUnread) Color(0xFF3B427E) else NotifCardBorder,
        shape = RoundedCornerShape(16.dp)
      )
      .clickable(
        role = Role.Button,
        onClick = onCardClick
      )
      .padding(horizontal = 10.dp, vertical = 11.dp)
      .testTag("notification_item_$index"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Unread Magenta Dot (or spacing placeholder to align cards cleanly)
    Box(
      modifier = Modifier
        .width(14.dp),
      contentAlignment = Alignment.Center
    ) {
      if (entry.isUnread) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(NotifUnreadDot)
            .testTag("notification_unread_dot_${entry.id}")
        )
      }
    }

    Spacer(modifier = Modifier.width(6.dp))

    // Left Circular Feature Badge Icon
    NotificationLeadingBadge(iconStyle = entry.iconStyle)

    Spacer(modifier = Modifier.width(12.dp))

    // Title & Message Column
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Text(
          text = entry.title,
          color = NotifTextPrimary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = entry.timestampLabel,
          color = NotifTextSecondary,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Medium
        )
      }

      Text(
        text = entry.message,
        color = NotifTextSecondary,
        fontSize = 12.8.sp,
        lineHeight = 17.5.sp
      )
    }

    Spacer(modifier = Modifier.width(4.dp))

    // 3-dot Menu Icon on Right
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .clickable(
          role = Role.Button,
          onClick = onMenuClick
        )
        .testTag("notification_item_menu_$index"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "Notification options",
        tint = NotifTextSecondary,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
private fun NotificationLeadingBadge(iconStyle: NotificationIconStyle) {
  val bgColor = when (iconStyle) {
    NotificationIconStyle.CLOUD_BACKUP_SUCCESS -> Color(0xFF103134)
    NotificationIconStyle.SUBSCRIPTION_CROWN -> Color(0xFF2B2424)
    NotificationIconStyle.EXPORT_TRAY -> Color(0xFF231F4F)
    NotificationIconStyle.PROJECT_CLAPPER -> Color(0xFF182449)
    NotificationIconStyle.APP_UPDATE_DOWNLOAD -> Color(0xFF182449)
  }
  val borderColor = when (iconStyle) {
    NotificationIconStyle.CLOUD_BACKUP_SUCCESS -> NotifGreenSuccess.copy(alpha = 0.45f)
    NotificationIconStyle.SUBSCRIPTION_CROWN -> NotifGoldCrown.copy(alpha = 0.45f)
    NotificationIconStyle.EXPORT_TRAY -> Color(0xFFA78BFA).copy(alpha = 0.45f)
    NotificationIconStyle.PROJECT_CLAPPER -> Color(0xFF60A5FA).copy(alpha = 0.4f)
    NotificationIconStyle.APP_UPDATE_DOWNLOAD -> Color(0xFF60A5FA).copy(alpha = 0.4f)
  }

  Box(
    modifier = Modifier
      .size(44.dp)
      .clip(CircleShape)
      .background(bgColor)
      .border(1.dp, borderColor, CircleShape),
    contentAlignment = Alignment.Center
  ) {
    when (iconStyle) {
      NotificationIconStyle.CLOUD_BACKUP_SUCCESS -> {
        Canvas(modifier = Modifier.size(24.dp)) {
          val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
          val w = size.width
          val h = size.height
          val cloudPath = Path().apply {
            moveTo(w * 0.25f, h * 0.72f)
            lineTo(w * 0.65f, h * 0.72f)
            cubicTo(w * 0.85f, h * 0.72f, w * 0.88f, h * 0.46f, w * 0.70f, h * 0.44f)
            cubicTo(w * 0.66f, h * 0.22f, w * 0.36f, h * 0.22f, w * 0.32f, h * 0.42f)
            cubicTo(w * 0.12f, h * 0.44f, w * 0.12f, h * 0.72f, w * 0.25f, h * 0.72f)
          }
          drawPath(cloudPath, color = NotifGreenSuccess, style = stroke)
          drawCircle(
            color = NotifGreenSuccess,
            radius = w * 0.21f,
            center = Offset(w * 0.75f, h * 0.68f)
          )
          val checkPath = Path().apply {
            moveTo(w * 0.67f, h * 0.68f)
            lineTo(w * 0.73f, h * 0.74f)
            lineTo(w * 0.84f, h * 0.62f)
          }
          drawPath(
            checkPath,
            color = Color(0xFF0A241E),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
          )
        }
      }

      NotificationIconStyle.SUBSCRIPTION_CROWN -> {
        CrownOutlineIcon(
          tint = NotifGoldCrown,
          modifier = Modifier.size(23.dp)
        )
      }

      NotificationIconStyle.EXPORT_TRAY -> {
        Canvas(modifier = Modifier.size(22.dp)) {
          val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
          val w = size.width
          val h = size.height
          // Upward arrow
          drawLine(
            color = Color(0xFFDDD6FE),
            start = Offset(w * 0.5f, h * 0.66f),
            end = Offset(w * 0.5f, h * 0.18f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
          )
          val arrowHead = Path().apply {
            moveTo(w * 0.30f, h * 0.36f)
            lineTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.70f, h * 0.36f)
          }
          drawPath(arrowHead, color = Color(0xFFDDD6FE), style = stroke)
          // Tray base
          val tray = Path().apply {
            moveTo(w * 0.16f, h * 0.58f)
            lineTo(w * 0.16f, h * 0.82f)
            lineTo(w * 0.84f, h * 0.82f)
            lineTo(w * 0.84f, h * 0.58f)
          }
          drawPath(tray, color = Color(0xFFDDD6FE), style = stroke)
        }
      }

      NotificationIconStyle.PROJECT_CLAPPER -> {
        Canvas(modifier = Modifier.size(22.dp)) {
          val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
          val w = size.width
          val h = size.height
          drawRoundRect(
            color = Color(0xFFBFDBFE),
            topLeft = Offset(w * 0.14f, h * 0.22f),
            size = Size(w * 0.72f, h * 0.62f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = stroke
          )
          drawLine(
            color = Color(0xFFBFDBFE),
            start = Offset(w * 0.14f, h * 0.40f),
            end = Offset(w * 0.86f, h * 0.40f),
            strokeWidth = 1.6.dp.toPx()
          )
          val playTriangle = Path().apply {
            moveTo(w * 0.43f, h * 0.50f)
            lineTo(w * 0.43f, h * 0.72f)
            lineTo(w * 0.63f, h * 0.61f)
            close()
          }
          drawPath(playTriangle, color = Color(0xFFBFDBFE))
        }
      }

      NotificationIconStyle.APP_UPDATE_DOWNLOAD -> {
        Canvas(modifier = Modifier.size(22.dp)) {
          val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
          val w = size.width
          val h = size.height
          drawRoundRect(
            color = Color(0xFFBFDBFE),
            topLeft = Offset(w * 0.16f, h * 0.16f),
            size = Size(w * 0.68f, h * 0.68f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = stroke
          )
          drawLine(
            color = Color(0xFFBFDBFE),
            start = Offset(w * 0.5f, h * 0.30f),
            end = Offset(w * 0.5f, h * 0.66f),
            strokeWidth = 1.9.dp.toPx(),
            cap = StrokeCap.Round
          )
          val downHead = Path().apply {
            moveTo(w * 0.35f, h * 0.52f)
            lineTo(w * 0.50f, h * 0.68f)
            lineTo(w * 0.65f, h * 0.52f)
          }
          drawPath(downHead, color = Color(0xFFBFDBFE), style = stroke)
        }
      }
    }
  }
}

@Composable
private fun NotificationSettingToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onToggle: () -> Unit,
  rowTestTag: String,
  switchTestTag: String,
  iconContent: @Composable () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        role = Role.Switch,
        onClick = onToggle
      )
      .padding(horizontal = 14.dp, vertical = 12.dp)
      .testTag(rowTestTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier.size(34.dp),
      contentAlignment = Alignment.Center
    ) {
      iconContent()
    }

    Spacer(modifier = Modifier.width(10.dp))

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = title,
        color = NotifTextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = subtitle,
        color = NotifTextSecondary,
        fontSize = 12.2.sp
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    // Custom Neon Purple Toggle Switch matching CineFlow AI Notification Center.png
    Box(
      modifier = Modifier
        .width(48.dp)
        .height(26.dp)
        .clip(RoundedCornerShape(13.dp))
        .background(
          if (checked) {
            Brush.horizontalGradient(
              colors = listOf(NotifPurpleStart, NotifPurpleEnd)
            )
          } else {
            Brush.horizontalGradient(
              colors = listOf(Color(0xFF283056), Color(0xFF283056))
            )
          }
        )
        .border(
          width = 1.dp,
          color = if (checked) Color(0xFFC084FC) else Color(0xFF3E4878),
          shape = RoundedCornerShape(13.dp)
        )
        .padding(horizontal = 3.dp)
        .testTag(switchTestTag),
      contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
      Box(
        modifier = Modifier
          .size(20.dp)
          .clip(CircleShape)
          .background(Color.White)
      )
    }
  }
}

@Composable
private fun FilterCategorySmallIcon(
  filter: NotificationCategoryFilter,
  tint: Color
) {
  when (filter) {
    NotificationCategoryFilter.ALL -> {
      Icon(
        imageVector = Icons.Outlined.Notifications,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(14.dp)
      )
    }
    NotificationCategoryFilter.ACCOUNT -> {
      Icon(
        imageVector = Icons.Outlined.Person,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(14.dp)
      )
    }
    NotificationCategoryFilter.PROJECTS -> {
      Canvas(modifier = Modifier.size(13.dp)) {
        val w = size.width
        val h = size.height
        drawRoundRect(
          color = tint,
          topLeft = Offset(w * 0.08f, h * 0.16f),
          size = Size(w * 0.84f, h * 0.68f),
          cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
          style = Stroke(width = 1.3.dp.toPx())
        )
        val play = Path().apply {
          moveTo(w * 0.40f, h * 0.34f)
          lineTo(w * 0.40f, h * 0.66f)
          lineTo(w * 0.66f, h * 0.50f)
          close()
        }
        drawPath(play, color = tint)
      }
    }
    NotificationCategoryFilter.BACKUP -> {
      Canvas(modifier = Modifier.size(14.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
          moveTo(w * 0.24f, h * 0.74f)
          lineTo(w * 0.74f, h * 0.74f)
          cubicTo(w * 0.92f, h * 0.74f, w * 0.92f, h * 0.46f, w * 0.76f, h * 0.44f)
          cubicTo(w * 0.70f, h * 0.20f, w * 0.36f, h * 0.20f, w * 0.30f, h * 0.42f)
          cubicTo(w * 0.10f, h * 0.44f, w * 0.10f, h * 0.74f, w * 0.24f, h * 0.74f)
        }
        drawPath(
          path,
          color = tint,
          style = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
      }
    }
    NotificationCategoryFilter.SUBSCRIPTION -> {
      CrownOutlineIcon(
        tint = tint,
        modifier = Modifier.size(13.dp)
      )
    }
  }
}

@Composable
private fun CrownOutlineIcon(
  tint: Color,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val crown = Path().apply {
      moveTo(w * 0.14f, h * 0.76f)
      lineTo(w * 0.86f, h * 0.76f)
      lineTo(w * 0.90f, h * 0.32f)
      lineTo(w * 0.68f, h * 0.52f)
      lineTo(w * 0.50f, h * 0.24f)
      lineTo(w * 0.32f, h * 0.52f)
      lineTo(w * 0.10f, h * 0.32f)
      close()
    }
    drawPath(
      crown,
      color = tint,
      style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
  }
}

@Composable
private fun CloudDownIcon(
  tint: Color,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val cloud = Path().apply {
      moveTo(w * 0.24f, h * 0.72f)
      lineTo(w * 0.76f, h * 0.72f)
      cubicTo(w * 0.92f, h * 0.72f, w * 0.92f, h * 0.46f, w * 0.76f, h * 0.44f)
      cubicTo(w * 0.70f, h * 0.20f, w * 0.36f, h * 0.20f, w * 0.30f, h * 0.42f)
      cubicTo(w * 0.10f, h * 0.44f, w * 0.10f, h * 0.72f, w * 0.24f, h * 0.72f)
    }
    drawPath(cloud, color = tint, style = stroke)
    drawLine(
      color = tint,
      start = Offset(w * 0.50f, h * 0.40f),
      end = Offset(w * 0.50f, h * 0.64f),
      strokeWidth = 1.7.dp.toPx(),
      cap = StrokeCap.Round
    )
    val head = Path().apply {
      moveTo(w * 0.40f, h * 0.55f)
      lineTo(w * 0.50f, h * 0.65f)
      lineTo(w * 0.60f, h * 0.55f)
    }
    drawPath(head, color = tint, style = stroke)
  }
}

@Composable
private fun ShieldAlertIcon(
  tint: Color,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val shield = Path().apply {
      moveTo(w * 0.50f, h * 0.12f)
      lineTo(w * 0.84f, h * 0.26f)
      lineTo(w * 0.84f, h * 0.52f)
      cubicTo(w * 0.84f, h * 0.74f, w * 0.68f, h * 0.86f, w * 0.50f, h * 0.92f)
      cubicTo(w * 0.32f, h * 0.86f, w * 0.16f, h * 0.74f, w * 0.16f, h * 0.52f)
      lineTo(w * 0.16f, h * 0.26f)
      close()
    }
    drawPath(
      shield,
      color = tint,
      style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawLine(
      color = tint,
      start = Offset(w * 0.50f, h * 0.34f),
      end = Offset(w * 0.50f, h * 0.56f),
      strokeWidth = 1.9.dp.toPx(),
      cap = StrokeCap.Round
    )
    drawCircle(
      color = tint,
      radius = 1.4.dp.toPx(),
      center = Offset(w * 0.50f, h * 0.68f)
    )
  }
}
