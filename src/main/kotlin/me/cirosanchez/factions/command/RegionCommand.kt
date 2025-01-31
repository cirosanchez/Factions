package me.cirosanchez.factions.command

import com.mongodb.Block
import me.cirosanchez.clib.extension.send
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFile
import me.cirosanchez.clib.extension.sendColorizedMessageFromMessagesFileList
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.region.Region
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission
import kotlin.rem


@Command("region")
@CommandPermission("factions.command.region")
@Description("Region related commands.")
class RegionCommand {

    val mapsEnabled: HashMap<Player, Set<BlockDisplay>> = hashMapOf()

    @DefaultFor("~")
    fun def(actor: Player){
        actor.sendColorizedMessageFromMessagesFileList("region.default")
    }

    @Subcommand("info")
    fun info(actor: Player){
        actor.send("<gray><st>---------------------------------------------------</st></gray>")
        actor.send("")
    }

    @Subcommand("map")
    @CommandPermission("factions.command.region.map")
    fun map(actor: Player){
        val regions = Factions.get().regionManager.getRegionsInRadius(actor.location)

        val relatedRegions: HashMap<Region, Material> = hashMapOf()
        if (regions.isEmpty()){
            actor.sendColorizedMessageFromMessagesFile("map.no-regions-nearby")
            return
        }

        if (mapsEnabled.contains(actor)){

            val set = mapsEnabled.get(actor)!!
            set.forEach {
                it.remove()
            }

            actor.sendColorizedMessageFromMessagesFile("region.map.removed-map")
            return
        }

        val materials = listOf(
            Material.GOLD_BLOCK,
            Material.DIAMOND_BLOCK,
            Material.IRON_BLOCK,
            Material.REDSTONE_BLOCK,
            Material.LAPIS_BLOCK
        )
        regions.forEachIndexed { index, region ->
            if (region.cuboid == null){
                return@forEachIndexed
            }
            val world = actor.world
            val x1 = region.cuboid.x1
            val x2 = region.cuboid.x2
            val z1 = region.cuboid.z1
            val z2 = region.cuboid.z2
            val y = actor.location.y // Use the player's current altitude

            val corners = listOf(
                Location(world, x1.toDouble(), -64.0, z1.toDouble()),
                Location(world, x1.toDouble(), -64.0, z2.toDouble()),
                Location(world, x2.toDouble(), -64.0, z1.toDouble()),
                Location(world, x2.toDouble(), -64.0, z2.toDouble())
            )

            var count = -64.0

            val material = materials[index % materials.size]

            val displays: MutableSet<BlockDisplay> = mutableSetOf()

            corners.forEach {
                for (i in -65..320){
                    it.y = i.toDouble()
                    val display: BlockDisplay? = world.spawn(it, BlockDisplay::class.java, { entity ->
                        if (it.y % 6 == 0.0) {
                            entity.setBlock(material.createBlockData())
                            displays.add(entity)
                            if (!relatedRegions.contains(region)){
                                relatedRegions.put(region, material)
                            }
                            return@spawn
                        }
                        entity.setBlock(Material.GLASS.createBlockData())
                        displays.add(entity)
                    })
                }
            }
            mapsEnabled.put(actor, displays)
        }
        actor.send("<gray><st>---------------------------------------------------</st></gray>")
        relatedRegions.forEach { region, material ->
            actor.sendColorizedMessageFromMessagesFile("map.found-region", Placeholder("{region}", region.name),
                Placeholder("{material}", material.name))
        }
        actor.send("<gray><st>---------------------------------------------------</st></gray>")
    }
}