package com.example.hemakase.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.runtime.*


@Composable
fun GuestSettingsScreen() {
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid ?: return

    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }

    LaunchedEffect(uid) {
        val db = Firebase.firestore
        val today = Timestamp.now()

        db.collection("reservations")
            .whereEqualTo("customer_id", uid)
            .whereLessThan("date", today)
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    val date = doc.getTimestamp("date")?.toDate() ?: return@mapNotNull null
                    val stylist = doc.getString("stylist_name") ?: "알 수 없음"
                    val salon = doc.getString("salon_name") ?: "알 수 없음"
                    val status = doc.getString("status") ?: "unknown"
                    Reservation(date, stylist, salon, status)
                }
                reservations = list.sortedByDescending { it.date }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("내 정보", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("이름: ${user.displayName ?: "알 수 없음"}")
        Text("이메일: ${user.email ?: "없음"}")

        Spacer(Modifier.height(24.dp))

        Text("지난 예약", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        reservations.forEach { res ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Text("일시: ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(res.date)}")
                Text("미용사: ${res.stylist}")
                Text("미용실: ${res.salon}")
                Text("상태: ${res.status}")
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { FirebaseAuth.getInstance().signOut() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("로그아웃", color = MaterialTheme.colorScheme.onError)
        }
    }
}

data class Reservation(
    val date: Date,
    val stylist: String,
    val salon: String,
    val status: String
)
