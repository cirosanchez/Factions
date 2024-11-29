package me.cirosanchez.factions.util

import me.cirosanchez.clib.CLib
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.clib.extension.colorize
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.team.Team
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player


fun OfflinePlayer.getTeam(): Team? {
    return Factions.get().teamManager.getTeam(this)
}

fun Player.getTeam(): Team? {
    return Factions.get().teamManager.getTeam(this)
}

fun String.broadcast(vararg placeholders: Placeholder){
    var string = this
    placeholders.forEach { placeholder ->
        string = placeholder.replace(string)
    }
    Bukkit.broadcast(string.colorize())
}

fun broadcastFromConfiguration(key: String, vararg placeholders: Placeholder){
    CLib.instance!!.messagesFile.getString(key)?.broadcast(*placeholders)
}