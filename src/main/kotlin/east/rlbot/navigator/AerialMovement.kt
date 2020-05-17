package east.rlbot.navigator

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.math.*
import kotlin.math.abs

class AerialMovement(val bot: BaseBot) {

    companion object {
        private const val ALPHA_MAX = 9.0f
    }

    fun orientate(
            targetOri: Mat3,
            epsilonAngVel: Float = 0.01f,
            epsilonRotation: Float = 0.04f
    ): OutputController {
        val controls = OutputController()

        val dt = bot.data.matchInfo.dt
        val car = bot.data.me

        val relativeRotation = car.ori.mat.transpose() dot targetOri
        val geodesicLocal = rotationToAxis(relativeRotation);

        // Figure out the axis of minimal rotation to target
        val geodesicWorld = car.ori.mat dot geodesicLocal

        // The angular acceleration
        var alpha = Vec3(
                controller(geodesicWorld.x, car.angVel.x, dt),
                controller(geodesicWorld.y, car.angVel.y, dt),
                controller(geodesicWorld.z, car.angVel.z, dt)
        )

        // Reduce the corrections for when the solution is nearly converged
        val g = geodesicWorld.abs() + car.angVel.abs()
        alpha *= Vec3(q(g.x), q(g.y), q(g.z))

        // The desired next angular velocity
        val angVelNext = car.angVel + alpha * dt

        // determine the controls that produce that angular velocity
        val rpy = aerialPPY(car.angVel, angVelNext, car.ori.mat, dt)
        controls.withRoll(rpy.x)
        controls.withPitch(rpy.y)
        controls.withYaw(rpy.z)

        controls.withThrottle(1f)

        return controls
    }

    private fun controller(delta: Float, angv: Float, dt: Float): Float {
        val ri = r(delta, angv)
        var alpha = sign0(ri) * ALPHA_MAX
        val rf = r(delta - angv * dt, angv + alpha * dt)

        // use a single step of secant method to improve
        // the acceleration when residual changes sign
        if (ri * rf < 0.0f)
            alpha *= 2.0f * (ri / (ri - rf)) - 1f

        return alpha
    }

    private fun r(delta: Float, angv: Float): Float = delta - 0.5f * sign0(angv) * angv * angv / ALPHA_MAX

    private fun q(x: Float): Float = 1f - (1f / (1f + 500f * x * x))

    private fun aerialPPY(angVelStart: Vec3, angVelNext: Vec3, rot: Mat3, dt: Float): Vec3 {

        // Car's moment of inertia (spherical symmetry)
        val J = 10.5f

        // Aerial control torque coefficients
        val T = Vec3(-400.0f, -130.0f, 95.0f)

        // Aerial damping torque coefficients
        val H = Vec3(-50.0, -30.0, -20.0)

        // Angular velocities in local coordinates
        val w0Local = angVelStart dot rot
        val w1Local = angVelNext dot rot

        // PWL equation coefficients
        val ax = T.x * dt  / J
        val ay = T.y * dt  / J
        val az = T.z * dt  / J
        val bx = 0f // RL treats roll damping differently
        val by = -w0Local.y * H.y * dt / J
        val bz = -w0Local.z * H.z * dt / J
        val cx = w1Local.x - (1f + H.x * dt / J) * w0Local.x
        val cy = w1Local.y - (1f + H.y * dt / J) * w0Local.y
        val cz = w1Local.z - (1f + H.z * dt / J) * w0Local.z

        return Vec3(
                solvePWL(ax, bx, cx),
                solvePWL(ay, by, cy),
                solvePWL(az, bz, cz)
        )
    }

    /**
     * Solves a piecewise linear (PWL) equation of the form
     *
     * a x + b | x | + (or - ?) c == 0
     *
     * for -1 <= x <= 1. If no solution exists, this returns
     * the x value that gets closest
     */
    private fun solvePWL(a: Float, b: Float, c: Float): Float {
        val xp = if (abs(a + b) > 10e-6) c / (a + b) else -1f
        val xm = if (abs(a - b) > 10e-6) c / (a - b) else 1f

        if (xm <= 0f && 0f <= xp) {
            if (abs(xp) < abs(xm)) return clamp(xp, 0f, 1f)
            else return clamp(xm, -1f, 0f)
        } else {
            if (0 <= xp) return clamp(xp, 0f, 1f)
            if (xm <= 0) return clamp(xm, -1f, 0f)
        }

        return 0f
    }
}