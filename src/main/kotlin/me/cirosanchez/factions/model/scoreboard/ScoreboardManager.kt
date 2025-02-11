package me.cirosanchez.factions.model.scoreboard

import fr.mrmicky.fastboard.adventure.FastBoard
import me.cirosanchez.clib.extension.colorize
import me.cirosanchez.clib.extension.replacePlaceholdersForString
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.model.scoreboard.listener.ScoreboardListener
import me.cirosanchez.factions.model.scoreboard.util.Footer
import me.cirosanchez.factions.model.scoreboard.util.Title
import me.cirosanchez.factions.model.scoreboard.util.Line
import me.cirosanchez.factions.model.scoreboard.util.counter.Counter
import me.cirosanchez.factions.model.scoreboard.util.counter.CounterManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ScoreboardManager : Manager {

    lateinit var plugin: Factions
    lateinit var counterManager: CounterManager

    lateinit var title: Title
    lateinit var footer: Footer

    val lines: MutableList<Line> = mutableListOf()
    val boards: HashMap<Player, FastBoard> = hashMapOf()


    val countersText: MutableList<String> = mutableListOf()

    override fun load() {
        plugin = Factions.get()
        counterManager = CounterManager()
        counterManager.load()

        plugin.server.pluginManager.registerEvents(ScoreboardListener(this), plugin)

        title = Title(plugin)
        footer = Footer(plugin)

        plugin.configurationManager.scoreboard.getConfigurationSection("lines")!!.getKeys(false).forEach { key ->
            lines.add(Line(plugin, key.toInt()))
        }


        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            countersText.clear()
            counterManager.timers.values.forEach {
                it.remainingTime--
                if (it.remainingTime <= 0){
                    counterManager.timers.remove(it.name)
                }
                countersText.add(it.getNextSecond())
            }
        }, 0L, 20L)

        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (Bukkit.getOnlinePlayers().isEmpty()) return@Runnable

            val dTitle = title.getNextLine()
            val dLines = lines.map { it.getNextLine() }.toMutableList()
            val dFooter = footer.getNextLine()


            countersText.forEach {
                dLines.add(it)
            }
            dLines.add(dFooter)



            boards.values.forEach { board ->

                board.updateTitle(board.player.replacePlaceholdersForString(dTitle).colorize())
                board.updateLines(dLines.map { board.player.replacePlaceholdersForString(it).colorize() })
            }


        }, 0L, plugin.configurationManager.scoreboard.getLong("refresh"))
    }

    override fun unload() {
        boards.forEach { player, board ->
            board.delete()
            boards.remove(player)
        }
    }



    fun register(player: Player){
        val board = FastBoard(player)
        boards.put(player, board)
    }

    fun unregister(player: Player){
        boards[player]!!.delete()
    }


}