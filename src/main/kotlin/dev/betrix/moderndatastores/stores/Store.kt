package dev.betrix.moderndatastores.stores

import dev.betrix.moderndatastores.ModernDatastores
import dev.betrix.moderndatastores.ModernDatastoresRegistry
import dev.betrix.moderndatastores.providers.Provider
import dev.betrix.moderndatastores.utils.Entry
import org.bukkit.plugin.java.JavaPlugin

/**
 * A wrapper for the key, value stores.
 * Use [get] to retrieve values, and [set] to set them.
 */
class Store(plugin: JavaPlugin, store: String, private val datastores: ModernDatastores, registry: ModernDatastoresRegistry) {

    // Prefix store name with the plugin name so different plugins can use the same store names without overlaps
    private val storeName = "${plugin.name}$store"
    private val provider: Provider

    init {
        if (!isValidStoreName(store)) {
            throw IllegalArgumentException("Store name must only contain letters and numbers.")
        }

        if (!registry.isStoreRegistered(store, plugin)) {
            throw IllegalStateException("You must register a store before using it.")
        }

        val customProvider = datastores.config.getString("custom_store_providers.${plugin.name}.${store}")

        provider = if (customProvider == null) {
            registry.defaultProvider
        } else {
            registry.registeredProviders[customProvider]!!
        }
    }

    private fun isValidStoreName(name: String): Boolean {
        if (name.isEmpty()) return false
        return name.all { it.isLetter() || it.isDigit() }
    }

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
        datastores.logger.info("Accessing $key from store $storeName, with a default value of $defaultValue")
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
        datastores.logger.info("Accessing $key from store $storeName")
        return provider.getStoreValue<T>(storeName, key)
    }

    /**
     * Set the value under the given key.
     *
     * @param key Key that will be written to.
     * @param value Value that will be written.
     * @return the [Store] object to allow for chaining.
     * @since 0.1.0
     */
    fun set(key: String, value: Any): Store {
        datastores.logger.info("Setting $key from store $storeName with a value of [$value]")
        provider.setStoreValue(storeName, key, value)

        return this
    }

    /**
     * Retrieve a list of keys in the store.
     *
     * @return A string list of keys in the store.
     * @since 0.1.0
     */
    fun keys(): List<String> {
        datastores.logger.info("Returning all keys in $storeName")
        return provider.retrieveKeys(storeName)
    }

    /**
     * Retrieve a list of values in the store.
     *
     * @return A list of values in the store.
     * @since 0.1.0
     */
    fun <T> values(): List<T> {
        datastores.logger.info("Returning all values in $storeName")
        return provider.retrieveValues(storeName)
    }

    /**
     * Retrieve a list of entries in the store.
     *
     * @return A string list of entries in the store using a custom [Entry] data class.
     * @since 0.1.0
     */
    fun <T> entries(): List<Entry<T>> {
        datastores.logger.info("Returning all entries in $storeName")
        return provider.retrieveEntries(storeName)
    }

    /**
     * Remove the specified key/value pair from a [Store]
     *
     * @param key The key for the pair to remove.
     * @since 0.1.0
     */
    fun remove(key: String) {
        datastores.logger.info("Removing $key from $storeName")
        provider.removeEntry(storeName, key)
    }
}