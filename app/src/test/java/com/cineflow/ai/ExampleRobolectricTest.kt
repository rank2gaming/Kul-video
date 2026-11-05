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
}


