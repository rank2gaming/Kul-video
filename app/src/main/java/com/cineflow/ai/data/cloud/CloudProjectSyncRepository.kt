package com.cineflow.ai.data.cloud

import android.content.Context
import com.cineflow.ai.R
import com.cineflow.ai.core.firebase.OperationType
import com.cineflow.ai.core.firebase.handleFirestoreError
import com.cineflow.ai.core.model.VideoProject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Cloud synchronization repository for CineFlow AI.
 * Strictly synchronizes project metadata ONLY (never uploads source video/audio/photo files).
 * Requires authenticated user session for all operations (Section 3.H & 11).
 */
class CloudProjectSyncRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth
) {
  // CRITICAL: Always initialize with R.string.firestore_database_id
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  /**
   * Generates a deterministic 8-digit Public User ID (`CF` + 8 digits) from the server UID hash
   * without exposing the raw Firebase UID.
   */
  fun formatPublicUid(uid: String): String {
    val numeric = (abs(uid.hashCode().toLong()) % 99_999_999L) + 1L
    return String.format(Locale.US, "CF%08d", numeric)
  }

  suspend fun ensureUserProfile(
    username: String,
    displayName: String? = null,
    email: String? = null,
    photoUrl: String? = null
  ): Result<UserProfile> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    try {
      val existingSnap = docRef.get().await()
      if (existingSnap.exists()) {
        val profile = existingSnap.toObject(
          UserProfile::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        )
        if (profile != null) return@runCatching profile
      }

      val cleanUsername = username.trim().take(30).let {
        if (it.length < 3) "creator_${uid.take(5)}" else it
      }
      val publicUid = formatPublicUid(uid)
      val payload = mapOf(
        "userId" to uid,
        "publicUid" to publicUid,
        "username" to cleanUsername,
        "displayName" to displayName?.take(80),
        "email" to email?.take(120),
        "photoUrl" to photoUrl?.take(500),
        "planTier" to "FREE",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      ).filterValues { it != null }

      docRef.set(payload).await()
      val createdSnap = docRef.get().await()
      checkNotNull(
        createdSnap.toObject(
          UserProfile::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        )
      ) { "Created UserProfile could not be deserialized" }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun syncProjectMetadata(project: VideoProject): Result<String> = runCatching {
    val uid = requireUserId()
    require(project.projectId.isNotBlank()) { "Project ID must not be blank" }
    val docRef = db.collection("users").document(uid)
      .collection("projects").document(project.projectId)

    try {
      val existing = docRef.get().await()
      if (existing.exists()) {
        val updatePayload = mapOf(
          "name" to project.name.take(80),
          "aspectRatio" to project.aspectRatio.take(20),
          "durationMicros" to project.durationMicros.coerceAtLeast(0L),
          "frameRateNum" to project.frameRateNum.coerceIn(1, 240).toLong(),
          "frameRateDen" to project.frameRateDen.coerceIn(1, 1001).toLong(),
          "trackCount" to project.trackCount.coerceIn(0, 100).toLong(),
          "clipCount" to project.clipCount.coerceIn(0, 5000).toLong(),
          "missingMediaCount" to project.missingMediaCount.coerceIn(0, 5000).toLong(),
          "schemaVersion" to project.schemaVersion.coerceIn(1, 100).toLong(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.update(updatePayload).await()
      } else {
        val createPayload = mapOf(
          "userId" to uid,
          "projectId" to project.projectId,
          "name" to project.name.take(80),
          "aspectRatio" to project.aspectRatio.take(20),
          "durationMicros" to project.durationMicros.coerceAtLeast(0L),
          "frameRateNum" to project.frameRateNum.coerceIn(1, 240).toLong(),
          "frameRateDen" to project.frameRateDen.coerceIn(1, 1001).toLong(),
          "trackCount" to project.trackCount.coerceIn(0, 100).toLong(),
          "clipCount" to project.clipCount.coerceIn(0, 5000).toLong(),
          "missingMediaCount" to project.missingMediaCount.coerceIn(0, 5000).toLong(),
          "schemaVersion" to project.schemaVersion.coerceIn(1, 100).toLong(),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(createPayload).await()
      }
      project.projectId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun getProjectMetadataById(
    ownerUserId: String,
    projectId: String
  ): Result<CloudProjectMetadata?> = runCatching {
    val docRef = db.collection("users").document(ownerUserId)
      .collection("projects").document(projectId)
    try {
      val snap = docRef.get().await()
      snap.toObject(
        CloudProjectMetadata::class.java,
        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      throw e
    }
  }

  fun observeCloudProjects(userId: String): Flow<List<CloudProjectMetadata>> = flow {
    val path = "users/$userId/projects"
    emitAll(
      db.collection("users").document(userId).collection("projects")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            CloudProjectMetadata::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
          )
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }
}
