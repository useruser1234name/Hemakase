package com.example.hemakase.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hemakase.data.User
import com.example.hemakase.data.Salon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class RegisterViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    var tempName: String = ""
    var tempPhone: String = ""
    var tempAddress: String = ""
    var tempIsHairdresser: Boolean = false
    var tempProfileUri: Uri? = null

    fun setTempUserInfo(
        name: String,
        phone: String,
        address: String,
        isHairdresser: Boolean,
        profileUri: Uri?
    ) {
        tempName = name
        tempPhone = phone
        tempAddress = address
        tempIsHairdresser = isHairdresser
        tempProfileUri = profileUri
    }


    fun registerUser(
        name: String,
        context: Context,
        phone: String,
        address: String,
        isHairdresser: Boolean,
        profileUri: Uri? = null,
        salonName: String? = null,
        salonAddress: String? = null,
        selectedSalonId: String? = null,
        selectedStylistId: String,
        selectedStylistName: String

    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: throw Exception("로그인 상태가 아닙니다.")
                val uid = currentUser.uid
                val email = currentUser.email ?: ""

                val photoUrl = profileUri?.let {
                    try {
                        val stream = context.contentResolver.openInputStream(it)
                        val bitmap = BitmapFactory.decodeStream(stream)
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()

                        val ref = storage.reference.child("profiles/$uid.jpg")

                        ref.putBytes(data).await()
                        ref.downloadUrl.await().toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _errorMessage.value = "사진 업로드 실패: ${e.message}"
                        null
                    }
                }



                if (isHairdresser) {
                    // 미용사 → 미용실 등록 또는 선택
                    val salonId = selectedSalonId ?: run {
                        // 미용실 신규 등록
                        val salonRef = db.collection("salons").document()
                        val salon = Salon(
                            id = salonRef.id,
                            name = salonName ?: "",
                            address = salonAddress ?: "",
                            ownerId = uid,
                            stylistIds = listOf(uid)
                        )
                        salonRef.set(salon).await()
                        salonRef.id
                    }

                    val user = User(
                        id = uid,
                        name = name,
                        email = email,
                        role = "stylist",
                        phone = phone,
                        photo = photoUrl,
                        salonId = salonId,
                        address = address,
                        stylistName = name
                    )

                    db.collection("users").document(uid).set(user).await()
                } else {
                    // 고객 → salonId 선택 필요
                    val user = User(
                        id = uid,
                        name = name,
                        email = email,
                        role = "customer",
                        phone = phone,
                        photo = photoUrl,
                        salonId = selectedSalonId, // 고객은 선택 필수
                        address = address,
                        stylistId = selectedStylistId,
                        stylistName = selectedStylistName
                    )

                    db.collection("users").document(uid).set(user).await()
                }

                _registerSuccess.value = true

            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = e.message
            }
        }
    }
}
