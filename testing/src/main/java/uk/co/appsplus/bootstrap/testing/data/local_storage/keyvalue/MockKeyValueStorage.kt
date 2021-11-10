package uk.co.appsplus.bootstrap.testing.data.local_storage.keyvalue

import com.squareup.moshi.Moshi
import uk.co.appsplus.bootstrap.data.local_storage.key_value.KeyValueStorage
import java.io.Serializable

class MockKeyValueStorage : KeyValueStorage {
    var itemAdded: Pair<String, Any>? = null
    var items = mutableMapOf<String, String>()
    var calledClear = false
    var removedItem: String? = null

    override fun <T : Serializable> putItem(key: String, item: T) {
        val json = Moshi
            .Builder()
            .build()
            .adapter<T>(item::class.java)
            .toJson(item)
        itemAdded = key to json
    }

    override fun getBool(key: String): Boolean? {
        return getItem(key, Boolean::class.java)
    }

    override fun getDouble(key: String): Double? {
        return getItem(key, Double::class.java)
    }

    override fun getInt(key: String): Int? {
        return getItem(key, Int::class.java)
    }

    override fun getString(key: String): String? {
        return items[key]
    }

    override fun <T : Serializable> getItem(key: String, clazz: Class<T>): T? {
        return items[key]?.let {
            Moshi
                .Builder()
                .build()
                .adapter(clazz)
                .fromJson(it)
        }
    }

    override fun removeItem(key: String) {
        removedItem = key
    }

    override fun clear() {
        calledClear = true
    }
}
