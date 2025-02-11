package me.cirosanchez.factions.model.ability

import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.clib.storage.toDocument
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.listener.PlayerListener
import me.cirosanchez.factions.model.ability.AbilityType.*
import me.cirosanchez.factions.model.ability.impl.boost.BoostAbility
import me.cirosanchez.factions.model.ability.impl.normal.NormalAbility
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class AbilityListener(val manager: AbilityManager) : Listener {

    @EventHandler
    fun playerInteract(event: PlayerInteractEvent){
        val item = event.item ?: return
        val player = event.player
        val pdc = item.itemMeta.persistentDataContainer

        if (!pdc.has(NamespacedKey(Factions.get(), "ABILITY-TYPE"))) return

        val type = AbilityType.valueOf(pdc.get(NamespacedKey(Factions.get(), "ABILITY-TYPE"), PersistentDataType.STRING)!!)

        when (type){
            POSITIVE -> {
                val ability = manager.abilities.get(pdc.get(NamespacedKey(Factions.get(), type.toString()),
                    PersistentDataType.STRING)!!)

                if (ability == null) return

                val effects = (ability as NormalAbility).effects

                effects.forEach {
                    player.addPotionEffect(it)
                }
                player.sendColorizedMessageFromMessagesFile("ability.used", Placeholder("{displayName}", ability.displayName))
            }
            NEGATIVE -> {
                val ability = manager.abilities.get(pdc.get(NamespacedKey(Factions.get(), type.toString()),
                    PersistentDataType.STRING)!!)

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
                player.sendColorizedMessageFromMessagesFile("ability.used", Placeholder("{displayName}", ability.displayName))
            }
            BOOST -> {
                val ability = manager.abilities.get(pdc.get(NamespacedKey(Factions.get(), type.toString()),
                    PersistentDataType.STRING)!!)

                if (ability == null) return
                if (ability !is BoostAbility) return



                player.sendColorizedMessageFromMessagesFile("ability.used", Placeholder("{displayName}", ability.displayName))
            }
            ANTI_TRAP -> {
                return
            }
            PROJECTILE -> {
                return
            }
        }
    }

    fun getPlayersInRadius(player: Player, radius: Int): List<Player> {
        val entities = player.getNearbyEntities(radius.toDouble(), radius.toDouble(), radius.toDouble())

        return entities.filter { it is Player }.map { it as Player }
    }
}