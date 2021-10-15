package east.rlbot.experimental

import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.math.*
import east.rlbot.simulation.DriveModel
import east.rlbot.simulation.timeSpentTurning
import east.rlbot.simulation.turnRadius
import east.rlbot.util.DebugDraw
import java.awt.Color
import kotlin.math.abs
import kotlin.math.sign

class NumericArcLineArcStrike(
    val car: Car,
    private var data: DataPack,
    private val radius2Sign: Float,
) {

    private val params = FloatArray(PARAM_COUNT)

    private val momentum = floatArrayOf(0f, 0f)
    private val epsilon = floatArrayOf(0.01f, 5f)
    private val descentRate = floatArrayOf(0.001f, 1f)
    private val regularization = floatArrayOf(0.95f, 1f)

    private var hitParam: HitParameters
    private lateinit var ala: ArcLineArc
    private var radius1: Float
    private var curForwardSpeed: Float
    var err = 0f

    init {
        val ball = data.ball
        curForwardSpeed = car.forwardSpeed().coerceAtLeast(10f)
        val ballLocal = car.toLocal(ball.pos)
        radius1 = ballLocal.y.sign * turnRadius(curForwardSpeed)

        hitParam = findHitParameters(ball.asFuture(), 4000f * ball.pos.dirTo(data.enemyGoal.pos), car.hitbox.size.x / 2f)

        reset()

        updateAla()
    }

    /**
     * If momentum or params becomes NaN we reset them both
     */
    private fun reset() {
        val ball = data.ball
        params[TIME] = DriveModel.drive1D(car.pos.dist(ball.pos), curForwardSpeed, car.boost.toFloat()).timeSpent
        params[RADIUS_2] = radius1

        for (i in 0 until PARAM_COUNT) {
            momentum[i] = 0f
        }
        updateAla()
    }

    /**
     * Improve params
     */
    fun adjust(data: DataPack, iterations: Int) {
        this.data = data
        val ball = data.ball
        curForwardSpeed = car.forwardSpeed().coerceAtLeast(10f)
        val ballLocal = car.toLocal(ball.pos)
        radius1 = ballLocal.y.sign * turnRadius(curForwardSpeed)
        hitParam =
            findHitParameters(ball.asFuture(), 4000f * ball.pos.dirTo(data.enemyGoal.pos), car.hitbox.size.x / 2f)

        if (params.any { it.isNaN() }) reset()

        // Gradient descent
        for (k in 0 until iterations) {
            val e = error()

            // Find gradient per parameter
            val gradient = FloatArray(PARAM_COUNT)
            for (i in 0 until PARAM_COUNT) {
                // Temporarily add small delta to parameter i to find how it affects the error
                params[i] += epsilon[i]
                updateAla()
                gradient[i] = (error() - e) / epsilon[i]
                params[i] -= epsilon[i]
            }

            // Update parameters to minimize error
            for (i in 0 until PARAM_COUNT) {
                momentum[i] = 0.5f * momentum[i] + gradient[i]
                params[i] = regularization[i] * params[i] - momentum[i] * descentRate[i]
            }

            // Turn radius should realistically never be more than max
            params[RADIUS_2] = params[RADIUS_2].coerceIn(radius1, Car.TURN_RADIUS_AT_MAX_SPEED)
        }

        updateAla()
        err = error()
    }

    /**
     * This is the error we want to minimize
     */
    private fun error(): Float {
        val timeSpentTurning1 = timeSpentTurning(curForwardSpeed, ala.angle1)
        val straight = DriveModel.drive1D(ala.straightLength, curForwardSpeed, car.boost.toFloat())
        val speedAtCirc2 = straight.endSpeed
        val deltaRadius = abs(abs(ala.radius2) - turnRadius(speedAtCirc2))
        val timeSpentTurning2 = timeSpentTurning(speedAtCirc2, ala.angle2)
        val timeTotal = timeSpentTurning1 + straight.timeSpent + timeSpentTurning2
        val hitImpulse = ((ala.endDir * speedAtCirc2) dot hitParam.hitDir) * hitParam.hitDir
        val hitImpulseDelta = (hitImpulse - hitParam.impulse).mag() // TODO Might have negative impact if speed is higher than desiredVel
        return 2f * deltaRadius + ala.arc2Length
    }

    private fun updateAla() {
        val cone = AimCone(hitParam.hitDir, 0.6f)
        val dir = cone.clamp(data.ball.pos - car.pos).dir2D()
        ala = ArcLineArc(
            car.pos.flat(),
            car.ori.forward.dir2D(),
            hitParam.carPos.flat(),
            dir,
            radius1,
            radius2Sign * params[RADIUS_2]
        )
    }

    fun draw(draw: DebugDraw) {
        draw.arcLineArc(ala)
        draw.line(ala.end.withZ(Car.REST_HEIGHT), (ala.end + ala.endDir * 300f).withZ(Car.REST_HEIGHT))
        val cone = AimCone(hitParam.hitDir, 0.6f)
        draw.aimCone(data.ball.pos, cone, color = Color.RED)
    }

    companion object {
        const val PARAM_COUNT = 2
        const val TIME = 0
        const val RADIUS_2 = 1
    }
}