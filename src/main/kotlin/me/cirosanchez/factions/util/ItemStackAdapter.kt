package me.cirosanchez.factions.util

import com.google.gson.*
import me.cirosanchez.clib.adapter.Adapter
import me.cirosanchez.clib.getPlugin
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.lang.reflect.Type
import java.util.*

class ItemStackAdapter : Adapter<ItemStack> {
    override fun getTypeClass(): Class<ItemStack> {
        return ItemStack::class.java
    }

    override fun serialize(
        itemStack: ItemStack?,
        p1: Type?,
        p2: JsonSerializationContext?
    ): JsonElement? {
        if (itemStack == null) throw JsonParseException("ItemStack cannot be null")
        return JsonObject().apply {
            addProperty("type", itemStack.type.name)
            addProperty("amount", itemStack.amount)
            // Meta
            if (itemStack.hasItemMeta()) {
                add("meta", JsonObject().apply {
                    val meta = itemStack.itemMeta
                    addProperty("displayName", meta.displayName)
                    addProperty("unbreakable", meta.isUnbreakable)
                    add("lore", GSON.toJsonTree(meta.lore()) as JsonArray)
                    add("enchants", JsonObject().apply {
                        meta.enchants.forEach { enchant ->
                            addProperty(enchant.key.key.key, enchant.value)
                        }
                    })
                    add("flags", GSON.toJsonTree(meta.itemFlags) as JsonArray)
                    if (meta is SkullMeta) {
                        add("skullData", JsonObject().apply {
                            addProperty("owner", meta.owner)
                        })
                    }
                })

            }
        }
    }

    override fun deserialize(
        json: JsonElement?,
        p1: Type?,
        p2: JsonDeserializationContext?
    ): ItemStack? {
        if (json == null) throw JsonParseException("JSON cannot be null.")
        if (json !is JsonObject) throw JsonParseException("Not a valid JSON Object.")

        val materialType = json.get("type")
        val amount = json.get("amount").asInt
        val meta = json.getAsJsonObject("meta")

        if (materialType == null) throw JsonParseException("Invalid JSON format, some required values are null.")


        if (!materialType.isJsonPrimitive && !(materialType as JsonPrimitive).isString) throw JsonParseException("\"type\" not of type string.")
        val material = enumValue<Material>(materialType.asString)
            ?: throw JsonParseException("Invalid JSON, Invalid Material Provided.")
        val builder = ItemBuilder(material, amount)


        // Meta stuff
        if (meta != null) {
            val displayName = meta.get("displayName")
            val lore = meta.getAsJsonArray("lore")
            val enchants = meta.getAsJsonObject("enchants")
            val flags = meta.getAsJsonArray("flags")
            val unbreakable = meta.get("unbreakable")

            if (unbreakable != null && unbreakable.isJsonPrimitive && (unbreakable as JsonPrimitive).isBoolean) {
                builder.unbreakable = unbreakable.asBoolean
            }

            if (displayName != null && displayName.isJsonPrimitive && (displayName as JsonPrimitive).isString) {
                builder.name = Component.text(displayName.asString)
            }

            if (lore != null) {
                builder.lore = lore.map { Component.text(it.asString) }.toMutableList()
            }

            if (enchants != null && !enchants.isEmpty) {
                enchants.asMap().forEach { (enchant, level) ->
                    builder.enchantments[Enchantment.getByName(enchant)!!] = level.asInt
                }
            }

            if (flags != null) {
                // ...
            }
        }

        return builder.build()
    }
}


