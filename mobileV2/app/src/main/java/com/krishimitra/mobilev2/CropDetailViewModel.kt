package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.api.CropTimelineItemDto
import com.krishimitra.mobilev2.data.api.CropTimelineResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CropDetailViewModel : ViewModel() {

    private val _timeline = mutableStateOf<List<CropTimelineItemDto>>(emptyList())
    val timeline: State<List<CropTimelineItemDto>> = _timeline

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun loadTimeline(cropId: String) {
        _isLoading.value = true
        _error.value = null

        RetrofitClient.farmerApi.getCropTimeline(cropId).enqueue(object : Callback<CropTimelineResponse> {
            override fun onResponse(call: Call<CropTimelineResponse>, response: Response<CropTimelineResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _timeline.value = response.body()!!.timeline
                } else {
                    _error.value = "Failed to load timeline"
                }
            }

            override fun onFailure(call: Call<CropTimelineResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }
}
