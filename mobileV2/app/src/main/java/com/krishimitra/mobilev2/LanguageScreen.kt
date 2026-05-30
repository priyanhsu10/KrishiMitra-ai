package com.krishimitra.mobilev2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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

@Composable
fun LanguageScreen(onContinue: (String) -> Unit) {
    var selectedLang by remember { mutableStateOf("mr") }
    val languages = listOf(
        Triple("mr", "मराठी", "Marathi"),
        Triple("hi", "हिंदी", "Hindi"),
        Triple("en", "English", "English")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.green_primary))
            .verticalScroll(rememberScrollState())
    ) {
        // Logo Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🌾", fontSize = 80.sp)
            Text(
                text = "KrishiMitra",
                fontSize = 36.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.02.sp
            )
            Text(
                text = "कृषीमित्र",
                fontSize = 24.sp,
                color = colorResource(id = R.color.green_mid),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "AI-powered farm assistant",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // Language Selection Section (Bottom Sheet like)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "भाषा निवडा / Select Language",
                    fontSize = 18.sp,
                    color = colorResource(id = R.color.text_color),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Column(modifier = Modifier.selectableGroup()) {
                    languages.forEach { (code, label, _) ->
                        LanguageOption(
                            label = label,
                            selected = selectedLang == code,
                            onClick = { selectedLang = code }
                        )
                    }
                }

                Button(
                    onClick = { onContinue(selectedLang) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.green_primary)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text(
                        text = "पुढे जा / Continue →",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .selectable(
                selected = selected,
                onClick = onClick
            ),
        color = if (selected) colorResource(id = R.color.green_pale) else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, colorResource(id = R.color.border_color))
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) colorResource(id = R.color.green_primary) else colorResource(id = R.color.text_color)
            )
        }
    }
}
