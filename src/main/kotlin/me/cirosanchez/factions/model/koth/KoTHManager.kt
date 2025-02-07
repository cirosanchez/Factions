package me.cirosanchez.factions.model.koth

import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import org.bukkit.Location

class KoTHManager : Manager {

    lateinit var plugin: Factions
    val koths: HashMap<String, KoTH> = hashMapOf()

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
    }



    fun getKoth(string: String) = this.koths.get(string)

    fun getKothByCuboid(loc: Location): KoTH? {
        koths.values.forEach {
            if (it.cuboid != null && it.cuboid.contains(loc)) return it
        }

        return null
    }


    fun getKothByRegion(loc: Location): KoTH? {
        koths.values.forEach {
            if (it.region != null && it.region.cuboid != null && it.region.cuboid.contains(loc)) return it
        }

        return null
    }
}