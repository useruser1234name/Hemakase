package com.example.hemakase

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hemakase.ui.theme.*
import com.example.hemakase.viewmodel.LoginViewModel
import com.example.hemakase.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    enum class Screen {
        First, Personal, Camera, Hairshop, Dashboard
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HemakaseTheme {
                val viewModel: LoginViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel()
                val context = this
                val activity = this

                val loginState by viewModel.loginState.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val needToRegister by viewModel.needToRegister.collectAsState()

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
                            val role = authViewModel.getUserRole(uid)
                            // 추후 role에 따라 분기 가능
                            currentScreen = Screen.Dashboard
                            Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
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
                                currentScreen = Screen.Dashboard
                            }
                        )
                    }

                    Screen.Dashboard -> {
                        DashboardScreen()
                    }
                }
            }
        }
    }
}
