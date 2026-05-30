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
import com.krishimitra.mobilev2.data.api.MandiPrice
import com.krishimitra.mobilev2.data.api.MandiResponse

@Composable
fun MandiScreen(viewModel: MandiViewModel) {
    val mandiData by viewModel.mandiData
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val language by viewModel.language
    
    val isMarathi = language.equals("mr", ignoreCase = true)
    val isEnglish = language.equals("en", ignoreCase = true)

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
                text = "मंडी भाव / Mandi Prices",
                modifier = Modifier.padding(20.dp),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                    text = error!!,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = colorResource(id = R.color.muted_color)
                )
            } else if (mandiData != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        MandiSummaryCard(mandiData!!, isMarathi, isEnglish)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isMarathi) "बाजार भाव तपशील" else "Market Price Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.text_color),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(mandiData!!.prices) { price ->
                        MandiPriceCard(price, isMarathi, isEnglish)
                    }
                }
            }
        }
    }
}

@Composable
fun MandiSummaryCard(mandi: MandiResponse, isMarathi: Boolean, isEnglish: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isEnglish) "Crop: " else "पीक: ",
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.muted_color)
                )
                Text(
                    text = mandi.crop,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorResource(id = R.color.text_color)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (isEnglish) "AI Advisory" else "AI सल्ला",
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.green_primary)
            )
            
            val advice = mandi.advice ?: (if (isMarathi) mandi.advice_mr else mandi.advice_en)
            Text(
                text = advice ?: "",
                modifier = Modifier.padding(top = 4.dp),
                color = colorResource(id = R.color.text_color)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row {
                Text(
                    text = if (isMarathi) "विक्रीसाठी सर्वोत्तम वेळ: " else "Best time to sell: ",
                    color = colorResource(id = R.color.muted_color)
                )
                val bestTime = mandi.best_time_to_sell
                val displayTime = if (isMarathi) {
                    if ("now".equals(bestTime, true)) "आता (Now)" else "पुढील आठवड्यात (Next Week)"
                } else bestTime
                
                Text(
                    text = displayTime ?: "",
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.blue)
                )
            }
        }
    }
}

@Composable
fun MandiPriceCard(price: MandiPrice, isMarathi: Boolean, isEnglish: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = price.mandi,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text_color)
                )
                
                val trend = price.trend
                val trendIcon = when (trend?.lowercase()) {
                    "rising" -> "📈"
                    "falling" -> "📉"
                    else -> "➖"
                }
                Text(text = trendIcon, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${price.price_per_quintal}${if (isMarathi) " प्रति क्विंटल" else " per quintal"}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.green_primary)
                )
                
                val trendText = if (isMarathi) {
                    when (price.trend?.lowercase()) {
                        "rising" -> "वाढत आहे"
                        "falling" -> "कमी होत आहे"
                        else -> "स्थिर"
                    }
                } else price.trend
                
                Text(
                    text = trendText ?: "",
                    color = when (price.trend?.lowercase()) {
                        "rising" -> colorResource(id = R.color.green_primary)
                        "falling" -> colorResource(id = R.color.red)
                        else -> colorResource(id = R.color.muted_color)
                    }
                )
            }
        }
    }
}
