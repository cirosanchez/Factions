package me.cirosanchez.factions.model.world

import com.mongodb.Block
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.world
import me.cirosanchez.clib.configuration.Configuration
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.util.InvalidConfigurationException
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player

class WorldManager {
    lateinit var config: Configuration

    lateinit var mainWorld: World
    lateinit var wildernessWorld: World
    lateinit var netherWorld: World
    lateinit var endWorld: World
    lateinit var eventsWorld: World

    val worlds: HashMap<World, String> = hashMapOf()

    fun load(){
        config = Factions.get().configurationManager.config
        val worldsNode = config.getConfigurationSection("worlds")

        if (worldsNode == null){
            throw InvalidConfigurationException("'worlds' parent in config.yml isn't present. Please, check your config.yml and contact support.")
        }

        val mainWorldName = worldsNode.getString("main.world-name") ?: throw InvalidConfigurationException("'worlds.main' child in config.yml isn't present. Please, check config.yml and contact support.")
        val wildernessWorldName = worldsNode.getString("wilderness.world-name") ?: throw InvalidConfigurationException("'worlds.wilderness' child in config.yml isn't present. Please, check config.yml and contact support.")
        val netherWorldName = worldsNode.getString("nether.world-name") ?: throw InvalidConfigurationException("'worlds.nether' child in config.yml isn't present. Please, check config.yml and contact support.")
        val endWorldName = worldsNode.getString("end.world-name") ?: throw InvalidConfigurationException("'worlds.end' child in config.yml isn't present. Please, check config.yml and contact support.")
        val eventsWorldName = worldsNode.getString("events.world-name") ?: throw InvalidConfigurationException("'worlds.events' child in config.yml isn't present. Please, check config.yml and contact support.")

        val mainWorldDisplayName = worldsNode.getString("main.world-name") ?: throw InvalidConfigurationException("'worlds.main' child in config.yml isn't present. Please, check config.yml and contact support.")
        val wildernessWorldDisplayName = worldsNode.getString("wilderness.world-name") ?: throw InvalidConfigurationException("'worlds.wilderness' child in config.yml isn't present. Please, check config.yml and contact support.")
        val netherWorldDisplayName = worldsNode.getString("nether.world-name") ?: throw InvalidConfigurationException("'worlds.nether' child in config.yml isn't present. Please, check config.yml and contact support.")
        val endWorldDisplayName = worldsNode.getString("end.world-name") ?: throw InvalidConfigurationException("'worlds.end' child in config.yml isn't present. Please, check config.yml and contact support.")
        val eventsWorldDisplayName = worldsNode.getString("events.world-name") ?: throw InvalidConfigurationException("'worlds.events' child in config.yml isn't present. Please, check config.yml and contact support.")

        mainWorld = Bukkit.getWorld(mainWorldName) ?: throw InvalidConfigurationException("$mainWorldName isn't a world. Please, create the world and restart the server.")
        wildernessWorld = Bukkit.getWorld(wildernessWorldName) ?: throw InvalidConfigurationException("$wildernessWorldName isn't a world. Please, create the world and restart the server.")
        netherWorld = Bukkit.getWorld(netherWorldName) ?: throw InvalidConfigurationException("$netherWorldName isn't a world. Please, create the world and restart the server.")
        endWorld = Bukkit.getWorld(endWorldName) ?: throw InvalidConfigurationException("$endWorldName isn't a world. Please, create the world and restart the server.")
        eventsWorld = Bukkit.getWorld(eventsWorldName) ?: throw InvalidConfigurationException("$eventsWorldName isn't a world. Please, create the world and restart the server.")


        worlds.put(mainWorld, mainWorldDisplayName)
        worlds.put(wildernessWorld, wildernessWorldDisplayName)
        worlds.put(netherWorld, netherWorldDisplayName)
        worlds.put(endWorld, endWorldDisplayName)
        worlds.put(eventsWorld, eventsWorldDisplayName)

    }

    fun getWorldName(player: Player): String {
        val name = worlds.get(player.world)

        if (name == null){
            return "<gray>Unrecognized</gray>"
        }

        return name
    }

    fun deleteBlockDisplaysInAllWorlds(){
        mainWorld.entities.forEach {
            if (it is BlockDisplay) {
                it.remove()
            }
        }

        endWorld.entities.forEach {
            if (it is BlockDisplay) {
                it.remove()
            }
        }

        netherWorld.entities.forEach {
            if (it is BlockDisplay) {
                it.remove()
            }
        }
        eventsWorld.entities.forEach {
            if (it is BlockDisplay) {
                it.remove()
            }
        }
        wildernessWorld.entities.forEach {
            if (it is BlockDisplay) {
                it.remove()
            }
        }
    }
}