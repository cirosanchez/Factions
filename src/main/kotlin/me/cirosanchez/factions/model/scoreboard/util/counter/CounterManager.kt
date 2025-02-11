package me.cirosanchez.factions.model.scoreboard.util.counter

import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.model.scoreboard.util.counter.Counter

class CounterManager : Manager {


    lateinit var plugin: Factions



    val timers: HashMap<String, Counter> = hashMapOf()

    override fun load() {
        plugin = Factions.get()
    }


    fun addTimer(counter: Counter){
        timers.put(counter.name, counter)
    }


    fun removeTimer(name: String) = timers.remove(name)

    fun renameTimer(name: String, text: String){
        val timer = timers.get(name) ?: return
        timer.text = text
    }

    fun timerIsKoth(name: String): Boolean {
        val timer = timers.get(name) ?: return false

        return timer.isKoTH
    }

    fun hasTimer(name: String) = timers.contains(name)


    override fun unload() {

    }
}