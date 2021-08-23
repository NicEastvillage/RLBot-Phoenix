package east.rlbot.navigator

import east.rlbot.BaseBot
import east.rlbot.maneuver.Maneuver
import east.rlbot.maneuver.strike.ChipStrike
import east.rlbot.math.Vec3
import east.rlbot.simulation.BallPredictionManager

class ShotFinder(val bot: BaseBot) {

    fun shootAt(
            target: Vec3
    ): Maneuver? {
        // TODO
        return null
    }

    fun findGroundStrike(): ChipStrike? {
        return BallPredictionManager.latest?.mapNotNull { ChipStrike.from(bot, it) }?.firstOrNull()
    }
}