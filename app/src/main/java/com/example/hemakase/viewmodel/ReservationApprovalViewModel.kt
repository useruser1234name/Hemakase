package com.example.hemakase.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hemakase.data.ChatMessage
import com.example.hemakase.data.Message
import com.example.hemakase.repository.FirebaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
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

    fun rejectReservation(reservation: Map<String, Any>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val docId = reservation["docId"] as? String ?: return@launch
            val customerId = reservation["customer_id"] as? String ?: return@launch
            val customerName = reservation["customer_name"] as? String ?: "고객"
            val stylistId = reservation["stylist_id"] as? String ?: return@launch
            val time = (reservation["date"] as? Timestamp)?.toDate()?.toString() ?: "시간 미정"

            try {
                // 1. 예약 상태 업데이트
                db.collection("reservations").document(docId)
                    .update("status", "rejected").await()

                // 2. 메시지 전송 대상: 오너 + 미용사 → 고객
                val senderIds = listOf("owner", "stylist")
                val senders = listOf(stylistId, /* ownerId 추출 예정 */)

                // 🔍 오너 ID 가져오기 (salonId 기반)
                val salonId = reservation["salonId"] as? String
                val ownerId = salonId?.let {
                    db.collection("salons").document(it).get().await().getString("ownerId")
                }

                val realSenders = listOfNotNull(stylistId, ownerId)

                for (senderId in realSenders) {
                    val messageText = "${customerName}님의 예약이 거절되었습니다. 시간: $time"
                    val message = ChatMessage(
                        senderId = "system", // 실제로는 senderId를 넘겨도 OK
                        receiverId = customerId,
                        message = messageText,
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        type = "notification"
                    )

                    // Firestore 메시지 저장
                    FirebaseRepository.sendMessage(message)

                    // Realtime DB 저장
                    val chatRoomQuery = db.collection("chat_rooms")
                        .whereArrayContains("participants", customerId)
                        .get().await()
                    val roomId = chatRoomQuery.documents.firstOrNull()?.id

                    if (!roomId.isNullOrBlank()) {
                        val realtimeDb = FirebaseDatabase.getInstance()
                            .getReference("chat_rooms/$roomId/messages")
                        realtimeDb.push().setValue(message).await()
                    }

                    // FCM 전송
                    val userDoc = db.collection("users").document(customerId).get().await()
                    val fcmToken = userDoc.getString("fcmToken")
                    if (!fcmToken.isNullOrBlank()) {
                        FirebaseRepository.sendPushNotification(
                            token = fcmToken,
                            title = "예약 거절 안내",
                            body = messageText
                        )
                    }
                }

                onSuccess()

            } catch (e: Exception) {
                Log.e("ReservationVM", "거절 실패: ${e.message}")
            }
        }
    }



    fun approveReservation(reservation: Map<String, Any>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val docId = reservation["docId"] as? String
            if (docId == null) {
                Log.e("ReservationVM", "docId 없음")
                return@launch
            }

            val customerId = reservation["customer_id"] as? String ?: return@launch
            val customerName = reservation["customer_name"] as? String ?: "고객"
            val stylistId = reservation["stylist_id"] as? String ?: return@launch
            val time = (reservation["date"] as? Timestamp)?.toDate()?.toString() ?: "시간 미정"

            try {
                // 1. 예약 상태 업데이트
                db.collection("reservations").document(docId)
                    .update("status", "confirmed").await()

                // 2. 수신 대상 목록 구성 (고객 + 미용사)
                val targets = listOf(
                    Triple(customerId, "${customerName}님의 예약이 확정되었습니다. 시간: $time", "고객"),
                    Triple(stylistId, "${customerName}님의 예약이 확정되어 미용사님에게 전달되었습니다.", "미용사")
                )

                for ((receiverId, messageText, label) in targets) {
                    // 메시지 생성
                    val message = ChatMessage(
                        senderId = "system",
                        receiverId = receiverId,
                        message = messageText,
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        type = "notification"
                    )

                    // Firestore 메시지 저장
                    FirebaseRepository.sendMessage(message)

                    // Realtime DB 저장
                    val chatRoomQuery = db.collection("chat_rooms")
                        .whereArrayContains("participants", receiverId)
                        .get().await()
                    val matchingRoomId = chatRoomQuery.documents.firstOrNull()?.id

                    if (!matchingRoomId.isNullOrBlank()) {
                        val realtimeDb = com.google.firebase.database.FirebaseDatabase.getInstance()
                            .reference.child("chat_rooms").child(matchingRoomId).child("messages")
                        realtimeDb.push().setValue(message).await()
                    }

                    // FCM 푸시 알림
                    val userDoc = db.collection("users").document(receiverId).get().await()
                    val fcmToken = userDoc.getString("fcmToken")
                    if (!fcmToken.isNullOrBlank()) {
                        FirebaseRepository.sendPushNotification(
                            token = fcmToken,
                            title = "예약 확정 안내",
                            body = messageText
                        )
                    }
                }

                onSuccess()

            } catch (e: Exception) {
                Log.e("ReservationVM", "예약 확정 실패: ${e.message}")
            }
        }
    }

}
