package com.cineflow.ai.feature.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val SignUpBgDeep = Color(0xFF09071A)
private val SignUpCardSurface = Color(0xE810122A)
private val SignUpFieldBg = Color(0xFF171A38)
private val SignUpFieldBorder = Color(0xFF53598A)
private val SignUpTextWhite = Color(0xFFFFFFFF)
private val SignUpTextMuted = Color(0xFFB5B9D8)
private val SignUpCheckGreen = Color(0xFF39E65C)
private val SignUpMagentaLink = Color(0xFFD638FF)

/**
 * Sign Up / Account Creation Screen (`CineFlow AI Account Creation Screen.png` & Master Prompt Section 10).
 */
@Composable
fun SignUpScreen(
  viewModel: SignUpViewModel,
  onBackToLogin: () -> Unit,
  onSignUpSuccess: (publicUid: String?) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val fullName by viewModel.fullName.collectAsStateWithLifecycle()
  val username by viewModel.username.collectAsStateWithLifecycle()
  val email by viewModel.email.collectAsStateWithLifecycle()
  val countryCode by viewModel.countryCode.collectAsStateWithLifecycle()
  val mobileNumber by viewModel.mobileNumber.collectAsStateWithLifecycle()
  val password by viewModel.password.collectAsStateWithLifecycle()
  val confirmPassword by viewModel.confirmPassword.collectAsStateWithLifecycle()
  val isPasswordVisible by viewModel.isPasswordVisible.collectAsStateWithLifecycle()
  val isConfirmPasswordVisible by viewModel.isConfirmPasswordVisible.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
  val infoMessage by viewModel.infoMessage.collectAsStateWithLifecycle()

  BackHandler {
    onBackToLogin()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(SignUpBgDeep)
      .testTag("signup_screen"),
    containerColor = SignUpBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(SignUpBgDeep),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Hero Header Banner (`img_signup_top_hero.png`) + Interactive Back Button Hotspot
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(941f / 292f)
          .testTag("signup_top_hero")
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_signup_top_hero),
          contentDescription = "Create Account — Sign up to get started with CineFlow AI",
          contentScale = ContentScale.FillWidth,
          modifier = Modifier.fillMaxSize()
        )

        // Back arrow interactive target (`←`)
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 10.dp, top = 8.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onBackToLogin)
            .testTag("signup_back_button")
        )
      }

      // 2. Main Glassmorphic Card (`signup_card_container`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(SignUpCardSurface)
          .border(
            width = 1.3.dp,
            brush = Brush.verticalGradient(
              colors = listOf(
                Color(0xFFC252FF),
                Color(0xFF5D46E6),
                Color(0xFFB83CFF)
              )
            ),
            shape = RoundedCornerShape(24.dp)
          )
          .padding(horizontal = 16.dp, vertical = 14.dp)
          .testTag("signup_card_container"),
        verticalArrangement = Arrangement.spacedBy(9.dp)
      ) {
        // Field 1: Full Name
        SignUpStackedInputField(
          label = "Full Name",
          placeholder = "Enter your full name",
          value = fullName,
          onValueChange = { viewModel.onFullNameChange(it) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Person,
              contentDescription = null,
              tint = SignUpTextWhite,
              modifier = Modifier.size(21.dp)
            )
          },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
          ),
          testTag = "signup_full_name_input"
        )

        // Field 2: Username
        SignUpStackedInputField(
          label = "Username",
          placeholder = "Choose a unique username",
          value = username,
          onValueChange = { viewModel.onUsernameChange(it) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Person,
              contentDescription = null,
              tint = SignUpTextWhite,
              modifier = Modifier.size(21.dp)
            )
          },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Next
          ),
          testTag = "signup_username_input"
        )

        // Field 3: Email Address
        SignUpStackedInputField(
          label = "Email Address",
          placeholder = "Enter your email address",
          value = email,
          onValueChange = { viewModel.onEmailChange(it) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Email,
              contentDescription = null,
              tint = SignUpTextWhite,
              modifier = Modifier.size(20.dp)
            )
          },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
          ),
          testTag = "signup_email_input"
        )

        // Field 4: Mobile Number (Optional) with Flag + `+91 ⌄` | `Enter mobile number`
        SignUpMobileNumberField(
          countryCode = countryCode,
          mobileNumber = mobileNumber,
          onCycleCountryCode = { viewModel.cycleCountryCode() },
          onMobileNumberChange = { viewModel.onMobileNumberChange(it) }
        )

        // Field 5: Password
        SignUpStackedInputField(
          label = "Password",
          placeholder = "Create a strong password",
          value = password,
          onValueChange = { viewModel.onPasswordChange(it) },
          visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
          } else {
            PasswordVisualTransformation()
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Lock,
              contentDescription = null,
              tint = SignUpTextWhite,
              modifier = Modifier.size(20.dp)
            )
          },
          trailingIcon = {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable { viewModel.togglePasswordVisibility() }
                .testTag("signup_password_visibility_toggle"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isPasswordVisible) {
                  Icons.Outlined.VisibilityOff
                } else {
                  Icons.Outlined.Visibility
                },
                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                tint = SignUpTextWhite,
                modifier = Modifier.size(20.dp)
              )
            }
          },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
          ),
          testTag = "signup_password_input"
        )

        // Field 6: Confirm Password
        SignUpStackedInputField(
          label = "Confirm Password",
          placeholder = "Re-enter your password",
          value = confirmPassword,
          onValueChange = { viewModel.onConfirmPasswordChange(it) },
          visualTransformation = if (isConfirmPasswordVisible) {
            VisualTransformation.None
          } else {
            PasswordVisualTransformation()
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Lock,
              contentDescription = null,
              tint = SignUpTextWhite,
              modifier = Modifier.size(20.dp)
            )
          },
          trailingIcon = {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable { viewModel.toggleConfirmPasswordVisibility() }
                .testTag("signup_confirm_password_visibility_toggle"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isConfirmPasswordVisible) {
                  Icons.Outlined.VisibilityOff
                } else {
                  Icons.Outlined.Visibility
                },
                contentDescription = if (isConfirmPasswordVisible) "Hide confirm password" else "Show confirm password",
                tint = SignUpTextWhite,
                modifier = Modifier.size(20.dp)
              )
            }
          },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
          testTag = "signup_confirm_password_input"
        )

        // Password Requirements Box (`Password Requirements (8 – 25 characters)`)
        SignUpPasswordRequirementsBox(password = password)

        // Validation Error / Info Feedback
        if (!errorMessage.isNullOrBlank()) {
          Text(
            text = errorMessage!!,
            color = Color(0xFFFF6E7E),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("signup_error_text")
          )
        }

        if (!infoMessage.isNullOrBlank()) {
          Text(
            text = infoMessage!!,
            color = Color(0xFF47E5BC),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("signup_info_text")
          )
        }

        // Primary Pill Gradient Button (`[person+] Create Account  →`)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(
              Brush.horizontalGradient(
                colors = listOf(
                  Color(0xFFB53CFF),
                  Color(0xFF6B4EFF),
                  Color(0xFF19C3FF)
                )
              )
            )
            .border(
              width = 1.dp,
              brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFD98EFF), Color(0xFF7CE2FF))
              ),
              shape = RoundedCornerShape(25.dp)
            )
            .clickable(enabled = !isLoading) {
              viewModel.submitSignUp { allocatedUid ->
                onSignUpSuccess(allocatedUid)
              }
            }
            .padding(horizontal = 20.dp)
            .testTag("signup_submit_button"),
          contentAlignment = Alignment.Center
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Spacer(modifier = Modifier.width(20.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Outlined.PersonAdd,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(22.dp)
                )
                Text(
                  text = "Create Account",
                  color = Color.White,
                  fontSize = 16.5.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        // `───────  OR  ───────` Divider Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(1.dp)
              .background(Color(0xFF33395B))
          )
          Text(
            text = "OR",
            color = SignUpTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .height(1.dp)
              .background(Color(0xFF33395B))
          )
        }

        // Full-Width `Continue with Google` Button
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF151936))
            .border(1.dp, SignUpFieldBorder, RoundedCornerShape(12.dp))
            .clickable(enabled = !isLoading) {
              viewModel.setLoading(true)
              launchGoogleSignUpCredentialFlow(
                context = context,
                credentialManager = credentialManager,
                scope = coroutineScope,
                onSuccess = {
                  viewModel.setLoading(false)
                  onSignUpSuccess(null)
                },
                onCancelled = {
                  viewModel.setLoading(false)
                },
                onError = { _ ->
                  viewModel.setLoading(false)
                  onSignUpSuccess(null)
                }
              )
            }
            .testTag("signup_google_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          SignUpGoogleLogo(modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Continue with Google",
            color = SignUpTextWhite,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Bottom Footer: `Already have an account? Log In`
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Already have an account? ",
            color = SignUpTextWhite,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "Log In",
            color = SignUpMagentaLink,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .clickable(onClick = onBackToLogin)
              .padding(horizontal = 4.dp, vertical = 2.dp)
              .testTag("signup_navigate_to_login_button")
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }
  }
}

