package com.cineflow.ai.feature.auth

import android.app.Activity
import android.content.Context
import android.util.Log
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

private val LoginBgObsidian = Color(0xFF070818)
private val LoginCardFill = Color(0xE80D1126)
private val LoginInputFill = Color(0xFF121830)
private val LoginInputBorder = Color(0xFF364366)
private val LoginTextWhite = Color(0xFFFFFFFF)
private val LoginTextMuted = Color(0xFFB0B5D4)
private val LoginPurpleAccent = Color(0xFF9E47FF)
private val LoginSocialBtnFill = Color(0xFF12172B)
private val LoginSocialBtnBorder = Color(0xFF384160)

/**
 * Login Screen (`02_Login_Screen.png` & Master Prompt Section 3.E, Section 9 & Section 10).
 *
 * Implements:
 * 1. Top Hero Illustration (`bg_login_hero.png`) with 3D purple Play Logo, `CineFlow AI` brand identity,
 *    `AI Powered Video Editor`, `Create ✦ Edit ✦ Inspire`, glowing 3D filmstrip, and cinema camera.
 * 2. Top-Right `Skip` action (`login_skip_button`) entering Home Dashboard in unsigned local-only mode
 *    without creating any Firebase Auth user, anonymous account, or server-side account (Section 3.E & 9).
 * 3. Glassmorphic `Welcome Back` Card (`Sign in to continue to CineFlow AI`).
 * 4. `Email address` field (`login_email_input`) and `Password` field (`login_password_input`) with
 *    visibility toggle (`login_password_visibility_toggle`) and Section 10 password validation.
 * 5. `Forgot password?` action (`login_forgot_password_button`).
 * 6. Full-width purple-to-cyan gradient `Log In  →` button (`login_submit_button`).
 * 7. `OR` divider + `Continue with Google` (`login_google_button`, using Jetpack `CredentialManager`
 *    `GetSignInWithGoogleOption`) and `Continue with Apple` (`login_apple_button`).
 */
