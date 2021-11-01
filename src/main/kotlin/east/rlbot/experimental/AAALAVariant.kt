package east.rlbot.experimental

import east.rlbot.math.*
import east.rlbot.simulation.AccelerationModel
import east.rlbot.simulation.DriveModel
import east.rlbot.simulation.timeSpentTurning
import east.rlbot.simulation.turnRadius
import east.rlbot.util.PIf

class AAALAVariant(
    val sign1: Float,
    val sign2: Float,
    val useBoost: Boolean,
    start: Vec3,
    startDir: Vec3,
    startSpeed: Float,
    boostTotal: Float,
    end: Vec3,
    endDir: Vec3,
    iterations: Int,
) {
    /**
     * Radius of second arc. Always better than current radius2 in aaala due to iterativeness.
     */
    private var radius2: Float

    /**
     * Angle turned in first arc. Always better than current angle1 in aaala due to iterativeness.
     */
    private var angle1: Float

    /**
     * Indicates that bot was too close to end2 and thus inside radius2 after the first turn
     */
    var bad = false; private set

    lateinit var aaala: AccAwareArcLineArc; private set

    init {
        radius2 = turnRadius(1000f)

        val initSignedAngle1 = sign1 * (startDir.atan2() - (end - start).atan2())
        angle1 = (if (initSignedAngle1 >= 0f) initSignedAngle1 else initSignedAngle1 + 2 * PIf) / 10f

        adjust(
            start,
            startDir,
            startSpeed,
            boostTotal,
            end,
            endDir,
            iterations,
        )
    }

    /**
     * Improve variant assuming only a small change in start and end configuration.
     */
    fun adjust(
        start: Vec3,
        startDir: Vec3,
        startSpeed: Float,
        boostTotal: Float,
        end: Vec3,
        endDir: Vec3,
        iterations: Int = 3,
    ) {
        val start1 = start
        val start1Dir = startDir
        val end2 = end
        val end2Dir = endDir

        val boostAvailable = if (useBoost) boostTotal else 0f

        val start1Ori = Mat3.lookingInDir(start1Dir)
        val end2Ori = Mat3.lookingInDir(end2Dir)

        for (i in 0 until iterations) {
            val (end1, speedAtEnd1, time1) = if (startSpeed > 1210f) {
                // Approximately at top speed
                val radius1 = turnRadius(startSpeed)
                val rot = Mat3.rotationMatrix(Vec3.UP, -sign1 * angle1)
                val end1 = start1 - start1Ori.right * radius1 * sign1 + (rot dot start1Ori.right) * radius1 * sign1
                val time1 = timeSpentTurning(startSpeed, angle1)
                Triple(end1, startSpeed, time1)
            } else {
                val accResult = AccelerationModel.turnThrottle.simUntilLimit(startSpeed, angleLimit = angle1)
                val end1 = start1Ori.toGlobal(accResult.localDisplacement * Vec3(-1f, sign1, 1f)) + start1
                Triple(end1, accResult.endSpeed, accResult.duration)
            }

            bad = false
            val end1BallLocal = end2Ori.transpose() dot (end1 - end2)
            val end1BallLocalShifted = end1BallLocal + Vec3(y = sign2 * radius2)
            val tangentPointLocalShifted = tangentPoint(radius2, end1BallLocalShifted, sign2) ?: Vec3.ZERO.also {
                // Point is inside radius. Let's try reducing it
                bad = true
                radius2 *= 0.9f
            }
            val tangentPointLocal = tangentPointLocalShifted - Vec3(y = sign2 * radius2)
            val start2 = end2 + (end2Ori dot tangentPointLocal)

            val signedAngle1 = sign1 * (start1Dir.atan2() - (start2 - end1).atan2())
            angle1 = lerpAng(angle1, if (signedAngle1 >= 0f) signedAngle1 else signedAngle1 + 2 * PIf, 0.8f)

            val lineLength = start2.dist(end1)
            val driveRes = DriveModel.drive1D(lineLength, speedAtEnd1, boostAvailable)
            radius2 = lerp(radius2, turnRadius(driveRes.endSpeed), 0.8f)

            val start2Dir = end1.dirTo2D(start2)
            val signedAngle2 = sign2 * (start2Dir.atan2() - end2Dir.atan2())
            val angle2 = if (signedAngle2 >= 0f) signedAngle2 else signedAngle2 + 2 * PIf
            val time2 = timeSpentTurning(driveRes.endSpeed, angle2)
            radius2 *= correctionForTurningWheels(angle2)

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
                time1,
                driveRes.timeSpent,
                time2,
                driveRes.boostUsed,
                driveRes.endSpeed,
            )
        }
    }

    private fun correctionForTurningWheels(ang: Float): Float {
        return 1f / (13f * ang + 3f) + 0.9f
    }
}