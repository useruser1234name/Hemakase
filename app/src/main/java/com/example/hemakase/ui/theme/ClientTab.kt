package com.example.hemakase.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hemakase.R

@Composable
fun ClientsTabContent() {
    val clientList = listOf(
        Client("Dan", "New"),
        Client("John Doe", "This week"),
        Client("Eli King", "This week"),
        Client("Omron Samadi", "New"),
        Client("Arya", "This week")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // 상단 필터(예: "All" 드롭다운)
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "All", fontSize = 14.sp)
            Icon(
                painter = painterResource(id = R.drawable.arrow_down),
                contentDescription = "Filter",
                modifier = Modifier
                    .size(20.dp)
                    .padding(start = 4.dp)
                    .clickable {
                        // TODO: 필터 로직 (드롭다운 메뉴)
                    }
            )
        }

        // 클라이언트 목록 (각 아이템을 카드 형태로 나열)
        Column(modifier = Modifier.fillMaxWidth()) {
            clientList.forEachIndexed { index, client ->
                // 카드(박스)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color.White)
                        .padding(8.dp)  // 카드 내부 최소한의 padding (원하는 값으로 조절)
                ) {
                    ClientRow(client)
                }

                // 카드 사이 간격 20dp (마지막 항목 뒤엔 안 들어가도록 조건)
                if (index < clientList.size - 1) {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}



data class Client(
    val name: String,
    val status: String
)


@Composable
fun ClientRow(client: Client) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽에 [이미지 + 이름 + 상태]
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 이미지 자리 (임시 Box)
            Box(
                modifier = Modifier
                    .size(24.dp)               // 이미지 크기
                    .clip(CircleShape)         // 동그랗게
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // TODO: 실제 이미지 로드시 Coil의 AsyncImage 등 사용
                // e.g. AsyncImage(model = client.imageUrl, ...)
                Text(
                    text = "Img",
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // 이름 + 상태
            Text(
                text = client.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (client.status.isNotEmpty()) {
                Text(
                    text = " (${client.status})",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        // 오른쪽 3점 메뉴 아이콘
        Icon(
            painter = painterResource(id = R.drawable.more), // 3점 아이콘 리소스
            contentDescription = "More",
            modifier = Modifier
                .size(20.dp)
                .clickable {
                    // TODO: 메뉴 로직 (ex: 드롭다운)
                }
        )
    }
}

