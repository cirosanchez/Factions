package me.cirosanchez.factions.model.region

import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.region.util.RegionListener
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*

class RegionManager {


    lateinit var plugin: Factions

    var regions: MutableMap<World, MutableSet<Region>> = hashMapOf()

    val warzone = Region("<red>Warzone</red>", null, true, RegionType.WARZONE, UUID.fromString("9a5a6bd2-fa69-4f5a-b612-17d281123e97"))

    val wilderness = Region("<gray>Wilderness</gray>", null, true, RegionType.WILDERNESS, UUID.fromString("47d21880-f8b0-45d0-a25d-4a22fac6cd01"))

    fun load() {
        plugin = Factions.get()
        val worldsManager = plugin.worldManager
        plugin.server.pluginManager.registerEvents(RegionListener, plugin)

        regions.put(worldsManager.mainWorld, mutableSetOf())
        regions.put(worldsManager.wildernessWorld, mutableSetOf())
        regions.put(worldsManager.netherWorld, mutableSetOf())
        regions.put(worldsManager.endWorld, mutableSetOf())
        regions.put(worldsManager.eventsWorld, mutableSetOf())

        val regions = plugin.storageManager.readObjects(Region::class)
        for (region in regions){
            println(region.name )
        }
    }


    fun unload(){
        for (region in getAllRegions()){
            plugin.storageManager.saveObject<Region>(region)
        }
    }

    fun addRegion(region: Region){
        val world = region.cuboid!!.world
        val set = regions[world]!!
        set.add(region)
        regions.put(world, set)
    }

    fun getRegion(player: Player): Region? {
        return getRegion(player.location)
    }

    fun getRegion(location: Location): Region? {
        val world = location.world
        val set = regions[world] ?: run {
            plugin.logger.info("$location is in a unregistered world. PLEASE CONTACT SUPPORT")
            return null
        }

        val region = set.filter { it.cuboid!!.contains(location) }.firstOrNull()
        if (region == null && world.uid != plugin.worldManager.wildernessWorld.uid){
            return warzone
        }

        if (region == null && world.uid == plugin.worldManager.wildernessWorld.uid){
            return wilderness
        }

        return region
    }

    fun getRegion(string: String): Region? {
        regions.values.forEach {
            return it.filter { it.name == string }.firstOrNull()
        }
        return null
    }


    fun getRegions(type: RegionType): Set<Region> {
        val fset = mutableSetOf<Region>()

        for (world in regions.keys){
            val set = regions[world]
            for (region in set!!){
                if (region.type == type) fset.add(region)
            }
        }
        return fset
    }

    fun getRegions(location: Location): Set<Region> {
        val fset = mutableSetOf<Region>()

        for (world in regions.keys){
            val set = regions[world]
            for (region in set!!){
                if (region.cuboid!!.contains(location)) fset.add(region)
            }
        }
        return fset
    }

    fun getAllRegions(): Set<Region> {
        val fset = mutableSetOf<Region>()

        for (world in regions.keys){
            val set = regions[world]
            for (region in set!!){
                fset.add(region)
            }
        }
        return fset
    }

}