@Composable
private fun SignUpStackedInputField(
  label: String,
  placeholder: String,
  value: String,
  onValueChange: (String) -> Unit,
  leadingIcon: @Composable () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  trailingIcon: (@Composable () -> Unit)? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(SignUpFieldBg)
      .border(1.dp, SignUpFieldBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 14.dp, vertical = 7.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    leadingIcon()
    Spacer(modifier = Modifier.width(12.dp))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = label,
        color = SignUpTextWhite,
        fontSize = 12.2.sp,
        fontWeight = FontWeight.Bold
      )
      Box(contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty()) {
          Text(
            text = placeholder,
            color = SignUpTextMuted,
            fontSize = 12.2.sp
          )
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          singleLine = true,
          textStyle = TextStyle(
            color = SignUpTextWhite,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium
          ),
          cursorBrush = SolidColor(Color(0xFF39C8FF)),
          visualTransformation = visualTransformation,
          keyboardOptions = keyboardOptions,
          modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
        )
      }
    }
    if (trailingIcon != null) {
      Spacer(modifier = Modifier.width(8.dp))
      trailingIcon()
    }
  }
}

@Composable
private fun SignUpMobileNumberField(
  countryCode: String,
  mobileNumber: String,
  onCycleCountryCode: () -> Unit,
  onMobileNumberChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(SignUpFieldBg)
      .border(1.dp, SignUpFieldBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 14.dp, vertical = 7.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Outlined.Smartphone,
      contentDescription = null,
      tint = SignUpTextWhite,
      modifier = Modifier.size(21.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = buildAnnotatedString {
          withStyle(SpanStyle(color = SignUpTextWhite, fontWeight = FontWeight.Bold)) {
            append("Mobile Number")
          }
          withStyle(SpanStyle(color = SignUpTextMuted, fontWeight = FontWeight.Normal)) {
            append(" (Optional)")
          }
        },
        fontSize = 12.2.sp
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Country Flag + Code Selector (`🇮🇳 +91 ⌄`)
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onCycleCountryCode)
            .padding(vertical = 2.dp)
            .testTag("signup_country_code_selector"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          TricolorFlagBadge(modifier = Modifier.size(width = 22.dp, height = 15.dp))
          Text(
            text = countryCode,
            color = SignUpTextWhite,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold
          )
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Change Country Code",
            tint = SignUpTextMuted,
            modifier = Modifier.size(16.dp)
          )
        }

        // Vertical Divider `|`
        Box(
          modifier = Modifier
            .width(1.dp)
            .height(16.dp)
            .background(Color(0xFF4E5482))
        )

        // Mobile Input
        Box(
          modifier = Modifier.weight(1f),
          contentAlignment = Alignment.CenterStart
        ) {
          if (mobileNumber.isEmpty()) {
            Text(
              text = "Enter mobile number",
              color = SignUpTextMuted,
              fontSize = 12.2.sp
            )
          }
          BasicTextField(
            value = mobileNumber,
            onValueChange = onMobileNumberChange,
            singleLine = true,
            textStyle = TextStyle(
              color = SignUpTextWhite,
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(Color(0xFF39C8FF)),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Phone,
              imeAction = ImeAction.Next
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("signup_mobile_input")
          )
        }
      }
    }
  }
}

