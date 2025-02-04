package me.cirosanchez.factions.model.event

import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.model.region.Region
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import sun.jvm.hotspot.HelloWorld.e

class EventManager : Manager {

    lateinit var plugin: Factions

    val events: HashMap<String, Event> = hashMapOf()
    var activeEvent: Event? = null
    var timeRemaining: Long = 0
    var task: BukkitTask? = null
    var playerCapping: Player? = null
    var theresSomeoneCapping: Boolean = false

    override fun load() {
        plugin = Factions.get()
        val eventsFromDb = plugin.storageManager.readObjects<Event>(Event::class)

        eventsFromDb.forEach { event ->
            events.put(event.name, event)
        }
    }

    override fun unload() {
        events.values.forEach {
            it.save().get()
        }
    }

    fun addEvent(event: Event){
        events.put(event.name, event)
    }

    fun getEvent(name: String): Event? {
        return events.get(name)
    }

    fun getEvent(region: Region): Event? {
        return events.values.find { it.claim == region }
    }

    fun removeEvent(event: Event) {
        event.delete().get()
        events.remove(event.name)
    }

    fun startEvent(event: Event){
        activeEvent = event
        timeRemaining = event.time
        task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (theresSomeoneCapping && playerCapping == null){

            }

            timeRemaining -= 1
        }, 0L, 1L)
    }


    fun broadcastStoppingCapping(){
        
    }
}