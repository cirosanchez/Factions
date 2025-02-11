package me.cirosanchez.factions.model.rank.impl

import me.cirosanchez.factions.model.rank.RankProvider
import xyz.refinedev.phoenix.SharedAPI
import java.util.UUID

class PhoenixRankProvider : RankProvider {
    override fun getName(): String {
        return "Phoenix"
    }

    override fun getPrefix(uuid: UUID): String {
        val api = SharedAPI.getInstance()
        return api.profileHandler.getProfile(uuid)!!.highestRank.displayName
    }

    override fun getRankName(uuid: UUID): String {
        val api = SharedAPI.getInstance()
        return api.profileHandler.getProfile(uuid)!!.highestRank.name
    }
}