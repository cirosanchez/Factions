package me.cirosanchez.factions.model.user

import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID

class UserManager : Manager {

    lateinit var plugin: Factions
    val users: HashMap<UUID, User> = hashMapOf()

    override fun load() {
        plugin = Factions.get()
        val usersFetchedFromDb = plugin.storageManager.readObjects(User::class)
        usersFetchedFromDb.forEach {
            users.put(it.uuid, it)
        }



        val runnable = Runnable {
            users.values.forEach { user ->
                plugin.storageManager.saveObject(user)
            }
        }

        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val onlineUsers = users.keys.filter {
                Bukkit.getOfflinePlayer(it).isOnline
            }

            onlineUsers.forEach {
                users.get(it)!!.playtime.plusMinutes(1L)
            }
        }, 0L, 20L*60L)

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, 100L, plugin.configurationManager.config.getLong("db-saving-rate")*20)
    }

    override fun unload() {
        users.values.forEach { user ->
            plugin.storageManager.saveObject(user)
        }
    }


    fun getUser(player: Player): User? {
        return users.get(player.uniqueId)
    }

    fun getUser(uuid: UUID): User? {
        return users.get(uuid)
    }

    @Deprecated("Use UserManager#getUser(UUID) instead.")
    fun getUser(name: String): User? {
        return users.get(Bukkit.getPlayer(name)?.uniqueId)
    }

    fun userPresent(player: Player): Boolean {
        return users.contains(player.uniqueId)
    }

    fun createNewUser(player: Player): User {
        val user = User(player.uniqueId, player.name, 0, 0, Duration.ofSeconds(0))
        users.put(player.uniqueId, user)
        return user
    }

    fun reset(){
        this.users.forEach {
            it.value.delete().get()
        }
        this.users.clear()
    }
}