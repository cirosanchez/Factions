package me.cirosanchez.factions.model.region

import me.cirosanchez.clib.cuboid.Cuboid
import me.cirosanchez.clib.storage.Id
import me.cirosanchez.clib.storage.MongoSerializable
import org.bukkit.entity.Player
import java.util.*

class Region(val name: String, val cuboid: Cuboid?, val pvp: Boolean, val type: RegionType, @Id val uuid: UUID): MongoSerializable {
    fun isInRegion(player: Player): Boolean {
        return cuboid!!.contains(player.location)
    }
}