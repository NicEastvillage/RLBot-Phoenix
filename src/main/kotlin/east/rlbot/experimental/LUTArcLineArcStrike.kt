package east.rlbot.experimental

import east.rlbot.data.Car
import east.rlbot.data.FutureBall
import east.rlbot.math.Mat3
import east.rlbot.math.Vec3
import east.rlbot.math.tangentPoint
import east.rlbot.simulation.AccelerationModel
import east.rlbot.simulation.turnRadius
import east.rlbot.util.DebugDraw
import east.rlbot.util.PIf
import java.awt.Color

data class AccAwareArcLineArc(
    val start: Vec3,
    val startDir: Vec3,
    val end: Vec3,
    val endDir: Vec3,
    val sign1: Float,
    val firstEndAngle: Float, // Global coordinates
    val secondStart: Vec3, // Point where second turn should start
    //val length: Float,
    //val duration: Float,
)

fun findAccAwareArcLineArc(
    car: Car,
    ball: FutureBall,
    target: Vec3,
    draw: DebugDraw,
    iterations: Int=15,
): AccAwareArcLineArc? {

    var i = 0
    val colors = listOf(Color.RED, Color.CYAN, Color.GREEN, Color.PINK)
    val signs = listOf(
        1f to 1f,
        1f to -1f,
        -1f to 1f,
        -1f to -1f,
    )

    val start = car.pos.flat()
    val startDir = car.ori.forward.dir2D()
    val end = ball.pos
    val endDir = (target - ball.pos).dir2D()

    val initSpeed = car.forwardSpeed()
    val initRadius2 = turnRadius(1200f)

    for ((sign1, sign2) in signs) {

        val initAngleDelta = sign1 * (startDir.atan2() - (end - start).atan2())
        var angleDelta1 = if (initAngleDelta >= 0f) initAngleDelta else initAngleDelta + 2 * PIf
        var radius2 = initRadius2

        for (i in 0 until iterations) {
            val end1: Vec3 = if (initSpeed > 1210f) {
                // Assume no acceleration
                val radius1 = turnRadius(initSpeed)
                val rot = Mat3.rotationMatrix(Vec3.UP, -sign1 * angleDelta1)
                car.pos - car.ori.right * radius1 * sign1 + (rot dot car.ori.right) * radius1 * sign1
            } else {
                val accResult = AccelerationModel.turnThrottle.simUntilLimit(initSpeed, angleLimit = angleDelta1)
                car.toGlobal(accResult.localDisplacement * Vec3(-1f, sign1, 1f))
            }

            val ballOri = Mat3.lookingInDir(endDir * -1f)
            val end1BallLocal = ballOri.transpose() dot (end1 - end)
            val end1BallLocalShifted = end1BallLocal - Vec3(y = sign2 * radius2)
            val tangentPointLocalShifted = tangentPoint(radius2, end1BallLocalShifted, sign2) ?: return null
            val tangentPointLocal = tangentPointLocalShifted + Vec3(y = sign2 * radius2)
            val start2 = end + (ballOri dot tangentPointLocal)

            val delta = sign1 * (startDir.atan2() - (start2 - end1).atan2())
            angleDelta1 = if (delta >= 0f) delta else delta + 2 * PIf
        }

        val end1: Vec3 = if (initSpeed > 1210f) {
            // Assume no acceleration
            val radius1 = turnRadius(initSpeed)
            val rot = Mat3.rotationMatrix(Vec3.UP, -sign1 * angleDelta1)
            car.pos - car.ori.right * radius1 * sign1 + (rot dot car.ori.right) * radius1 * sign1
        } else {
            val accResult = AccelerationModel.turnThrottle.simUntilLimit(initSpeed, angleLimit = angleDelta1)
            car.toGlobal(accResult.localDisplacement * Vec3(-1f, sign1, 1f))
        }

        val ballOri = Mat3.lookingInDir(endDir * -1f)
        val end1BallLocal = ballOri.transpose() dot (end1 - end)
        val end1BallLocalShifted = end1BallLocal - Vec3(y = sign2 * radius2)
        val tangentPointLocalShifted = tangentPoint(radius2, end1BallLocalShifted, sign2) ?: return null
        val tangentPointLocal = tangentPointLocalShifted + Vec3(y = sign2 * radius2)
        val start2 = end + (ballOri dot tangentPointLocal)

        val aaala = AccAwareArcLineArc(
            start,
            startDir,
            end,
            endDir,
            sign1,
            angleDelta1,
            start2,
        )

        draw.circle(end.withZ(Car.REST_HEIGHT) + ballOri.right() * radius2 * sign2, Vec3.UP, radius2, color = Color.LIGHT_GRAY)

        draw.color = colors[i]
        draw.rect3D(end1.withZ(Car.REST_HEIGHT), 7, 7)
        draw.rect3D(start2.withZ(Car.REST_HEIGHT), 7, 7)
        draw.polyline(listOf(
            start.withZ(Car.REST_HEIGHT),
            end1.withZ(Car.REST_HEIGHT),
            start2.withZ(Car.REST_HEIGHT),
            end.withZ(Car.REST_HEIGHT),
        ))
        i++
    }

    return null
}