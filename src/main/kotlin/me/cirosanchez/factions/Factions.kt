package me.cirosanchez.factions

import com.sun.org.apache.xml.internal.serializer.utils.Utils.messages
import me.cirosanchez.clib.CLib.Companion.plugin
import me.cirosanchez.clib.adapter.Adapter
import me.cirosanchez.clib.adapter.impl.DurationTypeAdapter
import me.cirosanchez.clib.cLib
import me.cirosanchez.clib.exception.InvalidConfigurationException
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler

import me.cirosanchez.factions.command.SpawnCommand
import me.cirosanchez.factions.listener.PlayerListener
import me.cirosanchez.factions.model.configuration.ConfigurationManager
import me.cirosanchez.factions.model.region.RegionManager
import me.cirosanchez.factions.model.spawn.SpawnManager
import me.cirosanchez.factions.model.storage.StorageManager
import me.cirosanchez.factions.model.user.UserManager
import me.cirosanchez.factions.model.world.WorldManager
import me.cirosanchez.factions.util.LocationAdapter
import me.cirosanchez.factions.util.WorldAdapter
import org.bukkit.Bukkit


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
    lateinit var commandHandler: BukkitCommandHandler

    override fun onEnable() {

        // Initializing
        loadCLib()
        commandHandler = BukkitCommandHandler.create(this)

        // Loading
        worldManager.load()
        regionManager.load()
        storageManager.load()
        spawnManager.load()
        userManager.load()
        commandHandler.register(SpawnCommand())

        // Listeners
        server.pluginManager.registerEvents(PlayerListener(), this)

    }

    override fun onDisable() {
        regionManager.unload()
        userManager.unload()
    }


    fun loadCLib(){
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
            adapters = mutableListOf(me.cirosanchez.clib.adapter.impl.LocationAdapter, me.cirosanchez.clib.adapter.impl.WorldAdapter,
                DurationTypeAdapter()) as MutableList<Adapter<Any>>
        }
    }

}
