package east.rlbot.maneuver.strike

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.*
import east.rlbot.maneuver.Recovery
import java.awt.Color
import kotlin.math.min

class CatchIntoDribble(
    interceptBall: AdjustableFutureBall,
) : Strike(interceptBall) {

    override var done: Boolean = false; private set

    override fun exec(data: DataPack): OutputController {

        val betterStrike = data.bot.shotFinder.findSoonestStrike(interceptBall.time - data.match.time, listOf(CatchIntoDribble))
        if (betterStrike != null) {
            data.bot.maneuver = betterStrike
        }

        if (!data.me.wheelContact)
            data.bot.maneuver = Recovery()

        interceptBall.adjust()

        val target = data.enemyGoal.middle
        val ballToTargetDir = interceptBall.pos.dirTo(target)
        val desiredBallVel = ballToTargetDir * min(interceptBall.vel.mag(), 300f)

        val arriveDir = (desiredBallVel - interceptBall.vel).dir()
        val arrivePos = (interceptBall.pos - arriveDir * (Ball.RADIUS + data.me.hitbox.size.x / 2f)).withZ(Car.REST_HEIGHT)
        val timeLeft = interceptBall.time - data.match.time
        val aveSpeed = data.me.pos.dist(arrivePos) / timeLeft
        val speed = (aveSpeed - 100f) * 1.1f // TODO

        done = timeLeft <= 0 || aveSpeed > Car.MAX_SPEED + 10f || (aveSpeed > Car.MAX_THROTTLE_SPEED + 10f && data.me.boost == 0) || !interceptBall.valid

        data.bot.draw.crossAngled(interceptBall.pos, 85f, Color.YELLOW)
        data.bot.draw.line(data.me.pos, arrivePos, Color.CYAN)

        return data.bot.drive.towards(arrivePos, speed, 0)
    }

    companion object Factory : StrikeFactory {
        override fun tryCreate(bot: BaseBot, ball: FutureBall): Strike? {
            if (Ball.RADIUS + Car.REST_HEIGHT < ball.pos.z) return null

            val target = bot.data.enemyGoal.middle
            val ballToTargetDir = ball.pos.dirTo(target)
            val desiredBallVel = ballToTargetDir * min(ball.vel.mag(), 300f)
            val arriveDir = (desiredBallVel - ball.vel).dir()
            val arrivePos = (ball.pos - arriveDir * (Ball.RADIUS + bot.data.me.hitbox.size.x / 2f)).withZ(Car.REST_HEIGHT)

            if (bot.drive.estimateTime2D(arrivePos) ?: Float.MAX_VALUE > ball.time - bot.data.match.time) return null

            return CatchIntoDribble(ball.adjustable())
        }
    }
}