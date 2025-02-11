package me.cirosanchez.factions.model.ability

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

abstract class Ability(val name: String, val displayName: String, val lore: List<String>, val material: Material, val type: AbilityType, var enabled: Boolean) {
    fun toggle(): Boolean {
        enabled = !enabled
        return enabled
    }
    abstract fun getItemStack(): ItemStack
}