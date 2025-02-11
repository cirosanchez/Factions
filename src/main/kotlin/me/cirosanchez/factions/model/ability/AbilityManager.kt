package me.cirosanchez.factions.model.ability

import me.cirosanchez.clib.logger
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.model.ability.impl.antitrap.AntiTrapAbility
import me.cirosanchez.factions.model.ability.impl.antitrap.AntiTrapType
import me.cirosanchez.factions.model.ability.impl.boost.BoostAbility
import me.cirosanchez.factions.model.ability.impl.normal.NormalAbility
import me.cirosanchez.factions.model.ability.impl.projectile.ProjectileAbility
import me.cirosanchez.factions.model.ability.impl.projectile.ProjectileType
import org.bukkit.Material
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class AbilityManager : Manager {
    lateinit var plugin: Factions

    var durationPositive: Int = 0
    var durationNegative: Int = 0
    var durationProjectile: Int = 0

    val abilities: HashMap<String, Ability> = hashMapOf()

    override fun load() {
        plugin = Factions.get()
        loadAbilities()
        plugin.server.pluginManager.registerEvents(AbilityListener(), plugin)
    }

    override fun unload() {

    }

    fun loadAbilities(){
        val config = plugin.configurationManager.abilities
        durationPositive = config.getInt("duration-positive")
        durationNegative = config.getInt("duration-negative")
        durationProjectile = config.getInt("duration-projectile")

        val node = config.getConfigurationSection("abilities") ?: run {
            logger().warning("No abilities node in abilities.yml.")
            return
        }

        node.getKeys(false).forEach { key ->
            val child = node.getConfigurationSection(key)!!

            val name = child.getString("name")!!
            val displayName = child.getString("display-name")!!
            val lore = child.getStringList("lore")!!
            val material = Material.valueOf(child.getString("material")!!)
            val type = AbilityType.valueOf(child.getString("type")!!)

            when (type) {
                AbilityType.POSITIVE -> {
                    val effects: MutableList<PotionEffect> = mutableListOf()

                    val effectsChild = child.getConfigurationSection("effects")!!
                    effectsChild.getKeys(false).forEach { effectString ->
                        val power = effectsChild.getInt(effectString)
                        val potionEffect = PotionEffect(PotionEffectType.getByName(effectString)!!, config.getInt("duration-positive"), power, true, true, true)
                        effects.add(potionEffect)
                    }
                    val ability = NormalAbility(name, displayName, lore, material, type, effects, true, -1)
                    this.abilities.put(name, ability)
                }
                AbilityType.NEGATIVE -> {
                    val effects: MutableList<PotionEffect> = mutableListOf()
                    val radius = child.getInt("radius")

                    val effectsChild = child.getConfigurationSection("effects")!!
                    effectsChild.getKeys(false).forEach { effectString ->
                        val power = effectsChild.getInt(effectString)
                        val potionEffect = PotionEffect(PotionEffectType.getByName(effectString)!!, config.getInt("duration-negative"), power, true, true, true)
                        effects.add(potionEffect)
                    }
                    val ability = NormalAbility(name, displayName, lore, material, type, effects, true, radius)
                    this.abilities.put(name, ability)
                }
                AbilityType.BOOST -> {
                    val power = child.getInt("power")
                    val ability = BoostAbility(name, displayName, lore, material, type, power, true)
                    this.abilities.put(name, ability)
                }
                AbilityType.ANTI_TRAP -> {
                    val antiTrapType = AntiTrapType.valueOf(child.getString("anti-trap-type")!!)
                    when (antiTrapType) {
                        AntiTrapType.BLOCK -> {
                            val radius = child.getInt("radius")
                            val ability = AntiTrapAbility(name, displayName, lore, material, type, antiTrapType, radius, 0, true)
                            this.abilities.put(name, ability)
                        }
                        AntiTrapType.WAND -> {
                            val hits = child.getInt("hits")
                            val ability = AntiTrapAbility(name, displayName, lore, material, type, antiTrapType, 0, hits, true)
                            this.abilities.put(name, ability)
                        }
                    }
                }
                AbilityType.PROJECTILE -> {
                    val projectileType = ProjectileType.valueOf(child.getString("projectile-type")!!)
                    when (projectileType) {
                        ProjectileType.POTION -> {
                            val effects: MutableList<PotionEffect> = mutableListOf()

                            val effectsChild = child.getConfigurationSection("effects")!!
                            effectsChild.getKeys(false).forEach { effectString ->
                                val power = effectsChild.getInt(effectString)
                                val potionEffect = PotionEffect(PotionEffectType.getByName(effectString)!!, config.getInt("duration-positive"), power, true, true, true)
                                effects.add(potionEffect)
                            }

                            val ability = ProjectileAbility(name, displayName, lore, material, type, projectileType, effects, true)
                        }
                        ProjectileType.SWITCH -> {
                            val ability = ProjectileAbility(name, displayName, lore, material, type, projectileType, listOf(), true)
                            this.abilities.put(name, ability)
                        }
                    }
                }
            }
        }
    }
}