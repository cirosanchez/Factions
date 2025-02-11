package me.cirosanchez.factions.model.koth

import me.cirosanchez.clib.extension.placeholders
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.listener.PlayerListener
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.model.scoreboard.util.counter.Counter
import me.cirosanchez.factions.util.broadcastFromConfiguration
import me.cirosanchez.factions.util.getTeam
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scheduler.BukkitTask

class KoTHManager : Manager {

    lateinit var plugin: Factions
    lateinit var counter: Counter
    val koths: HashMap<String, KoTH> = hashMapOf()

    var activeKoth: KoTH? = null
    var bukkitTask: BukkitTask? = null
    var remainingTime = 0L
    var totalTimeInTicks = 0L


    override fun load() {
        plugin = Factions.get()
        val kothsFromDb = plugin.storageManager.readObjects<KoTH>(KoTH::class)

        kothsFromDb.forEach {
            koths.put(it.name, it)
        }
    }

    override fun unload() {
        koths.values.forEach {
            plugin.storageManager.saveObject(it)
        }
    }

    fun addKoTH(koTH: KoTH){
        this.koths.put(koTH.name, koTH)
    }

    fun removeKoTH(koTH: KoTH){
        this.koths.remove(koTH.name)
        koTH.delete().get()
        if (koTH.region != null) plugin.regionManager.removeRegion(koTH.region!!)
    }

    fun hasKoTH(string: String): Boolean {
        return koths.contains(string)
    }



    fun getKoth(string: String) = this.koths.get(string)

    fun getKothByCuboid(loc: Location): KoTH? {
        koths.values.forEach {
            if (it.cuboid != null && it.cuboid!!.contains(loc)) return it
        }

        return null
    }


    fun getKothByRegion(loc: Location): KoTH? {
        koths.values.forEach {
            if (it.region != null && it.region!!.cuboid != null && it.region!!.cuboid!!.contains(loc)) return it
        }

        return null
    }



    fun startKoth(koTH: KoTH){
        activeKoth = koTH
        totalTimeInTicks = koTH.time*20
        remainingTime = totalTimeInTicks
        var count = 0

        var format = Factions.get().configurationManager.config.getString("koth.timer-format") ?: "<aqua>{name}</aqua>"
        counter = Counter(plugin, koTH.name+"_KOTH", format.placeholders(Placeholder("{name}", koTH.name)), koTH.time+1, true)
        plugin.scoreboardManager.counterManager.addTimer(counter)

        bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (remainingTime <= 0){
                activeKoth = null
                bukkitTask!!.cancel()
                bukkitTask = null
                remainingTime = 0L

                broadcastKothCapped(activeKoth!!)

                koTH.commands.forEach {
                    plugin.server.dispatchCommand(Bukkit.getConsoleSender(), it)
                }
            }

            if (remainingTime % 600L == 0L && remainingTime != totalTimeInTicks){
                println(remainingTime % 600L)
                broadcastKothBeingCapped(activeKoth!!)
            }

            if (PlayerListener.playersInsideCapzone.isEmpty()){
                if (remainingTime != totalTimeInTicks){
                    remainingTime = totalTimeInTicks
                    broadcastKothLostCap(activeKoth!!)
                }
                counter.resetTime()
            }

            if (!PlayerListener.playersInsideCapzone.isEmpty()){
                remainingTime--
            }
        }, 0L, 1L)
    }


    fun stopKoTH(){

        Bukkit.getConsoleSender().sendColorizedMessageFromMessagesFile("koth.has-been-stopped", Placeholder("{koth}", activeKoth!!.name))

        this.activeKoth = null
        this.bukkitTask!!.cancel()
        this.bukkitTask = null
        this.remainingTime = 0L
        this.plugin.scoreboardManager.counterManager.removeTimer(this.counter.name)
        PlayerListener.playersInsideCapzone.clear()

    }




    fun broadcastKothCapped(koTH: KoTH){
        val player = PlayerListener.playersInsideCapzone.first()
        var teamString = "???"
        val team = player.getTeam()

        if (team != null) teamString = team.name

        team!!.koths++
        val points = plugin.configurationManager.config.getInt("koth.points")
        team.points += points

        broadcastFromConfiguration("koth.was-capped", Placeholder("{team}", teamString), Placeholder("{name}", player.name),
            Placeholder("{koth}", koTH.name))
    }

    fun broadcastKothBeingCapped(koTH: KoTH){
        broadcastFromConfiguration("koth.is-being-capped", Placeholder("{koth}", koTH.name))
    }

    fun broadcastKothLostCap(koTH: KoTH){
        broadcastFromConfiguration("koth.lost-cap", Placeholder("{koth}", koTH.name))
    }

}