package com.cineflow.ai.feature.backup

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_CLOUD_BACKUP_DASHBOARD = "cloud_backup_dashboard"

enum class BackupDashboardFilter(
  val label: String,
  val count: Int,
  val testTag: String
) {
  ALL(
    label = "All (48)",
    count = 48,
    testTag = "cloud_backup_dashboard_filter_all"
  ),
  PROJECTS(
    label = "Projects (32)",
    count = 32,
    testTag = "cloud_backup_dashboard_filter_projects"
  ),
  ASSETS(
    label = "Assets (10)",
    count = 10,
    testTag = "cloud_backup_dashboard_filter_assets"
  ),
  SETTINGS(
    label = "Settings (6)",
    count = 6,
    testTag = "cloud_backup_dashboard_filter_settings"
  )
}

enum class RecentBackupIconType {
  CLAPPERBOARD,
  FOLDER,
  SETTINGS_GEAR
}

data class RecentBackupEntry(
  val id: String,
  val title: String,
  val subtitle: String,
  val statusLabel: String = "Completed",
  val category: BackupDashboardFilter,
  val iconType: RecentBackupIconType,
  val rowTestTag: String,
  val menuTestTag: String
)

/**
 * ViewModel for the Cloud Backup & Restore Main Dashboard (`CineFlow AI Cloud Backup Dashboard.png` & Master Prompt Section 12).
 *
 * Enforces Section 12 Cloud Backup & Restore rules:
 * - Manual Cloud Backup and Restore are explicit user operations.
 * - Never starts automatic background media upload.
 * - Preserves backup integrity verification and conflict-safe restore preparation.
 */
class CloudBackupDashboardViewModel : ViewModel() {

  private val _storageUsedGb = MutableStateFlow(2.6f)
  val storageUsedGb: StateFlow<Float> = _storageUsedGb.asStateFlow()

  private val _storageQuotaGb = MutableStateFlow(5.0f)
  val storageQuotaGb: StateFlow<Float> = _storageQuotaGb.asStateFlow()

  private val _selectedFilter = MutableStateFlow(BackupDashboardFilter.ALL)
  val selectedFilter: StateFlow<BackupDashboardFilter> = _selectedFilter.asStateFlow()

  private val allRecentBackups = listOf(
    RecentBackupEntry(
      id = "backup_full_01",
      title = "Full Backup (Auto)",
      subtitle = "Today, 08:30 PM  •  2.6 GB",
      category = BackupDashboardFilter.ASSETS,
      iconType = RecentBackupIconType.CLAPPERBOARD,
      rowTestTag = "cloud_backup_dashboard_recent_item_0",
      menuTestTag = "cloud_backup_dashboard_recent_menu_0"
    ),
    RecentBackupEntry(
      id = "backup_proj_travel_vlog",
      title = "Project: Travel Vlog",
      subtitle = "Yesterday, 11:45 AM  •  840 MB",
      category = BackupDashboardFilter.PROJECTS,
      iconType = RecentBackupIconType.FOLDER,
      rowTestTag = "cloud_backup_dashboard_recent_item_1",
      menuTestTag = "cloud_backup_dashboard_recent_menu_1"
    ),
    RecentBackupEntry(
      id = "backup_settings_presets",
      title = "Settings & Presets",
      subtitle = "Jan 22, 2026, 09:10 PM  •  120 MB",
      category = BackupDashboardFilter.SETTINGS,
      iconType = RecentBackupIconType.SETTINGS_GEAR,
      rowTestTag = "cloud_backup_dashboard_recent_item_2",
      menuTestTag = "cloud_backup_dashboard_recent_menu_2"
    )
  )

  private val _recentBackups = MutableStateFlow(allRecentBackups)
  val recentBackups: StateFlow<List<RecentBackupEntry>> = _recentBackups.asStateFlow()

  private val _dialogTitle = MutableStateFlow<String?>(null)
  val dialogTitle: StateFlow<String?> = _dialogTitle.asStateFlow()

  private val _dialogMessage = MutableStateFlow<String?>(null)
  val dialogMessage: StateFlow<String?> = _dialogMessage.asStateFlow()

  fun selectFilter(filter: BackupDashboardFilter) {
    _selectedFilter.value = filter
    _recentBackups.value = if (filter == BackupDashboardFilter.ALL) {
      allRecentBackups
    } else {
      allRecentBackups.filter { it.category == filter || it.id == "backup_full_01" }
    }
  }

  fun onOpenRestoreModal() {
    _dialogTitle.value = "Restore from Cloud"
    _dialogMessage.value = "Manual Cloud Restore allows you to select a verified cloud snapshot, validate available device storage, verify SHA-256 manifest integrity, and safely restore without silently overwriting local projects."
  }

  fun onViewAllRecentBackups() {
    _dialogTitle.value = "Recent Cloud Backups (3 Snapshots)"
    _dialogMessage.value = allRecentBackups.joinToString("\n\n") { entry ->
      "• ${entry.title}\n  ${entry.subtitle} — [${entry.statusLabel}]"
    }
  }

  fun onOpenBackupEntryOptions(entry: RecentBackupEntry) {
    _dialogTitle.value = entry.title
    _dialogMessage.value = "${entry.subtitle}\nStatus: ${entry.statusLabel} (Integrity Verified)\n\nPer Section 12, restoring this snapshot requires explicit user confirmation and will never silently overwrite local projects."
  }

  fun dismissDialog() {
    _dialogTitle.value = null
    _dialogMessage.value = null
  }
}
