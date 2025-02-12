package me.cirosanchez.factions.model.spawn

import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.util.EmptyPlaceholder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

class SpawnManager : Manager {

    lateinit var plugin: Factions
    var seconds: Int = 0

    var spawn = Spawn(location = null)

    val players: HashMap<Player, Location> = hashMapOf()

    val countdown: HashMap<Player, Int> = hashMapOf()

    override fun load(){
        plugin = Factions.get()

        seconds = Factions.get().configurationManager.config.getInt("spawn.time-to-tp")

        Bukkit.getScheduler().runTaskTimer(Factions.get(), PlayerSpawnTimerCountdownRunnable(this), 0L, 20L)

        val spawnFromDb = plugin.storageManager.readObjects(Spawn::class).firstOrNull() as Spawn?

        if (spawnFromDb == null) {
            plugin.spawnManager.spawn = Spawn(location = null)
            return
        }

        spawn = spawnFromDb
    }

    override fun unload() {
        plugin.storageManager.saveObject(spawn)
    }

    fun setSpawn(loc: Location){
        this.spawn.location = loc
    }

    fun tpToSpawn(player: Player){
        if (seconds == 0){
            tp(player)
            return
        }
        players.put(player, player.location)
        countdown.put(player, seconds)
    }

    fun tp(p: Player){
        if (spawn.location == null){
            p.sendColorizedMessageFromMessagesFile("spawn.not-set", EmptyPlaceholder.E)
            return
        }
        p.teleport(spawn.location!!)
        p.sendColorizedMessageFromMessagesFile("spawn.teleported", EmptyPlaceholder.E)

    }

    fun remove(p: Player){
        this.players.remove(p)
        this.countdown.remove(p)
    }

    fun reset(){
        this.players.clear()
        this.countdown.clear()
        this.spawn = Spawn(location = null)
    }
}



class PlayerSpawnTimerCountdownRunnable(val manager: SpawnManager) : Runnable {
    override fun run() {
        for (player in manager.players.keys){
            val secondsLeft = manager.countdown.get(player)!!
            if (secondsLeft == 0){
                manager.tp(player)
                manager.remove(player)
                return
            }
            manager.countdown[player] = secondsLeft-1
            val secondsPlaceholder = Placeholder("{seconds}", (secondsLeft-1).toString())
            player.sendColorizedMessageFromMessagesFile("spawn.teleporting", secondsPlaceholder)
            return
        }
    }
}