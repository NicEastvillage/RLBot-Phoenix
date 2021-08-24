package east.rlbot

import east.rlbot.maneuver.decideKickoff
import east.rlbot.states.BallChaseState
import east.rlbot.states.DefenceState
import east.rlbot.states.UtilitySystem

class PhoenixBot(index: Int, team: Int, name: String) : BaseBot(index, team, name) {

    val utilitySystem = UtilitySystem(listOf(
            BallChaseState(),
            DefenceState()
    ))

    override fun onKickoffBegin() {
        decideKickoff(this)
    }

    override fun getOutput(): OutputController {
        val state = utilitySystem.eval(data)
        return state.exec(data)
    }
}