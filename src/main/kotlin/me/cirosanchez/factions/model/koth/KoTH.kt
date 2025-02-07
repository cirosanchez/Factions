package me.cirosanchez.factions.model.koth

import me.cirosanchez.clib.cuboid.Cuboid
import me.cirosanchez.clib.storage.Id
import me.cirosanchez.clib.storage.MongoSerializable
import me.cirosanchez.factions.model.region.Region

class KoTH(@Id val name: String, val displayName: String, val time: Long, val region: Region?, val cuboid: Cuboid?, val commands: MutableList<String>): MongoSerializable