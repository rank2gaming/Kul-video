package com.cineflow.ai.feature.projects

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R
import com.cineflow.ai.core.model.ProjectFilterTab
import com.cineflow.ai.core.model.VideoProject
import com.cineflow.ai.feature.home.BottomNavTab
import com.cineflow.ai.ui.theme.CineFlowBadgeRed
import com.cineflow.ai.ui.theme.CineFlowCyan
import com.cineflow.ai.ui.theme.CineFlowPurple
import com.cineflow.ai.ui.theme.PmBadgeBlue
import com.cineflow.ai.ui.theme.PmBadgeFavorites
import com.cineflow.ai.ui.theme.PmBadgePurple
import com.cineflow.ai.ui.theme.PmCardBgEnd
import com.cineflow.ai.ui.theme.PmCardBgStart
import com.cineflow.ai.ui.theme.PmCardBorder
import com.cineflow.ai.ui.theme.PmCloudGreen
import com.cineflow.ai.ui.theme.PmCosmicBgBottom
import com.cineflow.ai.ui.theme.PmCosmicBgTop
import com.cineflow.ai.ui.theme.PmTabActiveBgEnd
import com.cineflow.ai.ui.theme.PmTabActiveBgStart
import com.cineflow.ai.ui.theme.PmTabContainerBg
import com.cineflow.ai.ui.theme.PmTextMuted
import com.cineflow.ai.ui.theme.PmTextPrimary
import com.cineflow.ai.ui.theme.PmTextSecondary

const val ROUTE_EDITOR_BOUNDARY = "editor_entry_boundary"

