package com.cineflow.ai.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cineflow.ai.core.model.EditorMainCategory
import com.cineflow.ai.core.model.ExportResolutionCap
import com.cineflow.ai.core.model.TimelineClipItem
import com.cineflow.ai.core.model.TrackType
import com.cineflow.ai.core.model.VideoProject
import com.cineflow.ai.data.project.LocalProjectRepository
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditorHistorySnapshot(
  val clips: List<TimelineClipItem>,
  val aspectRatio: String,
  val resolutionLabel: String,
  val frameRateNum: Int,
  val isMainTrackMuted: Boolean
)

data class ProbedMediaClip(
  val uri: String,
  val label: String,
  val durationMicros: Long,
  val widthPx: Int = 1920,
  val heightPx: Int = 1080
)

/**
 * ViewModel for the CineFlow AI Video Editor Workspace (`17_Video_Editor_Dashboard.jpg`).
 *
 * Strictly enforces:
 * - Default 16:9 horizontal preview canvas when the project timeline is empty.
 * - Zero demo videos, zero upload buttons/badges inside the preview canvas.
 * - Automatic aspect ratio detection (9:16 vs 16:9, etc.) when the user uploads a video/photo clip
 *   via the fixed right-side `+` button on the timeline.
 * - 64-bit microsecond canonical timebase (Section 16).
 * - Undo/Redo history snapshots and Room persistence (`CineFlowDatabase`).
 */
