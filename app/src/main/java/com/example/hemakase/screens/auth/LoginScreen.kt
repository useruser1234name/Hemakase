//package com.example.hemakase.screens.auth
//
//import android.app.Activity
//import android.content.Intent
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//import com.google.android.gms.auth.api.signin.GoogleSignInClient
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.GoogleAuthProvider
//
//@Composable
//fun LoginScreen(navController: NavController) {
//    val context = LocalContext.current
//    val activity = context as Activity
//
//    val auth = FirebaseAuth.getInstance()
//    var error by remember { mutableStateOf<String?>(null) }
//
//    // GoogleSignInClient 설정
//    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//        .requestIdToken("YOUR_WEB_CLIENT_ID") // Firebase 프로젝트에서 발급
//        .requestEmail()
//        .build()
//
//    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
//
//    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//        try {
//            val account = task.result as GoogleSignInAccount
//            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//            auth.signInWithCredential(credential)
//                .addOnSuccessListener {
//                    navController.navigate("add_user") // or 홈 분기
//                }
//                .addOnFailureListener {
//                    error = "Firebase 로그인 실패: ${it.message}"
//                }
//        } catch (e: Exception) {
//            error = "Google 로그인 실패: ${e.localizedMessage}"
//        }
//    }
//
//    Column(modifier = Modifier
//        .fillMaxSize()
//        .padding(16.dp)) {
//
//        Text("Google 로그인", style = MaterialTheme.typography.titleLarge)
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        Button(onClick = {
//            val signInIntent = googleSignInClient.signInIntent
//            launcher.launch(signInIntent)
//        }) {
//            Text("Google 계정으로 로그인")
//        }
//
//        error?.let {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(it, color = MaterialTheme.colorScheme.error)
//        }
//    }
//}
