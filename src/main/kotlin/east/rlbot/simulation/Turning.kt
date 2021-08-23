package east.rlbot.simulation

fun turnRadius(forwardVel: Float): Float {
    if (forwardVel == 0f) return 0f
    return 1.0f / turnCurvature(forwardVel)
}

fun turnCurvature(forwardVel: Float): Float {
    if (0f <= forwardVel && forwardVel < 500f)
        return 0.006900f - 5.84e-6f * forwardVel
    if (500f <= forwardVel && forwardVel < 1000f)
        return 0.005610f - 3.26e-6f * forwardVel
    if (1000f <= forwardVel && forwardVel < 1500f)
        return 0.004300f - 1.95e-6f * forwardVel
    if (1500.0 <= forwardVel && forwardVel < 1750f)
        return 0.003025f - 1.1e-6f * forwardVel
    if (1750f <= forwardVel && forwardVel < 2500f)
        return 0.001800f - 4e-7f * forwardVel
    return 0f
}

/**
 * Returns the number of seconds spent turning the given angle assuming a constant forward velocity
 */
fun timeSpentTurning(forwardVel: Float, angle: Float): Float {
    return angle / forwardVel * turnCurvature(forwardVel)
}