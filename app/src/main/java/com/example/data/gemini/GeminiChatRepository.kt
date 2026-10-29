package com.example.data.gemini

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ChatMessage
import com.example.data.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GeminiChatRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aura_tv_gemini_chat_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HISTORY_JSON = "key_history_json"
        private const val KEY_HISTORY_ENABLED = "key_history_enabled"
    }

    private val apiClient = GeminiApiClient()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isHistoryEnabled = MutableStateFlow(prefs.getBoolean(KEY_HISTORY_ENABLED, true))
    val isHistoryEnabled: StateFlow<Boolean> = _isHistoryEnabled.asStateFlow()

    init {
        loadHistoryFromStorage()
    }

    private fun loadHistoryFromStorage() {
        if (!_isHistoryEnabled.value) {
            _messages.value = listOf(getWelcomeMessage())
            return
        }

        val jsonStr = prefs.getString(KEY_HISTORY_JSON, null)
        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                val loadedList = mutableListOf<ChatMessage>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id", System.currentTimeMillis().toString())
                    val roleStr = obj.optString("role", "MODEL")
                    val role = try { MessageRole.valueOf(roleStr) } catch (_: Exception) { MessageRole.MODEL }
                    val text = obj.optString("text", "")
                    val time = obj.optLong("timestamp", System.currentTimeMillis())
                    val isError = obj.optBoolean("isError", false)
                    loadedList.add(ChatMessage(id, role, text, time, isError))
                }
                if (loadedList.isNotEmpty()) {
                    _messages.value = loadedList
                    return
                }
            } catch (_: Exception) { }
        }

        _messages.value = listOf(getWelcomeMessage())
    }

    private fun getWelcomeMessage(): ChatMessage {
        return ChatMessage(
            id = "welcome_msg",
            role = MessageRole.MODEL,
            text = "Hello! I am your Aura TV AI Assistant powered by Google Gemini. Ask me for movie recommendations, TV tips, general knowledge, or entertainment ideas!"
        )
    }

    private fun persistHistory() {
        if (!_isHistoryEnabled.value) return
        val currentList = _messages.value.filter { it.id != "welcome_msg" }
        val array = JSONArray()
        for (msg in currentList) {
            val obj = JSONObject().apply {
                put("id", msg.id)
                put("role", msg.role.name)
                put("text", msg.text)
                put("timestamp", msg.timestamp)
                put("isError", msg.isError)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY_JSON, array.toString()).apply()
    }

    suspend fun sendMessage(userText: String): Boolean = withContext(Dispatchers.IO) {
        if (userText.isBlank()) return@withContext false

        val userMessage = ChatMessage(
            role = MessageRole.USER,
            text = userText.trim()
        )

        // Add user message to current state
        val updated = _messages.value.toMutableList().apply { add(userMessage) }
        _messages.value = updated
        _isLoading.value = true

        val result = apiClient.generateResponse(updated, userText.trim())

        _isLoading.value = false

        when (result) {
            is GeminiResult.Success -> {
                val aiMessage = ChatMessage(
                    role = MessageRole.MODEL,
                    text = result.text
                )
                val finalList = _messages.value.toMutableList().apply { add(aiMessage) }
                _messages.value = finalList
                persistHistory()
                true
            }
            is GeminiResult.Error -> {
                val errorMessage = ChatMessage(
                    role = MessageRole.MODEL,
                    text = result.userFriendlyMessage,
                    isError = true
                )
                val finalList = _messages.value.toMutableList().apply { add(errorMessage) }
                _messages.value = finalList
                persistHistory()
                false
            }
        }
    }

    fun startNewChat() {
        _messages.value = listOf(getWelcomeMessage())
        if (_isHistoryEnabled.value) {
            prefs.edit().remove(KEY_HISTORY_JSON).apply()
        }
    }

    fun clearHistory() {
        _messages.value = listOf(getWelcomeMessage())
        prefs.edit().remove(KEY_HISTORY_JSON).apply()
    }

    fun setHistoryEnabled(enabled: Boolean) {
        _isHistoryEnabled.value = enabled
        prefs.edit().putBoolean(KEY_HISTORY_ENABLED, enabled).apply()
        if (!enabled) {
            prefs.edit().remove(KEY_HISTORY_JSON).apply()
        } else {
            persistHistory()
        }
    }
}