class VideoEditorViewModel(
  private val repository: LocalProjectRepository,
  initialProject: VideoProject
) : ViewModel() {

  private val _project = MutableStateFlow(
    if (initialProject.clipCount == 0) {
      initialProject.copy(aspectRatio = "16:9")
    } else {
      initialProject
    }
  )
  val project: StateFlow<VideoProject> = _project.asStateFlow()

  private val _clips = MutableStateFlow<List<TimelineClipItem>>(emptyList())
  val clips: StateFlow<List<TimelineClipItem>> = _clips.asStateFlow()

  private val _selectedClipId = MutableStateFlow<String?>(null)
  val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

  private val _activeCategory = MutableStateFlow<EditorMainCategory?>(null)
  val activeCategory: StateFlow<EditorMainCategory?> = _activeCategory.asStateFlow()

  private val _playheadMicros = MutableStateFlow(0L)
  val playheadMicros: StateFlow<Long> = _playheadMicros.asStateFlow()

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _isClipConfinementOn = MutableStateFlow(true)
  val isClipConfinementOn: StateFlow<Boolean> = _isClipConfinementOn.asStateFlow()

  private val _isMainTrackMuted = MutableStateFlow(false)
  val isMainTrackMuted: StateFlow<Boolean> = _isMainTrackMuted.asStateFlow()

  private val _isFullscreenPreview = MutableStateFlow(false)
  val isFullscreenPreview: StateFlow<Boolean> = _isFullscreenPreview.asStateFlow()

  private val _timelineZoomScale = MutableStateFlow(1.0f)
  val timelineZoomScale: StateFlow<Float> = _timelineZoomScale.asStateFlow()

  private val _coverUri = MutableStateFlow<String?>(null)
  val coverUri: StateFlow<String?> = _coverUri.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private val _canUndo = MutableStateFlow(false)
  val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

  private val _canRedo = MutableStateFlow(false)
  val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

  private val undoStack = ArrayDeque<EditorHistorySnapshot>()
  private val redoStack = ArrayDeque<EditorHistorySnapshot>()
  private var playbackJob: Job? = null

  init {
    viewModelScope.launch {
      val persistedClips = repository.getClipsForProjectSnapshot(initialProject.projectId)
      val persistedProject = repository.getProjectById(initialProject.projectId)
      if (persistedProject != null) {
        val initialRatio = if (persistedClips.isEmpty()) "16:9" else persistedProject.aspectRatio
        _project.value = persistedProject.copy(aspectRatio = initialRatio)
      }
      val normalized = recalculateMainTrackStarts(persistedClips)
      _clips.value = normalized
      if (_coverUri.value == null) {
        _coverUri.value = normalized.firstOrNull { it.trackType == TrackType.MAIN_VIDEO }?.mediaUri
      }
    }
  }

  val totalDurationMicros: Long
    get() = _clips.value.maxOfOrNull { it.timelineEndMicros }?.coerceAtLeast(0L) ?: 0L

  fun selectClip(clipId: String?) {
    _selectedClipId.value = if (_selectedClipId.value == clipId) null else clipId
    val selected = _clips.value.firstOrNull { it.clipId == _selectedClipId.value }
    if (selected != null && _isClipConfinementOn.value) {
      _playheadMicros.value = _playheadMicros.value.coerceIn(
        selected.timelineStartMicros,
        selected.timelineEndMicros
      )
    }
  }

  fun selectMainCategory(category: EditorMainCategory?) {
    _activeCategory.value = if (_activeCategory.value == category) null else category
  }

  fun toggleFullscreenPreview() {
    _isFullscreenPreview.value = !_isFullscreenPreview.value
  }

  fun toggleClipConfinement() {
    _isClipConfinementOn.value = !_isClipConfinementOn.value
  }

  fun toggleMuteClipOrTrack() {
    pushUndoSnapshot()
    val selectedId = _selectedClipId.value
    if (selectedId != null) {
      val updated = _clips.value.map { clip ->
        if (clip.clipId == selectedId) clip.copy(isMuted = !clip.isMuted) else clip
      }
      applyAndPersistClips(updated)
    } else {
      _isMainTrackMuted.value = !_isMainTrackMuted.value
      val updated = _clips.value.map { clip ->
        if (clip.trackType == TrackType.MAIN_VIDEO) {
          clip.copy(isMuted = _isMainTrackMuted.value)
        } else {
          clip
        }
      }
      applyAndPersistClips(updated)
    }
  }

  fun setCoverUri(uri: String?) {
    _coverUri.value = uri
    _statusMessage.value = if (uri != null) "Project cover updated." else null
  }

  fun setTimelineZoom(scale: Float) {
    _timelineZoomScale.value = scale.coerceIn(0.5f, 3.0f)
  }

  fun seekToMicros(targetMicros: Long) {
    val maxDuration = totalDurationMicros
    val selected = _clips.value.firstOrNull { it.clipId == _selectedClipId.value }
    val clamped = if (selected != null && _isClipConfinementOn.value) {
      targetMicros.coerceIn(selected.timelineStartMicros, selected.timelineEndMicros)
    } else {
      targetMicros.coerceIn(0L, maxDuration)
    }
    _playheadMicros.value = clamped
  }

  fun togglePlayPause() {
    if (_isPlaying.value) {
      pausePlayback()
    } else {
      startPlayback()
    }
  }

  fun pausePlayback() {
    _isPlaying.value = false
    playbackJob?.cancel()
    playbackJob = null
  }

  private fun startPlayback() {
    val maxDuration = totalDurationMicros
    if (maxDuration <= 0L) {
      return
    }
    val selected = _clips.value.firstOrNull { it.clipId == _selectedClipId.value }
    val startBound = if (selected != null && _isClipConfinementOn.value) selected.timelineStartMicros else 0L
    val endBound = if (selected != null && _isClipConfinementOn.value) selected.timelineEndMicros else maxDuration

    if (_playheadMicros.value >= endBound || _playheadMicros.value < startBound) {
      _playheadMicros.value = startBound
    }

    _isPlaying.value = true
    playbackJob?.cancel()
    playbackJob = viewModelScope.launch {
      val stepMicros = 33_333L
      while (_isPlaying.value) {
        delay(33L)
        val next = _playheadMicros.value + stepMicros
        if (next >= endBound) {
          _playheadMicros.value = endBound
          _isPlaying.value = false
          break
        } else {
          _playheadMicros.value = next
        }
      }
    }
  }

  fun setAspectRatio(newAspectRatio: String) {
    if (newAspectRatio !in APPROVED_ASPECT_RATIOS) return
    if (_project.value.aspectRatio == newAspectRatio) return
    pushUndoSnapshot()
    val updatedProject = _project.value.copy(aspectRatio = newAspectRatio)
    _project.value = updatedProject
    persistCurrentTimelineState()
  }

  fun setResolutionAndFps(
    resolutionCap: ExportResolutionCap,
    fps: Int,
    isAuthenticated: Boolean
  ): Boolean {
    if (!ExportResolutionCap.isAllowedForSession(resolutionCap, isAuthenticated)) {
      _statusMessage.value = "${resolutionCap.label} export requires Google Sign-In. Unsigned maximum is 720p."
      return false
    }
    val clampedFps = fps.coerceIn(25, 240)
    pushUndoSnapshot()
    val shortLabel = when (resolutionCap) {
      ExportResolutionCap.SD_480P -> "480p"
      ExportResolutionCap.HD_720P -> "720p"
      ExportResolutionCap.FHD_1080P -> "1080p"
      ExportResolutionCap.QHD_2K -> "2K"
      ExportResolutionCap.UHD_4K -> "4K"
      ExportResolutionCap.UHD_8K -> "8K"
    }
    _project.value = _project.value.copy(
      resolutionLabel = shortLabel,
      frameRateNum = clampedFps,
      frameRateDen = 1
    )
    persistCurrentTimelineState()
    return true
  }

  /**
   * Imports real user-selected media clips into the Main Video Track and automatically
   * detects the video's aspect ratio (e.g., 9:16 portrait vs 16:9 landscape) to size
   * the Preview Canvas accordingly.
   */
  fun importMainVideoClipsWithDimensions(
    importedItems: List<ProbedMediaClip>
  ) {
    if (importedItems.isEmpty()) return
    pushUndoSnapshot()

    val currentMainClips = _clips.value.filter { it.trackType == TrackType.MAIN_VIDEO }
    val otherClips = _clips.value.filter { it.trackType != TrackType.MAIN_VIDEO }
    var runningStartMicros = currentMainClips.maxOfOrNull { it.timelineEndMicros } ?: 0L
    var nextOrderIndex = currentMainClips.size

    // Automatically detect aspect ratio from the uploaded video's dimensions (9:16 vs 16:9, etc.)
    val firstUploaded = importedItems.first()
    val detectedRatio = detectAspectRatioFromDimensions(
      widthPx = firstUploaded.widthPx,
      heightPx = firstUploaded.heightPx
    )
    _project.value = _project.value.copy(aspectRatio = detectedRatio)

    val newClips = importedItems.map { item ->
      val safeDuration = item.durationMicros.coerceAtLeast(1_000_000L)
      val clip = TimelineClipItem(
        clipId = "clip_${UUID.randomUUID().toString().replace("-", "").take(12)}",
        projectId = _project.value.projectId,
        trackType = TrackType.MAIN_VIDEO,
        trackIndex = 0,
        label = item.label,
        mediaUri = item.uri,
        mimeType = if (item.uri.endsWith(".jpg", true) || item.uri.endsWith(".png", true)) "image/*" else "video/mp4",
        sourceDurationMicros = safeDuration,
        sourceInMicros = 0L,
        sourceOutMicros = safeDuration,
        timelineStartMicros = runningStartMicros,
        isMuted = _isMainTrackMuted.value,
        orderIndex = nextOrderIndex++
      )
      runningStartMicros += clip.timelineDurationMicros
      clip
    }

    if (_coverUri.value == null && newClips.firstOrNull()?.mediaUri != null) {
      _coverUri.value = newClips.first().mediaUri
    }

    val combined = recalculateMainTrackStarts(currentMainClips + newClips + otherClips)
    _selectedClipId.value = newClips.first().clipId
    applyAndPersistClips(combined)
  }

  /**
   * Convenience overload for importing clips with default 16:9 or specified dimensions.
   */
  fun importMainVideoClips(
    importedItems: List<Triple<String, String, Long>>,
    widthPx: Int = 1920,
    heightPx: Int = 1080
  ) {
    importMainVideoClipsWithDimensions(
      importedItems.map { (uri, label, durationMicros) ->
        ProbedMediaClip(
          uri = uri,
          label = label,
          durationMicros = durationMicros,
          widthPx = widthPx,
          heightPx = heightPx
        )
      }
    )
  }

  /**
   * Adds an independently positioned visual overlay clip on its own separate track (Section 15).
   */
  fun addOverlayMediaClip(uri: String, label: String, durationMicros: Long = 5_000_000L) {
    pushUndoSnapshot()
    val safeDuration = durationMicros.coerceAtLeast(1_000_000L)
    val existingOverlayTrackCount = _clips.value.count {
      it.trackType == TrackType.OVERLAY || it.trackType == TrackType.TEXT || it.trackType == TrackType.STICKER
    }
    val startAt = _playheadMicros.value.coerceAtLeast(0L)
    val overlayClip = TimelineClipItem(
      clipId = "ovl_${UUID.randomUUID().toString().replace("-", "").take(12)}",
      projectId = _project.value.projectId,
      trackType = TrackType.OVERLAY,
      trackIndex = existingOverlayTrackCount + 1,
      label = label,
      mediaUri = uri,
      mimeType = "image/*",
      sourceDurationMicros = safeDuration,
      sourceInMicros = 0L,
      sourceOutMicros = safeDuration,
      timelineStartMicros = startAt,
      overlayNormalizedX = 0.5f,
      overlayNormalizedY = 0.5f,
      overlayScale = 1.0f,
      orderIndex = _clips.value.size
    )
    val updated = recalculateMainTrackStarts(_clips.value + overlayClip)
    _selectedClipId.value = overlayClip.clipId
    applyAndPersistClips(updated)
  }

  /**
   * Adds a new floating text layer centered on the preview canvas on its own separate track (Sections 14 & 15).
   */
  fun addTextOverlay(text: String, durationMicros: Long = 4_000_000L) {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return
    pushUndoSnapshot()
    val existingOverlayTrackCount = _clips.value.count {
      it.trackType == TrackType.OVERLAY || it.trackType == TrackType.TEXT || it.trackType == TrackType.STICKER
    }
    val startAt = _playheadMicros.value.coerceAtLeast(0L)
    val textClip = TimelineClipItem(
      clipId = "txt_${UUID.randomUUID().toString().replace("-", "").take(12)}",
      projectId = _project.value.projectId,
      trackType = TrackType.TEXT,
      trackIndex = existingOverlayTrackCount + 1,
      label = trimmed,
      overlayText = trimmed,
      mimeType = "text/plain",
      sourceDurationMicros = durationMicros,
      sourceInMicros = 0L,
      sourceOutMicros = durationMicros,
      timelineStartMicros = startAt,
      overlayNormalizedX = 0.5f,
      overlayNormalizedY = 0.5f,
      overlayScale = 1.0f,
      orderIndex = _clips.value.size
    )
    val updated = recalculateMainTrackStarts(_clips.value + textClip)
    _selectedClipId.value = textClip.clipId
    applyAndPersistClips(updated)
  }

  /**
   * Adds an audio track clip in the same universal timeline model (Section 15).
   */
  fun addAudioClip(label: String, uri: String? = null, durationMicros: Long = 6_000_000L) {
    pushUndoSnapshot()
    val safeDuration = durationMicros.coerceAtLeast(1_000_000L)
    val audioCount = _clips.value.count { it.trackType == TrackType.AUDIO }
    val startAt = _playheadMicros.value.coerceAtLeast(0L)
    val audioClip = TimelineClipItem(
      clipId = "aud_${UUID.randomUUID().toString().replace("-", "").take(12)}",
      projectId = _project.value.projectId,
      trackType = TrackType.AUDIO,
      trackIndex = audioCount + 1,
      label = label,
      mediaUri = uri,
      mimeType = "audio/mp4",
      sourceDurationMicros = safeDuration,
      sourceInMicros = 0L,
      sourceOutMicros = safeDuration,
      timelineStartMicros = startAt,
      orderIndex = _clips.value.size
    )
    val updated = recalculateMainTrackStarts(_clips.value + audioClip)
    _selectedClipId.value = audioClip.clipId
    applyAndPersistClips(updated)
  }

  /**
   * Splits the currently selected clip (or clip under playhead) at `playheadMicros` (Section 17).
   */
  fun splitClipAtPlayhead(): Boolean {
    val target = _clips.value.firstOrNull { it.clipId == _selectedClipId.value }
      ?: _clips.value.firstOrNull {
        it.trackType == TrackType.MAIN_VIDEO &&
          _playheadMicros.value > it.timelineStartMicros + 200_000L &&
          _playheadMicros.value < it.timelineEndMicros - 200_000L
      }
      ?: run {
        _statusMessage.value = "Move playhead inside a clip to split."
        return false
      }

    val offsetInTimelineMicros = _playheadMicros.value - target.timelineStartMicros
    if (offsetInTimelineMicros < 200_000L || offsetInTimelineMicros > target.timelineDurationMicros - 200_000L) {
      _statusMessage.value = "Playhead must be at least 0.2s inside the clip to split."
      return false
    }

    val splitSourcePoint = (target.sourceInMicros + (offsetInTimelineMicros * target.speedMultiplier).toLong())
      .coerceIn(target.sourceInMicros + 100_000L, target.sourceOutMicros - 100_000L)

    pushUndoSnapshot()
    val leftClip = target.copy(
      sourceOutMicros = splitSourcePoint
    )
    val rightClip = target.copy(
      clipId = "clip_${UUID.randomUUID().toString().replace("-", "").take(12)}",
      label = "${target.label} (Part 2)",
      sourceInMicros = splitSourcePoint,
      timelineStartMicros = leftClip.timelineEndMicros,
      orderIndex = target.orderIndex + 1
    )

    val updatedList = mutableListOf<TimelineClipItem>()
    _clips.value.forEach { existing ->
      if (existing.clipId == target.clipId) {
        updatedList.add(leftClip)
        updatedList.add(rightClip)
      } else {
        updatedList.add(existing)
      }
    }

    val normalized = recalculateMainTrackStarts(updatedList)
    _selectedClipId.value = rightClip.clipId
    applyAndPersistClips(normalized)
    return true
  }

  /**
   * Trims the selected clip's source-in or source-out boundaries without extending beyond real source duration (Section 16).
   */
  fun trimSelectedClip(deltaSourceInMicros: Long = 0L, deltaSourceOutMicros: Long = 0L): Boolean {
    val selected = _clips.value.firstOrNull { it.clipId == _selectedClipId.value } ?: return false
    val newIn = (selected.sourceInMicros + deltaSourceInMicros)
      .coerceIn(0L, selected.sourceOutMicros - 200_000L)
    val newOut = (selected.sourceOutMicros + deltaSourceOutMicros)
      .coerceIn(newIn + 200_000L, selected.sourceDurationMicros)

    if (newIn == selected.sourceInMicros && newOut == selected.sourceOutMicros) return false

    pushUndoSnapshot()
    val updated = _clips.value.map { clip ->
      if (clip.clipId == selected.clipId) {
        clip.copy(sourceInMicros = newIn, sourceOutMicros = newOut)
      } else {
        clip
      }
    }
    applyAndPersistClips(recalculateMainTrackStarts(updated))
    return true
  }

  fun deleteSelectedClip(): Boolean {
    val selectedId = _selectedClipId.value ?: return false
    pushUndoSnapshot()
    val remaining = _clips.value.filterNot { it.clipId == selectedId }
    _selectedClipId.value = remaining.firstOrNull()?.clipId
    if (remaining.none { it.trackType == TrackType.MAIN_VIDEO }) {
      // Revert to default 16:9 horizontal preview when timeline becomes empty
      _project.value = _project.value.copy(aspectRatio = "16:9")
    }
    applyAndPersistClips(recalculateMainTrackStarts(remaining))
    return true
  }

  fun duplicateSelectedClip(): Boolean {
    val selected = _clips.value.firstOrNull { it.clipId == _selectedClipId.value } ?: return false
    pushUndoSnapshot()
    val copy = selected.copy(
      clipId = "clip_${UUID.randomUUID().toString().replace("-", "").take(12)}",
      label = "${selected.label} Copy",
      timelineStartMicros = selected.timelineEndMicros,
      orderIndex = selected.orderIndex + 1
    )
    val updated = mutableListOf<TimelineClipItem>()
    _clips.value.forEach { clip ->
      updated.add(clip)
      if (clip.clipId == selected.clipId) {
        updated.add(copy)
      }
    }
    _selectedClipId.value = copy.clipId
    applyAndPersistClips(recalculateMainTrackStarts(updated))
    return true
  }

  fun setSelectedClipSpeed(speed: Float) {
    val selectedId = _selectedClipId.value ?: return
    val clampedSpeed = speed.coerceIn(0.25f, 4.0f)
    pushUndoSnapshot()
    val updated = _clips.value.map { clip ->
      if (clip.clipId == selectedId) clip.copy(speedMultiplier = clampedSpeed) else clip
    }
    applyAndPersistClips(recalculateMainTrackStarts(updated))
  }

  fun rotateSelectedClip90() {
    val selectedId = _selectedClipId.value ?: return
    pushUndoSnapshot()
    val updated = _clips.value.map { clip ->
      if (clip.clipId == selectedId) {
        clip.copy(rotationDegrees = (clip.rotationDegrees + 90) % 360)
      } else {
        clip
      }
    }
    applyAndPersistClips(updated)
  }

  fun setTransitionAfterClip(clipId: String, transitionName: String?) {
    pushUndoSnapshot()
    val updated = _clips.value.map { clip ->
      if (clip.clipId == clipId) clip.copy(transitionAfter = transitionName) else clip
    }
    applyAndPersistClips(updated)
  }

  fun updateOverlayPosition(clipId: String, normalizedX: Float, normalizedY: Float) {
    val updated = _clips.value.map { clip ->
      if (clip.clipId == clipId) {
        clip.copy(
          overlayNormalizedX = normalizedX.coerceIn(-0.2f, 1.2f),
          overlayNormalizedY = normalizedY.coerceIn(-0.2f, 1.2f)
        )
      } else {
        clip
      }
    }
    applyAndPersistClips(updated)
  }

  fun undo(): Boolean {
    if (undoStack.isEmpty()) return false
    val currentSnapshot = captureSnapshot()
    redoStack.addLast(currentSnapshot)
    val previous = undoStack.removeLast()
    restoreSnapshot(previous)
    updateUndoRedoFlags()
    return true
  }

  fun redo(): Boolean {
    if (redoStack.isEmpty()) return false
    val currentSnapshot = captureSnapshot()
    undoStack.addLast(currentSnapshot)
    val next = redoStack.removeLast()
    restoreSnapshot(next)
    updateUndoRedoFlags()
    return true
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }

  private fun pushUndoSnapshot() {
    undoStack.addLast(captureSnapshot())
    if (undoStack.size > 40) {
      undoStack.removeFirst()
    }
    redoStack.clear()
    updateUndoRedoFlags()
  }

  private fun captureSnapshot(): EditorHistorySnapshot = EditorHistorySnapshot(
    clips = _clips.value,
    aspectRatio = _project.value.aspectRatio,
    resolutionLabel = _project.value.resolutionLabel,
    frameRateNum = _project.value.frameRateNum,
    isMainTrackMuted = _isMainTrackMuted.value
  )

  private fun restoreSnapshot(snapshot: EditorHistorySnapshot) {
    _isMainTrackMuted.value = snapshot.isMainTrackMuted
    _project.value = _project.value.copy(
      aspectRatio = snapshot.aspectRatio,
      resolutionLabel = snapshot.resolutionLabel,
      frameRateNum = snapshot.frameRateNum
    )
    applyAndPersistClips(recalculateMainTrackStarts(snapshot.clips))
  }

  private fun updateUndoRedoFlags() {
    _canUndo.value = undoStack.isNotEmpty()
    _canRedo.value = redoStack.isNotEmpty()
  }

  private fun recalculateMainTrackStarts(items: List<TimelineClipItem>): List<TimelineClipItem> {
    var cursorMicros = 0L
    var mainOrder = 0
    val mainUpdated = items
      .filter { it.trackType == TrackType.MAIN_VIDEO }
      .sortedBy { it.orderIndex }
      .map { clip ->
        val updated = clip.copy(
          timelineStartMicros = cursorMicros,
          orderIndex = mainOrder++
        )
        cursorMicros += updated.timelineDurationMicros
        updated
      }
    val nonMain = items.filter { it.trackType != TrackType.MAIN_VIDEO }
    return mainUpdated + nonMain
  }

  private fun applyAndPersistClips(updatedClips: List<TimelineClipItem>) {
    _clips.value = updatedClips
    val maxDur = updatedClips.maxOfOrNull { it.timelineEndMicros }?.coerceAtLeast(0L) ?: 0L
    if (_playheadMicros.value > maxDur) {
      _playheadMicros.value = maxDur
    }
    persistCurrentTimelineState()
  }

  private fun persistCurrentTimelineState() {
    val currentProj = _project.value
    val currentClips = _clips.value
    viewModelScope.launch {
      repository.saveProjectTimelineState(
        projectId = currentProj.projectId,
        clips = currentClips,
        aspectRatio = currentProj.aspectRatio,
        resolutionLabel = currentProj.resolutionLabel,
        frameRateNum = currentProj.frameRateNum
      ).onSuccess { saved ->
        _project.value = saved
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    playbackJob?.cancel()
  }

  companion object {
    val APPROVED_ASPECT_RATIOS = listOf(
      "9:16",
      "16:9",
      "1:1",
      "4:5",
      "3:4",
      "4:3",
      "2:1",
      "2.35:1",
      "2.39:1",
      "5:4",
      "5:3",
      "3:2",
      "21:9",
      "Custom"
    )

    /**
     * Automatically detects the closest canonical aspect ratio (`9:16` vs `16:9`, etc.)
     * from the uploaded video/image pixel dimensions.
     * Defaults to `"16:9"` if dimensions are invalid.
     */
    fun detectAspectRatioFromDimensions(widthPx: Int, heightPx: Int): String {
      if (widthPx <= 0 || heightPx <= 0) return "16:9"
      val ratio = widthPx.toFloat() / heightPx.toFloat()
      return when {
        abs(ratio - (9f / 16f)) <= 0.12f || ratio < 0.68f -> "9:16"
        abs(ratio - (16f / 9f)) <= 0.18f || ratio in 1.6f..1.95f -> "16:9"
        abs(ratio - 1.0f) <= 0.08f -> "1:1"
        abs(ratio - (4f / 5f)) <= 0.08f -> "4:5"
        abs(ratio - (3f / 4f)) <= 0.08f -> "3:4"
        abs(ratio - (4f / 3f)) <= 0.08f -> "4:3"
        ratio > 1.0f -> "16:9"
        else -> "9:16"
      }
    }

    fun parseAspectRatioValue(aspectRatio: String): Float {
      return when (aspectRatio) {
        "9:16" -> 9f / 16f
        "16:9" -> 16f / 9f
        "1:1" -> 1f
        "4:5" -> 4f / 5f
        "3:4" -> 3f / 4f
        "4:3" -> 4f / 3f
        "2:1" -> 2f / 1f
        "2.35:1" -> 2.35f
        "2.39:1" -> 2.39f
        "5:4" -> 5f / 4f
        "5:3" -> 5f / 3f
        "3:2" -> 3f / 2f
        "21:9" -> 21f / 9f
        else -> 16f / 9f
      }
    }

    fun formatTimeMmSs(micros: Long): String {
      val totalSeconds = (micros.coerceAtLeast(0L) / 1_000_000L).toInt()
      val minutes = totalSeconds / 60
      val seconds = totalSeconds % 60
      return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun provideFactory(
      repository: LocalProjectRepository,
      project: VideoProject
    ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VideoEditorViewModel(repository, project) as T
      }
    }
  }
}
