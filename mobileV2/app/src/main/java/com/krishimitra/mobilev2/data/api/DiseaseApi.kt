package com.krishimitra.mobilev2.data.api

import com.krishimitra.mobilev2.data.model.DiseaseDetectionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Retrofit interface for Disease Detection API endpoints.
 * Equivalent to Spring Boot @RestController for disease detection.
 */
interface DiseaseApi {
    
    /**
     * Detect disease from plant image.
     * POST /api/v1/disease/detect
     * Expects multipart/form-data with file, farmer_id, crop_id, crop_type
     */
    @Multipart
    @POST("disease/detect")
    fun detectDisease(
        @Part("farmer_id") farmer_id: RequestBody,
        @Part("crop_id") crop_id: RequestBody?,
        @Part("crop_type") crop_type: RequestBody,
        @Part("language") language: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<DiseaseDetectionResponse>
}