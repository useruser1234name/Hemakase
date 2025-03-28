package com.example.hemakase

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hemakase.navigator.BaberDashboardScreen
import com.example.hemakase.ui.theme.*
import com.example.hemakase.viewmodel.LoginViewModel
import com.example.hemakase.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {

    enum class Screen {
        First, Personal, Camera, Hairshop,
        GuestDashboard, // 고객용
        BaberDashboard   // 미용사용
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HemakaseTheme(darkTheme = false) {
                val viewModel: LoginViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel()
                val context = this
                val activity = this

                val loginState by viewModel.loginState.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val needToRegister by viewModel.needToRegister.collectAsState()
                val coroutineScope = rememberCoroutineScope()

                var currentScreen by remember { mutableStateOf(Screen.First) }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    viewModel.loginGoogle(result)
                }

//                LaunchedEffect(Unit) {
//                    uploadDummyReservations() // 한 번만 실행
//                }
//
//                LaunchedEffect(Unit) {
//                    uploadDummySalons() // 앱 실행 시 한 번만 실행됨
//                }

                // 로그인 성공 시 화면 전환 처리
                LaunchedEffect(loginState) {
                    if (loginState) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            coroutineScope.launch {
                                val role = authViewModel.getUserRole(uid)

                                currentScreen = when (role) {
                                    "customer" -> Screen.GuestDashboard
                                    "stylist", "babershop", "admin" -> Screen.BaberDashboard
                                    else -> Screen.GuestDashboard
                                }

                                Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // 로그인 실패 메시지 처리
                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }

                // 회원가입 흐름 진입 시 PersonalScreen으로 전환
                LaunchedEffect(needToRegister) {
                    if (needToRegister && currentScreen == Screen.First) {
                        viewModel.resetRegisterTrigger()
                        Toast.makeText(context, "회원가입이 필요합니다.", Toast.LENGTH_SHORT).show()
                        currentScreen = Screen.Personal
                    }
                }

                // 화면 상태에 따라 UI 표시
                when (currentScreen) {
                    Screen.First -> {
                        FirstScreen(
                            onLoginClick = {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken("1090328758097-nnsn94rh91ao7aiaan4peom1aagivqoe.apps.googleusercontent.com")
                                    .requestEmail()
                                    .build()
                                val signInClient = GoogleSignIn.getClient(activity, gso)
                                val signInIntent = signInClient.signInIntent
                                launcher.launch(signInIntent)
                            },
                            onRegisterClick = {
                                viewModel.triggerRegisterFlow()
                                currentScreen = Screen.Personal
                            }
                        )
                    }

                    Screen.Personal -> {
                        PersonalScreen(
                            onNextClick = {
                                currentScreen = Screen.Camera
                            }
                        )
                    }

                    Screen.Camera -> {
                        CameraScreen(
                            onNextClick = {
                                currentScreen = Screen.Hairshop
                            }
                        )
                    }

                    Screen.Hairshop -> {
                        HairshopScreen(
                            onFinishRegistration = {
                                currentScreen = Screen.GuestDashboard // 기본값
                            }
                        )
                    }

                    Screen.GuestDashboard -> {
                        DashboardScreen() // 고객용
                    }

                    Screen.BaberDashboard -> {
                        BaberDashboardScreen()  // 미용사용
                    }
                }
            }
        }
    }
}
//
//fun uploadDummyReservations() {
//    val db = Firebase.firestore
//
//    val stylistId = "stylist_001"
//    val customerNames = listOf("정우성", "김혜수", "이병헌", "한지민", "마동석")
//
//    val now = Calendar.getInstance()
//
//    repeat(customerNames.size) { i ->
//        val customerName = customerNames[i]
//
//        // 예약 시간을 i일 뒤 10 + i시로 설정 (예: 10AM, 11AM ...)
//        val reservationTime = Calendar.getInstance().apply {
//            add(Calendar.DATE, i)
//            set(Calendar.HOUR_OF_DAY, 10 + i)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//        }
//
//        val reservation = hashMapOf(
//            "customer_name" to customerName,
//            "stylist_id" to stylistId,
//            "date" to Timestamp(reservationTime.time),
//            "status" to if (i % 2 == 0) "confirmed" else "pending",
//            "note" to if (i % 2 == 0) "정기 방문" else "첫 방문"
//        )
//
//        db.collection("reservations").add(reservation)
//            .addOnSuccessListener {
//                println("예약 추가 완료: $customerName")
//            }
//            .addOnFailureListener {
//                println("추가 실패: ${it.message}")
//            }
//    }
//}
//
//fun uploadDummySalons() {
//    val db = Firebase.firestore
//
//    val salons = listOf(
//        mapOf(
//            "name" to "루미에르 헤어",
//            "address" to "서울 강남구 테헤란로 123",
//            "phone" to "02-123-4567"
//        ),
//        mapOf(
//            "name" to "블루밍 살롱",
//            "address" to "서울 마포구 홍익로 56",
//            "phone" to "02-987-6543"
//        ),
//        mapOf(
//            "name" to "헤어바이수",
//            "address" to "부산 해운대구 해운대로 77",
//            "phone" to "051-123-7890"
//        )
//    )
//
//    salons.forEachIndexed { index, salonInfo ->
//        val stylistIds = mutableListOf<String>()
//
//        repeat(2) { i ->
//            stylistIds.add("stylist_${index}_$i")
//        }
//
//        // ✅ salonData를 Map<String, Any>로 명확하게 지정
//        val salonData: Map<String, Any?> = mapOf(
//            "name" to salonInfo["name"],
//            "address" to salonInfo["address"],
//            "phone" to salonInfo["phone"],
//            "ownerId" to stylistIds[0],
//            "stylistIds" to stylistIds.toList()
//        )
//
//        db.collection("salons")
//            .add(salonData)
//            .addOnSuccessListener { salonDoc ->
//                val salonId = salonDoc.id
//
//                stylistIds.forEachIndexed { i, stylistId ->
//                    val stylist = mapOf(
//                        "id" to stylistId,
//                        "name" to "스타일리스트 ${index + 1}-${i + 1}",
//                        "email" to "stylist${index}_${i}@hemakase.com",
//                        "role" to "stylist",
//                        "photo" to null,
//                        "phone" to "010-0000-000${i}",
//                        "address" to salonInfo["address"],
//                        "salonId" to salonId,
//                        "createdAt" to System.currentTimeMillis()
//                    )
//                    db.collection("users").document(stylistId).set(stylist)
//                }
//
//                println("미용실 등록 완료: ${salonInfo["name"]}")
//            }
//            .addOnFailureListener {
//                println("등록 실패: ${it.message}")
//            }
//    }
//}
