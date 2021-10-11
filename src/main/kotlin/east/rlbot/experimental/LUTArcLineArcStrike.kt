package east.rlbot.experimental

import east.rlbot.data.Ball
import east.rlbot.data.Car
import east.rlbot.data.FutureBall
import east.rlbot.math.Mat3
import east.rlbot.math.Vec3
import east.rlbot.math.lerp
import east.rlbot.math.tangentPoint
import east.rlbot.simulation.AccelerationModel
import east.rlbot.simulation.DriveModel
import east.rlbot.simulation.timeSpentTurning
import east.rlbot.simulation.turnRadius
import east.rlbot.util.DebugDraw
import east.rlbot.util.PIf
import east.rlbot.util.half
import java.awt.Color

data class AccAwareArcLineArc(
    val start1: Vec3,
    val start1Dir: Vec3,
    val end2: Vec3,
    val end2Dir: Vec3,
    val start2: Vec3, // Point where second turn should start
    val start2Dir: Vec3,
    val end1: Vec3,
    val sign1: Float,
    val sign2: Float,
    val radius2: Float,
    val angle1: Float,
    //val length: Float,
    val duration: Float,
    val boostUsed: Float,
) {
    val end1Dir = start2Dir
}

fun findAccAwareArcLineArc(
    car: Car,
    ball: FutureBall,
    target: Vec3,
    draw: DebugDraw,
    iterations: Int=25,
): AccAwareArcLineArc? {

    // sign1, sign2, boostAvailable
    val setups = listOf(
        Triple(1f, 1f, car.boost),
        Triple(1f, 1f, 0),
        Triple(-1f, 1f, car.boost),
        Triple(-1f, 1f, 0),
        Triple(1f, -1f, car.boost),
        Triple(1f, -1f, 0),
        Triple(-1f, -1f, car.boost),
        Triple(-1f, -1f, 0),
    )

    val start1 = car.pos.flat()
    val start1Dir = car.ori.forward.dir2D()
    val end2Dir = (target - ball.pos).dir2D()
    val end2 = ball.pos.flat() - end2Dir * (Ball.RADIUS + car.hitbox.size.x / 2f)

    val ballOri = Mat3.lookingInDir(end2Dir * -1f)

    val initSpeed = car.forwardSpeed()
    val initRadius2 = turnRadius(1000f)

    val paths = setups.mapNotNull { (sign1, sign2, boostAvailable) ->

        val initSignedAngle1 = sign1 * (start1Dir.atan2() - (end2 - start1).atan2())
        var angle1 = (if (initSignedAngle1 >= 0f) initSignedAngle1 else initSignedAngle1 + 2 * PIf) / 10f
        var radius2 = initRadius2
        var aaala = AccAwareArcLineArc(start1, start1Dir, end2, end2Dir, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, sign1, sign2, radius2, angle1, 0f, boostAvailable.toFloat())

        for (i in 0 until iterations) {
            val (end1, speedAtEnd1, time1) = if (initSpeed > 1210f) {
                // Approximately at top speed
                val radius1 = turnRadius(initSpeed)
                val rot = Mat3.rotationMatrix(Vec3.UP, -sign1 * angle1)
                val end1 = car.pos - car.ori.right * radius1 * sign1 + (rot dot car.ori.right) * radius1 * sign1
                val time1 = timeSpentTurning(initSpeed, angle1)
                Triple(end1, initSpeed, time1)
            } else {
                val accResult = AccelerationModel.turnThrottle.simUntilLimit(initSpeed, angleLimit = angle1)
                val end1 = car.toGlobal(accResult.localDisplacement * Vec3(-1f, sign1, 1f))
                Triple(end1, accResult.endSpeed, accResult.duration)
            }

            val end1BallLocal = ballOri.transpose() dot (end1 - end2)
            val end1BallLocalShifted = end1BallLocal - Vec3(y = sign2 * radius2)
            val tangentPointLocalShifted = tangentPoint(radius2, end1BallLocalShifted, sign2) ?: return@mapNotNull null
            val tangentPointLocal = tangentPointLocalShifted + Vec3(y = sign2 * radius2)
            val start2 = end2 + (ballOri dot tangentPointLocal)

            val signedAngle1 = sign1 * (start1Dir.atan2() - (start2 - end1).atan2())
            angle1 = lerp(angle1, if (signedAngle1 >= 0f) signedAngle1 else signedAngle1 + 2 * PIf, 0.8f)

            val lineLength = start2.dist(end1)
            val driveRes = DriveModel.drive1D(lineLength, speedAtEnd1, boostAvailable.toFloat())
            radius2 = lerp(radius2, turnRadius(driveRes.endSpeed), 0.8f)

            val start2Dir = end1.dirTo2D(start2)
            val signedAngle2 = sign2 * (start2Dir.atan2() - end2Dir.atan2())
            val angle2 = if (signedAngle2 >= 0f) signedAngle2 else signedAngle2 + 2 * PIf
            val time2 = timeSpentTurning(driveRes.endSpeed, angle2)

            aaala = AccAwareArcLineArc(
                start1,
                start1Dir,
                end2,
                end2Dir,
                start2,
                start2Dir,
                end1,
                sign1,
                sign2,
                radius2,
                angle1,
                time1 + driveRes.timeSpent + time2,
                driveRes.boostUsed,
            )
        }

        aaala
    }

    val colors = listOf(Color.WHITE, Color.GREEN.half(), Color.CYAN.half(), Color.RED.half(), Color.MAGENTA.half(), Color.BLUE.half(), Color.YELLOW.half(), Color.PINK.half())
    for ((i, path) in paths.sortedBy { it.duration }.withIndex().reversed()) {
        //draw.circle(end2.withZ(Car.REST_HEIGHT) + ballOri.right() * path.radius2 * path.sign2, Vec3.UP, path.radius2, color = Color.GRAY)

        draw.color = colors[i]
        draw.rect3D(path.end1.withZ(Car.REST_HEIGHT), 7, 7)
        draw.rect3D(path.start2.withZ(Car.REST_HEIGHT), 7, 7)
        draw.polyline(listOf(
            start1.withZ(Car.REST_HEIGHT),
            path.end1.withZ(Car.REST_HEIGHT),
            path.start2.withZ(Car.REST_HEIGHT),
            end2.withZ(Car.REST_HEIGHT),
        ))
    }

    return null
}