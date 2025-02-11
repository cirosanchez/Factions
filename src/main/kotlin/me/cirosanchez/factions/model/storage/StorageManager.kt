package me.cirosanchez.factions.model.storage

import me.cirosanchez.clib.storage.MongoDB.collection
import me.cirosanchez.clib.storage.MongoSerializable
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager


import kotlin.reflect.KClass

class StorageManager : Manager {
    lateinit var plugin: Factions

    fun <T : MongoSerializable> saveObject(obj: T){
        obj.save().get()
    }

    fun <T : MongoSerializable> readObjects(type: KClass<T>): List<T> {
        val list =  collection(type).find().get().toList()

        if (list.isEmpty()) return listOf<T>()
        return list as List<T>
    }

    override fun load() {
        // Nothing in here, yay!
    }

    override fun unload() {
        // Nothing in here, yay!
    }

}