package com.example.hemakase.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatListViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance()

    private val _chatRooms = MutableStateFlow<List<ChatRoomItem>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoomItem>> = _chatRooms

    init {
        fetchChatRooms()
    }

    private fun fetchChatRooms() {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("chat_rooms")
            .whereArrayContains("participants", myUid)
            .get()
            .addOnSuccessListener { snapshot ->
                val rooms = mutableListOf<ChatRoomItem>()
                val documents = snapshot.documents

                if (documents.isEmpty()) {
                    _chatRooms.value = emptyList()
                    return@addOnSuccessListener
                }

                var loadedCount = 0

                documents.forEach { doc ->
                    val chatRoomId = doc.getString("chatRoomId") ?: doc.id
                    val participants = doc.get("participants") as? List<String> ?: return@forEach
                    val otherUid = participants.firstOrNull { it != myUid } ?: return@forEach

                    // 상대방 유저 정보 가져오기
                    db.collection("users").document(otherUid).get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("name") ?: "알 수 없음"
                            val photo = userDoc.getString("photo")

                            // 마지막 메시지 가져오기 (RealtimeDB)
                            realtimeDb.getReference("chat_rooms/$chatRoomId/messages")
                                .orderByChild("timestamp")
                                .limitToLast(1)
                                .get()
                                .addOnSuccessListener { messageSnapshot ->
                                    val lastMessage = messageSnapshot.children.firstOrNull()
                                        ?.child("message")?.value?.toString() ?: "메시지가 없습니다."

                                    rooms.add(
                                        ChatRoomItem(
                                            roomId = chatRoomId,
                                            name = name,
                                            photoUrl = photo,
                                            lastMessage = lastMessage
                                        )
                                    )
                                    loadedCount++
                                    if (loadedCount == documents.size) {
                                        _chatRooms.value = rooms.sortedByDescending { it.roomId }
                                    }
                                }
                                .addOnFailureListener {
                                    rooms.add(
                                        ChatRoomItem(
                                            roomId = chatRoomId,
                                            name = name,
                                            photoUrl = photo,
                                            lastMessage = "메시지 없음"
                                        )
                                    )
                                    loadedCount++
                                    if (loadedCount == documents.size) {
                                        _chatRooms.value = rooms.sortedByDescending { it.roomId }
                                    }
                                }
                        }
                        .addOnFailureListener {
                            loadedCount++
                            if (loadedCount == documents.size) {
                                _chatRooms.value = rooms
                            }
                        }
                }
            }
            .addOnFailureListener {
                Log.e("ChatListViewModel", "채팅방 불러오기 실패: ${it.message}")
                _chatRooms.value = emptyList()
            }
    }
}

data class ChatRoomItem(
    val roomId: String,
    val name: String,
    val photoUrl: String? = null,
    val lastMessage: String
)
