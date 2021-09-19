package east.rlbot.data

import east.rlbot.math.Vec3
import east.rlbot.simulation.BallPredictionManager
import rlbot.flat.PredictionSlice

data class FutureBall(
    val pos: Vec3,
    val vel: Vec3,
    val time: Float,
) {
    constructor(predictionSlice: PredictionSlice) : this(
        Vec3(predictionSlice.physics().location()),
        Vec3(predictionSlice.physics().velocity()),
        predictionSlice.gameSeconds(),
    )

    fun valid(): Boolean {
        return BallPredictionManager.getAtTime(time)?.let { (it.pos - pos).magSqr() < 100f } ?: false
    }

    fun adjustable() = AdjustableFutureBall(this)
}

class AdjustableFutureBall(
    ball: FutureBall
) {
    var ball: FutureBall = ball; private set
    val pos get() = ball.pos
    val vel get() = ball.vel
    val time get() = ball.time

    var valid = true; private set

    /**
     * Check if prediction changed. If the change is very small, update ball. If change is big, become invalid.
     * Returns true if an adjustment was made
     */
    fun adjust(allowErrorMargin: Float = 10f): Boolean {
        BallPredictionManager.getAtTime(time)?.let { newPred ->
            if (newPred.pos.distSqr(pos) < allowErrorMargin * allowErrorMargin) {
                ball = newPred
                return true
            } else {
                valid = false
            }
        }
        return false
    }
}