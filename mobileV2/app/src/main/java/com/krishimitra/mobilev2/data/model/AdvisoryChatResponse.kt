package com.krishimitra.mobilev2.data.model

/**
 * Advisory chat response from AI service.
 */
data class AdvisoryChatResponse(
    val advice: String?,
    val advice_mr: String?,
    val advice_en: String?,
    val alert_type: String?,
    val priority: String?
)
