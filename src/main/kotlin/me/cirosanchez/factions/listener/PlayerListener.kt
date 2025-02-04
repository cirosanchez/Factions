package me.cirosanchez.factions.listener

import me.cirosanchez.clib.cuboid.Cuboid
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.mine.Mine
import me.cirosanchez.factions.model.region.Region
import me.cirosanchez.factions.model.region.RegionType
import me.cirosanchez.factions.model.region.util.PlayerRegionChangeEvent
import me.cirosanchez.factions.util.EmptyPlaceholder
import me.cirosanchez.factions.util.WandSession
import me.cirosanchez.factions.util.WandType
import me.cirosanchez.factions.util.getRegions
import me.cirosanchez.factions.util.toPrettyStringWithoutWorld
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Type
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*
import java.util.function.Consumer

class PlayerListener : Listener {

    val userManager = Factions.get().userManager
    val regionManager = Factions.get().regionManager
    val spawnManager = Factions.get().spawnManager
    val mineManager = Factions.get().mineManager
    val eventManager = Factions.get().eventManager

    var hasSomeoneJoined = false

    val spawnRadius = Factions.get().configurationManager.config.getInt("spawn.radius")


    companion object {
        val wandPlayers: HashMap<Player, WandSession> = hashMapOf()
        val cuboidPlayers: HashMap<Player, Mine> = hashMapOf()
        val claimPlayers: HashMap<Player, Mine> = hashMapOf()
    }


    @EventHandler
    fun hasSomeoneJoined(preLoginEvent: PlayerJoinEvent) {

        println("zzz")
        if (hasSomeoneJoined) return

        println("KKKK")
        Factions.get().worldManager.deleteBlockDisplaysInAllWorlds()
        hasSomeoneJoined = true
    }


    @EventHandler
    fun spawnMove(e: PlayerMoveEvent) {
        val p = e.player
        if (!spawnManager.players.contains(p)) return

        val loc = spawnManager.players.get(p)!!

        if (e.to.distance(loc) > spawnRadius) {
            spawnManager.players.remove(p)
            spawnManager.countdown.remove(p)
            p.sendColorizedMessageFromMessagesFile("spawn.cancelled", EmptyPlaceholder.E)
        }
    }