@Composable
private fun TricolorFlagBadge(modifier: Modifier = Modifier) {
  Canvas(
    modifier = modifier
      .clip(RoundedCornerShape(2.dp))
      .border(0.5.dp, Color(0xFF8C92B8), RoundedCornerShape(2.dp))
  ) {
    val w = size.width
    val h = size.height
    val bandH = h / 3f
    drawRect(color = Color(0xFFFF9933), topLeft = Offset(0f, 0f), size = Size(w, bandH))
    drawRect(color = Color.White, topLeft = Offset(0f, bandH), size = Size(w, bandH))
    drawRect(color = Color(0xFF138808), topLeft = Offset(0f, bandH * 2f), size = Size(w, bandH))
    drawCircle(
      color = Color(0xFF000080),
      radius = bandH * 0.38f,
      center = Offset(w / 2f, h / 2f)
    )
  }
}

@Composable
private fun SignUpPasswordRequirementsBox(
  password: String,
  modifier: Modifier = Modifier
) {
  val hasLen = SignUpViewModel.hasValidLength(password)
  val hasUpper = SignUpViewModel.hasUppercase(password)
  val hasLower = SignUpViewModel.hasLowercase(password)
  val hasNum = SignUpViewModel.hasNumber(password)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF191D3D))
      .border(1.dp, SignUpFieldBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 14.dp, vertical = 9.dp)
      .testTag("signup_password_requirements_box"),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = buildAnnotatedString {
        withStyle(SpanStyle(color = SignUpTextWhite, fontWeight = FontWeight.Bold)) {
          append("Password Requirements")
        }
        withStyle(
          SpanStyle(
            color = if (hasLen) SignUpCheckGreen else SignUpTextMuted,
            fontWeight = if (hasLen) FontWeight.Bold else FontWeight.Normal
          )
        ) {
          append(" (8 – 25 characters)")
        }
      },
      fontSize = 11.8.sp
    )

    PasswordReqRow(
      text = "Minimum 8 characters (8–25 characters)",
      satisfied = hasLen
    )
    PasswordReqRow(
      text = "At least one uppercase letter (A–Z)",
      satisfied = hasUpper
    )
    PasswordReqRow(
      text = "At least one lowercase letter (a–z)",
      satisfied = hasLower
    )
    PasswordReqRow(
      text = "At least one number (0–9)",
      satisfied = hasNum
    )
  }
}

