package me.cirosanchez.factions.util

import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.region.Region
import org.bukkit.Location

fun Location.toPrettyString(): String {
    return "$x, $y, $z, $world"
}

fun Location.toPrettyStringWithoutWorld(): String {
    return "$x, $y, $z"
}

fun Location.toPrettyStringWithBrackets(): String {
    return "[$x, $y, $z]"
}

fun Location.getRegions(): Set<Region> {
    return Factions.get().regionManager.getRegions(this)
}

fun Location.getRegion(): Region? {
    return Factions.get().regionManager.getRegion(this)
}


fun <K, V> Map<K, V>.toHashMap() = HashMap(this)