package east.rlbot.maneuver.strike

import east.rlbot.OutputController
import east.rlbot.data.AdjustableFutureBall
import east.rlbot.data.DataPack
import east.rlbot.data.FutureBall
import east.rlbot.maneuver.Maneuver
import east.rlbot.math.Vec3

abstract class Strike(
    var interceptBall: AdjustableFutureBall,
) : Maneuver {
    abstract override fun exec(data: DataPack): OutputController
}

interface StrikeFactory {
    fun tryCreate(data: DataPack, ball: FutureBall, target: Vec3): Strike?
}