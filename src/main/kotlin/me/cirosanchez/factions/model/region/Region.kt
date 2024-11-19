package me.cirosanchez.factions.model.region

import me.cirosanchez.clib.cuboid.Cuboid
import org.bson.codecs.pojo.annotations.BsonId
import org.bukkit.entity.Player
import java.util.*

class Region(val name: String, val cuboid: Cuboid?, val pvp: Boolean, val type: RegionType, @BsonId val uuid: UUID) {
    fun isInRegion(player: Player): Boolean {
        return cuboid!!.contains(player.location)
    }
}