package me.cirosanchez.factions.model.scoreboard.listener

import me.cirosanchez.factions.model.scoreboard.ScoreboardManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ScoreboardListener(val manager: ScoreboardManager) : Listener {



    @EventHandler
    fun join(event: PlayerJoinEvent){
        manager.register(event.player)
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent){
        manager.unregister(event.player)
    }
}