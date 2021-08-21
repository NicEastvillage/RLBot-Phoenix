package east.rlbot.data

import east.rlbot.BaseBot
import east.rlbot.prediction.BallPredictionManager
import rlbot.flat.GameTickPacket

class DataPack(val bot: BaseBot, val index: Int) {

    val match = Match()

    val me = Player(bot.index, bot.team, bot.name)

    val allPlayers = mutableListOf<Player>()
    val wholeTeam = mutableListOf<Player>() // includes us!
    val allies = mutableListOf<Player>()
    val enemies = mutableListOf<Player>()

    val ball = Ball()

    val myGoal = Goal.get(bot.team)
    val enemyGoal = Goal.get(bot.team.other())

    fun update(packet: GameTickPacket) {

        ball.update(packet.ball())
        match.update(packet.gameInfo(), ball)

        BallPredictionManager.update(match.time)

        // Update players
        for (playerIndex in 0 until packet.playersLength()) {
            val playerInfo = packet.players(playerIndex)

            if (playerIndex >= allPlayers.size) {
                // We found a new player
                val newPlayer = if (playerIndex == index) me else Player(playerIndex, Team.get(playerInfo.team()), playerInfo.name())

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