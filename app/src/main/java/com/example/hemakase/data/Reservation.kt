package com.example.hemakase.data

data class Reservation(
    val id: String = "",
    val customer_id: String = "",
    val stylist_id: String = "",
    val date: com.google.firebase.Timestamp? = null,
    val status: String = "pending", // confirmed, cancelled_by_customer 등
    val style: String = "",
    val note: String = "",
    val reference_photo: String? = null
)
