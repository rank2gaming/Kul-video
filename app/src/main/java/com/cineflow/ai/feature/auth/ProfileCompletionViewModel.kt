package com.cineflow.ai.feature.auth

import androidx.lifecycle.ViewModel
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_PROFILE_COMPLETION = "profile_completion"

/**
 * ViewModel for the Profile Completion Screen (`CineFlow AI Profile Setup.png` & Master Prompt Section 10).
 *
 * Triggered exclusively after Google Sign-In so that a Google-authenticated user must complete
 * their profile before entering the Home Dashboard:
 * - Username (required, unique, 3–24 characters using letters, numbers, '_' or '-')
 * - Optional Mobile Number with Country Code selector (`+91` default; validates digit count when provided)
 * - Set Password * and Confirm Password * with live green tick requirements:
 *   1. Minimum 8 characters (8–25 characters)
 *   2. At least one uppercase letter (A–Z)
 *   3. At least one lowercase letter (a–z)
 *   4. At least one number (0–9)
 * - Allocates/verifies Public User ID (`CF` + 8 numeric digits, e.g. `CF00000001`).
 */
class ProfileCompletionViewModel : ViewModel() {

  private val _username = MutableStateFlow("")
  val username: StateFlow<String> = _username.asStateFlow()

  private val _isUsernameValidAndAvailable = MutableStateFlow(false)
  val isUsernameValidAndAvailable: StateFlow<Boolean> = _isUsernameValidAndAvailable.asStateFlow()

  private val _countryCode = MutableStateFlow("+91")
  val countryCode: StateFlow<String> = _countryCode.asStateFlow()

  private val _mobileNumber = MutableStateFlow("")
  val mobileNumber: StateFlow<String> = _mobileNumber.asStateFlow()

  private val _isMobileNumberValid = MutableStateFlow(false)
  val isMobileNumberValid: StateFlow<Boolean> = _isMobileNumberValid.asStateFlow()

  private val _passwordRequired = MutableStateFlow(true)
  val passwordRequired: StateFlow<Boolean> = _passwordRequired.asStateFlow()

  private val _password = MutableStateFlow("")
  val password: StateFlow<String> = _password.asStateFlow()

  private val _confirmPassword = MutableStateFlow("")
  val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

  private val _hasValidPasswordLength = MutableStateFlow(false)
  val hasValidPasswordLength: StateFlow<Boolean> = _hasValidPasswordLength.asStateFlow()

  private val _hasUppercase = MutableStateFlow(false)
  val hasUppercase: StateFlow<Boolean> = _hasUppercase.asStateFlow()

  private val _hasLowercase = MutableStateFlow(false)
  val hasLowercase: StateFlow<Boolean> = _hasLowercase.asStateFlow()

  private val _hasNumber = MutableStateFlow(false)
  val hasNumber: StateFlow<Boolean> = _hasNumber.asStateFlow()

  private val _isPasswordStrong = MutableStateFlow(false)
  val isPasswordStrong: StateFlow<Boolean> = _isPasswordStrong.asStateFlow()

  private val _isConfirmPasswordMatching = MutableStateFlow(false)
  val isConfirmPasswordMatching: StateFlow<Boolean> = _isConfirmPasswordMatching.asStateFlow()

  private val _isPasswordVisible = MutableStateFlow(false)
  val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

  private val _isConfirmPasswordVisible = MutableStateFlow(false)
  val isConfirmPasswordVisible: StateFlow<Boolean> = _isConfirmPasswordVisible.asStateFlow()

  private val _selectedAvatarUri = MutableStateFlow<String?>(null)
  val selectedAvatarUri: StateFlow<String?> = _selectedAvatarUri.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _infoMessage = MutableStateFlow<String?>(null)
  val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

  private val _completedPublicUid = MutableStateFlow<String?>(null)
  val completedPublicUid: StateFlow<String?> = _completedPublicUid.asStateFlow()

  private val _isProfileSetupCompleted = MutableStateFlow(false)
  val isProfileSetupCompleted: StateFlow<Boolean> = _isProfileSetupCompleted.asStateFlow()

  private val registeredUsernames = mutableSetOf("admin", "cineflow_official")

