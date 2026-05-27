package com.krishimitra.mobilev2.data.model

/**
 * Disease detection response from AI service.
 */
data class DiseaseDetectionResponse(
    val report_id: String,
    val disease: String,
    val confidence: Double,
    val severity: String,
    val remedy_mr: String,
    val remedy_en: String,
    val notification_sent: Boolean
)