package east.rlbot.maneuver.strike

import east.rlbot.OutputController
import east.rlbot.data.*
import east.rlbot.maneuver.Recovery
import east.rlbot.math.Vec3
import east.rlbot.simulation.JumpModel
import java.awt.Color
import kotlin.math.min

class DodgeStrike(
    interceptBall: AdjustableFutureBall,
) : Strike(interceptBall) {

    override var done: Boolean = false

    private val dodgeStrikeDodge = DodgeStrikeDodge(interceptBall)

    override fun exec(data: DataPack): OutputController {
        val car = data.me

        val betterStrike = data.bot.shotFinder.findSoonestStrike(interceptBall.time - data.match.time)
        if (betterStrike != null) {
            data.bot.maneuver = betterStrike
        }

        if (!car.wheelContact)
            data.bot.maneuver = Recovery()

        interceptBall.adjust()

        // Find positions and directions
        val target = data.enemyGoal.middle
        val ballToTargetDir = interceptBall.pos.dirTo(target)
        val desiredBallVel = ballToTargetDir * min(interceptBall.vel.mag(), 750f)
        val arriveDir = (desiredBallVel - interceptBall.vel).dir()
        val arrivePos = (interceptBall.pos - arriveDir * (Ball.RADIUS + car.hitbox.size.x / 2f)).withZ(Car.REST_HEIGHT)

        // Find speed
        val timeLeft = interceptBall.time - data.match.time
        val speed = car.pos.dist(arrivePos) / timeLeft

        // Start dodge?
        if (dodgeStrikeDodge.canBegin(data))
            data.bot.maneuver = dodgeStrikeDodge

        done = timeLeft <= 0 || speed > Car.MAX_SPEED + 10f || (speed > Car.MAX_THROTTLE_SPEED + 10f && car.boost == 0) || !interceptBall.valid

        data.bot.draw.crossAngled(interceptBall.pos, 85f, Color.MAGENTA)
        data.bot.draw.line(car.pos, arrivePos, Color.CYAN)

        return data.bot.drive.towards(arrivePos, speed, 0)
    }

    companion object Factory : StrikeFactory {
        override fun tryCreate(data: DataPack, ball: FutureBall, target: Vec3): Strike? {
            val car = data.me
            if (Ball.RADIUS * 1.25f > ball.pos.z && ball.pos.y * data.bot.team.ysign > 0f) return null // If rolling, then only on opponent half
            if (JumpModel.single.maxHeight() + Ball.RADIUS / 5f < ball.pos.z) return null
            if (ball.vel.flat().mag() > 230f && ball.vel.angle2D(car.vel) < 1.3f) return null

            val ballToTargetDir = ball.pos.dirTo(target)
            val desiredBallVel = ballToTargetDir * min(ball.vel.mag(), 300f)
            val arriveDir = (desiredBallVel - ball.vel).dir()
            val arrivePos = (ball.pos - arriveDir * (Ball.RADIUS + car.hitbox.size.x / 2f)).withZ(Car.REST_HEIGHT)

            if ((data.bot.drive.estimateTime2D(arrivePos) ?: Float.MAX_VALUE) > ball.time - data.match.time) return null
            return DodgeStrike(ball.adjustable())
        }
    }
}