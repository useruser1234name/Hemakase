// GuestDashboard.kt - 예약 선택 및 시간 선택 + 확인 버튼

package com.example.hemakase.ui.theme

import android.app.DatePickerDialog
import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
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

    LaunchedEffect(Unit) {
        viewModel.loadLatestReservation()
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
                customerName = viewModel.customerName.ifBlank { "고객 없음" },
                salonName = viewModel.salonName.ifBlank { "미용실 없음" },
                reservationDate = viewModel.run {
                    if (reservedYear != null && reservedMonth != null && reservedDay != null)
                        formatReservationDate(reservedYear!!, reservedMonth!!, reservedDay!!)
                    else "예약 없음"
                },
                reservationTime = viewModel.reservedTime.ifBlank { "시간 없음" },
                onRescheduleClick = {
                    showDatePicker = true
                }
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
                "10:00", "11:00", "12:00", "13:00", "14:00",
                "15:00", "16:00", "17:00", "18:00", "19:00"
            )

            val availableTimeSlots = timeSlots.filterNot { viewModel.reservedTimeSlots.contains(it) }

            Text(
                text = "예약 시간 선택",
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
                availableTimeSlots.forEach { time ->
                    OutlinedButton(
                        onClick = {
                            viewModel.selectedTime = time
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (viewModel.selectedTime == time) Color.Black else Color.White,
                            contentColor = if (viewModel.selectedTime == time) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color.Black),
                    ) {
                        Text(
                            text = time,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.submitReservation(
                        onSuccess = {
                            Toast.makeText(context, "예약 요청이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("예약 확인", color = Color.White)
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
                rescheduleCalendar.set(Calendar.HOUR_OF_DAY, selectedTime.substringBefore(":").toInt())
                rescheduleCalendar.set(Calendar.MINUTE, 0)
                rescheduleCalendar.set(Calendar.SECOND, 0)
                rescheduleCalendar.set(Calendar.MILLISECOND, 0)
                showCustomTimeDialog = false

                viewModel.rescheduleReservation(
                    newDateTime = rescheduleCalendar,
                    onSuccess = {
                        Toast.makeText(context, "예약이 수정되었습니다.", Toast.LENGTH_SHORT).show()
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
    onRescheduleClick: () -> Unit
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
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = customerName, color = Color.Gray, fontSize = 15.sp)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = reservationDate, color = Color.Gray, fontSize = 15.sp)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = reservationTime, color = Color.Gray, fontSize = 15.sp)
                }
            }

            Divider(thickness = 1.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Reschedule", fontSize = 14.sp, modifier = Modifier.clickable { onRescheduleClick() })
                Text("Cancel", fontSize = 14.sp)
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
