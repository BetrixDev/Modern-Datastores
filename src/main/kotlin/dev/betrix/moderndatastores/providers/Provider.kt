package dev.betrix.moderndatastores.providers

import dev.betrix.moderndatastores.utils.Entry

interface Provider {
    fun <T> getStoreValue(storeName: String, key: String, defaultValue: T? = null): T?
    fun setStoreValue(storeName: String, key: String, value: Any)
    fun setStoreValue(storeName: String, key: String, value: Map<String, Any>)
    fun retrieveKeys(storeName: String): List<String>
    fun <T> retrieveValues(storeName: String): ArrayList<T>
    fun <T> retrieveEntries(storeName: String): ArrayList<Entry<T>>

    fun isSupportedType(value: Any): Boolean {
        return value is Boolean || value is Number || value is String || value is Map<*, *> || value is List<*>
    }
}