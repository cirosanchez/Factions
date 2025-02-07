package me.cirosanchez.factions.model.koth

import me.cirosanchez.clib.cuboid.Cuboid
import me.cirosanchez.clib.storage.Id
import me.cirosanchez.clib.storage.MongoSerializable
import me.cirosanchez.factions.model.region.Region

class KoTH(@Id var name: String, var time: Long, val region: Region?, val cuboid: Cuboid?, val commands: MutableList<String>): MongoSerializable