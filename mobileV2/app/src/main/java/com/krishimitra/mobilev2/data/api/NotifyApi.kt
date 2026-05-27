package com.krishimitra.mobilev2.data.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface NotifyApi {
    @POST("notify")
    fun sendNotification(@Body request: NotifyRequest): Call<NotifyResponse>
}

data class NotifyRequest(
    val farmerId: String,
    val alertType: String,
    val messageEn: String,
    val priority: String,
    val cropId: String? = null,
    val messageMr: String? = null,
    val messageHi: String? = null
)

data class NotifyResponse(
    val advisoryId: String?,
    val saved: Boolean,
    val fcmSent: Boolean
)
