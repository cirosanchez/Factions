package me.cirosanchez.factions.model.scoreboard


import fr.mrmicky.fastboard.adventure.FastBoard
import me.cirosanchez.clib.configuration.Configuration
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.model.scoreboard.util.Lines
import me.cirosanchez.factions.model.scoreboard.util.Title
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import me.cirosanchez.clib.extension.colorize

class ScoreboardManager : Manager {
    lateinit var plugin: Factions
    val boards: HashMap<Player, FastBoard> = hashMapOf()
    lateinit var configuration: Configuration
    var refresh: Long = 10
    lateinit var title: Title
    lateinit var lines: Lines

    override fun load() {
        plugin = Factions.get()
        configuration = plugin.configurationManager.scoreboard
        refresh = configuration.getLong("refresh")
        title = Title(configuration)
        lines = Lines(configuration)

        plugin.server.pluginManager.registerEvents(ScoreboardListener(this), plugin)
        Bukkit.getScheduler()
            .runTaskTimer(plugin,
                Runnable {
                    for (p in boards.keys){
                        update(p)
                    }
                } , 0L, refresh)
    }


    // Add a player to send the scoreboard.
    fun register(player: Player){
        val board = FastBoard(player)
        this.boards[player] = board
    }

    // Remove a player from the scoreboard
    fun unregister(player: Player){
        this.boards.remove(player)
    }

    private fun update(player: Player) {
        val board = boards[player]

        if (board == null) {
            plugin.logger.warning("An error ocurred while managing $player scoreboard! Inform to developer.")
            return
        }

        board.updateTitle(title.next())
        val lines = lines.get(player)
        val newLines = lines.map { it.colorize() }
        board.updateLines(newLines)
    }


    override fun unload() {

    }
}