package east.rlbot.maneuver.strike

import east.rlbot.OutputController
import east.rlbot.data.Ball
import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.data.FutureBall
import east.rlbot.maneuver.Dodge
import east.rlbot.math.Mat3
import east.rlbot.math.OrientedCube
import east.rlbot.simulation.Physics.GRAVITY
import java.awt.Color
import kotlin.math.pow

class AerialStrike(
    interceptBall: FutureBall,
    var doSecondJump: Boolean,
    val doDodgeStrike: Boolean,
) : Strike(interceptBall) {

    init {
        assert(doDodgeStrike != doSecondJump) { "Can't do two jumps and dodge" }
    }

    override var done: Boolean = false

    private var initialized = false
    private var jumping = false
    private var firstJumpDone = true
    private var jumpBeginTime = -1f
    private var jumpPauseCounter = 0

    override fun exec(data: DataPack): OutputController {
        if (!initialized) {
            initialized = true
            jumping = data.me.wheelContact
            firstJumpDone = !data.me.wheelContact
        }

        val curTime = data.match.time
        val car = data.me
        val up = car.ori.up
        val controls = OutputController()

        val timeLeft = interceptBall.time - curTime
        var expectedPos = car.pos + car.vel * timeLeft + GRAVITY * timeLeft.pow(2) * 0.5f
        var expectedVel = car.vel + GRAVITY * timeLeft

        var canRotate = true

        if (jumping) {
            val jumpElapsed: Float
            if (jumpBeginTime == -1f) {
                jumpElapsed = 0f
                jumpBeginTime = curTime
            } else {
                jumpElapsed = curTime - jumpBeginTime
            }

            val jumpLeft = Car.MAX_JUMP_HOLD_TIME - jumpElapsed

            // Current jump pulse
            if (jumpElapsed == 0f) {
                expectedPos += up * Car.JUMP_IMPULSE * timeLeft
                expectedVel += up * Car.JUMP_IMPULSE
                // We can rotate if this is our first jump
                canRotate = !firstJumpDone
            }

            if (firstJumpDone) {
                // Acceleration from holding up
                expectedPos += up * Car.JUMP_HOLD_FORCE * jumpLeft * (timeLeft - 0.5f * jumpLeft)
                expectedVel += up * Car.JUMP_HOLD_FORCE * jumpLeft
            }

            if (doSecondJump) {
                // Second jump impulse
                expectedPos += up * Car.JUMP_IMPULSE * (timeLeft - jumpLeft)
                expectedVel += up * Car.JUMP_IMPULSE
            }

            if (jumpElapsed < Car.MAX_JUMP_HOLD_TIME) {
                controls.withJump()
            } else if (doSecondJump) {
                if (jumpPauseCounter < 3) {
                    // Do 3-tick pause between jumps
                    jumpPauseCounter += 1
                } else {
                    // Time to start second jump
                    jumpBeginTime = -1f
                    firstJumpDone = true
                    doSecondJump = false
                }
            } else {
                // We are done jumping
                jumping = false
            }
        }

        val shootDirection = car.pos.dirTo(interceptBall.pos).flat()
        // TODO Consider ball velocity in offset
        val desiredPos = interceptBall.pos - shootDirection * (Ball.RADIUS + car.hitbox.size.x)
        val desiredOri = Mat3.lookingAt(desiredPos, interceptBall.pos, up)

        val expectedDelta = desiredPos - expectedPos
        val expectedDeltaDir = expectedDelta.dir()

        // Begin dodge TODO
        if (doDodgeStrike && firstJumpDone) {
            if (expectedDelta.magSqr() < 30*30) {
                // data.bot.maneuver = TODO
            }
        }

        // Rotate the car
        if (canRotate) {
            val pd = if (expectedDelta.magSqr() > 40*40)
                data.bot.fly.align(Mat3.lookingInDir(expectedDelta))
            else
                data.bot.fly.align(desiredOri)

            controls.withRoll(pd.roll)
            controls.withPitch(pd.pitch)
            controls.withYaw(pd.yaw)
        }

        // Boosting
        if (car.ori.forward.angle(expectedDeltaDir) < 0.3) {
            if (expectedDelta.magSqr() > 40*40) {
                controls.withBoost()
            } else {
                controls.withThrottle(0.5f * Car.THROTTLE_AIR_ACC * timeLeft.pow(2))
            }
        }

        done = interceptBall.valid()

        // Rendering
        val carToHitPosDir = (desiredPos - car.pos).dir()
        val draw = data.bot.draw
        draw.color = Color.PINK
        draw.orientedCube(desiredPos, OrientedCube(desiredOri, car.hitbox.size))
        draw.circle(car.pos.lerp(desiredPos, 0.33f), carToHitPosDir, 40f)
        draw.circle(car.pos.lerp(desiredPos, 0.66f), carToHitPosDir, 40f)
        draw.line(car.pos, desiredPos)

        return controls
    }
}