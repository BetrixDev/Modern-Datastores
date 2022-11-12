package dev.betrix.moderndatastores

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class ModernDatastoresExpansion : PlaceholderExpansion() {
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
        if (paramString == null) {
            return "INVALID PARAMS"
        }

        val params = paramString.split(".")

        if (!ModernDatastores.storeExists(params[0])) {
            return "INVALID STORE NAME"
        }

        val store = ModernDatastores.getStore(params[0])
        val value = store.get<Any>(params[1], true)

        if (value is Map<*, *>) {
            if (params.size == 2) {
                return "VALUE CAN'T BE MAP"
            }

            fun traversePath(depth: Int): Any {
                return if (value[params[depth]] is Map<*, *>) {
                    traversePath(depth + 1)
                } else {
                    value[params[depth]]!!
                }
            }

            return traversePath(2).toString()
        } else {
            return value.toString()
        }
    }

    override fun persist(): Boolean {
        return true
    }
}