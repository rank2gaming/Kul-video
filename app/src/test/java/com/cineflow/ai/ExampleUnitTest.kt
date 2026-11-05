package com.cineflow.ai

import com.cineflow.ai.core.model.ExportResolutionCap
import com.cineflow.ai.core.timebase.AspectRatioPreset
import com.cineflow.ai.core.timebase.RationalFrameRate
import com.cineflow.ai.core.timebase.Timebase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun timebase_rationalFrameRate_calculatesExactMicrosAndFrames() {
    val fps60 = RationalFrameRate(60, 1)
    assertEquals(0L, fps60.frameToMicros(0L))
    assertEquals(1_000_000L, fps60.frameToMicros(60L))
    assertEquals(60L, fps60.microsToFrame(1_000_000L))

    val fps240 = RationalFrameRate(240, 1)
    assertEquals(1_000_000L, fps240.frameToMicros(240L))
    assertEquals(240L, fps240.microsToFrame(1_000_000L))
  }

  @Test
  fun timebase_sourceTrimCannotExtendBeyondSourceDuration() {
    val sourceDuration = 10_000_000L
    val (clampedIn, clampedOut) = Timebase.clampSourceTrim(
      requestedInMicros = -500_000L,
      requestedOutMicros = 15_000_000L,
      sourceDurationMicros = sourceDuration
    )
    assertEquals(0L, clampedIn)
    assertEquals(sourceDuration, clampedOut)
  }

  @Test
  fun exportResolutionCap_enforcesUnsigned720pAndAuthenticated8k() {
    assertTrue(ExportResolutionCap.isAllowedForSession(ExportResolutionCap.SD_480P, isAuthenticated = false))
    assertTrue(ExportResolutionCap.isAllowedForSession(ExportResolutionCap.HD_720P, isAuthenticated = false))
    assertFalse(ExportResolutionCap.isAllowedForSession(ExportResolutionCap.FHD_1080P, isAuthenticated = false))
    assertFalse(ExportResolutionCap.isAllowedForSession(ExportResolutionCap.UHD_8K, isAuthenticated = false))

    assertTrue(ExportResolutionCap.isAllowedForSession(ExportResolutionCap.FHD_1080P, isAuthenticated = true))
    assertTrue(ExportResolutionCap.isAllowedForSession(ExportResolutionCap.UHD_8K, isAuthenticated = true))
  }

  @Test
  fun aspectRatioPresets_containsAll14ApprovedPresets() {
    assertEquals(14, AspectRatioPreset.entries.size)
  }
}
