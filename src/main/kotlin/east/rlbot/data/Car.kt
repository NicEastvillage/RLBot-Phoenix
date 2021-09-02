package east.rlbot.data

import east.rlbot.math.OrientedCube
import east.rlbot.math.Vec3
import rlbot.flat.PlayerInfo

class Car(
        val index: Int,
        val team: Team,
        val name: String
) {
    lateinit var pos: Vec3
    lateinit var vel: Vec3
    lateinit var ori: Orientation
    lateinit var angVel: Vec3
    lateinit var hitboxCenter: Vec3
    lateinit var hitbox: OrientedCube

    var boost = 0
    var supersonic = false
    var wheelContact = false
    var demolished = false
    var isFirstFrameOfBeingDemolished = false

    var isUpright = true
    var isHuman = false

    /**
     * Returns target as seen from this players perspective.
     * x is distance forward of the car,
     * y is distance right of the car,
     * z is distance above the car.
     */
    fun toLocal(target: Vec3): Vec3 = ori.toLocal(target - pos)

    fun forwardSpeed(): Float = vel dot ori.forward

    fun update(player: PlayerInfo) {
        val phy = player.physics()
        pos = Vec3(phy.location())
        vel = Vec3(phy.velocity())
        ori = Orientation.fromEuler(phy.rotation().pitch(), phy.rotation().yaw(), phy.rotation().roll())
        angVel = Vec3(phy.angularVelocity().x(), phy.angularVelocity().y(), phy.angularVelocity().z())
        hitboxCenter = pos + ori.toGlobal(Vec3((player.hitboxOffset())))
        val hb = player.hitbox()
        hitbox = OrientedCube(ori, Vec3(hb.length(), hb.width(), hb.height()))

        boost = player.boost()
        supersonic = player.isSupersonic()
        wheelContact = player.hasWheelContact()
        val wasDemolished = demolished
        demolished = player.isDemolished()
        isFirstFrameOfBeingDemolished = !wasDemolished && demolished

        isUpright = ori.up dot Vec3.UP > 0.55

        isHuman = !player.isBot
    }

    companion object {
        const val REST_HEIGHT = 17f
        const val MAX_SPEED = 2300f
        const val MAX_THROTTLE_SPEED = 2300f
        const val COAST_ACC = 525f
        const val BRAKE_ACC = 3500f
        const val BOOST_BONUS_ACC = 991.66f
        const val THROTTLE_AIR_ACC = 66.66f

        const val BOOST_USAGE_RATE = 33.3f

        const val WALL_STICKY_FORCE = 325f
        const val WALL_STICKY_TIME = 3/120f
        const val JUMP_IMPULSE = 292f
        const val JUMP_HOLD_FORCE = 292f * 5
        const val MAX_JUMP_HOLD_TIME = 0.2f
    }
}