package me.cirosanchez.factions.model.user


import me.cirosanchez.clib.storage.Id
import me.cirosanchez.clib.storage.MongoSerializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Duration
import java.util.*

data class User(@Id val uuid: UUID, val name: String, var kills: Long, var deaths: Long, val playtime: Duration): MongoSerializable {
    fun getPlayer(): Player? {
        return Bukkit.getPlayer(uuid)
    }
}