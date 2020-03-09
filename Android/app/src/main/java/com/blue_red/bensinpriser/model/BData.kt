package com.blue_red.bensinpriser.model

data class BDatum (
    val prices: List<Price>?,
    val stationID: Int?,
    val BCompany: BCompany?,
    val latitude: Double?,
    val longitude: Double?,
    val stationName: String?
)

data class BCompany (
    val companyURL: String?,
    val companyID: Int?,
    val companyName: String?,
    val logoURL: String?
)

data class Price (
    val type: FuelType?,
    val price: Double?
)

enum class FuelType {
    bensin95,
    bensin98,
    diesel,
    ethanol85
}
