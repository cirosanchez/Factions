package me.cirosanchez.factions.model.region

import gg.flyte.twilight.data.Id
import me.cirosanchez.clib.cuboid.Cuboid
import org.bukkit.entity.Player
import java.util.*

class Region(val name: String, val cuboid: Cuboid?, val pvp: Boolean, val type: RegionType, @Id val uuid: UUID) {
    fun isInRegion(player: Player): Boolean {
        return cuboid!!.contains(player.location)
    }
}