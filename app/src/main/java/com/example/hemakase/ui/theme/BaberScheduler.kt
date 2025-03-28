@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.hemakase.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.hemakase.navigator.DashboardBottomBar
import com.example.hemakase.navigator.StylistBottomBar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


data class Appointment(
    val timeLabel: String,    // 예: "시간"
    val clientName: String,   // 예: "이름"
    val serviceInfo: String,  // 예: "Haircut & Beard"
    val timeRange: String,    // 예: "12 AM - 1 PM with Agolsh"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaberSchedulerScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)")
    val formattedDate = selectedDate.format(dateFormatter)

    LaunchedEffect(selectedDate) {
        getAppointmentsByDate(selectedDate) {
            appointments = it
        }
    }

    Scaffold(
        containerColor = Color.White,
//        topBar = { DashboardTopBar() },
//        bottomBar = { StylistBottomBar(
//            selectedTab = selectedTab,
//            onTabSelected = onTabSelected
//        ) },

    ) { innerPadding ->
        // 샘플 데이터
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedDate = selectedDate.minusDays(1) }) {
                    Text(text = "←")
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { selectedDate = selectedDate.plusDays(1) }) {
                    Text(text = "→")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // (2) 큰 박스 안에 모든 예약 표시 + 항목 사이 Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(Color.White)
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    appointments.forEachIndexed { index, appointment ->
                        AgendaItem(appointment)
                        // 마지막 아이템이 아닐 때만 Divider
                        if (index < appointments.size - 1) {
                            Divider(color = Color.LightGray, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AgendaItem(appointment: Appointment) {
    // 한 항목을 Row로 표시
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        // 왼쪽 시간 레이블
        Text(
            text = appointment.timeLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(50.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // 오른쪽 예약 상세
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appointment.clientName,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = appointment.serviceInfo,
                color = Color.Gray
            )
            Text(
                text = appointment.timeRange,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────
// (E) 미리보기
// ─────────────────────────────────────────────────────
//@Preview(showBackground = true)
//@Composable
//fun DashboardScreenPreview() {
//    BaberSchedulerScreen()
//}

fun getAppointmentsByDate(
    selectedDate: LocalDate,
    onResult: (List<Appointment>) -> Unit
) {
    val db = Firebase.firestore

    val startOfDay = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
    val endOfDay = selectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

    db.collection("reservations")
        .whereGreaterThanOrEqualTo("date", Timestamp(startOfDay.epochSecond, 0))
        .whereLessThan("date", Timestamp(endOfDay.epochSecond, 0))
        .get()
        .addOnSuccessListener { result ->
            val appointments = result.documents.mapNotNull { doc ->
                val customerName = doc.getString("customer_name") ?: return@mapNotNull null
                val stylist = doc.getString("stylist_id") ?: "Unknown"
                val date = doc.getTimestamp("date")?.toDate() ?: return@mapNotNull null

                // 시간 정보 구성
                val timeLabel = SimpleDateFormat("h a", Locale.getDefault()).format(date)
                val timeRange = SimpleDateFormat("h:mm a", Locale.getDefault()).format(date) +
                        " - " + SimpleDateFormat("h:mm a", Locale.getDefault())
                    .format(Date(date.time + 30 * 60 * 1000)) + " with $stylist"

                Appointment(
                    timeLabel = timeLabel,
                    clientName = customerName,
                    serviceInfo = "Haircut & Beard",
                    timeRange = timeRange
                )
            }

            onResult(appointments)
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}
