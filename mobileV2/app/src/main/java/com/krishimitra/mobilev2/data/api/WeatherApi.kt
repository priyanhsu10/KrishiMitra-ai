package com.krishimitra.mobilev2.data.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for Weather API endpoints.
 * Equivalent to Spring Boot @RestController for weather data.
 */
interface WeatherApi {
    
    /**
     * Get weather forecast and advisory for a farmer's farm.
     * GET /api/v1/weather?farmer_id={farmerId}
     */
    @GET("weather")
    fun getWeather(
        @Query("farmer_id") farmerId: String,
        @Query("farm_id") farmId: String? = null
    ): Call<WeatherResponse>

    /**
     * Trigger weather scheduler manually.
     */
    @POST("weather/schedule/run-now")
    fun runSchedulerNow(): Call<String>

    /**
     * Get weather scheduler status.
     */
    @GET("weather/schedule/status")
    fun getSchedulerStatus(): Call<Map<String, Any>>
}

// Response DTO
data class WeatherResponse(
    val priority: String?,
    val temperature: Double?,
    val humidity: Double?,
    val description: String?,
    val weather_summary: String?,
    val advice_mr: String?,
    val advice_en: String?,
    val alert_type: String?,
    val rainfall_mm: Double?,
    val notification_sent: Boolean
)
