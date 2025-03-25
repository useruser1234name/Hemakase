package com.example.hemakase.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hemakase.R


@Preview(showBackground = true)
@Composable
fun FirstScreenPreview() {
    FirstScreen() // 기본값 있는 버전 호출
}


@Composable
fun FirstScreen(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    // 전체 화면을 세로(Column)로 배치
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Box를 사용하여 hairshop 이미지 안에 로고와 텍스트 "헤마카세" 삽입
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(490.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.hairshop),
                contentDescription = "Hairshop Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(width = 68.dp, height = 62.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "헤마카세",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(70.dp))

        Text(
            text = "미용실 고객 관리 및 이탈 방지",
            modifier = Modifier,
            color = Color.Gray,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.weight(1f))

        // Login Button
        Button(
            onClick = { onLoginClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(5.dp)
        ) {
            Text(
                text = "Login",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Register 이동 버튼(텍스트로 구현)
        Button(
            onClick = { onRegisterClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(5.dp),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text(
                text = "Register",
                color = Color.Black
            )
        }
    }
}