package east.rlbot.maneuver.strike

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.DataPack
import east.rlbot.data.FutureBall
import east.rlbot.maneuver.Maneuver
import java.awt.Color
import kotlin.math.abs

class GroundStrike(
    var interceptBall: FutureBall,
) : Maneuver {

    override var done: Boolean = false

    override fun exec(data: DataPack): OutputController {
        val carToBallDir = (interceptBall.pos - data.me.pos).flat().unit()
        val arrivePos = (interceptBall.pos - carToBallDir * 100).withZ(17f)
        val timeLeft = interceptBall.time - data.match.time
        val speed = data.me.pos.dist(arrivePos) / timeLeft
        done = timeLeft <= 0 || !interceptBall.valid()

        data.bot.draw.crossAngled(interceptBall.pos, 85f, Color.GREEN)
        data.bot.draw.line(data.me.pos, arrivePos, Color.CYAN)

        return data.bot.drive.towards(arrivePos, speed, 0)
    }

    companion object {
        fun from(bot: BaseBot, ball: FutureBall): GroundStrike? {
            if (ball.pos.z > 190 - abs(ball.vel.z) / 5f || abs(ball.vel.z) > 280) return null
            if (ball.vel.flat().mag() > 400f && ball.vel.angle2D(bot.data.me.vel) < 1f) return null
            if (bot.drive.estimateTime2D(ball.pos) > ball.time - bot.data.match.time) return null
            return GroundStrike(ball)
        }
    }
}
