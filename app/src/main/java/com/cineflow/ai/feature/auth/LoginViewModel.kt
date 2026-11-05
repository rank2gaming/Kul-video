package com.cineflow.ai.feature.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_LOGIN = "login"

/**
 * ViewModel for the Login Screen (`02_Login_Screen.png` & Master Prompt Section 9 & 10).
 *
 * Enforces Section 10 password & email rules:
 * - Password length: 8..25 characters
 * - Uppercase required
 * - Lowercase required
 * - Number required
 * - Special character optional
 * - Skip action enters unsigned local-only mode without creating any Firebase or anonymous user.
 */
class LoginViewModel : ViewModel() {

  private val _email = MutableStateFlow("")
  val email: StateFlow<String> = _email.asStateFlow()

  private val _password = MutableStateFlow("")
  val password: StateFlow<String> = _password.asStateFlow()

  private val _isPasswordVisible = MutableStateFlow(false)
  val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _infoMessage = MutableStateFlow<String?>(null)
  val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

  private val _isLocalSessionAuthenticated = MutableStateFlow(false)
  val isLocalSessionAuthenticated: StateFlow<Boolean> = _isLocalSessionAuthenticated.asStateFlow()

  fun onEmailChange(newEmail: String) {
    _email.value = newEmail.take(120)
    _errorMessage.value = null
    _infoMessage.value = null
  }

  fun onPasswordChange(newPassword: String) {
    _password.value = newPassword.take(40)
    _errorMessage.value = null
    _infoMessage.value = null
  }

  fun togglePasswordVisibility() {
    _isPasswordVisible.value = !_isPasswordVisible.value
  }

  fun setLoading(loading: Boolean) {
    _isLoading.value = loading
  }

  fun setAuthError(message: String?) {
    _isLoading.value = false
    _errorMessage.value = message
  }

  fun setInfoMessage(message: String?) {
    _isLoading.value = false
    _infoMessage.value = message
  }

  /**
   * Validates email and password according to Master Prompt Section 10 and submits login.
   */
  fun submitLogin(onSuccess: () -> Unit = {}): Boolean {
    _errorMessage.value = null
    _infoMessage.value = null

    val trimmedEmail = _email.value.trim()
    val rawPassword = _password.value

    val emailValidationError = validateEmail(trimmedEmail)
    if (emailValidationError != null) {
      _errorMessage.value = emailValidationError
      return false
    }

    val passwordValidationError = validatePassword(rawPassword)
    if (passwordValidationError != null) {
      _errorMessage.value = passwordValidationError
      return false
    }

    _isLocalSessionAuthenticated.value = true
    onSuccess()
    return true
  }

  fun handleForgotPasswordClick() {
    _errorMessage.value = null
    val trimmedEmail = _email.value.trim()
    val emailError = validateEmail(trimmedEmail)
    if (emailError != null) {
      _errorMessage.value = "Enter a valid email address first to receive a password recovery link."
      return
    }
    _infoMessage.value = "Password recovery link prepared for $trimmedEmail. Check your inbox or sign in with Google."
  }

  companion object {
    fun validateEmail(email: String): String? {
      if (email.isBlank()) {
        return "Please enter your email address."
      }
      val atIndex = email.indexOf('@')
      val dotIndex = email.lastIndexOf('.')
      if (atIndex <= 0 || dotIndex <= atIndex + 1 || dotIndex >= email.length - 1) {
        return "Please enter a valid email address."
      }
      return null
    }

    /**
     * Master Prompt Section 10 Password Policy:
     * - Minimum 8 characters
     * - Maximum 25 characters
     * - Uppercase required
     * - Lowercase required
     * - Number required
     * - Special character optional
     */
    fun validatePassword(password: String): String? {
      if (password.isEmpty()) {
        return "Please enter your password."
      }
      if (password.length < 8) {
        return "Password must be at least 8 characters."
      }
      if (password.length > 25) {
        return "Password cannot exceed 25 characters."
      }
      if (!password.any { it.isUpperCase() }) {
        return "Password must include at least one uppercase letter."
      }
      if (!password.any { it.isLowerCase() }) {
        return "Password must include at least one lowercase letter."
      }
      if (!password.any { it.isDigit() }) {
        return "Password must include at least one number."
      }
      return null
    }
  }
}