  /**
   * Resets the Profile Setup state when a user initiates Google Sign-In so that
   * all fields start clean and green ticks only activate as the user enters valid data.
   */
  fun prepareForGoogleSignIn() {
    _username.value = ""
    _isUsernameValidAndAvailable.value = false
    _countryCode.value = "+91"
    _mobileNumber.value = ""
    _isMobileNumberValid.value = false
    _passwordRequired.value = true
    _password.value = ""
    _confirmPassword.value = ""
    updatePasswordRequirementStates()
    _isPasswordVisible.value = false
    _isConfirmPasswordVisible.value = false
    _errorMessage.value = null
    _infoMessage.value = null
    _completedPublicUid.value = null
    _isProfileSetupCompleted.value = false
  }

  fun onUsernameChange(value: String) {
    val sanitized = value.replace(" ", "").take(24)
    _username.value = sanitized
    _errorMessage.value = null
    _infoMessage.value = null
    _isUsernameValidAndAvailable.value = isUsernameFormatAndAvailabilityValid(sanitized.trim())
  }

  fun cycleCountryCode() {
    val codes = SUPPORTED_COUNTRY_CODES
    val nextIdx = (codes.indexOf(_countryCode.value) + 1) % codes.size
    selectCountryCode(codes[nextIdx])
  }

  fun selectCountryCode(code: String) {
    if (code in SUPPORTED_COUNTRY_CODES) {
      _countryCode.value = code
      val maxDigits = maxDigitsForCountry(code)
      val currentDigits = _mobileNumber.value.filter { it.isDigit() }.take(maxDigits)
      _mobileNumber.value = currentDigits
      _isMobileNumberValid.value = currentDigits.isNotEmpty() &&
        validateMobileNumber(code, currentDigits) == null
      _errorMessage.value = null
    }
  }

  fun onMobileNumberChange(value: String) {
    val maxDigits = maxDigitsForCountry(_countryCode.value)
    val digitsOnly = value.filter { it.isDigit() }.take(maxDigits)
    _mobileNumber.value = digitsOnly
    _isMobileNumberValid.value = digitsOnly.isNotEmpty() &&
      validateMobileNumber(_countryCode.value, digitsOnly) == null
    _errorMessage.value = null
    _infoMessage.value = null
  }

  /**
   * Section 10: "Password only when required by the approved flow.
   * Do not ask for a password that is already configured."
   */
  fun setPasswordAlreadyConfigured(alreadyConfigured: Boolean) {
    _passwordRequired.value = !alreadyConfigured
    if (alreadyConfigured) {
      _password.value = ""
      _confirmPassword.value = ""
      updatePasswordRequirementStates()
    }
  }

  fun prefillFromAuthUser(suggestedUsername: String?, alreadyHasPassword: Boolean) {
    setPasswordAlreadyConfigured(alreadyHasPassword)
    if (_username.value.isBlank() && !suggestedUsername.isNullOrBlank()) {
      val cleaned = suggestedUsername
        .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        .take(24)
      if (cleaned.length >= 3) {
        onUsernameChange(cleaned)
      }
    }
  }

  fun onPasswordChange(value: String) {
    _password.value = value.take(30)
    _errorMessage.value = null
    _infoMessage.value = null
    updatePasswordRequirementStates()
  }

  fun onConfirmPasswordChange(value: String) {
    _confirmPassword.value = value.take(30)
    _errorMessage.value = null
    _infoMessage.value = null
    updatePasswordRequirementStates()
  }

  private fun updatePasswordRequirementStates() {
    val pwd = _password.value
    val confirm = _confirmPassword.value
    val lenOk = pwd.length in 8..25
    val upperOk = pwd.any { it.isUpperCase() }
    val lowerOk = pwd.any { it.isLowerCase() }
    val numOk = pwd.any { it.isDigit() }
    val strong = lenOk && upperOk && lowerOk && numOk

    _hasValidPasswordLength.value = lenOk
    _hasUppercase.value = upperOk
    _hasLowercase.value = lowerOk
    _hasNumber.value = numOk
    _isPasswordStrong.value = strong
    _isConfirmPasswordMatching.value = confirm.isNotEmpty() && confirm == pwd && strong
  }

