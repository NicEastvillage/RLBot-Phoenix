package east.rlbot

import east.rlbot.maneuver.Recovery
import east.rlbot.maneuver.decideKickoff
import east.rlbot.math.Vec3
import east.rlbot.simulation.JumpModel
import east.rlbot.simulation.turnRadius
import east.rlbot.states.BallChaseState
import east.rlbot.states.DefenceState
import east.rlbot.states.UtilitySystem
import java.awt.Color
import kotlin.math.sign
import kotlin.math.sqrt

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