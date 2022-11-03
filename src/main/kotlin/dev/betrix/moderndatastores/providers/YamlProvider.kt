package dev.betrix.moderndatastores.providers

import dev.betrix.moderndatastores.utils.Entry
import org.bukkit.Bukkit
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class YamlProvider : Provider {

    private val file: FileConfiguration
    private val rawFile: File = File(Bukkit.getServer().pluginManager.getPlugin("ModernDatastores")!!.dataFolder,
            "store.yml")

    init {
        if (!rawFile.exists()) {
            rawFile.createNewFile()
        }

        file = YamlConfiguration.loadConfiguration(rawFile)
    }

    override fun <T> getStoreValue(storeName: String, key: String, defaultValue: T?): T? {
        if (storeName.contains(".")) {
            throw IllegalArgumentException("storeName should not contain any periods")
        }

        if (defaultValue != null && !isSupportedType(defaultValue)) {
            throw IllegalArgumentException("value must be of type Number, Boolean, String, Map or List")
        }

        if (file.contains("$storeName.$key")) {
            @Suppress("UNCHECKED_CAST")
            return file.get("$storeName.$key", defaultValue) as T
        } else if (defaultValue != null) {
            return defaultValue
        } else {
            return null
        }
    }

    override fun setStoreValue(storeName: String, key: String, value: Any) {
        if (!isSupportedType(value)) {
            throw IllegalArgumentException("value must be of type Number, Boolean, String, Map or List")
        }

        if (storeName.contains(".")) {
            throw IllegalArgumentException("storeName should not contain any periods")
        }

        file.set("$storeName.$key", value)
        writeFile()
    }

    override fun setStoreValue(storeName: String, key: String, value: Map<String, Any>) {
        if (storeName.contains(".")) {
            throw IllegalArgumentException("storeName should not contain any periods")
        }

        // Using recursion supports nested maps
        fun loopMap(map: Map<String, Any>, pathSuffix: String = "") {
            for (e in map.entries) {
                if (!isSupportedType(e.value)) {
                    throw IllegalArgumentException(
                            "values in a Map must be of type Number, Boolean, String, Map or List")
                }

                if (e.value is Map<*, *>) {
                    // Nested map
                    @Suppress("UNCHECKED_CAST")
                    loopMap(e.value as Map<String, Any>, pathSuffix + ".${e.key}")
                } else {
                    // Value was a primitive
                    if (pathSuffix == "") {
                        file.set("$storeName.$key.${e.key}", e.value)
                    } else {
                        file.set("$storeName.$key.$pathSuffix.${e.key}", e.value)
                    }
                }
            }
        }

        loopMap(value)
        writeFile()
    }

    override fun retrieveKeys(storeName: String): List<String> {
        val store = file.get(storeName) as MemorySection
        return store.getKeys(false).toList()
    }

    override fun <T> retrieveValues(storeName: String): ArrayList<T> {
        @Suppress("UNCHECKED_CAST")
        return retrieveKeys(storeName).map { file.get("$storeName.$it") } as ArrayList<T>
    }

    override fun <T> retrieveEntries(storeName: String): ArrayList<Entry<T>> {
        @Suppress("UNCHECKED_CAST")
        return retrieveKeys(storeName).map { Entry<T>(it, file.get("$storeName.$it") as T) } as ArrayList<Entry<T>>
    }

    private fun writeFile() {
        rawFile.writeText(file.saveToString())
    }
}