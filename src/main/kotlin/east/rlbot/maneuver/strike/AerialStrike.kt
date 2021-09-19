package east.rlbot.maneuver.strike

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.*
import east.rlbot.maneuver.DodgeFinish
import east.rlbot.math.Mat3
import east.rlbot.math.OrientedCube
import east.rlbot.simulation.JumpModel
import east.rlbot.simulation.Physics.GRAVITY
import east.rlbot.util.DT
import east.rlbot.util.coerceIn01
import java.awt.Color
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class AerialStrike(
    interceptBall: AdjustableFutureBall,
    var doSecondJump: Boolean,
    val doDodgeStrike: Boolean,
) : Strike(interceptBall) {

    init {
        assert(doDodgeStrike && doSecondJump) { "Can't do two jumps and dodge" }
    }

    override var done: Boolean = false

    private var initialized = false
    private var jumping = false
    private var jumpBeginTime = -1f
    private var jumpPauseCounter = 0

    override fun exec(data: DataPack): OutputController {
        if (!initialized) {
            initialized = true
            jumping = data.me.wheelContact
            doSecondJump = doSecondJump && data.me.wheelContact
        }

        val now = data.match.time
        val car = data.me
        val up = car.ori.up
        val controls = OutputController()

        interceptBall.adjust(car.pos.dist(interceptBall.pos) / 60f)

        val timeLeft = interceptBall.time - now
        var expectedPos = car.pos + car.vel * timeLeft + GRAVITY * timeLeft.pow(2) * 0.5f + car.vel * DT
        var expectedVel = car.vel + GRAVITY * timeLeft

        var canRotate = true

        if (jumping) {
            if (jumpBeginTime == -1f) {
                jumpBeginTime = now
            }

            val jumpElapsed = now - jumpBeginTime
            val jumpLeft = (Car.MAX_JUMP_HOLD_TIME - jumpElapsed).coerceAtLeast(0f)

            // Current jump pulse
            if (jumpElapsed == 0f) {
                expectedPos += up * Car.JUMP_IMPULSE * timeLeft
                expectedVel += up * Car.JUMP_IMPULSE
            }

            // Acceleration from holding up
            expectedPos += up * Car.JUMP_HOLD_FORCE * jumpLeft * (timeLeft - 0.5f * jumpLeft)
            expectedVel += up * Car.JUMP_HOLD_FORCE * jumpLeft

            if (doSecondJump) {
                // Second jump impulse
                expectedPos += up * Car.JUMP_IMPULSE * (timeLeft - jumpLeft)
                expectedVel += up * Car.JUMP_IMPULSE
            }

            if (jumpElapsed <= Car.MAX_JUMP_HOLD_TIME) {
                // We are currently doing first jump
                controls.withJump()

            } else if (doSecondJump) {
                // Transitioning to second jump

                if (jumpPauseCounter < 3) {
                    // Do 3-tick pause between jumps
                    //controls.withJump(false)
                    jumpPauseCounter += 1
                } else {
                    // Do second jump
                    controls.withJump()
                    doSecondJump = false
                    canRotate = false
                }
            } else {
                // We are done jumping
                jumping = false
            }
        }

        val shootDirection = car.pos.dirTo(interceptBall.pos).flat()
        // TODO Consider ball velocity in offset
        val desiredPos = interceptBall.pos - shootDirection * (Ball.RADIUS + car.hitbox.size.x / 2f)
        val desiredOri = Mat3.lookingAt(desiredPos, interceptBall.pos, up)

        val posDelta = desiredPos - expectedPos
        val posDeltaDir = posDelta.dir()
        val bigPosDelta = posDelta.magSqr() > 40*40
        val forwardSpeedDelta = posDelta dot car.ori.forward / timeLeft

        // Begin dodge
        if (doDodgeStrike && !jumping) {
            if (jumpPauseCounter < 2) {
                // Do a 3-tick pause between first jump and dodge
                jumpPauseCounter += 1
            } else {
                // TODO find better conditions to start the dodge
                if (timeLeft <= 1 / 60f && posDelta.magSqr() < 30 * 30) {
                    data.bot.maneuver = DodgeFinish(interceptBall.pos)
                    return data.bot.maneuver!!.exec(data)!!
                }
            }
        }

        // Rotate the car
        if (canRotate) {
            val scale = if (jumping) 0.5f else 1f // Slower rotation during jumping

            val pd = if (bigPosDelta)
                data.bot.fly.align(Mat3.lookingInDir(posDelta))
            else
                data.bot.fly.align(desiredOri)

            controls.withRoll(pd.roll * scale)
            controls.withPitch(pd.pitch * scale)
            controls.withYaw(pd.yaw * scale)
        }

        // Boosting
        if (car.ori.forward.angle(posDeltaDir) <= 0.4) {
            if (bigPosDelta && forwardSpeedDelta >= Car.BOOST_BONUS_ACC * Car.MIN_BOOST_TIME + Car.THROTTLE_AIR_ACC * DT) {
                controls.withBoost()
            } else {
                controls.withThrottle(forwardSpeedDelta / (Car.THROTTLE_AIR_ACC * DT))
            }
        }

        // One frame of braking
        if (car.wheelContact && forwardSpeedDelta < 0f) {
            // Ground acceleration is a lot more efficient, so let's make something of it
            controls.withThrottle(forwardSpeedDelta / (Car.BRAKE_ACC * DT))
        }

        done = !interceptBall.valid || // Ball prediction changed
                timeLeft <= -0.2f //||
                // (!jumping && car.wheelContact) // We landed after jumping
        if (done) {
            data.bot.maneuver = data.bot.shotFinder.findSoonestStrike(2.5f, listOf(AerialStrike))
        }

        // Rendering
        val carToHitPosDir = (desiredPos - car.pos).dir()
        val draw = data.bot.draw
        draw.color = Color.MAGENTA
        draw.orientedCube(desiredPos, OrientedCube(desiredOri, car.hitbox.size))
        draw.circle(car.pos.lerp(desiredPos, 0.33f), carToHitPosDir, 40f)
        draw.circle(car.pos.lerp(desiredPos, 0.66f), carToHitPosDir, 40f)
        draw.line(car.pos, desiredPos)

        return controls
    }

    companion object Factory : StrikeFactory {
        override fun tryCreate(bot: BaseBot, ball: FutureBall): Strike? {
            val car = bot.data.me

            if (car.wheelContact && car.timeWithWheelContact < 8 * DT) return null // We just landed
            if (ball.pos.z < JumpModel.single.maxHeight() + 2 * Ball.RADIUS) return null

            val localPos = car.toLocal(ball.pos)
            if (localPos.x < 2400 && 0.01f * localPos.x.pow(1.4f) < abs(localPos.y)) return null // https://www.desmos.com/calculator/clgzwkpan1

            val up = car.ori.up
            val timeLeft = ball.time - bot.data.match.time
            val dodgePossible = timeLeft < Car.MAX_JUMP_HOLD_TIME + 1.25 && car.wheelContact // TODO Replace wheelcontact with hasDoubleJump
            val scenarios = if (dodgePossible) listOf(true, false) else listOf(false)

            for (doDodge in scenarios) {
                var expectedPos = car.pos + car.vel * timeLeft + GRAVITY * timeLeft.pow(2) * 0.5f + car.vel * DT
                var expectedVel = car.vel + GRAVITY * timeLeft

                if (car.wheelContact) {
                    val jumpTime = Car.MAX_JUMP_HOLD_TIME

                    // First jump impulse
                    expectedPos += up * Car.JUMP_IMPULSE * timeLeft
                    expectedVel += up * Car.JUMP_IMPULSE

                    // Acceleration from holding jump button
                    expectedPos += up * Car.JUMP_IMPULSE * jumpTime * (timeLeft - 0.5 * jumpTime)
                    expectedVel += up * Car.JUMP_IMPULSE * jumpTime

                    if (!doDodge) {
                        expectedPos += up * Car.JUMP_IMPULSE * (timeLeft - jumpTime)
                        expectedVel += up * Car.JUMP_IMPULSE
                    }
                }

                val shootDirection = car.pos.dirTo(ball.pos).flat()
                // TODO Consider ball velocity in offset
                val desiredPos = ball.pos - shootDirection * (Ball.RADIUS + car.hitbox.size.x / 2f)
                val desiredOri = Mat3.lookingAt(desiredPos, ball.pos, up)

                val posDelta = desiredPos - expectedPos
                val posDeltaDir = posDelta.dir()

                // RLU magic checks
                val oriAngle = car.ori.mat.angleTo(desiredOri)
                val turnTime = 0.75f * 2 * sqrt(oriAngle / 9f)
                val tau1 = turnTime * (1f - 0.3f / oriAngle).coerceIn01()

                val requiredAcc = (2 * posDelta.mag() / (timeLeft - tau1).pow(2))
                val accRatio = requiredAcc / (Car.BOOST_BONUS_ACC + Car.THROTTLE_AIR_ACC)
                val enoughTime = accRatio <= 0.9f

                val tau2 = timeLeft - (timeLeft - tau1) * sqrt(1f - accRatio.coerceIn01())
                val boostEstimate = (tau2 - tau1) * Car.BOOST_USAGE_RATE
                val enoughBoost = boostEstimate < car.boost

                val velEstimate = expectedVel + posDeltaDir * Car.BOOST_BONUS_ACC * (tau2 - tau1)
                val realisticSpeed = velEstimate.mag() < Car.MAX_SPEED

                if (enoughTime && enoughBoost && realisticSpeed)
                    return AerialStrike(ball.adjustable(), !doDodge, doDodge)
            }

            return null
        }
    }
}