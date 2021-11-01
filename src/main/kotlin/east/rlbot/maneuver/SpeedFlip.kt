package east.rlbot.maneuver

import east.rlbot.OutputController
import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.math.Vec3
import east.rlbot.simulation.turnRadius

/**
 * A fast diagonal dodge that cancels the forward rotation for more speed.
 * Inspired by RedUtils by CodeRed: https://github.com/ItsCodeRed/RedUtils/blob/master/RedUtils/Actions/SpeedFlip.cs
 */
class SpeedFlip(val target: Vec3) : Maneuver {
    override var done = false

    private var startTime = -1f
    private var side = 1f

    override fun exec(data: DataPack): OutputController? {
        val car = data.me
        val dir = car.pos.dirTo2D(target)

        if (startTime < 0) {
            // Speed flips can only be performed while looking (almost) in the target direction
            // Are we looking in the right direction? Otherwise adjust
            val ang = 0.09f * car.forwardSpeed() / turnRadius(car.forwardSpeed())
            val leftDir = dir.rotate2D(ang)
            val rightDir = dir.rotate2D(-ang)
            val closest: Vec3

            if (car.vel dot leftDir > car.vel dot rightDir) {
                closest = leftDir
                if (car.vel.angle(leftDir) < 0.05f) {
                    startTime = data.match.time
                    side = 1f
                }
            } else {
                closest = rightDir
                if (car.vel.angle(rightDir) < 0.05f) {
                    startTime = data.match.time
                    side = -1f
                }
            }

            if (startTime < 0) {
                // If startTime is still -1 we need to steer towards the closest dodge dir
                return data.bot.drive.towards(car.pos + closest * 1000, Car.MAX_SPEED, 100)
            }

        }

        val elapsed = data.match.time - startTime
        val controls = OutputController()

        if (0 < elapsed && elapsed < 0.1f) {
            controls.withJump()
        } else if (0.12 < elapsed && elapsed < 0.15) {
            controls.withJump()
            controls.withPitch(-1f)
            controls.withRoll(0.95f * side)
        } else if (0.15 < elapsed && elapsed < 0.65) {
            // Cancel the forward part of the dodge, and continue air rolling
            controls.withPitch(1f)
            controls.withRoll(0.95f * side)
        } else if (.65 < elapsed && elapsed < 0.9) {
            // Land safely on the ground by turning slightly and holding drift
            controls.withPitch(1f)
            controls.withSlide()
            controls.withYaw(side)
        } else if (0.9 < elapsed) {
            done = true
        }

        return controls
    }
}