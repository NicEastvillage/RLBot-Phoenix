package east.rlbot.states

import east.rlbot.OutputController
import east.rlbot.data.DataPack
import east.rlbot.math.clamp
import east.rlbot.navigator.ShotFinder
import east.rlbot.prediction.reachHeuristic
import east.rlbot.util.DebugDraw
import java.awt.Color

class BallChaseState : UtilityState {

    override fun utility(data: DataPack): Float {
        return clamp(1000f / data.me.pos.dist(data.ball.pos), 0f, 1f)
    }

    override fun exec(data: DataPack): OutputController {
        val groundStrike = data.bot.shotFinder.findGroundStrike()
        if (groundStrike != null) {
            data.bot.maneuver = groundStrike
            return groundStrike.exec(data)
        }

        val car = data.me
        val pred = reachHeuristic(data, car)
        if (pred != null) {

            val dist = car.pos.dist(pred.where)
            val speed = dist / pred.wen

            return data.bot.drive.towards(
                    pred.where,
                    targetSpeed = speed,
                    boostPreservation = 0
            )

        } else {
            return data.bot.drive.towards(
                    data.ball.pos,
                    targetSpeed = 2300f,
                    boostPreservation = 0
            )
        }
    }
}