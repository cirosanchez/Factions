package me.cirosanchez.factions.command

import me.cirosanchez.clib.CLib
import me.cirosanchez.clib.extension.colorize
import me.cirosanchez.clib.extension.send
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.util.broadcastFromConfiguration
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.util.getTeam
import me.cirosanchez.factions.util.toPrettyStringWithoutWorld
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("faction", "f")
@CommandPermission("factions.command.team")
@Description("Commands to manage teams")
class TeamCommand {
    val plugin = Factions.get()

    @DefaultFor("~")
    fun default(actor: Player){
        val keys = CLib.get().messagesFile.getConfigurationSection("team.default")!!.getKeys(false)

        for (key in keys){
            actor.sendColorizedMessageFromMessagesFile("team.default.$key")
        }
    }

    @Subcommand("info", "i", "who", "w")
    @CommandPermission("factions.command.team.info")
    fun info(actor: Player, @Optional name: String){
        val player = Bukkit.getOfflinePlayer(name)

        if (!player.hasPlayedBefore()) {
            return
        }

        var team = player.getTeam()

        if (team == null){
            return
        }

        team = plugin.teamManager.getTeam(name)

        if (team == null){
            actor.sendColorizedMessageFromMessagesFile("team.no-team", Placeholder("{name}", name))
            return
        }

        val onlinePlayers = team.getOnlineMembers()
        val offlinePlayers = team.getOfflineMembers()

        var leader = team.getLeader()

        val onlineCoLeaders = onlinePlayers.filter { team.isCoLeader(it) }
        val offlineCoLeaders = offlinePlayers.filter { team.isCoLeader(it) }

        val onlineCaptains = onlinePlayers.filter { team.isCaptain(it) }
        val offlineCaptains = offlinePlayers.filter { team.isCaptain(it) }

        val onlineMembers = onlinePlayers.filter { team.isMember(it) }
        val offlineMembers = offlinePlayers.filter { team.isMember(it) }

        actor.send("<gray><st>--------------------------------------------------</st>")
        if (leader.isOnline){
            actor.sendMessage("Leader: <green>${leader.name}</green>".colorize())
        } else {
            actor.sendMessage("Leader: <gray>${leader.name}</gray>".colorize())
        }

        val coLeadersStringBuilder = StringBuilder()
        onlineCoLeaders.forEach {
            coLeadersStringBuilder.append(", <green>${it.name}</green>")
        }
        offlineCoLeaders.forEach {
            coLeadersStringBuilder.append(", <gray>${it.name}</gray>")
        }
        coLeadersStringBuilder.append(".")

        val coLeadersString = coLeadersStringBuilder.toString()
        actor.send(coLeadersString)

        val captainsStringBuilder = StringBuilder()
        onlineCaptains.forEach {
            captainsStringBuilder.append(", <green>${it.name}</green>")
        }
        offlineCaptains.forEach {
            captainsStringBuilder.append(", <gray>${it.name}</gray>")
        }
        captainsStringBuilder.append(".")
        var captainsString = captainsStringBuilder.toString()
        actor.send(captainsString)

        val membersStringBuilder = StringBuilder()
        onlineMembers.forEach {
            membersStringBuilder.append(", <green>${it.name}</green>")
        }

        offlineMembers.forEach {
            membersStringBuilder.append(", <gray>${it.name}</gray>")
        }

        membersStringBuilder.append(".")
        val membersString = membersStringBuilder.toString()
        actor.send(membersString)

        val points = team.points
        actor.send("Points: $points")
        val koths = team.koths
        actor.send("KoTHs: $koths")
        val kills = team.kills
        actor.send("Kills: $kills")
        val home = team.home
        if (home == null) {
            actor.send("Home: Not Set")
        } else {
            actor.send("Home: ${home.toPrettyStringWithoutWorld()}")
        }
        actor.send("<gray><st>--------------------------------------------------</st>")
    }


    @Subcommand("create")
    @CommandPermission("factions.command.team.create")
    fun create(actor: Player, name: String){
        val team = actor.getTeam()

        if (team != null){
            actor.sendColorizedMessageFromMessagesFile("team.create.already-in-team")
            return
        }

        plugin.teamManager.createTeam(name, actor)
        actor.sendColorizedMessageFromMessagesFile("team.create.created", Placeholder("{name}", name))
        broadcastFromConfiguration("team.create.broadcast", Placeholder("{name}", name), Placeholder("{player}", actor.name))
    }

    @Subcommand("disband")
    @CommandPermission("factions.command.team.disband")
    fun disband(actor: Player){
        val team = actor.getTeam()

        if (team == null){
            actor.sendColorizedMessageFromMessagesFile("team.not-in-a-team")
            return
        }

        if (!team.isLeader(actor)){
            actor.sendColorizedMessageFromMessagesFile("team.not-leader")
            return
        }

        plugin.teamManager.disbandTeam(actor)
        actor.sendColorizedMessageFromMessagesFile("team.disband.disbanded")
        broadcastFromConfiguration("team.disband.broadcast", Placeholder("{name}", team.name), Placeholder("{player}", actor.name))
    }

