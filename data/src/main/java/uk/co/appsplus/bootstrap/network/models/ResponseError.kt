package uk.co.appsplus.bootstrap.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseError(
    val errors: Map<String, List<String>>
)
