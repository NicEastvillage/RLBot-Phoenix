package east.rlbot.math

import east.rlbot.data.Car
import east.rlbot.util.DebugDraw
import java.awt.Color
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sign
import kotlin.math.sqrt

class ArcLineArc(
    val start: Vec3,
    val startDir: Vec3,
    val end: Vec3,
    val endDir: Vec3,
    val radius1: Float,
    val radius2: Float,
) {
    val circle1Center: Vec3
    val circle2Center: Vec3
    val startDirNormal: Vec3
    val endDirNormal: Vec3
    val tangentPoint1: Vec3
    val tangentPoint2: Vec3

    val angle1: Float
    val angle2: Float
    val length: Float

    init {
        val l1 = 1f
        val l2 = 1f

        val p1 = start + l1 * startDir
        startDirNormal = startDir.perp2D()
        circle1Center = p1 + startDirNormal * radius1

        val p2 = end - endDir * l2
        endDirNormal = endDir.perp2D()
        circle2Center = p2 + endDirNormal * radius2

        val centerDelta = circle2Center - circle1Center

        // Figure out if we transition from CW to CCW or vice versa
        // and compute some of the characteristic lengths for the problem
        val sign = -radius1.sign * radius2.sign
        val R = abs(radius1) + sign * abs(radius2)
        val centerDist = centerDelta.mag()
        val beta = 0.97f

        // Resize the radii if the circles are too close
//        if (R * R / (centerDist * centerDist) > beta) {
//            val deltaP = p2 - p1
//            val deltaN = (endDirNormal * radius2) - (startDirNormal * radius1)
//
//            val a = beta * deltaN.dot(deltaN) - R * R
//            val b = 2.0f * beta * deltaN.dot(deltaP)
//            val c = beta * deltaP.dot(deltaP)
//            val alpha = (-b - sqrt(b * b - 4.0f * a * c)) / (2.0f * a)
//
//            // Scale the radii by alpha, and update the relevant quantities
//            radius1 *= alpha
//            radius2 *= alpha
//            R *= alpha
//            circle1Center = p1 + startDirNormal * radius1
//            circle2Center = p2 + endDirNormal * radius2
//            centerDelta = circle2Center - circle1Center
//            centerDist = centerDelta.mag()
//        }

        // Set up a coordinate system along the axis
        // connecting the two circle's centers
        val e1 = centerDelta.dir()
        val e2 = -sign(radius1) * e1.perp2D()

        val H = sqrt(centerDist * centerDist - R * R)

        // The endpoints of the line segment connecting the circles
        tangentPoint1 = circle1Center + (e1 * (R / centerDist) + e2 * (H / centerDist)) * abs(radius1)
        tangentPoint2 = circle2Center - (e1 * (R / centerDist) + e2 * (H / centerDist)) * abs(radius2) * sign

        val pq1 = (tangentPoint1 - p1).dir()
        var _angle1 = 2.0f * sign(pq1 dot startDir) * asin(abs(pq1 dot startDirNormal))
        if (_angle1 < 0.0f) _angle1 += 2.0f * Math.PI.toFloat()
        angle1 = _angle1

        val pq2 = (tangentPoint2 - p2).dir()
        var _angle2 = -2.0f * sign(pq2 dot endDir) * asin(abs(pq2 dot endDirNormal))
        if (_angle2 < 0.0f) _angle2 += 2.0f * Math.PI.toFloat()
        angle2 = _angle2

        val arc1Length = angle1 * abs(radius1)
        val straightLength = tangentPoint2.dist(tangentPoint1)
        val arc2Length = angle2 * abs(radius2)
        length = arc1Length + straightLength + arc2Length
    }

    fun draw(draw: DebugDraw) {
        draw.color = Color.RED
        draw.circle(circle1Center.withZ(Car.REST_HEIGHT), Vec3.UP, abs(radius1))
        draw.line(tangentPoint1.withZ(Car.REST_HEIGHT), tangentPoint2.withZ(Car.REST_HEIGHT))
        draw.circle(circle2Center.withZ(Car.REST_HEIGHT), Vec3.UP, abs(radius2))
    }

    companion object {
        fun findShortest(
            start: Vec3,
            startDir: Vec3,
            end: Vec3,
            endDir: Vec3,
            radius1: Float,
            radius2: Float,
        ): ArcLineArc {
            var best: ArcLineArc? = null
            for (s1 in listOf(1f, -1f)) {
                for (s2 in listOf(1f, -1f)) {
                    val curve = ArcLineArc(start.flat(), startDir, end, endDir, s1 * radius1, s2 * radius2)
                    if (best == null || best.length > curve.length) best = curve
                }
            }
            return best!!
        }
    }
}