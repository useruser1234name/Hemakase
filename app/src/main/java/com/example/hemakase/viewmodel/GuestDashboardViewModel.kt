package com.example.hemakase.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hemakase.data.ChatMessage
import com.example.hemakase.data.Treatment
import com.example.hemakase.repository.FirebaseRepository
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

class GuestDashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var reservedYear by mutableStateOf<Int?>(null)
    var reservedMonth by mutableStateOf<Int?>(null)
    var reservedDay by mutableStateOf<Int?>(null)
    var reservedTime by mutableStateOf("")

    var customerName by mutableStateOf("")
    var stylistName by mutableStateOf("")
    var salonName by mutableStateOf("")

    var selectedYear by mutableStateOf(LocalDate.now().year)
    var selectedMonth by mutableStateOf(LocalDate.now().monthValue)
    var selectedDay by mutableStateOf<Int?>(null)

    var selectedTime by mutableStateOf("")

    var reservedTimeSlots = mutableStateListOf<String>()
    var isReservedLoaded by mutableStateOf(false)

    var salonId by mutableStateOf<String?>(null)

    var treatmentType by mutableStateOf("")
    var sameAsLastTime by mutableStateOf(false)
    var quietMode by mutableStateOf(false)
    var referencePhotoUri by mutableStateOf<Uri?>(null)
    var treatmentList by mutableStateOf<List<Treatment>>(emptyList())
    var selectedTreatment by mutableStateOf<Treatment?>(null)
    var treatmentCategories by mutableStateOf<List<String>>(emptyList())
    var selectedCategory by mutableStateOf<String?>(null)
    val filteredTreatmentList: List<Treatment>
        get() = treatmentList.filter { it.category == selectedCategory }

    var latestReservation by mutableStateOf<Map<String, Any>>(emptyMap())


    val treatmentName = selectedTreatment?.name ?: treatmentType
    val note = when {
        sameAsLastTime -> "이전 방문과 동일"
        quietMode -> "조용한 시술 요청"
        else -> ""
    }

    fun selectCategory(category: String) {
        selectedCategory = category
        selectedTreatment = null // 탭 바뀌면 선택 초기화
    }


    fun selectTreatment(treatment: Treatment) {
        selectedTreatment = treatment
    }


    var userProfilePhotoUrl by mutableStateOf<String?>(null)

    fun loadUserProfilePhoto(uid: String) {
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(uid).get().await()
                userProfilePhotoUrl = userDoc.getString("photoUrl")
                Log.d("프로필", "불러온 photoUrl: $userProfilePhotoUrl")
            } catch (e: Exception) {
                Log.e("프로필", "유저 사진 로드 실패: ${e.message}")
            }
        }
    }

    fun toggleQuietTreatment() {
        quietMode = !quietMode
    }

    fun toggleSameAsLastTime() {
        sameAsLastTime = !sameAsLastTime
    }



    fun loadTreatments() {
        viewModelScope.launch {
            try {
                FirebaseRepository.addDefaultTreatmentsIfEmpty()
                val snapshot = Firebase.firestore.collection("treatments").get().await()
                treatmentList = snapshot.documents.mapNotNull { it.toObject(Treatment::class.java) }
                treatmentCategories = treatmentList.map { it.category }.distinct()
                if (selectedCategory == null && treatmentCategories.isNotEmpty()) {
                    selectedCategory = treatmentCategories.first()
                }
            } catch (e: Exception) {
                Log.e("시술", "불러오기 실패: ${e.message}")
            }
        }
    }

    fun notifyReservationChange(
        reservation: Map<String, Any>,
        isCancel: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val customerId = reservation["customer_id"] as? String ?: return@launch
            val customerName = reservation["customer_name"] as? String ?: "고객"
            val stylistId = reservation["stylist_id"] as? String ?: return@launch
            val salonId = reservation["salonId"] as? String ?: return@launch
            val time = (reservation["date"] as? Timestamp)?.toDate()?.toString() ?: "시간 미정"

            try {
                val salonDoc = db.collection("salons").document(salonId).get().await()
                val ownerId = salonDoc.getString("ownerId") ?: return@launch
                val targets = listOf(ownerId to "owner", stylistId to "stylist")

                for ((receiverId, receiverRole) in targets) {
                    val messageText = if (isCancel)
                        "${customerName}님이 예약을 취소했습니다. 시간: $time"
                    else
                        "${customerName}님이 예약을 변경했습니다. 시간: $time"

                    val message = ChatMessage(
                        senderId = customerId,
                        receiverId = receiverId,
                        message = messageText,
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        type = "notification"
                    )

                    FirebaseRepository.sendMessage(message)

                    val roomId = FirebaseRepository.getOrCreateChatRoom(
                        senderId = customerId,
                        receiverId = receiverId,
                        senderRole = "guest",
                        receiverRole = receiverRole
                    )
                    val realtimeDb = FirebaseDatabase.getInstance()
                        .getReference("chat_rooms/$roomId/messages")
                    realtimeDb.push().setValue(message).await()

                    val userDoc = db.collection("users").document(receiverId).get().await()
                    val fcmToken = userDoc.getString("fcmToken")
                    if (!fcmToken.isNullOrBlank()) {
                        FirebaseRepository.sendPushNotificationV1(
                            targetToken = fcmToken,
                            title = if (isCancel) "예약 취소 알림" else "예약 변경 알림",
                            body = messageText
                        )
                    }
                }


                onSuccess()

            } catch (e: Exception) {
                Log.e("ReservationVM", "예약 변경/취소 알림 실패: ${e.message}")
            }
        }
    }

    fun loadLatestReservation() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Log.w("GuestDashboardViewModel", "로그인된 유저 없음")
                return@launch
            }

            try {
                val snapshot = db.collection("reservations")
                    .whereEqualTo("customer_id", uid)
                    .orderBy("date")
                    .limit(1)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val res = snapshot.documents[0]

                    latestReservation = res.data!! + ("docId" to res.id)


                    val time = res.getTimestamp("date")?.toDate()
                    time?.let {
                        val cal = Calendar.getInstance().apply { this.time = it }
                        reservedYear = cal.get(Calendar.YEAR)
                        reservedMonth = cal.get(Calendar.MONTH) + 1
                        reservedDay = cal.get(Calendar.DAY_OF_MONTH)
                        reservedTime = SimpleDateFormat("HH:mm", Locale.KOREAN).format(it)
                    }
                    customerName = res.getString("customer_name") ?: ""
                    stylistName = res.getString("stylist_name") ?: ""
                    salonId = res.getString("salonId")
                    salonId?.let {
                        val salonDoc = db.collection("salons").document(it).get().await()
                        salonName = salonDoc.getString("name") ?: ""
                    }
                }

                loadUserProfilePhoto(uid)
                loadTreatments()
            } catch (e: Exception) {
                Log.e("예약", "불러오기 실패: ${e.message}")
            }
        }
    }

    fun cancelReservation(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val docId = latestReservation["docId"] as? String
                if (docId.isNullOrBlank()) {
                    onFailure("예약 정보가 없습니다.")
                    return@launch
                }

                db.collection("reservations").document(docId)
                    .update("status", "cancelled")
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onFailure("예약 취소 실패: ${e.message}")
            }
        }
    }


    fun loadReservedSlots(salonId: String?, stylistId: String?) {
        viewModelScope.launch {
            isReservedLoaded = false
            reservedTimeSlots.clear()

            if (selectedDay != null && salonId != null && !stylistId.isNullOrBlank()) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth - 1)
                    set(Calendar.DAY_OF_MONTH, selectedDay!!)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }

                val start = Timestamp(cal.time)
                cal.add(Calendar.DATE, 1)
                val end = Timestamp(cal.time)

                try {
                    val snapshot = db.collection("reservations")
                        .whereEqualTo("salonId", salonId)
                        .whereEqualTo("stylist_id", stylistId)
                        .whereGreaterThanOrEqualTo("date", start)
                        .whereLessThan("date", end)
                        .whereEqualTo("status", "confirmed")
                        .get()
                        .await()

                    val formatter = SimpleDateFormat("HH:mm", Locale.KOREAN)
                    reservedTimeSlots.addAll(snapshot.documents.mapNotNull {
                        it.getTimestamp("date")?.toDate()?.let(formatter::format)
                    })
                    isReservedLoaded = true
                } catch (e: Exception) {
                    Log.e("예약슬롯", "불러오기 실패: ${e.message}")
                }
            }
        }
    }

    // GuestDashboardViewModel 안에서 submitReservation 수정
    fun submitReservation(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch onFailure("로그인된 사용자 없음")

                val userDoc = db.collection("users").document(uid).get().await()
                val customerName = userDoc.getString("name") ?: ""
                val stylistId = userDoc.getString("stylistId") ?: ""
                val stylistName = userDoc.getString("stylistName") ?: ""

                val salonId = userDoc.getString("salonId") ?: return@launch onFailure("살롱 정보가 없습니다.")
                val salonDoc = db.collection("salons").document(salonId).get().await()
                val salonName = salonDoc.getString("name") ?: "알 수 없음"

                val dateTimeStr = "$selectedYear-${"%02d".format(selectedMonth)}-${"%02d".format(selectedDay)} $selectedTime"
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREAN)
                val timestamp = Timestamp(formatter.parse(dateTimeStr)!!)

                val treatment = selectedTreatment ?: return@launch onFailure("시술이 선택되지 않음")

                val note = when {
                    sameAsLastTime -> "이전 방문과 동일"
                    quietMode -> "조용한 시술 요청"
                    else -> ""
                }

                val reservation = hashMapOf(
                    "customer_id" to uid,
                    "customer_name" to customerName,
                    "stylist_id" to stylistId,
                    "stylist_name" to stylistName,
                    "salonId" to salonId,
                    "salonName" to salonName,
                    "date" to timestamp,
                    "status" to "pending",
                    "style" to treatment.name,
                    "note" to note,
                    "reference_photo" to referencePhotoUri?.toString(),
                    "treatmentName" to treatment.name,
                    "treatmentDescription" to treatment.description,
                    "treatmentPrice" to treatment.price,
                    "ownerEmail" to "sandpingping2@gmail.com"
                )

                val docRef = db.collection("reservations").add(reservation).await()

                // 메시지 전송 (고객 ↔ 오너, 고객 ↔ 미용사)
                FirebaseRepository.sendReservationConfirmedMessage(
                    customerId = uid,
                    stylistId = stylistId,
                    reservationId = docRef.id
                )

                onSuccess()
            } catch (e: Exception) {
                onFailure("예약 실패: ${e.message}")
            }
        }
    }


    fun rescheduleReservation(
        newDateTime: Calendar,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val timestamp = Timestamp(newDateTime.time)

                val snapshot = db.collection("reservations")
                    .whereEqualTo("customer_id", uid)
                    .orderBy("date")
                    .limit(1)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val docRef = snapshot.documents[0].reference
                    docRef.update("date", timestamp).await()

                    reservedYear = newDateTime.get(Calendar.YEAR)
                    reservedMonth = newDateTime.get(Calendar.MONTH) + 1
                    reservedDay = newDateTime.get(Calendar.DAY_OF_MONTH)
                    reservedTime = SimpleDateFormat("HH:mm", Locale.KOREAN).format(newDateTime.time)

                    onSuccess()
                } else {
                    onFailure("예약 내역이 없습니다.")
                }
            } catch (e: Exception) {
                onFailure("예약 변경 실패: ${e.message}")
            }
        }
    }
}


