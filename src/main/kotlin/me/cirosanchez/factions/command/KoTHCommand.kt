package me.cirosanchez.factions.command

import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.koth.KoTH
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("koth")
@CommandPermission("factions.command.koth")
class KoTHCommand {

    val plugin = Factions.get()
    val manager = plugin.kothManager

    @DefaultFor("~")
    fun default(actor: Player) {
        actor.sendColorizedMessageFromMessagesFileList("koth.default")
    }

    @Subcommand("create")
    @CommandPermission("factions.command.koth.create")
    fun create(actor: Player, name: String, time: String) {
        if (manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.create.already-exists", Placeholder("{name}", name))
            return
        }

        val seconds = formatTime(time)

        if (seconds <= 0L) {
            actor.sendColorizedMessageFromMessagesFile("koth.create.secs-cant-be-zero")
            return
        }

        val koth = KoTH(name, seconds, null, null, mutableListOf())
        manager.addKoTH(koth)
    }

    @Subcommand("delete")
    @CommandPermission("factions.command.koth.delete")
    fun delete(actor: Player, name: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        manager.removeKoTH(koth)
        actor.sendColorizedMessageFromMessagesFile("koth.delete.deleted", Placeholder("{name}", name))
    }

    @Subcommand("rename")
    @CommandPermission("factions.command.koth.rename")
    fun rename(actor: Player, name: String, newName: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        if (manager.hasKoTH(name)){
            actor.sendColorizedMessageFromMessagesFile("koth.rename.already-exists", Placeholder("{newName}", newName))
            return
        }

        koth.name = newName
        actor.sendColorizedMessageFromMessagesFile("koth.rename.renamed", Placeholder("{newName}", newName), Placeholder("{name}", name))
    }

    @Subcommand("settime")
    @CommandPermission("factions.command.koth.settime")
    fun time(actor: Player, name: String, time: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        val seconds = formatTime(time)

        if (seconds == 0L){
            actor.sendColorizedMessageFromMessagesFile("koth.settime.secs-cant-be-zero")
            return
        }

        koth.time = seconds
        actor.sendColorizedMessageFromMessagesFile("koth.settime.set", Placeholder("{time}", time))
    }

    @Subcommand("listcommands")
    @CommandPermission("factions.command.koth.listcommands")
    fun listCmds(actor: Player, name: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        koth.commands.forEachIndexed { i, cmd ->
            actor.sendColorizedMessageFromMessagesFile("koth.list-commands.list-format", Placeholder("{id}", i.toString()),
                Placeholder("{cmd}", cmd))
        }
    }

    @Subcommand("addcommand")
    @CommandPermission("factions.command.koth.addcommand")
    fun addCommand(actor: Player, name: String, command: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        if (command.contains("/")){
            actor.sendColorizedMessageFromMessagesFile("koth.add.cant-have-slash")
            return
        }

        koth.commands.add(command)
        actor.sendColorizedMessageFromMessagesFile("koth.add.added", Placeholder("{cmd}", command))
    }

    @Subcommand("removecommand")
    @CommandPermission("factions.command.koth.addcommand")
    fun removeCommand(actor: Player, name: String, id: Int){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!
        val list = koth.commands

        if (list.size-1 < id){
            actor.sendColorizedMessageFromMessagesFile("koth.remove-command.id-doesnt-exist", Placeholder("{name}", name))
            return
        }

        val removed = list.removeAt(id)
        actor.sendColorizedMessageFromMessagesFile("koth.remove-command.removed", Placeholder("{name}", name), Placeholder("{cmd}", removed))
    }

    fun formatTime(string: String): Long {
        var totalSeconds = 0L
        val regex = Regex("(\\d+h)?(\\d+m)?(\\d+s)?")
        val matchResult = regex.matchEntire(string)

        if (matchResult != null) {
            val (hours, minutes, seconds) = matchResult.destructured

            if (hours.isNotEmpty()) {
                totalSeconds += hours.dropLast(1).toLong() * 3600
            }
            if (minutes.isNotEmpty()) {
                totalSeconds += minutes.dropLast(1).toLong() * 60
            }
            if (seconds.isNotEmpty()) {
                totalSeconds += seconds.dropLast(1).toLong()
            }
        }

        return totalSeconds
    }

}