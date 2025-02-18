package me.cirosanchez.factions.listener

import io.papermc.paper.event.player.AsyncChatEvent
import me.cirosanchez.clib.cuboid.Cuboid
import me.cirosanchez.clib.extension.colorize
import me.cirosanchez.clib.extension.placeholders
import me.cirosanchez.clib.extension.send
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.chat.ChatType
import me.cirosanchez.factions.model.koth.KoTH
import me.cirosanchez.factions.model.mine.Mine
import me.cirosanchez.factions.model.region.Region
import me.cirosanchez.factions.model.region.RegionType
import me.cirosanchez.factions.model.region.util.PlayerRegionChangeEvent
import me.cirosanchez.factions.util.EmptyPlaceholder
import me.cirosanchez.factions.util.WandSession
import me.cirosanchez.factions.util.WandType
import me.cirosanchez.factions.util.getRegions
import me.cirosanchez.factions.util.getTeam
import me.cirosanchez.factions.util.getUser
import me.cirosanchez.factions.util.toPrettyStringWithoutWorld
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Type
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.damage.DeathMessageType.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.function.Consumer
import kotlin.random.Random

class PlayerListener : Listener {

    val plugin = Factions.get()
    val userManager = plugin.userManager
    val regionManager = plugin.regionManager
    val spawnManager = plugin.spawnManager
    val mineManager = plugin.mineManager
    val kothManager = plugin.kothManager
    val chatManager = plugin.chatManager

    var hasSomeoneJoined = false

    val spawnRadius = plugin.configurationManager.config.getInt("spawn.radius")

    companion object {
        val wandPlayers: HashMap<Player, WandSession> = hashMapOf()
        val cuboidPlayers: HashMap<Player, Mine> = hashMapOf()
        val claimPlayers: HashMap<Player, Mine> = hashMapOf()
        val playersInsideCapzone: MutableList<Player> = mutableListOf()
        val kothCapzonePlayer: HashMap<Player, KoTH> = hashMapOf()
        val kothClaimPlayer: HashMap<Player, KoTH> = hashMapOf()
    }


