package me.cirosanchez.factions.util

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


fun <K, V> Map<K, V>.toHashMap() = HashMap(this)