package east.rlbot.states

import east.rlbot.OutputController
import east.rlbot.data.DataPack
import east.rlbot.math.clamp

class DefenceState : UtilityState {
    override fun utility(data: DataPack): Float {
        val goalToBall = data.ball.pos - data.myGoal.pos
        val goalToCar = data.me.pos - data.myGoal.pos
        val offsite = (goalToBall.unit() dot goalToCar) / goalToBall.mag()
        return clamp(offsite - 0.2f, 0f, 1f)
    }

    override fun exec(data: DataPack): OutputController {
        return data.bot.drive.towards(
                data.myGoal.pos,
                targetSpeed = 2300f,
                boostPreservation = 100
        )
    }
}