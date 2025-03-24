package com.example.hemakase.repository

import android.util.Log
import com.example.hemakase.data.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private const val TAG = "FirebaseRepository"

    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(user.id)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e(TAG, "addUser failed", e)
                onFailure(e)
            }
    }

    fun createReservation(reservation: Reservation, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("reservations")
            .add(reservation)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e(TAG, "createReservation failed", e)
                onFailure(e)
            }
    }

    fun addCustomerNote(note: CustomerNote, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("customer_notes")
            .add(note)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e(TAG, "addCustomerNote failed", e)
                onFailure(e)
            }
    }

    // 예: 유저 role 가져오기
    fun getUserRole(uid: String, onResult: (String?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role")
                onResult(role)
            }
            .addOnFailureListener {
                Log.e(TAG, "getUserRole failed", it)
                onResult(null)
            }
    }
}
