package dev.betrix.moderndatastores

import dev.betrix.moderndatastores.stores.Store
import dev.betrix.moderndatastores.utils.DataStore
import org.bukkit.plugin.java.JavaPlugin

class ModernDatastores : JavaPlugin() {

    companion object {
        private lateinit var instance: ModernDatastores
        private lateinit var registry: ModernDatastoresRegistry

        /**
         * Retrieve the key/value store under the specified name.
         *
         * @param storeName Name of the store to retrieve.
         * @return The associated [Store] object.
         * @since 0.1.0
         */
        @JvmStatic
        fun getStore(plugin: JavaPlugin, storeName: String): Store {
            return Store(plugin, storeName, instance, registry)
        }

        /**
         * Register all stores you plan to use within your plugin.
         *
         * @param plugin Your plugin's main class.
         * @param stores A list of [DataStore]s containing the name of usage description for each store you are using.
         * @since 0.1.0
         */
        @JvmStatic
        fun registerStores(plugin: JavaPlugin, stores: List<DataStore>) {
            registry.appendRegistry(plugin, stores)
        }
    }

    override fun onEnable() {
        saveDefaultConfig()

        instance = this
        registry = ModernDatastoresRegistry(this)

        logger.info("Modern Datastores Initialized")
    }
}