@Composable
fun LoginScreen(
  viewModel: LoginViewModel,
  onLoginSuccess: () -> Unit,
  onSkipToHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val email by viewModel.email.collectAsStateWithLifecycle()
  val password by viewModel.password.collectAsStateWithLifecycle()
  val isPasswordVisible by viewModel.isPasswordVisible.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
  val infoMessage by viewModel.infoMessage.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(LoginBgObsidian)
      .testTag("login_screen"),
    containerColor = LoginBgObsidian,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(LoginBgObsidian),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Hero Artwork Section + Top-Right Skip Action (Section 3.E & Section 9)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(340.dp)
          .testTag("login_hero_banner")
      ) {
        Image(
          painter = painterResource(id = R.drawable.bg_login_hero),
          contentDescription = "CineFlow AI Hero Banner",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Bottom gradient vignette blending hero artwork seamlessly into the login card area
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(64.dp)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, LoginBgObsidian)
              )
            )
        )

        // Skip Button (Section 3.E & Section 9: Opens Home Dashboard in unsigned local-only mode)
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 12.dp, end = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x99151A36))
            .border(1.dp, Color(0xFF475384), RoundedCornerShape(18.dp))
            .clickable(onClick = onSkipToHome)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("login_skip_button"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Skip",
            color = LoginTextWhite,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      // 2. Glassmorphic Welcome Back Card (`02_Login_Screen.png`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(LoginCardFill)
          .border(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
              colors = listOf(
                Color(0xFF4F88FF),
                Color(0xFF7B42FF),
                Color(0xFF5634B8)
              )
            ),
            shape = RoundedCornerShape(24.dp)
          )
          .padding(horizontal = 20.dp, vertical = 22.dp)
          .testTag("login_card_container"),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Welcome Back",
          color = LoginTextWhite,
          fontSize = 24.sp,
          fontWeight = FontWeight.ExtraBold,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("login_title_text")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Sign in to continue to CineFlow AI",
          color = LoginTextMuted,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("login_subtitle_text")
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Email Address Input Field
        LoginInputField(
          value = email,
          onValueChange = { viewModel.onEmailChange(it) },
          placeholder = "Email address",
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Email,
              contentDescription = null,
              tint = Color(0xFFD6DAEE),
              modifier = Modifier.size(20.dp)
            )
          },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
          ),
          testTag = "login_email_input"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password Input Field
        LoginInputField(
          value = password,
          onValueChange = { viewModel.onPasswordChange(it) },
          placeholder = "Password",
          visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
          } else {
            PasswordVisualTransformation()
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Lock,
              contentDescription = null,
              tint = Color(0xFFD6DAEE),
              modifier = Modifier.size(20.dp)
            )
          },
          trailingIcon = {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .clickable { viewModel.togglePasswordVisibility() }
                .testTag("login_password_visibility_toggle"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isPasswordVisible) {
                  Icons.Outlined.VisibilityOff
                } else {
                  Icons.Outlined.Visibility
                },
                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                tint = Color(0xFFD6DAEE),
                modifier = Modifier.size(20.dp)
              )
            }
          },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              viewModel.submitLogin(onSuccess = onLoginSuccess)
            }
          ),
          testTag = "login_password_input"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Forgot Password Link (Right-Aligned)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Text(
            text = "Forgot password?",
            color = LoginPurpleAccent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .clickable { viewModel.handleForgotPasswordClick() }
              .padding(horizontal = 4.dp, vertical = 2.dp)
              .testTag("login_forgot_password_button")
          )
        }

        // Validation Error / Info Feedback
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
              .testTag("login_error_text")
          )
        }

        if (!infoMessage.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = infoMessage!!,
            color = Color(0xFF47E5BC),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_info_text")
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Primary Gradient `Log In  →` Button
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
              Brush.horizontalGradient(
                colors = listOf(
                  Color(0xFF9538FF),
                  Color(0xFF5354FF),
                  Color(0xFF17AAFF)
                )
              )
            )
            .clickable(enabled = !isLoading) {
              viewModel.submitLogin(onSuccess = onLoginSuccess)
            }
            .testTag("login_submit_button"),
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
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "Log In",
                color = Color.White,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold
              )
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(19.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

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
              .background(Color(0xFF2B3350))
          )
          Text(
            text = "OR",
            color = Color(0xFF9EA4C4),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .height(1.dp)
              .background(Color(0xFF2B3350))
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Social Sign-In Row (`Continue with Google` | `Continue with Apple`)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // 1. Continue with Google (Jetpack CredentialManager + GetSignInWithGoogleOption)
          Row(
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(LoginSocialBtnFill)
              .border(1.dp, LoginSocialBtnBorder, RoundedCornerShape(12.dp))
              .clickable(enabled = !isLoading) {
                viewModel.setLoading(true)
                launchGoogleCredentialSignIn(
                  context = context,
                  credentialManager = credentialManager,
                  scope = coroutineScope,
                  onSuccess = {
                    viewModel.setLoading(false)
                    onLoginSuccess()
                  },
                  onCancelled = {
                    viewModel.setLoading(false)
                  },
                  onError = { errMsg ->
                    viewModel.setAuthError(errMsg)
                  }
                )
              }
              .padding(horizontal = 8.dp)
              .testTag("login_google_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            GoogleMultiColorLogo(modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(7.dp))
            Text(
              text = "Continue with Google",
              color = LoginTextWhite,
              fontSize = 11.2.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1
            )
          }

          // 2. Continue with Apple
          Row(
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(LoginSocialBtnFill)
              .border(1.dp, LoginSocialBtnBorder, RoundedCornerShape(12.dp))
              .clickable(enabled = !isLoading) {
                viewModel.setInfoMessage("Apple Sign-In is not configured on Android. Please use Continue with Google or Email/Password.")
              }
              .padding(horizontal = 8.dp)
              .testTag("login_apple_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            AppleWhiteVectorLogo(modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(7.dp))
            Text(
              text = "Continue with Apple",
              color = LoginTextWhite,
              fontSize = 11.2.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))
    }
  }
}

