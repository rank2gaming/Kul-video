package com.cineflow.ai.data.cloud

import com.cineflow.ai.base.FirestoreEmulatorTestBase
import com.cineflow.ai.core.model.VideoProject
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CloudProjectSyncRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun ensureUserProfile_authenticatedOwner_createsProfileWithCfPublicUid() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = CloudProjectSyncRepository(firestore, auth)

    val profileResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.ensureUserProfile(
        username = "alice_cineflow",
        displayName = "Alice Director",
        email = ALICE_EMAIL
      )
    }
    assertTrue("Expected ensureUserProfile success: ${profileResult.exceptionOrNull()}", profileResult.isSuccess)
    val profile = profileResult.getOrThrow()
    assertEquals(aliceUid, profile.userId)
    assertTrue(profile.publicUid.matches(Regex("^CF[0-9]{8}$")))
    assertEquals("FREE", profile.planTier)
  }

  @Test
  fun syncProjectMetadata_authenticatedOwner_createsAndUpdatesProjectMetadata() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = CloudProjectSyncRepository(firestore, auth)
    val projectId = "proj_${UUID.randomUUID().toString().replace("-", "").take(12)}"
    val now = System.currentTimeMillis()

    val project = VideoProject(
      projectId = projectId,
      name = "Neon Trailer",
      aspectRatio = "16:9",
      durationMicros = 12_000_000L,
      frameRateNum = 60,
      frameRateDen = 1,
      trackCount = 2,
      clipCount = 5,
      missingMediaCount = 0,
      schemaVersion = 1,
      createdAtEpochMillis = now,
      updatedAtEpochMillis = now
    )

    val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.syncProjectMetadata(project)
    }
    assertTrue("Expected syncProjectMetadata create success: ${createResult.exceptionOrNull()}", createResult.isSuccess)

    // Update existing metadata
    val updatedProject = project.copy(name = "Neon Trailer v2", durationMicros = 18_500_000L)
    val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.syncProjectMetadata(updatedProject)
    }
    assertTrue("Expected syncProjectMetadata update success: ${updateResult.exceptionOrNull()}", updateResult.isSuccess)

    val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.getProjectMetadataById(aliceUid, projectId).getOrThrow()
    }
    assertNotNull(fetched)
    assertEquals("Neon Trailer v2", fetched?.name)
    assertEquals(18_500_000L, fetched?.durationMicros)
  }

  @Test
  fun observeCloudProjects_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = CloudProjectSyncRepository(firestore, auth)
    val projectId = "proj_${UUID.randomUUID().toString().replace("-", "").take(12)}"
    val now = System.currentTimeMillis()

    val project = VideoProject(
      projectId = projectId,
      name = "Realtime Sync Cut",
      aspectRatio = "9:16",
      durationMicros = 8_000_000L,
      frameRateNum = 30,
      frameRateDen = 1,
      trackCount = 1,
      clipCount = 2,
      missingMediaCount = 0,
      schemaVersion = 1,
      createdAtEpochMillis = now,
      updatedAtEpochMillis = now
    )

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.syncProjectMetadata(project).getOrThrow()
    }

    val emitted = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeCloudProjects(aliceUid).first { list ->
        list.any { it.projectId == projectId }
      }
    }
    assertTrue(emitted.any { it.projectId == projectId })
  }

  @Test
  fun getProjectMetadataById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = CloudProjectSyncRepository(firestore, auth)
    val projectId = "proj_${UUID.randomUUID().toString().replace("-", "").take(12)}"
    val now = System.currentTimeMillis()

    withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.syncProjectMetadata(
        VideoProject(
          projectId = projectId,
          name = "Alice Private Cut",
          createdAtEpochMillis = now,
          updatedAtEpochMillis = now
        )
      ).getOrThrow()
    }

    signInTestUser(BOB_EMAIL)
    val bobRepo = CloudProjectSyncRepository(firestore, auth)
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        bobRepo.getProjectMetadataById(aliceUid, projectId).getOrThrow()
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED for cross-user access")
    } catch (e: Exception) {
      val firestoreEx = checkNotNull(e.unwrapFirestoreException()) {
        "Expected FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }
  }

  @Test
  fun observeCloudProjects_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    auth.signOut()
    val repository = CloudProjectSyncRepository(firestore, auth)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeCloudProjects(aliceUid).first()
      }
      fail("Expected PERMISSION_DENIED when unauthenticated")
    } catch (e: Exception) {
      val firestoreEx = checkNotNull(e.unwrapFirestoreException()) {
        "Expected nested FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice_cineflow@test.com"
    const val BOB_EMAIL = "bob_cineflow@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
