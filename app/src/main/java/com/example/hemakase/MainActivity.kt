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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HemakaseTheme {
                val viewModel: LoginViewModel = viewModel()
                val context = this
                val activity = this

                // 로그인 결과 처리용 launcher
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    viewModel.loginGoogle(result)
                }

                val loginState by viewModel.loginState.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val needToRegister by viewModel.needToRegister.collectAsState()

                // 로그인 성공 시 Toast 띄우기
                LaunchedEffect(loginState) {
                    if (loginState) {
                        Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
                    }
                }
                //회원가입 필요 분기
                LaunchedEffect(needToRegister) {
                    if (needToRegister) {
                        Toast.makeText(context, "회원가입이 필요합니다.", Toast.LENGTH_SHORT).show()
                        // TODO: 이후 RegisterScreen으로 네비게이션
                    }
                }

                // 로그인 실패 시 Toast 띄우기
                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }

                FirstScreen(
                    onLoginClick = {
                        // 로그인 intent 실행
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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
                    }
                )
            }
        }
    }
}