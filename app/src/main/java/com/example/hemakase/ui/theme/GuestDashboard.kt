@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.hemakase.ui.theme

import java.text.SimpleDateFormat
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.hemakase.R
import com.example.hemakase.navigator.DashboardBottomBar
import java.time.LocalDate
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hemakase.viewmodel.RegisterViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.TimeZone

@Preview(showBackground = true)
@Composable
fun DashboardcreenPreview() {
    DashboardScreen()
}


@Composable
fun DashboardScreen(registerViewModel: RegisterViewModel = viewModel()) {

    val today = LocalDate.now()
    var selectedMonth by remember { mutableStateOf(today.monthValue) }
    var selectedYear by remember { mutableStateOf(today.year) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var selectedTime by remember { mutableStateOf("") }

    var reservedYear by remember { mutableStateOf<Int?>(null) }
    var reservedMonth by remember { mutableStateOf<Int?>(null) }
    var reservedDay by remember { mutableStateOf<Int?>(null) }
    var reservedTime by remember { mutableStateOf("") }

    var showBookingUI by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var rescheduleDate by remember { mutableStateOf<java.util.Calendar?>(null) }
    var isRescheduling by remember { mutableStateOf(false) }

    val formattedReservedDate = reservedYear?.let { y ->
        reservedMonth?.let { m ->
            reservedDay?.let { d ->
                formatReservationDate(y, m, d)
            }
        }
    } ?: "예약 없음"


    val scrollState = rememberScrollState()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var salonName by remember { mutableStateOf("") }
    var stylistName by remember { mutableStateOf("") }
    var salonId by remember { mutableStateOf<String?>(null) }
    var customerName by remember { mutableStateOf("") }
    var customerPhoto by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // 앱 시작 시 가장 가까운 예약 정보 불러오기
    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect
            Log.d("예약불러오기", "현재 uid: $uid")

            val snapshot = db.collection("reservations")
                .whereEqualTo("customer_id", uid)
                .orderBy("date") // 날짜 기준 정렬
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val reservation = snapshot.documents[0]
                val timestamp = reservation.getTimestamp("date")?.toDate()

                if (timestamp != null) {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = timestamp

                    reservedYear = calendar.get(java.util.Calendar.YEAR)
                    reservedMonth = calendar.get(java.util.Calendar.MONTH) + 1
                    reservedDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    reservedTime = SimpleDateFormat("HH:mm", Locale.KOREAN).format(timestamp)

                    Log.d(
                        "예약불러오기",
                        "불러온 예약: $reservedYear-$reservedMonth-$reservedDay $reservedTime"
                    )
                } else {
                    Log.e("예약불러오기", "timestamp null! date 필드가 Timestamp 형식인지 확인하세요.")
                }

                // 예약 정보에서 나머지 필드 추출
                customerName = reservation.getString("customer_name") ?: ""
                customerPhoto = reservation.getString("customer_photo")

                // 미용실 이름 가져오기
                val salonIdFromRes = reservation.getString("salonId")
                salonIdFromRes?.let {
                    val salonDoc = db.collection("salons").document(it).get().await()
                    salonName = salonDoc.getString("name") ?: ""
                }
            } else {
                Log.w("예약불러오기", "예약이 없습니다.")
            }

        } catch (e: Exception) {
            Log.e("예약불러오기", "에러 발생: ${e.message}")
        }
    }



    LaunchedEffect(showDialog) {
        if (showDialog) {
            try {
                val uid = auth.currentUser?.uid ?: return@LaunchedEffect
                val userDoc = db.collection("users").document(uid).get().await()
                val userData = userDoc.data
                salonId = userData?.get("salonId") as? String
                stylistName = userData?.get("stylistName") as? String ?: ""
                customerName = userData?.get("name") as? String ?: ""
                customerPhoto = userData?.get("photo") as? String


                salonId?.let {
                    val salonDoc = db.collection("salons").document(it).get().await()
                    salonName = salonDoc.getString("name") ?: ""
                }
            } catch (e: Exception) {
                Log.e("Dialog", "Firestore Error: ${e.message}")
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = { DashboardTopBar() },
        bottomBar = { DashboardBottomBar() },

        ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Nextmyreservation(
                customerName = if (customerName.isNotBlank()) customerName else "고객 없음",
                salonName = if (salonName.isNotBlank()) salonName else "미용실 없음",
                reservationDate = if (reservedDay != null && reservedMonth != null && reservedYear != null)
                    formatReservationDate(reservedYear!!, reservedMonth!!, reservedDay!!)
                else "예약 없음",
                reservationTime = if (reservedTime.isNotBlank()) reservedTime else "시간 없음",
                onRescheduleClick = {
                    isRescheduling = true
                    showDatePicker = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            CustomCalendarUI(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                selectedDay = selectedDay,
                onDaySelected = {
                    selectedDay = it
                    selectedTime = ""
                    showBookingUI = true
                },
                onMonthChange = { selectedMonth = it },
                onYearChange = { selectedYear = it }
            )

            val reservedTimeSlots = remember { mutableStateListOf<String>() }
            val isReservedLoaded = remember { mutableStateOf(false) }

            LaunchedEffect(selectedDay, selectedMonth, selectedYear, salonId, stylistName) {
                isReservedLoaded.value = false
                val selectedDayVal = selectedDay

                if (selectedDayVal != null && salonId != null && stylistName.isNotEmpty()) {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth - 1)
                        set(Calendar.DAY_OF_MONTH, selectedDayVal)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }

                    val startOfDay = Timestamp(calendar.time)
                    calendar.add(Calendar.DATE, 1)
                    val endOfDay = Timestamp(calendar.time)

                    val snapshot = FirebaseFirestore.getInstance()
                        .collection("reservations")
                        .whereEqualTo("salonId", salonId)
                        .whereEqualTo("stylist_id", stylistName)
                        .whereGreaterThanOrEqualTo("date", startOfDay)
                        .whereLessThan("date", endOfDay)
                        .whereEqualTo("status", "confirmed")
                        .get()
                        .await()

                    val formatter = SimpleDateFormat("HH:mm", Locale.KOREAN)
                    formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")

                    reservedTimeSlots.clear()
                    reservedTimeSlots.addAll(snapshot.documents.mapNotNull {
                        it.getTimestamp("date")?.toDate()?.let { d ->
                            formatter.format(d).trim()
                        }
                    })

                    isReservedLoaded.value = true // 로딩 완료
                }
            }

            if (showBookingUI && selectedDay != null) {
                val timeSlots = listOf(
                    "10:00", "11:00", "12:00", "13:00", "14:00",
                    "15:00", "16:00", "17:00", "18:00", "19:00"
                )

                Column(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                        .background(Color(0xFFF8F8F8), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "예약 시간 선택",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        timeSlots.forEach { time ->
                            val isReserved = if (isReservedLoaded.value) {
                                reservedTimeSlots.any { it.trim() == time }
                            } else {
                                false
                            }

                            OutlinedButton(
                                onClick = { if (!isReserved) selectedTime = time },
                                enabled = !isReserved,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedTime == time) Color.Black else Color.White,
                                    contentColor = if (selectedTime == time) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color.Black),
                                modifier = if (isReserved) Modifier.alpha(0.3f) else Modifier
                            ) {
                                Text(
                                    time,
                                    color = if (isReserved) Color.Gray else Color.Black,
                                    fontWeight = if (isReserved) FontWeight.Normal else FontWeight.SemiBold
                                )
                            }
                        }
                    }



                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("예약 확인", color = Color.White)
                    }
                }
            }

            if (showDialog) {

                val coroutineScope = rememberCoroutineScope()

                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = {
                        Text(
                            text = "예약 확인",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "날짜: ${selectedYear}년 ${selectedMonth}월 ${selectedDay}일",
                                color = Color.Black
                            )
                            Text("시간: $selectedTime", color = Color.Black)
                            Text("미용실: $salonName", color = Color.Black)
                            Text("미용사: $stylistName", color = Color.Black)
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val uid = auth.currentUser?.uid ?: return@launch

                                    try {
                                        val reservationTime = "$selectedYear-${"%02d".format(selectedMonth)}-${"%02d".format(selectedDay)} $selectedTime"
                                        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREAN)
                                        val parsedDate = formatter.parse(reservationTime)
                                        val calendar = Calendar.getInstance().apply {
                                            time = parsedDate!!
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        val timestamp = Timestamp(calendar.time)

                                        val userDoc = db.collection("users").document(uid).get().await()
                                        val stylistId = userDoc.getString("stylistId")

                                        // 예약 중복 여부 체크
                                        val existing = db.collection("reservations")
                                            .whereEqualTo("salonId", salonId)
                                            .whereEqualTo("stylist_id", stylistId)
                                            .whereEqualTo("date", timestamp)
                                            .whereEqualTo("status", "confirmed")
                                            .get()
                                            .await()
                                        // [2] 이미 예약된 시간
                                        if (!existing.isEmpty) {
                                            Log.w("예약", "이미 예약된 시간입니다.")
                                            showDialog = false
                                            // → 토스트나 에러 처리 추가도 가능
                                            return@launch
                                        }

                                        val reservation = hashMapOf(
                                            "customer_id" to uid,
                                            "customer_name" to customerName,
                                            "customer_photo" to customerPhoto,
                                            "stylist_id" to stylistId,
                                            "date" to timestamp,
                                            "status" to "pending",
                                            "style" to "",
                                            "note" to "",
                                            "reference_photo" to null,
                                            "salonId" to salonId
                                        )

                                        db.collection("reservations")
                                            .add(reservation)
                                            .addOnSuccessListener {
                                                Log.d("Firestore", "예약 저장 성공")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("Firestore", "예약 저장 실패: ${e.message}")
                                            }

                                        showDialog = false

                                    } catch (e: Exception) {
                                        Log.e("예약 오류", "날짜 파싱 오류: ${e.message}")
                                    }
                                }
                            }
                        ) {
                            Text("확정", color = Color.Black)
                        }
                    }
                    ,
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("취소", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFFFDFDFD),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // 날짜 선택 다이얼로그
        if (showDatePicker && isRescheduling) {
            val now = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(
                context,
                { _, year, month, day ->
                    rescheduleDate = java.util.Calendar.getInstance().apply {
                        set(year, month, day)
                    }
                    showDatePicker = false
                    showTimePicker = true // 날짜 선택 후 시간 선택으로 이동
                },
                now.get(java.util.Calendar.YEAR),
                now.get(java.util.Calendar.MONTH),
                now.get(java.util.Calendar.DAY_OF_MONTH)
            ).show()
        }

