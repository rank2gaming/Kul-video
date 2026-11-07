package com.cineflow.ai

import android.content.Context
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
import com.cineflow.ai.feature.auth.AccountRecoveryScreen
import com.cineflow.ai.feature.auth.AccountRecoveryViewModel
import com.cineflow.ai.feature.auth.LoginScreen
import com.cineflow.ai.feature.auth.LoginViewModel
import com.cineflow.ai.feature.auth.PersonalInformationScreen
import com.cineflow.ai.feature.auth.PersonalInformationViewModel
import com.cineflow.ai.feature.auth.ProfileCompletionScreen
import com.cineflow.ai.feature.auth.ProfileCompletionViewModel
import com.cineflow.ai.feature.auth.ProfileHubScreen
import com.cineflow.ai.feature.auth.ProfileHubSection
import com.cineflow.ai.feature.auth.ProfileHubViewModel
import com.cineflow.ai.feature.auth.ROUTE_ACCOUNT_RECOVERY
import com.cineflow.ai.feature.auth.ROUTE_LOGIN
import com.cineflow.ai.feature.auth.ROUTE_PERSONAL_INFORMATION
import com.cineflow.ai.feature.auth.ROUTE_PROFILE_COMPLETION
import com.cineflow.ai.feature.auth.ROUTE_PROFILE_HUB
import com.cineflow.ai.feature.auth.ROUTE_SIGN_UP
import com.cineflow.ai.feature.auth.ROUTE_SUBSCRIPTION_PLANS
import com.cineflow.ai.feature.auth.SignUpScreen
import com.cineflow.ai.feature.auth.SignUpViewModel
import com.cineflow.ai.feature.auth.SubscriptionPlansScreen
import com.cineflow.ai.feature.auth.SubscriptionPlansViewModel
import com.cineflow.ai.feature.backup.CloudBackupDashboardScreen
import com.cineflow.ai.feature.backup.CloudBackupDashboardViewModel
import com.cineflow.ai.feature.backup.CloudBackupScreen
import com.cineflow.ai.feature.backup.CloudBackupViewModel
import com.cineflow.ai.feature.backup.CloudRestoreScreen
import com.cineflow.ai.feature.backup.CloudRestoreViewModel
import com.cineflow.ai.feature.backup.ROUTE_CLOUD_BACKUP
import com.cineflow.ai.feature.backup.ROUTE_CLOUD_BACKUP_DASHBOARD
import com.cineflow.ai.feature.backup.ROUTE_CLOUD_RESTORE
import com.cineflow.ai.feature.editor.VideoEditorScreen
import com.cineflow.ai.feature.editor.VideoEditorViewModel
import com.cineflow.ai.feature.export.ExportVideoScreen
import com.cineflow.ai.feature.export.ExportVideoViewModel
import com.cineflow.ai.feature.export.ROUTE_EXPORT_VIDEO
import com.cineflow.ai.feature.home.HomeDashboardScreen
import com.cineflow.ai.feature.home.HomeDashboardViewModel
import com.cineflow.ai.feature.home.ROUTE_HOME_DASHBOARD
import com.cineflow.ai.feature.home.ROUTE_PROJECT_MANAGEMENT
import com.cineflow.ai.feature.notifications.NotificationCenterScreen
import com.cineflow.ai.feature.notifications.NotificationCenterViewModel
import com.cineflow.ai.feature.notifications.ROUTE_NOTIFICATION_CENTER
import com.cineflow.ai.feature.onboarding.OnboardingPreferences
import com.cineflow.ai.feature.onboarding.OnboardingScreen
import com.cineflow.ai.feature.onboarding.OnboardingViewModel
import com.cineflow.ai.feature.onboarding.ROUTE_ONBOARDING
import com.cineflow.ai.feature.onboarding.ROUTE_SPLASH
import com.cineflow.ai.feature.onboarding.SplashScreen
import com.cineflow.ai.feature.projects.CreateNewProjectScreen
import com.cineflow.ai.feature.projects.ProjectManagementScreen
import com.cineflow.ai.feature.projects.ProjectManagementViewModel
import com.cineflow.ai.feature.projects.ROUTE_CREATE_NEW_PROJECT
import com.cineflow.ai.feature.projects.ROUTE_EDITOR_BOUNDARY
import com.cineflow.ai.feature.settings.ROUTE_SETTINGS
import com.cineflow.ai.feature.settings.SettingsScreen
import com.cineflow.ai.feature.settings.SettingsViewModel
import com.cineflow.ai.ui.theme.CineFlowTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

