package com.example.hemakase

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hemakase.ui.theme.FirstScreen
import com.example.hemakase.viewmodel.LoginViewModel
import com.example.hemakase.ui.theme.HemakaseTheme
import com.example.hemakase.ui.theme.PersonalScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HemakaseTheme {
                val viewModel: LoginViewModel = viewModel()
                val context = this
                val activity = this

                val loginState by viewModel.loginState.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val needToRegister by viewModel.needToRegister.collectAsState()

                var showRegisterScreen by remember { mutableStateOf(false) }

                // 로그인 결과 처리용 launcher
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    viewModel.loginGoogle(result)
                }

                // 로그인 성공 시 Toast 띄우기
                LaunchedEffect(loginState) {
                    if (loginState) {
                        Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        // TODO: 홈 화면으로 이동 처리
                    }
                }

                // 회원가입 플로우 진입 처리
                LaunchedEffect(needToRegister) {
                    if (needToRegister && !showRegisterScreen) {
                        Toast.makeText(context, "회원가입이 필요합니다.", Toast.LENGTH_SHORT).show()
                        showRegisterScreen = true
                    }
                }

                // 회원가입 화면으로 진입한 이후에 상태 초기화
                LaunchedEffect(showRegisterScreen) {
                    if (showRegisterScreen) {
                        viewModel.resetRegisterTrigger()
                    }
                }

                // 로그인 실패 시 Toast 띄우기
                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }

                if (showRegisterScreen) {
                    PersonalScreen(
                        onNextClick = {
                            Toast.makeText(context, "다음 단계로 이동", Toast.LENGTH_SHORT).show()
                            // 추후: 사진 등록 화면으로 연결 예정
                        }
                    )
                } else {
                    FirstScreen(
                        onLoginClick = {
                            // 로그인 intent 실행
                            val gso =
                                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken("1090328758097-nnsn94rh91ao7aiaan4peom1aagivqoe.apps.googleusercontent.com") // <-- Web Client ID 입력!
                                    .requestEmail()
                                    .build()

                            val signInClient = GoogleSignIn.getClient(activity, gso)
                            val signInIntent = signInClient.signInIntent
                            launcher.launch(signInIntent)
                        },
                        onRegisterClick = {
                            Toast.makeText(context, "회원가입 시작!", Toast.LENGTH_SHORT).show()
                            viewModel.triggerRegisterFlow()
                            showRegisterScreen = true
                        }
                    )
                }
            }
        }
    }
}