package me.cirosanchez.factions.model.mine

import me.cirosanchez.clib.getPlugin
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.model.region.Region
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scheduler.BukkitTask

class MineManager : Manager {
    lateinit var plugin: Factions


    val mines: HashMap<String, Mine> = hashMapOf()
    val runnables: HashMap<Mine, BukkitTask> = hashMapOf()
    override fun load() {
        plugin = Factions.get()

        val minesdb = plugin.storageManager.readObjects<Mine>(Mine::class)

        minesdb.forEach { mine ->
            mines.put(mine.name, mine)

            val runnable = Runnable {

                if (mine.cuboid == null) {
                    getPlugin().logger.warning("Mine ${mine.name} doesn't have a mining region set. No blocks regenerated.")
                    return@Runnable
                }


                mine.cuboid!!.forEach {

                    if (mine.block == null) {
                        getPlugin().logger.warning("Mine ${mine.name} doesn't have a block set. No blocks regenerated.")
                        return@Runnable
                    }

                    if (it.type != mine.block.type) {
                        it.type = mine.block.type
                    }
                }
            }
            val task = Bukkit.getScheduler().runTaskTimer(Factions.get(), runnable, 0L, mine.regenTime * 20)

            runnables.put(mine, task)
        }

        plugin.server.pluginManager.registerEvents(MineListener(plugin), plugin)
    }

    override fun unload() {
        for (mine in mines.values) {
            plugin.storageManager.saveObject(mine)
        }
    }

    fun addMine(mine: Mine) {
        this.mines.put(mine.name, mine)
        val runnable = Runnable {

            if (mine.cuboid == null) {
                getPlugin().logger.warning("Mine ${mine.name} doesn't have a mining region set. No blocks regenerated.")
                return@Runnable
            }


            mine.cuboid!!.forEach {

                if (mine.block == null) {
                    getPlugin().logger.warning("Mine ${mine.name} doesn't have a block set. No blocks regenerated.")
                    return@Runnable
                }

                if (it.type != mine.block.type) {
                    it.type = mine.block.type
                }
            }
        }
        val task = Bukkit.getScheduler().runTaskTimer(Factions.get(), runnable, 0L, mine.regenTime * 20)
        this.runnables.put(mine, task)
    }

    fun getMine(name: String): Mine? {
        return mines[name]
    }

    fun getMine(loc: Location): Mine? {
        mines.values.forEach {
            if (it.claim == null || it.claim!!.cuboid == null) return@forEach

            if (it.claim!!.cuboid!!.contains(loc)) {
                return it
            }
        }
        return null
    }

    fun getMine(region: Region): Mine? {
        mines.values.forEach {
            if (it.claim == null) return@forEach

            if (it.claim == region) {
                return it
            }
        }
        return null
    }

    fun removeMine(mine: Mine) {
        mines.remove(mine.name)
        runnables.get(mine)!!.cancel()
    }

    fun reloadTask(mine: Mine, time: Long){
        runnables.get(mine)!!.cancel()
        runnables.remove(mine)

        val runnable = Runnable {

            if (mine.cuboid == null) {
                getPlugin().logger.warning("Mine ${mine.name} doesn't have a mining region set. No blocks regenerated.")
                return@Runnable
            }


            mine.cuboid!!.forEach {

                if (mine.block == null) {
                    getPlugin().logger.warning("Mine ${mine.name} doesn't have a block set. No blocks regenerated.")
                    return@Runnable
                }

                if (it.type != mine.block.type) {
                    it.type = mine.block.type
                }
            }
        }

        val task = Bukkit.getScheduler().runTaskTimer(Factions.get(), runnable, 0L, time)

        runnables.put(mine, task)
    }

    fun mineExists(name: String): Boolean {
        return mines.contains(name)
    }



}