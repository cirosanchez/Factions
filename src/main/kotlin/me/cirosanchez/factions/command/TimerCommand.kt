package me.cirosanchez.factions.command

import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.scoreboard.util.counter.Counter
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission


@Command("timer")
@CommandPermission("factions.command.timer")
class TimerCommand {

    val manager = Factions.get().scoreboardManager.counterManager

    @DefaultFor("~")
    fun default(actor: Player){
        actor.sendColorizedMessageFromMessagesFileList("timer.default")
    }

    @Subcommand("create")
    @CommandPermission("factions.command.timer.create")
    fun create(actor: Player, name: String, time: String, text: String){
        if (this.manager.hasTimer(name)){
            actor.sendColorizedMessageFromMessagesFile("timer.create.already-exists")
            return
        }
        val secs = convertToSeconds(time)

        if (secs == 0L){
            actor.sendColorizedMessageFromMessagesFile("timer.create.secs-cant-be-zero")
            return
        }
        val counter = Counter(manager.plugin, name, text, secs, false)
        manager.addTimer(counter)
    }

    @Subcommand("delete")
    @CommandPermission("factions.command.timer.delete")
    fun delete(actor: Player, name: String){
        if (!this.manager.hasTimer(name)){
            actor.sendColorizedMessageFromMessagesFile("timer.doesnt-exist", Placeholder("{name}", name))
            return
        }

        if (manager.timerIsKoth(name)){
            actor.sendColorizedMessageFromMessagesFile("timer.is-koth")
            return
        }

        manager.removeTimer(name)
        actor.sendColorizedMessageFromMessagesFile("timer.delete.deleted", Placeholder("{name}", name))
    }

    @Subcommand("reset")
    @CommandPermission("factions.command.timer.reset")
    fun reset(actor: Player, name: String){
        if (!this.manager.hasTimer(name)){
            actor.sendColorizedMessageFromMessagesFile("timer.doesnt-exist", Placeholder("{name}", name))
            return
        }

        if (manager.timerIsKoth(name)){
            actor.sendColorizedMessageFromMessagesFile("timer.is-koth")
            return
        }

        val counter = manager.timers.get(name)!!

        counter.resetTime()

        actor.sendColorizedMessageFromMessagesFile("timer.reset.reset", Placeholder("{name}", name))
    }

    @Subcommand("rename")
    @CommandPermission("factions.command.timer.rename")
    fun rename(actor: Player, name: String, text: String){
        if (!this.manager.hasTimer(name)){
            actor.sendColorizedMessageFromMessagesFile("timer.doesnt-exist", Placeholder("{name}", name))
            return
        }

        if (manager.timerIsKoth(name)){
            actor.sendColorizedMessageFromMessagesFile("timer.is-koth")
            return
        }

        manager.renameTimer(name, text)
        actor.sendColorizedMessageFromMessagesFile("timer.rename.renamed", Placeholder("{name}", name), Placeholder("{text}", text))
    }

    @Subcommand("list")
    @CommandPermission("factions.command.timer.list")
    fun list(actor: Player){
        this.manager.timers.values.forEach {
            val name = Placeholder("{name}", it.name)
            val text = Placeholder("{text}", it.text)
            val time = Placeholder("{time}", it.remainingTime.toString())

            actor.sendColorizedMessageFromMessagesFile("timer.list.format", name, text, time)
        }
    }







    fun convertToSeconds(timeString: String): Long {
        val regex = Regex("""(\d+h)?(\d+m)?(\d+s)?""")
        val matchResult = regex.matchEntire(timeString) ?: return 0L

        val (hours, minutes, seconds) = matchResult.destructured

        val totalSeconds = (hours.dropLast(1).toLongOrNull() ?: 0L) * 3600 +
                (minutes.dropLast(1).toLongOrNull() ?: 0L) * 60 +
                (seconds.dropLast(1).toLongOrNull() ?: 0L)

        return totalSeconds
    }
}