    @EventHandler
    fun playerDamage(e: EntityDamageEvent) {
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
    fun wandInteract(e: PlayerInteractEvent) {
        val p = e.player

        if (!wandPlayers.contains(p)) return


        val session = wandPlayers.get(p)!!

        val item = p.itemInHand
        val meta = item.itemMeta
        val pdc = meta.persistentDataContainer

        if (!pdc.has(NamespacedKey(Factions.get(), "WAND"), PersistentDataType.STRING) || !pdc.has(
                NamespacedKey(
                    Factions.get(),
                    "WAND"
                ), PersistentDataType.STRING
            )
        ) return
        e.isCancelled = true

        val action = e.action
        val block = e.clickedBlock
        when (session.type) {
            WandType.SPAWN -> {

                when (action) {
                    Action.LEFT_CLICK_BLOCK -> {
                        session.pos1 = block!!.location
                        p.sendColorizedMessageFromMessagesFile(
                            "spawn.pos", Placeholder("{location}", block.location.toPrettyStringWithoutWorld()),
                            Placeholder("{number}", "1")
                        )
                    }

                    Action.RIGHT_CLICK_BLOCK -> {
                        session.pos2 = block!!.location
                        p.sendColorizedMessageFromMessagesFile(
                            "spawn.pos", Placeholder("{location}", block.location.toPrettyStringWithoutWorld()),
                            Placeholder("{number}", "2")
                        )
                    }

                    Action.LEFT_CLICK_AIR -> {
                        if (!session.isCompleted()) {
                            p.sendColorizedMessageFromMessagesFile("spawn.positions-missing", EmptyPlaceholder.E)
                            return
                        }

                        if (!regionManager.getRegions(RegionType.SPAWN).isEmpty()) {
                            regionManager.regions.get(session.pos1!!.world)!!.clear()
                            return
                        }

                        val cuboid = Cuboid(session.pos1!!.add(0.0, -300.0, 0.0), session.pos2!!.add(0.0, 300.0, 0.0))
                        val region = Region(
                            Factions.get().configurationManager.config.getString("spawn.name")
                                ?: "<green>Spawn</green>", cuboid, false, RegionType.SPAWN, UUID.randomUUID()
                        )

                        regionManager.addRegion(region)
                        p.sendColorizedMessageFromMessagesFile(
                            "spawn.claimed", Placeholder("{pos1}", session.pos1!!.toPrettyStringWithoutWorld()),
                            Placeholder("{pos2}", session.pos2!!.toPrettyStringWithoutWorld())
                        )
                        p.inventory.itemInMainHand.amount = 0
                        wandPlayers.remove(p)
                    }

                    else -> {
                        return
                    }
                }

            }

            WandType.TEAM -> TODO()
            WandType.MINE_CLAIM -> {
                when (action) {
                    Action.LEFT_CLICK_BLOCK -> {
                        session.pos1 = block!!.location
                        p.sendColorizedMessageFromMessagesFile(
                            "mine.pos", Placeholder("{location}", block.location.toPrettyStringWithoutWorld()),
                            Placeholder("{number}", "1")
                        )
                    }

                    Action.RIGHT_CLICK_BLOCK -> {
                        session.pos2 = block!!.location
                        p.sendColorizedMessageFromMessagesFile(
                            "mine.pos", Placeholder("{location}", block.location.toPrettyStringWithoutWorld()),
                            Placeholder("{number}", "2")
                        )
                    }

                    Action.LEFT_CLICK_AIR -> {
                        if (!session.isCompleted()) {
                            p.sendColorizedMessageFromMessagesFile("mine.positions-missing", EmptyPlaceholder.E)
                            return
                        }

                        val mine = claimPlayers.get(p) ?: return




                        val cuboid = Cuboid(session.pos1!!.add(0.0, -300.0, 0.0), session.pos2!!.add(0.0, 300.0, 0.0))
                        val region = Region(mine.displayName, cuboid, true, RegionType.MINE, UUID.randomUUID())

                        mine.claim = region

                        regionManager.addRegion(region)
                        p.sendColorizedMessageFromMessagesFile(
                            "mine.claimed",
                            Placeholder("{pos1}", session.pos1!!.toPrettyStringWithoutWorld()),
                            Placeholder("{pos2}", session.pos2!!.toPrettyStringWithoutWorld()),
                            Placeholder("{mine}", mine.name)
                        )
                        p.inventory.itemInMainHand.amount = 0
                        wandPlayers.remove(p)
                        claimPlayers.remove(p)
                    }

                    else -> {
                        return
                    }
                }
            }

            WandType.MINE_CUBOID -> {
                when (action) {
                    Action.LEFT_CLICK_BLOCK -> {
                        session.pos1 = block!!.location
                        p.sendColorizedMessageFromMessagesFile(
                            "mine.pos", Placeholder("{location}", block.location.toPrettyStringWithoutWorld()),
                            Placeholder("{number}", "1")
                        )
                    }

                    Action.RIGHT_CLICK_BLOCK -> {
                        session.pos2 = block!!.location
                        p.sendColorizedMessageFromMessagesFile(
                            "mine.pos", Placeholder("{location}", block.location.toPrettyStringWithoutWorld()),
                            Placeholder("{number}", "2"),
                        )
                    }

                    Action.LEFT_CLICK_AIR -> {
                        if (!session.isCompleted()) {
                            p.sendColorizedMessageFromMessagesFile("mine.positions-missing", EmptyPlaceholder.E)
                            return
                        }

                        val mine = cuboidPlayers.get(p) ?: return


                        val cuboid = Cuboid(session.pos1!!, session.pos2!!)
                        mine.cuboid = cuboid


                        p.sendColorizedMessageFromMessagesFile(
                            "mine.claimed",
                            Placeholder("{pos1}", session.pos1!!.toPrettyStringWithoutWorld()),
                            Placeholder("{pos2}", session.pos2!!.toPrettyStringWithoutWorld()),
                            Placeholder("{mine}", mine.name)
                        )

                        p.inventory.itemInMainHand.amount = 0

                        wandPlayers.remove(p)
                        cuboidPlayers.remove(p)
                    }

                    else -> {
                        return
                    }
                }
            }
        }
    }


    /*
    REGION IN, OUT RELATED
     */
    @EventHandler
    fun region(event: PlayerRegionChangeEvent) {
        val from = event.from
        val to = event.to
        val player = event.player

        player.sendColorizedMessageFromMessagesFile(
            "region-change", Placeholder("{to-region}", to.name),
            Placeholder("{from-region}", from.name)
        )
    }

    /*
    User related
     */
    @EventHandler
    fun playerJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (!userManager.userPresent(player)) {
            userManager.createNewUser(player)
        }
    }

    @EventHandler
    fun mineBlockBreakEvent(event: BlockBreakEvent) {
        val location = event.block.location
        val player = event.player


        val mine = mineManager.getMine(location) ?: return


        if (!player.hasPermission(mine.permission)) {
            player.sendColorizedMessageFromMessagesFile("mine.no-permission-to-break")
            event.isCancelled = true
            return
        }

        if (player.gameMode == GameMode.CREATIVE) return

        player.giveExp(event.expToDrop)
        event.expToDrop = 0
        event.isDropItems = false
        player.inventory.addItem(mine.reward)
    }

    @EventHandler
    fun playerRegionBreakingBlock(event: BlockBreakEvent){
        val location = event.block.location
        val region = regionManager.getAbsoluteRegion(location) ?: return

        event.isCancelled = true
        event.player.sendColorizedMessageFromMessagesFile("cant-break")
    }

    @EventHandler
    fun playerDamageInMine(event: EntityDamageByEntityEvent) {
        val location = event.entity.location
        val mine = mineManager.getMineC(location) ?: return
        println("1")

        if (mine.pvp) return
        println("2")

        if (event.entity !is Player) return

        println("3")

        event.isCancelled = true

        val damager = event.damager

        if (damager !is Player) return

        println("4")

        damager.sendColorizedMessageFromMessagesFile("mine.no-pvp-mine")
    }

    @EventHandler
    fun playerMoveInEvent(e: PlayerRegionChangeEvent){
        val to = e.to
        val event = eventManager.getEvent(to) ?: return

        if (event != eventManager.activeEvent) return

        if (eventManager.theresSomeoneCapping) return

        eventManager.playerCapping = e.player
    }

    @EventHandler
    fun playerMoveOutEvent(e: PlayerRegionChangeEvent){
        val from = e.from
        val event = eventManager.getEvent(from) ?: return

        if (event != eventManager.activeEvent) return

        if (eventManager.playerCapping != e.player) return

        eventManager.playerCapping = null
    }
}