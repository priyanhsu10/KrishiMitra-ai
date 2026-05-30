package com.krishimitra.mobilev2.data.model

import com.google.gson.annotations.SerializedName

/**
 * Disease detection response from AI service.
 */
data class DiseaseDetectionResponse(
    @SerializedName("report_id")
    val report_id: String?,
    
    @SerializedName("disease")
    val disease: String,
    
    @SerializedName("disease_mr")
    val disease_mr: String?,
    
    @SerializedName("confidence")
    val confidence: Double,
    
    @SerializedName("severity")
    val severity: String,
    
    @SerializedName("cause_en")
    val cause_en: String?,
    
    @SerializedName("cause_mr")
    val cause_mr: String?,
    
    @SerializedName("remedy_mr")
    val remedy_mr: String?,
    
    @SerializedName("remedy_en")
    val remedy_en: String?,
    
    @SerializedName("notification_sent")
    val notification_sent: Boolean
)
