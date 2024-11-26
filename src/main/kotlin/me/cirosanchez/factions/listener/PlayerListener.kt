package me.cirosanchez.factions.listener

import me.cirosanchez.clib.cuboid.Cuboid
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.region.Region
import me.cirosanchez.factions.model.region.RegionType
import me.cirosanchez.factions.model.region.util.PlayerRegionChangeEvent
import me.cirosanchez.factions.util.EmptyPlaceholder
import me.cirosanchez.factions.util.WandSession
import me.cirosanchez.factions.util.WandType
import me.cirosanchez.factions.util.toPrettyStringWithoutWorld
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.collections.HashMap

class PlayerListener : Listener {

    val userManager = Factions.get().userManager
    val regionManager = Factions.get().regionManager
    val spawnManager = Factions.get().spawnManager

    val spawnRadius = Factions.get().configurationManager.config.getInt("spawn.radius")




    companion object {
        val wandPlayers: HashMap<Player, WandSession> = hashMapOf()

    }

    @EventHandler
    fun changeRegion(e: PlayerMoveEvent){
        val fromRegion = regionManager.getRegion(e.from)
        val toRegion = regionManager.getRegions(e.to)
    }

    @EventHandler
    fun spawnMove(e: PlayerMoveEvent){
        val p = e.player
        if (!spawnManager.players.contains(p)) return

        val loc = spawnManager.players.get(p)!!

        if (e.to.distance(loc) > spawnRadius){
            spawnManager.players.remove(p)
            spawnManager.countdown.remove(p)
            p.sendColorizedMessageFromMessagesFile("spawn.cancelled", EmptyPlaceholder.E)
        }
    }

    @EventHandler
    fun playerDamage(e: EntityDamageEvent){
        val entity = e.entity

        if (entity !is Player) return

        val p = entity

        if (!spawnManager.players.contains(p)) return

        spawnManager.players.remove(p)
        spawnManager.countdown.remove(p)
        p.sendColorizedMessageFromMessagesFile("spawn.damage", EmptyPlaceholder.E)
    }

    /*
    WAND RELATED
     */
    @EventHandler
    fun wandInteract(e: PlayerInteractEvent){
        val p = e.player

        if (!wandPlayers.contains(p)) return


        val session = wandPlayers.get(p)!!

        val item = p.itemInHand
        val meta = item.itemMeta
        val pdc = meta.persistentDataContainer

        if (!pdc.has(NamespacedKey(Factions.get(), "SPAWN-WAND"), PersistentDataType.STRING)) return
        e.isCancelled = true

        val action = e.action
        val block = e.clickedBlock

        when (session.type) {
            WandType.SPAWN -> {

                when (action){
                    Action.LEFT_CLICK_BLOCK -> {
                        session.pos1 = block!!.location
                        p.sendColorizedMessageFromMessagesFile("spawn.pos", Placeholder("{location}", block.location.toPrettyStringWithoutWorld()),
                            Placeholder("{number}","1"))
                    }
                    Action.RIGHT_CLICK_BLOCK -> {
                        session.pos2 = block!!.location
                        p.sendColorizedMessageFromMessagesFile("spawn.pos", Placeholder("{location}", block.location.toPrettyStringWithoutWorld()),
                            Placeholder("{number}","2"))
                    }
                    Action.LEFT_CLICK_AIR -> {
                        if (!session.isCompleted()){
                            p.sendColorizedMessageFromMessagesFile("spawn.positions-missing", EmptyPlaceholder.E)
                            return
                        }
                        val cuboid = Cuboid(session.pos1, session.pos2)
                        val region = Region(Factions.get().configurationManager.config.getString("spawn.name") ?: "<green>Spawn</green>", cuboid, false, RegionType.SPAWN, UUID.randomUUID())

                        regionManager.addRegion(region)
                        p.sendColorizedMessageFromMessagesFile("spawn.claimed", Placeholder("{pos1}", session.pos1!!.toPrettyStringWithoutWorld()),
                            Placeholder("{pos2}", session.pos2!!.toPrettyStringWithoutWorld()))
                        p.itemInHand.type = Material.AIR
                        wandPlayers.remove(p)
                    }

                    else -> {return}
                }

            }
            WandType.TEAM -> TODO()
        }
    }


    /*
    REGION IN, OUT RELATED
     */
    @EventHandler
    fun region(event: PlayerRegionChangeEvent){
        val from = event.from
        val to = event.to
        val player = event.player

        player.sendMessage("${from.name} >>> ${to.name}")
    }

    /*
    User related
     */
    @EventHandler
    fun playerJoin(event: PlayerJoinEvent){
        val player = event.player
        if (!userManager.userPresent(player)){
            userManager.createNewUser(player)
        }
    }
}