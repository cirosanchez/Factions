package me.cirosanchez.factions.util

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.*

object WorldCodec : Codec<World> {
    override fun encode(writer: BsonWriter, world: World, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        writer.writeString("_id", world.uid.toString())
        writer.writeString("name", world.name)
        writer.writeEndDocument()
    }

    override fun getEncoderClass(): Class<World>{
        return World::class.java
    }

    override fun decode(reader: BsonReader, context: DecoderContext?): World {
        reader.readStartDocument()
        val uid = UUID.fromString(reader.readString("_id"))
        val world = Bukkit.getWorld(uid)
        reader.readEndDocument()
        if (world == null){
            return Bukkit.getWorld(reader.readString("name"))!!
        }
        return world
    }

}