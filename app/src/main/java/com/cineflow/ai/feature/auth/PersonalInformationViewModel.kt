package com.cineflow.ai.feature.auth

import androidx.lifecycle.ViewModel
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_PERSONAL_INFORMATION = "personal_information"

enum class EditablePersonalField(
  val dialogTitle: String,
  val fieldHint: String
) {
  FULL_NAME("Edit Full Name", "Enter your full name"),
  USERNAME("Edit Username", "Enter a unique username (3–24 chars)"),
  EMAIL("Edit Email Address", "Enter your email address"),
  MOBILE("Edit Mobile Number", "Enter mobile number (e.g. +91 98765 43210)"),
  DATE_OF_BIRTH("Edit Date of Birth", "e.g. Jan 15, 2000"),
  GENDER("Select Gender", "e.g. Male, Female, Non-binary, Prefer not to say"),
  LOCATION("Edit Location", "e.g. India, Bangladesh, United States")
}

/**
 * ViewModel for the Personal Information / Edit Profile Screen (`CineFlow AI Personal Profile Screen.png`
 * & Master Prompt Section 6 & Section 10).
 */
class PersonalInformationViewModel : ViewModel() {

  private val _fullName = MutableStateFlow("Manirul Hasan")
  val fullName: StateFlow<String> = _fullName.asStateFlow()

  private val _username = MutableStateFlow("manirul_editz")
  val username: StateFlow<String> = _username.asStateFlow()

  private val _email = MutableStateFlow("manirulhasan@gmail.com")
  val email: StateFlow<String> = _email.asStateFlow()

  private val _isEmailVerified = MutableStateFlow(true)
  val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

  private val _mobileNumber = MutableStateFlow("+91 98765 43210")
  val mobileNumber: StateFlow<String> = _mobileNumber.asStateFlow()

  private val _isMobileVerified = MutableStateFlow(true)
  val isMobileVerified: StateFlow<Boolean> = _isMobileVerified.asStateFlow()

  // Section 10: Public User ID format CF + 8 numeric digits (never exposes raw Firebase UID)
  private val _publicUid = MutableStateFlow("CF00100247")
  val publicUid: StateFlow<String> = _publicUid.asStateFlow()

  private val _dateOfBirth = MutableStateFlow("Jan 15, 2000")
  val dateOfBirth: StateFlow<String> = _dateOfBirth.asStateFlow()

  private val _gender = MutableStateFlow("Male")
  val gender: StateFlow<String> = _gender.asStateFlow()

  private val _location = MutableStateFlow("India")
  val location: StateFlow<String> = _location.asStateFlow()

  private val _selectedAvatarUri = MutableStateFlow<String?>(null)
  val selectedAvatarUri: StateFlow<String?> = _selectedAvatarUri.asStateFlow()

  private val _activeEditField = MutableStateFlow<EditablePersonalField?>(null)
  val activeEditField: StateFlow<EditablePersonalField?> = _activeEditField.asStateFlow()

  private val _editDraftValue = MutableStateFlow("")
  val editDraftValue: StateFlow<String> = _editDraftValue.asStateFlow()

  private val _showUidInfoDialog = MutableStateFlow(false)
  val showUidInfoDialog: StateFlow<Boolean> = _showUidInfoDialog.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private val registeredUsernames = mutableSetOf("admin", "cineflow_official")

  fun syncFromProfile(
    fullName: String?,
    username: String?,
    email: String?,
    mobile: String?,
    publicUid: String?
  ) {
    if (!fullName.isNullOrBlank()) {
      _fullName.value = fullName.trim()
    }
    if (!username.isNullOrBlank()) {
      _username.value = username.trim().removePrefix("@")
    }
    if (!email.isNullOrBlank()) {
      _email.value = email.trim()
    }
    if (!mobile.isNullOrBlank()) {
      _mobileNumber.value = mobile.trim()
    }
    if (!publicUid.isNullOrBlank()) {
      _publicUid.value = ProfileHubViewModel.normalizePublicUid(publicUid.trim())
    }
  }

  fun openEditDialog(field: EditablePersonalField) {
    _errorMessage.value = null
    _statusMessage.value = null
    _activeEditField.value = field
    _editDraftValue.value = when (field) {
      EditablePersonalField.FULL_NAME -> _fullName.value
      EditablePersonalField.USERNAME -> _username.value
      EditablePersonalField.EMAIL -> _email.value
      EditablePersonalField.MOBILE -> _mobileNumber.value
      EditablePersonalField.DATE_OF_BIRTH -> _dateOfBirth.value
      EditablePersonalField.GENDER -> _gender.value
      EditablePersonalField.LOCATION -> _location.value
    }
  }

  fun onEditDraftChange(value: String) {
    _editDraftValue.value = value.take(120)
    _errorMessage.value = null
  }

