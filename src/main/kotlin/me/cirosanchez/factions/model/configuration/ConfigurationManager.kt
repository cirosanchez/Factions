package me.cirosanchez.factions.model.configuration

import me.cirosanchez.clib.configuration.Configuration
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.util.FileConfiguration

class ConfigurationManager : Manager {

    lateinit var config: Configuration
    lateinit var scoreboard: Configuration

    override fun load() {
        config = FileConfiguration("config.yml").load()
        scoreboard = FileConfiguration("scoreboard.yml").load()
    }

    override fun unload() {
        // There's nothing in here, yay!
    }
}