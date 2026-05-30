package com.krishimitra.mobilev2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel) {
    val notifications by viewModel.notifications
    val isLoading by viewModel.isLoading
    val language by viewModel.language

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_color))
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(id = R.color.green_primary),
            shadowElevation = 4.dp
        ) {
            Text(
                text = "सूचना / Notifications",
                modifier = Modifier.padding(20.dp),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (notifications.isEmpty()) {
                Text(
                    text = "No notifications found",
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(id = R.color.muted_color)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(notification, language)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Map<String, Any>, language: String) {
    val title = notification["alertType"] as? String ?: "Alert"
    var message = notification["messageEn"] as? String ?: ""
    
    if (language.equals("mr", ignoreCase = true) && notification["messageMr"] != null) {
        message = notification["messageMr"] as String
    } else if (language.equals("hi", ignoreCase = true) && notification["messageHi"] != null) {
        message = notification["messageHi"] as String
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚠️", fontSize = 20.sp)
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 12.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text_color)
                )
            }
            Text(
                text = message,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 15.sp,
                color = colorResource(id = R.color.muted_color),
                lineHeight = 22.sp
            )
        }
    }
}
