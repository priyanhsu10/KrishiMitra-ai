package com.krishimitra.mobilev2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import com.krishimitra.mobilev2.data.api.CropTimelineItemDto

@Composable
fun CropDetailScreen(
    viewModel: CropDetailViewModel,
    cropType: String,
    sowingDate: String,
    language: String
) {
    val timeline by viewModel.timeline
    val isLoading by viewModel.isLoading
    val isMarathi = language.equals("mr", ignoreCase = true)

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
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isMarathi) "$cropType मागोवा" else "$cropType Tracking",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isMarathi) "पेरणी: $sowingDate" else "Sown on: $sowingDate",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    itemsIndexed(timeline) { index, item ->
                        TimelineItem(
                            item = item,
                            isFirst = index == 0,
                            isLast = index == timeline.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItem(item: CropTimelineItemDto, isFirst: Boolean, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier.width(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(if (isFirst) Color.Transparent else colorResource(id = R.color.border_color))
            )
            Surface(
                modifier = Modifier.size(16.dp),
                shape = CircleShape,
                color = if (item.completed == true) colorResource(id = R.color.green_primary) else colorResource(id = R.color.green_mid)
            ) {}
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(if (isLast) Color.Transparent else colorResource(id = R.color.border_color))
            )
        }

        Card(
            modifier = Modifier
                .padding(start = 12.dp, bottom = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.stage ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.text_color)
                    )
                    Text(
                        text = item.estimatedDate ?: "",
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.muted_color)
                    )
                }
                Text(
                    text = item.description ?: "",
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.text_color),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