/**
 * Project Management Dashboard Screen implemented strictly from approved reference image:
 * `CineFlow AI Project Dashboard(1).png`.
 *
 * Uses exclusively real locally persisted Room projects (`LocalProjectRepository`) —
 * demo project rows are excluded when the local database is empty, while the header,
 * colors, search row, `+ Create New Project` button, `Recent` / `All Projects` / `Favorites`
 * tab bar, bottom summary bar, and bottom navigation bar match the reference image exactly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagementScreen(
  viewModel: ProjectManagementViewModel,
  isSignedIn: Boolean,
  onBackToHome: () -> Unit,
  onOpenProjectInEditor: (VideoProject) -> Unit,
  onSyncProjectToCloud: suspend (VideoProject) -> Result<Unit>,
  onNavigateToCreateProject: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBackToHome)

  val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
  val displayedProjects by viewModel.filteredAndSortedProjects.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

  val showCreateDialog by viewModel.showCreateDialog.collectAsStateWithLifecycle()
  val createErrorMessage by viewModel.createErrorMessage.collectAsStateWithLifecycle()
  val activeMenuProject by viewModel.activeMenuProject.collectAsStateWithLifecycle()
  val renameTargetProject by viewModel.renameTargetProject.collectAsStateWithLifecycle()
  val renameErrorMessage by viewModel.renameErrorMessage.collectAsStateWithLifecycle()
  val deleteTargetProject by viewModel.deleteTargetProject.collectAsStateWithLifecycle()
  val showManualBackupSheet by viewModel.showManualBackupSheet.collectAsStateWithLifecycle()
  val backupStatusMessage by viewModel.backupStatusMessage.collectAsStateWithLifecycle()
  val comingSoonFeature by viewModel.comingSoonFeature.collectAsStateWithLifecycle()

  val recentCount = remember(allProjects) { allProjects.take(5).size }
  val allCount = allProjects.size
  val favoritesCount = remember(allProjects) { allProjects.count { it.isFavorite } }
  val handleCreateProjectClick = {
    if (onNavigateToCreateProject != null) {
      viewModel.dismissCreateProjectDialog()
      onNavigateToCreateProject()
    } else {
      viewModel.openCreateProjectDialog()
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(PmCosmicBgTop, PmCosmicBgBottom, Color(0xFF0E0648))
        )
      )
      .testTag("project_management_screen"),
    containerColor = Color.Transparent,
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF0C053D))
      ) {
        // Bottom Summary & Cloud Backup Bar ("Total Projects: X | Cloud Backup: Available | Manage >")
        ProjectBackupSummaryBar(
          totalProjectsCount = allCount,
          onManageBackupClick = viewModel::openManualBackupSheet,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        // Exact Reference Bottom Navigation Bar
        ProjectReferenceBottomNavigationBar(
          onTabSelected = { tab ->
            when (tab) {
              BottomNavTab.HOME -> onBackToHome()
              BottomNavTab.PROJECTS -> Unit
              BottomNavTab.NOTIFICATIONS -> viewModel.showComingSoon("Notification Center")
              BottomNavTab.SETTINGS -> viewModel.showComingSoon("Settings")
            }
          }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 600.dp)
      ) {
        // 1. Exact Full-Bleed Reference Top Header & Hero Clapperboard Banner
        ProjectReferenceTopHeaderBanner(
          onBackClick = onBackToHome,
          onCloudBackupClick = viewModel::openManualBackupSheet,
          onSettingsClick = { viewModel.showComingSoon("Settings") }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
        ) {
          // 2. Search Bar ("Search projects...") + "+ Create New Project" Neon Button Row
          ProjectSearchAndCreateRow(
            searchQuery = searchQuery,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onCreateNewProjectClick = handleCreateProjectClick
          )

          Spacer(modifier = Modifier.height(12.dp))

          // 3. Segmented Filter Tabs Bar (Recent [count] | All Projects [count] | Favorites [count])
          ProjectFilterTabsRow(
            selectedTab = selectedTab,
            recentCount = recentCount,
            allCount = allCount,
            favoritesCount = favoritesCount,
            onTabSelected = viewModel::onTabSelected
          )

          Spacer(modifier = Modifier.height(12.dp))

          // 4. Real Persisted Projects List (Demo records excluded; shows real user projects)
          if (displayedProjects.isEmpty()) {
            ProjectEmptyStateCard(
              hasAnyProjects = allProjects.isNotEmpty(),
              searchQuery = searchQuery,
              selectedTab = selectedTab,
              onCreateProjectClick = handleCreateProjectClick,
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            )
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("project_list"),
              verticalArrangement = Arrangement.spacedBy(10.dp),
              contentPadding = PaddingValues(bottom = 8.dp)
            ) {
              items(displayedProjects, key = { it.projectId }) { project ->
                ProjectDashboardCard(
                  project = project,
                  onCardClick = { onOpenProjectInEditor(project) },
                  onMenuClick = { viewModel.openProjectActionsMenu(project) }
                )
              }
            }
          }
        }
      }
    }
  }

  // Create New Project Dialog (Validates name, handles duplicate names, persists in Room, opens Editor boundary)
  if (showCreateDialog) {
    CreateNewProjectDialog(
      errorMessage = createErrorMessage,
      onDismiss = viewModel::dismissCreateProjectDialog,
      onCreateProject = { name, aspectRatio, resolution, fps ->
        viewModel.createNewProject(
          rawName = name,
          aspectRatio = aspectRatio,
          resolutionLabel = resolution,
          fps = fps,
          onProjectCreated = onOpenProjectInEditor
        )
      }
    )
  }

  // Project Item Actions Sheet (Open, Rename, Duplicate, Favorite, Manual Backup, Delete)
  activeMenuProject?.let { project ->
    ProjectActionsBottomSheet(
      project = project,
      onDismiss = viewModel::dismissProjectActionsMenu,
      onOpenClick = {
        viewModel.dismissProjectActionsMenu()
        onOpenProjectInEditor(project)
      },
      onRenameClick = { viewModel.openRenameDialog(project) },
      onDuplicateClick = { viewModel.duplicateProject(project) },
      onToggleFavoriteClick = { viewModel.toggleFavorite(project) },
      onBackupClick = {
        viewModel.dismissProjectActionsMenu()
        viewModel.openManualBackupSheet()
      },
      onDeleteClick = { viewModel.openDeleteConfirmation(project) }
    )
  }

  // Rename Project Dialog
  renameTargetProject?.let { project ->
    RenameProjectDialog(
      project = project,
      errorMessage = renameErrorMessage,
      onDismiss = viewModel::dismissRenameDialog,
      onConfirmRename = viewModel::confirmRenameProject
    )
  }

  // Delete Project Confirmation Dialog
  deleteTargetProject?.let { project ->
    AlertDialog(
      onDismissRequest = viewModel::dismissDeleteConfirmation,
      title = {
        Text(
          text = "Delete Project?",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = "Are you sure you want to permanently delete \"${project.name}\" from local storage?"
        )
      },
      confirmButton = {
        Button(
          onClick = viewModel::confirmDeleteProject,
          colors = ButtonDefaults.buttonColors(containerColor = CineFlowBadgeRed),
          modifier = Modifier.testTag("delete_project_confirm_button")
        ) {
          Text("Delete", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = viewModel::dismissDeleteConfirmation) {
          Text("Cancel")
        }
      },
      modifier = Modifier.testTag("delete_project_dialog")
    )
  }

  // Manual Cloud Backup Sheet
  if (showManualBackupSheet) {
    ManualCloudBackupSheet(
      projects = allProjects,
      isSignedIn = isSignedIn,
      statusMessage = backupStatusMessage,
      onDismiss = viewModel::dismissManualBackupSheet,
      onBackupSingleProject = { project ->
        viewModel.markProjectBackedUp(project, onSyncProjectToCloud)
      },
      onBackupAllProjects = {
        viewModel.markAllProjectsBackedUp(onSyncProjectToCloud)
      }
    )
  }

  // Honest Coming Soon Dialog for non-scope tabs
  comingSoonFeature?.let { featureTitle ->
    AlertDialog(
      onDismissRequest = viewModel::dismissComingSoon,
      title = {
        Text(
          text = stringResource(R.string.coming_soon_title),
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = stringResource(R.string.coming_soon_body, featureTitle)
        )
      },
      confirmButton = {
        TextButton(
          onClick = viewModel::dismissComingSoon,
          modifier = Modifier.testTag("pm_coming_soon_dismiss_button")
        ) {
          Text(
            text = stringResource(R.string.action_got_it),
            color = CineFlowPurple,
            fontWeight = FontWeight.Bold
          )
        }
      },
      modifier = Modifier.testTag("pm_coming_soon_dialog")
    )
  }
}

/**
 * Full-bleed Top Header & Hero Clapperboard Banner matching `CineFlow AI Project Dashboard(1).png`
 * (`R.drawable.img_pm_top_header_banner`) with interactive hit-targets for Back, Cloud Backup, and Settings.
 */
