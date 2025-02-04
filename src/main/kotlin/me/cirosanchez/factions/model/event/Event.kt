package me.cirosanchez.factions.model.event

import me.cirosanchez.clib.cuboid.Cuboid
import me.cirosanchez.clib.storage.Id
import me.cirosanchez.clib.storage.MongoSerializable
import me.cirosanchez.factions.model.region.Region

class Event(@Id val name: String, val displayName: String, val type: EventType, val time: Long, val commands: List<String>, val claim: Region, val capZone: Cuboid): MongoSerializable