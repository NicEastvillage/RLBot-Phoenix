package east.rlbot.data

import east.rlbot.math.Vec3
import rlbot.flat.BallInfo

class Ball {
    lateinit var pos: Vec3
    lateinit var vel: Vec3

    fun update(info: BallInfo) {
        val phy = info.physics()
        pos = Vec3(phy.location())
        vel = Vec3(phy.velocity())
    }
}