package com.krishimitra.mobilev2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.krishimitra.mobilev2.data.model.CropResponse

@Composable
fun CropTrackingScreen(
    viewModel: CropTrackingViewModel,
    onCropClick: (CropResponse) -> Unit,
    onAddCropClick: (String) -> Unit
) {
    val crops by viewModel.crops
    val isLoading by viewModel.isLoading
    val farmId by viewModel.farmId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_color))
    ) {
        // Header
        Text(
            text = "पीक ट्रॅकिंग / Crop Tracking",
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.green_primary))
                .padding(20.dp),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (crops.isEmpty()) {
                Text(
                    text = "No crops found",
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(id = R.color.muted_color)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(crops) { crop ->
                        CropItem(crop = crop, onClick = { onCropClick(crop) })
                    }
                }
            }
        }

        Button(
            onClick = { farmId?.let { onAddCropClick(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.green_primary)
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(12.dp),
            enabled = farmId != null
        ) {
            Text(
                text = "+ नवीन पीक जोडा / Add New Crop",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun CropItem(crop: CropResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = crop.crop_type,
                    modifier = Modifier.weight(1f),
                    color = colorResource(id = R.color.text_color),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = colorResource(id = R.color.green_pale),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = crop.stage,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = colorResource(id = R.color.green_primary),
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "Sown on: ${crop.sowing_date}",
                modifier = Modifier.padding(top = 4.dp),
                color = colorResource(id = R.color.muted_color),
                fontSize = 14.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = colorResource(id = R.color.border_color)
            )

            Text(
                text = "Growth Tracking",
                color = colorResource(id = R.color.text_color),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            LinearProgressIndicator(
                progress = { (crop.growth_progress ?: 0) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(8.dp),
                color = colorResource(id = R.color.green_primary),
                trackColor = colorResource(id = R.color.border_color)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Estimated Harvest",
                    color = colorResource(id = R.color.muted_color),
                    fontSize = 12.sp
                )
                Text(
                    text = crop.estimated_harvest_date ?: "Calculating...",
                    color = colorResource(id = R.color.text_color),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
