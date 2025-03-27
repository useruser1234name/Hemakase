@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.hemakase.ui.theme

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            NextClientSection()

            Spacer(modifier = Modifier.height(24.dp))

            // (2) Calendar 섹션
            CustomCalendarUI()

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
fun CustomCalendarUI() {
    // 현재 월 (1~12)
    var currentMonth by remember { mutableStateOf(1) }
    // 현재 월의 최대 날짜: 2월=28, 4·6·9·11=30, 그 외=31
    val validDays = when (currentMonth) {
        2 -> 28
        in listOf(4, 6, 9, 11) -> 30
        else -> 31
    }

    // 총 42칸(6주) 표시
    val totalCells = 42
    // 1..42
    val days = (1..totalCells).toList()
    // 7개씩 잘라서 주 단위로
    val weeks = days.chunked(7)

    // 선택된 날짜 (기본값 11)
    var selectedDay by remember { mutableStateOf(11) }

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
            MonthDropdown(selectedMonth = currentMonth) { month ->
                currentMonth = month
            }
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
                // 요일 헤더 (S, M, T, W, T, F, S)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")
                    dayNames.forEach { dayName ->
                        Text(
                            text = dayName,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
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
                            // 현재 달 유효 날짜인지 체크
                            val isValid = day <= validDays
                            // 유효하지 않은 날짜 -> 다음 달 날짜( day - validDays ), 여기선 배경 투명
                            val displayText = if (isValid) "$day" else "${day - validDays}"

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    // 유효한 날짜 + 선택된 날짜만 검정 배경
                                    .background(
                                        if (isValid && day == selectedDay) Color.Black
                                        else Color.Transparent
                                    )
                                    // 유효 날짜만 클릭 가능
                                    .then(
                                        if (isValid) Modifier.clickable { selectedDay = day }
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayText,
                                    fontSize = 14.sp,
                                    // 유효하지 않은 날짜면 Gray 텍스트, 유효+선택이면 White, 그 외 Black
                                    color = when {
                                        !isValid -> Color.Gray
                                        day == selectedDay -> Color.White
                                        else -> Color.Black
                                    },
                                    textAlign = TextAlign.Center
                                )
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
fun MonthDropdown(selectedMonth: Int, onMonthSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$selectedMonth 월", fontSize = 14.sp)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select Month",
                modifier = Modifier.size(20.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..12).forEach { month ->
                DropdownMenuItem(
                    text = { Text("$month 월", fontSize = 14.sp) },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}


// ─────────────────────────────────────────────────────
// (D) 하단 바 (Bottom Navigation)
// ─────────────────────────────────────────────────────
@Composable
fun DashboardBottomBar() {
    Column {
        Divider(
            color = Color.LightGray,
            thickness = 1.dp
        )
        NavigationBar(
            containerColor = Color.White
        ) {
            NavigationBarItem(
                selected = false,
                onClick = { /*TODO*/ },
                icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null) },
                label = { Text("Dashboard") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { /*TODO*/ },
                icon = { Icon(imageVector = Icons.Default.Message, contentDescription = null) },
                label = { Text("Messages") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { /*TODO*/ },
                icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) },
                label = { Text("Settings") }
            )
        }
    }
}
