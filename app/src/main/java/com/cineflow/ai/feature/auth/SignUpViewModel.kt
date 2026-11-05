package com.cineflow.ai.feature.auth

import androidx.lifecycle.ViewModel
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_SIGN_UP = "sign_up"

/**
 * ViewModel for the Sign Up / Account Creation Screen (`CineFlow AI Account Creation Screen.png`
 * & Master Prompt Section 10).
 *
 * Enforces Section 10 requirements:
 * - Full Name (required)
 * - Unique Username (`3..24` chars, alphanumeric / `_` / `-`)
 * - Email Address (required, email verification link notice; no SMS/Email OTP)
 * - Optional Mobile Number with Country Code selector (`+91` default)
 * - Password (`8..25` chars, uppercase, lowercase, number required; special character optional)
 * - Confirm Password matching Password
 * - Public User ID allocation (`CF` + 8 numeric digits, e.g., `CF00000001`)
 */
class SignUpViewModel : ViewModel() {

  private val _fullName = MutableStateFlow("")
  val fullName: StateFlow<String> = _fullName.asStateFlow()

  private val _username = MutableStateFlow("")
  val username: StateFlow<String> = _username.asStateFlow()

  private val _email = MutableStateFlow("")
  val email: StateFlow<String> = _email.asStateFlow()

  private val _countryCode = MutableStateFlow("+91")
  val countryCode: StateFlow<String> = _countryCode.asStateFlow()

  private val _mobileNumber = MutableStateFlow("")
  val mobileNumber: StateFlow<String> = _mobileNumber.asStateFlow()

  private val _password = MutableStateFlow("")
  val password: StateFlow<String> = _password.asStateFlow()

  private val _confirmPassword = MutableStateFlow("")
  val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

  private val _isPasswordVisible = MutableStateFlow(false)
  val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

  private val _isConfirmPasswordVisible = MutableStateFlow(false)
  val isConfirmPasswordVisible: StateFlow<Boolean> = _isConfirmPasswordVisible.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _infoMessage = MutableStateFlow<String?>(null)
  val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

  private val _createdPublicUid = MutableStateFlow<String?>(null)
  val createdPublicUid: StateFlow<String?> = _createdPublicUid.asStateFlow()

  private val registeredUsernames = mutableSetOf("admin", "cineflow_official")

  fun onFullNameChange(value: String) {
    _fullName.value = value.take(80)
    _errorMessage.value = null
  }

  fun onUsernameChange(value: String) {
    _username.value = value.take(24)
    _errorMessage.value = null
  }

  fun onEmailChange(value: String) {
    _email.value = value.take(120)
    _errorMessage.value = null
  }

  fun cycleCountryCode() {
    val codes = listOf("+91", "+880", "+1", "+44")
    val nextIdx = (codes.indexOf(_countryCode.value) + 1) % codes.size
    _countryCode.value = codes[nextIdx]
  }

  fun onMobileNumberChange(value: String) {
    val digitsOnly = value.filter { it.isDigit() || it == ' ' || it == '-' }.take(16)
    _mobileNumber.value = digitsOnly
    _errorMessage.value = null
  }

  fun onPasswordChange(value: String) {
    _password.value = value.take(40)
    _errorMessage.value = null
  }

  fun onConfirmPasswordChange(value: String) {
    _confirmPassword.value = value.take(40)
    _errorMessage.value = null
  }

  fun togglePasswordVisibility() {
    _isPasswordVisible.value = !_isPasswordVisible.value
  }

  fun toggleConfirmPasswordVisibility() {
    _isConfirmPasswordVisible.value = !_isConfirmPasswordVisible.value
  }

  fun setLoading(loading: Boolean) {
    _isLoading.value = loading
  }

  fun setAuthError(message: String?) {
    _isLoading.value = false
    _errorMessage.value = message
  }

  fun submitSignUp(onSuccess: (publicUid: String) -> Unit = {}): Boolean {
    _errorMessage.value = null
    _infoMessage.value = null

    val nameTrimmed = _fullName.value.trim()
    if (nameTrimmed.length < 2) {
      _errorMessage.value = "Please enter your full name (at least 2 characters)."
      return false
    }

    val userTrimmed = _username.value.trim()
    if (!USERNAME_REGEX.matches(userTrimmed)) {
      _errorMessage.value = "Username must be 3–24 characters using letters, numbers, '_' or '-'."
      return false
    }
    if (userTrimmed.lowercase(Locale.US) in registeredUsernames) {
      _errorMessage.value = "Username '$userTrimmed' is already taken. Please choose a unique username."
      return false
    }

    val emailTrimmed = _email.value.trim()
    val emailError = LoginViewModel.validateEmail(emailTrimmed)
    if (emailError != null) {
      _errorMessage.value = emailError
      return false
    }

    val mobileTrimmed = _mobileNumber.value.filter { it.isDigit() }
    if (mobileTrimmed.isNotEmpty() && (mobileTrimmed.length < 7 || mobileTrimmed.length > 15)) {
      _errorMessage.value = "Optional mobile number must be 7–15 digits or left blank."
      return false
    }

    val rawPassword = _password.value
    val passwordError = LoginViewModel.validatePassword(rawPassword)
    if (passwordError != null) {
      _errorMessage.value = passwordError
      return false
    }

    if (_confirmPassword.value != rawPassword) {
      _errorMessage.value = "Confirm Password does not match Password."
      return false
    }

    registeredUsernames.add(userTrimmed.lowercase(Locale.US))
    val numericHash = (abs((userTrimmed + ":" + emailTrimmed).hashCode()) % 99_999_999) + 1
    val allocatedPublicUid = String.format(Locale.US, "CF%08d", numericHash)
    _createdPublicUid.value = allocatedPublicUid
    _infoMessage.value = "Account created ($allocatedPublicUid). Verification link prepared for $emailTrimmed."
    onSuccess(allocatedPublicUid)
    return true
  }

  companion object {
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_\\-]{3,24}$")

    fun hasValidLength(password: String): Boolean = password.length in 8..25
    fun hasUppercase(password: String): Boolean = password.any { it.isUpperCase() }
    fun hasLowercase(password: String): Boolean = password.any { it.isLowerCase() }
    fun hasNumber(password: String): Boolean = password.any { it.isDigit() }
  }
}
