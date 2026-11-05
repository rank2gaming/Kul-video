package com.cineflow.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cineflow.ai.core.database.CineFlowDatabase
import com.cineflow.ai.core.firebase.authStateFlow
import com.cineflow.ai.core.firebase.configureAppCheck
import com.cineflow.ai.core.model.VideoProject
import com.cineflow.ai.data.cloud.CloudProjectSyncRepository
import com.cineflow.ai.data.project.LocalProjectRepository
import com.cineflow.ai.feature.editor.VideoEditorScreen
import com.cineflow.ai.feature.editor.VideoEditorViewModel
import com.cineflow.ai.feature.home.HomeDashboardScreen
import com.cineflow.ai.feature.home.HomeDashboardViewModel
import com.cineflow.ai.feature.home.ROUTE_HOME_DASHBOARD
import com.cineflow.ai.feature.home.ROUTE_PROJECT_MANAGEMENT
import com.cineflow.ai.feature.projects.CreateNewProjectScreen
import com.cineflow.ai.feature.projects.ProjectManagementScreen
import com.cineflow.ai.feature.projects.ProjectManagementViewModel
import com.cineflow.ai.feature.projects.ROUTE_CREATE_NEW_PROJECT
import com.cineflow.ai.feature.projects.ROUTE_EDITOR_BOUNDARY
import com.cineflow.ai.ui.theme.CineFlowTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
    configureAppCheck(this, intent)

    enableEdgeToEdge()

    // STEP 2: Initialize Compose UI
    setContent {
      CineFlowTheme {
        CineFlowAppRoot()
      }
    }
  }
}

@Composable
fun CineFlowAppRoot(auth: FirebaseAuth = Firebase.auth) {
  val context = LocalContext.current
  val database = remember(context) { CineFlowDatabase.getInstance(context) }
  val localProjectRepository = remember(database) {
    LocalProjectRepository(database.localProjectDao())
  }
  val homeViewModel: HomeDashboardViewModel = viewModel(
    factory = HomeDashboardViewModel.provideFactory(localProjectRepository)
  )
  val projectManagementViewModel: ProjectManagementViewModel = viewModel(
    factory = ProjectManagementViewModel.provideFactory(localProjectRepository)
  )

  val currentUser by auth.authStateFlow()
    .collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val localProjects by homeViewModel.localProjects.collectAsStateWithLifecycle()

  var currentRoute by remember { mutableStateOf(ROUTE_HOME_DASHBOARD) }
  var activeEditorProject by remember { mutableStateOf<VideoProject?>(null) }
  var publicUid by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    localProjectRepository.seedDemoProjectsIfEmpty()
  }

  // Auth-gated Cloud Metadata Synchronization:
  // Only instantiated and executed when currentUser != null (Section 3.H & 11).
  val signedInUser = currentUser
  LaunchedEffect(signedInUser?.uid, localProjects.size) {
    if (signedInUser != null) {
      val cloudRepo = CloudProjectSyncRepository(context.applicationContext)
      publicUid = cloudRepo.formatPublicUid(signedInUser.uid)
      val fallbackUsername = signedInUser.displayName
        ?.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        ?.take(24)
        ?: "creator_${signedInUser.uid.take(6)}"
      cloudRepo.ensureUserProfile(
        username = fallbackUsername,
        displayName = signedInUser.displayName,
        email = signedInUser.email,
        photoUrl = signedInUser.photoUrl?.toString()
      ).onSuccess { profile ->
        publicUid = profile.publicUid
      }
      localProjects.forEach { project ->
        cloudRepo.syncProjectMetadata(project)
      }
    } else {
      publicUid = null
    }
  }

  when (currentRoute) {
    ROUTE_HOME_DASHBOARD -> {
      HomeDashboardScreen(
        viewModel = homeViewModel,
        isSignedIn = signedInUser != null,
        currentUserEmail = signedInUser?.email,
        publicUid = publicUid,
        onNavigateToVideoEdit = { currentRoute = ROUTE_PROJECT_MANAGEMENT },
        onAuthChanged = { /* AuthStateListener Flow updates currentUser reactively */ }
      )
    }

    ROUTE_PROJECT_MANAGEMENT -> {
      ProjectManagementScreen(
        viewModel = projectManagementViewModel,
        isSignedIn = signedInUser != null,
        onBackToHome = { currentRoute = ROUTE_HOME_DASHBOARD },
        onOpenProjectInEditor = { project ->
          activeEditorProject = project
          currentRoute = ROUTE_EDITOR_BOUNDARY
        },
        onSyncProjectToCloud = { project ->
          if (signedInUser == null) {
            Result.failure(IllegalStateException("Sign in with Google to sync project metadata."))
          } else {
            val cloudRepo = CloudProjectSyncRepository(context.applicationContext)
            cloudRepo.syncProjectMetadata(project).map { Unit }
          }
        },
        onNavigateToCreateProject = {
          currentRoute = ROUTE_CREATE_NEW_PROJECT
        }
      )
    }

    ROUTE_CREATE_NEW_PROJECT -> {
      CreateNewProjectScreen(
        viewModel = projectManagementViewModel,
        onBackToProjects = { currentRoute = ROUTE_PROJECT_MANAGEMENT },
        onProjectCreated = { createdProject ->
          activeEditorProject = createdProject
          currentRoute = ROUTE_EDITOR_BOUNDARY
        }
      )
    }

    ROUTE_EDITOR_BOUNDARY -> {
      val targetProject = activeEditorProject
      if (targetProject != null) {
        val editorViewModel = remember(targetProject.projectId) {
          VideoEditorViewModel(localProjectRepository, targetProject)
        }
        VideoEditorScreen(
          viewModel = editorViewModel,
          isSignedIn = signedInUser != null,
          onBackToProjects = {
            activeEditorProject = null
            currentRoute = ROUTE_PROJECT_MANAGEMENT
          }
        )
      } else {
        currentRoute = ROUTE_PROJECT_MANAGEMENT
      }
    }
  }
}

