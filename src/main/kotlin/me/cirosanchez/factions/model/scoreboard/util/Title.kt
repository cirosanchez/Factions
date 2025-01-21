package me.cirosanchez.factions.model.scoreboard.util

import me.cirosanchez.clib.configuration.Configuration
import me.cirosanchez.clib.extension.colorize

import net.kyori.adventure.text.Component

class Title(configuration: Configuration) {

    private var count = 0
    private var lines = configuration.getStringList("title.lines").map { it.colorize() }.toList()

    fun next(): Component {
        if (count == lines.size-1) count = -1
        count++
        return lines[count]
    }
}