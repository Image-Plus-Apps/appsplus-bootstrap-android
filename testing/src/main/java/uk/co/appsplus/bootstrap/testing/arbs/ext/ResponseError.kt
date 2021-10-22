package uk.co.appsplus.bootstrap.testing.arbs.ext

import com.squareup.moshi.Moshi
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import uk.co.appsplus.bootstrap.network.models.ResponseError

fun Arb.Companion.domainError(): Arb<ResponseError> {
    return Arb
        .map(Arb.string(), Arb.list(Arb.string(), range = 1..3), minSize = 0, maxSize = 2)
        .map { ResponseError(it) }
}

fun ResponseError.toJson(): String {
    return Moshi.Builder().build().adapter(ResponseError::class.java).toJson(this)
}

fun Arb.Companion.domainErrorJson(): Arb<String> {
    return domainError()
        .map {
            it.toJson()
        }
}
