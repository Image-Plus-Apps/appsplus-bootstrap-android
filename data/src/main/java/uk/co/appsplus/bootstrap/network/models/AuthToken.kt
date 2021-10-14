package uk.co.appsplus.bootstrap.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

interface AuthToken : Serializable {
    val accessToken: String
    val refreshToken: String
}

@JsonClass(generateAdapter = true)
data class SimpleAuthToken(
    @Json(name = "token")
    override val accessToken: String,
    @Json(name = "refresh_token")
    override val refreshToken: String
) : AuthToken
