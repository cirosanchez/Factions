package me.cirosanchez.factions.model.mine

import com.mojang.brigadier.ParseResults
import me.cirosanchez.clib.cuboid.Cuboid
import me.cirosanchez.clib.storage.Id
import me.cirosanchez.clib.storage.MongoSerializable
import me.cirosanchez.factions.model.region.Region
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe

data class Mine(@Id val name: String, var displayName: String, var block: ItemStack, var reward: ItemStack, var claim: Region?, var cuboid: Cuboid?, val permission: String, var regenTime: Long, var pvp: Boolean, val merchantRecipes: MutableList<MerchantRecipe>) : MongoSerializable {
}