class ItemBuilder(
    var type: Material,
    val amount: Int = 1,
    var name: Component? = null,
    var lore: MutableList<Component>? = null,
    var unbreakable: Boolean = false,
    val enchantments: MutableMap<Enchantment, Int> = HashMap(),
    var customModelData: Int? = null,
    var attributes: MutableMap<Attribute, AttributeModifier> = EnumMap(org.bukkit.attribute.Attribute::class.java),

    val persistentStrings: MutableMap<String, String> = HashMap<String, String>().apply {
        put(INTERACTION_UUID_KEY, uuidPdc())
    },

    val persistentInts: MutableMap<String, Int> = HashMap(),
    val persistentDoubles: MutableMap<String, Double> = HashMap(),
    val persistentFloats: MutableMap<String, Float> = HashMap(),
    val persistentLongs: MutableMap<String, Long> = HashMap(),
    val persistentBooleans: MutableMap<String, Boolean> = HashMap(),

    block: ItemBuilder.() -> Unit = {}
) {

    init {
        apply(block)
    }

    companion object {
        const val INTERACTION_UUID_KEY = "interaction_uuid"

        private val clickInteractions =
            mutableMapOf<String, HashMap<ItemInteraction, HashSet<PlayerInteractEvent.() -> Unit>>>()

        private val dropInteractions =
            mutableMapOf<String, HashSet<PlayerDropItemEvent.() -> Unit>>()

        init {
            fun invoke(item: ItemStack, interaction: ItemInteraction, event: Event) {
                when (interaction) {
                    ItemInteraction.RIGHT,
                    ItemInteraction.LEFT -> {
                        clickInteractions[item.getTwilightInteractUuid()]
                            ?.get(interaction)
                            ?.forEach { it.invoke(event as PlayerInteractEvent) }
                    }

                    ItemInteraction.DROP -> {
                        dropInteractions[item.getTwilightInteractUuid()]
                            ?.forEach { it.invoke(event as PlayerDropItemEvent) }
                    }
                }
            }

        }

        fun ItemStack.getTwilightInteractUuid() = itemMeta
            ?.persistentDataContainer
            ?.get(
                NamespacedKey(
                    getPlugin(),
                    INTERACTION_UUID_KEY
                ),
                PersistentDataType.STRING
            )

    }

    fun build(): ItemStack {
        ItemStack(type, amount).apply {
            itemMeta = itemMeta?.apply {
                this.isUnbreakable = this@ItemBuilder.unbreakable

                this.lore(this@ItemBuilder.lore)
                this.displayName(this@ItemBuilder.name)
                this.setCustomModelData(this@ItemBuilder.customModelData)

                this@ItemBuilder.enchantments
                    .forEach { (enchantment, level) -> addUnsafeEnchantment(enchantment, level) }

                this@ItemBuilder.attributes
                    .forEach { (attribute, modifier) -> addAttributeModifier(attribute, modifier) }

                persistentDataContainer.apply {
                    fun getKey(key: String) = NamespacedKey(getPlugin(), key)

                    persistentStrings.forEach { (key, value) -> set(getKey(key), PersistentDataType.STRING, value) }
                    persistentInts.forEach { (key, value) -> set(getKey(key), PersistentDataType.INTEGER, value) }
                    persistentDoubles.forEach { (key, value) -> set(getKey(key), PersistentDataType.DOUBLE, value) }
                    persistentFloats.forEach { (key, value) -> set(getKey(key), PersistentDataType.FLOAT, value) }
                    persistentLongs.forEach { (key, value) -> set(getKey(key), PersistentDataType.LONG, value) }
                    persistentBooleans.forEach { (key, value) -> set(getKey(key), PersistentDataType.BOOLEAN, value) }
                }
            }
        }.also { item -> return item }
    }

    private fun getInteractionKey(): String = persistentStrings[INTERACTION_UUID_KEY] as String

    fun onRightClick(block: PlayerInteractEvent.() -> Unit) {
        clickInteractions
            .getOrPut(getInteractionKey()) { HashMap() }
            .getOrPut(ItemInteraction.RIGHT) { HashSet() }
            .add(block)
    }

    fun onLeftClick(block: PlayerInteractEvent.() -> Unit) {
        clickInteractions
            .getOrPut(getInteractionKey()) { HashMap() }
            .getOrPut(ItemInteraction.LEFT) { HashSet() }
            .add(block)
    }

    fun onDrop(block: PlayerDropItemEvent.() -> Unit) {
        dropInteractions
            .getOrPut(getInteractionKey()) { HashSet() }
            .add(block)
    }
}

enum class ItemInteraction {
    RIGHT,
    LEFT,
    DROP,
}


data class PersistentData<Z>(
    val dataType: PersistentDataType<Z, Z>,
    val value: Z
)

fun uuidPdc(value: UUID = UUID.randomUUID()) = value.toString()

inline fun <reified T : Enum<T>> enumValue(value: String): T? {
    return enumValues<T>().find { it.name == value.uppercase() }
}
