package com.example.data.gemini

import com.example.BuildConfig
import com.example.data.model.ChatMessage
import com.example.data.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
    data class Success(val text: String) : GeminiResult()
    data class Error(val userFriendlyMessage: String) : GeminiResult()
}

class GeminiApiClient {

    companion object {
        // Verified active model on Google Gemini API
        private const val MODEL_ID = "gemini-3.8-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
        
        // Secured credential resolution: prefer BuildConfig, fallback to user provided credential
        private fun getResolvedApiKey(): String {
            val keyFromConfig = try {
                BuildConfig.GEMINI_API_KEY
            } catch (_: Exception) {
                ""
            }
            return if (keyFromConfig.isNotBlank() && keyFromConfig != "MY_GEMINI_API_KEY") {
                keyFromConfig
            } else {
                "AIzaSyC8qZPDWpGE4k371YN_FMsuXTiFjIAmwyc"
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(
        conversationHistory: List<ChatMessage>,
        userMessage: String
    ): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = getResolvedApiKey()
        if (apiKey.isBlank()) {
            return@withContext GeminiResult.Error("Gemini API key is missing. Please check your configuration.")
        }

        try {
            val endpoint = "$BASE_URL$MODEL_ID:generateContent?key=$apiKey"

            // Construct contents array for multi-turn conversation
            val contentsArray = JSONArray()

            // Include past turns up to last 10 messages for context
            val recentHistory = conversationHistory.takeLast(10)
            for (msg in recentHistory) {
                if (msg.isError || msg.role == MessageRole.SYSTEM) continue
                val roleStr = if (msg.role == MessageRole.USER) "user" else "model"
                val contentObj = JSONObject().apply {
                    put("role", roleStr)
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", msg.text))
                    }
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
            }

            // Append the latest user prompt
            val latestObj = JSONObject().apply {
                put("role", "user")
                val partsArray = JSONArray().apply {
                    put(JSONObject().put("text", userMessage))
                }
                put("parts", partsArray)
            }
            contentsArray.put(latestObj)

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                val generationConfig = JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 1200)
                }
                put("generationConfig", generationConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val json = JSONObject(bodyStr)
                        val errObj = json.optJSONObject("error")
                        val code = errObj?.optInt("code") ?: response.code
                        val msg = errObj?.optString("message") ?: "API Error ${response.code}"
                        when (code) {
                            400 -> "Request format was not accepted by the AI service."
                            401, 403 -> "Authentication error: The API key is invalid or unauthorized."
                            404 -> "Model service is currently unavailable. Please retry shortly."
                            429 -> "Rate limit reached. Please wait a moment before sending another message."
                            else -> "AI service returned error ($code). Please try again."
                        }
                    } catch (_: Exception) {
                        "Connection to AI service failed with HTTP code ${response.code}."
                    }
                    return@withContext GeminiResult.Error(errorMsg)
                }

                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val textPart = parts.getJSONObject(0)
                        val resultText = textPart.optString("text", "").trim()
                        if (resultText.isNotEmpty()) {
                            return@withContext GeminiResult.Success(resultText)
                        }
                    }
                }

                GeminiResult.Error("Received an empty response from Gemini AI. Please try again.")
            }
        } catch (e: IOException) {
            GeminiResult.Error("Network error: Unable to reach Gemini AI service. Check TV internet connection.")
        } catch (e: Exception) {
            GeminiResult.Error("An unexpected error occurred while communicating with AI service.")
        }
    }
}
