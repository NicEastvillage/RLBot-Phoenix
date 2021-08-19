package east.rlbot.math

import kotlin.math.*

operator fun Number.times(mat: Mat3): Mat3 = mat * this
operator fun Number.times(vec: Vec3): Vec3 = vec * this

fun clamp(value: Double, min: Double, max: Double) = value.coerceIn(min, max)
fun clamp(value: Float, min: Float, max: Float) = value.coerceIn(min, max)

/**
 * Inverse of [axisToRotation]
 */
fun rotationToAxis(rot: Mat3): Vec3 {

    val ang = acos(clamp(0.5f * (rot.tr() - 1.0f), -1.0f, 1.0f))

    // For small angles, prefer series expansion to division by sin(theta) ~ 0
    val scale = if (abs(ang) < 0.00001) {
        0.5 + ang * ang / 12.0
    } else {
        0.5 * ang / sin(ang)
    }

    return Vec3(
            rot.get(2, 1) - rot.get(1, 2),
            rot.get(0, 2) - rot.get(2, 0),
            rot.get(1, 0) - rot.get(0, 1)
    ) * scale
}

/**
 * Constructs a rotation matrix that rotates around the given axis direction
 * by an amount equal to the length of axis vector.
 */
fun axisToRotation(axis: Vec3): Mat3 {
    val radians = axis.mag()
    return if (radians < 0.000001)
        Mat3.IDENTITY
    else {
        val axisU = axis.unit()
        val K = Mat3(floatArrayOf(
            0f, -axisU.z, axis.y,
            axis.z, 0f, -axisU.x,
            -axisU.y, axis.x, 0f,
        ))
        Mat3.IDENTITY + sin(radians) * K + (1f - cos(radians)) * (K dot K)
    }
}

/**
 * returns sign of x, and 0 if x == 0
 */
fun sign0(x: Number): Float = if (x == 0) x.toFloat() else sign(x.toFloat())