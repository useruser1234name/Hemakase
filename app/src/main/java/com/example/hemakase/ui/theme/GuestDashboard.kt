// GuestDashboard.kt - 예약 선택 및 시간 선택 + 확인 버튼

package com.example.hemakase.ui.theme

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hemakase.navigator.DashboardBottomBar
import com.example.hemakase.viewmodel.GuestDashboardViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.hemakase.viewmodel.CustomCalendarUI
import com.example.hemakase.viewmodel.TimeSelectDialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(viewModel: GuestDashboardViewModel = viewModel()) {

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showCustomTimeDialog by remember { mutableStateOf(false) }
    var rescheduleCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.referencePhotoUri = it }
        }

    LaunchedEffect(Unit) {
        viewModel.loadLatestReservation()
        Log.d("예약", "loadLatestReservation 호출됨")

    }

    Scaffold(
        containerColor = Color.White,
        topBar = { DashboardTopBar() },
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
                customerName = viewModel.stylistName.ifBlank { "미용사 없음" },
                salonName = viewModel.salonName.ifBlank { "미용실 없음" },
                reservationDate = viewModel.run {
                    if (reservedYear != null && reservedMonth != null && reservedDay != null)
                        formatReservationDate(reservedYear!!, reservedMonth!!, reservedDay!!)
                    else "예약 없음"
                },
                reservationTime = viewModel.reservedTime.ifBlank { "시간 없음" },
                onRescheduleClick = {
                    showDatePicker = true
                },
                viewModel = viewModel,
                context = context
            )

            Spacer(modifier = Modifier.height(24.dp))

            CustomCalendarUI(
                selectedMonth = viewModel.selectedMonth,
                selectedYear = viewModel.selectedYear,
                selectedDay = viewModel.selectedDay,
                onDaySelected = {
                    viewModel.selectedDay = it
                    viewModel.selectedTime = ""
                    viewModel.loadReservedSlots(
                        salonId = viewModel.salonId,
                        stylistId = viewModel.stylistName
                    )
                },
                onMonthChange = { viewModel.selectedMonth = it },
                onYearChange = { viewModel.selectedYear = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            val timeSlots = listOf(
                "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00",
                "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
                "17:00", "17:30", "18:00", "18:30", "19:00", "19:30"
            )
            val availableTimeSlots =
                timeSlots.filterNot { viewModel.reservedTimeSlots.contains(it) }

            Text(
                "예약 시간 선택",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                timeSlots.forEach { time ->
                    val isReserved = viewModel.reservedTimeSlots.contains(time)
                    OutlinedButton(
                        onClick = { if (!isReserved) viewModel.selectedTime = time },
                        enabled = !isReserved,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (viewModel.selectedTime == time) Color.Black else Color.White,
                            contentColor = if (viewModel.selectedTime == time) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color.Black),
                    ) {
                        Text(text = time, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("시술 선택", fontWeight = FontWeight.SemiBold)

            if (viewModel.treatmentCategories.isNotEmpty()) {
                val selectedCategory = viewModel.selectedCategory
                val selectedIndex = viewModel.treatmentCategories.indexOf(selectedCategory)

                ScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    edgePadding = 8.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedIndex])
                                .height(2.dp),
                            color = Color.Black
                        )
                    },
                    divider = {}
                ) {
                    viewModel.treatmentCategories.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedIndex == index,
                            onClick = { viewModel.selectedCategory = category },
                            text = { Text(category, color = Color.Black) }
                        )
                    }
                }
            }

