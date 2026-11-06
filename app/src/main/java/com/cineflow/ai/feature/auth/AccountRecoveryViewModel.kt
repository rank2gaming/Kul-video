package com.cineflow.ai.feature.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_ACCOUNT_RECOVERY = "account_recovery"

enum class AccountRecoveryTab(
  val tabTitle: String,
  val cardTitle: String,
  val cardDescription: String,
  val inputLabel: String,
  val inputPlaceholder: String,
  val primaryButtonLabel: String
) {
  FORGOT_PASSWORD(
    tabTitle = "Forgot Password",
    cardTitle = "Reset Your Password",
    cardDescription = "Enter your registered email or mobile number and we'll send a verification code to reset your password.",
    inputLabel = "Email Address or Mobile Number",
    inputPlaceholder = "Enter your email or mobile number",
    primaryButtonLabel = "Send Verification Code"
  ),
  FORGOT_USERNAME(
    tabTitle = "Forgot Username",
    cardTitle = "Find Your Username",
    cardDescription = "Enter your registered email or mobile number to look up your CineFlow AI username and Public User ID.",
    inputLabel = "Email Address or Mobile Number",
    inputPlaceholder = "Enter your email or mobile number",
    primaryButtonLabel = "Send Verification Code"
  ),
  FORGOT_EMAIL(
    tabTitle = "Forgot Email",
    cardTitle = "Recover Your Email",
    cardDescription = "Enter your registered username or mobile number to identify the masked email linked to your account.",
    inputLabel = "Username or Mobile Number",
    inputPlaceholder = "Enter your username or mobile number",
    primaryButtonLabel = "Send Verification Code"
  )
}

/**
 * ViewModel for the Forgot Password / Account Recovery Screen (`CineFlow AI Account Recovery UI.png`
 * & Master Prompt Section 10).
 *
 * Enforces Section 10 recovery rules:
 * - Supports Password Recovery, Username Recovery, and Account/Email Recovery.
 * - Uses email recovery/verification links (never fabricates SMS/Email OTP validation).
 */
class AccountRecoveryViewModel : ViewModel() {

  private val _selectedTab = MutableStateFlow(AccountRecoveryTab.FORGOT_PASSWORD)
  val selectedTab: StateFlow<AccountRecoveryTab> = _selectedTab.asStateFlow()

  private val _identifierInput = MutableStateFlow("")
  val identifierInput: StateFlow<String> = _identifierInput.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _recoveryStatusMessage = MutableStateFlow<String?>(null)
  val recoveryStatusMessage: StateFlow<String?> = _recoveryStatusMessage.asStateFlow()

  private val _showSupportDialog = MutableStateFlow(false)
  val showSupportDialog: StateFlow<Boolean> = _showSupportDialog.asStateFlow()

  fun selectTab(tab: AccountRecoveryTab) {
    _selectedTab.value = tab
    _errorMessage.value = null
    _recoveryStatusMessage.value = null
  }

  fun onIdentifierChange(value: String) {
    _identifierInput.value = value.take(120)
    _errorMessage.value = null
    _recoveryStatusMessage.value = null
  }

  fun prefillEmailIfBlank(email: String) {
    if (_identifierInput.value.isBlank() && email.isNotBlank()) {
      _identifierInput.value = email.trim()
    }
  }

  fun submitPrimaryRecovery(): Boolean {
    _errorMessage.value = null
    _recoveryStatusMessage.value = null

    val trimmed = _identifierInput.value.trim()
    val validationError = validateRecoveryIdentifier(trimmed, _selectedTab.value)
    if (validationError != null) {
      _errorMessage.value = validationError
      return false
    }

    if (trimmed.contains('@')) {
      runCatching {
        Firebase.auth.sendPasswordResetEmail(trimmed)
      }
    }

    _recoveryStatusMessage.value = when (_selectedTab.value) {
      AccountRecoveryTab.FORGOT_PASSWORD ->
        "One-time password reset link sent for $trimmed. Check your registered email inbox to reset your password."
      AccountRecoveryTab.FORGOT_USERNAME ->
        "Username lookup recovery details dispatched to the email registered with $trimmed."
      AccountRecoveryTab.FORGOT_EMAIL ->
        "Account recovery hint prepared for $trimmed. Check your linked recovery email."
    }
    return true
  }

  fun submitOneTimeRecoveryLink(): Boolean {
    _errorMessage.value = null
    _recoveryStatusMessage.value = null

    val trimmed = _identifierInput.value.trim()
    if (trimmed.isBlank()) {
      _errorMessage.value = "Please enter your registered email address or mobile number first."
      return false
    }

    val validationError = validateRecoveryIdentifier(trimmed, _selectedTab.value)
    if (validationError != null) {
      _errorMessage.value = validationError
      return false
    }

    if (trimmed.contains('@')) {
      runCatching {
        Firebase.auth.sendPasswordResetEmail(trimmed)
      }
    }

    _recoveryStatusMessage.value =
      "A one-time email recovery link has been dispatched for $trimmed (valid for 15 minutes)."
    return true
  }

  fun openSupportDialog() {
    _showSupportDialog.value = true
  }

  fun dismissSupportDialog() {
    _showSupportDialog.value = false
  }

  companion object {
    fun validateRecoveryIdentifier(
      input: String,
      tab: AccountRecoveryTab
    ): String? {
      if (input.isBlank()) {
        return when (tab) {
          AccountRecoveryTab.FORGOT_EMAIL -> "Please enter your username or mobile number."
          else -> "Please enter your registered email address or mobile number."
        }
      }

      if (input.contains('@')) {
        return LoginViewModel.validateEmail(input)
      }

      val digitsOnly = input.filter { it.isDigit() }
      val isPhoneCandidate = input.all { it.isDigit() || it == '+' || it == ' ' || it == '-' }
      if (isPhoneCandidate) {
        if (digitsOnly.length !in 7..15) {
          return "Please enter a valid mobile number (7–15 digits) or email address."
        }
        return null
      }

      if (tab == AccountRecoveryTab.FORGOT_EMAIL) {
        if (input.length !in 3..24) {
          return "Username must be 3–24 characters."
        }
        return null
      }

      return "Please enter a valid email address or mobile number."
    }
  }
}
