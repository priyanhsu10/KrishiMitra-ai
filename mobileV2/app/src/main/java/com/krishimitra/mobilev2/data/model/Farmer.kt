package com.krishimitra.mobilev2.data.model

import com.google.gson.annotations.SerializedName

/**
 * Farmer entity — equivalent to Redux state and database table.
 * Holds farmer profile data after OTP verification.
 */
data class Farmer(
    val id: String,
    val mobile: String,
    val name: String,
    val language: String = "mr", // mr=Marathi, hi=Hindi, en=English
    val fcmToken: String? = null,
    val createdAt: String = System.currentTimeMillis().toString()
)

/**
 * Farmer API response from backend
 */
data class FarmerResponse(
    @SerializedName("id")
    val farmer_id: String,
    val name: String,
    val language: String
) {
    fun toFarmer(): Farmer = Farmer(
        id = farmer_id,
        mobile = "", // Not included in response for security
        name = name,
        language = language
    )
}