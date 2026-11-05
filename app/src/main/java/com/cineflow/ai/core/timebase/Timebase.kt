package com.cineflow.ai.core.timebase

/**
 * Canonical 64-bit microsecond timebase and rational frame-rate arithmetic for CineFlow AI.
 * Strictly avoids floating-point seconds as the canonical timeline representation (Section 16).
 */
object Timebase {
  const val MICROS_PER_SECOND: Long = 1_000_000L
  const val MICROS_PER_MILLISECOND: Long = 1_000L

  val SUPPORTED_FPS_PRESETS: List<Int> = listOf(
    25, 30, 48, 50, 60, 90, 100, 120, 144, 165, 180, 200, 240
  )

  fun addMicrosSafe(a: Long, b: Long): Long = Math.addExact(a, b)

  fun subtractMicrosSafe(a: Long, b: Long): Long = Math.subtractExact(a, b)

  fun clampSourceTrim(
    requestedInMicros: Long,
    requestedOutMicros: Long,
    sourceDurationMicros: Long
  ): Pair<Long, Long> {
    require(sourceDurationMicros >= 0L) { "Source duration must be non-negative" }
    val safeIn = requestedInMicros.coerceIn(0L, sourceDurationMicros)
    val safeOut = requestedOutMicros.coerceIn(safeIn, sourceDurationMicros)
    return safeIn to safeOut
  }
}

data class RationalFrameRate(
  val numerator: Int,
  val denominator: Int = 1
) {
  init {
    require(numerator in 25..240_000) { "Frame rate numerator out of supported range: $numerator" }
    require(denominator in 1..1001) { "Frame rate denominator out of supported range: $denominator" }
    val fps = numerator / denominator
    require(fps in 25..240) { "Effective FPS must be between 25 and 240 (got $fps)" }
  }

  /**
   * Converts a 0-based frame index to canonical 64-bit microseconds using integer arithmetic.
   */
  fun frameToMicros(frameIndex: Long): Long {
    require(frameIndex >= 0L) { "Frame index cannot be negative: $frameIndex" }
    val scaled = Math.multiplyExact(frameIndex, Timebase.MICROS_PER_SECOND)
    return Math.multiplyExact(scaled, denominator.toLong()) / numerator.toLong()
  }

  /**
   * Converts canonical 64-bit microseconds to the floor frame index.
   */
  fun microsToFrame(timeMicros: Long): Long {
    require(timeMicros >= 0L) { "Timestamp cannot be negative: $timeMicros" }
    val scaled = Math.multiplyExact(timeMicros, numerator.toLong())
    return scaled / Math.multiplyExact(Timebase.MICROS_PER_SECOND, denominator.toLong())
  }

  /**
   * Snaps an arbitrary microsecond timestamp to the nearest exact frame boundary.
   */
  fun snapToFrameBoundary(timeMicros: Long): Long {
    val frame = microsToFrame(timeMicros)
    return frameToMicros(frame)
  }
}

enum class AspectRatioPreset(
  val label: String,
  val widthRatio: Float,
  val heightRatio: Float
) {
  RATIO_1_1("1:1", 1f, 1f),
  RATIO_4_5("4:5", 4f, 5f),
  RATIO_9_16("9:16", 9f, 16f),
  RATIO_3_4("3:4", 3f, 4f),
  RATIO_4_3("4:3", 4f, 3f),
  RATIO_16_9("16:9", 16f, 9f),
  RATIO_2_1("2:1", 2f, 1f),
  RATIO_235_1("2.35:1", 2.35f, 1f),
  RATIO_239_1("2.39:1", 2.39f, 1f),
  RATIO_5_4("5:4", 5f, 4f),
  RATIO_5_3("5:3", 5f, 3f),
  RATIO_3_2("3:2", 3f, 2f),
  RATIO_21_9("21:9", 21f, 9f),
  CUSTOM("Custom", 16f, 9f);

  val aspectFloat: Float
    get() = widthRatio / heightRatio
}
