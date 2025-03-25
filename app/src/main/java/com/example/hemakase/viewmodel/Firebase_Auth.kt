package com.example.hemakase.viewmodel

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _loginState = MutableStateFlow(false)
    val loginState: StateFlow<Boolean> = _loginState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _needToRegister = MutableStateFlow(false)
    val needToRegister: StateFlow<Boolean> = _needToRegister

    fun triggerRegisterFlow() {
        _needToRegister.value = true
    }

    fun loginGoogle(activityResult: ActivityResult) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
        task.addOnCompleteListener { completedTask ->
            try {
                val account = completedTask.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    signInWithCredential(credential)
                } else {
                    _errorMessage.value = "구글 ID 토큰이 없습니다."
                }
            } catch (e: ApiException) {
                Log.e("Login", "Google login failed", e)
                _errorMessage.value = "구글 로그인에 실패했습니다."
            }
        }
    }

    private fun signInWithCredential(credential: AuthCredential) {
        viewModelScope.launch {
            try {
                val result = firebaseAuth.signInWithCredential(credential).await()
                val user = firebaseAuth.currentUser

                user?.let {
                    val uid = it.uid
                    val doc = firestore.collection("users").document(uid).get().await()
                    if (doc.exists()) {
                        _loginState.value = true // 로그인 성공
                    } else {
                        _needToRegister.value = true // 회원가입 필요
                    }
                }

            } catch (e: Exception) {
                Log.e("Login", "Firebase login failed", e)
                _errorMessage.value = "파이어베이스 인증 실패"
            }
        }
    }
}