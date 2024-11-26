package me.cirosanchez.factions.model.region.util

import me.cirosanchez.factions.Factions
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

object RegionListener : Listener {
    val rm = Factions.get().regionManager
    @EventHandler
    fun move(event: PlayerMoveEvent){
        val from = event.from
        val to = event.from

        val fromRegion = rm.getRegion(from) ?: return
        val toRegion = rm.getRegion(to) ?: return

        if (fromRegion.uuid != toRegion.uuid){
            Bukkit.getPluginManager().callEvent(PlayerRegionChangeEvent(event.player, fromRegion, toRegion))
        }
    }
}