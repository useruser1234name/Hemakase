package com.example.hemakase.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hemakase.R
import com.example.hemakase.viewmodel.RegisterViewModel

@Composable
fun BaberRegisterScreen(
    onNextClick: () -> Unit = {}
) {
    var salonSearch by remember { mutableStateOf("") }
    var salonName by remember { mutableStateOf("") }
    var salonAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var allAgree by remember { mutableStateOf(false) }
    var agreeService by remember { mutableStateOf(false) }
    var agreePrivacy by remember { mutableStateOf(false) }
    var agreeLocation by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .systemBarsPadding()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBarWithBackArrow()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val centerY = size.height / 2
                    val startX = 24.dp.toPx()
                    val endX = size.width - 24.dp.toPx()

                    drawLine(
                        color = Color.Gray,
                        start = Offset(startX, centerY),
                        end = Offset(endX, centerY),
                        strokeWidth = strokeWidth
                    )
                },
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 현재 단계(1단계) 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F4F4))
                    .border(1.dp, shape = CircleShape, color = Color.Black)// 필요 없다면 제거 가능
            ) {
                Image(
                    painter = painterResource(id = R.drawable.choicefirst),
                    contentDescription = "Step 1",
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )
            }

            // 2) 두 번째 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F4F4))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.second),
                    contentDescription = "Step 2",
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )
            }

            // 3) 세 번째 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F4F4))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.third),
                    contentDescription = "Step 3",
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )
            }

            // 4) 네 번째 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F4F4))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fourth),
                    contentDescription = "Step 4",
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )
            }

            // 5) 다섯 번째 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F4F4))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fifth),
                    contentDescription = "Step 5",
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )
            }
        }
        // 4) 상단 타이틀
        Text(
            text = "미용실 등록",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = salonSearch,
            onValueChange = { salonSearch = it },
            placeholder = { Text("미용실 검색") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = salonName,
            onValueChange = { salonName = it },
            placeholder = { Text("미용실") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = salonAddress,
            onValueChange = { salonAddress = it },
            placeholder = { Text("미용실 주소") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 전화번호 입력
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.korea),
                contentDescription = "국기",
                modifier = Modifier.size(30.dp)
            )
            Text(text = "+82", modifier = Modifier.padding(start = 8.dp))
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = { Text("미용실 전화번호") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    allAgree = !allAgree
                    agreeService = allAgree
                    agreePrivacy = allAgree
                    agreeLocation = allAgree
                }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "전체동의",
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.weight(1f)) // 남는 공간 밀어주기

            RadioButton(
                selected = allAgree,
                onClick = {
                    allAgree = !allAgree
                    agreeService = allAgree
                    agreePrivacy = allAgree
                    agreeLocation = allAgree
                }
            )
        }

        Divider()

        AgreementItem("필수", "서비스 이용약관", agreeService) {
            agreeService = it
            allAgree = agreeService && agreePrivacy && agreeLocation
        }

        AgreementItem("필수", "개인정보 수집 및 이용동의", agreePrivacy) {
            agreePrivacy = it
            allAgree = agreeService && agreePrivacy && agreeLocation
        }

        AgreementItem("선택", "위치 서비스 이용약관", agreeLocation) {
            agreeLocation = it
            allAgree = agreeService && agreePrivacy && agreeLocation
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = { onNextClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text("Next")
        }
    }
}

@Composable
fun AgreementItem(
    label: String,
    title: String,
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckChanged(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 텍스트 영역
        Column {
            Row {
                Text(
                    text = "$label ",
                    color = if (label == "필수") Color(0xFF014421) else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(23.dp))
                Text(
                    text = "$title  >",
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // 남는 공간 밀어주기

        // 오른쪽에 RadioButton
        RadioButton(
            selected = checked,
            onClick = { onCheckChanged(!checked) }
        )
    }
}

