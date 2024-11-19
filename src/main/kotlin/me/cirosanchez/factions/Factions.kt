package me.cirosanchez.factions

import me.cirosanchez.clib.cLib
import me.cirosanchez.factions.command.SpawnCommand
import me.cirosanchez.factions.listener.PlayerListener
import me.cirosanchez.factions.model.region.RegionManager
import me.cirosanchez.factions.model.spawn.SpawnManager
import me.cirosanchez.factions.model.storage.StorageManager
import me.cirosanchez.factions.model.world.WorldManager
import me.cirosanchez.factions.util.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler


class Factions : JavaPlugin() {

    companion object {
        fun get() = getPlugin(Factions::class.java)
    }

    // Configuration
    lateinit var configFile: FileConfiguration

    // Managers
    lateinit var worldManager: WorldManager
    lateinit var regionManager: RegionManager
    lateinit var storageManager: StorageManager
    lateinit var spawnManager: SpawnManager
    lateinit var commandHandler: BukkitCommandHandler

    override fun onEnable() {

        cLib(this) {
            messages = true
        }

        // Initializing
        configFile = FileConfiguration("config.yml").load()


        worldManager = WorldManager()
        regionManager = RegionManager()
        storageManager = StorageManager()
        spawnManager = SpawnManager()
        commandHandler = BukkitCommandHandler.create(this)

        // Loading
        storageManager.load()
        worldManager.load()
        regionManager.load()
        commandHandler.register(SpawnCommand())

        // Listeners
        server.pluginManager.registerEvents(PlayerListener(), this)

    }

    override fun onDisable() {
        storageManager.unload()
    }


}
