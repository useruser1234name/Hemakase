package com.example.hemakase.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*

@Composable
fun ChooseClientGuestScreen(
    onBackPressed: () -> Unit = {},
    onNextClicked: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TopBarWithBackArrow()
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        width = 1.dp,
                        color = if (selectedRole == "고객") Color.Black else Color.LightGray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { selectedRole = "고객" }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "고객",
                    fontSize = 16.sp,
                    fontWeight = if (selectedRole == "고객") FontWeight.Bold else FontWeight.Normal
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        width = 1.dp,
                        color = if (selectedRole == "미용사") Color.Black else Color.LightGray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { selectedRole = "미용사" }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "미용사",
                    fontSize = 16.sp,
                    fontWeight = if (selectedRole == "미용사") FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                .clickable(enabled = selectedRole != null) {
                    selectedRole?.let { onNextClicked(it) }
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Next",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}