package com.krishimitra.mobilev2

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CropRegistrationScreen(
    viewModel: CropRegistrationViewModel,
    farmId: String,
    onSuccess: () -> Unit
) {
    val selectedCrop by viewModel.selectedCrop
    val sowingDate by viewModel.sowingDate
    val selectedStage by viewModel.selectedStage
    val isLoading by viewModel.isLoading
    val error by viewModel.error

    val context = LocalContext.current
    val cropOptions = listOf("Soybean", "Cotton", "Rice", "Wheat", "Sugarcane", "Onion")
    val cropStages = listOf(
        "Planting/Seedling (लागवड/रोप अवस्था)",
        "Vegetative Growth (वाढ)",
        "Tillering (फुटवे येणे)",
        "Flowering (फुलोरा)",
        "Fruit/Grain Filling (फळ धारणा/दाणे भरणे)",
        "Maturity (पक्वता)",
        "Harvesting (कापणी)"
    )
    var expanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
            viewModel.onSowingDateChange(date)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_color))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Crop Registration",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.text_color),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Select Crop / पीक निवडा",
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cropOptions.forEach { option ->
                val isSelected = selectedCrop == option
                Surface(
                    modifier = Modifier.clickable { viewModel.onCropSelected(option) },
                    color = if (isSelected) colorResource(id = R.color.green_pale) else Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSelected) {
                        null
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, colorResource(id = R.color.border_color))
                    }
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        color = if (isSelected) colorResource(id = R.color.green_primary) else colorResource(id = R.color.text_color),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        OutlinedTextField(
            value = sowingDate,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sowing Date / पेरणीची तारीख") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { datePickerDialog.show() },
            enabled = false, // Use clickable box instead
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = colorResource(id = R.color.text_color),
                disabledBorderColor = colorResource(id = R.color.border_color),
                disabledLabelColor = colorResource(id = R.color.muted_color),
                disabledContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        // Transparent overlay for clickable text field
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .offset(y = (-72).dp)
            .clickable { datePickerDialog.show() }
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            OutlinedTextField(
                value = selectedStage,
                onValueChange = {},
                readOnly = true,
                label = { Text("Current Stage / सध्याची अवस्था") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                cropStages.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.onStageSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = { viewModel.saveCrop(farmId, onSuccess) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.green_primary)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Complete Registration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
