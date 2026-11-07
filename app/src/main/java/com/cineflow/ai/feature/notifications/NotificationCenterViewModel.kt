package com.cineflow.ai.feature.notifications

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_NOTIFICATION_CENTER = "notification_center"

enum class NotificationCategoryFilter(
  val label: String,
  val testTag: String
) {
  ALL("All", "notification_filter_all"),
  ACCOUNT("Account", "notification_filter_account"),
  PROJECTS("Projects", "notification_filter_projects"),
  BACKUP("Backup", "notification_filter_backup"),
  SUBSCRIPTION("Subscription", "notification_filter_subscription")
}

enum class NotificationIconStyle {
  CLOUD_BACKUP_SUCCESS,
  SUBSCRIPTION_CROWN,
  EXPORT_TRAY,
  PROJECT_CLAPPER,
  APP_UPDATE_DOWNLOAD
}

data class NotificationEntry(
  val id: String,
  val title: String,
  val message: String,
  val timestampLabel: String,
  val category: NotificationCategoryFilter,
  val iconStyle: NotificationIconStyle,
  val isUnread: Boolean
)

/**
 * ViewModel for TASK-018: Notification Center Screen
 * (`CineFlow AI Notification Center.png` — Reference Index 11).
 */
class NotificationCenterViewModel : ViewModel() {

  private val _selectedFilter = MutableStateFlow(NotificationCategoryFilter.ALL)
  val selectedFilter: StateFlow<NotificationCategoryFilter> = _selectedFilter.asStateFlow()

  private val _notifications = MutableStateFlow(
    listOf(
      NotificationEntry(
        id = "notif_backup_01",
        title = "Cloud Backup Completed",
        message = "Your 3 projects (2.6 GB) have been backed up successfully.",
        timestampLabel = "2 min ago",
        category = NotificationCategoryFilter.BACKUP,
        iconStyle = NotificationIconStyle.CLOUD_BACKUP_SUCCESS,
        isUnread = true
      ),
      NotificationEntry(
        id = "notif_sub_02",
        title = "Subscription Renewed",
        message = "Your Pro Plan has been renewed successfully.\nNext billing: Feb 15, 2026.",
        timestampLabel = "1 hour ago",
        category = NotificationCategoryFilter.SUBSCRIPTION,
        iconStyle = NotificationIconStyle.SUBSCRIPTION_CROWN,
        isUnread = true
      ),
      NotificationEntry(
        id = "notif_export_03",
        title = "Export Completed",
        message = "“Travel Vlog.mp4” has been exported successfully (4K, 1.2 GB).",
        timestampLabel = "3 hours ago",
        category = NotificationCategoryFilter.PROJECTS,
        iconStyle = NotificationIconStyle.EXPORT_TRAY,
        isUnread = true
      ),
      NotificationEntry(
        id = "notif_sync_04",
        title = "Project Synced",
        message = "“Nature Edit” is now synced across your devices.",
        timestampLabel = "5 hours ago",
        category = NotificationCategoryFilter.PROJECTS,
        iconStyle = NotificationIconStyle.PROJECT_CLAPPER,
        isUnread = false
      ),
      NotificationEntry(
        id = "notif_update_05",
        title = "App Update Available",
        message = "A new version (v2.3.0) is available with new features and improvements.",
        timestampLabel = "Yesterday",
        category = NotificationCategoryFilter.ACCOUNT,
        iconStyle = NotificationIconStyle.APP_UPDATE_DOWNLOAD,
        isUnread = false
      )
    )
  )
  val notifications: StateFlow<List<NotificationEntry>> = _notifications.asStateFlow()

  private val _appNotificationsEnabled = MutableStateFlow(true)
  val appNotificationsEnabled: StateFlow<Boolean> = _appNotificationsEnabled.asStateFlow()

  private val _backupRestoreAlertsEnabled = MutableStateFlow(true)
  val backupRestoreAlertsEnabled: StateFlow<Boolean> = _backupRestoreAlertsEnabled.asStateFlow()

  private val _subscriptionAlertsEnabled = MutableStateFlow(true)
  val subscriptionAlertsEnabled: StateFlow<Boolean> = _subscriptionAlertsEnabled.asStateFlow()

  private val _accountAlertsEnabled = MutableStateFlow(true)
  val accountAlertsEnabled: StateFlow<Boolean> = _accountAlertsEnabled.asStateFlow()

  private val _statusDialogTitle = MutableStateFlow<String?>(null)
  val statusDialogTitle: StateFlow<String?> = _statusDialogTitle.asStateFlow()

  private val _statusDialogMessage = MutableStateFlow<String?>(null)
  val statusDialogMessage: StateFlow<String?> = _statusDialogMessage.asStateFlow()

  fun selectFilter(filter: NotificationCategoryFilter) {
    _selectedFilter.value = filter
  }

  fun filteredNotifications(): List<NotificationEntry> {
    val activeFilter = _selectedFilter.value
    val all = _notifications.value
    return if (activeFilter == NotificationCategoryFilter.ALL) {
      all
    } else {
      all.filter { it.category == activeFilter }
    }
  }

  fun unreadCount(): Int = _notifications.value.count { it.isUnread }

  fun markAllAsRead() {
    _notifications.value = _notifications.value.map { it.copy(isUnread = false) }
  }

  fun markNotificationAsRead(id: String) {
    _notifications.value = _notifications.value.map { entry ->
      if (entry.id == id) entry.copy(isUnread = false) else entry
    }
  }

  fun openNotificationDetail(entry: NotificationEntry) {
    markNotificationAsRead(entry.id)
    _statusDialogTitle.value = entry.title
    _statusDialogMessage.value = "${entry.message}\n\nReceived: ${entry.timestampLabel}"
  }

  fun openViewAllNotificationsDialog() {
    val total = _notifications.value.size
    val unread = unreadCount()
    _statusDialogTitle.value = "All Notifications ($total)"
    _statusDialogMessage.value =
      "Showing all $total notifications ($unread unread) across Account, Projects, Cloud Backup, and Subscription channels."
  }

  fun toggleAppNotifications() {
    _appNotificationsEnabled.value = !_appNotificationsEnabled.value
  }

  fun toggleBackupRestoreAlerts() {
    _backupRestoreAlertsEnabled.value = !_backupRestoreAlertsEnabled.value
  }

  fun toggleSubscriptionAlerts() {
    _subscriptionAlertsEnabled.value = !_subscriptionAlertsEnabled.value
  }

  fun toggleAccountAlerts() {
    _accountAlertsEnabled.value = !_accountAlertsEnabled.value
  }

  fun dismissStatusDialog() {
    _statusDialogTitle.value = null
    _statusDialogMessage.value = null
  }
}
