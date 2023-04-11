package dev.betrix.moderndatastores

import dev.betrix.moderndatastores.providers.MongoProvider
import dev.betrix.moderndatastores.providers.Provider
import dev.betrix.moderndatastores.providers.YamlProvider
import dev.betrix.moderndatastores.utils.DataStore
import org.bukkit.configuration.MemorySection
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class ModernDatastoresRegistry(private val datastores: ModernDatastores) {

    val defaultProvider: Provider
    val registeredProviders = HashMap<String, Provider>()
    val registeredStores = HashMap<String, HashMap<String, DataStore>>()

    init {
        val defaultSolution = datastores.config.getString("storage_solution") as String
        val provider = getProvider(defaultSolution)

        defaultProvider = provider
        registeredProviders[defaultSolution] = provider

        val customProviderList = datastores.config.get("custom_store_providers")

        if (customProviderList is MemorySection) {
            for (pluginName in customProviderList.getKeys(false)) {
                val customPluginProviders = customProviderList.get(pluginName) as MemorySection

                for (key in customPluginProviders.getKeys(false)) {
                    val value = customPluginProviders.get(key) as String

                    if (!registeredProviders.containsKey(value)) {
                        registeredProviders[value] = getProvider(value)
                    }
                }
            }
        }
    }

    private fun getProvider(name: String): Provider {
        return when (name) {
            "YAML" -> {
                val databaseName = datastores.config.getString("yaml_options.db_name")!!
                YamlProvider(databaseName)
            }
            "MONGO" -> {
                MongoProvider(datastores)
            }
            else -> {
                throw IllegalArgumentException("Unknown storage solution \"$name\" set in custom store providers.")
            }
        }
    }

    fun appendRegistry(plugin: JavaPlugin, stores: List<DataStore>) {
        val mappedStores = HashMap<String, DataStore>()

        for (store in stores) {
            datastores.logger.info("Registering store ${store.name} from ${plugin.name}")
            mappedStores[store.name] = store
        }

        registeredStores[plugin.name] = mappedStores
    }

    fun isStoreRegistered(storeName: String, plugin: Plugin): Boolean {
        val stores = registeredStores[plugin.name] ?: return false
        return stores.containsKey(storeName)
    }
}