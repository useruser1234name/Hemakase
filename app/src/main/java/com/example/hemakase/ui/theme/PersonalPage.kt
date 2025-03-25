package com.example.hemakase.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
@Preview(showBackground = true)
fun LoginScreenPreview() {
    PersonalScreen()
}

@Composable
fun TopBarWithBackArrow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(56.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.arrow_back),
            contentDescription = "Back",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(24.dp)
        )
        Text(
            text = "Register",
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PersonalScreen(
    onNextClick: () -> Unit = {} // 다음 단계로 넘어갈 때 호출
) {
    val registerViewModel: RegisterViewModel = viewModel()
    // 1) 사용자 입력 상태 정의
    var name by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isHairdresser by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .systemBarsPadding(),
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
            text = "개인 정보",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(33.dp))

        // 5) Name 입력
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 6) ID 입력
        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 7) Password 입력
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            trailingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.eye), // 원하는 이미지 리소스로 변경
                    contentDescription = "Trailing image",
                    modifier = Modifier.size(24.dp) // 원하는 크기로 조정
                )
            },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(30.dp))

        // 8) Phone number 입력 (+82 표시)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.korea),
                contentDescription = "Step 3",
                modifier = Modifier.size(35.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "+82",
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 10.dp, end = 8.dp)
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone number") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 9) Address 입력
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            leadingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.location), // 이미지 리소스
                    contentDescription = "Address image",
                    modifier = Modifier
                        .size(width = 13.dp, height = 18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(25.dp))

        // 10) 미용사 입니다 체크박스
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isHairdresser,
                onCheckedChange = { isHairdresser = it },
            )
            Text(text = "미용사 입니다.")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 11) Next 버튼
        Button(
            onClick = {
                // 회원가입 흐름 진행
                registerViewModel.registerUser(
                    name = name,
                    phone = phoneNumber,
                    address = address,
                    isHairdresser = isHairdresser
                )

                onNextClick() // 다음 단계로 넘어가기 위한 콜백 (예: 사진 등록 등)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(60.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
            ),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text(
                text = "Next",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
