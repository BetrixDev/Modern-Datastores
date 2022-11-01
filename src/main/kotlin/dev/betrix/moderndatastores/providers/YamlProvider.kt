package dev.betrix.moderndatastores.providers

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class YamlProvider : Provider {

    private val file: FileConfiguration
    private val rawFile: File = File(Bukkit.getServer().pluginManager.getPlugin("ModernDatastores")!!.dataFolder, "store.yml")

    init {
        if (!rawFile.exists()) {
            rawFile.createNewFile()
        }

        file = YamlConfiguration.loadConfiguration(rawFile)
    }

    override fun <T> getStoreValue(storeName: String, key: String, defaultValue: T?): T? {
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

        file.set("$storeName.$key", value)
        writeFile()
    }

    override fun setStoreValue(storeName: String, key: String, value: Map<String, Any>) {
        // Using recursion supports nested maps
        fun loopMap(map: Map<String, Any>, pathSuffix: String = "") {
            for (e in map.entries) {
                if (!isSupportedType(e.value)) {
                    throw IllegalArgumentException("values in a Map must be of type Number, Boolean, String, Map or List")
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

    private fun writeFile() {
        rawFile.writeText(file.saveToString())
    }
}