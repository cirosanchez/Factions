package me.cirosanchez.factions.model.ability.impl.projectile

import me.cirosanchez.clib.extension.colorize
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.ability.Ability
import me.cirosanchez.factions.model.ability.AbilityType
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect

class ProjectileAbility(name: String, displayName: String, lore: List<String>, material: Material, type: AbilityType, projectileType: ProjectileType, effects: List<PotionEffect>, enabled: Boolean) : Ability(name, displayName, lore, material, type, enabled){
    lateinit var item: ItemStack

    override fun getItemStack(): ItemStack {
        if (::item.isInitialized) {
            return item
        } else {
            val itemStack = ItemStack(material)
            val meta = itemStack.itemMeta

            meta.displayName(displayName.colorize())
            meta.lore(lore.map { it.colorize() })

            val pdc = meta.persistentDataContainer

            pdc.set(NamespacedKey(Factions.get(), "ABILITY-TYPE"), PersistentDataType.STRING, type.toString())
            pdc.set(NamespacedKey(Factions.get(), type.toString()), PersistentDataType.STRING, name)

            itemStack.itemMeta = meta

            item = itemStack
            return item
        }
    }
}