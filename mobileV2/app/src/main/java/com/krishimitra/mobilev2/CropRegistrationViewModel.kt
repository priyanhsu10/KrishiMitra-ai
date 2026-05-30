package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.SessionManager
import com.krishimitra.mobilev2.data.api.CropRequest
import com.krishimitra.mobilev2.data.model.CropResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CropRegistrationViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _selectedCrop = mutableStateOf("Soybean")
    val selectedCrop: State<String> = _selectedCrop

    private val _sowingDate = mutableStateOf("")
    val sowingDate: State<String> = _sowingDate

    private val _selectedStage = mutableStateOf("")
    val selectedStage: State<String> = _selectedStage

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun onCropSelected(crop: String) {
        _selectedCrop.value = crop
    }

    fun onSowingDateChange(date: String) {
        _sowingDate.value = date
    }

    fun onStageSelected(stage: String) {
        _selectedStage.value = stage
    }

    fun saveCrop(farmId: String, onSuccess: () -> Unit) {
        if (_selectedCrop.value.isEmpty() || _sowingDate.value.isEmpty() || _selectedStage.value.isEmpty()) {
            _error.value = "Please fill all fields"
            return
        }

        _isLoading.value = true
        _error.value = null

        val language = sessionManager.getLanguage()
        val request = CropRequest(farmId, _selectedCrop.value, _sowingDate.value, _selectedStage.value, language)

        RetrofitClient.farmerApi.createCrop(request).enqueue(object : Callback<CropResponse> {
            override fun onResponse(call: Call<CropResponse>, response: Response<CropResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    onSuccess()
                } else {
                    _error.value = "Failed to save crop"
                }
            }

            override fun onFailure(call: Call<CropResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }
}
