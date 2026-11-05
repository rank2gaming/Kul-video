package com.cineflow.ai.core.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.cineflow.ai.core.model.TimelineClipItem
import com.cineflow.ai.core.model.TrackType
import com.cineflow.ai.core.model.VideoProject
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "local_projects")
data class LocalProjectEntity(
  @PrimaryKey val projectId: String,
  val name: String,
  val aspectRatio: String,
  val durationMicros: Long,
  val frameRateNum: Int,
  val frameRateDen: Int,
  val resolutionLabel: String = "1080p",
  val statusTag: String = "Draft",
  val isFavorite: Boolean = false,
  val visualThemeIndex: Int = 0,
  val trackCount: Int,
  val clipCount: Int,
  val missingMediaCount: Int,
  val schemaVersion: Int,
  val createdAtEpochMillis: Long,
  val updatedAtEpochMillis: Long
) {
  fun toDomain(): VideoProject = VideoProject(
    projectId = projectId,
    name = name,
    aspectRatio = aspectRatio,
    durationMicros = durationMicros,
    frameRateNum = frameRateNum,
    frameRateDen = frameRateDen,
    resolutionLabel = resolutionLabel,
    statusTag = statusTag,
    isFavorite = isFavorite,
    visualThemeIndex = visualThemeIndex,
    trackCount = trackCount,
    clipCount = clipCount,
    missingMediaCount = missingMediaCount,
    schemaVersion = schemaVersion,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
  )

  companion object {
    fun fromDomain(project: VideoProject): LocalProjectEntity = LocalProjectEntity(
      projectId = project.projectId,
      name = project.name,
      aspectRatio = project.aspectRatio,
      durationMicros = project.durationMicros,
      frameRateNum = project.frameRateNum,
      frameRateDen = project.frameRateDen,
      resolutionLabel = project.resolutionLabel,
      statusTag = project.statusTag,
      isFavorite = project.isFavorite,
      visualThemeIndex = project.visualThemeIndex,
      trackCount = project.trackCount,
      clipCount = project.clipCount,
      missingMediaCount = project.missingMediaCount,
      schemaVersion = project.schemaVersion,
      createdAtEpochMillis = project.createdAtEpochMillis,
      updatedAtEpochMillis = project.updatedAtEpochMillis
    )
  }
}

@Entity(tableName = "local_timeline_clips")
data class LocalTimelineClipEntity(
  @PrimaryKey val clipId: String,
  val projectId: String,
  val trackTypeName: String,
  val trackIndex: Int,
  val label: String,
  val mediaUri: String?,
  val mimeType: String,
  val sourceDurationMicros: Long,
  val sourceInMicros: Long,
  val sourceOutMicros: Long,
  val timelineStartMicros: Long,
  val speedMultiplier: Float,
  val volume: Float,
  val isMuted: Boolean,
  val rotationDegrees: Int,
  val isMirrored: Boolean,
  val overlayText: String?,
  val overlayNormalizedX: Float,
  val overlayNormalizedY: Float,
  val overlayScale: Float,
  val transitionAfter: String?,
  val effectPreset: String?,
  val filterPreset: String?,
  val orderIndex: Int
) {
  fun toDomain(): TimelineClipItem = TimelineClipItem(
    clipId = clipId,
    projectId = projectId,
    trackType = runCatching { TrackType.valueOf(trackTypeName) }.getOrDefault(TrackType.MAIN_VIDEO),
    trackIndex = trackIndex,
    label = label,
    mediaUri = mediaUri,
    mimeType = mimeType,
    sourceDurationMicros = sourceDurationMicros,
    sourceInMicros = sourceInMicros,
    sourceOutMicros = sourceOutMicros,
    timelineStartMicros = timelineStartMicros,
    speedMultiplier = speedMultiplier,
    volume = volume,
    isMuted = isMuted,
    rotationDegrees = rotationDegrees,
    isMirrored = isMirrored,
    overlayText = overlayText,
    overlayNormalizedX = overlayNormalizedX,
    overlayNormalizedY = overlayNormalizedY,
    overlayScale = overlayScale,
    transitionAfter = transitionAfter,
    effectPreset = effectPreset,
    filterPreset = filterPreset,
    orderIndex = orderIndex
  )

  companion object {
    fun fromDomain(item: TimelineClipItem): LocalTimelineClipEntity = LocalTimelineClipEntity(
      clipId = item.clipId,
      projectId = item.projectId,
      trackTypeName = item.trackType.name,
      trackIndex = item.trackIndex,
      label = item.label,
      mediaUri = item.mediaUri,
      mimeType = item.mimeType,
      sourceDurationMicros = item.sourceDurationMicros,
      sourceInMicros = item.sourceInMicros,
      sourceOutMicros = item.sourceOutMicros,
      timelineStartMicros = item.timelineStartMicros,
      speedMultiplier = item.speedMultiplier,
      volume = item.volume,
      isMuted = item.isMuted,
      rotationDegrees = item.rotationDegrees,
      isMirrored = item.isMirrored,
      overlayText = item.overlayText,
      overlayNormalizedX = item.overlayNormalizedX,
      overlayNormalizedY = item.overlayNormalizedY,
      overlayScale = item.overlayScale,
      transitionAfter = item.transitionAfter,
      effectPreset = item.effectPreset,
      filterPreset = item.filterPreset,
      orderIndex = item.orderIndex
    )
  }
}

@Dao
interface LocalProjectDao {
  @Query("SELECT * FROM local_projects ORDER BY updatedAtEpochMillis DESC")
  fun observeAllProjects(): Flow<List<LocalProjectEntity>>

  @Query("SELECT * FROM local_projects ORDER BY updatedAtEpochMillis DESC")
  suspend fun getAllProjectsSnapshot(): List<LocalProjectEntity>

  @Query("SELECT * FROM local_projects WHERE projectId = :projectId LIMIT 1")
  suspend fun getProjectById(projectId: String): LocalProjectEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertProject(project: LocalProjectEntity)

  @Update
  suspend fun updateProject(project: LocalProjectEntity)

  @Query("DELETE FROM local_projects WHERE projectId = :projectId")
  suspend fun deleteProjectById(projectId: String)

  @Query("SELECT * FROM local_timeline_clips WHERE projectId = :projectId ORDER BY orderIndex ASC, timelineStartMicros ASC")
  fun observeClipsForProject(projectId: String): Flow<List<LocalTimelineClipEntity>>

  @Query("SELECT * FROM local_timeline_clips WHERE projectId = :projectId ORDER BY orderIndex ASC, timelineStartMicros ASC")
  suspend fun getClipsForProjectSnapshot(projectId: String): List<LocalTimelineClipEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertClips(clips: List<LocalTimelineClipEntity>)

  @Query("DELETE FROM local_timeline_clips WHERE projectId = :projectId")
  suspend fun deleteAllClipsForProject(projectId: String)
}

@Database(
  entities = [LocalProjectEntity::class, LocalTimelineClipEntity::class],
  version = 3,
  exportSchema = false
)
abstract class CineFlowDatabase : RoomDatabase() {
  abstract fun localProjectDao(): LocalProjectDao

  companion object {
    @Volatile
    private var INSTANCE: CineFlowDatabase? = null

    fun getInstance(context: Context): CineFlowDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          CineFlowDatabase::class.java,
          "cineflow_local.db"
        )
          .fallbackToDestructiveMigration(dropAllTables = true)
          .build()
          .also { INSTANCE = it }
      }
    }
  }
}

