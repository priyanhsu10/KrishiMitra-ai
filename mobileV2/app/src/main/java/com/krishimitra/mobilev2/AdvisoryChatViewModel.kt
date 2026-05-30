package com.krishimitra.mobilev2

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.krishimitra.mobilev2.data.RetrofitClient
import com.krishimitra.mobilev2.data.SessionManager
import com.krishimitra.mobilev2.data.api.AdvisoryChatRequest
import com.krishimitra.mobilev2.data.model.AdvisoryChatResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class ChatMessage(
    val sender: String,
    val message: String
)

class AdvisoryChatViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _chatHistory = mutableStateOf<List<ChatMessage>>(emptyList())
    val chatHistory: State<List<ChatMessage>> = _chatHistory

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _selectedCrop = mutableStateOf(sessionManager.getCropType() ?: "Soybean")
    val selectedCrop: State<String> = _selectedCrop

    private val _otherCrop = mutableStateOf("")
    val otherCrop: State<String> = _otherCrop

    fun onCropSelected(crop: String) {
        _selectedCrop.value = crop
    }

    fun onOtherCropChanged(crop: String) {
        _otherCrop.value = crop
    }

    fun sendMessage(question: String) {
        if (question.isBlank()) return

        val currentHistory = _chatHistory.value.toMutableList()
        currentHistory.add(ChatMessage("You", question))
        _chatHistory.value = currentHistory

        _isLoading.value = true

        val farmerId = sessionManager.getFarmerId() ?: ""
        val language = sessionManager.getLanguage()
        
        var cropType = _selectedCrop.value
        if (cropType == "Other") {
            cropType = _otherCrop.value
        }
        if (cropType.isEmpty()) cropType = "Soybean"

        val request = AdvisoryChatRequest(
            farmer_id = farmerId,
            crop_type = cropType,
            stage = "Vegetative Growth",
            language = language,
            question = question
        )

        RetrofitClient.advisoryApi.getAdvisory(request).enqueue(object : Callback<AdvisoryChatResponse> {
            override fun onResponse(call: Call<AdvisoryChatResponse>, response: Response<AdvisoryChatResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    val advisory = response.body()!!
                    val answer = advisory.advice ?: if ("mr".equals(language, ignoreCase = true)) advisory.advice_mr else advisory.advice_en
                    
                    val updatedHistory = _chatHistory.value.toMutableList()
                    updatedHistory.add(ChatMessage("AI", answer ?: ""))
                    _chatHistory.value = updatedHistory
                }
            }

            override fun onFailure(call: Call<AdvisoryChatResponse>, t: Throwable) {
                _isLoading.value = false
                // Error handling could be added here
            }
        })
    }
}
