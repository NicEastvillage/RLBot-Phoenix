package east.rlbot.experimental

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.*
import east.rlbot.maneuver.strike.Strike
import east.rlbot.maneuver.strike.StrikeFactory
import east.rlbot.math.AimCone
import east.rlbot.math.Vec3
import east.rlbot.util.DT
import kotlin.math.abs

class AccAwareArcLineStrike(
    car: Car,
    val ball: AdjustableFutureBall,
    val target: Vec3,
) : Strike(ball) {

    val carIndex = car.index

    override var done: Boolean = false

    val aaaala: AdjustableAAALA

    var init = false
    var startTime = 0f
    var phase = 0

    init {
        val start = car.pos.flat()
        val startDir = car.ori.forward.dir2D()
        val startSpeed = car.forwardSpeed()
        val shootDir = ball.pos.dirTo2D(target)
        val end = ball.pos.flat() - shootDir * (Ball.RADIUS + car.hitbox.size.x / 2f + 8f)

        aaaala = AdjustableAAALA(
            start,
            startDir,
            startSpeed,
            car.boost.toFloat(),
            end,
            shootDir,
        )
    }

    override fun exec(data: DataPack): OutputController {

        val car = data.allCars[carIndex]
        val posSoon2D = car.pos.flat() + car.vel * DT

        if (!init) {
            init = true
            startTime = data.match.time
        }

        ball.adjust()

        val start = car.pos.flat()
        val startDir = car.ori.forward.dir2D()
        val startSpeed = car.forwardSpeed()
        val shootDir = AimCone.atGoal(ball.pos, Goal[car.team.other()]).clamp(car.pos.dirTo2D(ball.pos))
        val end = ball.pos.flat() - shootDir * (Ball.RADIUS + car.hitbox.size.x / 2f + 30f)

        aaaala.adjust(
            start,
            startDir,
            startSpeed,
            car.boost.toFloat(),
            end,
            shootDir,
        )

        val path = aaaala.getBest()!!.aaala

        done = !ball.valid || ball.time - data.match.time - path.duration < -0.25f || path.duration.isNaN()

        val controls = when (phase) {
            0 -> {
                if (posSoon2D.dist(path.end1) <= EPSILON || startTime + path.arc1Duration + 0.3f <= data.match.time) {
                    phase = 1
                }

                data.bot.drive.towards(path.start2, Car.MAX_THROTTLE_SPEED, 100)
            }
            1 -> {
                if (posSoon2D.dist(path.start2) <= 1.5f * EPSILON) {
                    phase = 2
                }

                data.bot.drive.towards(path.start2, path.speedAtStart2, 0)
            }
            else -> {
                if (posSoon2D.dist(path.start2) <= EPSILON) {
                    phase = 2
                    done = true
                }

                data.bot.drive.towards(path.end2, path.speedAtStart2, 100)
            }
        }

        val coast = ball.time - data.match.time - path.duration > 0f
        if (coast) {
            // We are arriving too early, so we coast a bit
            controls.withThrottle(0.2f)
            controls.withBoost(false)
        }

        return controls
    }

    companion object {
        const val EPSILON = 40f
    }

    class Factory(val car: Car, val target: Vec3) : StrikeFactory {
        override fun tryCreate(bot: BaseBot, ball: FutureBall): Strike? {
            if (ball.pos.z > 190 - abs(ball.vel.z) / 5f || abs(ball.vel.z) > 280) return null

            // Cheap overestimation of arrive time
            val minTime = car.pos.dist(ball.pos) / Car.MAX_SPEED
            if (bot.data.match.time + minTime > ball.time) return null

            val strike = AccAwareArcLineStrike(car, ball.adjustable(), target)
            if (abs(ball.time - bot.data.match.time - strike.aaaala.getBest()!!.aaala.duration) < 0.05f)
                return strike
            return null
        }
    }
}