package me.cirosanchez.factions.model.scoreboard.util

import me.cirosanchez.factions.Factions

class Line(val plugin: Factions, val index: Int) {
    val node = plugin.configurationManager.scoreboard.getConfigurationSection("lines.$index")!!

    val isAnimated = node.getBoolean("animated")
    val content = node.getStringList("content")

    var i = 0

    fun getNextLine(): String {
        if (!isAnimated) return content.first()

        if (i == content.size-1) i = 0

        i++

        return content[i]
    }
}