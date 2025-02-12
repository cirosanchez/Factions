package me.cirosanchez.factions.model.team

import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.util.getTeam
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

class TeamManager : Manager {

    val teams: HashMap<String, Team> = hashMapOf()

    override fun load() {
        val plugin = Factions.get()
        val dbTeams = plugin.storageManager.readObjects<Team>(Team::class)
        for (team in dbTeams){
            teams.put(team.name, team)
        }
        val runnable = Runnable {
            teams.values.forEach { user ->
                plugin.storageManager.saveObject(user)
            }
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, 100L, plugin.configurationManager.config.getLong("db-saving-rate")*20)

    }

    override fun unload() {
        val plugin = Factions.get()
        for (team in teams.values){
            plugin.storageManager.saveObject(team)
        }
    }

    fun getTeam(player: OfflinePlayer): Team? {
        for (team in teams.values){
            if (team.playerIsInTeam(player)) return team
        }
        return null
    }

    fun getTeam(player: Player): Team? {
        for (team in teams.values){
            if (team.playerIsInTeam(player)) return team
        }
        return null
    }

    fun getTeam(name: String): Team? {
        return teams[name]
    }


    fun createTeam(name: String, leader: Player){
        if (teams.keys.contains(name)){
            return
        }

        if (leader.getTeam() != null) {
            return
        }

        val team = Team(name, leader.uniqueId, mutableSetOf(), mutableSetOf(), mutableSetOf(), 0, 0, 0, null, null, Bukkit.createInventory(null,
            InventoryType.CHEST),
            mutableSetOf(),
            false
        )

        this.teams.put(name, team)
    }

    fun disbandTeam(leader: Player){
        val team = leader.getTeam()

        if (team == null) return

        val name = team.name
        teams.remove(name)
    }

    fun renameTeam(name: String){
        val team = teams.get(name)

        if (team == null) return

        teams.remove(name)
        team.name = name
        teams.put(name, team)
    }

    fun reset(){
        teams.forEach { name, team ->
            team.delete().get()
        }
        teams.clear()
    }
}