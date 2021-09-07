package east.rlbot.navigator

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.Car
import east.rlbot.math.Vec3
import east.rlbot.simulation.StraightAccelerationLUT
import east.rlbot.simulation.AccelerationModel
import east.rlbot.simulation.timeSpentTurning


class SimpleDriving(val bot: BaseBot) {

    fun towards(
            target: Vec3,
            targetSpeed: Float,
            boostPreservation: Int // don't use boost if we are below this amount
    ): OutputController {

        val controls = OutputController()

        val car = bot.data.me

        // If we are on the wall, we choose a target that is closer to the ground.
        // This should make the bot go down the wall.
        val groundTarget = if (car.wheelContact && !car.isUpright)
            car.pos.flat().scaled(0.9)
        else target

        val carToTarget = groundTarget.minus(car.pos)
        val localTarget: Vec3 = car.toLocal(groundTarget)

        val forwardDotTarget = car.ori.forward dot carToTarget.dir()
        val facingTarget = forwardDotTarget > 0.8

        val currentSpeed = car.vel dot carToTarget.dir()
        if (currentSpeed < targetSpeed) {
            // We need to speed up
            controls.withThrottle(1.0)
            if (targetSpeed > 1410 && currentSpeed + 60 < targetSpeed && facingTarget && car.boost > boostPreservation) {
                controls.withBoost(true)
            }
        } else {
            // We are going too fast
            val extraSpeed = currentSpeed - targetSpeed
            controls.withThrottle(0.25 - extraSpeed / 500)
        }

        controls.withSteer(localTarget.dir().y * 5)

        return controls
    }

    /**
     * Estimate the minimum time needed to reach the given position from the current position
     */
    fun estimateTime2D(pos: Vec3, boostAvailable: Int = bot.data.me.boost): Float {
        // TODO Consider turning

        val car = bot.data.me
        var currentSpeed = car.forwardSpeed()

        // Turning, assuming constant speed TODO Find distance left after turning
        val dir = car.pos.dirTo(pos)
        val angle = car.ori.forward.angle2D(dir)
        val turnTime = timeSpentTurning(currentSpeed, angle)

        var distLeft = car.pos.dist2D(pos)
        var timeSpent = turnTime

        var accelerationResult: StraightAccelerationLUT.LookupResult? = null

        // Accelerate with boost
        if (boostAvailable > 0) {
            val boostTime = boostAvailable / Car.BOOST_USAGE_RATE
            accelerationResult = AccelerationModel.boost.simUntilLimit(currentSpeed, distanceLimit = distLeft, timeLimit = boostTime)
            distLeft -= accelerationResult.distance
            timeSpent += accelerationResult.duration
            currentSpeed = accelerationResult.endSpeed
        }

        // Accelerate with throttle
        if (distLeft > 0f && currentSpeed <= Car.MAX_THROTTLE_SPEED) {
            accelerationResult = AccelerationModel.throttle.simUntilLimit(currentSpeed, distanceLimit = distLeft)
            distLeft -= accelerationResult.distance
            timeSpent += accelerationResult.duration
            currentSpeed = accelerationResult.endSpeed
        }

        // If distance was not reached during acceleration, travel remain distance with constant speed
        if (accelerationResult == null || accelerationResult.distanceLimitReached)
            timeSpent += distLeft / currentSpeed

        return timeSpent * 1.02f
    }
}