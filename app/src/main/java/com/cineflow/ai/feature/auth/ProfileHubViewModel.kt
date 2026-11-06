package com.cineflow.ai.feature.auth

import androidx.lifecycle.ViewModel
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_PROFILE_HUB = "profile_hub"

enum class ProfileHubSection(
  val title: String,
  val subtitle: String
) {
  PERSONAL_INFORMATION(
    title = "Personal Information",
    subtitle = "Name, username, email, mobile"
  ),
  SUBSCRIPTION_PLAN(
    title = "Subscription & Plan",
    subtitle = "Manage your subscription, storage and benefits"
  ),
  CLOUD_BACKUP_RESTORE(
    title = "Cloud Backup & Restore",
    subtitle = "Backup projects to cloud and restore on any device"
  ),
  ACCOUNT_SECURITY(
    title = "Account Security",
    subtitle = "Password, account recovery and login activity"
  ),
  LINKED_ACCOUNTS(
    title = "Linked Accounts",
    subtitle = "Google and other sign-in methods"
  ),
  CLOUD_STORAGE_MANAGE(
    title = "Cloud Storage Management",
    subtitle = "2.6 GB of 5 GB cloud storage currently used"
  ),
  ACCOUNT_SETTINGS(
    title = "Account Settings",
    subtitle = "Preferences, privacy, and account management"
  )
}

/**
 * ViewModel for the Account / Profile Hub Screen (`CineFlow AI Neon Profile Dashboard.png`
 * & Master Prompt Section 6 & Section 10).
 */
class ProfileHubViewModel : ViewModel() {

  private val _displayName = MutableStateFlow("Manirul Hasan")
  val displayName: StateFlow<String> = _displayName.asStateFlow()

  private val _username = MutableStateFlow("manirul_editz")
  val username: StateFlow<String> = _username.asStateFlow()

  // Section 10: Public User ID format CF + 8 numeric digits (never exposes raw Firebase UID)
  private val _publicUid = MutableStateFlow("CF00100247")
  val publicUid: StateFlow<String> = _publicUid.asStateFlow()

  private val _planBadgeText = MutableStateFlow("Subscriber")
  val planBadgeText: StateFlow<String> = _planBadgeText.asStateFlow()

  private val _statusBadgeText = MutableStateFlow("Active")
  val statusBadgeText: StateFlow<String> = _statusBadgeText.asStateFlow()

  private val _projectsCount = MutableStateFlow(48)
  val projectsCount: StateFlow<Int> = _projectsCount.asStateFlow()

  private val _backupsCount = MutableStateFlow(12)
  val backupsCount: StateFlow<Int> = _backupsCount.asStateFlow()

  private val _usedStorageGb = MutableStateFlow(2.6f)
  val usedStorageGb: StateFlow<Float> = _usedStorageGb.asStateFlow()

  private val _totalStorageGb = MutableStateFlow(5.0f)
  val totalStorageGb: StateFlow<Float> = _totalStorageGb.asStateFlow()

  private val _selectedAvatarUri = MutableStateFlow<String?>(null)
  val selectedAvatarUri: StateFlow<String?> = _selectedAvatarUri.asStateFlow()

  private val _copiedUidFeedback = MutableStateFlow<String?>(null)
  val copiedUidFeedback: StateFlow<String?> = _copiedUidFeedback.asStateFlow()

  private val _activeSectionModal = MutableStateFlow<ProfileHubSection?>(null)
  val activeSectionModal: StateFlow<ProfileHubSection?> = _activeSectionModal.asStateFlow()

  fun syncIdentity(
    displayName: String?,
    username: String?,
    publicUid: String?,
    projectsCount: Int? = null
  ) {
    if (!displayName.isNullOrBlank()) {
      _displayName.value = displayName.trim()
    }
    if (!username.isNullOrBlank()) {
      _username.value = username.trim().removePrefix("@")
    }
    if (!publicUid.isNullOrBlank()) {
      _publicUid.value = normalizePublicUid(publicUid.trim())
    }
    if (projectsCount != null && projectsCount > 0) {
      _projectsCount.value = projectsCount
    }
  }

  fun copyPublicUid(): String {
    val uid = _publicUid.value
    _copiedUidFeedback.value = "Copied $uid to clipboard"
    return uid
  }

  fun clearCopiedFeedback() {
    _copiedUidFeedback.value = null
  }

  fun onAvatarSelected(uriString: String?) {
    _selectedAvatarUri.value = uriString
  }

  fun openSection(section: ProfileHubSection) {
    _activeSectionModal.value = section
  }

  fun dismissSection() {
    _activeSectionModal.value = null
  }

  companion object {
    fun normalizePublicUid(raw: String): String {
      val digits = raw.filter { it.isDigit() }.takeLast(8)
      if (digits.isEmpty()) return "CF00100247"
      val num = digits.toLongOrNull() ?: 100247L
      return String.format(Locale.US, "CF%08d", num)
    }
  }
}
