package me.cirosanchez.factions.command

import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("region")
@CommandPermission("factions.command.region")
@Description("Region related commands.")
class RegionCommand {

    @DefaultFor("~")
    fun def(actor: Player){
        actor.sendColorizedMessageFromMessagesFileList("region.default")
    }

    @Subcommand("map")
    @CommandPermission("factions.command.region.map")
    fun map(actor: Player){

    }
}