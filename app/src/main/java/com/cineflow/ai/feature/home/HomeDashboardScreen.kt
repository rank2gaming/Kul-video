package com.cineflow.ai.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R
import com.cineflow.ai.core.firebase.GoogleSignInButton
import com.cineflow.ai.core.firebase.signOut
import com.cineflow.ai.core.model.VideoProject
import com.cineflow.ai.ui.theme.CineFlowBadgeRed
import com.cineflow.ai.ui.theme.CineFlowDeepNavy
import com.cineflow.ai.ui.theme.CineFlowMutedGray
import com.cineflow.ai.ui.theme.CineFlowPurple
import com.cineflow.ai.ui.theme.CineFlowSearchBg
import com.cineflow.ai.ui.theme.CineFlowSlate
import com.cineflow.ai.ui.theme.CineFlowSurfaceBg
import com.cineflow.ai.ui.theme.CineFlowViolet

const val ROUTE_HOME_DASHBOARD = "home_dashboard"
const val ROUTE_PROJECT_MANAGEMENT = "project_management"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
  viewModel: HomeDashboardViewModel,
  isSignedIn: Boolean,
  currentUserEmail: String?,
  publicUid: String?,
  onNavigateToVideoEdit: () -> Unit,
  onAuthChanged: () -> Unit,
  onNavigateToProfileHub: () -> Unit = {},
  onNavigateToSettings: (() -> Unit)? = null,
  onNavigateToNotifications: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val heroPage by viewModel.heroBannerPage.collectAsStateWithLifecycle()
  val comingSoonFeature by viewModel.comingSoonFeatureName.collectAsStateWithLifecycle()
  val showAccountSheet by viewModel.showAccountSheet.collectAsStateWithLifecycle()
  var authErrorMessage by remember { mutableStateOf<String?>(null) }

  val filteredTools = remember(searchQuery, viewModel.allTools) {
    if (searchQuery.isBlank()) {
      viewModel.allTools
    } else {
      viewModel.allTools.filter {
        it.titleText.contains(searchQuery.trim(), ignoreCase = true)
      }
    }
  }

  val filteredTemplates = remember(searchQuery, viewModel.allTemplates) {
    if (searchQuery.isBlank()) {
      viewModel.allTemplates
    } else {
      viewModel.allTemplates.filter {
        it.titleText.contains(searchQuery.trim(), ignoreCase = true)
      }
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
      .testTag("home_dashboard_screen"),
    containerColor = Color.White,
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      CineFlowBottomNavigationBar(
        selectedTab = BottomNavTab.HOME,
        onTabSelected = { tab ->
          when (tab) {
            BottomNavTab.HOME -> Unit
            BottomNavTab.PROJECTS -> onNavigateToVideoEdit()
            BottomNavTab.NOTIFICATIONS -> {
              if (onNavigateToNotifications != null) {
                onNavigateToNotifications()
              } else {
                viewModel.showComingSoon("Notification Center")
              }
            }
            BottomNavTab.SETTINGS -> {
              if (onNavigateToSettings != null) {
                onNavigateToSettings()
              } else {
                viewModel.showComingSoon("Settings")
              }
            }
          }
        }
      )
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
          .fillMaxWidth()
          .widthIn(max = 600.dp)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        // 1. Top Header Bar (3-dot button, CineFlow AI logo/title, Notification bell with red dot, Avatar)
        HomeTopAppBar(
          isSignedIn = isSignedIn,
          onOverflowClick = { viewModel.setAccountSheetVisible(true) },
          onNotificationsClick = {
            if (onNavigateToNotifications != null) {
              onNavigateToNotifications()
            } else {
              viewModel.showComingSoon("Notification Center")
            }
          },
          onAvatarClick = onNavigateToProfileHub
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Pill Search Bar ("Search tools, templates, effects...")
        HomeSearchBar(
          query = searchQuery,
          onQueryChange = viewModel::onSearchQueryChanged
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. AI Powered Hero Banner (exact artwork from 01_Home_Dashboard.png)
        AiPoweredHeroBanner(
          currentPage = heroPage,
          onPageSelected = viewModel::onHeroPageSelected,
          onGetStartedClick = onNavigateToVideoEdit
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. 3x3 Feature Tools Grid (exact icons and pastel cards from 01_Home_Dashboard.png)
        val rows = filteredTools.chunked(3)
        Column(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.testTag("home_feature_grid")
        ) {
          rows.forEach { rowItems ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              rowItems.forEach { tool ->
                HomeToolCard(
                  tool = tool,
                  onClick = {
                    if (tool.isActiveInInitialScope) {
                      onNavigateToVideoEdit()
                    } else {
                      viewModel.showComingSoon(tool.titleText)
                    }
                  },
                  modifier = Modifier.weight(1f)
                )
              }
              repeat(3 - rowItems.size) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Trending Templates Section Header ("Trending Templates" + "See All >")
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(R.string.section_trending_templates),
            color = CineFlowDeepNavy,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
          )
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { viewModel.showComingSoon("Trending Templates Catalog") }
              .padding(horizontal = 4.dp, vertical = 2.dp)
              .testTag("home_see_all_templates_button"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = stringResource(R.string.action_see_all),
              color = CineFlowPurple,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = null,
              tint = CineFlowPurple,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6. 5-Column Trending Templates Row (All 5 cards visible simultaneously as in 01_Home_Dashboard.png)
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("home_trending_templates_row")
        ) {
          filteredTemplates.forEach { template ->
            TrendingTemplateCard(
              item = template,
              onClick = {
                viewModel.showComingSoon("${template.titleText} Template")
              },
              modifier = Modifier.weight(1f)
            )
          }
          repeat((5 - filteredTemplates.size).coerceAtLeast(0)) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
      }
    }
  }

  // Honest Coming Soon Modal (Section 9: "Other unfinished features must have honest Coming Soon states")
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
          modifier = Modifier.testTag("coming_soon_dismiss_button")
        ) {
          Text(
            text = stringResource(R.string.action_got_it),
            color = CineFlowPurple,
            fontWeight = FontWeight.Bold
          )
        }
      },
      modifier = Modifier.testTag("coming_soon_dialog")
    )
  }

  // Account, Download APK & Authentication Sheet (Opened from left 3-dot menu icon & avatar)
  if (showAccountSheet) {
    val context = LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val apkLandingPageUrl = "https://ais-dev-zwzvlrblweb22g5xcffg7w-975889889415.asia-east1.run.app/"

    ModalBottomSheet(
      onDismissRequest = { viewModel.setAccountSheetVisible(false) },
      containerColor = Color.White,
      modifier = Modifier.testTag("account_auth_sheet")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = if (isSignedIn) "Authenticated Cloud Session" else "Local-Only Mode (Unsigned)",
          fontSize = 18.sp,
          fontWeight = FontWeight.ExtraBold,
          color = CineFlowDeepNavy
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = if (isSignedIn) {
            "Signed in as ${currentUserEmail ?: "Creator"} • Public ID: ${publicUid ?: "Allocating..."}\nExport unlocked up to 8K (subject to device capability). Project metadata sync is active; source media is never uploaded automatically."
          } else {
            "Editing is offline-first and projects are stored locally. Unsigned users can export up to 720p. Sign in with Google to unlock up to 8K export and project metadata sync."
          },
          fontSize = 13.sp,
          color = CineFlowSlate,
          textAlign = TextAlign.Center
        )

        authErrorMessage?.let { errorMsg ->
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorMsg,
            color = CineFlowBadgeRed,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Direct APK Download Landing Page Button inside the left 3-dot menu
        Button(
          onClick = {
            runCatching {
              uriHandler.openUri(apkLandingPageUrl)
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = CineFlowPurple,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("home_overflow_download_apk_button")
        ) {
          Text(
            text = "Download CineFlow AI APK",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!isSignedIn) {
          GoogleSignInButton(
            onAuthSuccess = {
              authErrorMessage = null
              viewModel.setAccountSheetVisible(false)
              onAuthChanged()
            },
            onAuthError = { _ ->
              authErrorMessage = null
              viewModel.setAccountSheetVisible(false)
              onAuthChanged()
            },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedButton(
            onClick = {
              authErrorMessage = null
              viewModel.setAccountSheetVisible(false)
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auth_skip_button")
          ) {
            Text(
              text = stringResource(R.string.auth_continue_offline_skip),
              color = CineFlowDeepNavy,
              fontWeight = FontWeight.SemiBold
            )
          }
        } else {
          OutlinedButton(
            onClick = {
              signOut(
                context = context,
                credentialManager = credentialManager,
                onSignOutComplete = {
                  onAuthChanged()
                  viewModel.setAccountSheetVisible(false)
                },
                scope = scope
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auth_sign_out_button")
          ) {
            Text(
              text = stringResource(R.string.auth_sign_out),
              color = CineFlowBadgeRed,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}

@Composable
private fun HomeTopAppBar(
  isSignedIn: Boolean,
  onOverflowClick: () -> Unit,
  onNotificationsClick: () -> Unit,
  onAvatarClick: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      // 3-dot circular button
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(CineFlowSearchBg)
          .clickable(onClick = onOverflowClick)
          .testTag("home_overflow_menu_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.MoreVert,
          contentDescription = stringResource(R.string.cd_overflow_menu),
          tint = CineFlowDeepNavy,
          modifier = Modifier.size(21.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // 3D Play Ribbon Brand Logo from 01_Home_Dashboard.png
      CineFlowBrandLogo()

      Spacer(modifier = Modifier.width(8.dp))

      // "CineFlow AI" styled brand title
      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = CineFlowDeepNavy,
              fontWeight = FontWeight.ExtraBold
            )
          ) {
            append(stringResource(R.string.brand_prefix))
          }
          withStyle(
            SpanStyle(
              brush = Brush.horizontalGradient(
                listOf(CineFlowPurple, CineFlowViolet)
              ),
              fontWeight = FontWeight.ExtraBold
            )
          ) {
            append(stringResource(R.string.brand_suffix))
          }
        },
        fontSize = 23.sp,
        letterSpacing = (-0.3).sp
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      // Notification Bell in light circle with red badge dot at top-right edge
      Box(
        modifier = Modifier
          .size(44.dp)
          .clickable(onClick = onNotificationsClick)
          .testTag("home_notifications_bell_button"),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(CineFlowSearchBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = stringResource(R.string.cd_notifications),
            tint = CineFlowDeepNavy,
            modifier = Modifier.size(22.dp)
          )
        }
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = (-3).dp, y = 3.dp)
            .size(9.dp)
            .clip(CircleShape)
            .background(CineFlowBadgeRed)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Circular Creator Avatar from 01_Home_Dashboard.png
      CreatorAvatarButton(
        isSignedIn = isSignedIn,
        onClick = onAvatarClick,
        modifier = Modifier.testTag("home_profile_avatar_button")
      )
    }
  }
}

@Composable
private fun HomeSearchBar(
  query: String,
  onQueryChange: (String) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(46.dp)
      .clip(RoundedCornerShape(23.dp))
      .background(CineFlowSearchBg)
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.Search,
      contentDescription = stringResource(R.string.cd_search_icon),
      tint = CineFlowMutedGray,
      modifier = Modifier.size(21.dp)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Box(
      modifier = Modifier.weight(1f),
      contentAlignment = Alignment.CenterStart
    ) {
      if (query.isEmpty()) {
        Text(
          text = stringResource(R.string.search_placeholder),
          color = CineFlowMutedGray,
          fontSize = 14.5.sp
        )
      }
      BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(
          color = CineFlowDeepNavy,
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Medium
        ),
        cursorBrush = SolidColor(CineFlowPurple),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("home_search_input")
      )
    }
    if (query.isNotEmpty()) {
      IconButton(
        onClick = { onQueryChange("") },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = stringResource(R.string.cd_clear_search),
          tint = CineFlowSlate,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
private fun HomeToolCard(
  tool: HomeToolItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val title = stringResource(tool.titleRes)
  Column(
    modifier = modifier
      .height(104.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(tool.cardBackground)
      .clickable(onClick = onClick)
      .padding(horizontal = 4.dp, vertical = 10.dp)
      .testTag(tool.testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Image(
      painter = painterResource(id = tool.iconDrawableRes),
      contentDescription = title,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .size(54.dp)
        .shadow(
          elevation = 5.dp,
          shape = CircleShape,
          spotColor = tool.iconCircleColor.copy(alpha = 0.45f)
        )
        .clip(CircleShape)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = title,
      color = CineFlowDeepNavy,
      fontSize = 11.5.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun CineFlowBottomNavigationBar(
  selectedTab: BottomNavTab,
  onTabSelected: (BottomNavTab) -> Unit
) {
  Surface(
    color = Color.White,
    shadowElevation = 12.dp,
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.navigationBars)
        .height(68.dp)
        .padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Home
      BottomNavItem(
        label = stringResource(R.string.nav_home),
        icon = Icons.Default.Home,
        isSelected = selectedTab == BottomNavTab.HOME,
        showBadge = false,
        testTag = "nav_tab_home",
        onClick = { onTabSelected(BottomNavTab.HOME) }
      )
      // 2. Projects
      BottomNavItem(
        label = stringResource(R.string.nav_projects),
        icon = Icons.Outlined.Folder,
        isSelected = selectedTab == BottomNavTab.PROJECTS,
        showBadge = false,
        testTag = "nav_tab_projects",
        onClick = { onTabSelected(BottomNavTab.PROJECTS) }
      )
      // 3. Notifications (with red badge dot as in 01_Home_Dashboard.png)
      BottomNavItem(
        label = stringResource(R.string.nav_notifications),
        icon = Icons.Outlined.Notifications,
        isSelected = selectedTab == BottomNavTab.NOTIFICATIONS,
        showBadge = true,
        testTag = "nav_tab_notifications",
        onClick = { onTabSelected(BottomNavTab.NOTIFICATIONS) }
      )
      // 4. Settings
      BottomNavItem(
        label = stringResource(R.string.nav_settings),
        icon = Icons.Outlined.Settings,
        isSelected = selectedTab == BottomNavTab.SETTINGS,
        showBadge = false,
        testTag = "nav_tab_settings",
        onClick = { onTabSelected(BottomNavTab.SETTINGS) }
      )
    }
  }
}

@Composable
private fun BottomNavItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  showBadge: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  val tint = if (isSelected) CineFlowPurple else CineFlowSlate
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 4.dp)
      .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = tint,
        modifier = Modifier.size(25.dp)
      )
      if (showBadge) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 4.dp, y = (-2).dp)
            .size(8.5.dp)
            .clip(CircleShape)
            .background(CineFlowBadgeRed)
        )
      }
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      color = tint,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(4.dp))
    if (isSelected) {
      Box(
        modifier = Modifier
          .width(48.dp)
          .height(3.5.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(CineFlowPurple)
      )
    } else {
      Spacer(modifier = Modifier.height(3.5.dp))
    }
  }
}

/**
 * Video Edit Navigation Boundary (`ROUTE_PROJECT_MANAGEMENT`).
 */
@Composable
fun VideoEditNavigationBoundaryScreen(
  localProjects: List<VideoProject>,
  onBackToHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBackToHome)

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("video_edit_boundary_screen"),
    containerColor = CineFlowSurfaceBg,
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 480.dp)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(CineFlowPurple.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ContentCut,
              contentDescription = null,
              tint = CineFlowPurple,
              modifier = Modifier.size(28.dp)
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Video Edit → Project Management Boundary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = CineFlowDeepNavy,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Local Room repository connected (${localProjects.size} persisted local projects).",
            style = MaterialTheme.typography.bodyMedium,
            color = CineFlowSlate,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = onBackToHome,
            colors = ButtonDefaults.buttonColors(containerColor = CineFlowPurple),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("back_to_home_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Return to Home Dashboard", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
