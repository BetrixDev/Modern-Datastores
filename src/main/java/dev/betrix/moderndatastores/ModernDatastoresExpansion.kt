package dev.betrix.moderndatastores

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class ModernDatastoresExpansion(private val datastores: ModernDatastores) : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "md"
    }

    override fun getAuthor(): String {
        return "Betrix"
    }

    override fun getVersion(): String {
        return "0.1.0"
    }

    override fun onRequest(p: OfflinePlayer?, paramString: String?): String {
        /*
            %md_PLUGIN-NAME_STORE-NAME_KEY-NAME%
            %md_PLUGIN-NAME_STORE-NAME_KEY-NAME_KEY-FROM-MAP%
            // TODO: Add list support
         */

        if (paramString == null) {
            return "INVALID PARAMS"
        }

        val params = paramString.split(".")
        val pluginName = params[0]
        val storeName = params[1]

        val plugin = Bukkit.getPluginManager().getPlugin(pluginName) ?: return "INVALID PLUGIN NAME"

        if (!ModernDatastores.registry.registeredStores.containsKey(pluginName)) {
            return "PLUGIN IS NOT REGISTERED"
        }

        if (!ModernDatastores.registry.registeredStores[pluginName]!!.containsKey(storeName)) {
            return "INVALID STORE NAME"
        }

        val store = ModernDatastores.getStore(plugin, storeName)
        val value = store.get<Any>(params[2], true)

        if (value is Map<*, *>) {
            if (params.size == 3) {
                return "VALUE CAN'T BE MAP"
            }

            fun traversePath(depth: Int): Any {
                return if (value[params[depth]] is Map<*, *>) {
                    traversePath(depth + 1)
                } else {
                    value[params[depth]]!!
                }
            }

            return traversePath(3).toString()
        } else {
            return value.toString()
        }
    }

    override fun persist(): Boolean {
        return true
    }
}