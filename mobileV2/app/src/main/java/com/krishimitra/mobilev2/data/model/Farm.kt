package com.krishimitra.mobilev2.data.model

/**
 * Farm entity — represents a farmer's farm/land parcel.
 */
data class Farm(
    val id: String = "",
    val farmerId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val areaAcres: Double,
    val soilType: String
)

/**
 * Farm API response
 */
data class FarmResponse(
    val farm_id: String,
    val farmer_id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val area_acres: Double,
    val soil_type: String
) {
    fun toFarm(): Farm = Farm(
        id = farm_id,
        farmerId = farmer_id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        areaAcres = area_acres,
        soilType = soil_type
    )
}