// 시간 선택 다이얼로그
        if (showTimePicker && rescheduleDate != null && isRescheduling) {
            val now = java.util.Calendar.getInstance()
            android.app.TimePickerDialog(
                context,
                { _, hour, minute ->
                    rescheduleDate?.apply {
                        set(java.util.Calendar.HOUR_OF_DAY, hour)
                        set(java.util.Calendar.MINUTE, minute)
                    }

                    // Firebase 예약 업데이트
                    val uid = auth.currentUser?.uid ?: return@TimePickerDialog
                    val newTimestamp = com.google.firebase.Timestamp(rescheduleDate!!.time)

                    db.collection("reservations")
                        .whereEqualTo("customer_id", uid)
                        .orderBy("date")
                        .limit(1)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty) {
                                val docRef = snapshot.documents[0].reference
                                docRef.update("date", newTimestamp)
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "예약 시간 수정 완료")
                                        reservedYear = rescheduleDate!!.get(java.util.Calendar.YEAR)
                                        reservedMonth = rescheduleDate!!.get(java.util.Calendar.MONTH) + 1
                                        reservedDay = rescheduleDate!!.get(java.util.Calendar.DAY_OF_MONTH)
                                        reservedTime = SimpleDateFormat("HH:mm", Locale.KOREAN).format(rescheduleDate!!.time)
                                    }
                            }
                        }

                    showTimePicker = false
                    isRescheduling = false
                },
                now.get(java.util.Calendar.HOUR_OF_DAY),
                now.get(java.util.Calendar.MINUTE),
                true
            ).show()
        }

    }
}


