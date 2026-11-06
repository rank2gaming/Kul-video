package com.cineflow.ai.feature.auth

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R

private val ProfileSetupBgDeep = Color(0xFF09071A)
private val ProfileSetupCardSurface = Color(0xFF10132E)
private val ProfileSetupFieldBg = Color(0xFF171B3D)
private val ProfileSetupFieldBorder = Color(0xFF535B8E)
private val ProfileSetupTextWhite = Color(0xFFFFFFFF)
private val ProfileSetupTextMuted = Color(0xFFB5B9D8)
private val ProfileSetupAsteriskRed = Color(0xFFFF4D6D)
private val ProfileSetupCheckGreen = Color(0xFF2EDB58)
private val ProfileSetupInactiveTick = Color(0xFF5D648F)

/**
 * Profile Completion Screen (`CineFlow AI Profile Setup.png` & Master Prompt Section 10).
 *
 * Shown exclusively when a user signs in with Google, gating access to the Home Dashboard
 * until the user completes their profile and taps `Complete Profile`.
 */
@Composable
fun ProfileCompletionScreen(
  viewModel: ProfileCompletionViewModel,
  onBack: () -> Unit,
  onProfileCompleted: (publicUid: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val username by viewModel.username.collectAsStateWithLifecycle()
  val isUsernameValidAndAvailable by viewModel.isUsernameValidAndAvailable.collectAsStateWithLifecycle()
  val countryCode by viewModel.countryCode.collectAsStateWithLifecycle()
  val mobileNumber by viewModel.mobileNumber.collectAsStateWithLifecycle()
  val isMobileNumberValid by viewModel.isMobileNumberValid.collectAsStateWithLifecycle()
  val passwordRequired by viewModel.passwordRequired.collectAsStateWithLifecycle()
  val password by viewModel.password.collectAsStateWithLifecycle()
  val confirmPassword by viewModel.confirmPassword.collectAsStateWithLifecycle()
  val hasValidPasswordLength by viewModel.hasValidPasswordLength.collectAsStateWithLifecycle()
  val hasUppercase by viewModel.hasUppercase.collectAsStateWithLifecycle()
  val hasLowercase by viewModel.hasLowercase.collectAsStateWithLifecycle()
  val hasNumber by viewModel.hasNumber.collectAsStateWithLifecycle()
  val isPasswordStrong by viewModel.isPasswordStrong.collectAsStateWithLifecycle()
  val isConfirmPasswordMatching by viewModel.isConfirmPasswordMatching.collectAsStateWithLifecycle()
  val isPasswordVisible by viewModel.isPasswordVisible.collectAsStateWithLifecycle()
  val isConfirmPasswordVisible by viewModel.isConfirmPasswordVisible.collectAsStateWithLifecycle()
  val selectedAvatarUri by viewModel.selectedAvatarUri.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
  val infoMessage by viewModel.infoMessage.collectAsStateWithLifecycle()

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      viewModel.onAvatarSelected(uri.toString())
    }
  }

  BackHandler {
    onBack()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(ProfileSetupBgDeep)
      .testTag("profile_completion_screen"),
    containerColor = ProfileSetupBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(ProfileSetupBgDeep),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Hero Header + Overlapping Avatar Circle & Camera Badge (`img_profile_setup_top_hero.png`)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(941f / 505f)
          .testTag("profile_setup_top_hero")
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_profile_setup_top_hero),
          contentDescription = "Complete Your Profile — Set up your account to continue",
          contentScale = ContentScale.FillWidth,
          modifier = Modifier.fillMaxSize()
        )

        // Top-left Back arrow hotspot (`←`)
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 10.dp, top = 6.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onBack)
            .testTag("profile_setup_back_button")
        )

        // Interactive Avatar Circle + Camera Badge hotspot (`profile_setup_avatar_picker_button`)
        Box(
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 48.dp)
            .size(96.dp)
            .clip(CircleShape)
            .clickable {
              runCatching {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              }.onFailure {
                viewModel.onAvatarSelected("avatar://creator_default")
              }
            }
            .testTag("profile_setup_avatar_picker_button"),
          contentAlignment = Alignment.BottomEnd
        ) {
          if (selectedAvatarUri != null) {
            Box(
              modifier = Modifier
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(ProfileSetupCheckGreen),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Avatar Selected",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }

      // 2. Main Neon-Bordered Card Body (`profile_setup_main_card`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
          .background(ProfileSetupCardSurface)
          .border(
            width = 1.6.dp,
            brush = Brush.verticalGradient(
              colors = listOf(
                Color(0xFF8338FF),
                Color(0xFF4F5BFF),
                Color(0xFF00D4FF),
                Color(0xFFD629FF)
              )
            ),
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
          )
          .padding(horizontal = 16.dp, vertical = 12.dp)
          .testTag("profile_setup_main_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Field 1: Username * (with live green circle checkmark badge ONLY when valid & unique)
        ProfileUsernameFieldBox(
          username = username,
          isValidAndAvailable = isUsernameValidAndAvailable,
          onUsernameChange = { viewModel.onUsernameChange(it) }
        )

        // Field 2: Mobile Number (Optional) with Country Flag + Code selector & live validation
        ProfileMobileNumberFieldBox(
          countryCode = countryCode,
          mobileNumber = mobileNumber,
          isMobileValid = isMobileNumberValid,
          onCycleCountryCode = { viewModel.cycleCountryCode() },
          onSelectCountryCode = { viewModel.selectCountryCode(it) },
          onMobileChange = { viewModel.onMobileNumberChange(it) }
        )

        // Section 10: Password & Confirm Password + Live Password Requirements Card
        if (passwordRequired) {
          // Field 3: Set Password *
          ProfilePasswordFieldBox(
            label = "Set Password",
            placeholder = "Create a strong password",
            value = password,
            isVisible = isPasswordVisible,
            isValid = isPasswordStrong,
            hasMismatchError = false,
            onValueChange = { viewModel.onPasswordChange(it) },
            onToggleVisibility = { viewModel.togglePasswordVisibility() },
            inputTestTag = "profile_setup_password_input",
            toggleTestTag = "profile_setup_password_visibility_toggle",
            validBadgeTestTag = "profile_setup_password_valid_badge",
            imeAction = ImeAction.Next
          )

          // Field 4: Confirm Password *
          ProfilePasswordFieldBox(
            label = "Confirm Password",
            placeholder = "Re-enter your password",
            value = confirmPassword,
            isVisible = isConfirmPasswordVisible,
            isValid = isConfirmPasswordMatching,
            hasMismatchError = confirmPassword.isNotEmpty() && confirmPassword != password,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            onToggleVisibility = { viewModel.toggleConfirmPasswordVisibility() },
            inputTestTag = "profile_setup_confirm_password_input",
            toggleTestTag = "profile_setup_confirm_password_visibility_toggle",
            validBadgeTestTag = "profile_setup_confirm_password_valid_badge",
            imeAction = ImeAction.Done
          )

          // Field 5: Password Requirements (8 – 25 characters) Box with live green ticks
          ProfilePasswordRequirementsCard(
            hasValidLength = hasValidPasswordLength,
            hasUppercase = hasUppercase,
            hasLowercase = hasLowercase,
            hasNumber = hasNumber,
            isPasswordStrong = isPasswordStrong
          )
        }

        // Validation Error / Info Feedback Banner
        if (!errorMessage.isNullOrBlank()) {
          Text(
            text = errorMessage!!,
            color = Color(0xFFFF6B7A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_setup_error_text")
          )
        } else if (!infoMessage.isNullOrBlank()) {
          Text(
            text = infoMessage!!,
            color = Color(0xFF47E5BC),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_setup_info_text")
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Primary CTA Button (`✓ Complete Profile`)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(
              Brush.horizontalGradient(
                colors = listOf(
                  Color(0xFFC83BFF),
                  Color(0xFF6D58FF),
                  Color(0xFF05D8FF)
                )
              )
            )
            .border(
              width = 1.dp,
              brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFE78CFF), Color(0xFF7BEBFF))
              ),
              shape = RoundedCornerShape(27.dp)
            )
            .clickable {
              viewModel.submitProfileCompletion { allocatedPublicUid ->
                onProfileCompleted(allocatedPublicUid)
              }
            }
            .padding(horizontal = 20.dp)
            .testTag("profile_setup_submit_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
            Text(
              text = "Complete Profile",
              color = Color.White,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
      }

      Spacer(modifier = Modifier.height(18.dp))
    }
  }
}

@Composable
private fun ProfileUsernameFieldBox(
  username: String,
  isValidAndAvailable: Boolean,
  onUsernameChange: (String) -> Unit
) {
  val borderColor = when {
    isValidAndAvailable -> ProfileSetupCheckGreen.copy(alpha = 0.75f)
    username.isNotEmpty() -> ProfileSetupAsteriskRed.copy(alpha = 0.75f)
    else -> ProfileSetupFieldBorder
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(ProfileSetupFieldBg)
      .border(1.dp, borderColor, RoundedCornerShape(14.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Outlined.Person,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(24.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = ProfileSetupTextWhite,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          ) {
            append("Username ")
          }
          withStyle(
            SpanStyle(
              color = ProfileSetupAsteriskRed,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          ) {
            append("*")
          }
        }
      )
      Box(contentAlignment = Alignment.CenterStart) {
        if (username.isEmpty()) {
          Text(
            text = "Create a unique username",
            color = ProfileSetupTextMuted,
            fontSize = 13.sp
          )
        }
        BasicTextField(
          value = username,
          onValueChange = onUsernameChange,
          singleLine = true,
          textStyle = TextStyle(
            color = ProfileSetupTextWhite,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium
          ),
          cursorBrush = SolidColor(Color(0xFF00E5FF)),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Next
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_setup_username_input")
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Green Circle Checkmark Badge on the right of Username field:
    // Turns bright green ONLY when username is non-empty, valid (3-24 chars), and available!
    val badgeBg = if (isValidAndAvailable) {
      ProfileSetupCheckGreen
    } else {
      Color(0xFF3E4570)
    }
    val checkTint = if (isValidAndAvailable) {
      Color.White
    } else {
      Color(0xFF8B92BA)
    }
    Box(
      modifier = Modifier
        .size(22.dp)
        .clip(CircleShape)
        .background(badgeBg)
        .semantics { selected = isValidAndAvailable }
        .testTag("profile_setup_username_valid_badge"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.Check,
        contentDescription = if (isValidAndAvailable) "Username Valid" else "Username Incomplete",
        tint = checkTint,
        modifier = Modifier.size(14.dp)
      )
    }
  }
}

@Composable
private fun ProfileMobileNumberFieldBox(
  countryCode: String,
  mobileNumber: String,
  isMobileValid: Boolean,
  onCycleCountryCode: () -> Unit,
  onSelectCountryCode: (String) -> Unit,
  onMobileChange: (String) -> Unit
) {
  var showCountryMenu by remember { mutableStateOf(false) }
  val borderColor = when {
    isMobileValid -> ProfileSetupCheckGreen.copy(alpha = 0.75f)
    mobileNumber.isNotEmpty() -> ProfileSetupAsteriskRed.copy(alpha = 0.7f)
    else -> ProfileSetupFieldBorder
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(ProfileSetupFieldBg)
      .border(1.dp, borderColor, RoundedCornerShape(14.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Outlined.Smartphone,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(24.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = ProfileSetupTextWhite,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          ) {
            append("Mobile Number ")
          }
          withStyle(
            SpanStyle(
              color = ProfileSetupTextMuted,
              fontWeight = FontWeight.Normal,
              fontSize = 13.5.sp
            )
          ) {
            append("(Optional)")
          }
        }
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Country selector (`[Flag] +91 v`)
        Box {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .clickable {
                onCycleCountryCode()
              }
              .padding(end = 6.dp, top = 2.dp, bottom = 2.dp)
              .testTag("profile_setup_country_code_selector")
          ) {
            CountryFlagBadgeIcon(
              countryCode = countryCode,
              modifier = Modifier.size(width = 24.dp, height = 16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = countryCode,
              color = ProfileSetupTextWhite,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
              imageVector = Icons.Filled.KeyboardArrowDown,
              contentDescription = "Select Country Code",
              tint = ProfileSetupTextMuted,
              modifier = Modifier
                .size(18.dp)
                .clickable { showCountryMenu = true }
            )
          }

          DropdownMenu(
            expanded = showCountryMenu,
            onDismissRequest = { showCountryMenu = false },
            modifier = Modifier.background(Color(0xFF1A1F47))
          ) {
            ProfileCompletionViewModel.SUPPORTED_COUNTRY_CODES.forEach { code ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = when (code) {
                      "+91" -> "🇮🇳 India ($code)"
                      "+880" -> "🇧🇩 Bangladesh ($code)"
                      "+1" -> "🇺🇸 USA / Canada ($code)"
                      "+44" -> "🇬🇧 United Kingdom ($code)"
                      else -> code
                    },
                    color = Color.White,
                    fontSize = 13.sp
                  )
                },
                onClick = {
                  onSelectCountryCode(code)
                  showCountryMenu = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier = Modifier
            .width(1.dp)
            .height(18.dp)
            .background(Color(0xFF4D5482))
        )
        Spacer(modifier = Modifier.width(10.dp))

        Box(
          modifier = Modifier.weight(1f),
          contentAlignment = Alignment.CenterStart
        ) {
          if (mobileNumber.isEmpty()) {
            Text(
              text = "Enter mobile number",
              color = ProfileSetupTextMuted,
              fontSize = 13.sp
            )
          }
          BasicTextField(
            value = mobileNumber,
            onValueChange = onMobileChange,
            singleLine = true,
            textStyle = TextStyle(
              color = ProfileSetupTextWhite,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(Color(0xFF00E5FF)),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.NumberPassword,
              imeAction = ImeAction.Next
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_setup_mobile_input")
          )
        }

        if (mobileNumber.isNotEmpty()) {
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .size(20.dp)
              .clip(CircleShape)
              .background(if (isMobileValid) ProfileSetupCheckGreen else Color(0xFF3E4570))
              .semantics { selected = isMobileValid }
              .testTag("profile_setup_mobile_valid_badge"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = if (isMobileValid) "Mobile Number Valid" else "Mobile Number Incomplete",
              tint = if (isMobileValid) Color.White else Color(0xFF8B92BA),
              modifier = Modifier.size(13.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ProfilePasswordFieldBox(
  label: String,
  placeholder: String,
  value: String,
  isVisible: Boolean,
  isValid: Boolean,
  hasMismatchError: Boolean,
  onValueChange: (String) -> Unit,
  onToggleVisibility: () -> Unit,
  inputTestTag: String,
  toggleTestTag: String,
  validBadgeTestTag: String,
  imeAction: ImeAction
) {
  val borderColor = when {
    isValid -> ProfileSetupCheckGreen.copy(alpha = 0.75f)
    hasMismatchError -> ProfileSetupAsteriskRed.copy(alpha = 0.85f)
    else -> ProfileSetupFieldBorder
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(ProfileSetupFieldBg)
      .border(1.dp, borderColor, RoundedCornerShape(14.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Outlined.Lock,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(24.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = ProfileSetupTextWhite,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          ) {
            append("$label ")
          }
          withStyle(
            SpanStyle(
              color = ProfileSetupAsteriskRed,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          ) {
            append("*")
          }
        }
      )
      Box(contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty()) {
          Text(
            text = placeholder,
            color = ProfileSetupTextMuted,
            fontSize = 13.sp
          )
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          singleLine = true,
          visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
          textStyle = TextStyle(
            color = ProfileSetupTextWhite,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium
          ),
          cursorBrush = SolidColor(Color(0xFF00E5FF)),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag(inputTestTag)
        )
      }
    }

    if (isValid) {
      Spacer(modifier = Modifier.width(6.dp))
      Box(
        modifier = Modifier
          .size(20.dp)
          .clip(CircleShape)
          .background(ProfileSetupCheckGreen)
          .semantics { selected = true }
          .testTag(validBadgeTestTag),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.Check,
          contentDescription = "$label Valid",
          tint = Color.White,
          modifier = Modifier.size(13.dp)
        )
      }
    }

    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .clickable(onClick = onToggleVisibility)
        .testTag(toggleTestTag),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
        contentDescription = if (isVisible) "Hide Password" else "Show Password",
        tint = Color.White,
        modifier = Modifier.size(22.dp)
      )
    }
  }
}

@Composable
private fun ProfilePasswordRequirementsCard(
  hasValidLength: Boolean,
  hasUppercase: Boolean,
  hasLowercase: Boolean,
  hasNumber: Boolean,
  isPasswordStrong: Boolean
) {
  val cardBorderColor = if (isPasswordStrong) {
    ProfileSetupCheckGreen.copy(alpha = 0.7f)
  } else {
    ProfileSetupFieldBorder
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(ProfileSetupFieldBg)
      .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp))
      .padding(horizontal = 14.dp, vertical = 11.dp)
      .testTag("profile_setup_password_requirements_box"),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ShieldCheckVectorIcon(
        tintColor = if (isPasswordStrong) ProfileSetupCheckGreen else Color.White,
        modifier = Modifier.size(19.dp)
      )
      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = ProfileSetupTextWhite,
              fontWeight = FontWeight.Bold,
              fontSize = 13.2.sp
            )
          ) {
            append("Password Requirements ")
          }
          withStyle(
            SpanStyle(
              color = if (hasValidLength) ProfileSetupCheckGreen else ProfileSetupTextMuted,
              fontWeight = if (hasValidLength) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp
            )
          ) {
            append("(8 – 25 characters)")
          }
        }
      )
    }

    ProfileRequirementRow(
      label = "Minimum 8 characters (8–25 characters)",
      satisfied = hasValidLength,
      testTag = "profile_setup_req_length"
    )
    ProfileRequirementRow(
      label = "At least one uppercase letter (A–Z)",
      satisfied = hasUppercase,
      testTag = "profile_setup_req_uppercase"
    )
    ProfileRequirementRow(
      label = "At least one lowercase letter (a–z)",
      satisfied = hasLowercase,
      testTag = "profile_setup_req_lowercase"
    )
    ProfileRequirementRow(
      label = "At least one number (0–9)",
      satisfied = hasNumber,
      testTag = "profile_setup_req_number"
    )
  }
}

@Composable
private fun ProfileRequirementRow(
  label: String,
  satisfied: Boolean,
  testTag: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .semantics { selected = satisfied }
      .testTag(testTag)
  ) {
    Icon(
      imageVector = Icons.Filled.Check,
      contentDescription = if (satisfied) "$label satisfied" else "$label not satisfied",
      tint = if (satisfied) ProfileSetupCheckGreen else ProfileSetupInactiveTick,
      modifier = Modifier.size(16.dp)
    )
    Text(
      text = label,
      color = if (satisfied) ProfileSetupTextWhite else ProfileSetupTextMuted,
      fontSize = 12.2.sp,
      fontWeight = if (satisfied) FontWeight.SemiBold else FontWeight.Normal
    )
  }
}

@Composable
private fun ShieldCheckVectorIcon(
  tintColor: Color = Color.White,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val shieldPath = Path().apply {
      moveTo(w * 0.50f, h * 0.08f)
      lineTo(w * 0.88f, h * 0.24f)
      lineTo(w * 0.88f, h * 0.54f)
      cubicTo(
        w * 0.88f, h * 0.76f,
        w * 0.68f, h * 0.90f,
        w * 0.50f, h * 0.95f
      )
      cubicTo(
        w * 0.32f, h * 0.90f,
        w * 0.12f, h * 0.76f,
        w * 0.12f, h * 0.54f
      )
      lineTo(w * 0.12f, h * 0.24f)
      close()
    }
    drawPath(
      path = shieldPath,
      color = tintColor,
      style = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    val checkPath = Path().apply {
      moveTo(w * 0.34f, h * 0.52f)
      lineTo(w * 0.46f, h * 0.64f)
      lineTo(w * 0.68f, h * 0.40f)
    }
    drawPath(
      path = checkPath,
      color = tintColor,
      style = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
  }
}

@Composable
private fun CountryFlagBadgeIcon(
  countryCode: String,
  modifier: Modifier = Modifier
) {
  Canvas(
    modifier = modifier
      .clip(RoundedCornerShape(3.dp))
      .border(0.6.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
  ) {
    when (countryCode) {
      "+880" -> {
        // Bangladesh flag: dark green field with red circle
        drawRect(color = Color(0xFF006A4E), size = size)
        drawCircle(
          color = Color(0xFFF42A41),
          radius = size.height * 0.32f,
          center = Offset(size.width * 0.46f, size.height / 2f)
        )
      }
      "+1" -> {
        // USA flag stripes + blue canton
        val stripeH = size.height / 5f
        for (i in 0 until 5) {
          drawRect(
            color = if (i % 2 == 0) Color(0xFFB22234) else Color.White,
            topLeft = Offset(0f, i * stripeH),
            size = Size(size.width, stripeH)
          )
        }
        drawRect(
          color = Color(0xFF3C3B6E),
          topLeft = Offset(0f, 0f),
          size = Size(size.width * 0.45f, size.height * 0.55f)
        )
      }
      "+44" -> {
        // UK flag blue background + white/red cross
        drawRect(color = Color(0xFF012169), size = size)
        drawRect(
          color = Color.White,
          topLeft = Offset(0f, size.height * 0.35f),
          size = Size(size.width, size.height * 0.30f)
        )
        drawRect(
          color = Color.White,
          topLeft = Offset(size.width * 0.40f, 0f),
          size = Size(size.width * 0.20f, size.height)
        )
        drawRect(
          color = Color(0xFFC8102E),
          topLeft = Offset(0f, size.height * 0.41f),
          size = Size(size.width, size.height * 0.18f)
        )
        drawRect(
          color = Color(0xFFC8102E),
          topLeft = Offset(size.width * 0.44f, 0f),
          size = Size(size.width * 0.12f, size.height)
        )
      }
      else -> {
        // Default +91 India tricolor flag
        val stripeHeight = size.height / 3f
        drawRect(
          color = Color(0xFFFF9933),
          topLeft = Offset(0f, 0f),
          size = Size(size.width, stripeHeight)
        )
        drawRect(
          color = Color.White,
          topLeft = Offset(0f, stripeHeight),
          size = Size(size.width, stripeHeight)
        )
        drawRect(
          color = Color(0xFF138808),
          topLeft = Offset(0f, stripeHeight * 2f),
          size = Size(size.width, stripeHeight)
        )
        drawCircle(
          color = Color(0xFF000080),
          radius = stripeHeight * 0.38f,
          center = Offset(size.width / 2f, size.height / 2f),
          style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        )
      }
    }
  }
}
