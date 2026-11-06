package com.cineflow.ai.feature.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R

private val RecoveryBgDeep = Color(0xFF07071A)
private val RecoveryCardSurface = Color(0xE8131633)
private val RecoveryCardBorder = Color(0xFF394170)
private val RecoveryInputBg = Color(0xFF191E3F)
private val RecoveryInputBorder = Color(0xFF565E96)
private val RecoveryTextWhite = Color(0xFFFFFFFF)
private val RecoveryTextMuted = Color(0xFFB7BCD9)
private val RecoveryCyanHighlight = Color(0xFF19DCFF)

/**
 * Forgot Password / Account Recovery Screen (`CineFlow AI Account Recovery UI.png` & Master Prompt Section 10).
 */
@Composable
fun AccountRecoveryScreen(
  viewModel: AccountRecoveryViewModel,
  onBackToLogin: () -> Unit,
  modifier: Modifier = Modifier
) {
  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
  val identifierInput by viewModel.identifierInput.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
  val recoveryStatusMessage by viewModel.recoveryStatusMessage.collectAsStateWithLifecycle()
  val showSupportDialog by viewModel.showSupportDialog.collectAsStateWithLifecycle()

  BackHandler {
    onBackToLogin()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(RecoveryBgDeep)
      .testTag("account_recovery_screen"),
    containerColor = RecoveryBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(RecoveryBgDeep),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Hero Illustration (`img_recovery_top_hero.png`) + Interactive Back Button
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(941f / 505f)
          .testTag("recovery_top_hero")
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_recovery_top_hero),
          contentDescription = "CineFlow AI Account Recovery — Recover Your Account",
          contentScale = ContentScale.FillWidth,
          modifier = Modifier.fillMaxSize()
        )

        // Top-left back arrow hotspot (`←`)
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 10.dp, top = 6.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onBackToLogin)
            .testTag("recovery_back_button")
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 2. 3-Tab Segmented Bar (`Forgot Password` | `Forgot Username` | `Forgot Email`)
      AccountRecoverySegmentedTabBar(
        selectedTab = selectedTab,
        onSelectTab = { tab -> viewModel.selectTab(tab) },
        modifier = Modifier.padding(horizontal = 14.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Main Recovery Action Card (`Reset Your Password` / `Send Verification Code` / `Use a Recovery Link`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(RecoveryCardSurface)
          .border(1.2.dp, RecoveryCardBorder, RoundedCornerShape(20.dp))
          .padding(horizontal = 16.dp, vertical = 16.dp)
          .testTag("recovery_main_card")
      ) {
        // Card Header Row (Purple Square Envelope Badge + Title & Subtitle)
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(
                Brush.linearGradient(
                  colors = listOf(Color(0xFF422C7A), Color(0xFF2E225C))
                )
              )
              .border(1.dp, Color(0xFF664CB0), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Email,
              contentDescription = null,
              tint = RecoveryTextWhite,
              modifier = Modifier.size(28.dp)
            )
          }

          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = selectedTab.cardTitle,
              color = RecoveryTextWhite,
              fontSize = 19.sp,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.testTag("recovery_card_title")
            )
            Text(
              text = selectedTab.cardDescription,
              color = RecoveryTextMuted,
              fontSize = 12.2.sp,
              lineHeight = 16.5.sp,
              modifier = Modifier.testTag("recovery_card_subtitle")
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Field Label (`Email Address or Mobile Number`)
        Text(
          text = selectedTab.inputLabel,
          color = RecoveryTextWhite,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Input Box (`recovery_identifier_input`)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(RecoveryInputBg)
            .border(1.dp, RecoveryInputBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = null,
            tint = Color(0xFFD6DAEE),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
          ) {
            if (identifierInput.isEmpty()) {
              Text(
                text = selectedTab.inputPlaceholder,
                color = Color(0xFF969CC2),
                fontSize = 13.5.sp
              )
            }
            BasicTextField(
              value = identifierInput,
              onValueChange = { viewModel.onIdentifierChange(it) },
              singleLine = true,
              textStyle = TextStyle(
                color = RecoveryTextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
              ),
              cursorBrush = SolidColor(RecoveryCyanHighlight),
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
              ),
              keyboardActions = KeyboardActions(
                onDone = { viewModel.submitPrimaryRecovery() }
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("recovery_identifier_input")
            )
          }
        }

        // Validation Error / Recovery Status Banner
        if (!errorMessage.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorMessage!!,
            color = Color(0xFFFF6E7E),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("recovery_error_text")
          )
        }

        if (!recoveryStatusMessage.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = recoveryStatusMessage!!,
            color = Color(0xFF47E5BC),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("recovery_status_text")
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Primary Pill Gradient Button (`[paper-plane] Send Verification Code`)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(
              Brush.horizontalGradient(
                colors = listOf(
                  Color(0xFFD63BFF),
                  Color(0xFF7252FF),
                  Color(0xFF10DCFF)
                )
              )
            )
            .border(
              width = 1.dp,
              brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFEB94FF), Color(0xFF7CEEFF))
              ),
              shape = RoundedCornerShape(25.dp)
            )
            .clickable { viewModel.submitPrimaryRecovery() }
            .testTag("recovery_primary_submit_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            PaperPlaneVectorIcon(modifier = Modifier.size(20.dp))
            Text(
              text = selectedTab.primaryButtonLabel,
              color = Color.White,
              fontSize = 15.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // `───────  OR  ───────` Divider Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(1.dp)
              .background(Color(0xFF333960))
          )
          Text(
            text = "OR",
            color = RecoveryTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .height(1.dp)
              .background(Color(0xFF333960))
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // `Use a Recovery Link` Card Button (`recovery_link_card_button`)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF181C3E))
            .border(1.dp, Color(0xFF4F578E), RoundedCornerShape(14.dp))
            .clickable { viewModel.submitOneTimeRecoveryLink() }
            .padding(horizontal = 14.dp, vertical = 11.dp)
            .testTag("recovery_link_card_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  colors = listOf(Color(0xFF623CEA), Color(0xFF422C9E))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Link,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(21.dp)
            )
          }

          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Text(
              text = "Use a Recovery Link",
              color = RecoveryTextWhite,
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Get a one-time recovery link to your email",
              color = RecoveryTextMuted,
              fontSize = 12.sp
            )
          }

          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = RecoveryTextWhite,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4. Bottom `Need Help?` Support Card (`recovery_support_card`)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(RecoveryCardSurface)
          .border(1.dp, RecoveryCardBorder, RoundedCornerShape(16.dp))
          .padding(horizontal = 14.dp, vertical = 12.dp)
          .testTag("recovery_support_card"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Info,
          contentDescription = null,
          tint = Color(0xFFB4BAE0),
          modifier = Modifier.size(22.dp)
        )

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Text(
            text = "Need Help?",
            color = RecoveryTextWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "If you no longer have access to your email or mobile number, contact our support team for account recovery.",
            color = RecoveryTextMuted,
            fontSize = 10.8.sp,
            lineHeight = 14.5.sp
          )
        }

        // `Contact Support` Pill Button (`recovery_contact_support_button`)
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1A1E44))
            .border(1.dp, Color(0xFF6B5CE7), RoundedCornerShape(18.dp))
            .clickable { viewModel.openSupportDialog() }
            .padding(horizontal = 11.dp, vertical = 7.dp)
            .testTag("recovery_contact_support_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.HeadsetMic,
            contentDescription = null,
            tint = RecoveryTextWhite,
            modifier = Modifier.size(15.dp)
          )
          Text(
            text = "Contact Support",
            color = RecoveryTextWhite,
            fontSize = 11.2.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // Contact Support Dialog
  if (showSupportDialog) {
    AlertDialog(
      onDismissRequest = { viewModel.dismissSupportDialog() },
      containerColor = Color(0xFF161936),
      title = {
        Text(
          text = "CineFlow AI Account Support",
          color = RecoveryTextWhite,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier.testTag("recovery_support_dialog"),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "If you lost access to both your registered email and mobile number, provide your Public User ID (e.g., CF00000001) or username to our recovery team at support@cineflow.ai.",
            color = RecoveryTextMuted,
            fontSize = 13.sp
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.dismissSupportDialog() },
          colors = ButtonDefaults.buttonColors(containerColor = RecoveryCyanHighlight),
          modifier = Modifier.testTag("recovery_support_dialog_close_button")
        ) {
          Text("Got It", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

@Composable
private fun AccountRecoverySegmentedTabBar(
  selectedTab: AccountRecoveryTab,
  onSelectTab: (AccountRecoveryTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0xFF121530))
      .border(1.dp, Color(0xFF3E4574), RoundedCornerShape(16.dp))
      .padding(3.dp)
      .testTag("recovery_tab_bar"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    AccountRecoveryTab.entries.forEachIndexed { index, tab ->
      val isSelected = selectedTab == tab
      val tabTag = when (tab) {
        AccountRecoveryTab.FORGOT_PASSWORD -> "recovery_tab_forgot_password"
        AccountRecoveryTab.FORGOT_USERNAME -> "recovery_tab_forgot_username"
        AccountRecoveryTab.FORGOT_EMAIL -> "recovery_tab_forgot_email"
      }
      val icon = when (tab) {
        AccountRecoveryTab.FORGOT_PASSWORD -> Icons.Outlined.Key
        AccountRecoveryTab.FORGOT_USERNAME -> Icons.Outlined.Person
        AccountRecoveryTab.FORGOT_EMAIL -> Icons.Outlined.Email
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(13.dp))
          .then(
            if (isSelected) {
              Modifier
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color(0xFF9340FF), Color(0xFF5348FF))
                  )
                )
                .border(1.dp, Color(0xFFB57CFF), RoundedCornerShape(13.dp))
            } else {
              Modifier
            }
          )
          .clickable { onSelectTab(tab) }
          .padding(vertical = 8.dp, horizontal = 4.dp)
          .testTag(tabTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = RecoveryTextWhite,
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = tab.tabTitle,
          color = RecoveryTextWhite,
          fontSize = 11.2.sp,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          maxLines = 1
        )
      }

      if (index < AccountRecoveryTab.entries.lastIndex && !isSelected) {
        Box(
          modifier = Modifier
            .width(1.dp)
            .fillMaxHeight(0.6f)
            .background(Color(0xFF2D3358))
        )
      }
    }
  }
}

@Composable
private fun PaperPlaneVectorIcon(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val planePath = Path().apply {
      moveTo(w * 0.10f, h * 0.46f)
      lineTo(w * 0.88f, h * 0.14f)
      lineTo(w * 0.56f, h * 0.88f)
      lineTo(w * 0.44f, h * 0.56f)
      close()
    }
    drawPath(planePath, color = Color.White, style = stroke)
    drawLine(
      color = Color.White,
      start = androidx.compose.ui.geometry.Offset(w * 0.88f, h * 0.14f),
      end = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.56f),
      strokeWidth = w * 0.09f,
      cap = StrokeCap.Round
    )
  }
}
