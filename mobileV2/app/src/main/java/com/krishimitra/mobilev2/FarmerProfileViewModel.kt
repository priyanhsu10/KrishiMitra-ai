package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.SessionManager
import com.krishimitra.mobilev2.data.api.FarmerRequest
import com.krishimitra.mobilev2.data.model.FarmerResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FarmerProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _village = mutableStateOf("")
    val village: State<String> = _village

    private val _state = mutableStateOf("Maharashtra")
    val state: State<String> = _state

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onVillageChange(newVillage: String) {
        _village.value = newVillage
    }

    fun onStateChange(newState: String) {
        _state.value = newState
    }

    fun saveProfile(onSuccess: () -> Unit) {
        if (_name.value.isEmpty() || _village.value.isEmpty() || _state.value.isEmpty()) {
            _error.value = "Please fill all fields"
            return
        }

        _isLoading.value = true
        _error.value = null

        val farmerId = sessionManager.getFarmerId() ?: ""
        val language = sessionManager.getLanguage()
        val request = FarmerRequest(_name.value, language, _village.value, _state.value, farmerId)

        RetrofitClient.farmerApi.createFarmer(request).enqueue(object : Callback<FarmerResponse> {
            override fun onResponse(call: Call<FarmerResponse>, response: Response<FarmerResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    sessionManager.saveFarmerName(response.body()!!.name)
                    sessionManager.saveState(_state.value)
                    onSuccess()
                } else {
                    _error.value = "Failed to save profile"
                }
            }

            override fun onFailure(call: Call<FarmerResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }
}
