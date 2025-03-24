package com.example.hemakase.data

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "customer", "stylist"
    val photo: String? = null,
    val phone: String? = null,
    val created_at: com.google.firebase.Timestamp? = null
)