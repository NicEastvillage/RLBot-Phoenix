package east.rlbot.navigator

import east.rlbot.BaseBot
import east.rlbot.maneuver.Maneuver
import east.rlbot.maneuver.strike.ChipStrike
import east.rlbot.maneuver.strike.DodgeStrike
import east.rlbot.maneuver.strike.Strike
import east.rlbot.maneuver.strike.StrikeFactory
import east.rlbot.math.Vec3
import east.rlbot.simulation.BallPredictionManager
import east.rlbot.simulation.SLICES_PR_SEC

class ShotFinder(val bot: BaseBot) {

    fun shootAt(
            target: Vec3
    ): Maneuver? {
        // TODO
        return null
    }

    fun findSoonestStrike(timeLimit: Float = 6f, strikeFactories: List<StrikeFactory> = listOf(
        DodgeStrike,
        ChipStrike,
    )): Strike? {
        val slices = (SLICES_PR_SEC * timeLimit).toInt().coerceAtLeast(0)
        return BallPredictionManager.latest?.subList(0, slices)?.mapNotNull { ball ->
            strikeFactories.mapNotNull { it.tryCreate(bot, ball) }.firstOrNull()
        }?.firstOrNull()
    }
}