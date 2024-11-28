package me.cirosanchez.factions

import me.cirosanchez.clib.CLib.Companion.plugin
import me.cirosanchez.clib.adapter.Adapter
import me.cirosanchez.clib.adapter.impl.DurationTypeAdapter
import me.cirosanchez.clib.adapter.impl.LocationAdapter
import me.cirosanchez.clib.adapter.impl.WorldAdapter
import me.cirosanchez.clib.cLib
import me.cirosanchez.clib.exception.InvalidConfigurationException
import me.cirosanchez.factions.command.SpawnCommand
import me.cirosanchez.factions.listener.PlayerListener
import me.cirosanchez.factions.model.configuration.ConfigurationManager
import me.cirosanchez.factions.model.region.RegionManager
import me.cirosanchez.factions.model.spawn.SpawnManager
import me.cirosanchez.factions.model.storage.StorageManager
import me.cirosanchez.factions.model.team.Team
import me.cirosanchez.factions.model.team.TeamManager
import me.cirosanchez.factions.model.user.UserManager
import me.cirosanchez.factions.model.world.WorldManager
import me.cirosanchez.factions.util.InventoryTypeAdapter
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler


class Factions : JavaPlugin() {

    companion object {
        fun get() = getPlugin(Factions::class.java)
    }

    // Configuration
    val configurationManager = ConfigurationManager()

    // Managers
    val worldManager: WorldManager = WorldManager()
    val regionManager: RegionManager = RegionManager()
    val storageManager: StorageManager = StorageManager()
    val spawnManager: SpawnManager = SpawnManager()
    val userManager: UserManager = UserManager()
    val teamManager: TeamManager = TeamManager()
    lateinit var commandHandler: BukkitCommandHandler

    override fun onEnable() {

        // Initializing
        loadAllManagers()
        commandHandler = BukkitCommandHandler.create(this)
        commandHandler.register(SpawnCommand())

        // Listeners
        server.pluginManager.registerEvents(PlayerListener(), this)
    }

    override fun onDisable() {
        regionManager.unload()
        spawnManager.unload()
        userManager.unload()
        teamManager.unload()
    }


    fun loadAllManagers(){
        plugin = get()

        configurationManager.load()

        val uri = plugin.config.getString("mongo.uri")

        val dbName = this.configurationManager.config.getString("mongo.database")

        if (uri.isNullOrEmpty()) {
            plugin.logger.severe("MongoDB URI is not present. Shutting off.")
            Bukkit.getPluginManager().disablePlugin(plugin)
            throw InvalidConfigurationException("There's no connection URI in config.yml")
        }
        if (dbName.isNullOrEmpty()){
            plugin.logger.severe("MongoDB Database name is not present. Shutting off.")
            Bukkit.getPluginManager().disablePlugin(plugin)
            throw InvalidConfigurationException("There's no DB name in config.yml")
        }

        cLib(plugin) {
            messages = true
            mongoURI = uri
            mongoDB = dbName
            adapters = mutableListOf(LocationAdapter, WorldAdapter,
                DurationTypeAdapter(), InventoryTypeAdapter()) as MutableList<Adapter<Any>>
        }

        worldManager.load()
        regionManager.load()
        storageManager.load()
        spawnManager.load()
        userManager.load()
        teamManager.load()
    }

}