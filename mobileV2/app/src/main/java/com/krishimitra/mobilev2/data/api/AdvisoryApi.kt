package com.krishimitra.mobilev2.data.api

import com.krishimitra.mobilev2.data.model.AdvisoryChatResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * Retrofit interface for Advisory API endpoints.
 * Equivalent to Spring Boot @RestController for advisory chat.
 */
interface AdvisoryApi {
    
    /**
     * Get advisory from AI based on question.
     * POST /api/v1/advisory/chat
     */
    @POST("advisory/chat")
    fun getAdvisory(@Body request: AdvisoryChatRequest): Call<AdvisoryChatResponse>

    /**
     * Get list of advisories for a farmer.
     */
    @GET("advisories")
    fun getAdvisories(
        @Query("farmer_id") farmerId: String,
        @Query("unread") unread: Boolean = false
    ): Call<AdvisoryListResponse>

    /**
     * Mark an advisory as read.
     */
    @PATCH("advisories/{id}/read")
    fun markAsRead(@Path("id") id: String): Call<Any>
}

// Request/Response DTOs
data class AdvisoryListResponse(
    val advisories: List<Map<String, Any>>,
    val unread_count: Int
)

data class AdvisoryChatRequest(
    val farmer_id: String,
    val crop_type: String,
    val stage: String,
    val language: String,
    val question: String
)
