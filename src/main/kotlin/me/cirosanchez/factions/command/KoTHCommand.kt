package me.cirosanchez.factions.command

import me.cirosanchez.clib.extension.colorize
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.listener.PlayerListener
import me.cirosanchez.factions.model.koth.KoTH
import me.cirosanchez.factions.util.WandSession
import me.cirosanchez.factions.util.WandType
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("koth")
@CommandPermission("factions.command.koth")
class KoTHCommand {

    val plugin = Factions.get()
    val manager = plugin.kothManager
    val item = ItemStack(Material.NETHERITE_HOE)

    init {
        val meta = item.itemMeta
        meta.displayName("<yellow>Koth Claiming Wand</yellow>".colorize())

        val lore = listOf<String>(
            "<yellow>Left-Click</yellow> to set position 1.",
            "",
            "<yellow>Right-Click</yellow> to set position 2.",
            "",
            "<yellow>Left-Click+Shift</yellow> to claim."
        )
        meta.lore(lore.map { it.colorize() })

        meta.isUnbreakable = true
        meta.addEnchant(Enchantment.LURE, 10, false)

        val pdc = meta.persistentDataContainer
        pdc.set(NamespacedKey(Factions.get(), "WAND"), PersistentDataType.STRING, "YAY")

        item.itemMeta = meta
    }

    @DefaultFor("~")
    fun default(actor: Player) {
        actor.sendColorizedMessageFromMessagesFileList("koth.default")
    }

    @Subcommand("create")
    @CommandPermission("factions.command.koth.create")
    fun create(actor: Player, name: String, time: String) {
        if (manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.create.already-exists", Placeholder("{name}", name))
            return
        }

        val seconds = formatTime(time)

        if (seconds <= 0L) {
            actor.sendColorizedMessageFromMessagesFile("koth.create.secs-cant-be-zero")
            return
        }

        val koth = KoTH(name, seconds, null, null, mutableListOf())
        manager.addKoTH(koth)
        actor.sendColorizedMessageFromMessagesFile("koth.create.created", Placeholder("{name}", name))
    }

    @Subcommand("delete")
    @CommandPermission("factions.command.koth.delete")
    fun delete(actor: Player, name: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        manager.removeKoTH(koth)
        actor.sendColorizedMessageFromMessagesFile("koth.delete.deleted", Placeholder("{name}", name))
    }

    @Subcommand("rename")
    @CommandPermission("factions.command.koth.rename")
    fun rename(actor: Player, name: String, newName: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        if (manager.hasKoTH(name)){
            actor.sendColorizedMessageFromMessagesFile("koth.rename.already-exists", Placeholder("{newName}", newName))
            return
        }

        koth.name = newName
        actor.sendColorizedMessageFromMessagesFile("koth.rename.renamed", Placeholder("{newName}", newName), Placeholder("{name}", name))
    }

    @Subcommand("setcapzone")
    @CommandPermission("factions.command.koth.setcapzone")
    fun setcappzone(actor: Player, name: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        actor.inventory.addItem(item)
        PlayerListener.kothCapzonePlayer.put(actor, koth)
        PlayerListener.wandPlayers.put(actor, WandSession(null, null, WandType.KOTH_CAPZONE))
        actor.sendColorizedMessageFromMessagesFile("koth.gave-wand")
    }

    @Subcommand("setclaim")
    @CommandPermission("factions.command.koth.setcapzone")
    fun setclaim(actor: Player, name: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        actor.inventory.addItem(item)
        PlayerListener.kothClaimPlayer.put(actor, koth)
        PlayerListener.wandPlayers.put(actor, WandSession(null, null, WandType.KOTH_CLAIM))
        actor.sendColorizedMessageFromMessagesFile("koth.gave-wand")
    }

    @Subcommand("settime")
    @CommandPermission("factions.command.koth.settime")
    fun time(actor: Player, name: String, time: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        val seconds = formatTime(time)

        if (seconds == 0L){
            actor.sendColorizedMessageFromMessagesFile("koth.settime.secs-cant-be-zero")
            return
        }

        koth.time = seconds
        actor.sendColorizedMessageFromMessagesFile("koth.settime.set", Placeholder("{time}", time))
    }

    @Subcommand("listcommands")
    @CommandPermission("factions.command.koth.listcommands")
    fun listCmds(actor: Player, name: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        koth.commands.forEachIndexed { i, cmd ->
            actor.sendColorizedMessageFromMessagesFile("koth.list-commands.list-format", Placeholder("{id}", i.toString()),
                Placeholder("{cmd}", cmd))
        }
    }

    @Subcommand("addcommand")
    @CommandPermission("factions.command.koth.addcommand")
    fun addCommand(actor: Player, name: String, command: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!

        if (command.contains("/")){
            actor.sendColorizedMessageFromMessagesFile("koth.add.cant-have-slash")
            return
        }

        koth.commands.add(command)
        actor.sendColorizedMessageFromMessagesFile("koth.add.added", Placeholder("{cmd}", command))
    }

    @Subcommand("removecommand")
    @CommandPermission("factions.command.koth.removecommand")
    fun removeCommand(actor: Player, name: String, id: Int){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        val koth = manager.getKoth(name)!!
        val list = koth.commands

        if (list.size-1 < id){
            actor.sendColorizedMessageFromMessagesFile("koth.remove-command.id-doesnt-exist", Placeholder("{name}", name))
            return
        }

        val removed = list.removeAt(id)
        actor.sendColorizedMessageFromMessagesFile("koth.remove-command.removed", Placeholder("{name}", name), Placeholder("{cmd}", removed))
    }

    @Subcommand("start")
    @CommandPermission("factions.command.koth.start")
    fun start(actor: Player, name: String){
        if (!manager.hasKoTH(name)) {
            actor.sendColorizedMessageFromMessagesFile("koth.doesnt-exist", Placeholder("{name}", name))
            return
        }

        if (manager.activeKoth != null){
            actor.sendColorizedMessageFromMessagesFile("koth.start.theres-active-koth")
            return
        }


        val koth = manager.getKoth(name)!!

        if (koth.cuboid == null || koth.region == null){
            actor.sendColorizedMessageFromMessagesFile("koth.start.not-set-up", Placeholder("{name}", name))
            return
        }

        manager.startKoth(koth)
    }

    @Subcommand("stop")
    @CommandPermission("factions.command.koth.stop")
    fun stop(actor: Player){
        if (manager.activeKoth == null){
            actor.sendColorizedMessageFromMessagesFile("koth.stop.theres-no-active-koth")
            return
        }

        manager.stopKoTH()
        actor.sendColorizedMessageFromMessagesFile("koth.stop.stopped")
    }

    fun formatTime(string: String): Long {
        var totalSeconds = 0L
        val regex = Regex("(\\d+h)?(\\d+m)?(\\d+s)?")
        val matchResult = regex.matchEntire(string)

        if (matchResult != null) {
            val (hours, minutes, seconds) = matchResult.destructured

            if (hours.isNotEmpty()) {
                totalSeconds += hours.dropLast(1).toLong() * 3600
            }
            if (minutes.isNotEmpty()) {
                totalSeconds += minutes.dropLast(1).toLong() * 60
            }
            if (seconds.isNotEmpty()) {
                totalSeconds += seconds.dropLast(1).toLong()
            }
        }

        return totalSeconds
    }

}