// 해당 카테고리에 속한 시술 카드 목록 표시
            viewModel.filteredTreatmentList.forEach { treatment ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { viewModel.selectedTreatment = treatment }
                        .then(
                            if (viewModel.selectedTreatment == treatment)
                                Modifier.border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                            else Modifier
                        ),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.selectedTreatment == treatment)
                            Color(0xFFE0E0E0)
                        else Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "시술명: ${treatment.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("가격: ${treatment.price}원", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "설명: ${treatment.description}",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }



            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.sameAsLastTime = !viewModel.sameAsLastTime }
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (viewModel.sameAsLastTime) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = null
                )
                Text("이전처럼 잘라주세요", modifier = Modifier.padding(start = 8.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.quietMode = !viewModel.quietMode }
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (viewModel.quietMode) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = null
                )
                Text("조용한 시술을 원해요", modifier = Modifier.padding(start = 8.dp))
            }


            Spacer(modifier = Modifier.height(8.dp))

            Text("참고 이미지", fontWeight = FontWeight.SemiBold)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. 내 사진
                viewModel.userProfilePhotoUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "내 사진",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    )
                }

                // 2. 참고 이미지 or + 박스
                if (viewModel.referencePhotoUri == null) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                            .clickable {
                                // 포토피커 실행
                                launcher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", fontSize = 32.sp, color = Color.Gray)
                    }
                } else {
                    AsyncImage(
                        model = viewModel.referencePhotoUri,
                        contentDescription = "참고 이미지",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            var showConfirmDialog by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    showConfirmDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("예약 확인", color = Color.White)
            }

            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text("예약 확인") },
                    text = { Text("이 예약을 미용실에 요청하시겠습니까?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showConfirmDialog = false
                            viewModel.submitReservation(
                                onSuccess = {
                                    Toast.makeText(context, "예약 요청이 완료되었습니다.", Toast.LENGTH_SHORT)
                                        .show()
                                },
                                onFailure = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }) {
                            Text("확인")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text("취소")
                        }
                    }
                )
            }
        }
    }

    if (showDatePicker) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                rescheduleCalendar.set(Calendar.YEAR, year)
                rescheduleCalendar.set(Calendar.MONTH, month)
                rescheduleCalendar.set(Calendar.DAY_OF_MONTH, day)
                showDatePicker = false
                showCustomTimeDialog = true
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showCustomTimeDialog) {
        TimeSelectDialog(
            reservedSlots = viewModel.reservedTimeSlots,
            onDismiss = { showCustomTimeDialog = false },
            onTimeSelected = { selectedTime ->
                rescheduleCalendar.set(
                    Calendar.HOUR_OF_DAY,
                    selectedTime.substringBefore(":").toInt()
                )
                rescheduleCalendar.set(Calendar.MINUTE, 0)
                rescheduleCalendar.set(Calendar.SECOND, 0)
                rescheduleCalendar.set(Calendar.MILLISECOND, 0)
                showCustomTimeDialog = false

                viewModel.rescheduleReservation(
                    newDateTime = rescheduleCalendar,
                    onSuccess = {
                        Toast.makeText(context, "예약이 수정되었습니다.", Toast.LENGTH_SHORT).show()

                        viewModel.notifyReservationChange(
                            reservation = viewModel.latestReservation,
                            isCancel = false
                        )
                    },
                    onFailure = {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}


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
                containerColor = Color.White
            )
        )
        Divider(thickness = 1.dp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun Nextmyreservation(
    customerName: String,
    salonName: String,
    reservationDate: String,
    reservationTime: String,
    onRescheduleClick: () -> Unit,
    viewModel: GuestDashboardViewModel,
    context: Context
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(4.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = customerName, color = Color.Gray, fontSize = 15.sp)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = reservationDate, color = Color.Gray, fontSize = 15.sp)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = reservationTime, color = Color.Gray, fontSize = 15.sp)
                }
            }

            Divider(thickness = 1.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    "Reschedule",
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onRescheduleClick() })
                Text(
                    "Cancel",
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        viewModel.cancelReservation(
                            onSuccess = {
                                Toast.makeText(context, "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()

                                // 🔔 알림 전송
                                viewModel.notifyReservationChange(
                                    reservation = viewModel.latestReservation,
                                    isCancel = true
                                )
                            },
                            onFailure = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )


                Text("Add Note", fontSize = 14.sp)
            }
        }
    }
}

fun formatReservationDate(year: Int, month: Int, day: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, day)
    val date = calendar.time
    val formatter = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREAN)
    return formatter.format(date)
}
