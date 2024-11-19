package me.cirosanchez.factions.model.spawn

import com.google.gson.annotations.Expose
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.util.EmptyPlaceholder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

class SpawnManager {

    var spawn = Spawn(location = null)

    val players: HashMap<Player, Location> = hashMapOf()

    val countdown: HashMap<Player, Int> = hashMapOf()


    val seconds = Factions.get().configFile.getInt("spawn.time-to-tp")

    init {
        Bukkit.getScheduler().runTaskTimer(Factions.get(), PlayerSpawnTimerCountdownRunnable(this), 0L, 20L)
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