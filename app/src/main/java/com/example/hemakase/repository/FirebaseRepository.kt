package com.example.hemakase.repository

import android.util.Log
import com.example.hemakase.data.*
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private const val TAG = "FirebaseRepository"


    suspend fun addDefaultTreatmentsIfEmpty() {
        val treatmentsRef = Firebase.firestore.collection("treatments")
        val snapshot = treatmentsRef.get().await()

        if (snapshot.isEmpty) {
            val defaultTreatments = listOf(
                // 드라이 5개
                Treatment("베이직 드라이", 10000, "기본 드라이 스타일링입니다.", "", "드라이"),
                Treatment("볼륨 드라이", 15000, "볼륨을 살린 드라이 스타일링입니다.", "", "드라이"),
                Treatment("웨이브 드라이", 16000, "웨이브를 가미한 드라이 스타일입니다.", "", "드라이"),
                Treatment("스트레이트 드라이", 15000, "깔끔한 직모 스타일 드라이입니다.", "", "드라이"),
                Treatment("샴푸 + 드라이", 18000, "샴푸 후 드라이까지 포함된 시술입니다.", "", "드라이"),

                // 컷 5개
                Treatment("남성 커트 베이직", 15000, "기본 남성 커트입니다.", "", "커트"),
                Treatment("남성 커트 스타일링", 18000, "스타일링 포함된 남성 커트입니다.", "", "커트"),
                Treatment("남성 커트 프리미엄", 20000, "프리미엄 커트 및 두피 관리 포함", "", "커트"),
                Treatment("남성 커트 리프레시", 22000, "두피 클렌징과 커트 포함", "", "커트"),
                Treatment("학생 커트", 12000, "학생 전용 기본 커트입니다.", "", "커트"),

                // 펌 5개
                Treatment("베이직 펌", 35000, "기본 볼륨감을 주는 펌입니다.", "", "펌"),
                Treatment("쉼표 펌", 40000, "남성 인기 쉼표 펌 스타일입니다.", "", "펌"),
                Treatment("리젠트 펌", 45000, "리젠트 스타일 펌입니다.", "", "펌"),
                Treatment("애즈 펌", 48000, "내추럴한 애즈 펌 스타일입니다.", "", "펌"),
                Treatment("가르마 펌", 42000, "가르마를 정리해주는 스타일 펌입니다.", "", "펌"),

                // 염색 5개
                Treatment("기본 염색", 30000, "단색 염색입니다.", "", "염색"),
                Treatment("포인트 염색", 35000, "부분적으로 포인트를 주는 염색입니다.", "", "염색"),
                Treatment("전체 염색", 40000, "전체 톤을 바꾸는 염색입니다.", "", "염색"),
                Treatment("투톤 염색", 45000, "두 가지 컬러의 스타일링 염색입니다.", "", "염색"),
                Treatment("탈색 1회", 50000, "한 번의 탈색 시술입니다.", "", "염색")
            )


            defaultTreatments.forEach {
                treatmentsRef.add(it).await()
            }
        }
    }


    suspend fun getReservedTimes(
        year: Int, month: Int, day: Int,
        salonId: String?, stylistName: String
    ): List<String> {
        if (salonId == null || stylistName.isBlank()) return emptyList()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
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

        return snapshot.documents.mapNotNull {
            it.getTimestamp("date")?.toDate()?.let { d -> formatter.format(d).trim() }
        }
    }


    fun sendMessage(message: ChatMessage) {
        FirebaseFirestore.getInstance()
            .collection("messages")
            .document(message.receiverId)
            .collection("chats")
            .add(message)
            .addOnSuccessListener {
                Log.d("FirebaseRepo", "안 메시지 전송 성공")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseRepo", "안 메시지 전송 실패", e)
            }
    }

    fun sendPushNotification(token: String, title: String, body: String) {
        val notificationData = mapOf(
            "to" to token,
            "notification" to mapOf(
                "title" to title,
                "body" to body
            )
        )

        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject(notificationData).toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .post(requestBody)
            .addHeader("Authorization", "key=YOUR_SERVER_KEY_HERE") // 🔑 서버키 등록 필요
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCM", "푸시 전송 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("FCM", "푸시 전송 성공")
                } else {
                    Log.e("FCM", "푸시 응답 실패: ${response.code}")
                }
            }
        })
    }


    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(user.id)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e(TAG, "addUser failed", e)
                onFailure(e)
            }
    }

    fun createReservation(reservation: Reservation, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("reservations")
            .add(reservation)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e(TAG, "createReservation failed", e)
                onFailure(e)
            }
    }

    fun addCustomerNote(note: CustomerNote, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("customer_notes")
            .add(note)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e(TAG, "addCustomerNote failed", e)
                onFailure(e)
            }
    }

    // 예: 유저 role 가져오기
    fun getUserRole(uid: String, onResult: (String?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role")
                onResult(role)
            }
            .addOnFailureListener {
                Log.e(TAG, "getUserRole failed", it)
                onResult(null)
            }
    }
}
