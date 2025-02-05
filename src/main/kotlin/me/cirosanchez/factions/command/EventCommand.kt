package me.cirosanchez.factions.command

import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.bukkit.annotation.CommandPermission


@Command("event")
@CommandPermission("factions.command.event")
class EventCommand {

    @DefaultFor("~")
    fun default(actor: Player){

    }
}