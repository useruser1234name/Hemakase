@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.hemakase.ui.theme

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.hemakase.R
import com.example.hemakase.navigator.DashboardBottomBar
import java.time.LocalDate
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hemakase.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
    var showBookingUI by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var salonName by remember { mutableStateOf("") }
    var stylistName by remember { mutableStateOf("") }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            try {
                val uid = auth.currentUser?.uid ?: return@LaunchedEffect
                val userDoc = db.collection("users").document(uid).get().await()
                val userData = userDoc.data
                val salonId = userData?.get("salonId") as? String
                stylistName = userData?.get("name") as? String ?: ""

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            NextClientSection()

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

            if (showBookingUI && selectedDay != null) {
                val timeSlots = listOf("10:00", "11:00", "12:00", "13:00", "14:00",
                    "15:00", "16:00", "17:00", "18:00", "19:00")

                Column(modifier = Modifier
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
                            OutlinedButton(
                                onClick = { selectedTime = time },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedTime == time) Color.Black else Color.White,
                                    contentColor = if (selectedTime == time) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color.Black)
                            ) {
                                Text(time)
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
                            Text("날짜: ${selectedYear}년 ${selectedMonth}월 ${selectedDay}일", color = Color.Black)
                            Text("시간: $selectedTime", color = Color.Black)
                            Text("미용실: $salonName", color = Color.Black)
                            Text("미용사: $stylistName", color = Color.Black)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("확정", color = Color.Black)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("취소", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFFFDFDFD),
                    shape = RoundedCornerShape(16.dp)
                )
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
fun NextClientSection() {

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
                        text = "John.K",
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
                        text = "Mon, Aug 12",
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
                        text = "1 PM",
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
                    text = "Reschedule",
                    fontSize = 14.sp,
                    color = Color.Black
                )

                // 세로 Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.05f)
                        .background(Color.LightGray)
                        .padding(top = 18.dp, bottom = 18.dp)
                )

                Text(
                    text = "Add Service",
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
                    text = "Add Note",
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

    val selectedDayVal = selectedDay

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
