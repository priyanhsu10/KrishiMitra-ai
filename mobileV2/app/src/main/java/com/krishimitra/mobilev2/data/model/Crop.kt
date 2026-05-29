package com.krishimitra.mobilev2.data.model

import com.google.gson.annotations.SerializedName

/**
 * Crop entity — represents a crop planted on a farm.
 */
data class Crop(
    val id: String = "",
    val farmId: String,
    val cropType: String,
    val sowingDate: String,
    val stage: String // e.g., "sowing", "growing", "harvest"
)

/**
 * Crop API response
 */
data class CropResponse(
    @SerializedName("id")
    val crop_id: String,
    val farm_id: String,
    val crop_type: String,
    val sowing_date: String,
    val stage: String,
    val estimated_harvest_date: String? = null,
    val growth_progress: Int? = null
) {
    fun toCrop(): Crop = Crop(
        id = crop_id,
        farmId = farm_id,
        cropType = crop_type,
        sowingDate = sowing_date,
        stage = stage
    )
}