@Composable
private fun ProjectReferenceTopHeaderBanner(
  onBackClick: () -> Unit,
  onCloudBackupClick: () -> Unit,
  onSettingsClick: () -> Unit
) {
  val backDesc = stringResource(R.string.pm_cd_back)
  val cloudDesc = stringResource(R.string.pm_cd_cloud_backup)
  val settingsDesc = stringResource(R.string.pm_cd_settings)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(941f / 236f)
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_pm_top_header_banner),
      contentDescription = stringResource(R.string.pm_title),
      contentScale = ContentScale.FillBounds,
      modifier = Modifier.fillMaxSize()
    )

    // Back arrow interactive target (top-left)
    Box(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(start = 8.dp, top = 4.dp)
        .size(48.dp)
        .clip(CircleShape)
        .clickable(onClick = onBackClick)
        .semantics { contentDescription = backDesc }
        .testTag("project_back_button")
    )

    // Top-right Cloud & Settings interactive targets
    Row(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(end = 10.dp, top = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .clickable(onClick = onCloudBackupClick)
          .semantics { contentDescription = cloudDesc }
          .testTag("project_top_cloud_button")
      )
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .clickable(onClick = onSettingsClick)
          .semantics { contentDescription = settingsDesc }
          .testTag("project_top_settings_button")
      )
    }
  }
}

/**
 * Search Bar + "+ Create New Project" Row matching `CineFlow AI Project Dashboard(1).png`.
 */
