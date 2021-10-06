package east.rlbot.math

class AimCone(
    centerDir: Vec3,
    val angle: Float,
) {
    val centerDir = centerDir.dir()

    fun contains(dir: Vec3) = centerDir.angle(dir) <= angle

    fun clamp(dir: Vec3): Vec3 {
        val udir = dir.dir()
        val delta = centerDir.angle(dir)
        if (delta <= angle) return dir
        val adjustAng = delta - angle
        val axis = udir.cross(centerDir)
        val rot = Mat3.rotationMatrix(axis, adjustAng)
        return (rot dot udir) * dir.mag()
    }
}