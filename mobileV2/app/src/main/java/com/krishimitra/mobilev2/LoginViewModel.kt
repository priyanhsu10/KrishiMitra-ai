package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.api.LoginRequest
import com.krishimitra.mobilev2.data.api.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val _mobileNumber = mutableStateOf("")
    val mobileNumber: State<String> = _mobileNumber

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun onMobileNumberChange(number: String) {
        if (number.length <= 10) {
            _mobileNumber.value = number
        }
    }

    fun sendOtp(onSuccess: (String) -> Unit) {
        if (_mobileNumber.value.length != 10) {
            _error.value = "Enter valid 10-digit mobile number"
            return
        }

        _isLoading.value = true
        _error.value = null

        val mobile = _mobileNumber.value
        RetrofitClient.authApi.login(LoginRequest(mobile)).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.otp_sent == true) {
                    onSuccess(mobile)
                } else {
                    // Demo fallback
                    if (mobile.startsWith("8421") || mobile == "9876543210") {
                        onSuccess(mobile)
                    } else {
                        _error.value = "Failed to send OTP"
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                // Demo bypass
                if (mobile.startsWith("8421") || mobile == "9876543210") {
                    onSuccess(mobile)
                } else {
                    _error.value = "Error: ${t.message}"
                }
            }
        })
    }
}
