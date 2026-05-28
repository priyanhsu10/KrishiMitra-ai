package com.krishimitra.mobilev2.data.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Mandi Price API endpoints.
 * Equivalent to Spring Boot @RestController for mandi data.
 */
interface MandiApi {
    
    /**
     * Get mandi price information for a crop.
     * GET /api/v1/mandi/prices?crop={crop}&state={state}
     */
    @GET("mandi/prices")
    fun getMandiPrices(
        @Query("crop") crop: String,
        @Query("state") state: String
    ): Call<MandiResponse>
}

// Response DTO
data class MandiResponse(
    val crop: String,
    val prices: List<MandiPrice>,
    val advice_mr: String,
    val advice_en: String,
    val best_time_to_sell: String
)

data class MandiPrice(
    val mandi: String,
    val price_per_quintal: Double,
    val trend: String
)
