package com.krishimitra.mobilev2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krishimitra.mobilev2.data.api.WeatherResponse

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weather by viewModel.weather
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val language by viewModel.language

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_color))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(id = R.color.green_primary),
            shadowElevation = 4.dp
        ) {
            Text(
                text = "हवामान / Weather",
                modifier = Modifier.padding(20.dp),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                    text = error!!,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    color = colorResource(id = R.color.muted_color)
                )
            } else if (weather != null) {
                WeatherContent(weather!!, language)
            }
        }
    }
}

@Composable
fun WeatherContent(weather: WeatherResponse, language: String) {
    val isMarathi = language.equals("mr", ignoreCase = true)

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "⛅", fontSize = 64.sp)
                Text(
                    text = String.format("%.1f°C", weather.temperature ?: 0.0),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text_color)
                )
                Text(
                    text = weather.weather_summary ?: weather.description ?: "",
                    fontSize = 20.sp,
                    color = colorResource(id = R.color.muted_color),
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherDetailItem("Humidity", "${weather.humidity?.toInt() ?: "--"}%", "💧")
                    WeatherDetailItem("Rainfall", "${weather.rainfall_mm ?: 0.0} mm", "🌧")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isMarathi) "कृषी सल्ला / Advisory" else "Agricultural Advisory",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.green_primary)
                )
                
                val advice = weather.advice ?: (if (isMarathi) weather.advice_mr else weather.advice_en)
                Text(
                    text = advice ?: "",
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.text_color),
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun WeatherDetailItem(label: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 24.sp)
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.text_color),
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = colorResource(id = R.color.muted_color)
        )
    }
}
