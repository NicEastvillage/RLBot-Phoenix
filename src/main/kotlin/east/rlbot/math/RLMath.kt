package east.rlbot.math

import east.rlbot.data.Ball
import east.rlbot.data.Car
import east.rlbot.data.FutureBall
import east.rlbot.simulation.AccelerationModel
import east.rlbot.simulation.StraightAccelerationLUT
import east.rlbot.simulation.timeSpentTurning
import kotlin.math.sqrt

operator fun Number.times(mat: Mat3): Mat3 = mat * this
operator fun Number.times(vec: Vec3): Vec3 = vec * this

fun clamp(value: Double, min: Double, max: Double) = value.coerceIn(min, max)
fun clamp(value: Float, min: Float, max: Float) = value.coerceIn(min, max)

/**
 * Linear interpolation
 */
fun lerp(a: Float, b: Float, t: Float) = (1f - t) * a + t * b

/**
 * Inverse linear interpolation
 */
fun invLerp(a: Float, b: Float, v: Float) = (v - a) / (b - a)

/**
 * Returns the intersection point of a 2D circle and a tangent. The circle is located at origin with the given radius
 * and the tangent goes through the given point.
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

data class HitParameters(
    val impulse: Vec3,
    val hitDir: Vec3,
    val carPos: Vec3,
    // carVel is any velocity where the component in the direction of the normal has a size equal to the impulse
)

/**
 * Approximation figuring out how to change the ball's velocity to the desired velocity.
 * TODO: Numeric method https://discord.com/channels/348658686962696195/535605770436345857/890257276076707950
 */
fun findHitParameters(ball: FutureBall, desiredBallVel: Vec3, carRadius: Float): HitParameters {
    // We assume that the car has a spherical hitbox and that RL has normal physics
    val deltaVel = desiredBallVel - ball.vel
    val normal = deltaVel.dir()
    val impulse = 2 * Ball.MASS * deltaVel / (Ball.MASS + Car.MASS)
    val carPos = ball.pos - (carRadius + Ball.RADIUS) * normal

    // carVel is any velocity where the component in the direction of the normal has a size equal to the impulse
    return HitParameters(
        impulse,
        normal,
        carPos,
    )
}