@Composable
private fun ProjectSearchAndCreateRow(
  searchQuery: String,
  onSearchQueryChanged: (String) -> Unit,
  onCreateNewProjectClick: () -> Unit
) {
  val createDesc = stringResource(R.string.pm_create_new_project)

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Search projects pill input (exact dark indigo pill from reference)
    Row(
      modifier = Modifier
        .weight(1.92f)
        .height(48.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(
          brush = Brush.horizontalGradient(
            listOf(Color(0xFF1F1864), Color(0xFF14114E))
          )
        )
        .border(
          width = 1.2.dp,
          brush = Brush.horizontalGradient(
            listOf(Color(0xFF433B95), Color(0xFF2B256E))
          ),
          shape = RoundedCornerShape(24.dp)
        )
        .padding(horizontal = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = stringResource(R.string.cd_search_icon),
        tint = Color.White,
        modifier = Modifier.size(23.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.CenterStart
      ) {
        if (searchQuery.isEmpty()) {
          Text(
            text = stringResource(R.string.pm_search_placeholder),
            color = PmTextMuted,
            fontSize = 14.5.sp
          )
        }
        BasicTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChanged,
          singleLine = true,
          textStyle = TextStyle(
            color = PmTextPrimary,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium
          ),
          cursorBrush = SolidColor(CineFlowCyan),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_search_input")
        )
      }
      if (searchQuery.isNotEmpty()) {
        IconButton(
          onClick = { onSearchQueryChanged("") },
          modifier = Modifier
            .size(32.dp)
            .testTag("project_search_clear_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.cd_clear_search),
            tint = PmTextSecondary,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // Exact "+ Create New Project" Neon Pill Button from reference
    Box(
      modifier = Modifier
        .weight(1.08f)
        .height(50.dp)
        .clip(RoundedCornerShape(25.dp))
        .clickable(onClick = onCreateNewProjectClick)
        .semantics { contentDescription = createDesc }
        .testTag("project_create_new_button")
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_pm_create_button),
        contentDescription = createDesc,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}

/**
 * Segmented Filter Bar (`Recent`, `All Projects`, `Favorites`) matching
 * `CineFlow AI Project Dashboard(1).png` with vertical dividers and bottom neon glow on active tab.
 */
@Composable
private fun ProjectFilterTabsRow(
  selectedTab: ProjectFilterTab,
  recentCount: Int,
  allCount: Int,
  favoritesCount: Int,
  onTabSelected: (ProjectFilterTab) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(44.dp)
      .clip(RoundedCornerShape(22.dp))
      .background(PmTabContainerBg)
      .border(1.dp, Color(0xFF2D2676), RoundedCornerShape(22.dp)),
    verticalAlignment = Alignment.CenterVertically
  ) {
    ProjectFilterTabButton(
      title = stringResource(R.string.pm_tab_recent),
      count = recentCount,
      icon = Icons.Default.History,
      badgeColor = PmBadgePurple,
      isSelected = selectedTab == ProjectFilterTab.RECENT,
      testTag = "project_tab_recent",
      onClick = { onTabSelected(ProjectFilterTab.RECENT) },
      modifier = Modifier.weight(1f)
    )

    Box(
      modifier = Modifier
        .width(1.dp)
        .height(20.dp)
        .background(Color(0xFF393282))
    )

    ProjectFilterTabButton(
      title = stringResource(R.string.pm_tab_all_projects),
      count = allCount,
      icon = Icons.Default.GridView,
      badgeColor = PmBadgeBlue,
      isSelected = selectedTab == ProjectFilterTab.ALL_PROJECTS,
      testTag = "project_tab_all",
      onClick = { onTabSelected(ProjectFilterTab.ALL_PROJECTS) },
      modifier = Modifier.weight(1.18f)
    )

    Box(
      modifier = Modifier
        .width(1.dp)
        .height(20.dp)
        .background(Color(0xFF393282))
    )

    ProjectFilterTabButton(
      title = stringResource(R.string.pm_tab_favorites),
      count = favoritesCount,
      icon = Icons.Default.Star,
      badgeColor = PmBadgeFavorites,
      isSelected = selectedTab == ProjectFilterTab.FAVORITES,
      testTag = "project_tab_favorites",
      onClick = { onTabSelected(ProjectFilterTab.FAVORITES) },
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun ProjectFilterTabButton(
  title: String,
  count: Int,
  icon: ImageVector,
  badgeColor: Color,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxHeight()
      .clip(RoundedCornerShape(22.dp))
      .background(
        if (isSelected) {
          Brush.horizontalGradient(listOf(PmTabActiveBgStart, PmTabActiveBgEnd))
        } else {
          Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
        }
      )
      .then(
        if (isSelected) {
          Modifier.border(1.dp, Color(0xFF7E22CE), RoundedCornerShape(22.dp))
        } else {
          Modifier
        }
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 6.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = title,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.width(5.dp))
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(badgeColor)
          .padding(horizontal = 6.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = count.toString(),
          color = Color.White,
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold
        )
      }
    }

    // Glowing bottom neon indicator line when selected (matching reference)
    if (isSelected) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth(0.75f)
          .height(2.5.dp)
          .background(
            brush = Brush.horizontalGradient(
              listOf(
                Color.Transparent,
                Color(0xFFE9D5FF),
                Color(0xFF38BDF8),
                Color.Transparent
              )
            )
          )
      )
    }
  }
}

/**
 * Project Row Card matching CineFlow AI Project Dashboard(1).png:
 * - Scenic thumbnail + Play overlay + Duration pill on left
 * - Bold project name, Calendar timestamp, Clock duration/resolution/FPS, "Project" + status pills
 * - 3-dot MoreVert menu button on right
 */
@Composable
fun ProjectDashboardCard(
  project: VideoProject,
  onCardClick: () -> Unit,
  onMenuClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val durationFormatted = remember(project.durationMicros) {
    ProjectManagementViewModel.formatDurationMmSs(project.durationMicros)
  }
  val dateFormatted = remember(project.updatedAtEpochMillis) {
    ProjectManagementViewModel.formatTimestamp(project.updatedAtEpochMillis)
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(
        brush = Brush.horizontalGradient(
          colors = listOf(PmCardBgStart, PmCardBgEnd)
        )
      )
      .border(1.dp, PmCardBorder, RoundedCornerShape(16.dp))
      .clickable(onClick = onCardClick)
      .padding(9.dp)
      .testTag("project_card_${project.projectId}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    ProjectThumbnailPreview(
      visualThemeIndex = project.visualThemeIndex,
      durationText = durationFormatted
    )

    Spacer(modifier = Modifier.width(12.dp))

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = project.name,
          color = PmTextPrimary,
          fontSize = 15.5.sp,
          fontWeight = FontWeight.ExtraBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f, fill = false)
        )
        if (project.isFavorite) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Favorite project",
            tint = Color(0xFFFACC15),
            modifier = Modifier.size(15.dp)
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CalendarToday,
          contentDescription = null,
          tint = PmTextSecondary,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = dateFormatted,
          color = PmTextSecondary,
          fontSize = 11.5.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Schedule,
          contentDescription = null,
          tint = PmTextSecondary,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = "$durationFormatted • ${project.resolutionLabel} • ${project.displayFps} FPS",
          color = PmTextSecondary,
          fontSize = 11.5.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(2.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ProjectStatusPill(text = stringResource(R.string.pm_tag_project))
        ProjectStatusPill(text = project.statusTag)
      }
    }

    IconButton(
      onClick = onMenuClick,
      modifier = Modifier
        .size(48.dp)
        .testTag("project_menu_${project.projectId}")
    ) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = stringResource(R.string.pm_cd_project_options),
        tint = PmTextPrimary
      )
    }
  }
}

