package com.example.hemakase.navigator

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DashboardBottomBar() {
    Column {
        Divider(
            color = Color.LightGray,
            thickness = 1.dp
        )
        NavigationBar(
            containerColor = Color.White
        ) {
            NavigationBarItem(
                selected = false,
                onClick = { /*TODO*/ },
                icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null) },
                label = { Text("Dashboard") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { /*TODO*/ },
                icon = { Icon(imageVector = Icons.Default.Message, contentDescription = null) },
                label = { Text("Messages") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { /*TODO*/ },
                icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) },
                label = { Text("Settings") }
            )
        }
    }
}