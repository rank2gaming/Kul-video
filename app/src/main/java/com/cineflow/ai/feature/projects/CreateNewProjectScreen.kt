package com.cineflow.ai.feature.projects

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R
import com.cineflow.ai.core.model.VideoProject
import com.cineflow.ai.ui.theme.CineFlowBadgeRed
import com.cineflow.ai.ui.theme.CineFlowCyan

const val ROUTE_CREATE_NEW_PROJECT = "create_new_project"

/**
 * Create New Project Screen implemented strictly from approved reference image:
 * `CineFlow AI Project Creation Screen.png`.
 *
 * Requirements (Section 9 & 26):
 * - Accept a project name (0 / 100 character counter).
 * - Validate the name and prevent blank names.
 * - Apply consistent duplicate-name handling (`LocalProjectRepository`).
 * - Generate a stable project ID.
 * - Persist the project locally in Room (`CineFlowDatabase`).
 * - Create an empty project/timeline structure (`durationMicros = 0L`, `clipCount = 0`, `trackCount = 1`).
 * - Navigate to the Editor entry boundary (`onProjectCreated`).
 * - Do not fabricate clips or media.
 */
@Composable
fun CreateNewProjectScreen(
  viewModel: ProjectManagementViewModel,
  onBackToProjects: () -> Unit,
  onProjectCreated: (VideoProject) -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBackToProjects)

  var projectName by remember { mutableStateOf("") }
  val errorMessage by viewModel.createErrorMessage.collectAsStateWithLifecycle()

  val backDesc = stringResource(R.string.cnp_cd_back)
  val createDesc = stringResource(R.string.cnp_action_create_project)
  val cancelDesc = stringResource(R.string.cnp_action_cancel)

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0xFF0E0538),
            Color(0xFF100A3E),
            Color(0xFF120C44)
          )
        )
      )
      .testTag("create_new_project_screen"),
    containerColor = Color.Transparent,
    contentWindowInsets = WindowInsets.safeDrawing
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
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // 1. Top Hero Artwork + 3D Cinema Camera + Floating Clapperboard Icon + "Create New Project" Header
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(941f / 652f)
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_create_project_top_hero),
            contentDescription = stringResource(R.string.cnp_title),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
          )

          // Top-left Back arrow interactive hit-target
          Box(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(start = 10.dp, top = 12.dp)
              .size(48.dp)
              .clip(CircleShape)
              .clickable {
                viewModel.dismissCreateProjectDialog()
                onBackToProjects()
              }
              .semantics { contentDescription = backDesc }
              .testTag("create_project_back_button")
          )
        }

        // 2. Main Neon Card Body (Seamlessly continuing the card from img_create_project_top_hero)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 9.5.dp)
            .shadow(
              elevation = 16.dp,
              shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
              spotColor = Color(0xFFE040FB)
            )
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color(0xFF13123F),
                  Color(0xFF131140),
                  Color(0xFF151145)
                )
              )
            )
            .border(
              width = 1.5.dp,
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color(0xFF6D28D9),
                  Color(0xFFA855F7),
                  Color(0xFFF43F5E),
                  Color(0xFF38BDF8)
                )
              ),
              shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
          // Label: "Project Name *"
          Text(
            text = buildAnnotatedString {
              withStyle(
                SpanStyle(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              ) {
                append(stringResource(R.string.cnp_label_project_name))
              }
              withStyle(
                SpanStyle(
                  color = Color(0xFFFF4B55),
                  fontWeight = FontWeight.ExtraBold
                )
              ) {
                append("*")
              }
            },
            fontSize = 16.5.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Input Box with Pencil Icon + "Enter project name"
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF1F1E4E))
              .border(
                width = 1.2.dp,
                color = if (errorMessage != null) CineFlowBadgeRed else Color(0xFF4C4A8A),
                shape = RoundedCornerShape(14.dp)
              )
              .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.Edit,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
              modifier = Modifier.weight(1f),
              contentAlignment = Alignment.CenterStart
            ) {
              if (projectName.isEmpty()) {
                Text(
                  text = stringResource(R.string.cnp_placeholder_project_name),
                  color = Color(0xFFB4B2D8),
                  fontSize = 15.5.sp
                )
              }
              BasicTextField(
                value = projectName,
                onValueChange = { updated ->
                  if (updated.length <= 100) {
                    projectName = updated
                  }
                },
                singleLine = true,
                textStyle = TextStyle(
                  color = Color.White,
                  fontSize = 15.5.sp,
                  fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(CineFlowCyan),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("create_project_name_input")
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Validation Error (left) & "0 / 100" Character Counter (right)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.weight(1f)) {
              if (errorMessage != null) {
                Text(
                  text = errorMessage ?: "",
                  color = CineFlowBadgeRed,
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.testTag("create_project_error_text")
                )
              }
            }
            Text(
              text = stringResource(R.string.cnp_char_counter, projectName.length),
              color = Color(0xFFB8B5E0),
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.testTag("create_project_char_counter")
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Info Box: "You can change the project name later from Project Settings."
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF1E2D66))
              .border(
                width = 1.2.dp,
                color = Color(0xFF4361AC),
                shape = RoundedCornerShape(16.dp)
              )
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .testTag("create_project_info_box"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = stringResource(R.string.cnp_info_rename_later),
              color = Color(0xFFE6E8F8),
              fontSize = 13.5.sp,
              lineHeight = 19.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Spacer(modifier = Modifier.height(22.dp))

          // Primary CTA Button: "Create Project ->" (Exact reference artwork button)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(814f / 116f)
              .clip(RoundedCornerShape(50))
              .clickable {
                viewModel.createNewProject(
                  rawName = projectName,
                  onProjectCreated = onProjectCreated
                )
              }
              .semantics { contentDescription = createDesc }
              .testTag("create_project_confirm_button")
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_create_project_cta_button),
              contentDescription = createDesc,
              contentScale = ContentScale.FillBounds,
              modifier = Modifier.fillMaxSize()
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Secondary Button: "Cancel"
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .clip(RoundedCornerShape(26.dp))
              .background(Color(0xFF141342))
              .border(
                width = 1.2.dp,
                color = Color(0xFF3E3B7A),
                shape = RoundedCornerShape(26.dp)
              )
              .clickable {
                viewModel.dismissCreateProjectDialog()
                onBackToProjects()
              }
              .semantics { contentDescription = cancelDesc }
              .testTag("create_project_cancel_button"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = stringResource(R.string.cnp_action_cancel),
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}
