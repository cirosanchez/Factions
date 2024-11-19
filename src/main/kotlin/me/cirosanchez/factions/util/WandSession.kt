package me.cirosanchez.factions.util

import org.bukkit.Location

data class WandSession(var pos1: Location?, var pos2: Location?, val type: WandType) {
    fun isCompleted() = pos1 != null && pos2 != null
}