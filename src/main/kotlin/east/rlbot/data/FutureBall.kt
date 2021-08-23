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
}