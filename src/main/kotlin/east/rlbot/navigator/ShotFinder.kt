package east.rlbot.navigator

import east.rlbot.BaseBot
import east.rlbot.maneuver.Maneuver
import east.rlbot.maneuver.strike.ChipStrike
import east.rlbot.maneuver.strike.DodgeStrike
import east.rlbot.maneuver.strike.Strike
import east.rlbot.math.Vec3
import east.rlbot.simulation.BallPredictionManager

class ShotFinder(val bot: BaseBot) {

    fun shootAt(
            target: Vec3
    ): Maneuver? {
        // TODO
        return null
    }

    fun findSoonestStrike(): Strike? {
        return BallPredictionManager.latest?.mapNotNull {
            DodgeStrike.tryCreate(bot, it) ?: ChipStrike.tryCreate(bot, it)
        }?.firstOrNull()
    }
}