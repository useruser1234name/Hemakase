package com.example.hemakase.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hemakase.data.User
import com.example.hemakase.repository.FirebaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.*

@Composable
fun AddUserScreen(
    onSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("customer") }

    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: UUID.randomUUID().toString()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "사용자 등록", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("이메일") })
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "역할 선택:")
        Row {
            RadioButton(selected = role == "customer", onClick = { role = "customer" })
            Text("고객", modifier = Modifier.padding(end = 8.dp))
            RadioButton(selected = role == "stylist", onClick = { role = "stylist" })
            Text("디자이너")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val user = User(
                id = uid,
                name = name,
                email = email,
                role = role,
                photo = null,
                phone = null,
                created_at = Timestamp.now()
            )
            FirebaseRepository.addUser(
                user,
                onSuccess = onSuccess,
                onFailure = { e -> e.printStackTrace() }
            )
        }) {
            Text("등록하기")
        }
    }
}
