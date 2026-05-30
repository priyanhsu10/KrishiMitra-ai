package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.SessionManager
import com.krishimitra.mobilev2.data.model.FarmerResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _farmerName = mutableStateOf(sessionManager.getFarmerName() ?: "")
    val farmerName: State<String> = _farmerName

    private val _farmerId = mutableStateOf(sessionManager.getFarmerId() ?: "")
    val farmerId: State<String> = _farmerId

    private val _state = mutableStateOf(sessionManager.getState() ?: "")
    val state: State<String> = _state

    private val _language = mutableStateOf(sessionManager.getLanguage())
    val language: State<String> = _language

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val id = sessionManager.getFarmerId() ?: return
        RetrofitClient.farmerApi.getFarmer(id).enqueue(object : Callback<FarmerResponse> {
            override fun onResponse(call: Call<FarmerResponse>, response: Response<FarmerResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    _farmerName.value = response.body()!!.name
                }
            }

            override fun onFailure(call: Call<FarmerResponse>, t: Throwable) {}
        })
    }

    fun logout() {
        sessionManager.clear()
    }
}
