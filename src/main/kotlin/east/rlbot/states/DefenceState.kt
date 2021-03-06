package east.rlbot.states

import east.rlbot.OutputController
import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.math.clamp

class DefenceState : UtilityState {
    override fun utility(data: DataPack): Float {
        val goalToBall = data.ball.pos - data.myGoal.pos
        val goalToCar = data.me.pos - data.myGoal.pos
        val offsite = (goalToBall.dir() dot goalToCar) / goalToBall.mag()
        return clamp(offsite - 0.2f, 0f, 1f)
    }

    override fun exec(data: DataPack): OutputController {
        return if (data.me.boost < 50)
            data.bot.drive.boostPickupTowards(
                data.myGoal.pos,
                targetSpeed = Car.MAX_SPEED,
                boostPreservation = 100,
            )
        else
            data.bot.drive.towards(
                data.myGoal.pos,
                targetSpeed = Car.MAX_SPEED,
                boostPreservation = 80,
                allowDodges = true
            )
    }
}