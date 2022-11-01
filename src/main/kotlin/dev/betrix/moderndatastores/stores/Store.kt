package dev.betrix.moderndatastores.stores

import dev.betrix.moderndatastores.ModernDatastores
import dev.betrix.moderndatastores.providers.Provider

/**
 * A wrapper for the key, value stores.
 * Use [get] to retrieve values, and [set] to set them.
 */
class Store(private val storeName: String, private val provider: Provider, private val plugin: ModernDatastores) {

    /**
     * Retrieve the value under the given key.
     * *Key will not be created in this store using this function, use [set] when you want to write the new key/value.*
     *
     * @param key Key for the value.
     * @param defaultValue defaultValue that is provided if [key] does not exist.
     * @return The value under [key] with the type inferred from [defaultValue].
     * @since 0.1.0
     */
    fun <T> get(key: String, defaultValue: T): T {
        plugin.logger.info("Accessing $key from store $storeName, with a default value of $defaultValue")
        return provider.getStoreValue(storeName, key, defaultValue)!!
    }

    /**
     * Retrieve the value under the given key.
     *
     * @param key Key for the value.
     * @return The value under [key], or null if the key does not exist.
     * @since 0.1.0
     */
    fun <T> get(key: String): T? {
        plugin.logger.info("Accessing $key from store $storeName")
        return provider.getStoreValue<T>(storeName, key)
    }

    /**
     * Set the value under the given key.
     *
     * @param key Key that will be written to.
     * @param value Value that will be written.
     * @since 0.1.0
     */
    fun set(key: String, value: Any) {
        plugin.logger.info("Setting $key from store $storeName with a value of [$value]")
        provider.setStoreValue(storeName, key, value)
    }
}