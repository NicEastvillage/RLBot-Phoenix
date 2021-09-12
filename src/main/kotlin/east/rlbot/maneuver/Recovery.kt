package east.rlbot.maneuver

import east.rlbot.OutputController
import east.rlbot.data.Arena
import east.rlbot.data.DataPack
import east.rlbot.data.Orientation
import east.rlbot.math.Mat3
import east.rlbot.simulation.Physics

class Recovery : Maneuver {
    override var done: Boolean = false

    override fun exec(data: DataPack): OutputController {
        done = data.me.wheelContact
        val landingRotation = findLandingRotation(data)
        return data.bot.fly.align(landingRotation, upIsImportant = true)
    }

    fun findLandingRotation(data: DataPack): Mat3 {
        // TODO Does NOT find landing position right now

        var rb = data.me.rigidBody()
        var prevPos = rb.pos

        for (i in 0 until 100) {
            rb = Physics.fall(rb, 0.1f)
            if (!Arena.SDF.contains(rb.pos)) {
                val intersectPoint = rb.pos.lerp(prevPos, 0.5f)
                val normal = Arena.SDF.normal(intersectPoint)
                val fallDir = prevPos.dirTo(rb.pos)
                val right = fallDir cross normal
                val forward = normal cross right
                val ori = Mat3(forward, right, normal)
                data.bot.draw.orientedOrigin(intersectPoint, Orientation(ori))
                return ori
            }
            prevPos = rb.pos
        }

        // No wall/ground intersection found during fall
        // Default to looking in direction of velocity, but upright

        val dir = data.me.vel.flat().takeIf { !it.isZero }?.dir() ?: data.me.ori.forward
        return Mat3.lookingInDir(dir)
    }
}