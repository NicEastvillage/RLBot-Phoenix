package east.rlbot.maneuver.strike

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.Ball
import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.data.FutureBall
import east.rlbot.maneuver.Dodge
import east.rlbot.maneuver.Recovery
import east.rlbot.math.Vec3
import east.rlbot.simulation.JumpModel
import java.awt.Color

class DodgeStrike(
    interceptBall: FutureBall,
) : Strike(interceptBall) {

    override var done: Boolean = false

    override fun exec(data: DataPack): OutputController {
        val car = data.me

        if (!car.wheelContact)
            data.bot.maneuver = Recovery()

        // Find speed
        val carToBallDir = (interceptBall.pos - car.pos).flat().dir()
        val arrivePos = (interceptBall.pos - carToBallDir * 100).withZ(Car.REST_HEIGHT)
        val timeLeft = interceptBall.time - data.match.time
        val speed = car.pos.dist(arrivePos) / timeLeft

        // Start dodge?
        val height = interceptBall.pos.z
        val result = JumpModel.single.simUntilLimit(heightLimit = height)
        val expectedDodgePos = car.pos + car.vel * (result.time + 0.03f) + Vec3(z=height)
        if (expectedDodgePos.dist(interceptBall.pos) < Ball.RADIUS + 40f) {
            data.bot.maneuver = Dodge(interceptBall.pos, firstJumpDuration = result.time)
        }

        done = timeLeft <= 0 || speed > Car.MAX_SPEED + 10f || (speed > Car.MAX_THROTTLE_SPEED + 10f && car.boost == 0) || !interceptBall.valid()

        data.bot.draw.crossAngled(interceptBall.pos, 85f, Color.MAGENTA)
        data.bot.draw.line(car.pos, arrivePos, Color.CYAN)

        return data.bot.drive.towards(arrivePos, speed, 0)
    }

    companion object Factory : StrikeFactory {
        override fun tryCreate(bot: BaseBot, ball: FutureBall): DodgeStrike? {
            if (JumpModel.single.maxHeight() + Ball.RADIUS / 3f < ball.pos.z) return null
            if (ball.vel.flat().mag() > 500f && ball.vel.angle2D(bot.data.me.vel) < 1f) return null
            if (bot.drive.estimateTime2D(ball.pos) > ball.time - bot.data.match.time) return null
            return DodgeStrike(ball)
        }
    }
}