@Composable
private fun ProjectEmptyStateCard(
  hasAnyProjects: Boolean,
  searchQuery: String,
  selectedTab: ProjectFilterTab,
  onCreateProjectClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.testTag("project_empty_state"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .background(
          brush = Brush.verticalGradient(
            listOf(PmCardBgStart, PmCardBgEnd)
          )
        )
        .border(1.dp, PmCardBorder, RoundedCornerShape(18.dp))
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(CircleShape)
          .background(Color(0x337C3AED))
          .border(1.dp, Color(0xFF7C3AED), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.FolderOpen,
          contentDescription = null,
          tint = CineFlowCyan,
          modifier = Modifier.size(26.dp)
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = when {
          searchQuery.isNotBlank() -> "No Matching Projects"
          hasAnyProjects && selectedTab == ProjectFilterTab.FAVORITES -> "No Favorite Projects Yet"
          else -> stringResource(R.string.pm_empty_title)
        },
        color = PmTextPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = when {
          searchQuery.isNotBlank() -> "No local projects matched \"$searchQuery\". Try clearing your search query."
          hasAnyProjects && selectedTab == ProjectFilterTab.FAVORITES -> "Mark any project as a Favorite from its 3-dot menu to see it pinned here."
          else -> stringResource(R.string.pm_empty_body)
        },
        color = PmTextSecondary,
        fontSize = 13.sp,
        textAlign = TextAlign.Center
      )
      if (!hasAnyProjects) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = onCreateProjectClick,
          colors = ButtonDefaults.buttonColors(containerColor = CineFlowPurple),
          modifier = Modifier.testTag("empty_state_create_button")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Create First Project", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

/**
 * Bottom Summary & Cloud Backup Bar matching CineFlow AI Project Dashboard(1).png:
 * - Left: Storage cylinder stack icon + "Total Projects: X"
 * - Divider: "|"
 * - Center: Cloud icon + "Cloud Backup: Available" in vibrant neon green
 * - Right: Exact "Manage >" neon-bordered pill button (`R.drawable.img_pm_manage_button`)
 */
@Composable
private fun ProjectBackupSummaryBar(
  totalProjectsCount: Int,
  onManageBackupClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val manageDesc = stringResource(R.string.pm_action_manage)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(50.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(
        brush = Brush.horizontalGradient(
          listOf(Color(0xFF201F64), Color(0xFF14154C), Color(0xFF1D1D60))
        )
      )
      .border(
        width = 1.2.dp,
        brush = Brush.horizontalGradient(
          listOf(Color(0xFF534FA8), Color(0xFF383682), Color(0xFF534FA8))
        ),
        shape = RoundedCornerShape(18.dp)
      )
      .padding(horizontal = 12.dp)
      .testTag("project_backup_summary_bar"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Default.Storage,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = stringResource(R.string.pm_total_projects, totalProjectsCount),
        color = PmTextPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Box(
      modifier = Modifier
        .width(1.dp)
        .height(22.dp)
        .background(Color(0xFF484592))
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Default.CloudUpload,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = stringResource(R.string.pm_cloud_backup_available),
        color = PmCloudGreen,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Box(
      modifier = Modifier
        .height(32.dp)
        .aspectRatio(152f / 56f)
        .clip(RoundedCornerShape(50))
        .clickable(onClick = onManageBackupClick)
        .semantics { contentDescription = manageDesc }
        .testTag("project_backup_manage_button")
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_pm_manage_button),
        contentDescription = manageDesc,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}

/**
 * Bottom Navigation Bar rendering the exact `R.drawable.img_pm_bottom_nav` bar from
 * `CineFlow AI Project Dashboard(1).png` with 4 accessible interactive tab zones.
 */
@Composable
private fun ProjectReferenceBottomNavigationBar(
  onTabSelected: (BottomNavTab) -> Unit
) {
  val homeDesc = stringResource(R.string.nav_home)
  val projectsDesc = stringResource(R.string.nav_projects)
  val notificationsDesc = stringResource(R.string.nav_notifications)
  val settingsDesc = stringResource(R.string.nav_settings)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color(0xFF0E0645))
      .windowInsetsPadding(WindowInsets.navigationBars)
      .aspectRatio(941f / 128f)
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_pm_bottom_nav),
      contentDescription = null,
      contentScale = ContentScale.FillBounds,
      modifier = Modifier.fillMaxSize()
    )

    Row(modifier = Modifier.fillMaxSize()) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clickable { onTabSelected(BottomNavTab.HOME) }
          .semantics { contentDescription = homeDesc }
          .testTag("pm_nav_tab_home")
      )
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clickable { onTabSelected(BottomNavTab.PROJECTS) }
          .semantics { contentDescription = projectsDesc }
          .testTag("pm_nav_tab_projects")
      )
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clickable { onTabSelected(BottomNavTab.NOTIFICATIONS) }
          .semantics { contentDescription = notificationsDesc }
          .testTag("pm_nav_tab_notifications")
      )
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clickable { onTabSelected(BottomNavTab.SETTINGS) }
          .semantics { contentDescription = settingsDesc }
          .testTag("pm_nav_tab_settings")
      )
    }
  }
}

