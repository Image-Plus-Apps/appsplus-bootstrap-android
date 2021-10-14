package uk.co.appsplus.bootstrap.data.local_storage.secure

import java.io.Serializable

interface KeyValueStorage {
    fun <T : Serializable> putItem(key: String, item: T)
    fun getBool(key: String): Boolean?
    fun getInt(key: String): Int?
    fun getDouble(key: String): Double?
    fun getString(key: String): String?
    fun <T : Serializable> getItem(key: String, clazz: Class<T>): T?
    fun removeItem(key: String)
    fun clear()
}
