package east.rlbot.math

operator fun Number.times(mat: Mat3): Mat3 = mat * this
operator fun Number.times(vec: Vec3): Vec3 = vec * this

fun clamp(value: Double, min: Double, max: Double) = value.coerceIn(min, max)
fun clamp(value: Float, min: Float, max: Float) = value.coerceIn(min, max)