  fun dismissEditDialog() {
    _activeEditField.value = null
    _errorMessage.value = null
  }

  fun confirmFieldEdit(): Boolean {
    val field = _activeEditField.value ?: return false
    val trimmed = _editDraftValue.value.trim()
    _errorMessage.value = null

    when (field) {
      EditablePersonalField.FULL_NAME -> {
        if (trimmed.length < 2) {
          _errorMessage.value = "Full Name must be at least 2 characters."
          return false
        }
        _fullName.value = trimmed
      }
      EditablePersonalField.USERNAME -> {
        val cleaned = trimmed.removePrefix("@")
        if (!USERNAME_REGEX.matches(cleaned)) {
          _errorMessage.value = "Username must be 3–24 characters using letters, numbers, '_' or '-'."
          return false
        }
        if (cleaned.lowercase(Locale.US) in registeredUsernames &&
          !cleaned.equals(_username.value, ignoreCase = true)
        ) {
          _errorMessage.value = "Username '$cleaned' is already taken."
          return false
        }
        _username.value = cleaned
      }
      EditablePersonalField.EMAIL -> {
        val emailErr = LoginViewModel.validateEmail(trimmed)
        if (emailErr != null) {
          _errorMessage.value = emailErr
          return false
        }
        _email.value = trimmed
      }
      EditablePersonalField.MOBILE -> {
        val digits = trimmed.filter { it.isDigit() }
        if (trimmed.isNotEmpty() && digits.length !in 7..15) {
          _errorMessage.value = "Mobile number must contain 7–15 digits or be left blank."
          return false
        }
        _mobileNumber.value = trimmed
      }
      EditablePersonalField.DATE_OF_BIRTH -> {
        if (trimmed.isBlank()) {
          _errorMessage.value = "Please enter a valid Date of Birth."
          return false
        }
        _dateOfBirth.value = trimmed
      }
      EditablePersonalField.GENDER -> {
        if (trimmed.isBlank()) {
          _errorMessage.value = "Please specify a gender."
          return false
        }
        _gender.value = trimmed
      }
      EditablePersonalField.LOCATION -> {
        if (trimmed.isBlank()) {
          _errorMessage.value = "Please enter a location."
          return false
        }
        _location.value = trimmed
      }
    }

    _activeEditField.value = null
    _statusMessage.value = "${field.dialogTitle.removePrefix("Edit ").removePrefix("Select ")} updated."
    return true
  }

  fun updateFullNameDirect(newName: String) {
    _fullName.value = newName
    _errorMessage.value = null
  }

  fun updateUsernameDirect(newUsername: String) {
    _username.value = newUsername
    _errorMessage.value = null
  }

  fun copyPublicUid(): String {
    val uid = _publicUid.value
    _statusMessage.value = "Copied Public User ID $uid to clipboard."
    return uid
  }

  fun openUidInfoDialog() {
    _showUidInfoDialog.value = true
  }

  fun dismissUidInfoDialog() {
    _showUidInfoDialog.value = false
  }

  fun onAvatarSelected(uriString: String?) {
    _selectedAvatarUri.value = uriString
    if (uriString != null) {
      _statusMessage.value = "Profile photo updated."
    }
  }

  fun saveChanges(
    onSaved: (fullName: String, username: String, email: String, mobile: String) -> Unit = { _, _, _, _ -> }
  ): Boolean {
    _errorMessage.value = null
    _statusMessage.value = null

    val nameTrimmed = _fullName.value.trim()
    if (nameTrimmed.length < 2) {
      _errorMessage.value = "Full Name must be at least 2 characters."
      return false
    }

    val userTrimmed = _username.value.trim().removePrefix("@")
    if (!USERNAME_REGEX.matches(userTrimmed)) {
      _errorMessage.value = "Username must be 3–24 characters using letters, numbers, '_' or '-'."
      return false
    }
    if (userTrimmed.lowercase(Locale.US) in registeredUsernames) {
      _errorMessage.value = "Username '$userTrimmed' is already taken."
      return false
    }

    val emailTrimmed = _email.value.trim()
    val emailErr = LoginViewModel.validateEmail(emailTrimmed)
    if (emailErr != null) {
      _errorMessage.value = emailErr
      return false
    }

    val mobileDigits = _mobileNumber.value.filter { it.isDigit() }
    if (_mobileNumber.value.isNotBlank() && mobileDigits.length !in 7..15) {
      _errorMessage.value = "Mobile number must be 7–15 digits or left blank."
      return false
    }

    _statusMessage.value = "Personal information saved successfully."
    onSaved(nameTrimmed, userTrimmed, emailTrimmed, _mobileNumber.value.trim())
    return true
  }

  companion object {
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_\\-]{3,24}$")
  }
}
