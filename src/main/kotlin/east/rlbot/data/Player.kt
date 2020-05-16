package east.rlbot.data

import east.rlbot.math.Vec3
import rlbot.flat.PlayerInfo

class Player(
        val index: Int,
        val team: Int,
        val name: String
) {
    lateinit var pos: Vec3
    lateinit var vel: Vec3
    lateinit var ori: Orientation
    lateinit var angVel: Vec3

    var boost = 0
    var supersonic = false
    var wheelContact = false
    var demolished = false

    var isUpright = true

    /**
     * Returns target as seen from this players perspective.
     * x is distance forward of the car,
     * y is distance right of the car,
     * z is distance above the car.
     */
    fun toLocal(target: Vec3): Vec3 = ori.toLocal(target - pos)

    fun update(player: PlayerInfo) {
        val phy = player.physics()
        pos = Vec3(phy.location())
        vel = Vec3(phy.velocity())
        ori = Orientation.fromEuler(phy.rotation().pitch(), phy.rotation().yaw(), phy.rotation().roll())
        // angVel =

        boost = player.boost()
        supersonic = player.isSupersonic()
        wheelContact = player.hasWheelContact()
        demolished = player.isDemolished()

        isUpright = ori.up dot Vec3.UP > 0.55
    }
}