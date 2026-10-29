package com.example.data.model

enum class MessageRole {
    USER,
    MODEL,
    SYSTEM
}

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)
