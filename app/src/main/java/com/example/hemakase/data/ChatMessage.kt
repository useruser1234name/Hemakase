package com.example.hemakase.data

data class ChatMessage(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "text", // "text", "notification"
    val reservationId: String? = null
    )
