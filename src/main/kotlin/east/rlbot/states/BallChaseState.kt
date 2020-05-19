package east.rlbot.states

import east.rlbot.OutputController
import east.rlbot.data.DataPack
import east.rlbot.math.clamp
import east.rlbot.prediction.reachHeuristic

class BallChaseState : UtilityState {

    override fun utility(data: DataPack): Float {
        return clamp(1000f / data.me.pos.dist(data.ball.pos), 0f, 1f)
    }

    override fun exec(data: DataPack): OutputController {
        val car = data.me
        val pred = reachHeuristic(data, car)
        if (pred != null) {

            val dist = car.pos.dist(pred.where)
            val speed = dist / pred.wen

            return data.bot.drive.towards(
                    pred.where,
                    targetSpeed = speed,
                    boostMinimum = 0
            )

        } else {
            return data.bot.drive.towards(
                    data.ball.pos,
                    targetSpeed = 2300f,
                    boostMinimum = 0
            )
        }
    }
}