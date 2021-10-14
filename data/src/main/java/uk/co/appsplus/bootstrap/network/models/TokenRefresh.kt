package uk.co.appsplus.bootstrap.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenRefresh(
    @Json(name = "device_name") val deviceName: String
)
