package com.example.hemakase.ui.theme

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

@Preview(showBackground = true)
@Composable
fun HairshopScreenPreview() {
    HairshopScreen()
}

@Composable
fun HairshopScreen() {
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
            // 단계 아이콘 Row
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
                // (1) 첫 번째 아이콘
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

                // (2) 두 번째 아이콘
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

                // (3) 세 번째 아이콘
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF4F4F4))
                        .border(1.dp, shape = CircleShape, color = Color.Black)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.choicethird),
                        contentDescription = "Step 3",
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.Center),
                        contentScale = ContentScale.Crop
                    )
                }

                // (4) 네 번째 아이콘
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

                // (5) 다섯 번째 아이콘
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
            text = "미용실 설정",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(33.dp))

        // ─────────────────────────────────────────────────
        // (중요) 여기서부터 "미용실 선택" 드롭다운 UI 추가
        // ─────────────────────────────────────────────────
        HairshopSelectionArea()

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

/**
 * "미용실 선택"과 "미용사 선택"을 드롭다운으로 구성한 예시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HairshopSelectionArea() {
    // 첫 번째 드롭다운: 미용실 선택
    var expandedShop by remember { mutableStateOf(false) }
    var selectedShop by remember { mutableStateOf("미용실 선택") }
    val shopList = listOf("헤어샵 A", "헤어샵 B", "헤어샵 C")

    // 두 번째 드롭다운: 미용사 선택
    var expandedHairdresser by remember { mutableStateOf(false) }
    var selectedHairdresser by remember { mutableStateOf("미용사 선택") }
    val hairdresserList = listOf("김철수", "박영희", "이민수")

    // Column으로 두 개의 드롭다운을 위아래로 배치
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expandedShop,
            onExpandedChange = { expandedShop = !expandedShop }
        ) {
            OutlinedTextField(
                value = selectedShop,
                onValueChange = {},
                label = { Text("") },
                trailingIcon = {
                    val icon = if (expandedShop) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                    Icon(imageVector = icon, contentDescription = null)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true
            )
            ExposedDropdownMenu(
                expanded = expandedShop,
                onDismissRequest = { expandedShop = false }
            ) {
                shopList.forEach { shop ->
                    DropdownMenuItem(
                        text = { Text(shop) },
                        onClick = {
                            selectedShop = shop
                            expandedShop = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expandedHairdresser,
            onExpandedChange = { expandedHairdresser = !expandedHairdresser }
        ) {
            OutlinedTextField(
                value = selectedHairdresser,
                onValueChange = {},
                label = { Text("") },
                trailingIcon = {
                    val icon = if (expandedHairdresser) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                    Icon(imageVector = icon, contentDescription = null)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true
            )
            ExposedDropdownMenu(
                expanded = expandedHairdresser,
                onDismissRequest = { expandedHairdresser = false }
            ) {
                hairdresserList.forEach { hairdresser ->
                    DropdownMenuItem(
                        text = { Text(hairdresser) },
                        onClick = {
                            selectedHairdresser = hairdresser
                            expandedHairdresser = false
                        }
                    )
                }
            }
        }
    }
}
