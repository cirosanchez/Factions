package me.cirosanchez.factions.util

import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.team.Team
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

fun OfflinePlayer.getTeam(): Team? {
    return Factions.get().teamManager.getTeam(this)
}

fun Player.getTeam(): Team? {
    return Factions.get().teamManager.getTeam(this)
}