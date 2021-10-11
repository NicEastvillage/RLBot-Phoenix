package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.Arena
import east.rlbot.data.Ball
import east.rlbot.data.Car
import east.rlbot.experimental.AccAwareArcLineArc
import east.rlbot.experimental.AccAwareArcLineStrike
import east.rlbot.experimental.findAccAwareArcLineArc
import east.rlbot.math.Vec3
import east.rlbot.util.PIf
import rlbot.cppinterop.RLBotDll
import rlbot.gamestate.*
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class AccAwareArcLineArcTest : Training {

    private val INTERVAL = 5f
    private val CREATE_TICK_DELAY = 3

    var next = 7f
    var counter = -2

    private var path: AccAwareArcLineArc? = null
    private var strike: AccAwareArcLineStrike? = null

    override fun exec(bot: BaseBot): OutputController? {
        if (bot.index != 0) return null

        if (bot.data.match.time >= next) {

            next = bot.data.match.time + INTERVAL
            counter = CREATE_TICK_DELAY

            val ballX = - 1000 + 2000 * Random.nextFloat()
            val ballY = 0.9f * Arena.LENGTH2 * Random.nextFloat()
            val ballPos = Vec3(ballX, ballY, Ball.RADIUS + 1f)

            val carX = 0.9f * (- Arena.WIDTH2 + Arena.WIDTH * Random.nextFloat())
            val carY = 0.9f * (- Arena.LENGTH2 + Arena.LENGTH * Random.nextFloat())
            val carPos = Vec3(carX, carY, Car.REST_HEIGHT + 1f)

            val yaw = 2 * PIf * Random.nextFloat()
            val vel = Vec3(cos(yaw), sin(yaw), 0f) * 1400f * Random.nextFloat()

            val gameState = GameState()
                .withCarState(
                    bot.index,
                    CarState()
                        .withPhysics(
                            PhysicsState()
                                .withLocation(carPos.toDesired())
                                .withVelocity(vel.toDesired())
                                .withRotation(DesiredRotation(0f, yaw, 0f))
                                .withAngularVelocity(Vec3.ZERO.toDesired())
                        )
                        .withBoostAmount(100f * Random.nextFloat())
                )
                .withBallState(
                    BallState(
                        PhysicsState()
                            .withLocation(ballPos.toDesired())
                            .withVelocity(Vec3.ZERO.toDesired())
                            .withAngularVelocity(Vec3.ZERO.toDesired())
                    )
                )

            RLBotDll.setGameState(gameState.buildPacket())
        }

        if (counter == -1) {
            path = findAccAwareArcLineArc(
                bot.data.me,
                bot.data.ball.asFuture(),
                bot.data.enemyGoal.pos,
                bot.draw,
            )
            strike = path?.let {
                next = bot.data.match.time + it.duration + 2f
                AccAwareArcLineStrike(it)
            }
        }

        bot.draw.color = Color.WHITE
        path?.draw(bot.draw)

        counter--

        return strike?.exec(bot.data) ?: OutputController()
    }
}