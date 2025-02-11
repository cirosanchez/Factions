package me.cirosanchez.factions.model.chat

import me.cirosanchez.clib.extension.placeholders
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.util.getTeam
import org.bukkit.entity.Player

class ChatManager : Manager {

    lateinit var plugin: Factions
    lateinit var chatFormat: String
    lateinit var staffFormat: String
    lateinit var adminFormat: String
    var status = ChatStatus.PUBLIC

    val chats: HashMap<Player, ChatType> = hashMapOf()

    override fun load() {
        plugin = Factions.get()
        chatFormat = plugin.configurationManager.config.getString("chat.format") ?: "{team} {rank} {name} <dark_gray>»</dark_gray> <gray>{message}</gray>"
        staffFormat = plugin.configurationManager.config.getString("chat.staff-format") ?: "{team} {rank} {name} <dark_gray>»</dark_gray> <gray>{message}</gray>"
        adminFormat = plugin.configurationManager.config.getString("chat.admin-format") ?: "{team} {rank} {name} <dark_gray>»</dark_gray> <gray>{message}</gray>"
    }

    override fun unload() {
        //
    }

    fun getChatStatusString(): String {
        return when (status){
            ChatStatus.MUTED -> {
                "<red>Muted</red>"
            }
            ChatStatus.VIP -> {
                "<gold>VIP</gold>"
            }
            ChatStatus.PUBLIC -> {
                "<green>Public</green>"
            }
            ChatStatus.STAFF -> {
                "<purple>Staff</purple>"
            }
            ChatStatus.ADMIN -> {
                "<aqua>Admin</aqua>"
            }
        }
    }

    fun getChatPermission(): String {
        return when (status){
            ChatStatus.MUTED -> {
                "factions.chat.muted"
            }
            ChatStatus.VIP -> {
                "factions.chat.vip"
            }
            ChatStatus.PUBLIC -> {
                "factions.chat.public"
            }
            ChatStatus.STAFF -> {
                "factions.chat.staff"
            }
            ChatStatus.ADMIN -> {
                "factions.chat.admin"
            }
        }
    }

    fun getFormattedMessage(player: Player, message: String): Pair<ChatType, String> {
        val teamName = player.getTeam()?.name ?: ""
        val rank = plugin.rankManager.rankProvider.getPrefix(player.uniqueId)
        val name = player.name

        val type = chats[player]

        return when (type) {
            ChatType.PUBLIC -> {
                ChatType.PUBLIC to chatFormat.placeholders(
                    Placeholder("{team}", teamName),
                    Placeholder("{rank}", rank),
                    Placeholder("{name}", name),
                    Placeholder("{message}", message)
                )
            }
            ChatType.STAFF -> {
                ChatType.STAFF to staffFormat.placeholders(
                    Placeholder("{team}", teamName),
                    Placeholder("{rank}", rank),
                    Placeholder("{name}", name),
                    Placeholder("{message}", message)
                )
            }
            ChatType.ADMIN -> {
                ChatType.ADMIN to adminFormat.placeholders(
                    Placeholder("{team}", teamName),
                    Placeholder("{rank}", rank),
                    Placeholder("{name}", name),
                    Placeholder("{message}", message)
                )
            }
            null -> {
                ChatType.PUBLIC to chatFormat.placeholders(
                    Placeholder("{team}", teamName),
                    Placeholder("{rank}", rank),
                    Placeholder("{name}", name),
                    Placeholder("{message}", message)
                )
            }
        }
    }

    fun addPlayer(player: Player){
        if (chats.contains(player)) return

        chats.put(player, ChatType.PUBLIC)
    }

    fun toggleStaffChatForPlayer(player: Player): Boolean {
        val type = chats.get(player)!!

        if (type == ChatType.STAFF){
            chats.put(player, ChatType.PUBLIC)
            return false
        } else{
            chats.put(player, ChatType.STAFF)
            return true
        }
    }

    fun toggleAdminChatForPlayer(player: Player): Boolean {
        val type = chats.get(player)!!

        if (type == ChatType.ADMIN){
            chats.put(player, ChatType.PUBLIC)
            return false
        } else {
            chats.put(player, ChatType.ADMIN)
            return true
        }
    }

}