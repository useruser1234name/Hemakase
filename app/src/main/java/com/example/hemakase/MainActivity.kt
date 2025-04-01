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
import com.example.hemakase.navigator.GuestMainScreen
import com.example.hemakase.ui.theme.*
import com.example.hemakase.viewmodel.LoginViewModel
import com.example.hemakase.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MainActivity : ComponentActivity() {


    enum class Screen {
        First, Personal, Camera, Hairshop,
        ChooseClientGuest,
        GuestDashboard, // 고객용
        BaberDashboard,   // 미용사용
        BaberRegister
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

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

                // 로그인 성공 시 화면 전환 처리
                LaunchedEffect(loginState) {
                    if (loginState) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            coroutineScope.launch {
                                val role = authViewModel.getUserRole(uid)

                                currentScreen = when (role) {
                                    "customer" -> Screen.GuestDashboard
                                    "stylist", "babershop", "admin", "owner" -> Screen.BaberDashboard
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
                        currentScreen = Screen.ChooseClientGuest
                    }
                }

                // 화면 상태에 따라 UI 표시
                when (currentScreen) {
                    Screen.First -> {
                        FirstScreen(
                            onLoginClick = {
                                val gso =
                                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken("1090328758097-nnsn94rh91ao7aiaan4peom1aagivqoe.apps.googleusercontent.com")
                                        .requestEmail()
                                        .build()
                                val signInClient = GoogleSignIn.getClient(activity, gso)
                                val signInIntent = signInClient.signInIntent
                                launcher.launch(signInIntent)
                            },
                            onRegisterClick = {
                                viewModel.setRegisterMode(true)
                                val gso =
                                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken("1090328758097-nnsn94rh91ao7aiaan4peom1aagivqoe.apps.googleusercontent.com")
                                        .requestEmail()
                                        .build()
                                val signInClient = GoogleSignIn.getClient(activity, gso)
                                val signInIntent = signInClient.signInIntent
                                launcher.launch(signInIntent)
                            }
                        )
                    }
                    Screen.ChooseClientGuest -> {
                        ChooseClientGuestScreen(
                            onNextClicked = { selectedRole ->
                                when (selectedRole) {
                                    "고객" -> currentScreen = Screen.Personal
                                    "미용사" -> currentScreen = Screen.BaberRegister
                                }
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
                                currentScreen = Screen.GuestDashboard
                            }
                        )
                    }
                    Screen.BaberRegister -> {
                        BaberRegisterScreen(
                            onNextClick = {
                                currentScreen = Screen.BaberDashboard
                            }
                        )
                    }

                    Screen.GuestDashboard -> {
                        GuestMainScreen() // 하단바 포함 전체 구조 연결
                    }

                    Screen.BaberDashboard -> {
                        BaberDashboardScreen()
                    }
                }
            }
        }
    }
}

//
//
//    fun addSampleTreatments() {
//        val db = FirebaseFirestore.getInstance()
//        val treatments = listOf(
//            // 드라이
//            Treatment(name = "베이직 드라이", price = 10000, description = "기본 드라이 스타일링입니다."),
//            Treatment(name = "볼륨 드라이", price = 15000, description = "볼륨을 살린 드라이 스타일링입니다."),
//            Treatment(name = "웨이브 드라이", price = 16000, description = "웨이브를 가미한 드라이 스타일입니다."),
//            Treatment(name = "스트레이트 드라이", price = 15000, description = "깔끔한 직모 스타일 드라이입니다."),
//            Treatment(name = "샴푸 + 드라이", price = 18000, description = "샴푸 후 드라이까지 포함된 시술입니다."),
//
//            // 컷
//            Treatment(name = "남성 커트 베이직", price = 15000, description = "기본 남성 커트입니다."),
//            Treatment(name = "남성 커트 스타일링", price = 18000, description = "스타일링 포함된 남성 커트입니다."),
//            Treatment(name = "남성 커트 프리미엄", price = 20000, description = "프리미엄 커트 및 두피 관리 포함."),
//            Treatment(name = "남성 커트 리프레시", price = 22000, description = "두피 클렌징과 커트 포함."),
//            Treatment(name = "학생 커트", price = 12000, description = "학생 전용 기본 커트입니다."),
//
//            // 펌
//            Treatment(name = "베이직 펌", price = 35000, description = "기본 볼륨감을 주는 펌입니다."),
//            Treatment(name = "쉼표 펌", price = 40000, description = "남성 인기 쉼표 펌 스타일입니다."),
//            Treatment(name = "리젠트 펌", price = 45000, description = "리젠트 스타일 펌입니다."),
//            Treatment(name = "애즈 펌", price = 48000, description = "내추럴한 애즈 펌 스타일입니다."),
//            Treatment(name = "가르마 펌", price = 42000, description = "가르마를 정리해주는 스타일 펌입니다."),
//
//            // 염색
//            Treatment(name = "기본 염색", price = 30000, description = "단색 염색입니다."),
//            Treatment(name = "포인트 염색", price = 35000, description = "포인트로 부분 염색을 진행합니다."),
//            Treatment(name = "전체 염색", price = 40000, description = "전체 톤을 바꾸는 염색입니다."),
//            Treatment(name = "투톤 염색", price = 45000, description = "두 가지 컬러로 스타일링하는 염색입니다."),
//            Treatment(name = "탈색 1회", price = 50000, description = "한 번의 탈색 시술입니다.")
//        )
//
//        treatments.forEach { treatment ->
//            db.collection("treatments")
//                .add(treatment)
//                .addOnSuccessListener { println("✅ 추가됨: ${treatment.name}") }
//                .addOnFailureListener { println("❌ 실패: ${it.message}") }
//        }
//    }
//}