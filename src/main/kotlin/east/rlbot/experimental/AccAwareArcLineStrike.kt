package east.rlbot.experimental

import east.rlbot.OutputController
import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.maneuver.Maneuver
import east.rlbot.util.DT

class AccAwareArcLineStrike(
    val path: AccAwareArcLineArc,
) : Maneuver {

    override var done: Boolean = false

    var init = false
    var boostAvailable = 0f
    var phase = 0

    override fun exec(data: DataPack): OutputController? {
        val car = data.me
        val posSoon = car.pos + car.vel * DT
        if (!init) {
            init = true
            boostAvailable = path.boostUsed
        }

        return when (phase) {
            0 -> {
                if (posSoon.dist(path.end1) < EPSILON) {
                    phase = 1
                }

                data.bot.drive.towards(path.end1, Car.MAX_THROTTLE_SPEED, 100)
            }
            1 -> {
                if (posSoon.dist(path.start2) < EPSILON) {
                    phase = 2
                }

                boostAvailable -= DT * Car.BOOST_USAGE_RATE

                data.bot.drive.towards(path.start2, path.speedAtStart2, 0)
                    .withBoost(boostAvailable > 0)
            }
            else -> {
                if (posSoon.dist(path.start2) < EPSILON) {
                    phase = 2
                    done = true
                }

                data.bot.drive.towards(path.end2, path.speedAtStart2, 100)
            }
        }
    }

    companion object {
        const val EPSILON = 45f
    }
}