private const val PROFILE_SETUP_PREFS = "cineflow_profile_setup_prefs"

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
fun CineFlowAppRoot(
  auth: FirebaseAuth = Firebase.auth,
  splashDurationMillis: Long = 1600L
) {
  val context = LocalContext.current
  val prefs = remember(context) {
    context.getSharedPreferences(PROFILE_SETUP_PREFS, Context.MODE_PRIVATE)
  }
  val database = remember(context) { CineFlowDatabase.getInstance(context) }
  val localProjectRepository = remember(database) {
    LocalProjectRepository(database.localProjectDao())
  }
  val onboardingViewModel: OnboardingViewModel = viewModel()
  val loginViewModel: LoginViewModel = viewModel()
  val signUpViewModel: SignUpViewModel = viewModel()
  val accountRecoveryViewModel: AccountRecoveryViewModel = viewModel()
  val profileCompletionViewModel: ProfileCompletionViewModel = viewModel()
  val profileHubViewModel: ProfileHubViewModel = viewModel()
  val personalInfoViewModel: PersonalInformationViewModel = viewModel()
  val subscriptionPlansViewModel: SubscriptionPlansViewModel = viewModel()
  val cloudBackupDashboardViewModel: CloudBackupDashboardViewModel = viewModel()
  val cloudBackupViewModel: CloudBackupViewModel = viewModel()
  val cloudRestoreViewModel: CloudRestoreViewModel = viewModel()
  val settingsViewModel: SettingsViewModel = viewModel()
  val notificationCenterViewModel: NotificationCenterViewModel = viewModel()
  val homeViewModel: HomeDashboardViewModel = viewModel(
    factory = HomeDashboardViewModel.provideFactory(localProjectRepository)
  )
  val projectManagementViewModel: ProjectManagementViewModel = viewModel(
    factory = ProjectManagementViewModel.provideFactory(localProjectRepository)
  )

  val currentUser by auth.authStateFlow()
    .collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val isLocalSessionAuthenticated by loginViewModel.isLocalSessionAuthenticated
    .collectAsStateWithLifecycle()
  val createdPublicUid by signUpViewModel.createdPublicUid
    .collectAsStateWithLifecycle()
  val completedPublicUid by profileCompletionViewModel.completedPublicUid
    .collectAsStateWithLifecycle()
  val isProfileSetupCompleted by profileCompletionViewModel.isProfileSetupCompleted
    .collectAsStateWithLifecycle()
  val localProjects by homeViewModel.localProjects.collectAsStateWithLifecycle()

  val initialGoogleUid = auth.currentUser?.uid
  val wasGoogleProfilePreviouslyCompleted = initialGoogleUid != null &&
    prefs.getBoolean("profile_setup_completed_$initialGoogleUid", false)

  // Startup & Login Navigation Requirements:
  // - Application always begins at the Animated Splash Screen (`ROUTE_SPLASH`).
  // - First launch: Splash Screen -> Onboarding Screen -> Home Dashboard.
  // - Subsequent launches: Splash Screen -> Home Dashboard.
  // - Login Screen never opens automatically during application startup; it opens when the user
  //   manually taps Profile while unauthenticated.
  var currentRoute by remember {
    mutableStateOf(ROUTE_SPLASH)
  }
  var activeEditorProject by remember { mutableStateOf<VideoProject?>(null) }
  var publicUid by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    onboardingViewModel.syncCompletionFromStorage(OnboardingPreferences.isOnboardingCompleted(context))
    localProjectRepository.seedDemoProjectsIfEmpty()
  }

  val signedInUser = currentUser
  val isGoogleUserCompleted = isProfileSetupCompleted ||
    (signedInUser != null && prefs.getBoolean("profile_setup_completed_${signedInUser.uid}", false))
  val isEffectiveSignedIn = (signedInUser != null && isGoogleUserCompleted) ||
    isLocalSessionAuthenticated ||
    (createdPublicUid != null) ||
    (completedPublicUid != null)

  LaunchedEffect(signedInUser?.uid, isGoogleUserCompleted, completedPublicUid, localProjects.size) {
    if (signedInUser != null && isGoogleUserCompleted) {
      val cloudRepo = CloudProjectSyncRepository(context.applicationContext)
      publicUid = completedPublicUid ?: cloudRepo.formatPublicUid(signedInUser.uid)
      val preferredUsername = profileCompletionViewModel.username.value
        .trim()
        .takeIf { it.length >= 3 }
        ?: prefs.getString("profile_setup_username_${signedInUser.uid}", null)
        ?: signedInUser.displayName
          ?.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
          ?.take(24)
        ?: "creator_${signedInUser.uid.take(6)}"
      cloudRepo.ensureUserProfile(
        username = preferredUsername,
        displayName = signedInUser.displayName,
        email = signedInUser.email,
        photoUrl = signedInUser.photoUrl?.toString()
      ).onSuccess { profile ->
        publicUid = completedPublicUid ?: profile.publicUid
      }
      localProjects.forEach { project ->
        cloudRepo.syncProjectMetadata(project)
      }
    } else if (completedPublicUid != null) {
      publicUid = completedPublicUid
    } else if (createdPublicUid != null) {
      publicUid = createdPublicUid
    } else {
      publicUid = null
    }
  }

  when (currentRoute) {
    ROUTE_SPLASH -> {
      SplashScreen(
        durationMillis = splashDurationMillis.toInt(),
        onSplashFinished = {
          val onboardingDone = OnboardingPreferences.isOnboardingCompleted(context)
          onboardingViewModel.syncCompletionFromStorage(onboardingDone)
          currentRoute = when {
            !onboardingDone -> ROUTE_ONBOARDING
            initialGoogleUid != null && !wasGoogleProfilePreviouslyCompleted -> {
              profileCompletionViewModel.prepareForGoogleSignIn()
              ROUTE_PROFILE_COMPLETION
            }
            else -> ROUTE_HOME_DASHBOARD
          }
        }
      )
    }

    ROUTE_ONBOARDING -> {
      OnboardingScreen(
        viewModel = onboardingViewModel,
        onGetStarted = {
          OnboardingPreferences.setOnboardingCompleted(context, true)
          onboardingViewModel.completeOnboarding()
          currentRoute = if (initialGoogleUid != null && !wasGoogleProfilePreviouslyCompleted) {
            profileCompletionViewModel.prepareForGoogleSignIn()
            ROUTE_PROFILE_COMPLETION
          } else {
            ROUTE_HOME_DASHBOARD
          }
        }
      )
    }

    ROUTE_LOGIN -> {
      LoginScreen(
        viewModel = loginViewModel,
        onLoginSuccess = {
          // Email + Password login goes directly to Home Dashboard (Profile Setup is ONLY for Google Sign-In)
          currentRoute = ROUTE_HOME_DASHBOARD
        },
        onSkipToHome = {
          // Section 3.E & 9: Skip opens Home Dashboard in unsigned local-only mode without creating any Firebase user.
          currentRoute = ROUTE_HOME_DASHBOARD
        },
        onGoogleSignInSuccess = {
          // Google Sign-In MUST route to Profile Setup screen before allowing entry to Home Dashboard
          auth.currentUser?.uid?.let { uid ->
            prefs.edit().putBoolean("profile_setup_completed_$uid", false).apply()
          }
          profileCompletionViewModel.prepareForGoogleSignIn()
          currentRoute = ROUTE_PROFILE_COMPLETION
        },
        onNavigateToSignUp = {
          currentRoute = ROUTE_SIGN_UP
        },
        onNavigateToForgotPassword = {
          accountRecoveryViewModel.prefillEmailIfBlank(loginViewModel.email.value)
          currentRoute = ROUTE_ACCOUNT_RECOVERY
        }
      )
    }

    ROUTE_SIGN_UP -> {
      SignUpScreen(
        viewModel = signUpViewModel,
        onBackToLogin = {
          currentRoute = ROUTE_LOGIN
        },
        onSignUpSuccess = { allocatedPublicUid ->
          if (allocatedPublicUid != null) {
            // Standard Sign Up form already collected all fields -> go to Home Dashboard
            publicUid = allocatedPublicUid
            currentRoute = ROUTE_HOME_DASHBOARD
          } else {
            // Section 10: Google Sign-In MUST route to Profile Completion before Home Dashboard
            auth.currentUser?.uid?.let { uid ->
              prefs.edit().putBoolean("profile_setup_completed_$uid", false).apply()
            }
            profileCompletionViewModel.prepareForGoogleSignIn()
            currentRoute = ROUTE_PROFILE_COMPLETION
          }
        }
      )
    }

    ROUTE_PROFILE_COMPLETION -> {
      ProfileCompletionScreen(
        viewModel = profileCompletionViewModel,
        onBack = {
          // Pressing Back before completing profile signs out incomplete Google session and returns to Login
          runCatching { auth.signOut() }
          currentRoute = ROUTE_LOGIN
        },
        onProfileCompleted = { allocatedPublicUid ->
          val completedUsername = profileCompletionViewModel.username.value.trim()
          val completedMobile = profileCompletionViewModel.mobileNumber.value.trim().let { digits ->
            if (digits.isNotEmpty()) "${profileCompletionViewModel.countryCode.value} $digits" else ""
          }
          auth.currentUser?.uid?.let { uid ->
            prefs.edit()
              .putBoolean("profile_setup_completed_$uid", true)
              .putString("profile_setup_username_$uid", completedUsername)
              .apply()
          }
          publicUid = allocatedPublicUid
          profileHubViewModel.syncIdentity(
            displayName = auth.currentUser?.displayName ?: completedUsername,
            username = completedUsername,
            publicUid = allocatedPublicUid
          )
          personalInfoViewModel.syncFromProfile(
            fullName = auth.currentUser?.displayName ?: completedUsername,
            username = completedUsername,
            email = auth.currentUser?.email,
            mobile = completedMobile,
            publicUid = allocatedPublicUid
          )
          currentRoute = ROUTE_HOME_DASHBOARD
        }
      )
    }

    ROUTE_ACCOUNT_RECOVERY -> {
      AccountRecoveryScreen(
        viewModel = accountRecoveryViewModel,
        onBackToLogin = {
          currentRoute = ROUTE_LOGIN
        }
      )
    }

    ROUTE_PROFILE_HUB -> {
      ProfileHubScreen(
        viewModel = profileHubViewModel,
        onBack = {
          currentRoute = ROUTE_HOME_DASHBOARD
        },
        onLogOut = {
          auth.currentUser?.uid?.let { uid ->
            prefs.edit().putBoolean("profile_setup_completed_$uid", false).apply()
          }
          runCatching { auth.signOut() }
          loginViewModel.signOutLocalSession()
          profileCompletionViewModel.prepareForGoogleSignIn()
          publicUid = null
          currentRoute = ROUTE_LOGIN
        },
        onNavigateToSection = { section ->
          when (section) {
            ProfileHubSection.PERSONAL_INFORMATION -> {
              val completedMobile = profileCompletionViewModel.mobileNumber.value.trim().let { digits ->
                if (digits.isNotEmpty()) "${profileCompletionViewModel.countryCode.value} $digits" else ""
              }
              personalInfoViewModel.syncFromProfile(
                fullName = profileHubViewModel.displayName.value,
                username = profileHubViewModel.username.value,
                email = signedInUser?.email ?: signUpViewModel.email.value,
                mobile = completedMobile.ifBlank { signUpViewModel.mobileNumber.value },
                publicUid = profileHubViewModel.publicUid.value
              )
              currentRoute = ROUTE_PERSONAL_INFORMATION
            }
            ProfileHubSection.SUBSCRIPTION_PLAN -> {
              currentRoute = ROUTE_SUBSCRIPTION_PLANS
            }
            ProfileHubSection.CLOUD_BACKUP_RESTORE -> {
              currentRoute = ROUTE_CLOUD_BACKUP_DASHBOARD
            }
            ProfileHubSection.ACCOUNT_SETTINGS -> {
              currentRoute = ROUTE_SETTINGS
            }
            else -> {
              profileHubViewModel.openSection(section)
            }
          }
        }
      )
    }

    ROUTE_PERSONAL_INFORMATION -> {
      PersonalInformationScreen(
        viewModel = personalInfoViewModel,
        onBack = {
          currentRoute = ROUTE_PROFILE_HUB
        },
        onChangesSaved = { savedName, savedUsername, _, _ ->
          profileHubViewModel.syncIdentity(
            displayName = savedName,
            username = savedUsername,
            publicUid = personalInfoViewModel.publicUid.value
          )
        }
      )
    }

    ROUTE_SUBSCRIPTION_PLANS -> {
      SubscriptionPlansScreen(
        viewModel = subscriptionPlansViewModel,
        onBack = {
          currentRoute = ROUTE_PROFILE_HUB
        }
      )
    }

    ROUTE_CLOUD_BACKUP_DASHBOARD -> {
      CloudBackupDashboardScreen(
        viewModel = cloudBackupDashboardViewModel,
        onBack = {
          currentRoute = ROUTE_PROFILE_HUB
        },
        onNavigateToSelectBackupData = {
          currentRoute = ROUTE_CLOUD_BACKUP
        },
        onNavigateToRestore = {
          currentRoute = ROUTE_CLOUD_RESTORE
        },
        onNavigateToUpgrade = {
          currentRoute = ROUTE_SUBSCRIPTION_PLANS
        }
      )
    }

    ROUTE_CLOUD_BACKUP -> {
      CloudBackupScreen(
        viewModel = cloudBackupViewModel,
        onBack = {
          currentRoute = ROUTE_CLOUD_BACKUP_DASHBOARD
        }
      )
    }

    ROUTE_CLOUD_RESTORE -> {
      CloudRestoreScreen(
        viewModel = cloudRestoreViewModel,
        onBack = {
          currentRoute = ROUTE_CLOUD_BACKUP_DASHBOARD
        }
      )
    }

    ROUTE_SETTINGS -> {
      SettingsScreen(
        viewModel = settingsViewModel,
        onBack = {
          currentRoute = ROUTE_HOME_DASHBOARD
        },
        onNavigateToCloudBackup = {
          currentRoute = ROUTE_CLOUD_BACKUP_DASHBOARD
        },
        onNavigateToSubscriptionPlan = {
          currentRoute = ROUTE_SUBSCRIPTION_PLANS
        },
        onNavigateToNotifications = {
          currentRoute = ROUTE_NOTIFICATION_CENTER
        }
      )
    }

    ROUTE_NOTIFICATION_CENTER -> {
      NotificationCenterScreen(
        viewModel = notificationCenterViewModel,
        onBack = {
          currentRoute = ROUTE_HOME_DASHBOARD
        }
      )
    }

    ROUTE_HOME_DASHBOARD -> {
      HomeDashboardScreen(
        viewModel = homeViewModel,
        isSignedIn = isEffectiveSignedIn,
        currentUserEmail = signedInUser?.email
          ?: signUpViewModel.email.value.takeIf { createdPublicUid != null }
          ?: loginViewModel.email.value.takeIf { isLocalSessionAuthenticated },
        publicUid = publicUid,
        onNavigateToVideoEdit = { currentRoute = ROUTE_PROJECT_MANAGEMENT },
        onAuthChanged = {
          // If an unsigned user initiates Google Sign-In from the Home 3-dot sheet,
          // route them to Profile Setup before allowing an authenticated Home session!
          if (!isEffectiveSignedIn) {
            auth.currentUser?.uid?.let { uid ->
              prefs.edit().putBoolean("profile_setup_completed_$uid", false).apply()
            }
            profileCompletionViewModel.prepareForGoogleSignIn()
            currentRoute = ROUTE_PROFILE_COMPLETION
          }
        },
        onNavigateToProfileHub = {
          // Section 2 & 12: When the user manually taps Profile, inspect authentication state:
          // - If authenticated -> open existing Profile screen (`ROUTE_PROFILE_HUB`)
          // - If unauthenticated -> open existing Login/Authentication screen (`ROUTE_LOGIN`)
          if (isEffectiveSignedIn) {
            val activeName = signedInUser?.displayName
              ?: signUpViewModel.fullName.value.takeIf { it.isNotBlank() }
            val activeUsername = profileCompletionViewModel.username.value.takeIf { it.isNotBlank() }
              ?: signUpViewModel.username.value.takeIf { it.isNotBlank() }
            profileHubViewModel.syncIdentity(
              displayName = activeName,
              username = activeUsername,
              publicUid = publicUid
            )
            currentRoute = ROUTE_PROFILE_HUB
          } else {
            currentRoute = ROUTE_LOGIN
          }
        },
        onNavigateToSettings = {
          currentRoute = ROUTE_SETTINGS
        },
        onNavigateToNotifications = {
          currentRoute = ROUTE_NOTIFICATION_CENTER
        }
      )
    }

    ROUTE_PROJECT_MANAGEMENT -> {
      ProjectManagementScreen(
        viewModel = projectManagementViewModel,
        isSignedIn = isEffectiveSignedIn,
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
          isSignedIn = isEffectiveSignedIn,
          onBackToProjects = {
            activeEditorProject = null
            currentRoute = ROUTE_PROJECT_MANAGEMENT
          },
          onNavigateToExport = {
            activeEditorProject = editorViewModel.project.value
            currentRoute = ROUTE_EXPORT_VIDEO
          }
        )
      } else {
        currentRoute = ROUTE_PROJECT_MANAGEMENT
      }
    }

    ROUTE_EXPORT_VIDEO -> {
      val targetProject = activeEditorProject
      if (targetProject != null) {
        val exportViewModel = remember(targetProject.projectId, targetProject.updatedAtEpochMillis) {
          ExportVideoViewModel(localProjectRepository, targetProject)
        }
        ExportVideoScreen(
          viewModel = exportViewModel,
          isSignedIn = isEffectiveSignedIn,
          onBackToEditor = {
            activeEditorProject = exportViewModel.project.value
            currentRoute = ROUTE_EDITOR_BOUNDARY
          }
        )
      } else {
        currentRoute = ROUTE_PROJECT_MANAGEMENT
      }
    }
  }
}
