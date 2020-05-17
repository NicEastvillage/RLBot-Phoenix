package east.rlbot

import east.rlbot.math.Mat3
import east.rlbot.training.AerialOrientateTraining
import java.awt.Color

class PhoenixBot(index: Int, team: Int, name: String) : BaseBot(index, team, name) {

    init {
        training = AerialOrientateTraining()
    }

    override fun getOutput(): OutputController {
//        return drive.towards(
//                data.ball.pos,
//                targetSpeed = 2300f,
//                boostMinimum = 0
//        )

        val rot = Mat3.lookingAt(data.me.pos, data.ball.pos)

        renderer.drawLine3d(Color.red, data.me.pos, data.me.pos + rot.forward() * 150f)

        return fly.orientate(rot)
    }
}