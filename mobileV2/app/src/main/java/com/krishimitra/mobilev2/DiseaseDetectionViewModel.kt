package com.krishimitra.mobilev2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.SessionManager
import com.krishimitra.mobilev2.data.model.DiseaseDetectionResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class DiseaseDetectionViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _imageUri = mutableStateOf<Uri?>(null)
    val imageUri: State<Uri?> = _imageUri

    private val _currentPhotoPath = mutableStateOf<String?>(null)
    val currentPhotoPath: State<String?> = _currentPhotoPath

    private val _selectedCrop = mutableStateOf(sessionManager.getCropType() ?: "Soybean")
    val selectedCrop: State<String> = _selectedCrop

    private val _otherCrop = mutableStateOf("")
    val otherCrop: State<String> = _otherCrop

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _resultText = mutableStateOf("")
    val resultText: State<String> = _resultText

    fun onImageSelected(uri: Uri?, path: String?) {
        _imageUri.value = uri
        _currentPhotoPath.value = path
        _resultText.value = ""
    }

    fun onCropSelected(crop: String) {
        _selectedCrop.value = crop
    }

    fun onOtherCropChanged(crop: String) {
        _otherCrop.value = crop
    }

    private fun compressImage(context: Context, file: File): File {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            val maxSize = 1024
            var inSampleSize = 1
            if (options.outWidth > maxSize || options.outHeight > maxSize) {
                val halfWidth = options.outWidth / 2
                val halfHeight = options.outHeight / 2
                while ((halfWidth / inSampleSize) >= maxSize && (halfHeight / inSampleSize) >= maxSize) {
                    inSampleSize *= 2
                }
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

            val compressedFile = File(context.cacheDir, "compressed_${file.name}")
            FileOutputStream(compressedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                out.flush()
            }
            compressedFile
        } catch (e: Exception) {
            file
        }
    }

    fun detectDisease(context: Context) {
        val path = _currentPhotoPath.value ?: return
        _isLoading.value = true

        val originalFile = File(path)
        val fileToUpload = compressImage(context, originalFile)

        val requestFile = fileToUpload.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", fileToUpload.name, requestFile)

        val farmerId = sessionManager.getFarmerId() ?: "unknown"
        val cropId = sessionManager.getCropId()
        val language = sessionManager.getLanguage()

        var selectedCropType = _selectedCrop.value
        if (selectedCropType == "Other") {
            selectedCropType = _otherCrop.value
        }
        if (selectedCropType.isEmpty()) selectedCropType = "Soybean"

        val farmerIdPart = farmerId.toRequestBody("text/plain".toMediaTypeOrNull())
        val cropIdPart = cropId?.toRequestBody("text/plain".toMediaTypeOrNull())
        val cropTypePart = selectedCropType.toRequestBody("text/plain".toMediaTypeOrNull())
        val languagePart = language.toRequestBody("text/plain".toMediaTypeOrNull())

        RetrofitClient.diseaseApi.detectDisease(
            farmerIdPart,
            cropIdPart,
            cropTypePart,
            languagePart,
            body
        ).enqueue(object : Callback<DiseaseDetectionResponse> {
            override fun onResponse(call: Call<DiseaseDetectionResponse>, response: Response<DiseaseDetectionResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _resultText.value = formatResult(response.body()!!, language)
                } else {
                    _resultText.value = "Detection failed"
                }
            }

            override fun onFailure(call: Call<DiseaseDetectionResponse>, t: Throwable) {
                _isLoading.value = false
                _resultText.value = "Error: ${t.message}"
            }
        })
    }

    private fun formatResult(res: DiseaseDetectionResponse, currentLang: String): String {
        val isEnglish = "en".equals(currentLang, ignoreCase = true)
        
        val diseaseName = if (!isEnglish && !res.disease_mr.isNullOrEmpty()) res.disease_mr else res.disease
        val remedy = if (!isEnglish && !res.remedy_mr.isNullOrEmpty()) res.remedy_mr else res.remedy_en
        val cause = if (!isEnglish && !res.cause_mr.isNullOrEmpty()) res.cause_mr else res.cause_en
        var severity = res.severity ?: ""
        
        if (!isEnglish) {
            severity = when (severity.lowercase()) {
                "high" -> "उच्च (High)"
                "medium" -> "मध्यम (Medium)"
                "low" -> "कमी (Low)"
                else -> severity
            }
        }

        return if (!isEnglish) {
            val diseaseLabel = if ("hi".equals(currentLang, ignoreCase = true)) "रोग" else "रोग"
            val severityLabel = if ("hi".equals(currentLang, ignoreCase = true)) "गंभीरता" else "गंभीरता"
            val confidenceLabel = if ("hi".equals(currentLang, ignoreCase = true)) "विश्वास" else "विश्वासार्हता"
            val causeLabel = if ("hi".equals(currentLang, ignoreCase = true)) "कारण" else "कारण"
            val remedyLabel = if ("hi".equals(currentLang, ignoreCase = true)) "उपाय" else "उपाय"

            """
                $diseaseLabel: $diseaseName
                $severityLabel: $severity
                $confidenceLabel: ${String.format("%.2f", res.confidence * 100)}%
                
                $causeLabel: ${if (cause.isNullOrEmpty()) "N/A" else cause}
                
                $remedyLabel: ${if (remedy.isNullOrEmpty()) "N/A" else remedy}
            """.trimIndent()
        } else {
            """
                Disease: $diseaseName
                Severity: $severity
                Confidence: ${String.format("%.2f", res.confidence * 100)}%
                
                Cause: ${if (cause.isNullOrEmpty()) "No info available" else cause}
                
                Remedy: ${if (remedy.isNullOrEmpty()) "No info available" else remedy}
            """.trimIndent()
        }
    }
}
