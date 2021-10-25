package east.rlbot.experimental

import east.rlbot.OutputController
import east.rlbot.data.AdjustableAimedFutureBall
import east.rlbot.data.Ball
import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.maneuver.strike.Strike
import east.rlbot.maneuver.strike.StrikeFactory
import east.rlbot.util.DT
import java.awt.Color
import kotlin.math.abs

class AAALAStrike(
    val car: Car,
    aimedBall: AdjustableAimedFutureBall
) : Strike(aimedBall) {

    override var done: Boolean = false

    val aaaala: AdjustableAAALA

    var init = false
    var startTime = 0f
    var phase = 0

    init {
        val start = (car.pos + car.vel * 2 * DT).flat()
        val startDir = car.ori.forward.dir2D()
        val startSpeed = car.forwardSpeed()
        val shootDir = aimedBall.aimCone.clamp(aimedBall.pos - start).dir2D()
        val end = aimedBall.pos.flat() - shootDir * (Ball.RADIUS + car.hitbox.size.x / 2f + 8f)
        val endDir = aimedBall.aimCone.withAngle(aimedBall.aimCone.angle + 0.3f).clamp(aimedBall.pos - start).dir2D()

        aaaala = AAALAFactory.create(
            start,
            startDir,
            startSpeed,
            car.boost.toFloat(),
            end,
            endDir,
        )
    }

    override fun exec(data: DataPack): OutputController {

        val posSoon2D = car.pos.flat() + car.vel * DT

        if (!init) {
            init = true
            startTime = data.match.time
        }

        aimedBall.adjust()

        val start = (car.pos + car.vel * DT).flat()
        val startDir = car.ori.forward.dir2D()
        val startSpeed = car.forwardSpeed()
        val end = aimedBall.pos.flat() - aimedBall.aimCone.centerDir * (Ball.RADIUS + car.hitbox.size.x / 2f + 8f)
        val endDir = aimedBall.aimCone.clamp(aimedBall.pos - start).dir2D()

        aaaala.adjust(
            start,
            startDir,
            startSpeed,
            car.boost.toFloat(),
            end,
            endDir,
        )

        val path = aaaala.getBest()!!.aaala
        data.bot.draw.color = Color.WHITE
        path.draw(data.bot.draw)

        done = !aimedBall.valid || aimedBall.time - data.match.time - path.duration < -0.25f || path.duration.isNaN()

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

        val coast = aimedBall.time - data.match.time - path.duration > 0f
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

    class Factory(val car: Car) : StrikeFactory {
        override fun tryCreate(data: DataPack, aimedBall: AdjustableAimedFutureBall): Strike? {
            if (aimedBall.pos.z > 190 - abs(aimedBall.vel.z) / 5f || abs(aimedBall.vel.z) > 280) return null

            // Cheap overestimation of arrive time
            val minTime = car.pos.dist(aimedBall.pos) / Car.MAX_SPEED
            if (data.match.time + minTime > aimedBall.time) return null

            val strike = AAALAStrike(car, aimedBall)
            if (abs(aimedBall.time - data.match.time - strike.aaaala.getBest()!!.aaala.duration) < 0.04f)
                return strike
            return null
        }
    }
}