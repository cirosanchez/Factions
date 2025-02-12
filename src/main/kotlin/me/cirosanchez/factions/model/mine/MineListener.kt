package me.cirosanchez.factions.model.mine

import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.region.util.PlayerRegionChangeEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.checkerframework.checker.units.qual.mm

class MineListener(val plugin: Factions) : Listener {


    @EventHandler
    fun regionChangeEvent(event: PlayerMoveEvent){
        val player = event.player
        val to = event.to   

        val mine = plugin.mineManager.getMine(plugin.regionManager.getRegion(to) ?: return) ?: return

        if (!player.hasPermission(mine.permission)){
            event.isCancelled = true
            player.sendColorizedMessageFromMessagesFile("mine.no-permission-to-enter")
            return
        }
    }
}