    @Subcommand("invite")
    @CommandPermission("factions.command.team.invite")
    fun invite(actor: Player, name: String){
        val team = actor.getTeam()

        if (team == null){
            actor.sendColorizedMessageFromMessagesFile("team.not-in-a-team")
            return
        }

        if (team.isMember(actor)){
            actor.sendColorizedMessageFromMessagesFile("team.not-captain")
            return
        }

        val player = resolvePlayerName(name)

        if (player == null) return

        if (team.isInvited(player)){
            actor.sendColorizedMessageFromMessagesFile("team.invite.already-invited", Placeholder("{name}", player.name!!))
            return
        }

        team.invite(player)
        actor.sendColorizedMessageFromMessagesFile("team.invite.invited", Placeholder("{name}", player.name!!))

        if (player.isOnline){
            player.player!!.sendColorizedMessageFromMessagesFile("team.invite.send-to-online-player", Placeholder("{name}", player.name!!), Placeholder("{team}", team.name))
        }

        for (member in team.getOnlineMembers()){
            if (member != actor){
                member.sendColorizedMessageFromMessagesFile("team.invite.send-to-team", Placeholder("{name}", actor.name), Placeholder("{team}", team.name),
                    Placeholder("{player}", player.name!!))
            }
        }
    }

    @Subcommand("disinvite")
    @CommandPermission("factions.command.team.disinvite")
    fun disinvite(actor: Player, name: String){
        val team = actor.getTeam()

        if (team == null){
            actor.sendColorizedMessageFromMessagesFile("team.not-in-a-team")
            return
        }

        if (team.isMember(actor)){
            actor.sendColorizedMessageFromMessagesFile("team.not-captain")
            return
        }

        val player = resolvePlayerName(name)

        if (player == null) return

        if (!team.isInvited(player)){
            actor.sendColorizedMessageFromMessagesFile("team.disinvite.not-invited", Placeholder("{name}", player.name!!))
            return
        }

        team.disinvite(player)
        actor.sendColorizedMessageFromMessagesFile("team.disinvite.disinvited", Placeholder("{name}", player.name!!))

        if (player.isOnline){
            player.player!!.sendColorizedMessageFromMessagesFile("team.disinvite.send-to-online-player", Placeholder("{player}", actor.name), Placeholder("{team}", team.name))
        }

        for (member in team.getOnlineMembers()){
            if (member != actor){
                member.sendColorizedMessageFromMessagesFile("team.disinvite.send-to-online-team", Placeholder("{name}", actor.name), Placeholder("{team}", team.name),
                    Placeholder("{player}", player.name!!))
            }
        }
    }

    @Subcommand("kick")
    @CommandPermission("factions.command.team.kick")
    fun kick(actor: Player, name: String){
        val team = actor.getTeam()

        if (team == null){
            actor.sendColorizedMessageFromMessagesFile("team.not-in-a-team")
            return
        }

        if (!team.isCaptain(actor)){
            actor.sendColorizedMessageFromMessagesFile("team.not-captain")
            return
        }

        val player = resolvePlayerName(name)

        if (player == null) return

        if (!team.playerIsInTeam(player)){
            actor.sendColorizedMessageFromMessagesFile("team.kick.not-in-team", Placeholder("{name}", player.name!!))
            return
        }

        team.kick(player)
        actor.sendColorizedMessageFromMessagesFile("team.kick.kicked", Placeholder("{name}", player.name!!))

        if (player.isOnline){
            player.player!!.sendColorizedMessageFromMessagesFile("team.kick.send-to-online-player", Placeholder("{team}", team.name))
        }

        for (member in team.getOnlineMembers()){
            if (member != actor){
                member.sendColorizedMessageFromMessagesFile("team.kick.send-to-online-team", Placeholder("{name}", actor.name), Placeholder("{team}", team.name),
                    Placeholder("{player}", player.name!!))
            }
        }
    }

    @Subcommand("promote")
    @CommandPermission("factions.command.team.promote")
    fun promote(actor: Player, name: String){
        val team = actor.getTeam()

        if (team == null){
            actor.sendColorizedMessageFromMessagesFile("team.not-in-a-team")
            return
        }

        if (!team.isLeader(actor)){
            actor.sendColorizedMessageFromMessagesFile("team.promote.not-leader")
            return
        }

        val player = resolvePlayerName(name)

        if (player == null || !player.hasPlayedBefore()) return

        if (!team.isMember(player)){
            actor.sendColorizedMessageFromMessagesFile("team.promote.not-a-member", Placeholder("{player}", player.name!!))
            return
        }
    }




    fun resolvePlayerName(name: String): OfflinePlayer? {
        val player = Bukkit.getOfflinePlayer(name)
        if (player.hasPlayedBefore()){
            return player
        } else {
            return null
        }
    }
}