@Composable
fun CustomCalendarUI(
    selectedMonth: Int,
    selectedYear: Int,
    selectedDay: Int?,
    onDaySelected: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
) {
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Calendar", fontWeight = FontWeight.Medium, fontSize = 17.sp)
            MonthDropdown(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthSelected = onMonthChange,
                onYearSelected = onYearChange
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(5.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                    val dayColors = listOf(
                        Color.Black, Color.Black, Color.Black,
                        Color.Black, Color.Black, Color.Blue, Color.Red
                    )
                    dayNames.forEachIndexed { index, name ->
                        Text(
                            text = name,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = dayColors[index],
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        week.forEach { day ->
                            val isValid = day.isNotBlank()
                            val dayNum = day.toIntOrNull()
                            val isSelected = isValid && dayNum == selectedDay
                            val isToday = isValid && isThisMonth && dayNum == today.dayOfMonth

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .background(
                                        if (isSelected) Color.Black else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .then(
                                        if (isValid) Modifier.clickable { onDaySelected(dayNum!!) }
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day,
                                        fontSize = 14.sp,
                                        color = when {
                                            !isValid -> Color.Gray
                                            isSelected -> Color.White
                                            else -> Color.Black
                                        }
                                    )
                                    if (isToday) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
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
    onYearSelected: (Int) -> Unit,
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
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
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
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
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
fun TimeSelectDialog(
    reservedSlots: List<String>,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit,
) {
    val allTimeOptions = listOf(
        "10:00", "11:00", "12:00", "13:00", "14:00",
        "15:00", "16:00", "17:00", "18:00", "19:00"
    )

    val availableOptions = allTimeOptions.filterNot { reservedSlots.contains(it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("시간 선택", fontWeight = FontWeight.Bold) },
        text = {
            if (availableOptions.isEmpty()) {
                Text("선택 가능한 시간이 없습니다.", color = Color.Gray)
            } else {
                Column {
                    availableOptions.forEach { time ->
                        Text(
                            text = time,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTimeSelected(time) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
