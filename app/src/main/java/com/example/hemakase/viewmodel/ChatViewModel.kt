package com.example.hemakase.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hemakase.data.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel(private val roomId: String) : ViewModel() {

    private val database = FirebaseDatabase.getInstance()
        .reference.child("chat_rooms").child(roomId).child("messages")

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    init {
        listenForMessages()
    }

    private fun listenForMessages() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(ChatMessage::class.java)
                msg?.let {
                    _messages.value += it
                }
            }

            override fun onCancelled(error: DatabaseError) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        })
    }

    fun sendMessage(message: String) {
        val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatMessage = ChatMessage(senderId = senderId, message = message)
        database.push().setValue(chatMessage)
    }
}
