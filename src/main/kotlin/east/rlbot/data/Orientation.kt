package east.rlbot.data

import east.rlbot.math.Mat3
import east.rlbot.math.Vec3

class Orientation(val mat: Mat3) {

    val forward = mat.forward()
    val up = mat.up()
    val right = mat.right()

    /**
     * Returns target as seen from this orientation
     */
    fun toLocal(target: Vec3): Vec3 = mat.transpose() dot target

    /**
     * Returns local target in global orientation
     */
    fun toGlobal(target: Vec3): Vec3 = mat dot target

    companion object {
        fun fromEuler(pitch: Float, yaw: Float, roll: Float): Orientation {
            return Orientation(Mat3.eulerToRotation(pitch, yaw, roll))
        }
    }
}