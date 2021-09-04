package east.rlbot.maneuver.strike

import east.rlbot.OutputController
import east.rlbot.data.Ball
import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.data.FutureBall
import east.rlbot.maneuver.Recovery
import east.rlbot.math.Mat3
import east.rlbot.math.Vec3
import east.rlbot.simulation.JumpModel
import java.awt.Color
import kotlin.math.*

class DodgeStrikeDodge(
    interceptBall: FutureBall,
) : Strike(interceptBall) {

    private val FIRST_JUMP_PAUSE_DURATION = 0.02f
    private val SECOND_JUMP_DURATION = 0.08f
    private val SECOND_JUMP_PAUSE_DURATION = 0.4f

    override var done = false

    private var expectedFirstJumpDuration = JumpModel.single.simUntilLimit(heightLimit = interceptBall.pos.z).time

    private var initialized = false
    private var phase = Phase.FIRST_JUMP
    private var startTime = 0f
    private var lastPhaseChange = 0f

    override fun exec(data: DataPack): OutputController {
        if (!initialized) {
            initialized = true
            startTime = data.match.time
            lastPhaseChange = data.match.time
            if (!data.me.wheelContact) {
                phase = Phase.FIRST_PAUSE
            }
        }
        val car = data.me
        val height = interceptBall.pos.z

        data.bot.draw.rect3D(car.pos + Vec3(z=100), 20, 20, color = when (phase) {
            Phase.FIRST_JUMP -> Color.CYAN
            Phase.FIRST_PAUSE -> Color.BLUE
            Phase.SECOND_JUMP -> Color.ORANGE
            Phase.SECOND_PAUSE -> Color.RED
        })

        return when (phase) {
            Phase.FIRST_JUMP, Phase.FIRST_PAUSE -> {
                val jumpTimeLeft = (startTime + expectedFirstJumpDuration - data.match.time).coerceAtLeast(0f)

                // Do we need to boost while jumping?
                val boostDuration = min(car.boost / Car.BOOST_USAGE_RATE, jumpTimeLeft) + FIRST_JUMP_PAUSE_DURATION
                val xyDisplacementDuringJumpWithoutBoost = car.vel.flat() * (jumpTimeLeft + FIRST_JUMP_PAUSE_DURATION)
                val xyDisplacementDuringJumpWithBoost = car.ori.forward.flat() * Car.BOOST_BONUS_ACC * boostDuration.pow(2) / 2 + xyDisplacementDuringJumpWithoutBoost
                val expectedDodgePos = (car.pos + xyDisplacementDuringJumpWithoutBoost).withZ(height)
                val boost = expectedDodgePos.dist(interceptBall.pos) > Ball.RADIUS + car.hitbox.size.x / 2f

                val ori = Mat3.lookingAt(car.pos, interceptBall.pos)
                val controls = data.bot.fly.align(ori).withJump(phase == Phase.FIRST_JUMP).withBoost(boost)

                // Do phase change?
                if (phase == Phase.FIRST_JUMP && jumpTimeLeft <= 0f) {
                    phase = Phase.FIRST_PAUSE
                    lastPhaseChange = data.match.time
                } else if (phase == Phase.FIRST_PAUSE && lastPhaseChange + FIRST_JUMP_PAUSE_DURATION < data.match.time) {
                    phase = Phase.SECOND_JUMP
                    lastPhaseChange = data.match.time
                }

                controls
            }
            Phase.SECOND_JUMP -> {
                if (lastPhaseChange + SECOND_JUMP_DURATION < data.match.time) {
                    phase = Phase.SECOND_PAUSE
                    lastPhaseChange = data.match.time
                }

                val dir = data.me.toLocal(interceptBall.pos).flat().dir()
                OutputController()
                    .withThrottle(1f)
                    .withJump()
                    .withPitch(-dir.x)
                    .withYaw(data.me.ori.up.z.sign * dir.y)
            }
            Phase.SECOND_PAUSE -> {
                if (lastPhaseChange + SECOND_JUMP_PAUSE_DURATION < data.match.time) {
                    done = true
                    data.bot.maneuver = Recovery()
                }

                OutputController().withThrottle(1f)
            }
        }
    }

    fun canBegin(data: DataPack): Boolean {
        val car = data.me
        val height = interceptBall.pos.z
        val result = JumpModel.single.simUntilLimit(heightLimit = height)
        val timeLeft = interceptBall.time - data.match.time
        val boostTime = min(car.boost / Car.BOOST_USAGE_RATE, result.time)
        val xyDisplacementDuringJumpWithoutBoost = car.vel.flat() * (result.time + 0.02f)
        val xyDisplacementDuringJumpWithBoost = car.ori.forward.flat() * Car.BOOST_BONUS_ACC * boostTime.pow(2) / 2 + xyDisplacementDuringJumpWithoutBoost
        val expectedDodgePos = car.pos + Vec3(z=height) + xyDisplacementDuringJumpWithBoost
        return expectedDodgePos.dist(interceptBall.pos) < Ball.RADIUS + car.hitbox.size.x / 2f + 5f && abs(timeLeft - result.time) < 0.15f
    }

    enum class Phase {
        FIRST_JUMP,
        FIRST_PAUSE,
        SECOND_JUMP,
        SECOND_PAUSE,
    }
}