package uk.co.appsplus.bootstrap.testing.arbs.ext

import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import uk.co.appsplus.bootstrap.network.models.ResponseError
import uk.co.appsplus.bootstrap.network.models.ServerValidation
import uk.co.appsplus.bootstrap.network.models.ValidationField

inline fun <reified Field> Arb.Companion.serverValidation(
    maximumErrorsCount: Int = 2
):
        Arb<ServerValidation<Field>> where Field : Enum<Field> {
    return Arb.list(
        Arb.enum<Field>(),
        1..(enumValues<Field>().size)
    )
        .flatMap { fields ->
            arbitrary {
                ServerValidation(
                    fields
                        .toSet()
                        .fold(emptyMap<Field, List<String>>()) { result, field ->
                            val errors = Arb.list(
                                Arb.string(),
                                1 until (maximumErrorsCount + 1)
                            )
                            val newMap = result.toMutableMap()
                            newMap[field] = errors.next(it)
                            newMap
                        }
                )
            }
        }
}

fun <Field> ServerValidation<Field>.toJson(): String where Field : ValidationField {
    return validation.mapKeys { it.key.fieldName }.toJson()
}

fun Map<String, List<String>>.toJson(): String {
    return ResponseError(this).toJson()
}
