package com.example.hemakase.data

data class Reservation(
    val id: String = "",
    val customer_id: String = "",
    val customer_name: String = "",
    val salonId: String = "",
    val salonName: String = "",
    val stylist_id: String = "",
    val stylist_name: String = "",
    val date: com.google.firebase.Timestamp? = null,
    val status: String = "pending",
    val style: String = "",
    val note: String = "",
    val reference_photo: String? = null,
    val treatmentName: String = "",
    val treatmentDescription: String = "",
    val treatmentPrice: Int = 0
)
