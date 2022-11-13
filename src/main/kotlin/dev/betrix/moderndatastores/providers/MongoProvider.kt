package dev.betrix.moderndatastores.providers

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import dev.betrix.moderndatastores.ModernDatastores
import dev.betrix.moderndatastores.utils.Entry
import org.bson.Document
import java.net.UnknownHostException

class MongoProvider(private val datastores: ModernDatastores) : Provider {

    private val mongoClient: MongoClient
    private val database: MongoDatabase

    init {
        try {
            mongoClient = MongoClients.create(datastores.config.getString("mongo_options.uri")!!)
            database = mongoClient.getDatabase(datastores.config.getString("mongo_options.database_name")!!)
        } catch (_: UnknownHostException) {
            throw UnknownHostException("Unable to connect to MongoDB")
        }
    }

    override fun <T> getStoreValue(storeName: String, key: String, defaultValue: T?): T? {
        val store = database.getCollection(storeName)
        val query = BasicDBObject("_id", key)
        val result = store.find(query).first() ?: return null

        @Suppress("UNCHECKED_CAST")
        return result["value"] as T
    }

    override fun setStoreValue(storeName: String, key: String, value: Any) {
        setValue(storeName, key, value)
    }

    override fun setStoreValue(storeName: String, key: String, value: Map<String, Any>) {
        // The mongo driver will handle Maps for us
        setValue(storeName, key, value)
    }

    private fun setValue(storeName: String, key: String, value: Any) {
        val collection = database.getCollection(storeName)
        val doc = Document("_id", key)

        val found = collection.find(doc).first()

        if (found != null) {
            collection.findOneAndReplace(found, Document().append("value", value))
        } else {
            doc["value"] = value
            collection.insertOne(doc)
        }
    }

    override fun retrieveKeys(storeName: String): List<String> {
        val collection = database.getCollection(storeName)
        @Suppress("UNCHECKED_CAST")
        return collection.find().map { it["_id"]!! }.toList() as List<String>
    }

    override fun <T> retrieveValues(storeName: String): ArrayList<T> {
        val collection = database.getCollection(storeName)
        @Suppress("UNCHECKED_CAST")
        return collection.find().map { it["value"]!! }.toList() as ArrayList<T>
    }

    override fun <T> retrieveEntries(storeName: String): List<Entry<T>> {
        val collection = database.getCollection(storeName)
        @Suppress("UNCHECKED_CAST")
        return collection.find().map { Entry<T>(it["_id"]!! as String, it["value"]!! as T) }.toList()
    }

    override fun checkStoreExists(storeName: String): Boolean {
        return database.listCollectionNames().contains(storeName)
    }
}