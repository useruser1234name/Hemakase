package com.example.hemakase.data

data class CustomerNote(
    val id: String = "",
    val customer_id: String = "",
    val name: String = "",
    val previous_history: String = "",
    val face_photo: String? = null,
    val memo: String = ""
)