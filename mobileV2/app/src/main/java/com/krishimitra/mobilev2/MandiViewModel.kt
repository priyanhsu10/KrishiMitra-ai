package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.SessionManager
import com.krishimitra.mobilev2.data.api.MandiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MandiViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _mandiData = mutableStateOf<MandiResponse?>(null)
    val mandiData: State<MandiResponse?> = _mandiData

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _language = mutableStateOf(sessionManager.getLanguage())
    val language: State<String> = _language

    init {
        loadMandiPrices()
    }

    fun loadMandiPrices() {
        _isLoading.value = true
        _error.value = null

        val crop = sessionManager.getCropType() ?: "Soybean"
        val state = sessionManager.getState() ?: "Maharashtra"
        val language = sessionManager.getLanguage()

        RetrofitClient.mandiApi.getMandiPrices(crop, state, language).enqueue(object : Callback<MandiResponse> {
            override fun onResponse(call: Call<MandiResponse>, response: Response<MandiResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _mandiData.value = response.body()
                } else {
                    _error.value = "No mandi prices found for $crop in $state"
                }
            }

            override fun onFailure(call: Call<MandiResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error loading mandi prices: ${t.message}"
            }
        })
    }
}
