package uk.co.appsplus.bootstrap.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import java.io.IOException

@JsonClass(generateAdapter = true)
data class ServerMessage(
    @Json(name = "message") val message: String
)

object ServerMessageAdapter {
    private val adapter = Moshi
        .Builder()
        .build()
        .adapter(ServerMessage::class.java)

    fun message(jsonReader: JsonReader): ServerMessage? {
        return try {
            adapter.fromJson(jsonReader)
        } catch (exception: IOException) {
            null
        }
    }
}