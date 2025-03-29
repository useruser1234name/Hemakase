package com.example.hemakase.repository

import android.util.Log
import com.example.hemakase.data.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private const val TAG = "FirebaseRepository"



    suspend fun getReservedTimes(
        year: Int, month: Int, day: Int,
        salonId: String?, stylistName: String
    ): List<String> {
        if (salonId == null || stylistName.isBlank()) return emptyList()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val startOfDay = Timestamp(calendar.time)
        calendar.add(Calendar.DATE, 1)
        val endOfDay = Timestamp(calendar.time)

        val snapshot = FirebaseFirestore.getInstance()
            .collection("reservations")
            .whereEqualTo("salonId", salonId)
            .whereEqualTo("stylist_id", stylistName)
            .whereGreaterThanOrEqualTo("date", startOfDay)
            .whereLessThan("date", endOfDay)
            .whereEqualTo("status", "confirmed")
            .get()
            .await()

        val formatter = SimpleDateFormat("HH:mm", Locale.KOREAN)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return snapshot.documents.mapNotNull {
            it.getTimestamp("date")?.toDate()?.let { d -> formatter.format(d).trim() }
        }
    }


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
