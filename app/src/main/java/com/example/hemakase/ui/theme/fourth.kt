package com.example.hemakase.ui.theme

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hemakase.R
import com.example.hemakase.viewmodel.RegisterViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// 데이터 모델
data class SalonItem(val id: String, val name: String)
data class StylistItem(val id: String, val name: String)

@Composable
fun HairshopScreen(
    onFinishRegistration: () -> Unit = {},
    registerViewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current

    var selectedSalonId by remember { mutableStateOf("") }
    var selectedStylistName by remember { mutableStateOf("") }
    var selectedStylistId by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        TopBarWithBackArrow()

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepProgressBar()
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "미용실 설정",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(33.dp))

        // 드롭다운 UI (미용실 + 미용사)
        HairshopSelectionArea(
            onSelectionChanged = { salonId, stylistId, stylistName ->
                selectedSalonId = salonId
                selectedStylistId = stylistId
                selectedStylistName = stylistName            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (selectedSalonId.isBlank() || selectedStylistId.isBlank()) {
                    Toast.makeText(context, "미용실과 미용사를 모두 선택해주세요.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                registerViewModel.registerUser(
                    context = context,
                    name = registerViewModel.tempName,
                    phone = registerViewModel.tempPhone,
                    address = registerViewModel.tempAddress,
                    isHairdresser = registerViewModel.tempIsHairdresser,
                    profileUri = registerViewModel.tempProfileUri,
                    selectedSalonId = selectedSalonId,
                    selectedStylistId = selectedStylistId,
                    selectedStylistName = selectedStylistName
                )
                onFinishRegistration()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(60.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
            ),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text("Next", fontSize = 17.sp, fontWeight = FontWeight.Medium)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HairshopSelectionArea(
    onSelectionChanged: (String, String, String) -> Unit
) {
    val db = Firebase.firestore

    var salonList by remember { mutableStateOf<List<SalonItem>>(emptyList()) }
    var stylistList by remember { mutableStateOf<List<StylistItem>>(emptyList()) }

    var selectedSalon by remember { mutableStateOf<SalonItem?>(null) }
    var selectedStylist by remember { mutableStateOf<StylistItem?>(null) }

    var expandedSalon by remember { mutableStateOf(false) }
    var expandedStylist by remember { mutableStateOf(false) }

    // 미용실 목록 불러오기
    LaunchedEffect(Unit) {
        db.collection("salons").get().addOnSuccessListener { result ->
            salonList = result.documents.mapNotNull {
                val id = it.id
                val name = it.getString("name") ?: return@mapNotNull null
                SalonItem(id, name)
            }
        }
    }

    // 미용실 선택 시 미용사 리스트 로드
    LaunchedEffect(selectedSalon) {
        selectedSalon?.let { salon ->
            db.collection("users")
                .whereEqualTo("role", "stylist")
                .whereEqualTo("salonId", salon.id)
                .get()
                .addOnSuccessListener { result ->
                    stylistList = result.documents.mapNotNull {
                        val id = it.getString("id") ?: return@mapNotNull null
                        val name = it.getString("name") ?: return@mapNotNull null
                        StylistItem(id, name)
                    }
                }
        }
    }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        // 미용실 드롭다운
        ExposedDropdownMenuBox(
            expanded = expandedSalon,
            onExpandedChange = { expandedSalon = !expandedSalon }
        ) {
            OutlinedTextField(
                value = selectedSalon?.name ?: "미용실 선택",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = if (expandedSalon) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedSalon,
                onDismissRequest = { expandedSalon = false }
            ) {
                salonList.forEach { salon ->
                    DropdownMenuItem(
                        text = { Text(salon.name) },
                        onClick = {
                            selectedSalon = salon
                            selectedStylist = null
                            expandedSalon = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 미용사 드롭다운
        ExposedDropdownMenuBox(
            expanded = expandedStylist,
            onExpandedChange = { expandedStylist = !expandedStylist }
        ) {
            OutlinedTextField(
                value = selectedStylist?.name ?: "미용사 선택",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = if (expandedStylist) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedStylist,
                onDismissRequest = { expandedStylist = false }
            ) {
                stylistList.forEach { stylist ->
                    DropdownMenuItem(
                        text = { Text(stylist.name) },
                        onClick = {
                            selectedStylist = stylist
                            expandedStylist = false
                            onSelectionChanged(
                                selectedSalon?.id ?: "",
                                stylist.id,
                                stylist.name
                            )
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun StepProgressBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val centerY = size.height / 2
                val startX = 24.dp.toPx()
                val endX = size.width - 24.dp.toPx()
                drawLine(Color.Gray, Offset(startX, centerY), Offset(endX, centerY), strokeWidth)
            },
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(R.drawable.first, R.drawable.second, R.drawable.choicethird, R.drawable.fourth, R.drawable.fifth).forEachIndexed { index, icon ->
            val borderColor = if (index == 2) Color.Black else Color.Transparent
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F4F4))
                    .border(1.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = "Step $index",
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
