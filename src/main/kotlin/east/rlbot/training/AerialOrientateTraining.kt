package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.math.Vec3
import rlbot.cppinterop.RLBotDll
import rlbot.gamestate.BallState
import rlbot.gamestate.CarState
import rlbot.gamestate.GameState
import rlbot.gamestate.PhysicsState
import kotlin.random.Random

class AerialOrientateTraining : Training {

    val TEST_DURATION = 4f
    val CAR_POS = Vec3(0f, 0f, 1500f)
    val BALL_DIST = 400f
    val ANTI_GRAVITY = Vec3(0f, 0f, 60f).toDesired()

    var nextTest = 20f

    var ballPos = Vec3(0, 0, 100f)

    override fun exec(bot: BaseBot) {
        if (bot.data.match.time >= nextTest) {
            ballPos = CAR_POS + Vec3(
                    Random.nextFloat() * 2 - 1,
                    Random.nextFloat() * 2 - 1,
                    Random.nextFloat() * 2 - 1
            ).unit() * BALL_DIST

            nextTest = bot.data.match.time + TEST_DURATION
        }

        val gameState = GameState()
                .withCarState(
                        bot.index,
                        CarState()
                                .withPhysics(
                                        PhysicsState()
                                                .withLocation(CAR_POS.toDesired())
                                                .withVelocity(ANTI_GRAVITY)
                                )
                )
                .withBallState(
                        BallState(
                                PhysicsState()
                                        .withLocation(ballPos.toDesired())
                                        .withVelocity(ANTI_GRAVITY)
                        )
                )

        RLBotDll.setGameState(gameState.buildPacket())
    }
}