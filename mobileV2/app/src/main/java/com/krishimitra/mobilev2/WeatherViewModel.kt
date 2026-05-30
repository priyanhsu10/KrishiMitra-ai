package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.SessionManager
import com.krishimitra.mobilev2.data.api.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _weather = mutableStateOf<WeatherResponse?>(null)
    val weather: State<WeatherResponse?> = _weather

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _language = mutableStateOf(sessionManager.getLanguage())
    val language: State<String> = _language

    init {
        loadWeather()
    }

    fun loadWeather() {
        val farmerId = sessionManager.getFarmerId()
        if (farmerId == null) {
            _error.value = "Farmer ID not found"
            return
        }

        _isLoading.value = true
        _error.value = null
        
        val language = sessionManager.getLanguage()

        RetrofitClient.weatherApi.getWeather(farmerId, language = language).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _weather.value = response.body()
                } else {
                    _error.value = "Failed to load weather data"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }
}
