package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.SessionManager
import com.krishimitra.mobilev2.data.api.CropListResponse
import com.krishimitra.mobilev2.data.api.FarmListResponse
import com.krishimitra.mobilev2.data.api.WeatherResponse
import com.krishimitra.mobilev2.data.model.CropResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _farmerName = mutableStateOf(sessionManager.getFarmerName() ?: "शेतकरी")
    val farmerName: State<String> = _farmerName

    private val _locationInfo = mutableStateOf(sessionManager.getState() ?: "Maharashtra")
    val locationInfo: State<String> = _locationInfo

    private val _weather = mutableStateOf<WeatherResponse?>(null)
    val weather: State<WeatherResponse?> = _weather

    private val _activeCrop = mutableStateOf<CropResponse?>(null)
    val activeCrop: State<CropResponse?> = _activeCrop

    private val _unreadNotifCount = mutableIntStateOf(0)
    val unreadNotifCount: State<Int> = _unreadNotifCount

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadData()
    }

    fun loadData() {
        val farmerId = sessionManager.getFarmerId() ?: return
        _isLoading.value = true
        
        loadWeather(farmerId)
        loadFarms(farmerId)
    }

    private fun loadWeather(farmerId: String) {
        val language = sessionManager.getLanguage()
        RetrofitClient.weatherApi.getWeather(farmerId, language = language).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    _weather.value = response.body()
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {}
        })
    }

    private fun loadFarms(farmerId: String) {
        RetrofitClient.farmerApi.getFarms(farmerId).enqueue(object : Callback<FarmListResponse> {
            override fun onResponse(call: Call<FarmListResponse>, response: Response<FarmListResponse>) {
                if (response.isSuccessful && response.body()?.farms?.isNotEmpty() == true) {
                    val farmId = response.body()!!.farms[0].farm_id
                    loadCrops(farmId)
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
                if (response.isSuccessful && response.body()?.crops?.isNotEmpty() == true) {
                    _activeCrop.value = response.body()!!.crops[0]
                    sessionManager.saveCropType(response.body()!!.crops[0].crop_type)
                    sessionManager.saveCropId(response.body()!!.crops[0].crop_id)
                }
            }
            override fun onFailure(call: Call<CropListResponse>, t: Throwable) {
                _isLoading.value = false
            }
        })
    }
}

private fun mutableIntStateOf(value: Int) = mutableStateOf(value)