    @EventHandler
    fun hasSomeoneJoined(preLoginEvent: PlayerJoinEvent) {

        println("zzz")
        if (hasSomeoneJoined) return

        println("KKKK")
        plugin.worldManager.deleteBlockDisplaysInAllWorlds()
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

        if (!pdc.has(NamespacedKey(plugin, "WAND"), PersistentDataType.STRING) || !pdc.has(
                NamespacedKey(
                    plugin,
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
                            plugin.configurationManager.config.getString("spawn.name")
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


                        if (mine.claim != null) regionManager.removeRegion(mine.claim!!)

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

            WandType.KOTH_CLAIM -> {
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

                        val koth = kothClaimPlayer.get(p) ?: return

                        var format = plugin.configurationManager.config.getString("koth.display-name") ?: "<aqua>{name}</aqua> <gold>KoTH</gold>"

                        if (koth.region != null) regionManager.removeRegion(koth.region!!)

                        val cuboid = Cuboid(session.pos1!!.add(0.0, -300.0, 0.0), session.pos2!!.add(0.0, 300.0, 0.0))
                        val region = Region(format.placeholders(Placeholder("{name}", koth.name)), cuboid, true, RegionType.MINE, UUID.randomUUID())

                        koth.region = region

                        regionManager.addRegion(region)
                        p.sendColorizedMessageFromMessagesFile(
                            "koth.claimed",
                            Placeholder("{pos1}", session.pos1!!.toPrettyStringWithoutWorld()),
                            Placeholder("{pos2}", session.pos2!!.toPrettyStringWithoutWorld()),
                            Placeholder("{koth}", koth.name)
                        )
                        p.inventory.itemInMainHand.amount = 0
                        wandPlayers.remove(p)
                        kothClaimPlayer.remove(p)
                    }

                    else -> {
                        return
                    }
                }
            }
            WandType.KOTH_CAPZONE -> {
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

                        val koth = kothCapzonePlayer.get(p) ?: return


                        if (koth.region == null){
                            p.sendColorizedMessageFromMessagesFile("koth.first-setup-claim")
                            return
                        }

                        val cuboid = Cuboid(session.pos1!!.add(0.0, -300.0, 0.0), session.pos2!!.add(0.0, 300.0, 0.0))


                        koth.cuboid = cuboid

                        p.sendColorizedMessageFromMessagesFile(
                            "koth.cuboid-claimed",
                            Placeholder("{pos1}", session.pos1!!.toPrettyStringWithoutWorld()),
                            Placeholder("{pos2}", session.pos2!!.toPrettyStringWithoutWorld()),
                            Placeholder("{koth}", koth.name)
                        )
                        p.inventory.itemInMainHand.amount = 0
                        wandPlayers.remove(p)
                        kothCapzonePlayer.remove(p)
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

        if (event.player.hasPermission("factions.region.breakBlock")) return

        event.isCancelled = true
        event.player.sendColorizedMessageFromMessagesFile("cant-break")
    }

    @EventHandler
    fun playerRegionPlacingBlock(event: BlockPlaceEvent){
        val location = event.block.location
        val region = regionManager.getAbsoluteRegion(location) ?: return

        if (event.player.hasPermission("factions.region.breakBlock")) return

        event.isCancelled = true
        event.player.sendColorizedMessageFromMessagesFile("cant-break")
    }

    @EventHandler
    fun playerDamageInMine(event: EntityDamageByEntityEvent) {
        val location = event.entity.location
        val mine = mineManager.getMineC(location) ?: return


        if (mine.pvp) return


        if (event.entity !is Player) return


        event.isCancelled = true

        val damager = event.damager

        if (damager !is Player) return


        damager.sendColorizedMessageFromMessagesFile("mine.no-pvp-mine")
    }

    @EventHandler
    fun moveInKothCapzone(event: PlayerMoveEvent){
        val to = event.to

        val koth = kothManager.activeKoth ?: return

        val cuboid = koth.cuboid ?: return

        if (cuboid.contains(to)){
            playersInsideCapzone.add(event.player)
        }
    }

    @EventHandler
    fun moveOutKothCapzone(event: PlayerMoveEvent){
        val from = event.from

        val koth = kothManager.activeKoth ?: return

        val cuboid = koth.cuboid ?: return

        if (!koth.region!!.cuboid!!.contains(from)) return

        if (!cuboid.contains(from)){
            if (playersInsideCapzone.first() == event.player){
                kothManager.remainingTime = kothManager.totalTimeInTicks
                kothManager.counter.resetTime()
            }

            playersInsideCapzone.remove(event.player)
        }


    }

    @EventHandler
    fun userKillAndDeath(event: PlayerDeathEvent){
        val player = event.player

        player.getUser().deaths++

        val team = player.getTeam()
        if (team != null){
            val points = plugin.configurationManager.config.getInt("points.death")
            team.points -= points
        }

        val cause = event.damageSource.causingEntity

        if (cause is Player){
            cause.getUser().kills++

            val team = cause.getTeam()

            if (team != null){
                team.kills++
                val points = plugin.configurationManager.config.getInt("points.kill")
                team.points += points
            }
        }
    }

    @EventHandler
    fun chat(event: AsyncChatEvent) {
        val msg = chatManager.getFormattedMessage(event.player, MiniMessage.miniMessage().serialize(event.message()))
        event.isCancelled = true

        val permission = chatManager.getChatPermission()

        if (!event.player.hasPermission(permission)){
            val status = chatManager.getChatStatusString()
            event.player.sendColorizedMessageFromMessagesFile("chat.no-permission", Placeholder("{status}", status))
            return
        }

        val type = msg.first

        when (type){
            ChatType.PUBLIC -> {
                Bukkit.broadcast(msg.second.colorize())
            }
            ChatType.STAFF -> {
                Bukkit.getOnlinePlayers().filter { it.hasPermission("factions.staff") }.forEach {
                    it.sendMessage(msg.second.colorize())
                }
            }
            ChatType.ADMIN -> {
                Bukkit.getOnlinePlayers().filter { it.hasPermission("factions.admin") }.forEach {
                    it.sendMessage(msg.second.colorize())
                }
            }
        }
    }

    @EventHandler
    fun chatJoinEvent(event: PlayerJoinEvent){
        chatManager.addPlayer(event.player)
        event.player.sendColorizedMessageFromMessagesFile("join-message")
        event.joinMessage = ""
    }

    @EventHandler
    fun spawnRandomTp(event: PlayerMoveEvent){
        val p = event.player
        val region = regionManager.getRegion(p)!!

        if (region.type != RegionType.SPAWN) return


        val to = event.to
        val block = p.world.getBlockAt(to)

        if (block.type == Material.WATER) randomTp(p)
    }

    fun randomTp(player: Player) {
        val world = plugin.worldManager.wildernessWorld
        val worldBorder = world.worldBorder

        val size = plugin.configurationManager.config.getInt("rtp-radius")
        var task: BukkitTask? = null

        task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val rndX = Random.nextInt(0, size+1)
            val rndZ = Random.nextInt(0, size+1)

            val block = player.world.getHighestBlockAt(rndX, rndZ)

            if (block.type.isSolid) {
                val loc = block.location
                player.teleport(loc.add(0.0, 1.0, 0.0))
                task!!.cancel()
            }
        }, 0L, 1L)



    }
}