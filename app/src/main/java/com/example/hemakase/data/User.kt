package com.example.hemakase.data

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "customer" or "stylist"
    val photo: String? = null,
    val phone: String = "",
    val salonId: String? = null, // 🔥 추가된 부분
    val createdAt: Long = System.currentTimeMillis()
)