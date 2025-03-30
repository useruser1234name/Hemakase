package com.example.hemakase.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hemakase.data.ChatMessage
import com.example.hemakase.data.Message
import com.example.hemakase.repository.FirebaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ReservationApprovalViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _pendingReservations = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val pendingReservations: StateFlow<List<Map<String, Any>>> = _pendingReservations

    fun loadPendingReservations(ownerEmail: String) {
        viewModelScope.launch {
            try {
                val userSnapshot = db.collection("users")
                    .whereEqualTo("email", ownerEmail)
                    .get()
                    .await()
                val ownerId = userSnapshot.documents.firstOrNull()?.getString("id") ?: return@launch

                val salonSnapshot = db.collection("salons")
                    .whereEqualTo("ownerId", ownerId)
                    .get()
                    .await()
                val salonId = salonSnapshot.documents.firstOrNull()?.id ?: return@launch

                val resSnapshot = db.collection("reservations")
                    .whereEqualTo("salonId", salonId)
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

                _pendingReservations.value = resSnapshot.documents.map { it.data!! + ("docId" to it.id) }
            } catch (e: Exception) {
                Log.e("ReservationVM", "불러오기 실패: ${e.message}")
            }
        }
    }

    fun rejectReservation(reservation: Map<String, Any>) {
        viewModelScope.launch {
            val docId = reservation["docId"] as? String ?: return@launch
            try {
                db.collection("reservations").document(docId).update("status", "rejected").await()
            } catch (e: Exception) {
                Log.e("ReservationVM", "거절 실패: ${e.message}")
            }
        }
    }


    fun approveReservation(reservation: Map<String, Any>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val docId = reservation["docId"] as String
            if (docId == null) {
                Log.e("ReservationVM", "docId 없음")
                return@launch
            }
            val customerId = reservation["customer_id"] as? String ?: return@launch
            val customerName = reservation["customer_name"] as? String ?: "고객"
            val time = (reservation["date"] as? Timestamp)?.toDate()?.toString() ?: "시간 미정"

            try {
                db.collection("reservations").document(docId).update("status", "confirmed").await()

                // 채팅 메시지 전송
                val message = ChatMessage(
                    senderId = "system",
                    receiverId = customerId,  // 반드시 포함!
                    message = "${customerName}님의 예약이 확정되었습니다. 시간: $time",
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    type = "notification"
                )
                FirebaseRepository.sendMessage(message)

                // FCM 푸시 전송
                val userDoc = db.collection("users").document(customerId).get().await()
                val fcmToken = userDoc.getString("fcmToken")

                if (!fcmToken.isNullOrBlank()) {
                    FirebaseRepository.sendPushNotification(
                        token = fcmToken,
                        title = "예약 확정 안내",
                        body = "${customerName}님의 예약이 확정되었습니다. 시간: $time"
                    )
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("ReservationVM", "수락 실패: ${e.message}")
            }
        }
    }
}
