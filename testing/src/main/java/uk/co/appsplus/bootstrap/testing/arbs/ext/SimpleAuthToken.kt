package uk.co.appsplus.bootstrap.testing.arbs.ext

import android.annotation.SuppressLint
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import uk.co.appsplus.bootstrap.network.models.SimpleAuthToken
import java.util.*

@SuppressLint("NewApi")
fun Arb.Companion.simpleAuthToken(): Arb<SimpleAuthToken> {
    return Arb.bind(
        Arb.string().filterNot { it.isEmpty() },
        Arb.string().filterNot { it.isEmpty() }
    ) { accessToken, refreshToken ->
        val encoder = Base64.getEncoder()
        SimpleAuthToken(
            encoder.encodeToString(accessToken.toByteArray()),
            encoder.encodeToString(refreshToken.toByteArray())
        )
    }
}
