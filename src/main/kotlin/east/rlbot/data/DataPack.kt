package east.rlbot.data

import east.rlbot.BaseBot
import rlbot.flat.GameTickPacket

class DataPack(val bot: BaseBot, val index: Int) {

    val matchInfo = MatchInfo()

    val me = Player(bot.index, bot.team, bot.name)

    val allPlayers = mutableListOf<Player>()
    val wholeTeam = mutableListOf<Player>() // includes us!
    val allies = mutableListOf<Player>()
    val enemies = mutableListOf<Player>()

    val ball = Ball()

    fun update(packet: GameTickPacket) {

        matchInfo.update(packet.gameInfo())
        ball.update(packet.ball())

        // Update players
        for (playerIndex in 0 until packet.playersLength()) {
            val playerInfo = packet.players(playerIndex)

            if (playerIndex >= allPlayers.size) {
                // We found a new player
                val newPlayer = if (playerIndex == index) me else Player(playerIndex, playerInfo.team(), playerInfo.name())

                // Add new player to relevant lists
                allPlayers += newPlayer
                if (newPlayer.team == bot.team) {
                    wholeTeam += newPlayer
                    if (playerIndex != index) {
                        allies += newPlayer
                    }
                } else {
                    enemies += newPlayer
                }
            }

            allPlayers[playerIndex].update(playerInfo)
        }
    }
}