@Composable
private fun CreateNewProjectDialog(
  errorMessage: String?,
  onDismiss: () -> Unit,
  onCreateProject: (name: String, aspectRatio: String, resolution: String, fps: Int) -> Unit
) {
  var projectName by remember { mutableStateOf("") }
  var selectedAspectRatio by remember { mutableStateOf("16:9") }
  var selectedResolution by remember { mutableStateOf("1080p") }
  var selectedFps by remember { mutableIntStateOf(30) }

  val aspectRatios = remember { listOf("16:9", "9:16", "1:1", "4:5", "21:9") }
  val resolutions = remember { listOf("720p", "1080p", "4K") }
  val fpsOptions = remember { listOf(24, 30, 60) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Create New Project",
        fontWeight = FontWeight.ExtraBold
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = projectName,
          onValueChange = { projectName = it },
          label = { Text("Project Name") },
          placeholder = { Text("e.g. My Travel Vlog") },
          singleLine = true,
          isError = errorMessage != null,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("create_project_name_input")
        )
        if (errorMessage != null) {
          Text(
            text = errorMessage,
            color = CineFlowBadgeRed,
            fontSize = 12.sp,
            modifier = Modifier.testTag("create_project_error_text")
          )
        }

        Text(
          text = "Aspect Ratio",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(aspectRatios) { ratio ->
            FilterChip(
              selected = selectedAspectRatio == ratio,
              onClick = { selectedAspectRatio = ratio },
              label = { Text(ratio) }
            )
          }
        }

        Text(
          text = "Resolution & Frame Rate",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          resolutions.forEach { res ->
            FilterChip(
              selected = selectedResolution == res,
              onClick = { selectedResolution = res },
              label = { Text(res) }
            )
          }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          fpsOptions.forEach { fps ->
            FilterChip(
              selected = selectedFps == fps,
              onClick = { selectedFps = fps },
              label = { Text("$fps FPS") }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onCreateProject(projectName, selectedAspectRatio, selectedResolution, selectedFps)
        },
        colors = ButtonDefaults.buttonColors(containerColor = CineFlowPurple),
        modifier = Modifier.testTag("create_project_confirm_button")
      ) {
        Text("Create & Open", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("create_project_cancel_button")
      ) {
        Text("Cancel")
      }
    },
    modifier = Modifier.testTag("create_project_dialog")
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectActionsBottomSheet(
  project: VideoProject,
  onDismiss: () -> Unit,
  onOpenClick: () -> Unit,
  onRenameClick: () -> Unit,
  onDuplicateClick: () -> Unit,
  onToggleFavoriteClick: () -> Unit,
  onBackupClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = PmCosmicBgBottom,
    contentColor = PmTextPrimary,
    modifier = Modifier.testTag("project_actions_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      Text(
        text = project.name,
        color = PmTextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold
      )
      Text(
        text = "${project.aspectRatio} • ${project.resolutionLabel} • ${project.displayFps} FPS",
        color = PmTextSecondary,
        fontSize = 12.sp
      )
      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = PmCardBorder)

      ProjectActionRow(
        icon = Icons.Default.FolderOpen,
        label = "Open Project",
        tint = PmTextPrimary,
        testTag = "action_open_project",
        onClick = onOpenClick
      )
      ProjectActionRow(
        icon = Icons.Default.Edit,
        label = "Rename Project",
        tint = PmTextPrimary,
        testTag = "action_rename_project",
        onClick = onRenameClick
      )
      ProjectActionRow(
        icon = Icons.Default.ContentCopy,
        label = "Duplicate Project",
        tint = PmTextPrimary,
        testTag = "action_duplicate_project",
        onClick = onDuplicateClick
      )
      ProjectActionRow(
        icon = if (project.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
        label = if (project.isFavorite) "Remove from Favorites" else "Add to Favorites",
        tint = Color(0xFFFACC15),
        testTag = "action_favorite_project",
        onClick = onToggleFavoriteClick
      )
      ProjectActionRow(
        icon = Icons.Default.CloudDone,
        label = "Manual Cloud Backup",
        tint = CineFlowCyan,
        testTag = "action_backup_project",
        onClick = onBackupClick
      )
      ProjectActionRow(
        icon = Icons.Default.Delete,
        label = "Delete Project",
        tint = CineFlowBadgeRed,
        testTag = "action_delete_project",
        onClick = onDeleteClick
      )
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun ProjectActionRow(
  icon: ImageVector,
  label: String,
  tint: Color,
  testTag: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 13.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.width(14.dp))
    Text(
      text = label,
      color = tint,
      fontSize = 15.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}

@Composable
private fun RenameProjectDialog(
  project: VideoProject,
  errorMessage: String?,
  onDismiss: () -> Unit,
  onConfirmRename: (String) -> Unit
) {
  var nameInput by remember(project.projectId) { mutableStateOf(project.name) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Rename Project",
        fontWeight = FontWeight.ExtraBold
      )
    },
    text = {
      Column {
        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          label = { Text("New Project Name") },
          singleLine = true,
          isError = errorMessage != null,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("rename_project_input")
        )
        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = errorMessage,
            color = CineFlowBadgeRed,
            fontSize = 12.sp
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmRename(nameInput) },
        colors = ButtonDefaults.buttonColors(containerColor = CineFlowPurple),
        modifier = Modifier.testTag("rename_project_confirm_button")
      ) {
        Text("Save", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
    modifier = Modifier.testTag("rename_project_dialog")
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualCloudBackupSheet(
  projects: List<VideoProject>,
  isSignedIn: Boolean,
  statusMessage: String?,
  onDismiss: () -> Unit,
  onBackupSingleProject: (VideoProject) -> Unit,
  onBackupAllProjects: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = PmCosmicBgBottom,
    contentColor = PmTextPrimary,
    modifier = Modifier.testTag("manual_backup_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      Text(
        text = "Manual Cloud Metadata Backup",
        color = PmTextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = if (isSignedIn) {
          "Sync your local project timeline structure and metadata to your authenticated Cloud account. Original media files are never uploaded automatically."
        } else {
          "You are currently in Local-Only Mode (Unsigned). All ${projects.size} project(s) are safely persisted on-device in SQLite/Room. Sign in with Google from the Home Dashboard to enable manual cloud metadata backup."
        },
        color = PmTextSecondary,
        fontSize = 13.sp
      )

      statusMessage?.let { msg ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = msg,
          color = PmCloudGreen,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.testTag("backup_status_message")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (isSignedIn && projects.isNotEmpty()) {
        Button(
          onClick = onBackupAllProjects,
          colors = ButtonDefaults.buttonColors(containerColor = CineFlowPurple),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("backup_all_projects_button")
        ) {
          Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Back Up All Project Metadata (${projects.size})", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        projects.take(4).forEach { proj ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${proj.name} (${proj.statusTag})",
              color = PmTextPrimary,
              fontSize = 13.sp,
              modifier = Modifier.weight(1f)
            )
            OutlinedButton(
              onClick = { onBackupSingleProject(proj) },
              modifier = Modifier.testTag("backup_single_${proj.projectId}")
            ) {
              Text("Sync", color = CineFlowCyan, fontSize = 12.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("backup_sheet_close_button")
      ) {
        Text("Close", color = PmTextPrimary)
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

/**
 * Editor Entry Boundary Screen (`ROUTE_EDITOR_BOUNDARY`).
 */
@Composable
fun EditorEntryBoundaryScreen(
  project: VideoProject,
  onBackToProjects: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBackToProjects)

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          listOf(PmCosmicBgTop, PmCosmicBgBottom)
        )
      )
      .testTag("editor_entry_boundary_screen"),
    containerColor = Color.Transparent,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PmCardBgStart),
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 480.dp)
          .border(1.dp, PmCardBorder, RoundedCornerShape(22.dp))
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(CineFlowPurple.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ContentCut,
              contentDescription = null,
              tint = CineFlowCyan,
              modifier = Modifier.size(28.dp)
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = project.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = PmTextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("editor_boundary_project_name")
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "ID: ${project.projectId} • ${project.aspectRatio} • ${project.resolutionLabel} • ${project.displayFps} FPS • ${project.trackCount} Track(s)",
            style = MaterialTheme.typography.bodySmall,
            color = CineFlowCyan,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Project persisted in local Room database. Dedicated Create New Project UI (CineFlow AI Project Creation Screen.png) and Video Editor Workspace (17_Video_Editor_Dashboard.jpg) await owner reference confirmation in upcoming tasks.",
            style = MaterialTheme.typography.bodyMedium,
            color = PmTextSecondary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = onBackToProjects,
            colors = ButtonDefaults.buttonColors(containerColor = CineFlowPurple),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("back_to_projects_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Back to Project Management", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
