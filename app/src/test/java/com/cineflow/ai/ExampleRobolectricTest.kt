package com.cineflow.ai

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cineflow.ai.core.database.CineFlowDatabase
import com.cineflow.ai.core.model.VideoProject
import com.cineflow.ai.data.project.LocalProjectRepository
import com.cineflow.ai.feature.home.HomeDashboardScreen
import com.cineflow.ai.feature.home.HomeDashboardViewModel
import com.cineflow.ai.feature.projects.CreateNewProjectScreen
import com.cineflow.ai.feature.projects.ProjectManagementScreen
import com.cineflow.ai.feature.projects.ProjectManagementViewModel
import com.cineflow.ai.ui.theme.CineFlowTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private lateinit var database: CineFlowDatabase
  private lateinit var repository: LocalProjectRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, CineFlowDatabase::class.java)
      .allowMainThreadQueries()
      .setQueryExecutor { it.run() }
      .setTransactionExecutor { it.run() }
      .build()
    repository = LocalProjectRepository(database.localProjectDao())
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun appName_matchesCineFlowAi() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CineFlow AI", appName)
  }

  @Test
  fun localProjectRepository_preventsBlankAndResolvesDuplicateNames() = runBlocking {
    val blankResult = repository.createProject("   ")
    assertTrue(blankResult.isFailure)

    val first = repository.createProject("My Vlog").getOrThrow()
    val second = repository.createProject("My Vlog").getOrThrow()
    assertEquals("My Vlog", first.name)
    assertEquals("My Vlog (2)", second.name)
  }

  @Test
  fun localProjectRepository_supportsRenameDuplicateFavoriteAndDelete() = runBlocking {
    val travel = repository.createProject(
      rawName = "My Travel Vlog",
      aspectRatio = "16:9",
      frameRateNum = 30,
      resolutionLabel = "1080p",
      durationMicros = 135_000_000L
    ).getOrThrow()

    val summer = repository.createProject(
      rawName = "Summer Moments",
      aspectRatio = "9:16",
      frameRateNum = 60,
      resolutionLabel = "1080p",
      durationMicros = 45_000_000L
    ).getOrThrow()

    // Rename Summer Moments to My Travel Vlog -> resolves collision to "My Travel Vlog (2)"
    val renamed = repository.renameProject(summer.projectId, "My Travel Vlog").getOrThrow()
    assertEquals("My Travel Vlog (2)", renamed.name)
    assertEquals("Edited", renamed.statusTag)

    // Duplicate original travel project -> resolves collision to "My Travel Vlog (3)"
    val duplicated = repository.duplicateProject(travel.projectId).getOrThrow()
    assertEquals("My Travel Vlog (3)", duplicated.name)
    assertEquals(travel.durationMicros, duplicated.durationMicros)

    // Toggle favorite
    val favorited = repository.toggleFavorite(travel.projectId).getOrThrow()
    assertTrue(favorited.isFavorite)
    val unfavorited = repository.toggleFavorite(travel.projectId).getOrThrow()
    assertFalse(unfavorited.isFavorite)

    // Delete project
    repository.deleteProject(duplicated.projectId)
    val remaining = repository.projectsFlow.first()
    assertEquals(2, remaining.size)
  }

  @Test
  fun homeDashboardScreen_displaysAllToolsAndHandlesVideoEditAndComingSoon() {
    val viewModel = HomeDashboardViewModel(repository)
    var navigatedToVideoEdit = false

    composeTestRule.setContent {
      CineFlowTheme {
        HomeDashboardScreen(
          viewModel = viewModel,
          isSignedIn = false,
          currentUserEmail = null,
          publicUid = null,
          onNavigateToVideoEdit = { navigatedToVideoEdit = true },
          onAuthChanged = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("home_dashboard_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("home_hero_banner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_card_video_edit").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_card_text_to_speech").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_card_remove_bg").assertExists()

    // Clicking an unfinished tool opens the honest Coming Soon dialog
    composeTestRule.onNodeWithTag("tool_card_text_to_speech").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("coming_soon_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("coming_soon_dismiss_button").performClick()
    composeTestRule.waitForIdle()

    // Clicking the left 3-dot menu icon opens the sheet with the Download CineFlow AI APK button
    composeTestRule.onNodeWithTag("home_overflow_menu_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("home_overflow_download_apk_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("auth_skip_button").performClick()
    composeTestRule.waitForIdle()

    // Clicking Video Edit triggers navigation to Project Management
    composeTestRule.onNodeWithTag("tool_card_video_edit").performClick()
    composeTestRule.waitForIdle()
    assertTrue(navigatedToVideoEdit)
  }

  @Test
  fun projectManagementScreen_usesRealPersistedProjectsAndSupportsCreateOpenAndFilter() {
    val viewModel = ProjectManagementViewModel(repository)
    var openedProject: VideoProject? = null

    composeTestRule.setContent {
      CineFlowTheme {
        ProjectManagementScreen(
          viewModel = viewModel,
          isSignedIn = false,
          onBackToHome = {},
          onOpenProjectInEditor = { openedProject = it },
          onSyncProjectToCloud = { Result.success(Unit) }
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("project_management_screen").assertIsDisplayed()
    // Initially 0 persisted projects -> shows honest empty state (no fake hardcoded sample records)
    composeTestRule.onNodeWithTag("project_empty_state").assertIsDisplayed()

    // Click "+ Create New Project" button and create a real local project
    composeTestRule.onNodeWithTag("project_create_new_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("create_project_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("create_project_name_input").performTextInput("Night City")
    composeTestRule.onNodeWithTag("create_project_confirm_button").performClick()

    // Wait for Room coroutine to persist project and emit updated Flow
    composeTestRule.waitUntil(timeoutMillis = 5_000L) { openedProject != null }
    composeTestRule.waitForIdle()

    // Creating a project persists it in Room and triggers onOpenProjectInEditor
    assertNotNull(openedProject)
    assertEquals("Night City", openedProject?.name)

    // Verify project card is rendered in the Project Management list
    val persistedId = openedProject!!.projectId
    composeTestRule.waitUntil(timeoutMillis = 5_000L) {
      composeTestRule.onAllNodes(androidx.compose.ui.test.hasTestTag("project_card_$persistedId"))
        .fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("project_card_$persistedId").assertExists()
    composeTestRule.onNodeWithText("Night City").assertExists()

    // Verify filter tabs and manual backup entry button exist
    composeTestRule.onNodeWithTag("project_tab_recent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("project_tab_all").assertIsDisplayed()
    composeTestRule.onNodeWithTag("project_tab_favorites").assertIsDisplayed()
    composeTestRule.onNodeWithTag("project_backup_manage_button").assertExists()
  }

  @Test
  fun createNewProjectScreen_validatesBlankNameUpdatesCounterAndPersistsProject() {
    val viewModel = ProjectManagementViewModel(repository)
    var createdProject: VideoProject? = null

    composeTestRule.setContent {
      CineFlowTheme {
        CreateNewProjectScreen(
          viewModel = viewModel,
          onBackToProjects = {},
          onProjectCreated = { createdProject = it }
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("create_new_project_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("create_project_char_counter").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("0 / 100").assertExists()
    composeTestRule.onNodeWithTag("create_project_info_box").performScrollTo().assertIsDisplayed()

    // Submitting blank project name shows validation error and does not create project
    composeTestRule.onNodeWithTag("create_project_confirm_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000L) {
      viewModel.createErrorMessage.value != null
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("create_project_error_text").performScrollTo().assertIsDisplayed()

    // Enter a valid project name ("Summer Documentary" -> 18 chars)
    composeTestRule.onNodeWithTag("create_project_name_input").performScrollTo().performTextInput("Summer Documentary")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("18 / 100").assertExists()

    // Click Create Project -> persists project in Room with empty timeline and navigates to Editor
    composeTestRule.onNodeWithTag("create_project_confirm_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000L) { createdProject != null }
    composeTestRule.waitForIdle()

    assertNotNull(createdProject)
    assertEquals("Summer Documentary", createdProject?.name)
    assertEquals(0L, createdProject?.durationMicros)
    assertEquals(0, createdProject?.clipCount)
    assertEquals(1, createdProject?.trackCount)
    assertTrue(createdProject!!.projectId.startsWith("proj_"))
  }

  @Test
  fun videoEditorScreen_displaysCineFlowLogoSupports16x9And9x16WithZeroDemoClipsAndPersistsEdits() {
    val initialProject = runBlocking {
      repository.createProject(
        rawName = "Studio Reel",
        aspectRatio = "16:9"
      ).getOrThrow()
    }
    val editorViewModel = com.cineflow.ai.feature.editor.VideoEditorViewModel(repository, initialProject)

    composeTestRule.setContent {
      CineFlowTheme {
        com.cineflow.ai.feature.editor.VideoEditorScreen(
          viewModel = editorViewModel,
          isSignedIn = false,
          onBackToProjects = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("video_editor_screen").assertIsDisplayed()
    // Top Header: Close (X) + CineFlow AI Logo + 720P + Export
    composeTestRule.onNodeWithTag("editor_close_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_brand_logo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_resolution_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_export_button").assertIsDisplayed()

    // Preview Stage: Defaults to clean 16:9 horizontal preview canvas with zero demo videos and no upload button/badges
    composeTestRule.onNodeWithTag("editor_preview_canvas_16:9").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_empty_preview_state").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_quick_aspect_switcher").assertDoesNotExist()

    // Timeline starts at 00:00 / 00:00 with zero demo clips, Mute clip, Cover, Playhead, fixed right-side +, and + Add audio
    composeTestRule.onNodeWithTag("editor_time_readout").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_mute_clip_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_cover_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_playhead_line").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_add_clip_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_add_audio_button").assertIsDisplayed()

    // Upload a portrait 9:16 video (1080x1920) -> automatically detects 9:16 and switches preview canvas to 9:16
    editorViewModel.importMainVideoClips(
      importedItems = listOf(Triple("content://com.cineflow.ai/media/portrait_clip.mp4", "Portrait Shot", 6_000_000L)),
      widthPx = 1080,
      heightPx = 1920
    )
    composeTestRule.waitForIdle()
    assertEquals("9:16", editorViewModel.project.value.aspectRatio)
    composeTestRule.onNodeWithTag("editor_preview_canvas_9:16").assertIsDisplayed()
    assertEquals(1, editorViewModel.clips.value.size)
    assertEquals(6_000_000L, editorViewModel.totalDurationMicros)

    // Seek to 3.0s and split clip at playhead -> produces 2 adjacent clips totaling 6.0s
    editorViewModel.seekToMicros(3_000_000L)
    val splitOk = editorViewModel.splitClipAtPlayhead()
    assertTrue(splitOk)
    assertEquals(2, editorViewModel.clips.value.size)
    assertEquals(6_000_000L, editorViewModel.totalDurationMicros)

    // Undo restores 1 clip
    assertTrue(editorViewModel.undo())
    assertEquals(1, editorViewModel.clips.value.size)

    // Redo restores 2 clips
    assertTrue(editorViewModel.redo())
    assertEquals(2, editorViewModel.clips.value.size)

    // Upload a landscape 16:9 video (1920x1080) -> automatically detects 16:9 and switches preview canvas to 16:9
    editorViewModel.importMainVideoClips(
      importedItems = listOf(Triple("content://com.cineflow.ai/media/landscape_clip.mp4", "Landscape Shot", 4_000_000L)),
      widthPx = 1920,
      heightPx = 1080
    )
    composeTestRule.waitForIdle()
    assertEquals("16:9", editorViewModel.project.value.aspectRatio)
    composeTestRule.onNodeWithTag("editor_preview_canvas_16:9").assertIsDisplayed()

    // Bottom navigation category bar & horizontal scroll slider are displayed
    composeTestRule.onNodeWithTag("editor_main_category_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_nav_slider_track").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_nav_slider_thumb").assertIsDisplayed()
  }

  @Test
  fun seedDemoProjectsIfEmpty_populates16x9And9x16MultiClipDemoProjects() = runBlocking {
    repository.seedDemoProjectsIfEmpty()
    val projects = repository.projectsFlow.first()
    assertEquals(2, projects.size)

    val demo16x9 = projects.firstOrNull { it.aspectRatio == "16:9" }
    val demo9x16 = projects.firstOrNull { it.aspectRatio == "9:16" }
    assertNotNull(demo16x9)
    assertNotNull(demo9x16)

    assertEquals("Garden Nature (16:9 Demo)", demo16x9?.name)
    assertEquals(17_000_000L, demo16x9?.durationMicros)
    assertEquals(3, demo16x9?.clipCount)

    assertEquals("Golden Dog & Parrot (9:16 Demo)", demo9x16?.name)
    assertEquals(30_000_000L, demo9x16?.durationMicros)
    assertEquals(3, demo9x16?.clipCount)

    val clips16x9 = repository.getClipsForProjectSnapshot(demo16x9!!.projectId)
    val clips9x16 = repository.getClipsForProjectSnapshot(demo9x16!!.projectId)
    assertEquals(3, clips16x9.size)
    assertEquals(3, clips9x16.size)
  }

  @Test
  fun exportVideoScreen_displaysReferenceUiUpdatesEstimatedSizeAndEnforcesAuthResolutionCap() = runBlocking {
    val project = repository.createProject(
      rawName = "Alpine Adventure",
      aspectRatio = "9:16",
      frameRateNum = 30,
      resolutionLabel = "1080p",
      durationMicros = 30_000_000L
    ).getOrThrow()

    val exportViewModel = com.cineflow.ai.feature.export.ExportVideoViewModel(repository, project)

    composeTestRule.setContent {
      CineFlowTheme {
        com.cineflow.ai.feature.export.ExportVideoScreen(
          viewModel = exportViewModel,
          isSignedIn = false,
          onBackToEditor = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("export_video_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_brand_header").assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_device_capability_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_video_preview_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_resolution_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_fps_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_format_codec_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_quality_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_bitrate_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_estimated_size_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_audio_settings_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_summary_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_video_submit_button").performScrollTo().assertIsDisplayed()

    // Reference defaults: 1080p, 30 FPS, MP4, H.264, HIGH, 25.0 Mbps -> 245 MB
    assertEquals(245, exportViewModel.estimatedSizeMb.value)

    // Changing codec to H.265 reduces estimated file size
    exportViewModel.selectVideoCodec(com.cineflow.ai.feature.export.ExportVideoCodec.H265)
    assertTrue(exportViewModel.estimatedSizeMb.value < 245)

    // Restore H.264 -> back to 245 MB
    exportViewModel.selectVideoCodec(com.cineflow.ai.feature.export.ExportVideoCodec.H264)
    assertEquals(245, exportViewModel.estimatedSizeMb.value)

    // Unsigned export at 1080p triggers Section 19 RequiresAuthForHighRes gate
    exportViewModel.startExport(isSignedIn = false, instantForTest = true)
    assertTrue(exportViewModel.exportJobState.value is com.cineflow.ai.feature.export.ExportJobState.RequiresAuthForHighRes)

    // Downgrading to 720p for unsigned session completes export and marks project "Exported"
    exportViewModel.downgradeTo720pAndExport(instantForTest = true)
    assertTrue(exportViewModel.exportJobState.value is com.cineflow.ai.feature.export.ExportJobState.Completed)
    assertEquals(1, exportViewModel.exportHistory.value.size)
    assertEquals("720p", exportViewModel.exportHistory.value.first().resolution.chipLabel)

    // Signed-in session can export at 8K / 240 FPS with explicit frame duplication policy note
    exportViewModel.selectResolution(com.cineflow.ai.feature.export.ExportResolutionOption.RES_8K)
    exportViewModel.selectFps(240)
    exportViewModel.startExport(isSignedIn = true, instantForTest = true)
    val completed8k = exportViewModel.exportJobState.value as com.cineflow.ai.feature.export.ExportJobState.Completed
    assertEquals("8K", completed8k.record.resolution.chipLabel)
    assertEquals(4320, completed8k.record.widthPx)
    assertEquals(7680, completed8k.record.heightPx)
    assertEquals(240, completed8k.record.fps)
    assertNotNull(completed8k.record.frameRatePolicyNote)
  }

  @Test
  fun loginScreen_displaysReferenceUiValidatesSection10PasswordAndSupportsSkipLocalMode() {
    val loginViewModel = com.cineflow.ai.feature.auth.LoginViewModel()
    var skippedToHome = false
    var loggedInSuccess = false

    composeTestRule.setContent {
      CineFlowTheme {
        com.cineflow.ai.feature.auth.LoginScreen(
          viewModel = loginViewModel,
          onLoginSuccess = { loggedInSuccess = true },
          onSkipToHome = { skippedToHome = true }
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("login_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_hero_banner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_skip_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_card_container").assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_title_text").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Sign in to continue to CineFlow AI").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_email_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_password_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_password_visibility_toggle").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_forgot_password_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_google_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_apple_button").performScrollTo().assertIsDisplayed()

    // Section 3.E & 9: Skip opens Home Dashboard in unsigned local-only mode without creating any user
    composeTestRule.onNodeWithTag("login_skip_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(skippedToHome)
    assertFalse(loginViewModel.isLocalSessionAuthenticated.value)

    // Section 10: Invalid password (<8 chars, missing uppercase/digit) is rejected
    loginViewModel.onEmailChange("creator@cineflow.ai")
    loginViewModel.onPasswordChange("short")
    assertFalse(loginViewModel.submitLogin())
    assertNotNull(loginViewModel.errorMessage.value)

    loginViewModel.onPasswordChange("alllowercase123")
    assertFalse(loginViewModel.submitLogin())
    assertNotNull(loginViewModel.errorMessage.value)

    // Valid Section 10 password (8..25 chars, uppercase, lowercase, number) succeeds
    loginViewModel.onPasswordChange("CineFlow2026")
    assertTrue(loginViewModel.submitLogin(onSuccess = { loggedInSuccess = true }))
    assertTrue(loggedInSuccess)
    assertTrue(loginViewModel.isLocalSessionAuthenticated.value)
  }

  @Test
  fun signUpScreen_displaysReferenceUiValidatesSection10RulesAndAllocatesPublicUid() {
    val signUpViewModel = com.cineflow.ai.feature.auth.SignUpViewModel()
    var navigatedBackToLogin = false
    var allocatedPublicUid: String? = null

    composeTestRule.setContent {
      CineFlowTheme {
        com.cineflow.ai.feature.auth.SignUpScreen(
          viewModel = signUpViewModel,
          onBackToLogin = { navigatedBackToLogin = true },
          onSignUpSuccess = { uid -> allocatedPublicUid = uid }
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("signup_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_top_hero").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_card_container").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_full_name_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_username_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_email_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_country_code_selector").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_mobile_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_password_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_password_visibility_toggle").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_confirm_password_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_confirm_password_visibility_toggle").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_password_requirements_box").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_submit_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_google_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("signup_navigate_to_login_button").performScrollTo().assertIsDisplayed()

    // Default country code is +91
    assertEquals("+91", signUpViewModel.countryCode.value)

    // Mismatched confirm password fails validation
    signUpViewModel.onFullNameChange("Rabiul Islam")
    signUpViewModel.onUsernameChange("rabiul_creator")
    signUpViewModel.onEmailChange("rabiul@cineflow.ai")
    signUpViewModel.onPasswordChange("CineFlow2026")
    signUpViewModel.onConfirmPasswordChange("Different2026")
    assertFalse(signUpViewModel.submitSignUp())
    assertNotNull(signUpViewModel.errorMessage.value)

    // Matching valid password succeeds and allocates CF + 8 numeric digits Public User ID
    signUpViewModel.onConfirmPasswordChange("CineFlow2026")
    assertTrue(signUpViewModel.submitSignUp { uid -> allocatedPublicUid = uid })
    assertNotNull(allocatedPublicUid)
    assertTrue(Regex("^CF\\d{8}$").matches(allocatedPublicUid!!))

    // Duplicate username is rejected on subsequent attempt
    assertFalse(signUpViewModel.submitSignUp())
    assertNotNull(signUpViewModel.errorMessage.value)

    // Navigating back to Login works
    composeTestRule.onNodeWithTag("signup_navigate_to_login_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(navigatedBackToLogin)
  }

  @Test
  fun accountRecoveryScreen_displaysReferenceUiSwitchesTabsAndValidatesRecoveryInput() {
    val recoveryViewModel = com.cineflow.ai.feature.auth.AccountRecoveryViewModel()
    var navigatedBack = false

    composeTestRule.setContent {
      CineFlowTheme {
        com.cineflow.ai.feature.auth.AccountRecoveryScreen(
          viewModel = recoveryViewModel,
          onBackToLogin = { navigatedBack = true }
        )
      }
    }

    composeTestRule.waitForIdle()

    // Verify top hero and 3-tab selector
    composeTestRule.onNodeWithTag("recovery_top_hero").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_tab_bar").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_tab_forgot_password").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_tab_forgot_username").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_tab_forgot_email").performScrollTo().assertIsDisplayed()

    // Verify main card elements and bottom Need Help support card
    composeTestRule.onNodeWithTag("recovery_main_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_card_title").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_identifier_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_primary_submit_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_link_card_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_support_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_contact_support_button").performScrollTo().assertIsDisplayed()

    // Default tab is FORGOT_PASSWORD
    assertEquals(
      com.cineflow.ai.feature.auth.AccountRecoveryTab.FORGOT_PASSWORD,
      recoveryViewModel.selectedTab.value
    )

    // Empty identifier fails validation
    assertFalse(recoveryViewModel.submitPrimaryRecovery())
    assertNotNull(recoveryViewModel.errorMessage.value)

    // Switch to Forgot Username tab
    composeTestRule.onNodeWithTag("recovery_tab_forgot_username").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(
      com.cineflow.ai.feature.auth.AccountRecoveryTab.FORGOT_USERNAME,
      recoveryViewModel.selectedTab.value
    )

    // Switch to Forgot Email tab
    composeTestRule.onNodeWithTag("recovery_tab_forgot_email").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(
      com.cineflow.ai.feature.auth.AccountRecoveryTab.FORGOT_EMAIL,
      recoveryViewModel.selectedTab.value
    )

    // Switch back to Forgot Password tab and test valid email
    composeTestRule.onNodeWithTag("recovery_tab_forgot_password").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    recoveryViewModel.onIdentifierChange("creator@cineflow.ai")
    assertTrue(recoveryViewModel.submitPrimaryRecovery())
    assertNotNull(recoveryViewModel.recoveryStatusMessage.value)

    // Test Use a Recovery Link button
    assertTrue(recoveryViewModel.submitOneTimeRecoveryLink())
    assertNotNull(recoveryViewModel.recoveryStatusMessage.value)

    // Test Contact Support dialog
    composeTestRule.onNodeWithTag("recovery_contact_support_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(recoveryViewModel.showSupportDialog.value)
    composeTestRule.onNodeWithTag("recovery_support_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recovery_support_dialog_close_button").performClick()
    composeTestRule.waitForIdle()
    assertFalse(recoveryViewModel.showSupportDialog.value)

    // Test Back button
    composeTestRule.onNodeWithTag("recovery_back_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(navigatedBack)
  }

  @Test
  fun profileCompletionScreen_displaysReferenceUiValidatesUsernameAndPasswordAndCompletesProfile() {
    val profileViewModel = com.cineflow.ai.feature.auth.ProfileCompletionViewModel()
    profileViewModel.prepareForGoogleSignIn()
    var navigatedBack = false
    var completedUid: String? = null

    composeTestRule.setContent {
      CineFlowTheme {
        com.cineflow.ai.feature.auth.ProfileCompletionScreen(
          viewModel = profileViewModel,
          onBack = { navigatedBack = true },
          onProfileCompleted = { uid -> completedUid = uid }
        )
      }
    }

    composeTestRule.waitForIdle()

    // Verify top hero, avatar picker, and main card fields matching CineFlow AI Profile Setup.png
    composeTestRule.onNodeWithTag("profile_setup_top_hero").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_avatar_picker_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_main_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_username_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_username_valid_badge").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_country_code_selector").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_mobile_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_password_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_password_visibility_toggle").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_confirm_password_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_confirm_password_visibility_toggle").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_password_requirements_box").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_req_length").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_req_uppercase").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_req_lowercase").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_req_number").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_setup_submit_button").performScrollTo().assertIsDisplayed()

    // Initial state: all green ticks must be FALSE (never pre-ticked when fields are empty)
    assertFalse(profileViewModel.isUsernameValidAndAvailable.value)
    assertFalse(profileViewModel.hasValidPasswordLength.value)
    assertFalse(profileViewModel.hasUppercase.value)
    assertFalse(profileViewModel.hasLowercase.value)
    assertFalse(profileViewModel.hasNumber.value)
    assertFalse(profileViewModel.isPasswordStrong.value)
    assertFalse(profileViewModel.isConfirmPasswordMatching.value)
    assertFalse(profileViewModel.isProfileSetupCompleted.value)

    // Default country code is +91
    assertEquals("+91", profileViewModel.countryCode.value)

    // Taken username ("admin") sets isUsernameValidAndAvailable to false
    profileViewModel.onUsernameChange("admin")
    assertFalse(profileViewModel.isUsernameValidAndAvailable.value)
    assertFalse(profileViewModel.submitProfileCompletion())
    assertNotNull(profileViewModel.errorMessage.value)

    // Valid unique username sets isUsernameValidAndAvailable to true
    profileViewModel.onUsernameChange("cineflow_studio")
    assertTrue(profileViewModel.isUsernameValidAndAvailable.value)

    // Mobile number validation: incomplete digits for +91 fail validation; 10 digits pass
    profileViewModel.onMobileNumberChange("98765")
    assertFalse(profileViewModel.isMobileNumberValid.value)
    assertFalse(profileViewModel.submitProfileCompletion())
    profileViewModel.onMobileNumberChange("9876543210")
    assertTrue(profileViewModel.isMobileNumberValid.value)

    // Live password requirement green ticks update step-by-step as characters are typed
    profileViewModel.onPasswordChange("a")
    assertTrue(profileViewModel.hasLowercase.value)
    assertFalse(profileViewModel.hasUppercase.value)
    assertFalse(profileViewModel.hasNumber.value)
    assertFalse(profileViewModel.hasValidPasswordLength.value)

    profileViewModel.onPasswordChange("aB1")
    assertTrue(profileViewModel.hasLowercase.value)
    assertTrue(profileViewModel.hasUppercase.value)
    assertTrue(profileViewModel.hasNumber.value)
    assertFalse(profileViewModel.hasValidPasswordLength.value)
    assertFalse(profileViewModel.isPasswordStrong.value)

    // Mismatched confirm password fails validation even when password is strong
    profileViewModel.onPasswordChange("CineFlow2026")
    assertTrue(profileViewModel.hasValidPasswordLength.value)
    assertTrue(profileViewModel.isPasswordStrong.value)
    profileViewModel.onConfirmPasswordChange("WrongPass2026")
    assertFalse(profileViewModel.isConfirmPasswordMatching.value)
    assertFalse(profileViewModel.submitProfileCompletion())
    assertNotNull(profileViewModel.errorMessage.value)

    // Matching valid password completes profile and allocates CF + 8 digits Public User ID
    profileViewModel.onConfirmPasswordChange("CineFlow2026")
    assertTrue(profileViewModel.isConfirmPasswordMatching.value)
    assertTrue(profileViewModel.submitProfileCompletion { uid -> completedUid = uid })
    assertTrue(profileViewModel.isProfileSetupCompleted.value)
    assertNotNull(completedUid)
    assertTrue(Regex("^CF\\d{8}$").matches(completedUid!!))

    // Section 10 rule: when password is already configured, do not ask for password
    profileViewModel.setPasswordAlreadyConfigured(true)
    assertFalse(profileViewModel.passwordRequired.value)
    profileViewModel.onUsernameChange("cineflow_pro_2")
    assertTrue(profileViewModel.submitProfileCompletion())

    // Back button works
    composeTestRule.onNodeWithTag("profile_setup_back_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(navigatedBack)
  }

  @Test
  fun profileHubScreen_displaysReferenceUiCopiesPublicUidOpensSectionsAndLogsOut() {
    val hubViewModel = com.cineflow.ai.feature.auth.ProfileHubViewModel()
    var navigatedBack = false
    var loggedOut = false

    composeTestRule.setContent {
      CineFlowTheme {
        com.cineflow.ai.feature.auth.ProfileHubScreen(
          viewModel = hubViewModel,
          onBack = { navigatedBack = true },
          onLogOut = { loggedOut = true }
        )
      }
    }

    composeTestRule.waitForIdle()

    // Verify top header, identity section, and badges matching CineFlow AI Neon Profile Dashboard.png
    composeTestRule.onNodeWithTag("profile_hub_top_banner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_settings_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_identity_section").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_avatar_camera_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_display_name").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_verified_badge").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_username").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_public_uid_badge").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_subscriber_badge").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_active_badge").performScrollTo().assertIsDisplayed()

    // Public User ID matches CF + 8 numeric digits format
    assertTrue(Regex("^CF\\d{8}$").matches(hubViewModel.publicUid.value))

    // Copy Public User ID
    composeTestRule.onNodeWithTag("profile_hub_public_uid_badge").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertNotNull(hubViewModel.copiedUidFeedback.value)

    // Verify 3-column stats bar and Cloud Storage card
    composeTestRule.onNodeWithTag("profile_hub_stats_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_stat_projects").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_stat_backups").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_stat_storage").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_cloud_storage_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_storage_usage_text").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_storage_progress_bar").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_storage_manage_button").performScrollTo().assertIsDisplayed()

    // Verify all 5 action navigation rows
    composeTestRule.onNodeWithTag("profile_hub_row_personal_info").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_row_subscription_plan").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_row_cloud_backup").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_row_account_security").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_row_linked_accounts").performScrollTo().assertIsDisplayed()

    // Clicking Personal Information opens section dialog
    composeTestRule.onNodeWithTag("profile_hub_row_personal_info").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profile_hub_section_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_hub_section_dialog_close_button").performClick()
    composeTestRule.waitForIdle()

    // Clicking Log Out triggers onLogOut
    composeTestRule.onNodeWithTag("profile_hub_logout_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(loggedOut)

    // Clicking Back button triggers onBack
    composeTestRule.onNodeWithTag("profile_hub_back_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(navigatedBack)
  }

  @Test
  fun personalInformationScreen_displaysReferenceUiEditsFieldsCopiesPublicUidAndSavesChanges() {
    val personalInfoViewModel = com.cineflow.ai.feature.auth.PersonalInformationViewModel()
    var navigatedBack = false
    var savedFullName: String? = null
    var savedUsername: String? = null

    composeTestRule.setContent {
      CineFlowTheme {
        com.cineflow.ai.feature.auth.PersonalInformationScreen(
          viewModel = personalInfoViewModel,
          onBack = { navigatedBack = true },
          onChangesSaved = { name, user, _, _ ->
            savedFullName = name
            savedUsername = user
          }
        )
      }
    }

    composeTestRule.waitForIdle()

    // Verify top banner, identity row, and Change Profile Photo pill matching CineFlow AI Personal Profile Screen.png
    composeTestRule.onNodeWithTag("personal_info_top_banner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_identity_section").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_avatar_camera_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_header_name").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_verified_badge").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_header_uid").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_header_username").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_change_photo_button").performScrollTo().assertIsDisplayed()

    // Verify Personal Details card & all 5 rows
    composeTestRule.onNodeWithTag("personal_info_details_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_row_full_name").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_row_username").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_row_email").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_email_verified_badge", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_row_mobile").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_mobile_verified_badge", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_row_public_uid").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_copy_uid_button").performScrollTo().assertIsDisplayed()

    // Verify Additional Information card & all 3 rows
    composeTestRule.onNodeWithTag("personal_info_additional_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_row_dob").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_row_gender").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("personal_info_row_location").performScrollTo().assertIsDisplayed()

    // Verify Save Changes CTA button
    composeTestRule.onNodeWithTag("personal_info_save_button").performScrollTo().assertIsDisplayed()

    // Public User ID matches CF + 8 numeric digits format
    assertTrue(Regex("^CF\\d{8}$").matches(personalInfoViewModel.publicUid.value))

    // Copy Public User ID button works
    composeTestRule.onNodeWithTag("personal_info_copy_uid_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertNotNull(personalInfoViewModel.statusMessage.value)

    // Edit Full Name via modal dialog
    composeTestRule.onNodeWithTag("personal_info_row_full_name").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("personal_info_edit_dialog").assertIsDisplayed()
    personalInfoViewModel.onEditDraftChange("Manirul Studio")
    composeTestRule.onNodeWithTag("personal_info_edit_dialog_confirm_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals("Manirul Studio", personalInfoViewModel.fullName.value)

    // Save Changes button validates and triggers callback
    composeTestRule.onNodeWithTag("personal_info_save_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("Manirul Studio", savedFullName)
    assertEquals("manirul_editz", savedUsername)

    // Back button works
    composeTestRule.onNodeWithTag("personal_info_back_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(navigatedBack)
  }

  @Test
  fun subscriptionPlansScreen_displaysReferenceUiSwitchesCyclesAndEnforcesSection21DisabledPayments() {
    val subViewModel = com.cineflow.ai.feature.auth.SubscriptionPlansViewModel()
    var navigatedBack = false

    composeTestRule.setContent {
      CineFlowTheme {
        com.cineflow.ai.feature.auth.SubscriptionPlansScreen(
          viewModel = subViewModel,
          onBack = { navigatedBack = true }
        )
      }
    }

    composeTestRule.waitForIdle()

    // Verify top hero, back button, and Monthly/Yearly billing toggle bar
    composeTestRule.onNodeWithTag("subscription_plans_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_top_hero").assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_billing_toggle_bar").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_toggle_monthly").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_toggle_yearly").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_save_40_badge").performScrollTo().assertIsDisplayed()

    // Verify Free Plan & Pro Plan cards and buttons
    composeTestRule.onNodeWithTag("subscription_plans_row").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_free_plan_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_free_price_text").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_free_plan_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_pro_plan_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_most_popular_badge").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_pro_price_text").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_upgrade_pro_button").performScrollTo().assertIsDisplayed()

    // Verify 4-column feature highlights bar
    composeTestRule.onNodeWithTag("subscription_feature_highlights_bar").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_highlight_cloud_storage").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_highlight_ai_tools").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_highlight_4k_export").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_highlight_priority_support").performScrollTo().assertIsDisplayed()

    // Verify bottom Management card rows (Manage Subscription & Subscription History)
    composeTestRule.onNodeWithTag("subscription_management_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_manage_row").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_history_row").performScrollTo().assertIsDisplayed()

    // Default cycle is MONTHLY; switching to YEARLY updates state
    assertEquals(
      com.cineflow.ai.feature.auth.SubscriptionBillingCycle.MONTHLY,
      subViewModel.selectedBillingCycle.value
    )
    composeTestRule.onNodeWithTag("subscription_toggle_yearly").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(
      com.cineflow.ai.feature.auth.SubscriptionBillingCycle.YEARLY,
      subViewModel.selectedBillingCycle.value
    )

    // Section 21: Payment integration is disabled; Upgrade to Pro shows prepared dialog and never grants fake PRO tier
    assertFalse(subViewModel.isPaymentIntegrationEnabled)
    composeTestRule.onNodeWithTag("subscription_upgrade_pro_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("subscription_status_dialog").assertIsDisplayed()
    assertEquals(
      com.cineflow.ai.feature.auth.SubscriptionPlanTier.FREE,
      subViewModel.currentPlanTier.value
    )
    composeTestRule.onNodeWithTag("subscription_dialog_close_button").performClick()
    composeTestRule.waitForIdle()

    // Manage Subscription & Subscription History open their prepared modals
    composeTestRule.onNodeWithTag("subscription_manage_row").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("subscription_status_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_dialog_close_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("subscription_history_row").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("subscription_status_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("subscription_dialog_close_button").performClick()
    composeTestRule.waitForIdle()

    // Back button works
    composeTestRule.onNodeWithTag("subscription_back_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(navigatedBack)
  }

  @Test
  fun cloudBackupScreen_displaysReferenceUiUpdatesSelectionSummaryAndEnforcesManualBackup() {
    val backupViewModel = com.cineflow.ai.feature.backup.CloudBackupViewModel()
    var navigatedBack = false

    composeTestRule.setContent {
      CineFlowTheme {
        com.cineflow.ai.feature.backup.CloudBackupScreen(
          viewModel = backupViewModel,
          onBack = { navigatedBack = true }
        )
      }
    }

    composeTestRule.waitForIdle()

    // Verify top hero and back button matching Neon Cloud Backup Dashboard.png
    composeTestRule.onNodeWithTag("cloud_backup_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_top_hero").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_back_button").assertIsDisplayed()

    // Verify Select Data to Backup card & rows
    composeTestRule.onNodeWithTag("cloud_backup_select_data_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_select_all_row").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_select_all_checkbox", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_selected_count_label", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_row_projects").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_row_media_assets").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_row_app_data").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_row_ai_assets").performScrollTo().assertIsDisplayed()

    // Verify Backup Summary card
    composeTestRule.onNodeWithTag("cloud_backup_summary_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_summary_selected_items").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_summary_total_size").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_summary_cloud_storage").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_storage_progress_bar").performScrollTo().assertIsDisplayed()

    // Verify Backup Options card & Start Backup CTA
    composeTestRule.onNodeWithTag("cloud_backup_options_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_auto_backup_row").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_auto_backup_switch", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_manual_now_row").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_manual_radio", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_start_button").performScrollTo().assertIsDisplayed()

    // Initial selection matches reference screenshot: 3 of 4 selected (Projects, Media Assets, App Data & Presets) = 5.23 GB
    assertEquals(3, backupViewModel.selectedCount())
    assertEquals(4, backupViewModel.totalCategoriesCount)
    assertEquals("5.23 GB", backupViewModel.formattedTotalSizeGb())

    // Toggle Select All -> 4 of 4 selected (5.73 GB)
    composeTestRule.onNodeWithTag("cloud_backup_select_all_row").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(4, backupViewModel.selectedCount())
    assertEquals("5.73 GB", backupViewModel.formattedTotalSizeGb())

    // Toggle Select All again -> 0 of 4 selected (0.00 GB)
    composeTestRule.onNodeWithTag("cloud_backup_select_all_row").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(0, backupViewModel.selectedCount())
    assertEquals("0.00 GB", backupViewModel.formattedTotalSizeGb())

    // Restore 3 default categories and trigger Manual Backup
    backupViewModel.toggleCategory(com.cineflow.ai.feature.backup.CloudBackupCategory.PROJECTS)
    backupViewModel.toggleCategory(com.cineflow.ai.feature.backup.CloudBackupCategory.MEDIA_ASSETS)
    backupViewModel.toggleCategory(com.cineflow.ai.feature.backup.CloudBackupCategory.APP_DATA_PRESETS)
    composeTestRule.waitForIdle()

    // Section 12: Auto Backup is strictly disabled; tapping Auto Backup informs user and keeps autoBackupEnabled = false
    assertFalse(backupViewModel.autoBackupEnabled.value)
    composeTestRule.onNodeWithTag("cloud_backup_auto_backup_row").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertFalse(backupViewModel.autoBackupEnabled.value)
    composeTestRule.onNodeWithTag("cloud_backup_status_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_dialog_close_button").performClick()
    composeTestRule.waitForIdle()

    // Start Backup (Manual) executes manual backup and shows completion modal
    composeTestRule.onNodeWithTag("cloud_backup_start_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("cloud_backup_status_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cloud_backup_dialog_close_button").performClick()
    composeTestRule.waitForIdle()

    // Back button works
    composeTestRule.onNodeWithTag("cloud_backup_back_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(navigatedBack)
  }
}








