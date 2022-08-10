package uk.co.appsplus.bootstrap.network.models

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import java.io.IOException

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

    fun errors(json: String): Map<String, List<String>>? {
        return try {
            adapter.fromJson(json)?.errors
        } catch (exception: IOException) {
            null
        }
    }
}