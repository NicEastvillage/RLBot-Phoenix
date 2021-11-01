package east.rlbot

import east.rlbot.maneuver.Recovery
import east.rlbot.maneuver.kickoff.decideKickoff
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
        if (!data.me.wheelContact) {
            maneuver = Recovery()
            return maneuver!!.exec(data)!!
        }

        drive.estimateTime2D(data.ball.pos, draw = true)

        val state = utilitySystem.eval(data)
        return state.exec(data)
    }
}