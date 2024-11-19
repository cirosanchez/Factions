package me.cirosanchez.factions.util

import me.cirosanchez.clib.configuration.Configuration
import me.cirosanchez.factions.Factions

class FileConfiguration(val string: String) : Configuration() {
    override fun getFileName(): String {
        return string
    }
    fun load(): FileConfiguration {
        super.loadConfig()
        return this

    }
}