package uk.co.appsplus.bootstrap.mocks.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import uk.co.appsplus.bootstrap.network.models.AuthToken
import java.util.*

fun Arb.Companion.authTokenGenerator(): Arb<MockAuthToken> {
    return Arb.bind(
        Arb.string().filterNot { it.isEmpty() },
        Arb.string().filterNot { it.isEmpty() },
        Arb.int().orNull(),
        Arb.string().orNull()
    ) { accessToken, refreshToken, extraInt, extraString ->
        val encoder = Base64.getEncoder()
        MockAuthToken(
            encoder.encodeToString(accessToken.toByteArray()),
            encoder.encodeToString(refreshToken.toByteArray()),
            extraInt,
            extraString
        )
    }
}

@JsonClass(generateAdapter = true)
data class MockAuthToken(
    @Json(name = "token")
    override val accessToken: String,
    @Json(name = "refresh_token")
    override val refreshToken: String,
    val extraInt: Int?,
    val extraString: String?
) : AuthToken
