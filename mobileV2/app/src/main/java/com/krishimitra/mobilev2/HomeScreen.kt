package com.krishimitra.mobilev2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krishimitra.mobilev2.data.model.CropResponse

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onActionClick: (String) -> Unit
) {
    val farmerName by viewModel.farmerName
    val locationInfo by viewModel.locationInfo
    val weather by viewModel.weather
    val activeCrop by viewModel.activeCrop
    val unreadNotifCount by viewModel.unreadNotifCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_color))
            .verticalScroll(rememberScrollState())
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.green_primary))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "नमस्कार 👋", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                    Text(
                        text = farmerName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onActionClick("profile") }
                    )
                    Text(text = "📍 $locationInfo", color = colorResource(id = R.color.green_mid), fontSize = 14.sp)
                }

                Box(modifier = Modifier.clickable { onActionClick("notifications") }) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🔔", fontSize = 24.sp)
                        }
                    }
                    if (unreadNotifCount > 0) {
                        Surface(
                            modifier = Modifier.size(20.dp).align(Alignment.TopEnd),
                            shape = CircleShape,
                            color = colorResource(id = R.color.red)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "$unreadNotifCount", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Weather Strip
            Surface(
                modifier = Modifier.padding(top = 20.dp).fillMaxWidth(),
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⛅", fontSize = 32.sp)
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(
                            text = "${weather?.temperature ?: "--"}°C",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = weather?.description ?: "Loading weather...",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "आर्द्रता", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Text(
                            text = "${weather?.humidity ?: "--"}%",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Actions Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    ActionItem(icon = "🔬", label = "रोग तपासा", onClick = { onActionClick("disease") }, modifier = Modifier.weight(1f))
                    ActionItem(icon = "💬", label = "AI सल्ला", onClick = { onActionClick("advisory") }, modifier = Modifier.weight(1f))
                    ActionItem(icon = "📈", label = "मंडी भाव", onClick = { onActionClick("mandi") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ActionItem(icon = "🌤", label = "हवामान", onClick = { onActionClick("weather") }, modifier = Modifier.weight(1f))
                    ActionItem(icon = "🌱", label = "पीक जोडा", onClick = { onActionClick("add_crop") }, modifier = Modifier.weight(1f))
                    ActionItem(icon = "📊", label = "पीक ट्रॅकिंग", onClick = { onActionClick("crop_tracking") }, modifier = Modifier.weight(1f))
                }
            }
        }

        // Active Crop Card
        CropSummaryCard(
            crop = activeCrop,
            onAddClick = { onActionClick("add_crop") },
            modifier = Modifier.padding(12.dp)
        )

        // Alert Strip
        if (weather?.advice != null) {
            AlertStrip(
                message = weather?.advice ?: "",
                modifier = Modifier.padding(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ActionItem(icon: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(12.dp),
            color = colorResource(id = R.color.bg_color)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 28.sp)
            }
        }
        Text(
            text = label,
            modifier = Modifier.padding(top = 6.dp),
            color = colorResource(id = R.color.text_color),
            fontSize = 12.sp
        )
    }
}

@Composable
fun CropSummaryCard(crop: CropResponse?, onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = crop?.crop_type ?: "No active crop registered",
                    modifier = Modifier.weight(1f),
                    color = colorResource(id = R.color.text_color),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (crop != null) {
                    Surface(
                        color = colorResource(id = R.color.green_pale),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Healthy",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = colorResource(id = R.color.green_primary),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (crop != null) {
                Text(
                    text = "Growth Progress",
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(id = R.color.muted_color),
                    fontSize = 14.sp
                )
                LinearProgressIndicator(
                    progress = { (crop.growth_progress ?: 0) / 100f },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(8.dp),
                    color = colorResource(id = R.color.green_primary),
                    trackColor = colorResource(id = R.color.border_color)
                )
                Text(
                    text = "Estimated Harvest: ${crop.estimated_harvest_date ?: "--"}",
                    modifier = Modifier.padding(top = 8.dp),
                    color = colorResource(id = R.color.muted_color),
                    fontSize = 13.sp
                )
            }

            TextButton(
                onClick = onAddClick,
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
            ) {
                Text(text = "+ Add New Crop", color = colorResource(id = R.color.green_primary), fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun AlertStrip(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFFFF8E1),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorResource(id = R.color.amber))
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(text = "⚠️", fontSize = 24.sp)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "हवामान सतर्कता / Weather Alert",
                    color = colorResource(id = R.color.amber),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color(0xFF5D4037),
                    fontSize = 14.sp
                )
            }
        }
    }
}
