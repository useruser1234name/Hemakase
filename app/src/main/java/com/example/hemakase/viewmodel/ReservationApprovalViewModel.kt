package com.example.hemakase.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hemakase.data.ChatMessage
import com.example.hemakase.repository.FirebaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

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
            try {
                val docId = reservation["docId"] as? String ?: return@launch
                val customerId = reservation["customer_id"] as? String ?: return@launch
                val customerName = reservation["customer_name"] as? String ?: "고객"
                val stylistId = reservation["stylist_id"] as? String ?: return@launch
                val time = (reservation["date"] as? Timestamp)?.toDate() ?: return@launch

                val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREAN).format(time)

                db.collection("reservations").document(docId)
                    .update("status", "rejected").await()

                val salonId = reservation["salonId"] as? String
                val ownerId = salonId?.let {
                    db.collection("salons").document(it).get().await().getString("ownerId")
                }

                val senders = listOfNotNull(stylistId, ownerId)

                for (senderId in senders) {
                    val messageText = "${customerName}님의 예약이 거절되었습니다. 시간: $formattedTime"
                    val message = ChatMessage(
                        senderId = "system",
                        receiverId = customerId,
                        message = messageText,
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        type = "notification"
                    )

                    FirebaseRepository.sendMessage(message)

                    val roomId = FirebaseRepository.getOrCreateChatRoom(
                        senderId = senderId,
                        receiverId = customerId,
                        senderRole = if (senderId == stylistId) "stylist" else "owner",
                        receiverRole = "guest"
                    )

                    FirebaseDatabase.getInstance()
                        .getReference("chat_rooms/$roomId/messages")
                        .push()
                        .setValue(message)
                        .await()

                    val userDoc = db.collection("users").document(customerId).get().await()
                    val fcmToken = userDoc.getString("fcmToken")
                    if (!fcmToken.isNullOrBlank()) {
                        FirebaseRepository.sendPushNotificationV1(
                            targetToken = fcmToken,
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
            try {
                val docId = reservation["docId"] as? String ?: return@launch
                val customerId = reservation["customer_id"] as? String ?: return@launch
                val customerName = reservation["customer_name"] as? String ?: "고객"
                val stylistId = reservation["stylist_id"] as? String ?: return@launch
                val time = (reservation["date"] as? Timestamp)?.toDate() ?: return@launch

                val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREAN).format(time)

                db.collection("reservations").document(docId)
                    .update("status", "confirmed").await()

                val targets = listOf(
                    customerId to ("guest" to "${customerName}님의 예약이 확정되었습니다. 시간: $formattedTime"),
                    stylistId to ("stylist" to "${customerName}님의 예약이 확정되어 미용사님에게 전달되었습니다.")
                )

                for ((receiverId, pair) in targets) {
                    val (receiverRole, messageText) = pair
                    val message = ChatMessage(
                        senderId = "system",
                        receiverId = receiverId,
                        message = messageText,
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        type = "notification"
                    )

                    FirebaseRepository.sendMessage(message)

                    val roomId = FirebaseRepository.getOrCreateChatRoom(
                        senderId = "system",
                        receiverId = receiverId,
                        senderRole = "system",
                        receiverRole = receiverRole
                    )

                    FirebaseDatabase.getInstance()
                        .getReference("chat_rooms/$roomId/messages")
                        .push()
                        .setValue(message)
                        .await()

                    val userDoc = db.collection("users").document(receiverId).get().await()
                    val fcmToken = userDoc.getString("fcmToken")
                    if (!fcmToken.isNullOrBlank()) {
                        FirebaseRepository.sendPushNotificationV1(
                            targetToken = fcmToken,
                            title = "예약 확정 알림",
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