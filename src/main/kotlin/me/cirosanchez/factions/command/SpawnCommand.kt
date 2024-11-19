package me.cirosanchez.factions.command

import me.cirosanchez.clib.extension.colorize
import me.cirosanchez.clib.extension.send
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.listener.PlayerListener
import me.cirosanchez.factions.util.EmptyPlaceholder
import me.cirosanchez.factions.util.WandSession
import me.cirosanchez.factions.util.WandType
import me.cirosanchez.factions.util.toPrettyStringWithoutWorld
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("spawn")
@Description("Spawn related commands.")
@CommandPermission("factions.command.spawn")
class SpawnCommand {

    val spawnManager = Factions.get().spawnManager
    val worldManager = Factions.get().worldManager

    @DefaultFor("~")
    fun spawn(actor: Player){
        spawnManager.tpToSpawn(actor)
    }

    val item = ItemStack(Material.WOODEN_HOE)

    init {
        val meta = item.itemMeta
        meta.displayName("<yellow>Spawn Claiming Wand</yellow>".colorize())

        val lore = listOf<String>("<yellow>Left-Click</yellow> to set position 1.","","<yellow>Right-Click</yellow> to set position 2.", "", "<yellow>Left-Click+Shift</yellow> to claim.")
        meta.lore(lore.map { it.colorize() })

        meta.isUnbreakable = true
        meta.addEnchant(Enchantment.LURE, 10, false)

        val pdc = meta.persistentDataContainer
        pdc.set(NamespacedKey(Factions.get(), "SPAWN-WAND"), PersistentDataType.STRING, "YAY")

        item.itemMeta = meta
    }

    @Subcommand("set")
    @Description("Set spawn point.")
    @CommandPermission("factions.command.spawn.set")
    fun set(actor: Player){
        val world = actor.location.world

        if (world.uid != worldManager.mainWorld.uid){
            actor.sendColorizedMessageFromMessagesFile("spawn.not-proper-world", Placeholder("",""))
            return
        }

        spawnManager.spawn.location = actor.location
        actor.sendColorizedMessageFromMessagesFile("spawn.set", Placeholder("{location}", actor.location.toPrettyStringWithoutWorld()))
    }

    @Subcommand("claim")
    @Description("Set spawn point.")
    @CommandPermission("factions.command.spawn.claim")
    fun claim(actor: Player){
        actor.inventory.addItem(item)
        actor.sendColorizedMessageFromMessagesFile("spawn.wand", EmptyPlaceholder.E)
        PlayerListener.wandPlayers.put(actor, WandSession(null, null, WandType.SPAWN))
    }
}