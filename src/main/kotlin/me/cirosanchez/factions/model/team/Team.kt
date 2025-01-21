package me.cirosanchez.factions.model.team

import me.cirosanchez.clib.getPlugin
import me.cirosanchez.clib.storage.Id
import me.cirosanchez.clib.storage.MongoSerializable
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.region.Region
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID

data class Team(@Id var name: String,
                private var leader: UUID,
                private val coleaders: MutableSet<UUID>,
                private val captains: MutableSet<UUID>,
                private val members: MutableSet<UUID>,
                var points: Long,
                var koths: Long,
                var kills: Long,
                var claim: Region?,
                var home: Location?,
                var vault: Inventory,
                var invites: MutableSet<UUID>,
                var isOpen: Boolean
                ) : MongoSerializable {

    fun getLeader(): OfflinePlayer {
        return Bukkit.getOfflinePlayer(leader)
    }

    fun getCoLeaders(): Set<OfflinePlayer> {
        return coleaders.map { Bukkit.getOfflinePlayer(it) }.toSet()
    }

    fun getCaptains(): Set<OfflinePlayer> {
        return captains.map { Bukkit.getOfflinePlayer(it) }.toSet()
    }

    fun getMembers(): Set<OfflinePlayer> {
        return members.map { Bukkit.getOfflinePlayer(it) }.toSet()
    }

    fun getAbsoluteMembers(): Set<OfflinePlayer> {
        val coleaders = coleaders.map { Bukkit.getOfflinePlayer(it) }
        val captains = captains.map { Bukkit.getOfflinePlayer(it) }
        val members = members.map { Bukkit.getOfflinePlayer(it) }

        val list = mutableListOf<OfflinePlayer>()

        coleaders.forEach { list.add(it) }
        captains.forEach { list.add(it) }
        members.forEach { list.add(it) }
        list.add(Bukkit.getOfflinePlayer(leader))

        return list.toSet()
    }

    fun setLeader(player: Player){
        this.coleaders.add(leader)
        this.leader = player.uniqueId
    }

    fun isLeader(player: Player): Boolean {
        return leader == player.uniqueId
    }

    fun isCoLeader(player: Player): Boolean {
        return coleaders.contains(player.uniqueId)
    }

    fun isCaptain(player: Player): Boolean {
        return captains.contains(player.uniqueId)
    }

    fun isMember(player: Player): Boolean {
        return members.contains(player.uniqueId)
    }

    fun isLeader(player: OfflinePlayer): Boolean {
        return leader == player.uniqueId
    }

    fun isCoLeader(player: OfflinePlayer): Boolean {
        return coleaders.contains(player.uniqueId)
    }

    fun isCaptain(player: OfflinePlayer): Boolean {
        return captains.contains(player.uniqueId)
    }

    fun isMember(player: OfflinePlayer): Boolean {
        return members.contains(player.uniqueId)
    }

    fun promote(player: Player){
        if (isLeader(player) || isCoLeader(player)){
            return
        }

        if (isCaptain(player)){
            this.captains.remove(player.uniqueId)
            this.coleaders.add(player.uniqueId)
            return
        }

        if (isMember(player)){
            this.members.remove(player.uniqueId)
            this.captains.remove(player.uniqueId)
            return
        }
    }

    fun promote(player: OfflinePlayer){
        if (isLeader(player) || isCoLeader(player)){
            return
        }

        if (isCaptain(player)){
            this.captains.remove(player.uniqueId)
            this.coleaders.add(player.uniqueId)
            return
        }

        if (isMember(player)){
            this.members.remove(player.uniqueId)
            this.captains.add(player.uniqueId)
            return
        }
    }

    fun demote(player: Player){
        if (isMember(player) || isLeader(player)){
            return
        }

        if (isCoLeader(player)){
            this.coleaders.remove(player.uniqueId)
            this.captains.add(player.uniqueId)
            return
        }

        if (isCaptain(player)){
            this.captains.remove(player.uniqueId)
            this.members.add(player.uniqueId)
            return
        }
    }

    fun demote(player: OfflinePlayer){
        if (isMember(player) || isLeader(player)){
            return
        }

        if (isCoLeader(player)){
            this.coleaders.remove(player.uniqueId)
            this.captains.add(player.uniqueId)
            return
        }

        if (isCaptain(player)){
            this.captains.remove(player.uniqueId)
            this.members.add(player.uniqueId)
            return
        }
    }

    fun openVault(player: Player) {
        player.openInventory(vault)
    }

    fun playerIsInTeam(player: Player): Boolean {
        return isLeader(player) || isMember(player) || isCaptain(player) || isCoLeader(player)
    }

    fun playerIsInTeam(player: OfflinePlayer): Boolean {
        return isLeader(player) || isMember(player) || isCaptain(player) || isCoLeader(player)
    }

    fun getOnlineMembers(): Set<Player> {
        return getAbsoluteMembers().filter { it.isOnline }.map {  it.player!!  }.toSet()
    }

    fun getOfflineMembers(): Set<OfflinePlayer> {
        return getAbsoluteMembers().filter { !it.isOnline }.toSet()
    }

    fun isInvited(offlinePlayer: OfflinePlayer): Boolean {
        return invites.contains(offlinePlayer.uniqueId)
    }

    fun invite(offlinePlayer: OfflinePlayer) {
        invites.add(offlinePlayer.uniqueId)
    }

    fun disinvite(offlinePlayer: OfflinePlayer) {
        invites.remove(offlinePlayer.uniqueId)
    }

    fun kick(offlinePlayer: OfflinePlayer){
        if (isLeader(offlinePlayer)){
            return
        }

        if (isCoLeader(offlinePlayer)){
            coleaders.remove(offlinePlayer.uniqueId)
        }

        if (isCaptain(offlinePlayer)){
            captains.remove(offlinePlayer.uniqueId)
        }

        if (isMember(offlinePlayer)){
            members.remove(offlinePlayer.uniqueId)
        }
    }

    fun isFull(): Boolean {
        return Factions.get().configurationManager.config.getInt("team.size") >= getAbsoluteMembers().size
    }


    fun join(player: Player){
        this.members.add(player.uniqueId)
    }
}