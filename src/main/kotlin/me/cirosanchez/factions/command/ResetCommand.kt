package me.cirosanchez.factions.command

import me.cirosanchez.factions.Factions
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import revxrsal.commands.annotation.Command
import revxrsal.commands.bukkit.annotation.CommandPermission

class ResetCommand {
    @Command("resetallinfo")
    @CommandPermission("factions.command.reset")
    fun reset(commandSender: CommandSender){
        val plugin = Factions.get()
        plugin.regionManager.reset()
        plugin.kothManager.reset()
        plugin.mineManager.reset()
        plugin.userManager.reset()
        plugin.spawnManager.reset()
        plugin.teamManager.reset()
        Bukkit.getServer().shutdown()
    }
}