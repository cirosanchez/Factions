package me.cirosanchez.factions.model.scoreboard

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.Listener

class ScoreboardListener(val scoreboardManager: ScoreboardManager) : Listener {

    // Register a new player in the scoreboard manager.
    @EventHandler
    fun join(e: PlayerJoinEvent){
        scoreboardManager.register(e.player)

    }


    // Unregister a disconnected player in the scoreboard manager.
    @EventHandler
    fun quit(e: PlayerQuitEvent){
        scoreboardManager.unregister(e.player)
    }
}