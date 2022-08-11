package uk.co.appsplus.bootstrap.testing.arbs.ext

import com.squareup.moshi.Moshi
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import uk.co.appsplus.bootstrap.network.models.ResponseError

fun Arb.Companion.domainError(
    minimumFieldsCount: Int = 0,
    maximumFieldsCount: Int = 2,
    maximumErrorsCount: Int = 2
): Arb<ResponseError> {
    return Arb
        .map(
            Arb.string(),
            Arb.list(
                Arb.string(), range = 1 until (maximumErrorsCount + 1)
            ),
            minSize = minimumFieldsCount,
            maxSize = maximumFieldsCount
        )
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
