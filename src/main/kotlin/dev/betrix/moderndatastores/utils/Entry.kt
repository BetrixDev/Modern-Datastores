package dev.betrix.moderndatastores.utils

/**
 * Data class for storing a key and value for a store pair
 *
 * @param key The key for the pair
 * @param value The value for the pair with the type of [T]
 */
data class Entry<T>(
        val key: String,
        val value: T
)
