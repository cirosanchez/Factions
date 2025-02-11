package me.cirosanchez.factions.model.rank.impl

import me.cirosanchez.factions.model.rank.RankProvider
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import java.util.UUID

class LuckPermsRankProvider : RankProvider {
    override fun getName(): String {
        return "LuckPerms"
    }

    override fun getPrefix(uuid: UUID): String {
        val api = LuckPermsProvider.get()
        val g = api.userManager.getUser(uuid)?.primaryGroup!!
        return api.groupManager.getGroup(g)!!.displayName!!
    }

    override fun getRankName(uuid: UUID): String {
        val api = LuckPermsProvider.get()
        val g = api.userManager.getUser(uuid)?.primaryGroup!!
        return g
    }
}