package east.rlbot.navigator

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.math.Mat3
import kotlin.math.atan2
import kotlin.math.pow

class AerialMovement(val bot: BaseBot) {

    fun align(
            targetOri: Mat3
    ): OutputController {

        val controls = OutputController()

        val localForward = bot.data.me.ori.toLocal(targetOri.forward())
        val localUp = bot.data.me.ori.toLocal(targetOri.up())
        val localAngVel = bot.data.me.ori.toLocal(bot.data.me.angVel)

        val pitchAng = atan2(-localForward.z, localForward.x)
        val pitchAngVel = localAngVel.y

        val yawAng = atan2(-localForward.y, localForward.x)
        val yawAngVel = -localAngVel.z

        val rollAng = atan2(-localUp.y, localUp.z)
        val rollAngVel = localAngVel.x

        val Pp = -3.3f
        val Dp = 0.8f

        val Py = -3.3f
        val Dy = 0.9f

        //val rollScale = (targetOri.forward() dot bot.data.me.ori.forward).pow(16.0f)
        val rollScale = (targetOri.forward() dot bot.data.me.ori.forward).let { if (it > 0.85f) it.pow(2f) else 0f }
        val Pr = -3f
        val Dr = 0.5f

        controls
                .withPitch(Pp * pitchAng + Dp * pitchAngVel)
                .withYaw(Py * yawAng + Dy * yawAngVel)
                .withRoll((Pr * rollAng + Dr * rollAngVel) * rollScale)

        return controls
    }
}