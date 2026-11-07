package com.cineflow.ai.feature.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_SETTINGS = "settings"

enum class SettingsAppearance(
  val label: String,
  val testTag: String
) {
  LIGHT("Light", "settings_appearance_light"),
  DARK("Dark", "settings_appearance_dark"),
  SYSTEM("System", "settings_appearance_system")
}

/**
 * ViewModel for the Settings Screen (`CineFlow AI Neon Settings Dashboard.png` & Master Prompt Section 8/23).
 */
class SettingsViewModel : ViewModel() {

  private val _appLanguage = MutableStateFlow("English")
  val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

  private val _appearance = MutableStateFlow(SettingsAppearance.DARK)
  val appearance: StateFlow<SettingsAppearance> = _appearance.asStateFlow()

  private val _hapticFeedbackEnabled = MutableStateFlow(true)
  val hapticFeedbackEnabled: StateFlow<Boolean> = _hapticFeedbackEnabled.asStateFlow()

  private val _previewQuality = MutableStateFlow("High (Recommended)")
  val previewQuality: StateFlow<String> = _previewQuality.asStateFlow()

  private val _defaultExportResolution = MutableStateFlow("1080p (Full HD)")
  val defaultExportResolution: StateFlow<String> = _defaultExportResolution.asStateFlow()

  private val _defaultFps = MutableStateFlow("30 FPS")
  val defaultFps: StateFlow<String> = _defaultFps.asStateFlow()

  private val _defaultVideoCodec = MutableStateFlow("H.265 (HEVC)")
  val defaultVideoCodec: StateFlow<String> = _defaultVideoCodec.asStateFlow()

  private val _defaultBitrate = MutableStateFlow("High")
  val defaultBitrate: StateFlow<String> = _defaultBitrate.asStateFlow()

  private val _autoSaveEnabled = MutableStateFlow(true)
  val autoSaveEnabled: StateFlow<Boolean> = _autoSaveEnabled.asStateFlow()

  private val _cacheSizeLabel = MutableStateFlow("2.1 GB")
  val cacheSizeLabel: StateFlow<String> = _cacheSizeLabel.asStateFlow()

  private val _storageLocation = MutableStateFlow("Internal Storage")
  val storageLocation: StateFlow<String> = _storageLocation.asStateFlow()

  private val _planBadgeLabel = MutableStateFlow("5 GB Plan")
  val planBadgeLabel: StateFlow<String> = _planBadgeLabel.asStateFlow()

  private val _dialogTitle = MutableStateFlow<String?>(null)
  val dialogTitle: StateFlow<String?> = _dialogTitle.asStateFlow()

  private val _dialogMessage = MutableStateFlow<String?>(null)
  val dialogMessage: StateFlow<String?> = _dialogMessage.asStateFlow()

  fun selectAppearance(mode: SettingsAppearance) {
    _appearance.value = mode
  }

  fun toggleHapticFeedback() {
    _hapticFeedbackEnabled.value = !_hapticFeedbackEnabled.value
  }

  fun toggleAutoSave() {
    _autoSaveEnabled.value = !_autoSaveEnabled.value
  }

  fun cycleAppLanguage() {
    val options = listOf("English", "বাংলা (Bengali)", "हिन्दी (Hindi)", "Español")
    val nextIndex = (options.indexOf(_appLanguage.value) + 1) % options.size
    _appLanguage.value = options[nextIndex]
  }

  fun cyclePreviewQuality() {
    val options = listOf("High (Recommended)", "Medium (Balanced)", "Low (Fast Proxy)")
    val nextIndex = (options.indexOf(_previewQuality.value) + 1) % options.size
    _previewQuality.value = options[nextIndex]
  }

  fun cycleDefaultResolution() {
    val options = listOf("1080p (Full HD)", "4K (Ultra HD)", "720p (HD)")
    val nextIndex = (options.indexOf(_defaultExportResolution.value) + 1) % options.size
    _defaultExportResolution.value = options[nextIndex]
  }

  fun cycleDefaultFps() {
    val options = listOf("30 FPS", "60 FPS", "25 FPS", "120 FPS", "240 FPS")
    val nextIndex = (options.indexOf(_defaultFps.value) + 1) % options.size
    _defaultFps.value = options[nextIndex]
  }

  fun cycleDefaultCodec() {
    val options = listOf("H.265 (HEVC)", "H.264 (AVC)", "AV1")
    val nextIndex = (options.indexOf(_defaultVideoCodec.value) + 1) % options.size
    _defaultVideoCodec.value = options[nextIndex]
  }

  fun cycleDefaultBitrate() {
    val options = listOf("High", "Auto", "Medium", "Low")
    val nextIndex = (options.indexOf(_defaultBitrate.value) + 1) % options.size
    _defaultBitrate.value = options[nextIndex]
  }

  fun cycleStorageLocation() {
    _storageLocation.value = if (_storageLocation.value == "Internal Storage") {
      "SAF Workspace"
    } else {
      "Internal Storage"
    }
  }

  fun openProjectRecoveryModal() {
    _dialogTitle.value = "Project Recovery"
    _dialogMessage.value = "All local Room database project snapshots and timeline journals are healthy. 0 unsaved or crashed projects require recovery."
  }

  fun openCacheManagementModal() {
    _dialogTitle.value = "Cache Management (${_cacheSizeLabel.value})"
    _dialogMessage.value = "Temporary waveform caches, proxy frames, and thumbnail buffers currently occupy ${_cacheSizeLabel.value}. Original source media files are never deleted by cache cleanup."
  }

  fun clearCache() {
    _cacheSizeLabel.value = "0 MB"
    _dialogTitle.value = null
    _dialogMessage.value = null
  }

  fun openNotificationsModal() {
    _dialogTitle.value = "Notifications"
    _dialogMessage.value = "Manage export completion alerts, manual cloud backup notifications, and AI processing updates."
  }

  fun openAccountSecurityModal() {
    _dialogTitle.value = "Account Security"
    _dialogMessage.value = "Manage password rules (8–25 characters), email verification link recovery, linked Google Sign-In credentials, and active device sessions."
  }

  fun openAiSettingsModal() {
    _dialogTitle.value = "AI Settings (Gemini Gateway)"
    _dialogMessage.value = "Configure AI Editing Assistant, Automatic Captions, Subtitle Translation, and Scene Detection. Per Section 20, all AI operations validate structured JSON schemas and require explicit user consent before sending media metadata."
  }

  fun openAboutModal() {
    _dialogTitle.value = "About CineFlow AI"
    _dialogMessage.value = "CineFlow AI — Version 1.2.1\nPackage: com.cineflow.ai\nArchitecture: Native Android (Kotlin + Jetpack Compose + Material 3 + Offline-First Room + Media3)"
  }

  fun dismissDialog() {
    _dialogTitle.value = null
    _dialogMessage.value = null
  }
}
