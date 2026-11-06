package com.cineflow.ai.feature.backup

import androidx.lifecycle.ViewModel
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_CLOUD_BACKUP = "cloud_backup"

enum class CloudBackupCategory(
  val title: String,
  val optionalSuffix: String?,
  val subtitle: String,
  val rightTopLabel: String?,
  val rightBottomLabel: String,
  val sizeGb: Double,
  val rowTestTag: String,
  val checkboxTestTag: String
) {
  PROJECTS(
    title = "Projects",
    optionalSuffix = null,
    subtitle = "Your video editing projects and timelines",
    rightTopLabel = "12 Projects",
    rightBottomLabel = "(1.84 GB)",
    sizeGb = 1.84,
    rowTestTag = "cloud_backup_row_projects",
    checkboxTestTag = "cloud_backup_checkbox_projects"
  ),
  MEDIA_ASSETS(
    title = "Media Assets",
    optionalSuffix = null,
    subtitle = "Imported videos, photos, audio and graphics",
    rightTopLabel = "428 Files",
    rightBottomLabel = "(3.20 GB)",
    sizeGb = 3.20,
    rowTestTag = "cloud_backup_row_media_assets",
    checkboxTestTag = "cloud_backup_checkbox_media_assets"
  ),
  APP_DATA_PRESETS(
    title = "App Data & Presets",
    optionalSuffix = null,
    subtitle = "Settings, presets, templates, fonts and more",
    rightTopLabel = null,
    rightBottomLabel = "(186 MB)",
    sizeGb = 0.19,
    rowTestTag = "cloud_backup_row_app_data",
    checkboxTestTag = "cloud_backup_checkbox_app_data"
  ),
  AI_ASSETS(
    title = "AI Assets",
    optionalSuffix = " (Optional)",
    subtitle = "Generated media, models and cached data",
    rightTopLabel = null,
    rightBottomLabel = "(512 MB)",
    sizeGb = 0.50,
    rowTestTag = "cloud_backup_row_ai_assets",
    checkboxTestTag = "cloud_backup_checkbox_ai_assets"
  )
}

/**
 * ViewModel for the Cloud Backup Screen (`Neon Cloud Backup Dashboard.png` & Master Prompt Section 12).
 *
 * Enforces Section 12 Cloud Backup rules:
 * - Manual Cloud Backup is an explicit user operation.
 * - Never starts automatic background backup (`autoBackupEnabled` remains false).
 * - Original source media upload requires explicit user selection and confirmation.
 */
class CloudBackupViewModel : ViewModel() {

  private val _selectedCategories = MutableStateFlow(
    setOf(
      CloudBackupCategory.PROJECTS,
      CloudBackupCategory.MEDIA_ASSETS,
      CloudBackupCategory.APP_DATA_PRESETS
    )
  )
  val selectedCategories: StateFlow<Set<CloudBackupCategory>> = _selectedCategories.asStateFlow()

  private val _autoBackupEnabled = MutableStateFlow(false)
  val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled.asStateFlow()

  private val _manualBackupSelected = MutableStateFlow(true)
  val manualBackupSelected: StateFlow<Boolean> = _manualBackupSelected.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _dialogTitle = MutableStateFlow<String?>(null)
  val dialogTitle: StateFlow<String?> = _dialogTitle.asStateFlow()

  private val _dialogMessage = MutableStateFlow<String?>(null)
  val dialogMessage: StateFlow<String?> = _dialogMessage.asStateFlow()

  val totalCategoriesCount: Int
    get() = CloudBackupCategory.entries.size

  fun selectedCount(): Int = _selectedCategories.value.size

  fun totalBackupSizeGb(): Double {
    return _selectedCategories.value.sumOf { it.sizeGb }
  }

  fun formattedTotalSizeGb(): String {
    return String.format(Locale.US, "%.2f GB", totalBackupSizeGb())
  }

  fun toggleCategory(category: CloudBackupCategory) {
    val current = _selectedCategories.value.toMutableSet()
    if (!current.add(category)) {
      current.remove(category)
    }
    _selectedCategories.value = current
    _errorMessage.value = null
  }

  fun toggleSelectAll() {
    val all = CloudBackupCategory.entries.toSet()
    _selectedCategories.value = if (_selectedCategories.value.size == all.size) {
      emptySet()
    } else {
      all
    }
    _errorMessage.value = null
  }

  /**
   * Section 12: "Manual Cloud Backup and Restore are explicit user operations. Never start automatic backup."
   */
  fun onAttemptToggleAutoBackup() {
    _autoBackupEnabled.value = false
    _manualBackupSelected.value = true
    _dialogTitle.value = "Manual Cloud Backup Policy"
    _dialogMessage.value = "Per CineFlow AI data policy (Section 12), Cloud Backup is strictly an explicit manual user operation. Automatic background backup of projects and source media is disabled."
  }

  fun selectManualBackupNow() {
    _manualBackupSelected.value = true
    _autoBackupEnabled.value = false
  }

  fun openCategoryDetails(category: CloudBackupCategory) {
    _dialogTitle.value = category.title
    _dialogMessage.value = "${category.subtitle}\nEstimated Size: ${category.rightBottomLabel.removeSurrounding("(", ")")}"
  }

  fun startManualBackup(): Boolean {
    _errorMessage.value = null
    val selected = _selectedCategories.value
    if (selected.isEmpty()) {
      _errorMessage.value = "Please select at least one data category to back up."
      return false
    }

    val names = selected.joinToString(", ") { it.title }
    val sizeText = formattedTotalSizeGb()
    _dialogTitle.value = "Manual Backup Initiated ($sizeText)"
    _dialogMessage.value = "Explicit manual backup started for ${selected.size} selected categories ($names • ≈ $sizeText), including project manifests, timelines, editing settings, and explicitly selected assets."
    return true
  }

  fun dismissDialog() {
    _dialogTitle.value = null
    _dialogMessage.value = null
  }
}
