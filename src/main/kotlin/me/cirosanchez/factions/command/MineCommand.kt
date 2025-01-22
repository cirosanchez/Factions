package me.cirosanchez.factions.command

import me.cirosanchez.clib.extension.colorize
import me.cirosanchez.clib.extension.send
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.listener.PlayerListener
import me.cirosanchez.factions.model.mine.Mine
import me.cirosanchez.factions.model.mine.MineManager
import me.cirosanchez.factions.util.WandSession
import me.cirosanchez.factions.util.WandType
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission


@Command("mine")
@Description("Mine related commands.")
@CommandPermission("factions.command.mine")
class MineCommand {
    val plugin = Factions.get()
    val mm = plugin.mineManager

    val item = ItemStack(Material.WOODEN_HOE)

    init {
        val meta = item.itemMeta
        meta.displayName("<yellow>Mine Claiming Wand</yellow>".colorize())

        val lore = listOf<String>("<yellow>Left-Click</yellow> to set position 1.","","<yellow>Right-Click</yellow> to set position 2.", "", "<yellow>Left-Click+Shift</yellow> to claim.")
        meta.lore(lore.map { it.colorize() })

        meta.isUnbreakable = true
        meta.addEnchant(Enchantment.LURE, 10, false)

        val pdc = meta.persistentDataContainer
        pdc.set(NamespacedKey(Factions.get(), "WAND"), PersistentDataType.STRING, "YAY")

        item.itemMeta = meta
    }

    @DefaultFor("~")
    fun def(actor: Player) {
        actor.sendColorizedMessageFromMessagesFileList("mine.default")
    }

    @Subcommand("create")
    @CommandPermission("factions.command.mine.create")
    fun create(actor: Player, @Named("name") name: String, @Named("regenTime") regenTime: Long, displayName: String) {


        if (mm.mineExists(name)) {
            actor.sendColorizedMessageFromMessagesFile("mine.create.mine-exists")
            return
        }

        val permission = "factions.mines.$name"

        val mine =
            Mine(name, displayName, ItemStack(Material.DIAMOND_ORE), ItemStack(Material.DIAMOND), null, null, permission, regenTime)
        mm.addMine(mine)
        actor.sendColorizedMessageFromMessagesFile("mine.create.created", Placeholder("{name}", name))
    }

    @Subcommand("delete")
    @CommandPermission("factions.command.mine.delete")
    fun delete(actor: Player, @Named("name") name: String) {
        if (!mm.mineExists(name)) {
            actor.sendColorizedMessageFromMessagesFile("mine.delete.mine-doesnt-exist", Placeholder("{name}", name))
            return
        }

        mm.removeMine(mm.getMine(name)!!)
        actor.sendColorizedMessageFromMessagesFile("mine.delete.deleted", Placeholder("{name}", name))
    }

    @Subcommand("info")
    @CommandPermission("factions.command.mine.info")
    fun info(actor: Player, @Named("name") name: String) {
        if (!mm.mineExists(name)) {
            actor.sendColorizedMessageFromMessagesFile("mine.delete.mine-doesnt-exist", Placeholder("{name}", name))
            return
        }

        val mine = mm.getMine(name)!!
        actor.send("<gray><st>---------------------------------------------------</st></gray>")
        actor.send("${mine.displayName} Mine")


        val blockDisplayName = MiniMessage.miniMessage().serialize(mine.block.displayName())
        val blockComponent = "Block: $blockDisplayName".colorize()
        actor.sendMessage(blockComponent)


        val rewardDisplayName = MiniMessage.miniMessage().serialize(mine.reward.displayName())
        val rewardComponent = "Reward: $rewardDisplayName".colorize()
        actor.sendMessage(rewardComponent)


        if (mine.claim != null) {
            val x1 = mine.claim!!.cuboid!!.x1
            val x2 = mine.claim!!.cuboid!!.x2

            val y1 = mine.claim!!.cuboid!!.y1
            val y2 = mine.claim!!.cuboid!!.y2

            actor.send("Claim: ${x1}, ${y1} - ${x2}, ${y2}")
        }

        if (mine.cuboid != null) {
            val x1 = mine.cuboid!!.x1
            val x2 = mine.cuboid!!.x2

            val y1 = mine.cuboid!!.y1
            val y2 = mine.cuboid!!.y2

            actor.send("Cuboid: $x1, $y1 - $x2, $y2")
        }
        val regenTime = mine.regenTime
        actor.send("Regeneration Time: $regenTime")

        val permission = mine.permission
        actor.send("Permission: $permission")
        actor.send("<gray><st>---------------------------------------------------</st></gray>")
    }

