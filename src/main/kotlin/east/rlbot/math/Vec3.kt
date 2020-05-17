package east.rlbot.math

import org.ejml.dense.row.CommonOps_FDRM
import rlbot.gamestate.DesiredVector3
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

class Vec3(x: Number = 0, y: Number = 0, z: Number = 0): rlbot.vector.Vector3(x.toFloat(), y.toFloat(), z.toFloat()) {

    constructor(flatVec: rlbot.flat.Vector3): this(flatVec.x(), flatVec.y(), flatVec.z())

    val isZero = (x == 0 && y == 0 && z == 0)

    operator fun plus(other: Vec3): Vec3 {
        return Vec3(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Vec3): Vec3 {
        return Vec3(x - other.x, y - other.y, z - other.z)
    }

    operator fun div(value: Number): Vec3 {
        return Vec3(x / value.toFloat(), y / value.toFloat(), z / value.toFloat())
    }

    operator fun div(value: Vec3): Vec3 {
        return Vec3(x / value.x, y / value.y, z / value.z)
    }

    operator fun times(value: Number): Vec3 {
        return Vec3(x * value.toFloat(), y * value.toFloat(), z * value.toFloat())
    }

    operator fun times(value: Vec3): Vec3 {
        return Vec3(x * value.x, y * value.y, z * value.z)
    }

    operator fun get(index: Int): Float {
        if (index == 0)
            return x
        if (index == 1)
            return y
        if (index == 2)
            return z
        return 0F
    }

    fun scaled(scale: Number): Vec3 {
        val s = scale.toFloat()
        return Vec3(x * s, y * s, z * s)
    }

    fun withX(x: Number): Vec3 {
        return Vec3(x, y, z)
    }

    fun withY(y: Number): Vec3 {
        return Vec3(x, y, z)
    }

    fun withZ(z: Number): Vec3 {
        return Vec3(x, y, z)
    }

    fun scaledToMag(magnitude: Number): Vec3 {
        if (isZero) {
            throw IllegalStateException("Cannot scale up a vector with length zero!")
        }
        val scaleRequired = magnitude.toFloat() / mag()
        return scaled(scaleRequired)
    }

    fun dist(other: Vec3): Float {
        return sqrt(distSqr(other))
    }

    fun distSqr(other: Vec3): Float {
        val xDiff = x - other.x
        val yDiff = y - other.y
        val zDiff = z - other.z
        return (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff)
    }

    fun mag(): Float {
        return sqrt(magSqr())
    }

    fun magSqr(): Float {
        return (x * x + y * y + z * z)
    }

    fun normalised(): Vec3 {
        if (isZero) {
            throw IllegalStateException("Cannot normalize a vector with length zero!")
        }
        return this.scaled(1 / mag())
    }

    infix fun dot(other: Vec3): Float {
        return x * other.x + y * other.y + z * other.z
    }

    infix fun dot(mat: Mat3): Vec3 {
        val ret = Mat3.emptyMatrix()
        val asMat = Mat3.toMatrix(this)
        CommonOps_FDRM.transpose(asMat)
        CommonOps_FDRM.mult(asMat, mat.internalMat, ret)
        return Mat3.toVec(ret)
    }

    fun flat(): Vec3 {
        return withZ(0)
    }

    fun angle(v: Vec3): Float {
        val mag2 = magSqr()
        val vmag2 = v.magSqr()
        val dot = this dot v
        return acos(dot / sqrt(mag2 * vmag2))
    }

    infix fun cross(v: Vec3): Vec3 {
        val tx = y * v.z - z * v.y
        val ty = z * v.x - x * v.z
        val tz = x * v.y - y * v.x
        return Vec3(tx, ty, tz)
    }

    fun projectToPlane(planeNormal: Vec3): Vec3 {
        val d = this dot planeNormal
        val antidote = planeNormal.scaled(-d)
        return plus(antidote)
    }

    fun abs(): Vec3 {
        return Vec3(abs(x), abs(y), abs(z))
    }

    fun toDesired(): DesiredVector3 = DesiredVector3(x, y, z)

    override fun toString(): String {
        return "(%.02f, %0.2f, %0.2f)".format(x, y, z)
    }

    override fun equals(other: Any?): Boolean {
        val o = other as? Vec3 ?: return false
        return o.x == x && o.y == y && o.z == z
    }

    companion object {
        val UP = Vec3(0.0, 0.0, 1.0)
        val DOWN = Vec3(0.0, 0.0, -1.0)
        val ZERO = Vec3()
    }
}
