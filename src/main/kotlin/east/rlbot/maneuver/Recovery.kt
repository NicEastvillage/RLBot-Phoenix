package east.rlbot.maneuver

import east.rlbot.OutputController
import east.rlbot.data.DataPack
import east.rlbot.math.Mat3
import east.rlbot.math.Vec3
import east.rlbot.util.DebugDraw

class Recovery : Maneuver {
    override var done: Boolean = false

    override fun exec(data: DataPack): OutputController? {
        done = data.me.wheelContact
        val landingRotation = findLandingRotation(data)
        return data.bot.fly.align(landingRotation)
    }

    fun findLandingRotation(data: DataPack): Mat3 {
        // TODO Does NOT find landing position right now

        val dir = data.me.vel.flat().takeIf { !it.isZero }?.unit() ?: Vec3(1f, 0f, 0f)
        return Mat3.lookingInDir(dir)
    }
}