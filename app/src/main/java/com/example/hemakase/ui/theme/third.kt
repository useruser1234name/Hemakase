package com.example.hemakase.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hemakase.R


@Composable
fun CameraScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding() // Column 자체에는 systemBarsPadding만 적용
    ) {
        TopBarWithBackArrow()

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.first),
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
                        .border(1.dp, shape = CircleShape, color = Color.Black)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.choicesecond),
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
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "사진등록",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(33.dp))

        // 점선 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(5.dp))
                .drawBehind {
                    val dashStyle = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawRoundRect(
                        color = Color.LightGray,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = dashStyle
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                }
        ) {
            // 내부에 Row를 중앙 정렬하여 아이콘과 텍스트를 수평 배치
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),  // 좌우 여백
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // + 아이콘 (원하는 리소스로 교체)
                Icon(
                    painter = painterResource(id = R.drawable.picture_plus),
                    contentDescription = "Plus icon",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Choose a file",
                    color = Color.Black
                )
            }
        }

        // Spacer로 남은 공간을 채워서 버튼을 맨 아래로 내림
        Spacer(modifier = Modifier.weight(1f))

        // 버튼 (맨 아래)
        Button(
            onClick = {
                /*TODO*/
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
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
