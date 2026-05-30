package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.SessionManager
import com.krishimitra.mobilev2.data.api.CropListResponse
import com.krishimitra.mobilev2.data.api.FarmListResponse
import com.krishimitra.mobilev2.data.model.CropResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CropTrackingViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _crops = mutableStateOf<List<CropResponse>>(emptyList())
    val crops: State<List<CropResponse>> = _crops

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _farmId = mutableStateOf<String?>(null)
    val farmId: State<String?> = _farmId

    init {
        loadFarmData()
    }

    private fun loadFarmData() {
        val farmerId = sessionManager.getFarmerId() ?: return
        _isLoading.value = true

        RetrofitClient.farmerApi.getFarms(farmerId).enqueue(object : Callback<FarmListResponse> {
            override fun onResponse(call: Call<FarmListResponse>, response: Response<FarmListResponse>) {
                if (response.isSuccessful && response.body() != null && response.body()!!.farms.isNotEmpty()) {
                    val farm = response.body()!!.farms[0]
                    _farmId.value = farm.farm_id
                    loadCrops(farm.farm_id)
                } else {
                    _isLoading.value = false
                }
            }

            override fun onFailure(call: Call<FarmListResponse>, t: Throwable) {
                _isLoading.value = false
            }
        })
    }

    private fun loadCrops(farmId: String) {
        RetrofitClient.farmerApi.getCrops(farmId).enqueue(object : Callback<CropListResponse> {
            override fun onResponse(call: Call<CropListResponse>, response: Response<CropListResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _crops.value = response.body()!!.crops
                }
            }

            override fun onFailure(call: Call<CropListResponse>, t: Throwable) {
                _isLoading.value = false
            }
        })
    }
}
