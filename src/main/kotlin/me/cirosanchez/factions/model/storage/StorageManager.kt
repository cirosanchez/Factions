package me.cirosanchez.factions.model.storage

import gg.flyte.twilight.data.MongoDB
import gg.flyte.twilight.data.MongoDB.collection
import gg.flyte.twilight.data.MongoSerializable
import gg.flyte.twilight.twilight
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.spawn.Spawn
import org.bukkit.Bukkit
import kotlin.reflect.KClass

class StorageManager {
    private val plugin = Factions.get()

    init {
        val uri = plugin.configFile.getString("mongo.uri")
        val dbName = plugin.configFile.getString("mongo.database")

        if (uri.isNullOrEmpty()) {
            plugin.logger.severe("MongoDB URI is not present. Shutting off.")
            Bukkit.getPluginManager().disablePlugin(plugin)
            throw org.bukkit.configuration.InvalidConfigurationException()
        }
        if (dbName.isNullOrEmpty()){
            plugin.logger.severe("MongoDB Database name is not present. Shutting off.")
            Bukkit.getPluginManager().disablePlugin(plugin)
            throw org.bukkit.configuration.InvalidConfigurationException()
        }

        twilight(plugin){
            mongo {
                this.uri = uri
                this.database = dbName
            }
        }
    }

    fun <T : MongoSerializable> saveObject(obj: T){
        obj.save().get()
    }

    fun <T : MongoSerializable> readObjects(type: KClass<T>): List<T> {
        return collection(type).find().get().toList() as List<T>
    }



    fun load(){
        plugin.spawnManager.spawn = readObjects(Spawn::class).first() as Spawn
    }

    fun unload(){
        saveObject(plugin.spawnManager.spawn)
    }
}