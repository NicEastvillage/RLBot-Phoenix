package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.maneuver.strike.AerialStrike
import east.rlbot.math.Vec3
import rlbot.cppinterop.RLBotDll
import rlbot.gamestate.BallState
import rlbot.gamestate.GameState
import rlbot.gamestate.PhysicsState

/**
 * Nudges the ball upwards every few seconds
 */
class BallNudgerTraining : Training {

    val NUDGE_INTERVAL = 10f

    var nextNudge = 12f

    override fun exec(bot: BaseBot): OutputController? {
        if (bot.index != 0) return null
        if (bot.data.match.time >= nextNudge && bot.maneuver !is AerialStrike) {
            val ball = bot.data.ball
            val gameState = GameState()
                .withBallState(BallState(PhysicsState()
                    .withVelocity((ball.vel.withZ(1000)).toDesired())))

            RLBotDll.setGameState(gameState.buildPacket())

            nextNudge = bot.data.match.time + NUDGE_INTERVAL
        }

        return null
    }
}