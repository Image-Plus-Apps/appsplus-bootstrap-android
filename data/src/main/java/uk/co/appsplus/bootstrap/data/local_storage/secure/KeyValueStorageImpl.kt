package uk.co.appsplus.bootstrap.data.local_storage.secure

import android.content.SharedPreferences
import android.util.Log
import com.squareup.moshi.Moshi
import java.io.Serializable

class KeyValueStorageImpl(
    private val sharedPrefs: () -> SharedPreferences
) : KeyValueStorage {

    override fun <T : Serializable> putItem(key: String, item: T) {
        saveData(key, item)
    }

    override fun getBool(key: String): Boolean? {
        return getItem(key, Boolean::class.java)
    }

    override fun getInt(key: String): Int? {
        return getItem(key, Int::class.java)
    }

    override fun getDouble(key: String): Double? {
        return getItem(key, Double::class.java)
    }

    override fun getString(key: String): String? {
        return getPreferences().getString(key, null)
    }

    override fun <T : Serializable> getItem(key: String, clazz: Class<T>): T? {
        val value = getPreferences().getString(key, null)
        return try {
            if (value == null) {
                null
            } else {
                Moshi
                    .Builder()
                    .build()
                    .adapter(clazz)
                    .fromJson(value)
            }
        } catch (e: IllegalArgumentException) {
            Log.e("Serialization", "Failed to serialize class from json String," +
                    " make sure you use @JsonClass(generateAdapter = true) on the class")
            null
        }
    }

    override fun removeItem(key: String) {
        edit {
            it.remove(key)
        }
    }

    override fun clear() {
        edit {
            it.clear()
        }
    }

    private fun <T : Serializable> saveData(key: String, data: T) {
        if (data is String) {
            edit {
                it.putString(key, data)
            }
        } else {
            try {
                val json = Moshi
                    .Builder()
                    .build()
                    .adapter<T>(data::class.java)
                    .toJson(data)
                edit {
                    it.putString(key, json)
                }
            } catch (e: IllegalArgumentException) {
                Log.e("Serialization", "Failed to serialize class to json String," +
                        " make sure you use @JsonClass(generateAdapter = true) on the class")
            }
        }
    }

    private fun edit(block: (SharedPreferences.Editor) -> SharedPreferences.Editor) {
        synchronized(this) {
            with(sharedPrefs().edit()) {
                block(this)
                apply()
            }
        }
    }

    @PublishedApi internal fun getPreferences(): SharedPreferences {
        synchronized(this) {
            return sharedPrefs()
        }
    }
}
