package east.rlbot.math

import kotlin.math.sqrt

operator fun Number.times(mat: Mat3): Mat3 = mat * this
operator fun Number.times(vec: Vec3): Vec3 = vec * this

fun clamp(value: Double, min: Double, max: Double) = value.coerceIn(min, max)
fun clamp(value: Float, min: Float, max: Float) = value.coerceIn(min, max)

/**
 * Returns the intersection point of a 2D circle located at origin with the given radius and the tangent goes through
 * the given point.
 * When [side] is 1 the right-side tangent is returned, and when [side] is -1 the left-side tangent is returned.
 * `null` is returned when the given point is inside the radius, since no tangents exists in that case.
 */
fun tangentPoint(radius: Float, point: Vec3, side: Float = 1f): Vec3? {
    // https://en.wikipedia.org/wiki/Tangent_lines_to_circles#With_analytic_geometry
    val point2D = point.flat()
    val distSqr = point2D.magSqr()
    if (distSqr < radius * radius) return null // point is inside radius
    val offsetTowardsPoint = point2D * radius * radius / distSqr
    return offsetTowardsPoint - Vec3(-point2D.y, point2D.x) * side * radius * sqrt(distSqr - radius * radius) / distSqr
}