package com.example.hemakase.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatListViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

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
            .addOnSuccessListener { snapshots ->
                val rooms = mutableListOf<ChatRoomItem>()
                val documents = snapshots.documents

                if (documents.isEmpty()) {
                    _chatRooms.value = emptyList()
                    return@addOnSuccessListener
                }

                var loadedCount = 0

                documents.forEach { doc ->
                    val roomId = doc.id
                    val participants = doc.get("participants") as? List<String> ?: return@forEach
                    val otherUid = participants.firstOrNull { it != myUid } ?: return@forEach

                    db.collection("users").document(otherUid).get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("name") ?: "알 수 없음"
                            val photo = userDoc.getString("photo")

                            FirebaseDatabase.getInstance()
                                .getReference("chat_rooms/$roomId/messages")
                                .orderByChild("timestamp")
                                .limitToLast(1)
                                .get()
                                .addOnSuccessListener { msgSnapshot ->
                                    val lastMessage = msgSnapshot.children.firstOrNull()
                                        ?.child("message")?.value?.toString()
                                        ?: "메시지가 없습니다."

                                    rooms.add(ChatRoomItem(roomId, name, photo, lastMessage))
                                    loadedCount++
                                    if (loadedCount == documents.size) {
                                        _chatRooms.value = rooms.sortedByDescending { it.roomId }
                                    }
                                }
                                .addOnFailureListener {
                                    rooms.add(ChatRoomItem(roomId, name, photo, "메시지 없음"))
                                    loadedCount++
                                    if (loadedCount == documents.size) {
                                        _chatRooms.value = rooms.sortedByDescending { it.roomId }
                                    }
                                }
                        }
                        .addOnFailureListener {
                            loadedCount++
                            if (loadedCount == documents.size) {
                                _chatRooms.value = rooms.sortedByDescending { it.roomId }
                            }
                        }
                }
            }
            .addOnFailureListener {
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