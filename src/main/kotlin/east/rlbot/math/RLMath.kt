package east.rlbot.math

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sign
import kotlin.math.sin

fun clamp(value: Double, min: Double, max: Double) = value.coerceIn(min, max)
fun clamp(value: Float, min: Float, max: Float) = value.coerceIn(min, max)

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
 * returns sign of x, and 0 if x == 0
 */
fun sign0(x: Number): Float = if (x == 0) x.toFloat() else sign(x.toFloat())