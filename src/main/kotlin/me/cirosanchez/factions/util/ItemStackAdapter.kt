package me.cirosanchez.factions.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext

import me.cirosanchez.clib.adapter.Adapter
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Type
import java.util.*

class ItemStackAdapter : Adapter<ItemStack> {
    override fun deserialize(jsonElement: JsonElement, p1: Type?, p2: JsonDeserializationContext?): ItemStack {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(jsonElement.asString));
    }

    override fun getTypeClass(): Class<ItemStack> {
        return ItemStack::class.java
    }

    override fun serialize(src: ItemStack, p1: Type?, p2: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(Base64.getEncoder().encodeToString(src.serializeAsBytes()));
    }

}