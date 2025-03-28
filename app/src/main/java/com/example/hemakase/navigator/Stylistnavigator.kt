package com.example.hemakase.navigator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hemakase.ui.theme.BaberSchedulerScreen
import com.example.hemakase.ui.theme.BarberSettingsScreenClientsTab
import com.example.hemakase.ui.theme.DashboardTopBar

@Composable
fun StylistBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column {
        Divider(color = Color.LightGray, thickness = 1.dp)
        NavigationBar(containerColor = Color.White) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                label = { Text("스케줄") }
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                icon = { Icon(Icons.Default.Message, contentDescription = null) },
                label = { Text("메시지") }
            )
            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text("설정") }
            )
        }
    }
}

@Composable
fun BaberDashboardScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { DashboardTopBar() },
        bottomBar = {
            StylistBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(innerPadding)) {
            when (selectedTab) {
                0 -> BaberSchedulerScreen(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
                // 1 -> MessageScreen()
                2 -> BarberSettingsScreenClientsTab(
//                    selectedTab = selectedTab,
//                    onTabSelected = { selectedTab = it }
                )
            }
        }
    }
}
