package me.cirosanchez.factions.model.ability

import io.papermc.paper.event.entity.EntityDamageItemEvent
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.clib.storage.toDocument
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.listener.PlayerListener
import me.cirosanchez.factions.model.ability.AbilityType.*
import me.cirosanchez.factions.model.ability.impl.antitrap.AntiTrapAbility
import me.cirosanchez.factions.model.ability.impl.boost.BoostAbility
import me.cirosanchez.factions.model.ability.impl.normal.NormalAbility
import me.cirosanchez.factions.model.ability.impl.projectile.ProjectileAbility
import me.cirosanchez.factions.model.ability.impl.projectile.ProjectileType.*
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class AbilityListener(val manager: AbilityManager) : Listener {

    val playersAntiTrapped: ArrayList<Player> = arrayListOf()
    val hits: HashMap<Player, Pair<Player, Int>> = hashMapOf()
    val projectilesPlayer: HashMap<Player, ProjectileAbility> = hashMapOf()


    @EventHandler
    fun playerInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val player = event.player
        val pdc = item.itemMeta.persistentDataContainer

        if (!pdc.has(NamespacedKey(Factions.get(), "ABILITY-TYPE"))) return

        val type =
            AbilityType.valueOf(pdc.get(NamespacedKey(Factions.get(), "ABILITY-TYPE"), PersistentDataType.STRING)!!)

        when (type) {
            POSITIVE -> {
                val ability = manager.abilities.get(
                    pdc.get(
                        NamespacedKey(Factions.get(), type.toString()),
                        PersistentDataType.STRING
                    )!!
                )

                if (ability == null) return

                val effects = (ability as NormalAbility).effects

                effects.forEach {
                    player.addPotionEffect(it)
                }
                player.sendColorizedMessageFromMessagesFile(
                    "ability.used",
                    Placeholder("{displayName}", ability.displayName)
                )
            }

            NEGATIVE -> {
                val ability = manager.abilities.get(
                    pdc.get(
                        NamespacedKey(Factions.get(), type.toString()),
                        PersistentDataType.STRING
                    )!!
                )

                if (ability == null) return
                if (ability !is NormalAbility) return

                val effects = ability.effects

                val radius = ability.radius

                val playersNearby = getPlayersInRadius(player, radius)

                playersNearby.forEach { p ->
                    effects.forEach { e ->
                        p.addPotionEffect(e)
                    }
                }
                player.sendColorizedMessageFromMessagesFile(
                    "ability.used",
                    Placeholder("{displayName}", ability.displayName)
                )
            }

            BOOST -> {
                val ability = manager.abilities.get(
                    pdc.get(
                        NamespacedKey(Factions.get(), type.toString()),
                        PersistentDataType.STRING
                    )!!
                )

                if (ability == null) return
                if (ability !is BoostAbility) return


                val direction = player.location.direction

                direction.y = ability.boost / 2.0
                direction.multiply(ability.boost)

                player.sendColorizedMessageFromMessagesFile(
                    "ability.used",
                    Placeholder("{displayName}", ability.displayName)
                )
            }

            ANTI_TRAP, PROJECTILE -> {
                return
            }
        }
    }


    @EventHandler
    fun blockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand

        val pdc = item.itemMeta.persistentDataContainer

        if (!pdc.has(NamespacedKey(Factions.get(), "ABILITY-TYPE"))) return

        val type =
            AbilityType.valueOf(pdc.get(NamespacedKey(Factions.get(), "ABILITY-TYPE"), PersistentDataType.STRING)!!)

        when (type) {
            BOOST, PROJECTILE, NEGATIVE, POSITIVE -> {
                return
            }

            ANTI_TRAP -> {
                val ability = manager.abilities.get(
                    pdc.get(
                        NamespacedKey(Factions.get(), type.toString()),
                        PersistentDataType.STRING
                    )!!
                )

                if (ability == null) return
                if (ability !is AntiTrapAbility) return

                val players = getPlayersInRadius(event.player, ability.radius)

                players.forEach {
                    playersAntiTrapped.add(event.player)
                }

                event.player.sendColorizedMessageFromMessagesFile(
                    "ability.used",
                    Placeholder("{displayName}", ability.displayName)
                )
            }
        }

    }

    @EventHandler
    fun damage(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        val damager = event.damager

        if (entity !is Player) return
        if (damager !is Player) return

        val item = damager.inventory.itemInMainHand

        val pdc = item.itemMeta.persistentDataContainer

        if (!pdc.has(NamespacedKey(Factions.get(), "ABILITY-TYPE"))) return

        val type =
            AbilityType.valueOf(pdc.get(NamespacedKey(Factions.get(), "ABILITY-TYPE"), PersistentDataType.STRING)!!)

        when (type) {
            BOOST, PROJECTILE, NEGATIVE, POSITIVE -> {
                return
            }

            ANTI_TRAP -> {
                val ability = manager.abilities.get(
                    pdc.get(
                        NamespacedKey(Factions.get(), type.toString()),
                        PersistentDataType.STRING
                    )!!
                )

                if (ability == null) return
                if (ability !is AntiTrapAbility) return


                if (!hits.contains(damager)) {
                    hits.put(damager, Pair(entity, 1))
                    damager.sendColorizedMessageFromMessagesFileList(
                        "ability.hit-anti-trapper", Placeholder("{actualHits}", "1"),
                        Placeholder("{totalHits}", ability.hits.toString())
                    )
                    return
                }

                val pair = hits.get(damager)!!
                if (pair.second + 1 == ability.hits) {
                    hits.remove(damager)
                    this.playersAntiTrapped.add(pair.first)
                    damager.sendColorizedMessageFromMessagesFile(
                        "ability.used",
                        Placeholder("{displayName}", ability.displayName)
                    )
                    return
                }

                val newHits = pair.second + 1
                hits.put(damager, Pair(entity, newHits))
                damager.sendColorizedMessageFromMessagesFileList(
                    "ability.hit-anti-trapper", Placeholder("{actualHits}", newHits.toString()),
                    Placeholder("{totalHits}", ability.hits.toString())
                )
            }
        }

        @EventHandler
        fun proj(event: ProjectileLaunchEvent) {
            val uuid = event.entity.ownerUniqueId ?: return
            val player = Bukkit.getPlayer(uuid) ?: return
            val item = player.inventory.itemInMainHand

            val pdc = item.itemMeta.persistentDataContainer

            if (!pdc.has(NamespacedKey(Factions.get(), "ABILITY-TYPE"))) return

            val type =
                AbilityType.valueOf(pdc.get(NamespacedKey(Factions.get(), "ABILITY-TYPE"), PersistentDataType.STRING)!!)

            when (type) {
                PROJECTILE -> {
                    val ability = manager.abilities.get(
                        pdc.get(
                            NamespacedKey(Factions.get(), type.toString()),
                            PersistentDataType.STRING
                        )!!
                    )

                    if (ability !is ProjectileAbility) return

                    projectilesPlayer.put(player, ability)
                }

                else -> {
                    return
                }
            }
        }

        @EventHandler
        fun projectile(event: ProjectileHitEvent) {
            val player = Bukkit.getPlayer(event.entity.ownerUniqueId ?: return) ?: return
            if (!projectilesPlayer.contains(player)) return
            val ability = projectilesPlayer.get(player)!!

            when (ability.projectileType){
                POTION -> {
                    val hit = event.hitEntity ?: return

                    if (hit !is Player) return

                    ability.effects.forEach {
                        hit.addPotionEffect(it)
                    }

                }
                SWITCH -> {
                    val hit = event.hitEntity ?: return

                    if (hit !is Player) return

                    val hitLoc = hit.location
                    val loc = player.location

                    hit.teleport(loc)
                    player.teleport(hitLoc)
                }
            }
        }
    }

    fun getPlayersInRadius(player: Player, radius: Int): List<Player> {
        val entities = player.getNearbyEntities(radius.toDouble(), radius.toDouble(), radius.toDouble())

        return entities.filter { it is Player }.filterIsInstance<Player>()
    }
}