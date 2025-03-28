package com.example.hemakase.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.hemakase.R
import com.example.hemakase.navigator.DashboardBottomBar
import com.example.hemakase.navigator.StylistBottomBar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ServiceItem(
    val name: String,
    val price: String
)

//@Preview(showBackground = true, name = "Clients Tab Preview")
//@Composable
//fun BarberSettingsScreenClientsTabPreview() {
//    // 강제로 Clients 탭을 선택한 Preview용
//    BarberSettingsScreenClientsTab()
//}

// Clients 탭을 고정한 컴포저블 (Preview 전용)
@Composable
fun BarberSettingsScreenClientsTab(
//    selectedTab: Int,
//    onTabSelected: (Int) -> Unit
) {

    var selectedInternalTab by remember { mutableStateOf(0) }

    // 샘플 서비스 리스트
    val serviceList = listOf(
        ServiceItem("Haircut", "18,000"),
        ServiceItem("Hair wash", "$$"),
        ServiceItem("Beard trim", "$")
    )

    Scaffold(
        containerColor = Color.White,
//        topBar = { SettingTopBar() },
//        bottomBar = { StylistBottomBar(
//            selectedTab = selectedTab,
//            onTabSelected = onTabSelected
//        ) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // (1) 프로필 섹션
            ProfileSection(
                imageUrl = "",  // 실제 URL 또는 리소스
                name = "John Daniel",
            )

            // (2) 탭 (Barber / Clients), 하지만 selectedTab=1로 고정
            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedInternalTab,
                containerColor = Color.White
            ) {
                Tab(
                    selected = selectedInternalTab == 0,
                    onClick = { selectedInternalTab = 0 },
                    text = { Text("Barber") }
                )
                Tab(
                    selected = selectedInternalTab == 1,
                    onClick = { selectedInternalTab = 1 },
                    text = { Text("Clients") }
                )
            }

            when (selectedInternalTab) {
                0 -> BarberTabContent(serviceList)
                1 -> ClientsTabContent()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingTopBar() {
    Column {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Settings",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White // 앱바 배경 흰색
            )
        )
        Divider(
            thickness = 1.dp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ProfileSection(
    imageUrl: String,
    name: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 예: 프로필 이미지 (동그란 형태)
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            // 실제로는 AsyncImage / Coil 등 사용해서 imageUrl 로드
            Text("Photo")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun ServiceRow(item: ServiceItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 왼쪽 서비스명
        Text(text = item.name, fontSize = 14.sp)
        // 가운데 가격
        Text(text = item.price, fontSize = 14.sp)
        // 오른쪽 수정 아이콘
        Icon(
            painter = painterResource(id = R.drawable.edit), // 실제 아이콘 리소스
            contentDescription = "Edit",
            modifier = Modifier
                .size(20.dp)
                .clickable {
                    // TODO: 수정 로직
                }
        )
    }
}

@Composable
fun BarberTabContent(serviceList: List<ServiceItem>) {
    Spacer(modifier = Modifier.height(16.dp))

    Column {

        ReservationApprovalList()
        Spacer(modifier = Modifier.height(24.dp))

        // 1) Time
        var timeText by remember { mutableStateOf("8 AM - 9 PM") }
        OutlinedTextField(
            value = timeText,
            onValueChange = { timeText = it },
            readOnly = true, // 기본적으로 편집 불가, trailingIcon 클릭 시 편집 가능하도록
            label = { Text("Time") },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.edit),
                    contentDescription = "Edit",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            // TODO: 편집 로직 (ex: Dialog 등으로 입력받기)
                        }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2) Days
        var daysText by remember { mutableStateOf("Monday - Friday") }
        OutlinedTextField(
            value = daysText,
            onValueChange = { daysText = it },
            readOnly = true,
            label = { Text("Days") },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.edit),
                    contentDescription = "Edit",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            // TODO: 편집 로직
                        }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3) Hairstyle
        var hairstyleText by remember { mutableStateOf("Haircut, Hair wash, Beard trim.") }
        OutlinedTextField(
            value = hairstyleText,
            onValueChange = { hairstyleText = it },
            readOnly = true,
            label = { Text("Hairstyle") },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.edit),
                    contentDescription = "Edit",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            // TODO: 편집 로직
                        }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }


    Spacer(modifier = Modifier.height(16.dp))

    // 기존 서비스 리스트 박스
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(4.dp))
            .background(Color.White)
            .padding(8.dp)
    ) {
        Column {
            serviceList.forEachIndexed { index, service ->
                ServiceRow(service)
                if (index < serviceList.size - 1) {
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }
        }
    }
}


@Composable
fun ReservationApprovalList() {
    val db = FirebaseFirestore.getInstance()
    var reservations by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // 테스트용 오너 계정 UID 가져오기
    val ownerEmail = "sandpingping2@gmail.com"
    var ownerId by remember { mutableStateOf<String?>(null) }

    // Firestore에서 예약 로딩
    LaunchedEffect(Unit) {
        // 오너 UID 가져오기
        val userSnapshot = db.collection("users")
            .whereEqualTo("email", ownerEmail)
            .get()
            .await()
        ownerId = userSnapshot.documents.firstOrNull()?.getString("id")

        ownerId?.let { uid ->
            val salonSnapshot = db.collection("salons")
                .whereEqualTo("ownerId", uid)
                .get()
                .await()

            val salonId = salonSnapshot.documents.firstOrNull()?.id

            if (salonId != null) {
                val resSnapshot = db.collection("reservations")
                    .whereEqualTo("salonId", salonId)
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

                reservations = resSnapshot.documents.map { it.data!! + ("docId" to it.id) }
            }
        }
    }

    Column {
        Text("예약 승인 요청", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        reservations.forEach { reservation ->
            val customerName = reservation["customer_name"] as? String ?: "이름 없음"
            val time = (reservation["date"] as? com.google.firebase.Timestamp)?.toDate()?.toString()
            val docId = reservation["docId"] as String

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("고객: $customerName")
                    Text("시간: $time")
                }

                Row {
                    Text(
                        "수락",
                        modifier = Modifier
                            .clickable {
                                db.collection("reservations")
                                    .document(docId)
                                    .update("status", "confirmed")
                            }
                            .padding(horizontal = 8.dp),
                        color = Color.Green
                    )
                    Text(
                        "거절",
                        modifier = Modifier
                            .clickable {
                                db.collection("reservations")
                                    .document(docId)
                                    .update("status", "rejected")
                            }
                            .padding(horizontal = 8.dp),
                        color = Color.Red
                    )
                }
            }
            Divider()
        }
    }
}