// ─────────────────────────────────────────────────────
// (A) 상단 앱바
// ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar() {
    Column {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Dashboard",
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


// ─────────────────────────────────────────────────────
// (B) Next Client 섹션
// ─────────────────────────────────────────────────────
@Composable
fun Nextmyreservation(
    customerName: String,
    salonName: String,
    reservationDate: String,
    reservationTime: String,
    onRescheduleClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(4.dp)
            )
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 첫 번째 컬럼
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "Person",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = customerName,
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
                // 두 번째 컬럼
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "Calendar",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reservationDate,
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
                // 세 번째 컬럼
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clock),
                        contentDescription = "Clock",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reservationTime,
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            }

            // 가운데 가로 Divider
            Divider(thickness = 1.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(18.dp))

            // 하단 Row: Reschedule / Add Service / Add Note
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Reschedule", //예약 수정
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.clickable { onRescheduleClick() } //콜백 호출

                )
 //
                // 세로 Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.05f)
                        .background(Color.LightGray)
                )

                Text(
                    text = "cancel", // 예약 취소
                    fontSize = 14.sp,
                    color = Color.Black
                )

                // 세로 Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.05f)
                        .background(Color.LightGray)
                )

                Text(
                    text = "Add Note", //요청사항 수정
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}


// ─────────────────────────────────────────────────────
// (C) Calendar 섹션
// ─────────────────────────────────────────────────────
@Composable
fun CustomCalendarUI(
    selectedMonth: Int,
    selectedYear: Int,
    selectedDay: Int?,
    onDaySelected: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit
) {

    // 현재 월의 최대 날짜: 2월=28, 4·6·9·11=30, 그 외=31
    val validDays = when (selectedMonth) {
        2 -> if ((selectedYear % 4 == 0 && selectedYear % 100 != 0) || selectedYear % 400 == 0) 29 else 28
        in listOf(4, 6, 9, 11) -> 30
        else -> 31
    }

    val today = LocalDate.now()
    val isThisMonth = today.monthValue == selectedMonth && today.year == selectedYear

    val firstDayOfMonth = LocalDate.of(selectedYear, selectedMonth, 1)
    val startDayOfWeek = (firstDayOfMonth.dayOfWeek.value + 6) % 7 // Monday=0 ~ Sunday=6

    val dayCells = (1..validDays).map { it.toString() }
    val leadingBlanks = List(startDayOfWeek) { "" }
    val totalCells = 42
    val trailingBlanks = List(totalCells - (leadingBlanks.size + dayCells.size)) { "" }

    val calendarCells = leadingBlanks + dayCells + trailingBlanks
    val weeks = calendarCells.chunked(7)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // 상단 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calendar",
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp
            )
            MonthDropdown(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthSelected = onMonthChange,
                onYearSelected = onYearChange
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 달력 박스 (테두리 + 내부 패딩)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(5.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 요일 헤더 (월~일)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

                    val dayColors = listOf(
                        Color.Black,  // Monday
                        Color.Black,  // Tuesday
                        Color.Black,  // Wednesday
                        Color.Black,  // Thursday
                        Color.Black,  // Friday
                        Color.Blue,   // Saturday
                        Color.Red     // Sunday
                    )

                    dayNames.forEachIndexed { index, dayName ->
                        Text(
                            text = dayName,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = dayColors[index],
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 날짜 그리드 (6주=42칸)
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        week.forEach { day ->
                            val isValid = day.isNotBlank()
                            val dayNumber = day.toIntOrNull()
                            val isSelected = isValid && dayNumber == selectedDay
                            val isToday = isValid && isThisMonth && dayNumber == today.dayOfMonth

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .background(
                                        if (isSelected) Color.Black else Color.Transparent
                                    )
                                    .then(if (isValid) Modifier.clickable { onDaySelected(dayNumber!!) } else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = day,
                                        fontSize = 14.sp,
                                        color = when {
                                            !isValid -> Color.Gray
                                            isSelected -> Color.White
                                            else -> Color.Black
                                        },
                                        textAlign = TextAlign.Center
                                    )
                                    if (isToday) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .height(2.dp)
                                                .width(20.dp)
                                                .background(
                                                    Color.Red,
                                                    shape = RoundedCornerShape(1.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun MonthDropdown(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthSelected: (Int) -> Unit,
    onYearSelected: (Int) -> Unit
) {
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {

        Box {
            Row(
                modifier = Modifier
                    .clickable { yearExpanded = true }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$selectedYear 년", fontSize = 14.sp)
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
            DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                val currentYear = LocalDate.now().year
                ((currentYear - 10)..(currentYear + 10)).forEach {
                    DropdownMenuItem(
                        text = { Text("$it 년") },
                        onClick = {
                            onYearSelected(it)
                            yearExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box {
            Row(
                modifier = Modifier
                    .clickable { monthExpanded = true }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$selectedMonth 월", fontSize = 14.sp)
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
            DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                (1..12).forEach {
                    DropdownMenuItem(
                        text = { Text("$it 월") },
                        onClick = {
                            onMonthSelected(it)
                            monthExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun formatReservationDate(year: Int, month: Int, day: Int): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(year, month - 1, day) // Calendar는 0부터 시작해서 month-1

    val date = calendar.time
    val formatter = java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.ENGLISH)
    return formatter.format(date) // 예: "Mon, Aug 12"
}
