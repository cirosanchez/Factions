package me.cirosanchez.factions.model.spawn
import gg.flyte.twilight.data.Id
import gg.flyte.twilight.data.MongoSerializable

import org.bukkit.Location
import java.util.*

data class Spawn(@Id val uuid: UUID = UUID.fromString("1e08ba4c-55ef-4fb7-ae25-a682e77cee97"), var location: Location?): MongoSerializable