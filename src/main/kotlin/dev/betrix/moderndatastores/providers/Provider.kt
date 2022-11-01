package dev.betrix.moderndatastores.providers

interface Provider {
    fun <T> getStoreValue(storeName: String, key: String, defaultValue: T ?= null): T?
    fun setStoreValue(storeName: String, key: String, value: Any)
    fun setStoreValue(storeName: String, key: String, value: Map<String, Any>)

    fun isSupportedType(value: Any): Boolean {
        return value is Boolean || value is Number || value is String || value is Map<*, *> || value is List<*>
    }
}