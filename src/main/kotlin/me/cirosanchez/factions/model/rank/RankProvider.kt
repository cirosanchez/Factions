package me.cirosanchez.factions.model.rank

import java.util.*

interface RankProvider {
    fun getName(): String
    fun getPrefix(uuid: UUID): String
    fun getRankName(uuid: UUID): String
}