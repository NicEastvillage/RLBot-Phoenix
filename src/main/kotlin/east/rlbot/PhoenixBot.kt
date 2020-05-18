package east.rlbot

import east.rlbot.math.Mat3
import east.rlbot.training.AerialOrientateTraining
import java.awt.Color

class PhoenixBot(index: Int, team: Int, name: String) : BaseBot(index, team, name) {

    init {
        training = AerialOrientateTraining()
    }

    override fun getOutput(): OutputController {
        return drive.towards(
                data.ball.pos,
                targetSpeed = 2300f,
                boostMinimum = 0
        )
    }
}