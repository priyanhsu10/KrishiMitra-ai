package com.krishimitra.mobilev2.data.model

data class DiseaseDetectionRequest(
    val disease_image_url: String,
    val farmer_id: String,
    val crop_id: String,
    val crop_type: String
)