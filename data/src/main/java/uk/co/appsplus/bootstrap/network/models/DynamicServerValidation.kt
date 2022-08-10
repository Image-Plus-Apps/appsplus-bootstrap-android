package uk.co.appsplus.bootstrap.network.models

import com.squareup.moshi.JsonReader

@SuppressWarnings("SwallowedException")
data class DynamicServerValidation(
    val validation: Map<String, List<String>>
) {
        companion object {
            fun from(
                jsonReader: JsonReader,
            ): DynamicServerValidation? {
                return ServerValidationAdapter.errors(jsonReader)
                    ?.let { DynamicServerValidation(it) }
            }

            fun from(
                jsonReader: String,
            ): DynamicServerValidation? {
                return ServerValidationAdapter.errors(jsonReader)
                    ?.let { DynamicServerValidation(it) }
            }
        }
}

fun DynamicServerValidation.toList(): List<String> {
    return validation.values.flatten()
}

fun DynamicServerValidation.joinErrors(separator: String = "\n"): String {
    return toList().joinToString(separator)
}

operator fun DynamicServerValidation.get(field: String): List<String>? {
    return validation[field]
}
