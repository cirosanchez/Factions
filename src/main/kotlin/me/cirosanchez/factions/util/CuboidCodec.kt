package me.cirosanchez.factions.util

import me.cirosanchez.clib.cuboid.Cuboid
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bukkit.Bukkit
import java.util.UUID

object CuboidCodec : Codec<Cuboid> {
    override fun encode(writer: BsonWriter, cuboid: Cuboid, ctx: EncoderContext) {
        writer.writeStartDocument()
        writer.writeInt32("x1", cuboid.x1)
        writer.writeInt32("x2", cuboid.x2)
        writer.writeInt32("y1", cuboid.y1)
        writer.writeInt32("y2", cuboid.y2)
        writer.writeInt32("z1", cuboid.z1)
        writer.writeInt32("z2", cuboid.z2)
        writer.writeString("worldUid", cuboid.world.uid.toString())
        writer.writeEndDocument()
    }

    override fun getEncoderClass(): Class<Cuboid> {
        return Cuboid::class.java
    }

    override fun decode(reader: BsonReader, ctx: DecoderContext): Cuboid {
        reader.readStartDocument()
        val x1 = reader.readInt32("x1")
        val x2 = reader.readInt32("x2")
        val y1 = reader.readInt32("y1")
        val y2 = reader.readInt32("y2")
        val z1 = reader.readInt32("z1")
        val z2 = reader.readInt32("z2")
        val world = Bukkit.getWorld(UUID.fromString(reader.readString("worldUid")))
        reader.readEndDocument()
        val cuboid = Cuboid(world, x1, y1, z1, x2, y2, z2)
        return cuboid
    }
}