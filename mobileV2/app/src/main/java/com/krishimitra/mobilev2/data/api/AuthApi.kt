package com.krishimitra.mobilev2.data.api

import com.google.gson.annotations.SerializedName
import com.krishimitra.mobilev2.data.model.FarmerResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for Authentication API endpoints.
 * Equivalent to Spring Boot @RestController for auth endpoints.
 */
interface AuthApi {
    
    /**
     * Request OTP for login
     * POST /api/v1/auth/login
     */
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    
    /**
     * Verify OTP and get auth token
     * POST /api/v1/auth/verify
     */
    @POST("auth/verify")
    fun verify(@Body request: VerifyRequest): Call<VerifyResponse>
    
    /**
     * Register FCM token for push notifications
     * POST /api/v1/auth/register-token
     */
    @POST("auth/register-token")
    fun registerToken(@Body request: RegisterTokenRequest): Call<Any>
}

// Request/Response DTOs
data class LoginRequest(val mobile: String)
data class LoginResponse(val otp_sent: Boolean)

data class VerifyRequest(val mobile: String, val otp: String)
data class VerifyResponse(
    val token: String,
    @SerializedName("farmer_id", alternate = ["id"])
    val farmer_id: String,
    val is_new_user: Boolean
)

data class RegisterTokenRequest(val farmerId: String, val fcmToken: String)
