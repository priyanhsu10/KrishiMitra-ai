package com.krishimitra.mobilev2.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Centralized session manager for storing authentication tokens and farmer profile.
 * Uses SharedPreferences under the hood for persistent storage.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("krishimitra_session", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? = prefs.getString("auth_token", null)

    fun saveFarmerId(farmerId: String) {
        prefs.edit().putString("farmer_id", farmerId).apply()
    }

    fun getFarmerId(): String? = prefs.getString("farmer_id", null)

    fun saveFarmerName(name: String) {
        prefs.edit().putString("farmer_name", name).apply()
    }

    fun getFarmerName(): String? = prefs.getString("farmer_name", null)

    fun saveState(state: String) {
        prefs.edit().putString("farmer_state", state).apply()
    }

    fun getState(): String? = prefs.getString("farmer_state", "Maharashtra")

    fun saveCropType(cropType: String) {
        prefs.edit().putString("active_crop", cropType).apply()
    }

    fun getCropType(): String? = prefs.getString("active_crop", "Soybean")

    fun saveCropId(cropId: String) {
        prefs.edit().putString("active_crop_id", cropId).apply()
    }

    fun getCropId(): String? = prefs.getString("active_crop_id", null)

    fun saveLanguage(language: String) {
        prefs.edit().putString("language", language).apply()
    }

    fun getLanguage(): String = prefs.getString("language", "mr")!!

    fun saveFcmToken(fcmToken: String) {
        prefs.edit().putString("fcm_token", fcmToken).apply()
    }

    fun getFcmToken(): String? = prefs.getString("fcm_token", null)

    fun clear() {
        prefs.edit().clear().apply()
    }
}