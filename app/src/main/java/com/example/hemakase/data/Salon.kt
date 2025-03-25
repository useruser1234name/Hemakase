package com.example.hemakase.data

data class Salon(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val phone: String? = null,
    val ownerId: String = "", // 미용사(관리자) UID
    val stylistIds: List<String> = emptyList() // 해당 미용실 소속 미용사들
)