  fun togglePasswordVisibility() {
    _isPasswordVisible.value = !_isPasswordVisible.value
  }

  fun toggleConfirmPasswordVisibility() {
    _isConfirmPasswordVisible.value = !_isConfirmPasswordVisible.value
  }

  fun onAvatarSelected(uriString: String?) {
    _selectedAvatarUri.value = uriString
  }

  private fun isUsernameFormatAndAvailabilityValid(candidate: String): Boolean {
    return candidate.isNotEmpty() &&
      USERNAME_REGEX.matches(candidate) &&
      candidate.any { it.isLetterOrDigit() } &&
      candidate.lowercase(Locale.US) !in registeredUsernames
  }

  fun submitProfileCompletion(onSuccess: (publicUid: String) -> Unit = {}): Boolean {
    _errorMessage.value = null
    _infoMessage.value = null

    val userTrimmed = _username.value.trim()
    if (userTrimmed.isEmpty()) {
      _errorMessage.value = "Please create a unique username to continue."
      return false
    }
    if (!USERNAME_REGEX.matches(userTrimmed) || !userTrimmed.any { it.isLetterOrDigit() }) {
      _errorMessage.value = "Username must be 3–24 characters using letters, numbers, '_' or '-'."
      return false
    }
    if (userTrimmed.lowercase(Locale.US) in registeredUsernames) {
      _errorMessage.value = "Username '$userTrimmed' is already taken. Please create a unique username."
      return false
    }

    val mobileDigits = _mobileNumber.value.filter { it.isDigit() }
    val mobileError = validateMobileNumber(_countryCode.value, mobileDigits)
    if (mobileError != null) {
      _errorMessage.value = mobileError
      return false
    }

    if (_passwordRequired.value) {
      val rawPassword = _password.value
      val passwordError = LoginViewModel.validatePassword(rawPassword)
      if (passwordError != null) {
        _errorMessage.value = passwordError
        return false
      }
      if (_confirmPassword.value.isEmpty()) {
        _errorMessage.value = "Please re-enter your password in Confirm Password."
        return false
      }
      if (_confirmPassword.value != rawPassword) {
        _errorMessage.value = "Confirm Password does not match Set Password."
        return false
      }
    }

    registeredUsernames.add(userTrimmed.lowercase(Locale.US))
    val numericHash = (abs((userTrimmed + ":" + _countryCode.value + mobileDigits).hashCode()) % 99_999_999) + 1
    val allocatedPublicUid = String.format(Locale.US, "CF%08d", numericHash)
    _completedPublicUid.value = allocatedPublicUid
    _isProfileSetupCompleted.value = true
    _isUsernameValidAndAvailable.value = true
    _infoMessage.value = "Profile completed ($allocatedPublicUid)."
    onSuccess(allocatedPublicUid)
    return true
  }

  companion object {
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_\\-]{3,24}$")
    val SUPPORTED_COUNTRY_CODES = listOf("+91", "+880", "+1", "+44")

    fun maxDigitsForCountry(countryCode: String): Int = when (countryCode) {
      "+91", "+1" -> 10
      "+880", "+44" -> 11
      else -> 15
    }

    fun validateMobileNumber(countryCode: String, digits: String): String? {
      if (digits.isEmpty()) return null
      return when (countryCode) {
        "+91" -> if (digits.length != 10) {
          "Mobile number for +91 must be 10 digits, or leave it blank."
        } else {
          null
        }
        "+880" -> if (digits.length !in 10..11) {
          "Mobile number for +880 must be 10–11 digits, or leave it blank."
        } else {
          null
        }
        "+1" -> if (digits.length != 10) {
          "Mobile number for +1 must be 10 digits, or leave it blank."
        } else {
          null
        }
        "+44" -> if (digits.length !in 10..11) {
          "Mobile number for +44 must be 10–11 digits, or leave it blank."
        } else {
          null
        }
        else -> if (digits.length !in 7..15) {
          "Optional mobile number must be 7–15 digits or left blank."
        } else {
          null
        }
      }
    }
  }
}
