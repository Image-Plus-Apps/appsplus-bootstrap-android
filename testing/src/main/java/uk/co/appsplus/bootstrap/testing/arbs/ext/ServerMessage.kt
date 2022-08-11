package uk.co.appsplus.bootstrap.testing.arbs.ext

import com.squareup.moshi.Moshi
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import uk.co.appsplus.bootstrap.network.models.ServerMessage

fun Arb.Companion.serverMessage(): Arb<ServerMessage> {
    return Arb.string().map { ServerMessage(it) }
}

fun ServerMessage.toJson(): String {
    return Moshi.Builder().build().adapter(ServerMessage::class.java).toJson(this)
}
