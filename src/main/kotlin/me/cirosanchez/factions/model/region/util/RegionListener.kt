package me.cirosanchez.factions.model.region.util

import me.cirosanchez.factions.Factions
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

object RegionListener : Listener {
    val rm = Factions.get().regionManager

    @EventHandler
    fun move(event: PlayerMoveEvent) {
        val from = event.from
        val to = event.to ?: return


        if (from.blockX == to.blockX && from.blockY == to.blockY && from.blockZ == to.blockZ) return

        val fromRegion = rm.getRegion(from)
        val toRegion = rm.getRegion(to)

        if (fromRegion == null || toRegion == null) {
            println("Player moved to/from an unregistered region")
            return
        }

        if (fromRegion.uuid != toRegion.uuid) {
            Bukkit.getPluginManager().callEvent(PlayerRegionChangeEvent(event.player, fromRegion, toRegion))
        }
    }
}