    @Subcommand("list")
    @CommandPermission("factions.commands.mine.list")
    fun list(actor: Player) {
        actor.send("<gray><st>---------------------------------------------------</st></gray>")
        if (mm.mines.values.isEmpty()){
            actor.send("No mines created yet...")
        } else {
            mm.mines.values.forEach {
                actor.send("${it.displayName} Mine")
            }
        }
        actor.send("<gray><st>---------------------------------------------------</st></gray>")
    }

    @Subcommand("setDisplayName")
    @CommandPermission("factions.commands.mine.setDisplayName")
    fun setDisplayName(actor: Player, name: String, displayName: String){
        if (!mm.mineExists(name)) {
            actor.sendColorizedMessageFromMessagesFile("mine.delete.mine-doesnt-exist", Placeholder("{name}", name))
            return
        }

        val mine = mm.getMine(name)!!

        mine.displayName = displayName
    }

    @Subcommand("setBlock")
    @CommandPermission("factions.commands.mine.setBlock")
    fun setBlock(actor: Player, name: String){
        if (!mm.mineExists(name)) {
            actor.sendColorizedMessageFromMessagesFile("mine.delete.mine-doesnt-exist", Placeholder("{name}", name))
            return
        }

        val mine = mm.getMine(name)!!
        val item = actor.inventory.itemInMainHand

        if (!item.type.isBlock){
            actor.sendColorizedMessageFromMessagesFile("mine.setBlock.not-block")
        }

        mine.block = item
        actor.sendColorizedMessageFromMessagesFile("mine.setBlock.set", Placeholder("{item}", item.i18NDisplayName!!))
    }

    @Subcommand("setRegenTime")
    @CommandPermission("factions.commands.mine.setRegenTime")
    fun setRegenTime(actor: Player, name: String, regenTime: Long){
        if (!mm.mineExists(name)) {
            actor.sendColorizedMessageFromMessagesFile("mine.delete.mine-doesnt-exist", Placeholder("{name}", name))
            return
        }

        val mine = mm.getMine(name)!!
        mine.regenTime = regenTime



        actor.sendColorizedMessageFromMessagesFile("mine.regenTime.set", Placeholder("{time}", regenTime.toString()))
    }

    @Subcommand("setReward")
    @CommandPermission("factions.commands.mine.setReward")
    fun setRewards(actor: Player, name: String){
        if (!mm.mineExists(name)) {
            actor.sendColorizedMessageFromMessagesFile("mine.delete.mine-doesnt-exist", Placeholder("{name}", name))
            return
        }

        val mine = mm.getMine(name)!!
        val item = actor.inventory.itemInMainHand

        if (!item.type.isItem){
            actor.sendColorizedMessageFromMessagesFile("mine.setRewards.not-item")
            return
        }

        mine.reward = item
        actor.sendColorizedMessageFromMessagesFile("mine.setRewards.not-item", Placeholder("{item}", MiniMessage.miniMessage().serialize(item.displayName())))
    }


    @Subcommand("setClaim")
    @CommandPermission("factions.commands.mine.setClaim")
    fun setClaim(actor: Player, name: String){
        if (!mm.mineExists(name)) {
            actor.sendColorizedMessageFromMessagesFile("mine.delete.mine-doesnt-exist", Placeholder("{name}", name))
            return
        }

        val mine = mm.getMine(name)!!

        actor.inventory.addItem(item)
        PlayerListener.claimPlayers.put(actor, mine)
        PlayerListener.wandPlayers.put(actor, WandSession(null, null, WandType.MINE_CLAIM))
    }

    @Subcommand("setCuboid")
    @CommandPermission("factions.commands.mine.setCuboid")
    fun setCuboid(actor: Player, name: String){
        if (!mm.mineExists(name)) {
            actor.sendColorizedMessageFromMessagesFile("mine.delete.mine-doesnt-exist", Placeholder("{name}", name))
            return
        }

        val mine = mm.getMine(name)!!

        actor.inventory.addItem(item)
        PlayerListener.cuboidPlayers.put(actor, mine)
        PlayerListener.wandPlayers.put(actor, WandSession(null, null, WandType.MINE_CUBOID))
    }

}