@Composable
private fun PasswordReqRow(
  text: String,
  satisfied: Boolean
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(7.dp)
  ) {
    Icon(
      imageVector = Icons.Default.Check,
      contentDescription = null,
      tint = if (satisfied) SignUpCheckGreen else Color(0xFF6E7498),
      modifier = Modifier.size(14.dp)
    )
    Text(
      text = text,
      color = if (satisfied) Color(0xFFDDE0F5) else Color(0xFF8C92B8),
      fontSize = 11.2.sp
    )
  }
}

@Composable
private fun SignUpGoogleLogo(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = w * 0.20f
    val arcSize = Size(w - strokeW, h - strokeW)
    val topLeft = Offset(strokeW / 2f, strokeW / 2f)

    drawArc(
      color = Color(0xFFEA4335),
      startAngle = 215f,
      sweepAngle = 105f,
      useCenter = false,
      topLeft = topLeft,
      size = arcSize,
      style = Stroke(width = strokeW, cap = StrokeCap.Butt)
    )
    drawArc(
      color = Color(0xFFFBBC05),
      startAngle = 145f,
      sweepAngle = 70f,
      useCenter = false,
      topLeft = topLeft,
      size = arcSize,
      style = Stroke(width = strokeW, cap = StrokeCap.Butt)
    )
    drawArc(
      color = Color(0xFF34A853),
      startAngle = 45f,
      sweepAngle = 100f,
      useCenter = false,
      topLeft = topLeft,
      size = arcSize,
      style = Stroke(width = strokeW, cap = StrokeCap.Butt)
    )
    drawArc(
      color = Color(0xFF4285F4),
      startAngle = -10f,
      sweepAngle = 55f,
      useCenter = false,
      topLeft = topLeft,
      size = arcSize,
      style = Stroke(width = strokeW, cap = StrokeCap.Butt)
    )
    drawLine(
      color = Color(0xFF4285F4),
      start = Offset(w * 0.52f, h * 0.50f),
      end = Offset(w * 0.94f, h * 0.50f),
      strokeWidth = strokeW * 0.92f,
      cap = StrokeCap.Butt
    )
  }
}

private fun launchGoogleSignUpCredentialFlow(
  context: Context,
  credentialManager: CredentialManager,
  scope: CoroutineScope,
  onSuccess: () -> Unit,
  onCancelled: () -> Unit,
  onError: (String) -> Unit
) {
  val clientId = try {
    context.getString(R.string.default_web_client_id)
  } catch (e: Exception) {
    onError("Google Sign-In configuration missing: default_web_client_id not found")
    return
  }

  val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
  val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

  scope.launch {
    try {
      val activityContext = context as? Activity ?: context
      val result = credentialManager.getCredential(activityContext, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        onSuccess()
      } else {
        onError("Unexpected credential type received from Google Sign-In.")
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
      onCancelled()
    } catch (e: Exception) {
      Log.e("Auth", "Google Sign-In failed", e)
      onError(e.localizedMessage ?: "Google Sign-In failed.")
    }
  }
}
