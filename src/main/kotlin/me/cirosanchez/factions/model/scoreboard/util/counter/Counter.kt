package me.cirosanchez.factions.model.scoreboard.util.counter

import me.cirosanchez.clib.extension.placeholders
import me.cirosanchez.clib.placeholder.Placeholder
import me.cirosanchez.factions.Factions
import me.cirosanchez.factions.model.koth.KoTH
import java.util.UUID

class Counter(val plugin: Factions, val name: String, var text: String, val time: Long, val isKoTH: Boolean) {
    val format = plugin.configurationManager.scoreboard.getString("timer-format") ?: "{text}<white>: {time}</white>"
    var remainingTime = time
    val id = UUID.randomUUID()

    fun getNextSecond(): String {
        var string = format

        val time = formatTime(remainingTime)

        val textPlaceholder = Placeholder("{text}", text)
        val timePlaceholder = Placeholder("{time}", time)

        return string.placeholders(textPlaceholder, timePlaceholder)
    }

    fun resetTime(){
        remainingTime = time
    }

    private fun formatTime(timeRemaining: Long): String {
        val hours = timeRemaining / 3600
        val minutes = (timeRemaining % 3600) / 60
        val seconds = timeRemaining % 60

        if (hours == 0L){
            return String.format("%02d:%02d", minutes, seconds)
        }

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}