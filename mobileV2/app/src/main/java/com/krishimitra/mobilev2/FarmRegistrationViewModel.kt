package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.SessionManager
import com.krishimitra.mobilev2.data.api.FarmRequest
import com.krishimitra.mobilev2.data.model.FarmResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FarmRegistrationViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _farmName = mutableStateOf("")
    val farmName: State<String> = _farmName

    private val _area = mutableStateOf("")
    val area: State<String> = _area

    private val _soilType = mutableStateOf("")
    val soilType: State<String> = _soilType

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun onFarmNameChange(newName: String) {
        _farmName.value = newName
    }

    fun onAreaChange(newArea: String) {
        _area.value = newArea
    }

    fun onSoilTypeChange(newSoil: String) {
        _soilType.value = newSoil
    }

    fun saveFarm(onSuccess: (String) -> Unit) {
        if (_farmName.value.isEmpty() || _area.value.isEmpty() || _soilType.value.isEmpty()) {
            _error.value = "Please fill all fields"
            return
        }

        val areaDouble = _area.value.toDoubleOrNull()
        if (areaDouble == null) {
            _error.value = "Invalid area"
            return
        }

        _isLoading.value = true
        _error.value = null

        val farmerId = sessionManager.getFarmerId() ?: ""
        val request = FarmRequest(farmerId, _farmName.value, 18.5204, 73.8567, areaDouble, _soilType.value)

        RetrofitClient.farmerApi.createFarm(request).enqueue(object : Callback<FarmResponse> {
            override fun onResponse(call: Call<FarmResponse>, response: Response<FarmResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    onSuccess(response.body()!!.farm_id)
                } else {
                    _error.value = "Failed to save farm"
                }
            }

            override fun onFailure(call: Call<FarmResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }
}
