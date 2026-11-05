package com.cineflow.ai.feature.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cineflow.ai.core.model.ProjectFilterTab
import com.cineflow.ai.core.model.ProjectSortOption
import com.cineflow.ai.core.model.VideoProject
import com.cineflow.ai.data.project.LocalProjectRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Project Management Dashboard (CineFlow AI Project Dashboard(1).png).
 * Uses exclusively real locally persisted Room projects (no hardcoded fake sample records).
 */
class ProjectManagementViewModel(
  private val repository: LocalProjectRepository
) : ViewModel() {

  val allProjects: StateFlow<List<VideoProject>> = repository.projectsFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedTab = MutableStateFlow(ProjectFilterTab.RECENT)
  val selectedTab: StateFlow<ProjectFilterTab> = _selectedTab.asStateFlow()

  private val _sortOption = MutableStateFlow(ProjectSortOption.UPDATED_DESC)
  val sortOption: StateFlow<ProjectSortOption> = _sortOption.asStateFlow()

  private val _showCreateDialog = MutableStateFlow(false)
  val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

  private val _createErrorMessage = MutableStateFlow<String?>(null)
  val createErrorMessage: StateFlow<String?> = _createErrorMessage.asStateFlow()

  private val _activeMenuProject = MutableStateFlow<VideoProject?>(null)
  val activeMenuProject: StateFlow<VideoProject?> = _activeMenuProject.asStateFlow()

  private val _renameTargetProject = MutableStateFlow<VideoProject?>(null)
  val renameTargetProject: StateFlow<VideoProject?> = _renameTargetProject.asStateFlow()

  private val _renameErrorMessage = MutableStateFlow<String?>(null)
  val renameErrorMessage: StateFlow<String?> = _renameErrorMessage.asStateFlow()

  private val _deleteTargetProject = MutableStateFlow<VideoProject?>(null)
  val deleteTargetProject: StateFlow<VideoProject?> = _deleteTargetProject.asStateFlow()

  private val _showManualBackupSheet = MutableStateFlow(false)
  val showManualBackupSheet: StateFlow<Boolean> = _showManualBackupSheet.asStateFlow()

  private val _backupStatusMessage = MutableStateFlow<String?>(null)
  val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

  private val _comingSoonFeature = MutableStateFlow<String?>(null)
  val comingSoonFeature: StateFlow<String?> = _comingSoonFeature.asStateFlow()

  val filteredAndSortedProjects: StateFlow<List<VideoProject>> = combine(
    allProjects,
    _searchQuery,
    _selectedTab,
    _sortOption
  ) { projects, query, tab, sort ->
    val tabFiltered = when (tab) {
      ProjectFilterTab.RECENT -> projects
        .sortedByDescending { it.updatedAtEpochMillis }
        .take(5)
      ProjectFilterTab.ALL_PROJECTS -> projects
      ProjectFilterTab.FAVORITES -> projects.filter { it.isFavorite }
    }

    val trimmedQuery = query.trim()
    val queryFiltered = if (trimmedQuery.isEmpty()) {
      tabFiltered
    } else {
      tabFiltered.filter { project ->
        project.name.contains(trimmedQuery, ignoreCase = true) ||
          project.aspectRatio.contains(trimmedQuery, ignoreCase = true) ||
          project.resolutionLabel.contains(trimmedQuery, ignoreCase = true) ||
          project.statusTag.contains(trimmedQuery, ignoreCase = true)
      }
    }

    when (sort) {
      ProjectSortOption.UPDATED_DESC -> queryFiltered.sortedByDescending { it.updatedAtEpochMillis }
      ProjectSortOption.CREATED_DESC -> queryFiltered.sortedByDescending { it.createdAtEpochMillis }
      ProjectSortOption.NAME_ASC -> queryFiltered.sortedBy { it.name.lowercase() }
      ProjectSortOption.DURATION_DESC -> queryFiltered.sortedByDescending { it.durationMicros }
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000L),
    initialValue = emptyList()
  )

  fun onSearchQueryChanged(query: String) {
    _searchQuery.value = query
  }

  fun onTabSelected(tab: ProjectFilterTab) {
    _selectedTab.value = tab
  }

  fun onSortOptionSelected(option: ProjectSortOption) {
    _sortOption.value = option
  }

  fun openCreateProjectDialog() {
    _createErrorMessage.value = null
    _showCreateDialog.value = true
  }

  fun dismissCreateProjectDialog() {
    _createErrorMessage.value = null
    _showCreateDialog.value = false
  }

  fun createNewProject(
    rawName: String,
    aspectRatio: String = "16:9",
    resolutionLabel: String = "1080p",
    fps: Int = 30,
    onProjectCreated: (VideoProject) -> Unit = {}
  ) {
    viewModelScope.launch {
      repository.createProject(
        rawName = rawName,
        aspectRatio = aspectRatio,
        frameRateNum = fps,
        frameRateDen = 1,
        resolutionLabel = resolutionLabel,
        durationMicros = 0L,
        statusTag = "Draft"
      ).onSuccess { created ->
        _createErrorMessage.value = null
        _showCreateDialog.value = false
        onProjectCreated(created)
      }.onFailure { error ->
        _createErrorMessage.value = error.message ?: "Invalid project name."
      }
    }
  }

  fun openProjectActionsMenu(project: VideoProject) {
    _activeMenuProject.value = project
  }

  fun dismissProjectActionsMenu() {
    _activeMenuProject.value = null
  }

  fun openRenameDialog(project: VideoProject) {
    _activeMenuProject.value = null
    _renameErrorMessage.value = null
    _renameTargetProject.value = project
  }

  fun dismissRenameDialog() {
    _renameErrorMessage.value = null
    _renameTargetProject.value = null
  }

  fun confirmRenameProject(newRawName: String) {
    val target = _renameTargetProject.value ?: return
    viewModelScope.launch {
      repository.renameProject(target.projectId, newRawName)
        .onSuccess {
          _renameErrorMessage.value = null
          _renameTargetProject.value = null
        }
        .onFailure { error ->
          _renameErrorMessage.value = error.message ?: "Failed to rename project."
        }
    }
  }

  fun duplicateProject(project: VideoProject) {
    _activeMenuProject.value = null
    viewModelScope.launch {
      repository.duplicateProject(project.projectId)
    }
  }

  fun toggleFavorite(project: VideoProject) {
    _activeMenuProject.value = null
    viewModelScope.launch {
      repository.toggleFavorite(project.projectId)
    }
  }

  fun openDeleteConfirmation(project: VideoProject) {
    _activeMenuProject.value = null
    _deleteTargetProject.value = project
  }

  fun dismissDeleteConfirmation() {
    _deleteTargetProject.value = null
  }

  fun confirmDeleteProject() {
    val target = _deleteTargetProject.value ?: return
    viewModelScope.launch {
      repository.deleteProject(target.projectId)
      _deleteTargetProject.value = null
    }
  }

  fun openManualBackupSheet() {
    _activeMenuProject.value = null
    _backupStatusMessage.value = null
    _showManualBackupSheet.value = true
  }

  fun dismissManualBackupSheet() {
    _backupStatusMessage.value = null
    _showManualBackupSheet.value = false
  }

  fun markProjectBackedUp(
    project: VideoProject,
    onSyncToCloud: suspend (VideoProject) -> Result<Unit>
  ) {
    viewModelScope.launch {
      onSyncToCloud(project)
        .onSuccess {
          repository.markProjectStatus(project.projectId, "Backed Up")
          _backupStatusMessage.value = "Backed up metadata for \"${project.name}\"."
        }
        .onFailure { err ->
          _backupStatusMessage.value = err.message ?: "Cloud metadata backup failed."
        }
    }
  }

  fun markAllProjectsBackedUp(
    onSyncToCloud: suspend (VideoProject) -> Result<Unit>
  ) {
    viewModelScope.launch {
      val snapshot = allProjects.value
      if (snapshot.isEmpty()) {
        _backupStatusMessage.value = "No local projects to back up."
        return@launch
      }
      var successCount = 0
      var firstError: String? = null
      snapshot.forEach { proj ->
        onSyncToCloud(proj)
          .onSuccess {
            repository.markProjectStatus(proj.projectId, "Backed Up")
            successCount++
          }
          .onFailure { err ->
            if (firstError == null) {
              firstError = err.message
            }
          }
      }
      _backupStatusMessage.value = if (firstError == null) {
        "Backed up metadata for $successCount project(s)."
      } else {
        firstError
      }
    }
  }

  fun showComingSoon(featureName: String) {
    _comingSoonFeature.value = featureName
  }

  fun dismissComingSoon() {
    _comingSoonFeature.value = null
  }

  companion object {
    fun formatDurationMmSs(durationMicros: Long): String {
      val totalSeconds = (durationMicros.coerceAtLeast(0L) / 1_000_000L).toInt()
      val minutes = totalSeconds / 60
      val seconds = totalSeconds % 60
      return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun formatTimestamp(epochMillis: Long): String {
      val formatter = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.US)
      return formatter.format(Date(epochMillis))
    }

    fun provideFactory(repository: LocalProjectRepository): ViewModelProvider.Factory {
      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return ProjectManagementViewModel(repository) as T
        }
      }
    }
  }
}
