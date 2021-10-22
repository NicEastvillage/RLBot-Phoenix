package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.AdjustableAimedFutureBall
import east.rlbot.data.Arena
import east.rlbot.data.Ball
import east.rlbot.data.Car
import east.rlbot.experimental.AccAwareArcLineStrike
import east.rlbot.math.AimCone
import east.rlbot.math.Vec3
import east.rlbot.simulation.BallPredictionManager
import east.rlbot.util.PIf
import rlbot.cppinterop.RLBotDll
import rlbot.gamestate.*
import java.awt.Color
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class AccAwareArcLineArcTest : Training {

    private val INTERVAL = 1.5f
    private val CREATE_TICK_DELAY = 3

    var next = 7f
    var counter = -2

    private var strike: AccAwareArcLineStrike? = null

    override fun exec(bot: BaseBot): OutputController? {
        if (bot.index != 0) return null

        if (bot.data.match.time >= next) {

            next = bot.data.match.time + INTERVAL
            counter = CREATE_TICK_DELAY

            val ballX = -1000 + 2000 * Random.nextFloat()
            val ballY = 0.9f * Arena.LENGTH2 * Random.nextFloat()
            val ballPos = Vec3(ballX, ballY, Ball.RADIUS + 1f)

            val ballYaw = 2 * PIf * Random.nextFloat()
            val ballVel = (Vec3(cos(ballYaw), sin(ballYaw), 0f) * 1500f * Random.nextFloat()).withZ(-800 + 1600 * Random.nextFloat())

            val carX = min(0.9f * (-Arena.WIDTH2 + Arena.WIDTH * Random.nextFloat()), ballX + 4000f)
            val carY = 0.9f * (-Arena.LENGTH2 + Arena.LENGTH * Random.nextFloat())
            val carPos = Vec3(carX, carY, Car.REST_HEIGHT + 1f)

            val carYaw = 2 * PIf * Random.nextFloat()
            val carVel = Vec3(cos(carYaw), sin(carYaw), 0f) * 1400f * Random.nextFloat()

            val gameState = GameState()
                .withCarState(
                    bot.index,
                    CarState()
                        .withPhysics(
                            PhysicsState()
                                .withLocation(carPos.toDesired())
                                .withVelocity(carVel.toDesired())
                                .withRotation(DesiredRotation(0f, carYaw, 0f))
                                .withAngularVelocity(Vec3.ZERO.toDesired())
                        )
                        .withBoostAmount(100f * Random.nextFloat())
                )
                .withBallState(
                    BallState(
                        PhysicsState()
                            .withLocation(ballPos.toDesired())
                            .withVelocity(ballVel.toDesired())
                            .withAngularVelocity(Vec3.ZERO.toDesired())
                    )
                )

            RLBotDll.setGameState(gameState.buildPacket())
        }

        if (counter == -1 || strike?.done == true) {
            val factory = AccAwareArcLineStrike.Factory(bot.data.me)
            strike = BallPredictionManager.latest?.asSequence()?.mapNotNull { ball ->
                factory.tryCreate(bot.data, AdjustableAimedFutureBall(ball) {
                    AimCone.atGoal(it.pos, bot.data.enemyGoal)
                })
            }?.firstOrNull() as AccAwareArcLineStrike?

            if (counter == -1)
                next = bot.data.match.time + (strike?.aaaala?.getBest()?.aaala?.duration ?: (INTERVAL - 3f)) + 3f
        }

        bot.draw.color = Color.WHITE
        strike?.aaaala?.draw(bot.data, bot.draw)

        counter--

        val output = strike?.exec(bot.data)

        return if (strike?.car == bot.data.me) {
            output ?: OutputController()
        } else OutputController()
    }
}