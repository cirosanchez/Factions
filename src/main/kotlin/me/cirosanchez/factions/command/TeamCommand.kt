package me.cirosanchez.factions.command

import me.cirosanchez.clib.CLib
import me.cirosanchez.clib.extension.colorize
import me.cirosanchez.clib.extension.send
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.util.broadcastFromConfiguration
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.team.Team
import me.cirosanchez.factions.util.getTeam
import me.cirosanchez.factions.util.toPrettyStringWithoutWorld
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
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
    val regex = "^[a-zA-Z0-9]{3,16}$".toRegex()



    @DefaultFor("~")
    fun default(actor: Player) {
        actor.sendColorizedMessageFromMessagesFileList("team.default")
    }



    @Subcommand("info", "i", "who", "w")
    @CommandPermission("factions.command.team.info")
    fun info(actor: Player, @Optional @Default("me") name: String){


        var team: Team?

        if (name == "me"){
            team = actor.getTeam()

            if (team == null){
                println("asd")
                actor.sendColorizedMessageFromMessagesFile("team.not-in-a-team")
                return
            }

            println(team.name)

        } else if (plugin.teamManager.teams.contains(name)){
            team = plugin.teamManager.getTeam(name)
            if (team == null){

                println("asd2")
                actor.sendColorizedMessageFromMessagesFile("team.info.no-team",Placeholder("{name}", name) )
                return
            }
        } else {
            val player = Bukkit.getOfflinePlayer(name)

            if (!player.hasPlayedBefore()){
                println("asd3")
                actor.sendColorizedMessageFromMessagesFile("player-doesnt-exist", Placeholder("{name}", name))
                return
            }

            team = plugin.teamManager.getTeam(player)

            if (team == null){
                println("asd4")
                actor.sendColorizedMessageFromMessagesFile("team.info.no-team-with-player",Placeholder("{name}", name) )
                return
            }
        }


        if (team == null){
            println("asd5")
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
        actor.send("<blue>${team.name}</blue>")
        if (leader.isOnline){
            actor.sendMessage("Leader: <green>${leader.name}</green>".colorize())
        } else {
            actor.sendMessage("Leader: <gray>${leader.name}</gray>".colorize())
        }

        val coLeadersStringBuilder = StringBuilder()
        coLeadersStringBuilder.append("Coleaders: ")
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
        captainsStringBuilder.append("Captains: ")

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
        membersStringBuilder.append("Members: ")
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
    fun create(actor: Player, name: String) {
        val team = actor.getTeam()

        if (team != null) {
            actor.sendColorizedMessageFromMessagesFile("team.create.already-in-team")
            return
        }

        if (!isValidName(name)){
            actor.sendColorizedMessageFromMessagesFile("team.name-not-valid")
            return
        }

        if (plugin.teamManager.teams.contains(name)){
            actor.sendColorizedMessageFromMessagesFile("team.create.already-exists")
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

        if (team.isMember(actor)){
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

        if (!team.playerIsInTeam(player)){
            actor.sendColorizedMessageFromMessagesFile("team.promote.not-part-of-the-team", Placeholder("{player}", player.name!!))
            return
        }

        if (team.isCoLeader(player)){
            actor.sendColorizedMessageFromMessagesFile("team.promote.cant-promote", Placeholder("{player}", player.name!!))

        }

        team.promote(player)

        if (player.isOnline) (player as Player).sendColorizedMessageFromMessagesFile("team.promote.promoted-to-player")

        actor.sendColorizedMessageFromMessagesFile("team.promote.promoted")
    }

    @Subcommand("demote")
    @CommandPermission("factions.command.team.demote")
    fun demote(actor: Player, name: String){
        val team = actor.getTeam()

        if (team == null){
            actor.sendColorizedMessageFromMessagesFile("team.not-in-a-team")
            return
        }

        if (!team.isLeader(actor)){
            actor.sendColorizedMessageFromMessagesFile("team.not-leader")
            return
        }

        val player = resolvePlayerName(name)

        if (player == null || !player.hasPlayedBefore()) return

        if (!team.playerIsInTeam(player)){
            actor.sendColorizedMessageFromMessagesFile("team.promote.not-part-of-the-team", Placeholder("{player}", player.name!!))
            return
        }

        if (team.isMember(player)){
            actor.sendColorizedMessageFromMessagesFile("team.demote.cant-demote", Placeholder("{player}", player.name!!))
            return
        }

        team.demote(player)
        if (player.isOnline) (player as Player).sendColorizedMessageFromMessagesFileList("team.demote.demoted-to-player")

        actor.sendColorizedMessageFromMessagesFile("team.demote.demoted", Placeholder("{player}", player.name!!))
    }

    // works perfectly-
    @Subcommand("vault")
    @CommandPermission("factions.command.team.vault")
    fun vault(actor: Player){
        val team = actor.getTeam()

        if (team == null){
            actor.sendColorizedMessageFromMessagesFile("team.not-in-a-team")
            return
        }

        if (team.isMember(actor)){
            actor.sendColorizedMessageFromMessagesFile("team.not-captain")
            return
        }

        actor.openInventory(team.vault)
        actor.sendColorizedMessageFromMessagesFile("team.vault.open")
    }

    @Subcommand("open")
    @CommandPermission("factions.command.team.open")
    fun open(actor: Player){
        val team = actor.getTeam()

        if (team == null){
            actor.sendColorizedMessageFromMessagesFile("team.not-in-a-team")
            return
        }

        team.isOpen = !team.isOpen

        if (team.isOpen){
            broadcastFromConfiguration("team.open.open", Placeholder("{team}", team.name))
        } else {
            broadcastFromConfiguration("team.open.close", Placeholder("{team}", team.name))
        }
    }


    @Subcommand("join")
    @CommandPermission("factions.command.team.join")
    fun join(actor: Player, name: String){
        var team: Team? = null

        team = plugin.teamManager.getTeam(name)

        if (team == null){
            val player = resolvePlayerName(name)

            if (player == null){
                actor.sendColorizedMessageFromMessagesFile("team.join.no-player-or-team", Placeholder("{name}", name))
                return
            }

            team = player.getTeam()
            if (team == null){
                actor.sendColorizedMessageFromMessagesFile("team.join.player-has-no-team", Placeholder("{name}", name))
                return
            }
        }

        if (team.isInvited(actor)){
            if (team.isFull()){
                actor.sendColorizedMessageFromMessagesFile("team.join.team-full", Placeholder("{team}", team.name))
                return
            }

            team.join(actor)
            team.disinvite(actor)
            actor.sendColorizedMessageFromMessagesFile("team.join.joined", Placeholder("{team}", team.name))
        } else {
            actor.sendColorizedMessageFromMessagesFile("team.join.not-invited", Placeholder("{team}", team.name))
            return
        }
    }

    @Subcommand("leave")
    @CommandPermission("factions.command.team.leave")
    fun leave(actor: Player){
        val team = actor.getTeam()

        if (team == null){
            actor.sendColorizedMessageFromMessagesFile("team.not-in-a-team")
            return
        }

        team.kick(actor)
        actor.sendColorizedMessageFromMessagesFile("team.leave.left", Placeholder("{team}", team.name))
    }

    @Subcommand("rename")
    @CommandPermission("factions.command.team.rename")
    fun rename(actor: Player, name: String) {
        val team = actor.getTeam()

        if (team == null) {
            actor.sendColorizedMessageFromMessagesFile("team.not-in-a-team")
            return
        }

        if (!team.isLeader(actor)) {
            actor.sendColorizedMessageFromMessagesFile("team.not-leader")
            return
        }

        if (team.name == name) {
            actor.sendColorizedMessageFromMessagesFile("team.rename.same-name")
            return
        }

        if (!isValidName(name)){
            actor.sendColorizedMessageFromMessagesFile("team.name-not-valid")
            return
        }

        team.name = name
        actor.sendColorizedMessageFromMessagesFile("team.rename.set", Placeholder("{name}", team.name))
    }

    @Subcommand("list")
    @CommandPermission("factions.command.team.rename")
    fun list(actor: Player){

    }




    fun resolvePlayerName(name: String): OfflinePlayer? {
        val player = Bukkit.getOfflinePlayer(name)
        if (player.hasPlayedBefore()){
            return player
        } else {
            return null
        }
    }


    fun isValidName(input: String): Boolean {
        return regex.matches(input)
    }

}