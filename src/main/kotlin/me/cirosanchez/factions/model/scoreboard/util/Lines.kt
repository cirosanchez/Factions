package me.cirosanchez.factions.model.scoreboard.util

import org.bukkit.entity.Player
import me.cirosanchez.clib.extension.placeholders
import me.cirosanchez.clib.configuration.Configuration

class Lines(configuration: Configuration) {
    private var lines: List<String> = configuration.getStringList("content")

    fun get(player: Player) = lines.map { it.placeholders() }

}