package east.rlbot.math

fun clamp(value: Double, min: Double, max: Double) = value.coerceIn(min, max)
fun clamp(value: Float, min: Float, max: Float) = value.coerceIn(min, max)