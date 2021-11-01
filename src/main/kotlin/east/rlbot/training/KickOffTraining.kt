package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.Arena
import east.rlbot.math.Vec3
import rlbot.cppinterop.RLBotDll
import rlbot.gamestate.BallState
import rlbot.gamestate.GameState
import rlbot.gamestate.PhysicsState
import kotlin.math.sign

class KickOffTraining : Training {

    val POST_KICKOFF_TIME = 3f

    var kickOffEndTime: Float? = null
    var stateManipulated = false

    override fun exec(bot: BaseBot): OutputController? {
        if (bot.index != 0) return null

        if (bot.data.match.isKickOff) {
            stateManipulated = false
            kickOffEndTime = null

        } else {
            if (kickOffEndTime == null)
                kickOffEndTime = bot.data.match.time

            if (!stateManipulated && kickOffEndTime?.let { bot.data.match.time - it > POST_KICKOFF_TIME} == true) {

                stateManipulated = true // So it only happens once per kickoff

                val gameState = GameState()
                    .withBallState(
                        BallState(
                            PhysicsState()
                                .withLocation(Vec3(y= (Arena.LENGTH2 + 150) * bot.data.ball.pos.y.sign, z=120).toDesired())
                        )
                    )

                RLBotDll.setGameState(gameState.buildPacket())
            }
        }

        return null
    }
}