package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.data.Arena
import east.rlbot.data.Goal
import east.rlbot.data.Team
import east.rlbot.math.Vec3
import rlbot.cppinterop.RLBotDll
import rlbot.gamestate.BallState
import rlbot.gamestate.CarState
import rlbot.gamestate.GameState
import rlbot.gamestate.PhysicsState
import kotlin.random.Random

class KickOffTraining : Training {

    val POST_KICKOFF_TIME = 3f

    var kickOffEndTime: Float? = null
    var stateManipulated = false

    override fun exec(bot: BaseBot) {
        if (bot.index != 0) return

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
                                .withLocation(Vec3(y=Arena.LENGTH2 + 150, z=120).toDesired())
                        )
                    )

                RLBotDll.setGameState(gameState.buildPacket())
            }
        }
    }
}