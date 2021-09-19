package east.rlbot.util

fun Boolean.toInt() = if (this) 1 else 0

fun Float.coerceIn01() = this.coerceIn(0f, 1f)