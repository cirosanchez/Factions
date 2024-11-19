package me.cirosanchez.factions.model.world

import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.util.FileConfiguration
import me.cirosanchez.factions.util.InvalidConfigurationException
import org.bukkit.Bukkit
import org.bukkit.World

class WorldManager {
    var config: FileConfiguration = Factions.get().configFile

    lateinit var mainWorld: World
    lateinit var wildernessWorld: World
    lateinit var netherWorld: World
    lateinit var endWorld: World
    lateinit var eventsWorld: World

    fun load(){
        val worldsNode = config.getConfigurationSection("worlds")

        if (worldsNode == null){
            throw InvalidConfigurationException("'worlds' parent in config.yml isn't present. Please, check your config.yml and contact support.")
        }

        val mainWorldName = worldsNode.getString("main") ?: throw InvalidConfigurationException("'worlds.main' child in config.yml isn't present. Please, check config.yml and contact support.")
        val wildernessWorldName = worldsNode.getString("wilderness") ?: throw InvalidConfigurationException("'worlds.wilderness' child in config.yml isn't present. Please, check config.yml and contact support.")
        val netherWorldName = worldsNode.getString("nether") ?: throw InvalidConfigurationException("'worlds.nether' child in config.yml isn't present. Please, check config.yml and contact support.")
        val endWorldName = worldsNode.getString("end") ?: throw InvalidConfigurationException("'worlds.end' child in config.yml isn't present. Please, check config.yml and contact support.")
        val eventsWorldName = worldsNode.getString("events") ?: throw InvalidConfigurationException("'worlds.events' child in config.yml isn't present. Please, check config.yml and contact support.")


        Bukkit.getWorlds().forEach {
            println(it.name)
        }
        println("----------------------------------")
        println(mainWorldName)
        println(wildernessWorldName)
        println(netherWorldName)
        println(endWorldName)
        println(eventsWorldName)

        mainWorld = Bukkit.getWorld(mainWorldName) ?: throw InvalidConfigurationException("$mainWorldName isn't a world. Please, create the world and restart the server.")
        wildernessWorld = Bukkit.getWorld(wildernessWorldName) ?: throw InvalidConfigurationException("$wildernessWorldName isn't a world. Please, create the world and restart the server.")
        netherWorld = Bukkit.getWorld(netherWorldName) ?: throw InvalidConfigurationException("$netherWorldName isn't a world. Please, create the world and restart the server.")
        endWorld = Bukkit.getWorld(endWorldName) ?: throw InvalidConfigurationException("$endWorldName isn't a world. Please, create the world and restart the server.")
        eventsWorld = Bukkit.getWorld(eventsWorldName) ?: throw InvalidConfigurationException("$eventsWorldName isn't a world. Please, create the world and restart the server.")
    }
}