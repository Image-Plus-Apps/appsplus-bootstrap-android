package uk.co.appsplus.bootstrap.network.models

import com.squareup.moshi.JsonReader
import okhttp3.ResponseBody

@SuppressWarnings("SwallowedException", "TooGenericExceptionCaught")
inline fun <reified Field> ResponseBody.validationErrors():
        ServerValidation<Field>? where Field : Enum<Field>, Field : ValidationField {
    return try {
        ServerValidation.from(
            JsonReader.of(source()),
            enumValues()
        )
    } catch (exception: Exception) {
        null
    }
}

@SuppressWarnings("SwallowedException", "TooGenericExceptionCaught")
inline fun <reified Field> String.validationErrors():
        ServerValidation<Field>? where Field : Enum<Field>, Field : ValidationField {
    return try {
        ServerValidation.from(
            this,
            enumValues()
        )
    } catch (exception: Exception) {
        null
    }
}

fun ResponseBody.serverMessage(): ServerMessage? {
    return ServerMessageAdapter.message(JsonReader.of(source()))
}

fun String.serverMessage(): ServerMessage? {
    return ServerMessageAdapter.message(this)
}
