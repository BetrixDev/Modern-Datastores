package dev.betrix.moderndatastores.providers

import dev.betrix.moderndatastores.utils.Entry
import org.bukkit.Bukkit
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class YamlProvider(databaseName: String) : Provider {

    private val file: FileConfiguration
    private val rawFile: File

    init {
        rawFile = File(Bukkit.getServer().pluginManager.getPlugin("ModernDatastores")!!.dataFolder,
                "$databaseName.yaml")

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
            val value = file.get("$storeName.$key", defaultValue)

            if (value is MemorySection) {
                val mappedValue = HashMap<String, Any>()

                fun createMap(mem: MemorySection) {
                    for (memKey in mem.getKeys(false)) {
                        val memValue = mem.get(memKey)!!

                        if (memValue is MemoryConfiguration) {
                            createMap(memValue)
                        } else {
                            mappedValue[memKey] = memValue
                        }
                    }
                }

                createMap(value)

                @Suppress("UNCHECKED_CAST")
                return mappedValue as T
            } else {
                @Suppress("UNCHECKED_CAST")
                return value as T
            }

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

    override fun <T> retrieveEntries(storeName: String): List<Entry<T>> {
        @Suppress("UNCHECKED_CAST")
        return retrieveKeys(storeName).map { Entry(it, file.get("$storeName.$it")!! as T) }
    }

    override fun checkStoreExists(storeName: String): Boolean {
        return file.contains(storeName)
    }

    override fun removeEntry(storeName: String, key: String) {
        file.set("$storeName.$key", null)
        writeFile()
    }

    private fun writeFile() {
        rawFile.writeText(file.saveToString())
    }
}