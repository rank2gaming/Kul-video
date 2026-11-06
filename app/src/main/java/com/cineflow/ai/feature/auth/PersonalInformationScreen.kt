package com.cineflow.ai.feature.auth

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Transgender
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cineflow.ai.R

private val PersonalInfoBgDeep = Color(0xFF0A081E)
private val PersonalInfoCardSurface = Color(0xFF101434)
private val PersonalInfoRowBg = Color(0xFF15193E)
private val PersonalInfoRowBorder = Color(0xFF343B6E)
private val PersonalInfoTextWhite = Color(0xFFFFFFFF)
private val PersonalInfoTextMuted = Color(0xFFB5B9DB)
private val PersonalInfoAsteriskRed = Color(0xFFFF4D6D)
private val PersonalInfoVerifiedGreen = Color(0xFF22C55E)

/**
 * Personal Information / Edit Profile Screen (`CineFlow AI Personal Profile Screen.png`
 * & Master Prompt Section 6 & Section 10).
 */
@Composable
fun PersonalInformationScreen(
  viewModel: PersonalInformationViewModel,
  onBack: () -> Unit,
  onChangesSaved: (fullName: String, username: String, email: String, mobile: String) -> Unit = { _, _, _, _ -> },
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current

  val fullName by viewModel.fullName.collectAsStateWithLifecycle()
  val username by viewModel.username.collectAsStateWithLifecycle()
  val email by viewModel.email.collectAsStateWithLifecycle()
  val isEmailVerified by viewModel.isEmailVerified.collectAsStateWithLifecycle()
  val mobileNumber by viewModel.mobileNumber.collectAsStateWithLifecycle()
  val isMobileVerified by viewModel.isMobileVerified.collectAsStateWithLifecycle()
  val publicUid by viewModel.publicUid.collectAsStateWithLifecycle()
  val dateOfBirth by viewModel.dateOfBirth.collectAsStateWithLifecycle()
  val gender by viewModel.gender.collectAsStateWithLifecycle()
  val location by viewModel.location.collectAsStateWithLifecycle()
  val activeEditField by viewModel.activeEditField.collectAsStateWithLifecycle()
  val editDraftValue by viewModel.editDraftValue.collectAsStateWithLifecycle()
  val showUidInfoDialog by viewModel.showUidInfoDialog.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
  val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      viewModel.onAvatarSelected(uri.toString())
    }
  }

  val launchPhotoPicker: () -> Unit = {
    runCatching {
      photoPickerLauncher.launch(
        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
      )
    }.onFailure {
      viewModel.onAvatarSelected("avatar://creator_personal_info")
    }
  }

  BackHandler {
    onBack()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(PersonalInfoBgDeep)
      .testTag("personal_information_screen"),
    containerColor = PersonalInfoBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(PersonalInfoBgDeep),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Header & 3D ID Card Banner (`img_personal_info_top_banner.png`)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(941f / 146f)
          .testTag("personal_info_top_banner")
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_personal_info_top_banner),
          contentDescription = "CineFlow AI — Personal Information",
          contentScale = ContentScale.FillWidth,
          modifier = Modifier.fillMaxSize()
        )

        // Left Back arrow hotspot (`←`)
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 10.dp, top = 6.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onBack)
            .testTag("personal_info_back_button")
        )
      }

      // 2. Profile Identity Hero Row (`personal_info_identity_section`)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("personal_info_identity_section"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Left: Creator Circular Avatar + Bottom-Right Camera Picker Badge
        Box(
          modifier = Modifier
            .size(96.dp)
            .clickable(onClick = launchPhotoPicker)
            .testTag("personal_info_avatar_camera_button")
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_profile_hub_avatar),
            contentDescription = "$fullName Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        // Right: Display Name + Verified Badge, Public User ID, @username, Change Profile Photo Pill
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = fullName,
              color = PersonalInfoTextWhite,
              fontSize = 20.5.sp,
              fontWeight = FontWeight.ExtraBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("personal_info_header_name")
            )
            Box(
              modifier = Modifier
                .size(21.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    colors = listOf(Color(0xFF9333EA), Color(0xFF6366F1))
                  )
                )
                .border(1.dp, Color(0xFFC084FC), CircleShape)
                .testTag("personal_info_verified_badge"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Verified",
                tint = Color.White,
                modifier = Modifier.size(13.dp)
              )
            }
          }

          Text(
            text = publicUid,
            color = PersonalInfoTextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.testTag("personal_info_header_uid")
          )

          Text(
            text = "@$username",
            color = PersonalInfoTextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.testTag("personal_info_header_username")
          )

          Spacer(modifier = Modifier.height(3.dp))

          // `[📷 Change Profile Photo]` Pill Button
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(18.dp))
              .background(Color(0xFF1A1E46))
              .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                  colors = listOf(Color(0xFF9333EA), Color(0xFF6366F1))
                ),
                shape = RoundedCornerShape(18.dp)
              )
              .clickable(onClick = launchPhotoPicker)
              .padding(horizontal = 14.dp, vertical = 6.dp)
              .testTag("personal_info_change_photo_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.CameraAlt,
              contentDescription = null,
              tint = PersonalInfoTextWhite,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Change Profile Photo",
              color = PersonalInfoTextWhite,
              fontSize = 12.2.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Section Card 1: `Personal Details` (`personal_info_details_card`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(PersonalInfoCardSurface)
          .border(
            width = 1.3.dp,
            brush = Brush.verticalGradient(
              colors = listOf(Color(0xFF38BDF8), Color(0xFF6366F1), Color(0xFF4F46E5))
            ),
            shape = RoundedCornerShape(20.dp)
          )
          .padding(horizontal = 12.dp, vertical = 12.dp)
          .testTag("personal_info_details_card"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Section Header: `Personal Details`
        Row(
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.ManageAccounts,
            contentDescription = null,
            tint = Color(0xFFC084FC),
            modifier = Modifier.size(22.dp)
          )
          Text(
            text = "Personal Details",
            color = PersonalInfoTextWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Row 1: Full Name *
        PersonalDetailEditableRow(
          icon = Icons.Outlined.Person,
          label = "Full Name",
          required = true,
          value = fullName,
          testTag = "personal_info_row_full_name",
          onClick = { viewModel.openEditDialog(EditablePersonalField.FULL_NAME) }
        )

        // Row 2: Username *
        PersonalDetailEditableRow(
          icon = Icons.Outlined.AlternateEmail,
          label = "Username",
          required = true,
          value = username,
          testTag = "personal_info_row_username",
          onClick = { viewModel.openEditDialog(EditablePersonalField.USERNAME) }
        )

        // Row 3: Email Address * + [✓ Verified]
        PersonalDetailEditableRow(
          icon = Icons.Outlined.Email,
          label = "Email Address",
          required = true,
          value = email,
          isVerified = isEmailVerified,
          verifiedBadgeTag = "personal_info_email_verified_badge",
          testTag = "personal_info_row_email",
          onClick = { viewModel.openEditDialog(EditablePersonalField.EMAIL) }
        )

        // Row 4: Mobile Number + [✓ Verified]
        PersonalDetailEditableRow(
          icon = Icons.Outlined.Phone,
          label = "Mobile Number",
          required = false,
          value = mobileNumber,
          isVerified = isMobileVerified,
          verifiedBadgeTag = "personal_info_mobile_verified_badge",
          testTag = "personal_info_row_mobile",
          onClick = { viewModel.openEditDialog(EditablePersonalField.MOBILE) }
        )

        // Row 5: Public User ID ⓘ + [📋 Copy]
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PersonalInfoRowBg)
            .border(1.dp, PersonalInfoRowBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 13.dp, vertical = 10.dp)
            .testTag("personal_info_row_public_uid"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Badge,
            contentDescription = null,
            tint = PersonalInfoTextWhite,
            modifier = Modifier.size(23.dp)
          )

          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "Public User ID",
                color = PersonalInfoTextWhite,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
              )
              Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Public User ID Info",
                tint = PersonalInfoTextMuted,
                modifier = Modifier
                  .size(16.dp)
                  .clickable { viewModel.openUidInfoDialog() }
                  .testTag("personal_info_uid_info_icon")
              )
            }
            Text(
              text = publicUid,
              color = PersonalInfoTextMuted,
              fontSize = 13.2.sp,
              fontWeight = FontWeight.Medium
            )
          }

          // `[📋 Copy]` Pill Button
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF1F2352))
              .border(1.dp, Color(0xFF8B5CF6), RoundedCornerShape(16.dp))
              .clickable {
                val uid = viewModel.copyPublicUid()
                runCatching {
                  clipboardManager.setText(AnnotatedString(uid))
                }
              }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("personal_info_copy_uid_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.ContentCopy,
              contentDescription = "Copy Public User ID",
              tint = PersonalInfoTextWhite,
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = "Copy",
              color = PersonalInfoTextWhite,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 4. Section Card 2: `Additional Information` (`personal_info_additional_card`)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(PersonalInfoCardSurface)
          .border(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
              colors = listOf(Color(0xFF6366F1), Color(0xFF4338CA))
            ),
            shape = RoundedCornerShape(20.dp)
          )
          .padding(horizontal = 12.dp, vertical = 12.dp)
          .testTag("personal_info_additional_card"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Section Header: `Additional Information`
        Row(
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Language,
            contentDescription = null,
            tint = Color(0xFFC4B5FD),
            modifier = Modifier.size(21.dp)
          )
          Text(
            text = "Additional Information",
            color = PersonalInfoTextWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Row 1: Date of Birth
        AdditionalInfoRow(
          icon = Icons.Outlined.CalendarToday,
          label = "Date of Birth",
          value = dateOfBirth,
          testTag = "personal_info_row_dob",
          onClick = { viewModel.openEditDialog(EditablePersonalField.DATE_OF_BIRTH) }
        )

        // Row 2: Gender
        AdditionalInfoRow(
          icon = Icons.Outlined.Transgender,
          label = "Gender",
          value = gender,
          testTag = "personal_info_row_gender",
          onClick = { viewModel.openEditDialog(EditablePersonalField.GENDER) }
        )

        // Row 3: Location
        AdditionalInfoRow(
          icon = Icons.Outlined.LocationOn,
          label = "Location",
          value = location,
          testTag = "personal_info_row_location",
          onClick = { viewModel.openEditDialog(EditablePersonalField.LOCATION) }
        )
      }

      // Status / Error Banner
      if (!errorMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = errorMessage!!,
          color = Color(0xFFFF6B7A),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("personal_info_error_text")
        )
      } else if (!statusMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = statusMessage!!,
          color = Color(0xFF47E5BC),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("personal_info_status_text")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 5. Primary CTA Button: `[✎ Save Changes]` (`personal_info_save_button`)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
          .height(54.dp)
          .clip(RoundedCornerShape(27.dp))
          .background(
            Brush.horizontalGradient(
              colors = listOf(
                Color(0xFFD926FF),
                Color(0xFF6D58FF),
                Color(0xFF00E5FF)
              )
            )
          )
          .border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
              colors = listOf(Color(0xFFF098FF), Color(0xFF7CEEFF))
            ),
            shape = RoundedCornerShape(27.dp)
          )
          .clickable {
            viewModel.saveChanges { savedName, savedUser, savedEmail, savedMobile ->
              onChangesSaved(savedName, savedUser, savedEmail, savedMobile)
            }
          }
          .testTag("personal_info_save_button"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(21.dp)
          )
          Text(
            text = "Save Changes",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))
    }
  }

  // Edit Field Modal Dialog
  activeEditField?.let { field ->
    AlertDialog(
      onDismissRequest = { viewModel.dismissEditDialog() },
      containerColor = Color(0xFF15193B),
      title = {
        Text(
          text = field.dialogTitle,
          color = PersonalInfoTextWhite,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier.testTag("personal_info_edit_dialog"),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = editDraftValue,
            onValueChange = { viewModel.onEditDraftChange(it) },
            placeholder = { Text(field.fieldHint, color = PersonalInfoTextMuted) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("personal_info_edit_dialog_input")
          )
          if (!errorMessage.isNullOrBlank()) {
            Text(
              text = errorMessage!!,
              color = Color(0xFFFF6B7A),
              fontSize = 12.sp
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.confirmFieldEdit() },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
          modifier = Modifier.testTag("personal_info_edit_dialog_confirm_button")
        ) {
          Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(
          onClick = { viewModel.dismissEditDialog() },
          modifier = Modifier.testTag("personal_info_edit_dialog_cancel_button")
        ) {
          Text("Cancel", color = PersonalInfoTextMuted)
        }
      }
    )
  }

  // Public User ID Info Dialog
  if (showUidInfoDialog) {
    AlertDialog(
      onDismissRequest = { viewModel.dismissUidInfoDialog() },
      containerColor = Color(0xFF15193B),
      title = {
        Text(
          text = "About Public User ID",
          color = PersonalInfoTextWhite,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = "Your Public User ID ($publicUid) follows the CineFlow AI CF + 8 numeric digits standard. It identifies your creator profile for support and account recovery without exposing your internal Firebase UID.",
          color = PersonalInfoTextMuted,
          fontSize = 13.sp,
          modifier = Modifier.testTag("personal_info_uid_info_dialog")
        )
      },
      confirmButton = {
        Button(
          onClick = { viewModel.dismissUidInfoDialog() },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
          modifier = Modifier.testTag("personal_info_uid_info_dialog_close_button")
        ) {
          Text("Got It", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

@Composable
private fun PersonalDetailEditableRow(
  icon: ImageVector,
  label: String,
  required: Boolean,
  value: String,
  testTag: String,
  isVerified: Boolean = false,
  verifiedBadgeTag: String? = null,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(PersonalInfoRowBg)
      .border(1.dp, PersonalInfoRowBorder, RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 13.dp, vertical = 10.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = PersonalInfoTextWhite,
      modifier = Modifier.size(23.dp)
    )

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = PersonalInfoTextWhite,
              fontWeight = FontWeight.Bold,
              fontSize = 13.5.sp
            )
          ) {
            append(label)
          }
          if (required) {
            withStyle(
              SpanStyle(
                color = PersonalInfoAsteriskRed,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
              )
            ) {
              append(" *")
            }
          }
        }
      )
      Text(
        text = value,
        color = PersonalInfoTextMuted,
        fontSize = 13.2.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    if (isVerified) {
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFF0B3B2E))
          .border(1.dp, Color(0xFF10B981), RoundedCornerShape(14.dp))
          .padding(horizontal = 9.dp, vertical = 4.dp)
          .then(if (verifiedBadgeTag != null) Modifier.testTag(verifiedBadgeTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        Box(
          modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(PersonalInfoVerifiedGreen),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(10.dp)
          )
        }
        Text(
          text = "Verified",
          color = PersonalInfoTextWhite,
          fontSize = 11.2.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
      contentDescription = null,
      tint = Color(0xFFD1D5F0),
      modifier = Modifier.size(20.dp)
    )
  }
}

@Composable
private fun AdditionalInfoRow(
  icon: ImageVector,
  label: String,
  value: String,
  testTag: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(PersonalInfoRowBg)
      .border(1.dp, PersonalInfoRowBorder, RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 13.dp, vertical = 12.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = PersonalInfoTextWhite,
      modifier = Modifier.size(22.dp)
    )

    Text(
      text = label,
      color = PersonalInfoTextWhite,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.weight(1f)
    )

    Text(
      text = value,
      color = PersonalInfoTextWhite,
      fontSize = 13.5.sp,
      fontWeight = FontWeight.Medium
    )

    Icon(
      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
      contentDescription = null,
      tint = Color(0xFFD1D5F0),
      modifier = Modifier.size(20.dp)
    )
  }
}
