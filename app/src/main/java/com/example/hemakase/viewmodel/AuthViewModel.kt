package com.example.hemakase.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.hemakase.data.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // 현재 로그인한 유저 가져오기
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // UID로 회원 유무 확인
    suspend fun checkIfUserExists(uid: String): Boolean {
        val doc = db.collection("users").document(uid).get().await()
        return doc.exists()
    }

    // 고객 회원가입 (사진 포함)
    suspend fun registerCustomer(
        user: User,
        profileImageUri: android.net.Uri?,
        salonId: String,
        stylistId: String
    ): Boolean {
        return try {
            val uid = user.id

            // 사진 업로드 (선택사항)
            val photoUrl = profileImageUri?.let {
                val ref = storage.reference.child("photos/$uid.jpg")
                ref.putFile(it).await()
                ref.downloadUrl.await().toString()
            }

            val newUser = user.copy(
                photo = photoUrl,
                role = "customer"
            )

            db.collection("users").document(uid).set(newUser).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 미용사 회원가입 (미용실 등록 포함)
    suspend fun registerStylist(
        user: User,
        salonName: String,
        salonAddress: String
    ): Boolean {
        return try {
            val uid = user.id

            // 1. 미용실 등록
            val salonData = hashMapOf(
                "id" to UUID.randomUUID().toString(),
                "name" to salonName,
                "address" to salonAddress,
                "owner_id" to uid,
                "stylist_ids" to listOf(uid)
            )
            val salonRef = db.collection("salons").document()
            salonRef.set(salonData).await()

            // 2. 유저 등록
            val newUser = user.copy(
                role = "stylist",
                salonId = salonRef.id
            )
            db.collection("users").document(uid).set(newUser).await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 유저 역할 조회
    suspend fun getUserRole(uid: String): String? {
        return try {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()
            doc.getString("role")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "getUserRole 실패: ${e.message}")
            null
        }
    }

}
