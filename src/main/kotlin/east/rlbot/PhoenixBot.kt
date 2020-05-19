package east.rlbot

import east.rlbot.prediction.earliestReachBall

class PhoenixBot(index: Int, team: Int, name: String) : BaseBot(index, team, name) {

    override fun getOutput(): OutputController {

        val car = data.me
        val pred = earliestReachBall(data, car)
        if (pred != null) {

            val dist = car.pos.dist(pred.where)
            val speed = dist / pred.wen

            return drive.towards(
                    pred.where,
                    targetSpeed = speed,
                    boostMinimum = 0
            )

        } else {
            return drive.towards(
                    data.ball.pos,
                    targetSpeed = 2300f,
                    boostMinimum = 0
            )
        }
    }
}