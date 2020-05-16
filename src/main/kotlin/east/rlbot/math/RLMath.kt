package east.rlbot.math

fun clamp(value: Double, min: Double, max: Double) = value.coerceIn(min, max)