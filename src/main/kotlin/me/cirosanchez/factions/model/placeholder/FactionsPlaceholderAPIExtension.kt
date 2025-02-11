package me.cirosanchez.factions.model.placeholder

import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.util.getTeam
import me.cirosanchez.factions.util.getUser
import me.cirosanchez.factions.util.toPrettyString
import me.cirosanchez.factions.util.toPrettyStringWithBrackets
import me.cirosanchez.factions.util.toPrettyStringWithoutWorld
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.time.Duration
import kotlin.rem


class FactionsPlaceholderAPIExtension : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "vfactions"
    }

    override fun getAuthor(): String {
        return "ktCiro"
    }

    override fun getVersion(): String {
        return "1.0"
    }

    override fun onRequest(player: OfflinePlayer, params: String): String? {
        return when (params){
            "team" -> {
                val team = player.getTeam()
                team?.name ?: "???"
            }
            "team_points" -> {
                val team = player.getTeam()
                team?.points.toString()
            }
            "team_koths" -> {
                val team = player.getTeam()
                team?.koths.toString()
            }
            "team_kills" -> {
                val team = player.getTeam()
                team?.kills.toString()
            }
            "team_leader" -> {
                val team = player.getTeam()
                team?.getLeader()?.name ?: "John Doe"
            }
            "team_isOpen" -> {
                val team = player.getTeam()
                team?.isOpen.toString()
            }
            "team_home" -> {
                val team = player.getTeam()
                team?.home?.toPrettyStringWithoutWorld() ?: "No home set."
            }
            "team_coleaders" -> {
                val team = player.getTeam()
                val sb = StringBuilder()
                sb.append("Coleaders: ")
                team?.getCoLeaders()?.forEach { sb.append(", <gray>${it.name}</gray>") }
                sb.append(".")

                sb.toString()
            }
            "team_captains" -> {
                val team = player.getTeam()
                val sb = StringBuilder()
                sb.append("Captains: ")
                team?.getCaptains()?.forEach { sb.append(", <gray>${it.name}</gray>") }
                sb.append(".")

                sb.toString()
            }
            "team_members" -> {
                val team = player.getTeam()
                val sb = StringBuilder()
                sb.append("Members: ")
                team?.getMembers()?.forEach { sb.append(", <gray>${it.name}</gray>") }
                sb.append(".")

                sb.toString()
            }
            "team_isVaultFull" -> {
                val team = player.getTeam()
                team?.vault?.isEmpty?.let { (!it).toString() }
            }

            "kills" -> {
                player.getUser().kills.toString()
            }

            "deaths" -> {
                player.getUser().deaths.toString()
            }

            "playtime" -> {
                formatDuration(player.getUser().playtime)
            }
            "uuid" -> {
                player.getUser().uuid.toString()
            }
            "name" -> {
                player.getUser().name
            }
            "activeKoth" -> {
                Factions.get().kothManager.activeKoth?.name ?: "No active KoTH."
            }
            "activeKoth_location" -> {
                Factions.get().kothManager.activeKoth?.cuboid!!.center.toPrettyStringWithBrackets()
            }
            "activeKoth_remainingTime" -> {
                formatTime(Factions.get().kothManager.remainingTime)
            }
            "activeKoth_TotalTime" -> {
                formatTime(Factions.get().kothManager.totalTimeInTicks)
            }
            "spawn" -> {
                Factions.get().spawnManager.spawn.location?.toPrettyStringWithBrackets() ?: "Not set."
            }
            "region" -> {
                if (player.isOnline){
                    val p = player as Player

                    Factions.get().regionManager.getRegion(p)?.name ?: "No region."
                } else {
                    "Player is offline."
                }
            }
            else -> {
                null
            }
        }
    }

    fun formatDuration(duration: Duration): String {
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        return String.format("%02dd%02dh%02dm%02ds", days, hours, minutes, seconds)
    }

    private fun formatTime(timeRemaining: Long): String {
        val hours = timeRemaining / 3600
        val minutes = (timeRemaining % 3600) / 60
        val seconds = timeRemaining % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}