package east.rlbot.navigator

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.Player
import east.rlbot.math.Vec3
import east.rlbot.util.DebugDraw
import java.awt.Color


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

        val forwardDotTarget = car.ori.forward dot carToTarget.unit()
        val facingTarget = forwardDotTarget > 0.8

        val currentSpeed = car.vel dot carToTarget.unit()
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

        controls.withSteer(localTarget.unit().y * 5)

        return controls
    }

    fun reachable(pos: Vec3, time: Float): Boolean {
        // TODO
        val dist = bot.data.me.pos.dist(pos)
        val speed = dist / time
        return speed < 2300
    }
}