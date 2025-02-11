package me.cirosanchez.factions.command

import me.cirosanchez.clib.extension.send
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("ability")
@CommandPermission("factions.command.ability")
class AbilityCommand {

    val manager = Factions.get().abilityManager

    @DefaultFor("~")
    fun default(actor: Player){
        actor.sendColorizedMessageFromMessagesFileList("ability.default")
    }

    @Subcommand("list")
    @CommandPermission("factions.command.ability.list")
    fun list(actor: Player){
        val abilities = manager.abilities.values

        if (abilities.isEmpty()){
            actor.sendColorizedMessageFromMessagesFile("ability.no-abilities")
            return
        }

        actor.send("<gray><st>---------------------------------------------------</st></gray>")
        abilities.forEach {
            actor.sendColorizedMessageFromMessagesFile("vability.list.format", Placeholder("{name}", it.name),
                Placeholder("{displayName}", it.displayName))
        }
        actor.send("<gray><st>---------------------------------------------------</st></gray>")
    }

    @Subcommand("gui")
    @CommandPermission("factions.command.ability.gui")
    fun gui(actor: Player){
        val abilities = manager.abilities.values

        if (abilities.isEmpty()){
            actor.sendColorizedMessageFromMessagesFile("ability.no-abilities")
            return
        }

        val inv = Bukkit.createInventory(null, InventoryType.CHEST, "Abilities")
        abilities.forEach {
            inv.addItem(it.getItemStack())
        }

        actor.openInventory(inv)
    }

    @Subcommand("toggle")
    @CommandPermission("factions.command.ability.toggle")
    fun toggle(actor: Player, name: String){
        val ability = manager.abilities.get(name)

        if (ability == null){
            actor.sendColorizedMessageFromMessagesFile("ability.no-ability", Placeholder("{name}", name))
            return
        }

        val bool = ability.toggle()
        if (bool){
            actor.sendColorizedMessageFromMessagesFile("ability.toggle.toggled", Placeholder("{status}", "<green>Enabled</green>"),
                Placeholder("{displayName}", ability.displayName))
        } else {
            actor.sendColorizedMessageFromMessagesFile("ability.toggle.toggled", Placeholder("{status}", "<red>Disabled</red>"),
                Placeholder("{displayName}", ability.displayName))
        }
    }

    @Subcommand("get")
    @CommandPermission("factions.command.ability.get")
    fun get(actor: Player, name: String, amount: Int){
        val ability = manager.abilities.get(name)

        if (ability == null){
            actor.sendColorizedMessageFromMessagesFile("ability.no-ability", Placeholder("{name}", name))
            return
        }

        actor.inventory.addItem(ability.getItemStack().clone().add(amount-1))
        actor.sendColorizedMessageFromMessagesFile("ability.get.given", Placeholder("{amount}", amount.toString()),
            Placeholder("{displayName}", ability.displayName))
    }
}