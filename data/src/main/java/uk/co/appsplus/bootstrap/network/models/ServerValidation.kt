package uk.co.appsplus.bootstrap.network.models

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import java.io.IOException

@SuppressWarnings("SwallowedException")
data class ServerValidation<Field>(
    val validation: Map<Field, List<String>>
) {
    companion object {
        inline fun <reified Field> from(
            jsonReader: JsonReader,
            fields: Array<Field>
        ): ServerValidation<Field>? where Field : Enum<Field>, Field : ValidationField {
            return ServerValidationAdapter.errors(jsonReader)
                ?.let { errors ->
                    fields.fold(
                        emptyMap<Field, List<String>>().toMutableMap()
                    ) { result, field ->
                        errors[field.fieldName]
                            ?.let {
                                result[field] = it
                            }
                        result
                    }.toMap()
                }
                ?.takeUnless { it.isEmpty() }
                ?.let { ServerValidation(it) }
        }
    }
}

interface ValidationField {
    val fieldName: String
}

@SuppressWarnings("SwallowedException")
object ServerValidationAdapter {
    private val adapter = Moshi
        .Builder()
        .build()
        .adapter(ResponseError::class.java)

    fun errors(jsonReader: JsonReader): Map<String, List<String>>? {
        return try {
            adapter.fromJson(jsonReader)?.errors
        } catch (exception: IOException) {
            null
        }
    }
}

fun <Field> ServerValidation<Field>.toList(): List<String> {
    return validation.values.flatten()
}

fun <Field> ServerValidation<Field>.joinErrors(separator: String = "\n"): String {
    return toList().joinToString(separator)
}
