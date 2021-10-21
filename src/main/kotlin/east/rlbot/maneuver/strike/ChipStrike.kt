package east.rlbot.maneuver.strike

import east.rlbot.OutputController
import east.rlbot.data.AdjustableFutureBall
import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.data.FutureBall
import east.rlbot.maneuver.Recovery
import east.rlbot.math.Vec3
import java.awt.Color
import kotlin.math.abs
import kotlin.math.min

class ChipStrike(
    var car: Car,
    interceptBall: AdjustableFutureBall,
) : Strike(interceptBall) {

    override var done: Boolean = false

    override fun exec(data: DataPack): OutputController {

        val betterStrike = data.bot.shotFinder.findSoonestStrike(interceptBall.time - data.match.time)
        if (betterStrike != null) {
            data.bot.maneuver = betterStrike
        }

        if (!data.me.wheelContact)
            data.bot.maneuver = Recovery()

        interceptBall.adjust()

        val target = data.enemyGoal.middle
        val ballToTargetDir = interceptBall.pos.dirTo(target)
        val desiredBallVel = ballToTargetDir * min(interceptBall.vel.mag(), 750f)

        val arriveDir = (desiredBallVel - interceptBall.vel).dir()
        val arrivePos = (interceptBall.pos - arriveDir * 100).withZ(Car.REST_HEIGHT)
        val timeLeft = interceptBall.time - data.match.time
        val speed = data.me.pos.dist(arrivePos) / timeLeft
        done = timeLeft <= 0 || speed > Car.MAX_SPEED + 10f || (speed > Car.MAX_THROTTLE_SPEED + 10f && data.me.boost == 0) || !interceptBall.valid

        data.bot.draw.crossAngled(interceptBall.pos, 85f, Color.GREEN)
        data.bot.draw.line(data.me.pos, arrivePos, Color.CYAN)

        return data.bot.drive.towards(arrivePos, speed, 0)
    }

    companion object Factory : StrikeFactory {
        override fun tryCreate(data: DataPack, ball: FutureBall, target: Vec3): Strike? {
            val car = data.me
            if (ball.pos.z > 190 - abs(ball.vel.z) / 5f || abs(ball.vel.z) > 280) return null
            if (ball.vel.flat().mag() > 200f && ball.vel.angle2D(car.vel) < 1.3f) return null

            val ballToTargetDir = ball.pos.dirTo(target)
            val desiredBallVel = ballToTargetDir * min(ball.vel.mag(), 300f)
            val arriveDir = (desiredBallVel - ball.vel).dir()
            val arrivePos = (ball.pos - arriveDir * 100).withZ(Car.REST_HEIGHT)

            if ((data.bot.drive.estimateTime2D(arrivePos) ?: Float.MAX_VALUE) > ball.time - data.match.time) return null

            return ChipStrike(car, ball.adjustable())
        }
    }
}
