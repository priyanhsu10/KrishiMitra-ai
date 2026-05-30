package com.krishimitra.mobilev2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseDetectionScreen(viewModel: DiseaseDetectionViewModel) {
    val context = LocalContext.current
    val imageUri by viewModel.imageUri
    val selectedCrop by viewModel.selectedCrop
    val otherCrop by viewModel.otherCrop
    val isLoading by viewModel.isLoading
    val resultText by viewModel.resultText

    val cropOptions = listOf("Soybean", "Cotton", "Rice", "Wheat", "Sugarcane", "Onion", "Other")
    var expanded by remember { mutableStateOf(false) }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoPath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.onImageSelected(tempPhotoUri, tempPhotoPath)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val file = copyUriToFile(context, uri)
                viewModel.onImageSelected(uri, file.absolutePath)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (uri, path) = createTempImageFile(context)
            tempPhotoUri = uri
            tempPhotoPath = path
            cameraLauncher.launch(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_color))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Disease Detection",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.text_color),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Selected Plant",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val (uri, path) = createTempImageFile(context)
                        tempPhotoUri = uri
                        tempPhotoPath = path
                        cameraLauncher.launch(uri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f).padding(end = 5.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Capture", fontSize = 18.sp)
            }

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).padding(start = 5.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Gallery", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCrop,
                onValueChange = {},
                readOnly = true,
                label = { Text("पिकाचा प्रकार निवडा / Select Crop") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                cropOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.onCropSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (selectedCrop == "Other") {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = otherCrop,
                onValueChange = { viewModel.onOtherCropChanged(it) },
                label = { Text("इतर पिकाचे नाव / Enter Other Crop Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.detectDisease(context) },
            modifier = Modifier.fillMaxWidth(),
            enabled = imageUri != null && !isLoading,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(14.dp)
        ) {
            Text("Detect Disease", fontSize = 20.sp)
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 20.dp))
        }

        if (resultText.isNotEmpty()) {
            Text(
                text = resultText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                fontSize = 18.sp,
                color = colorResource(id = R.color.text_color)
            )
        }
    }
}

private fun createTempImageFile(context: Context): Pair<Uri, String> {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val image = File.createTempFile(imageFileName, ".jpg", storageDir)
    val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", image)
    return Pair(uri, image.absolutePath)
}

@Throws(IOException::class)
private fun copyUriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri) ?: throw IOException("Unable to open input stream")
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val tempFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    
    FileOutputStream(tempFile).use { outputStream ->
        inputStream.copyTo(outputStream)
    }
    return tempFile
}
