package com.example.hemakase.navigator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hemakase.ui.theme.ChatListScreen
import com.example.hemakase.ui.theme.ChatRoomScreen
import com.example.hemakase.ui.theme.DashboardScreen
import com.example.hemakase.ui.theme.GuestSettingsScreen

sealed class GuestScreen {
    data object Dashboard : GuestScreen()
    data object ChatList : GuestScreen()
    data class ChatRoom(val roomId: String) : GuestScreen()
    data object Settings : GuestScreen()
}

@Composable
fun GuestMainScreen() {
    var currentScreen by remember { mutableStateOf<GuestScreen>(GuestScreen.Dashboard) }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentScreen = currentScreen,
                onScreenChange = { currentScreen = it }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentScreen) {
                is GuestScreen.Dashboard -> DashboardScreen()
                is GuestScreen.ChatList -> ChatListScreen(
                    onChatClick = { roomId -> currentScreen = GuestScreen.ChatRoom(roomId) }
                )
                is GuestScreen.ChatRoom -> ChatRoomScreen(
                    roomId = (currentScreen as GuestScreen.ChatRoom).roomId
                )
                is GuestScreen.Settings -> GuestSettingsScreen()
            }
        }
    }
}

@Composable
fun DashboardBottomBar(
    currentScreen: GuestScreen,
    onScreenChange: (GuestScreen) -> Unit
) {
    Column {
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
        NavigationBar(containerColor = Color.White) {
            NavigationBarItem(
                selected = currentScreen is GuestScreen.Dashboard,
                onClick = { onScreenChange(GuestScreen.Dashboard) },
                icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                label = { Text("Dashboard") }
            )
            NavigationBarItem(
                selected = currentScreen is GuestScreen.ChatList || currentScreen is GuestScreen.ChatRoom,
                onClick = { onScreenChange(GuestScreen.ChatList) },
                icon = { Icon(Icons.Filled.Message, contentDescription = null) },
                label = { Text("Messages") }
            )
            NavigationBarItem(
                selected = currentScreen is GuestScreen.Settings,
                onClick = { onScreenChange(GuestScreen.Settings) },
                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                label = { Text("Settings") }
            )
        }
    }
}
