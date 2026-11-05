package com.cineflow.ai.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "CineFlowAiGateway"

data class AiEditPlanSuggestion(
  val suggestedTitle: String,
  val recommendedAspectRatio: String,
  val recommendedFps: Int,
  val summary: String
)

/**
 * Validated structured AI gateway powered by Firebase AI Logic (`Firebase.ai`).
 * Uses `gemini-3.8-flash` lazily with `Schema.obj(...)` and 60s timeout (Section 20).
 */
class CineFlowAiGateway {
  private val editPlanSchema = Schema.obj(
    properties = mapOf(
      "suggestedTitle" to Schema.string(description = "Suggested video project title"),
      "recommendedAspectRatio" to Schema.enumeration(
        values = listOf("16:9", "9:16", "1:1", "4:5", "21:9"),
        description = "Recommended canvas aspect ratio"
      ),
      "recommendedFps" to Schema.integer(description = "Recommended integer frame rate (25-240)"),
      "summary" to Schema.string(description = "Concise editing workflow summary")
    )
  )

  private val model by lazy {
    Firebase.ai.generativeModel(
      modelName = "gemini-3.8-flash",
      generationConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = editPlanSchema
      }
    )
  }

  suspend fun generateEditPlan(ideaPrompt: String): Result<AiEditPlanSuggestion> {
    return try {
      val response = withTimeout(60_000L) {
        model.generateContent(
          "Create a structured video editing setup plan for: $ideaPrompt"
        )
      }
      val jsonText = response.text
        ?: return Result.failure(IllegalStateException("AI model returned empty response."))
      val json = JSONObject(jsonText)
      val fps = json.optInt("recommendedFps", 30).coerceIn(25, 240)
      Result.success(
        AiEditPlanSuggestion(
          suggestedTitle = json.getString("suggestedTitle").take(80),
          recommendedAspectRatio = json.optString("recommendedAspectRatio", "16:9"),
          recommendedFps = fps,
          summary = json.optString("summary", "")
        )
      )
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "AI edit plan generation timed out", e)
      Result.failure(IllegalStateException("AI request timed out after 60 seconds."))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "AI edit plan generation failed", e)
      Result.failure(e)
    }
  }
}
