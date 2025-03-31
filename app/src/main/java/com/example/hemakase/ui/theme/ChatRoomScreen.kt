package com.example.hemakase.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hemakase.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChatRoomScreen(roomId: String) {
    val viewModel: ChatViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(roomId) as T
        }
    })

    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // 메시지 리스트
        Column(modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())) {
            messages.forEach { msg ->
                if (msg.type == "notification") {
                    // 예약 확정 알림 메시지 스타일
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${msg.message}",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // 일반 메시지 말풍선 (오른쪽/왼쪽 정렬)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.senderId == FirebaseAuth.getInstance().currentUser?.uid)
                            Arrangement.End else Arrangement.Start
                    ) {
                        Text(
                            text = msg.message,
                            modifier = Modifier
                                .padding(6.dp)
                                .background(Color(0xFFEDEDED), shape = MaterialTheme.shapes.small)
                                .padding(10.dp),
                            color = Color.Black
                        )
                    }
                }
            }

        }

        // 입력창
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .background(Color(0xFFF4F4F4))
                    .padding(8.dp)
            )
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                }
            ) {
                Text("전송")
            }
        }
    }
}
