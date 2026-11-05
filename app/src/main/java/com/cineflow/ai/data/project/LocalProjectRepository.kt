package com.cineflow.ai.data.project

import com.cineflow.ai.core.database.LocalProjectDao
import com.cineflow.ai.core.database.LocalProjectEntity
import com.cineflow.ai.core.database.LocalTimelineClipEntity
import com.cineflow.ai.core.model.TimelineClipItem
import com.cineflow.ai.core.model.VideoProject
import java.util.UUID
import kotlin.math.abs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Offline-first repository for local CineFlow AI projects backed by Room/SQLite.
 * Enforces non-blank project names, consistent duplicate-name resolution, and stable project IDs (Section 9 & 11).
 * Supports Create, Open, Search, Sort, Rename, Duplicate, Delete, Favorite, Timeline Clip persistence, and Backup status tracking.
 */
class LocalProjectRepository(
  private val projectDao: LocalProjectDao
) {
  val projectsFlow: Flow<List<VideoProject>> = projectDao.observeAllProjects()
    .map { entities -> entities.map { it.toDomain() } }

  fun observeClipsForProject(projectId: String): Flow<List<TimelineClipItem>> {
    return projectDao.observeClipsForProject(projectId)
      .map { entities -> entities.map { it.toDomain() } }
  }

  suspend fun getClipsForProjectSnapshot(projectId: String): List<TimelineClipItem> {
    return projectDao.getClipsForProjectSnapshot(projectId).map { it.toDomain() }
  }

  suspend fun getProjectById(projectId: String): VideoProject? {
    return projectDao.getProjectById(projectId)?.toDomain()
  }

  suspend fun createProject(
    rawName: String,
    aspectRatio: String = "16:9",
    frameRateNum: Int = 30,
    frameRateDen: Int = 1,
    resolutionLabel: String = "1080p",
    durationMicros: Long = 0L,
    statusTag: String = "Draft",
    isFavorite: Boolean = false
  ): Result<VideoProject> = runCatching {
    val trimmed = rawName.trim()
    require(trimmed.isNotEmpty()) { "Project name cannot be blank." }
    require(trimmed.length <= 100) { "Project name cannot exceed 100 characters." }

    val existingSnapshot = projectDao.getAllProjectsSnapshot()
    val existingNames = existingSnapshot
      .map { it.name.lowercase() }
      .toSet()

    val resolvedName = resolveUniqueProjectName(trimmed, existingNames)
    val now = System.currentTimeMillis()
    val projectId = "proj_${UUID.randomUUID().toString().replace("-", "").take(16)}"
    val themeIndex = abs(resolvedName.hashCode()) % 5

    val project = VideoProject(
      projectId = projectId,
      name = resolvedName,
      aspectRatio = aspectRatio,
      durationMicros = durationMicros.coerceAtLeast(0L),
      frameRateNum = frameRateNum,
      frameRateDen = frameRateDen,
      resolutionLabel = resolutionLabel,
      statusTag = statusTag,
      isFavorite = isFavorite,
      visualThemeIndex = themeIndex,
      trackCount = 1,
      clipCount = 0,
      missingMediaCount = 0,
      schemaVersion = 1,
      createdAtEpochMillis = now,
      updatedAtEpochMillis = now
    )
    projectDao.upsertProject(LocalProjectEntity.fromDomain(project))
    project
  }

  suspend fun saveProjectTimelineState(
    projectId: String,
    clips: List<TimelineClipItem>,
    aspectRatio: String? = null,
    resolutionLabel: String? = null,
    frameRateNum: Int? = null,
    statusTag: String? = null
  ): Result<VideoProject> = runCatching {
    val existingEntity = projectDao.getProjectById(projectId)
      ?: throw IllegalArgumentException("Project not found: $projectId")

    projectDao.deleteAllClipsForProject(projectId)
    if (clips.isNotEmpty()) {
      projectDao.upsertClips(clips.map { LocalTimelineClipEntity.fromDomain(it) })
    }

    val totalDurationMicros = clips.maxOfOrNull { it.timelineEndMicros }?.coerceAtLeast(0L) ?: 0L
    val distinctTrackCount = maxOf(
      1,
      clips.map { it.trackType to it.trackIndex }.distinct().size
    )
    val now = maxOf(System.currentTimeMillis(), existingEntity.updatedAtEpochMillis + 1L)
    val nextStatus = statusTag
      ?: if (clips.isNotEmpty() && existingEntity.statusTag == "Draft") "Edited" else existingEntity.statusTag

    val updatedDomain = existingEntity.toDomain().copy(
      aspectRatio = aspectRatio ?: existingEntity.aspectRatio,
      resolutionLabel = resolutionLabel ?: existingEntity.resolutionLabel,
      frameRateNum = frameRateNum ?: existingEntity.frameRateNum,
      durationMicros = totalDurationMicros,
      clipCount = clips.size,
      trackCount = distinctTrackCount,
      statusTag = nextStatus,
      updatedAtEpochMillis = now
    )
    projectDao.upsertProject(LocalProjectEntity.fromDomain(updatedDomain))
    updatedDomain
  }

  suspend fun renameProject(
    projectId: String,
    newRawName: String
  ): Result<VideoProject> = runCatching {
    val trimmed = newRawName.trim()
    require(trimmed.isNotEmpty()) { "Project name cannot be blank." }
    require(trimmed.length <= 100) { "Project name cannot exceed 100 characters." }

    val existingEntity = projectDao.getProjectById(projectId)
      ?: throw IllegalArgumentException("Project not found: $projectId")

    val otherLowercaseNames = projectDao.getAllProjectsSnapshot()
      .filter { it.projectId != projectId }
      .map { it.name.lowercase() }
      .toSet()

    val resolvedName = resolveUniqueProjectName(trimmed, otherLowercaseNames)
    val now = maxOf(System.currentTimeMillis(), existingEntity.updatedAtEpochMillis + 1L)
    val updatedStatus = if (existingEntity.statusTag == "Draft") "Edited" else existingEntity.statusTag

    val updatedDomain = existingEntity.toDomain().copy(
      name = resolvedName,
      statusTag = updatedStatus,
      updatedAtEpochMillis = now
    )
    projectDao.upsertProject(LocalProjectEntity.fromDomain(updatedDomain))
    updatedDomain
  }

  suspend fun duplicateProject(projectId: String): Result<VideoProject> = runCatching {
    val sourceEntity = projectDao.getProjectById(projectId)
      ?: throw IllegalArgumentException("Project not found: $projectId")
    val source = sourceEntity.toDomain()

    val existingNames = projectDao.getAllProjectsSnapshot()
      .map { it.name.lowercase() }
      .toSet()

    val resolvedCopyName = resolveUniqueProjectName(source.name, existingNames)
    val now = maxOf(System.currentTimeMillis(), source.updatedAtEpochMillis + 1L)
    val copyId = "proj_${UUID.randomUUID().toString().replace("-", "").take(16)}"

    val duplicated = source.copy(
      projectId = copyId,
      name = resolvedCopyName,
      statusTag = if (source.statusTag == "Backed Up") "Edited" else source.statusTag,
      createdAtEpochMillis = now,
      updatedAtEpochMillis = now
    )
    projectDao.upsertProject(LocalProjectEntity.fromDomain(duplicated))

    val sourceClips = projectDao.getClipsForProjectSnapshot(projectId)
    if (sourceClips.isNotEmpty()) {
      val copiedClips = sourceClips.map { clipEntity ->
        clipEntity.copy(
          clipId = "clip_${UUID.randomUUID().toString().replace("-", "").take(12)}",
          projectId = copyId
        )
      }
      projectDao.upsertClips(copiedClips)
    }
    duplicated
  }

  suspend fun toggleFavorite(projectId: String): Result<VideoProject> = runCatching {
    val existingEntity = projectDao.getProjectById(projectId)
      ?: throw IllegalArgumentException("Project not found: $projectId")
    val updated = existingEntity.toDomain().copy(
      isFavorite = !existingEntity.isFavorite
    )
    projectDao.upsertProject(LocalProjectEntity.fromDomain(updated))
    updated
  }

  suspend fun markProjectStatus(
    projectId: String,
    statusTag: String
  ): Result<VideoProject> = runCatching {
    val existingEntity = projectDao.getProjectById(projectId)
      ?: throw IllegalArgumentException("Project not found: $projectId")
    val now = maxOf(System.currentTimeMillis(), existingEntity.updatedAtEpochMillis + 1L)
    val updated = existingEntity.toDomain().copy(
      statusTag = statusTag,
      updatedAtEpochMillis = now
    )
    projectDao.upsertProject(LocalProjectEntity.fromDomain(updated))
    updated
  }

  suspend fun deleteProject(projectId: String) {
    projectDao.deleteAllClipsForProject(projectId)
    projectDao.deleteProjectById(projectId)
  }

  /**
   * Seeds the two owner-requested multi-clip demo projects (one 16:9 landscape project
   * and one 9:16 portrait project) if the local database has no projects yet, so the owner
   * can immediately inspect both 16:9 and 9:16 multi-clip Video Editor layouts from
   * Project Management.
   */
  suspend fun seedDemoProjectsIfEmpty() {
    val existing = projectDao.getAllProjectsSnapshot()
    if (existing.isNotEmpty()) return

    val now = System.currentTimeMillis()
    val uri16x9 = "android.resource://com.cineflow.ai/drawable/img_demo_video_16_9"
    val uri9x16 = "android.resource://com.cineflow.ai/drawable/img_demo_video_9_16"

    // 1. 16:9 Landscape Multi-Clip Demo Project (3 clips totaling 00:17, matching Reference 1)
    val proj16x9Id = "proj_demo_16_9"
    val proj16x9 = VideoProject(
      projectId = proj16x9Id,
      name = "Garden Nature (16:9 Demo)",
      aspectRatio = "16:9",
      durationMicros = 17_000_000L,
      frameRateNum = 30,
      frameRateDen = 1,
      resolutionLabel = "720p",
      statusTag = "Edited",
      isFavorite = true,
      visualThemeIndex = 0,
      trackCount = 1,
      clipCount = 3,
      missingMediaCount = 0,
      schemaVersion = 1,
      createdAtEpochMillis = now - 60_000L,
      updatedAtEpochMillis = now
    )
    val clips16x9 = listOf(
      TimelineClipItem(
        clipId = "clip_16_9_1",
        projectId = proj16x9Id,
        trackType = com.cineflow.ai.core.model.TrackType.MAIN_VIDEO,
        trackIndex = 0,
        label = "Garden Scene 1",
        mediaUri = uri16x9,
        sourceDurationMicros = 6_000_000L,
        sourceInMicros = 0L,
        sourceOutMicros = 6_000_000L,
        timelineStartMicros = 0L,
        orderIndex = 0
      ),
      TimelineClipItem(
        clipId = "clip_16_9_2",
        projectId = proj16x9Id,
        trackType = com.cineflow.ai.core.model.TrackType.MAIN_VIDEO,
        trackIndex = 0,
        label = "Garden Scene 2",
        mediaUri = uri16x9,
        sourceDurationMicros = 6_000_000L,
        sourceInMicros = 0L,
        sourceOutMicros = 6_000_000L,
        timelineStartMicros = 6_000_000L,
        orderIndex = 1
      ),
      TimelineClipItem(
        clipId = "clip_16_9_3",
        projectId = proj16x9Id,
        trackType = com.cineflow.ai.core.model.TrackType.MAIN_VIDEO,
        trackIndex = 0,
        label = "Garden Scene 3",
        mediaUri = uri16x9,
        sourceDurationMicros = 5_000_000L,
        sourceInMicros = 0L,
        sourceOutMicros = 5_000_000L,
        timelineStartMicros = 12_000_000L,
        orderIndex = 2
      )
    )

    // 2. 9:16 Portrait Multi-Clip Demo Project (3 clips totaling 00:30, matching Reference 2)
    val proj9x16Id = "proj_demo_9_16"
    val proj9x16 = VideoProject(
      projectId = proj9x16Id,
      name = "Golden Dog & Parrot (9:16 Demo)",
      aspectRatio = "9:16",
      durationMicros = 30_000_000L,
      frameRateNum = 30,
      frameRateDen = 1,
      resolutionLabel = "720p",
      statusTag = "Edited",
      isFavorite = true,
      visualThemeIndex = 3,
      trackCount = 1,
      clipCount = 3,
      missingMediaCount = 0,
      schemaVersion = 1,
      createdAtEpochMillis = now - 120_000L,
      updatedAtEpochMillis = now - 30_000L
    )
    val clips9x16 = listOf(
      TimelineClipItem(
        clipId = "clip_9_16_1",
        projectId = proj9x16Id,
        trackType = com.cineflow.ai.core.model.TrackType.MAIN_VIDEO,
        trackIndex = 0,
        label = "Parrot & Dog 1",
        mediaUri = uri9x16,
        sourceDurationMicros = 10_000_000L,
        sourceInMicros = 0L,
        sourceOutMicros = 10_000_000L,
        timelineStartMicros = 0L,
        orderIndex = 0
      ),
      TimelineClipItem(
        clipId = "clip_9_16_2",
        projectId = proj9x16Id,
        trackType = com.cineflow.ai.core.model.TrackType.MAIN_VIDEO,
        trackIndex = 0,
        label = "Parrot & Dog 2",
        mediaUri = uri9x16,
        sourceDurationMicros = 10_000_000L,
        sourceInMicros = 0L,
        sourceOutMicros = 10_000_000L,
        timelineStartMicros = 10_000_000L,
        orderIndex = 1
      ),
      TimelineClipItem(
        clipId = "clip_9_16_3",
        projectId = proj9x16Id,
        trackType = com.cineflow.ai.core.model.TrackType.MAIN_VIDEO,
        trackIndex = 0,
        label = "Parrot & Dog 3",
        mediaUri = uri9x16,
        sourceDurationMicros = 10_000_000L,
        sourceInMicros = 0L,
        sourceOutMicros = 10_000_000L,
        timelineStartMicros = 20_000_000L,
        orderIndex = 2
      )
    )

    projectDao.upsertProject(LocalProjectEntity.fromDomain(proj16x9))
    projectDao.upsertClips(clips16x9.map { LocalTimelineClipEntity.fromDomain(it) })

    projectDao.upsertProject(LocalProjectEntity.fromDomain(proj9x16))
    projectDao.upsertClips(clips9x16.map { LocalTimelineClipEntity.fromDomain(it) })
  }

  internal fun resolveUniqueProjectName(
    desiredName: String,
    existingLowercaseNames: Set<String>
  ): String {
    if (desiredName.lowercase() !in existingLowercaseNames) {
      return desiredName
    }
    var suffix = 2
    while (true) {
      val candidate = "$desiredName ($suffix)"
      if (candidate.lowercase() !in existingLowercaseNames) {
        return candidate
      }
      suffix++
    }
  }
}

