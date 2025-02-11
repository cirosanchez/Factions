package me.cirosanchez.factions.model.rank

import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.Manager
import me.cirosanchez.factions.model.rank.impl.LuckPermsRankProvider
import me.cirosanchez.factions.model.rank.impl.PhoenixRankProvider

class RankManager : Manager {
    lateinit var rankProvider: RankProvider
    override fun load() {
        val name = Factions.get().configurationManager.config.getString("rank-provider")
        when (name) {
            "LuckPerms" -> {
                rankProvider = LuckPermsRankProvider()
            }
            "Phoenix" -> {
                rankProvider = PhoenixRankProvider()
            }
        }
    }

    override fun unload() {}
}