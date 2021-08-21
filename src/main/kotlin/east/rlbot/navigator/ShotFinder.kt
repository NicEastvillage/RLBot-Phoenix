package east.rlbot.navigator

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.FutureBall
import east.rlbot.maneuver.Maneuver
import east.rlbot.maneuver.strike.GroundStrike
import east.rlbot.math.Vec3
import east.rlbot.prediction.BallPredictionManager
import rlbot.cppinterop.RLBotDll
import rlbot.cppinterop.RLBotInterfaceException

class ShotFinder(val bot: BaseBot) {

    fun shootAt(
            target: Vec3
    ): Maneuver? {
        // TODO
        return null
    }

    fun findGroundStrike(): GroundStrike? {
        return BallPredictionManager.latest?.mapNotNull { GroundStrike.from(bot, it) }?.firstOrNull()
    }
}