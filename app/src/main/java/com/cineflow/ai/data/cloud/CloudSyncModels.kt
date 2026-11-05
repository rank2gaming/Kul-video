package com.cineflow.ai.data.cloud

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val publicUid: String = "",
  val username: String = "",
  val displayName: String? = null,
  val email: String? = null,
  val photoUrl: String? = null,
  val planTier: String = "FREE",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudProjectMetadata(
  @DocumentId val id: String = "",
  val userId: String = "",
  val projectId: String = "",
  val name: String = "",
  val aspectRatio: String = "16:9",
  val durationMicros: Long = 0L,
  val frameRateNum: Long = 30L,
  val frameRateDen: Long = 1L,
  val trackCount: Long = 1L,
  val clipCount: Long = 0L,
  val missingMediaCount: Long = 0L,
  val schemaVersion: Long = 1L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
