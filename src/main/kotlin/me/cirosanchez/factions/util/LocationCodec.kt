package me.cirosanchez.factions.util

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*

object LocationCodec : Codec<Location> {
    override fun encode(writer: BsonWriter, loc: Location, context: EncoderContext) {
        writer.writeStartDocument()
        writer.writeDouble("x", loc.x)
        writer.writeDouble("y", loc.y)
        writer.writeDouble("z", loc.z)
        writer.writeString("world", loc.world.uid.toString())
        writer.writeEndDocument()
    }

    override fun getEncoderClass(): Class<Location> {
        return Location::class.java
    }

    override fun decode(reader: BsonReader, context: DecoderContext?): Location{
        reader.readStartDocument()
        val x = reader.readDouble("x")
        val y = reader.readDouble("y")
        val z = reader.readDouble("z")
        val world = Bukkit.getWorld(UUID.fromString(reader.readString("world")))!!
        reader.readEndDocument()
        return Location(world, x, y, z)
    }
}