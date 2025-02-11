package me.cirosanchez.factions.command

import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.chat.ChatStatus
import me.cirosanchez.factions.util.broadcastFromConfiguration
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("chat")
@CommandPermission("factions.command.chat")
class ChatCommand {

    val plugin = Factions.get()
    val chatManager = plugin.chatManager

    @DefaultFor("~")
    fun default(actor: Player){
        actor.sendColorizedMessageFromMessagesFileList("chat.default")
    }
    @Subcommand("status")
    @CommandPermission("factions.command.chat.status")
    fun status(actor: Player) {
        val type = chatManager.chats.get(actor)!!
        actor.sendColorizedMessageFromMessagesFileList("chat.status", Placeholder("{status}", chatManager.getChatStatusString()),
            Placeholder("{chat}", type.toString()))
    }

    @Subcommand("set")
    @CommandPermission("factions.command.chat.set")
    fun set(actor: Player, status: ChatStatus){
        chatManager.status = status
        val statusString = chatManager.getChatStatusString()
        actor.sendColorizedMessageFromMessagesFile("chat.set.set", Placeholder("{status}", statusString))
        broadcastFromConfiguration("chat.set.broadcast", Placeholder("{status}", statusString), Placeholder("{actor}", actor.name))
    }

    @Command("staffchat", "sc")
    @CommandPermission("factions.command.staffchat")
    fun staffChat(actor: Player){
        val bool = plugin.chatManager.toggleStaffChatForPlayer(actor)

        if (bool){
            actor.sendColorizedMessageFromMessagesFile("chat.staff-chat-enabled")
        } else {
            actor.sendColorizedMessageFromMessagesFile("chat.staff-chat-disabled")
        }
    }

    @Command("adminchat", "ac")
    @CommandPermission("factions.command.adminchat")
    fun adminChat(actor: Player){
        val bool = plugin.chatManager.toggleAdminChatForPlayer(actor)

        if (bool){
            actor.sendColorizedMessageFromMessagesFile("chat.admin-chat-enabled")
        } else {
            actor.sendColorizedMessageFromMessagesFile("chat.admin-chat-disabled")
        }
    }
}