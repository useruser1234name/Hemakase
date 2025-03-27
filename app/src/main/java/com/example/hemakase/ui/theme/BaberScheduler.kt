@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.hemakase.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.hemakase.navigator.DashboardBottomBar


data class Appointment(
    val timeLabel: String,    // 예: "시간"
    val clientName: String,   // 예: "이름"
    val serviceInfo: String,  // 예: "Haircut & Beard"
    val timeRange: String,    // 예: "12 AM - 1 PM with Agolsh"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaberSchedulerScreen() {
    Scaffold(
        containerColor = Color.White,
        topBar = { DashboardTopBar() },
        bottomBar = { DashboardBottomBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*TODO: 새 예약 추가 등*/ },
                shape = CircleShape,
                containerColor = Color.Black,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        // 샘플 데이터
        val appointments = listOf(
            Appointment("10 AM", "Osman Semedo", "Haircut & Beard", "6:25 PM - 7 PM with Arya"),
            Appointment(
                "11 AM",
                "Jamal Sane",
                "Haircut & Beard (VIP)",
                "7:15 PM - 8:15 PM with Agolsh"
            ),
            Appointment("1 PM", "Sam", "Haircut & Beard", "12 AM - 1 PM with Agolsh"),
            Appointment("2 PM", "John Doe", "Haircut & Beard", "1 PM - 1:45 PM with Arya"),
            Appointment("3 PM", "El King", "Haircut & Beard", "2 PM - 3 PM with Agolsh"),
            Appointment("4 PM", "Omron Samad", "Haircut (VIP)", "3:20 PM - 4 PM with Arya"),
            Appointment("5 PM", "Arya", "Haircut & Beard (VIP)", "4:15 PM - 4:55 PM with Agolsh"),
            Appointment("6 PM", "Joseph De", "Haircut & Beard", "5:20 PM - 6:20 PM with Agolsh"),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // (1) Next Client 섹션
            NextClientSection()

            Spacer(modifier = Modifier.height(24.dp))

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text("● Current date    ○ My client")
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
