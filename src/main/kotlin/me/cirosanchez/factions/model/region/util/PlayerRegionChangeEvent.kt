package me.cirosanchez.factions.model.region.util

import me.cirosanchez.factions.model.region.Region
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerRegionChangeEvent(val player: Player, val from: Region, val to: Region) : Event() {
    companion object {
        val HANDLERS = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }
}