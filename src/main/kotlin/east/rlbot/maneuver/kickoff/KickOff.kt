package east.rlbot.maneuver.kickoff

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.Ball
import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.maneuver.*
import east.rlbot.math.Vec3
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

fun decideKickoff(bot: BaseBot) {
    assert(bot.data.ball.pos == Vec3.ZERO) { "Ball is not a (0, 0)" }

    val teamByDist = bot.data.wholeTeam.sortedBy { it.pos.mag() + it.pos.x.sign * bot.team.ysign }
    if (teamByDist.indexOf(bot.data.me) == 0) {
        bot.maneuver = GeneralKickOff()
    }
    else {
        bot.maneuver = TimedOutputManeuver(1f) { OutputController().withThrottle(0f) }
    }
}

class SimpleKickOff() : SteppedManeuver(
    ConditionalOutputManeuver({ it.bot.data.me.pos.dist(it.bot.data.ball.pos) > 720 }, {
        val dist = it.bot.data.me.pos.dist(it.bot.data.ball.pos)
        it.bot.drive.towards(Vec3(y=it.bot.team.ysign * (dist * 0.5f - 500f + Random.nextFloat() * 10f)), Car.MAX_SPEED, 0)
    }),
    Dodge(Vec3(z=Ball.RADIUS))
)

class GeneralKickOff() : Maneuver {
    override var done = false

    private var dodge: Dodge? = null

    override fun exec(data: DataPack): OutputController? {
        done = !data.ball.pos.flat().isZero
        if (done) {
            // Promote dodge maneuver
            data.bot.maneuver = dodge
            return dodge?.exec(data)
        }
        if (dodge?.done == true) {
            dodge = null
        }
        if (dodge != null) {
            return dodge!!.exec(data)
        }

        val car = data.me
        val dist = car.pos.mag()

        if (dist <= 720)
            dodge = Dodge(Vec3(z=Ball.RADIUS))

        var target = Vec3(Random.nextFloat() * 10f, car.team.ysign * (dist * 0.5f - 500f))

        if (abs(car.pos.x) > 230 && abs(car.pos.y) > 2880) {
            target = target.withY(car.team.ysign * 2770)
        }

        return data.bot.drive.towards(target, Car.MAX_SPEED, 0)
    }
}