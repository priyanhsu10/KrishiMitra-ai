package com.krishimitra.mobilev2.data.api

import com.krishimitra.mobilev2.data.model.CropResponse
import com.krishimitra.mobilev2.data.model.FarmResponse
import com.krishimitra.mobilev2.data.model.FarmerResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * Retrofit interface for Farmer, Farm, and Crop registration.
 */
interface FarmerApi {

    /**
     * Create or update farmer profile.
     * POST /api/v1/farmers
     */
    @POST("farmers")
    fun createFarmer(@Body request: FarmerRequest): Call<FarmerResponse>

    /**
     * Get farmer profile by ID.
     */
    @GET("farmers/{id}")
    fun getFarmer(@Path("id") id: String): Call<FarmerResponse>

    /**
     * Register a new farm.
     * POST /api/v1/farms
     */
    @POST("farms")
    fun createFarm(@Body request: FarmRequest): Call<FarmResponse>

    /**
     * Get farms for a farmer.
     */
    @GET("farms")
    fun getFarms(@Query("farmer_id") farmerId: String): Call<FarmListResponse>

    /**
     * Register a new crop for a farm.
     * POST /api/v1/crops
     */
    @POST("crops")
    fun createCrop(@Body request: CropRequest): Call<CropResponse>

    /**
     * Get crops for a farm.
     */
    @GET("crops")
    fun getCrops(@Query("farm_id") farmId: String): Call<CropListResponse>

    /**
     * Get AI-assisted timeline for a crop.
     */
    @GET("crops/{id}/timeline")
    fun getCropTimeline(@Path("id") cropId: String): Call<CropTimelineResponse>

    /**
     * Update FCM token for push notifications.
     */
    @PATCH("farmers/{id}/fcm-token")
    fun updateFcmToken(@Path("id") id: String, @Body body: Map<String, String>): Call<Void>
}

// Response Wrapper DTOs
data class FarmListResponse(
    val farms: List<FarmResponse>,
    val count: Int
)

data class CropListResponse(
    val crops: List<CropResponse>,
    val count: Int
)

data class CropTimelineResponse(
    val crop_id: String,
    val timeline: List<CropTimelineItemDto>
)

data class CropTimelineItemDto(
    val id: String,
    val stage: String,
    val estimatedDate: String,
    val description: String,
    val completed: Boolean
)

// Request DTOs
data class FarmerRequest(
    val name: String,
    val language: String,
    val village: String,
    val state: String,
    val farmer_id: String? = null
)

data class FarmRequest(
    val farmer_id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val area_acres: Double,
    val soil_type: String
)

data class CropRequest(
    val farm_id: String,
    val crop_type: String,
    val sowing_date: String,
    val stage: String,
    val language: String = "marathi"
)
