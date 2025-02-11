package me.cirosanchez.factions.model.scoreboard.util

import me.cirosanchez.factions.Factions

class Footer(val plugin: Factions) {
    val list = plugin.configurationManager.scoreboard.getStringList("footer")
    var index = 0

    fun getNextLine(): String {
        if (index == list.size-1) index = 0

        index++
        return list[index]
    }
}