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
     * GET /api/v1/mandi?crop={crop}&state={state}
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
    val best_time_to_sell: String
)

data class MandiPrice(
    val district: String,
    val market: String,
    val min_price: Double,
    val max_price: Double,
    val average_price: Double
)