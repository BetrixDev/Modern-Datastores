package dev.betrix.moderndatastores

import dev.betrix.moderndatastores.providers.Provider
import dev.betrix.moderndatastores.providers.YamlProvider
import dev.betrix.moderndatastores.stores.Store
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class ModernDatastores : JavaPlugin() {

    companion object {
        private lateinit var instance: ModernDatastores
        private lateinit var provider: Provider // Eventually there will be more data providers

        /**
         * Retrieve the key, value store under the specified name.
         *
         * @param storeName Name of the store to retrieve.
         * @return The associated [Store] object. **If the store doesn't exist, it will be created.**
         * @since 0.1.0
         */
        @JvmStatic
        fun getStore(storeName: String): Store {
            return Store(storeName, provider, instance)
        }

        @JvmStatic
        fun storeExists(storeName: String): Boolean {
            return provider.checkStoreExists(storeName)
        }
    }

    override fun onEnable() {
        saveDefaultConfig()

        instance = this

        when (val solution = config.getString("storage_solution")) {
            "YAML" -> provider = YamlProvider(this)
            else -> {
                logger.severe("Unknown storage solution \"$solution\" in config, disabling")
                server.pluginManager.disablePlugin(this)
            }
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            logger.info("PlaceholderAPI detected, Registering expansion")
            ModernDatastoresExpansion().register()
        }

        logger.info("Modern Datastores Initialized")
    }
}