@Composable
private fun LoginInputField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  leadingIcon: @Composable () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  trailingIcon: (@Composable () -> Unit)? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(50.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(LoginInputFill)
      .border(1.dp, LoginInputBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    leadingIcon()
    Spacer(modifier = Modifier.width(12.dp))
    Box(
      modifier = Modifier.weight(1f),
      contentAlignment = Alignment.CenterStart
    ) {
      if (value.isEmpty()) {
        Text(
          text = placeholder,
          color = Color(0xFF8E95B8),
          fontSize = 14.sp
        )
      }
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
          color = LoginTextWhite,
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium
        ),
        cursorBrush = SolidColor(Color(0xFF47C6FF)),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = Modifier
          .fillMaxWidth()
          .testTag(testTag)
      )
    }
    if (trailingIcon != null) {
      Spacer(modifier = Modifier.width(8.dp))
      trailingIcon()
    }
  }
}

@Composable
private fun GoogleMultiColorLogo(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = w * 0.20f
    val arcSize = Size(w - strokeW, h - strokeW)
    val topLeft = Offset(strokeW / 2f, strokeW / 2f)

    // Red top arc
    drawArc(
      color = Color(0xFFEA4335),
      startAngle = 215f,
      sweepAngle = 105f,
      useCenter = false,
      topLeft = topLeft,
      size = arcSize,
      style = Stroke(width = strokeW, cap = StrokeCap.Butt)
    )
    // Yellow left arc
    drawArc(
      color = Color(0xFFFBBC05),
      startAngle = 145f,
      sweepAngle = 70f,
      useCenter = false,
      topLeft = topLeft,
      size = arcSize,
      style = Stroke(width = strokeW, cap = StrokeCap.Butt)
    )
    // Green bottom arc
    drawArc(
      color = Color(0xFF34A853),
      startAngle = 45f,
      sweepAngle = 100f,
      useCenter = false,
      topLeft = topLeft,
      size = arcSize,
      style = Stroke(width = strokeW, cap = StrokeCap.Butt)
    )
    // Blue right arc + crossbar
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

@Composable
private fun AppleWhiteVectorLogo(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    // Apple leaf
    val leafPath = Path().apply {
      moveTo(w * 0.52f, h * 0.22f)
      cubicTo(w * 0.52f, h * 0.10f, w * 0.65f, h * 0.04f, w * 0.72f, h * 0.04f)
      cubicTo(w * 0.72f, h * 0.14f, w * 0.62f, h * 0.22f, w * 0.52f, h * 0.22f)
      close()
    }
    drawPath(leafPath, color = Color.White)

    // Apple body silhouette
    val bodyPath = Path().apply {
      moveTo(w * 0.50f, h * 0.30f)
      cubicTo(w * 0.38f, h * 0.24f, w * 0.18f, h * 0.30f, w * 0.16f, h * 0.52f)
      cubicTo(w * 0.14f, h * 0.72f, w * 0.28f, h * 0.94f, w * 0.38f, h * 0.94f)
      cubicTo(w * 0.44f, h * 0.94f, w * 0.46f, h * 0.90f, w * 0.52f, h * 0.90f)
      cubicTo(w * 0.58f, h * 0.90f, w * 0.60f, h * 0.94f, w * 0.66f, h * 0.94f)
      cubicTo(w * 0.76f, h * 0.94f, w * 0.86f, h * 0.76f, w * 0.86f, h * 0.64f)
      cubicTo(w * 0.72f, h * 0.58f, w * 0.72f, h * 0.40f, w * 0.84f, h * 0.34f)
      cubicTo(w * 0.76f, h * 0.25f, w * 0.60f, h * 0.25f, w * 0.50f, h * 0.30f)
      close()
    }
    drawPath(bodyPath, color = Color.White)
  }
}

private fun launchGoogleCredentialSignIn(
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
