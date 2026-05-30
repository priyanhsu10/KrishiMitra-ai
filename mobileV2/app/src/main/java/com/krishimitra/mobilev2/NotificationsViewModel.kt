package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.SessionManager
import com.krishimitra.mobilev2.data.api.AdvisoryListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _notifications = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val notifications: State<List<Map<String, Any>>> = _notifications

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _language = mutableStateOf(sessionManager.getLanguage())
    val language: State<String> = _language

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        val farmerId = sessionManager.getFarmerId() ?: return
        _isLoading.value = true

        RetrofitClient.advisoryApi.getAdvisories(farmerId, false).enqueue(object : Callback<AdvisoryListResponse> {
            override fun onResponse(call: Call<AdvisoryListResponse>, response: Response<AdvisoryListResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _notifications.value = response.body()!!.advisories
                }
            }

            override fun onFailure(call: Call<AdvisoryListResponse>, t: Throwable) {
                _isLoading.value = false
            }
        })
    }
}
