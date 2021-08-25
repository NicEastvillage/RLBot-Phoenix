package east.rlbot.maneuver.strike

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.DataPack
import east.rlbot.data.FutureBall
import east.rlbot.maneuver.Maneuver

abstract class Strike(
    var interceptBall: FutureBall,
) : Maneuver {
    abstract override fun exec(data: DataPack): OutputController
}

interface StrikeFactory {
    fun tryCreate(bot: BaseBot, ball: FutureBall): Strike?
}