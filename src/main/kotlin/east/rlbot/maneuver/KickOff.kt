package east.rlbot.maneuver

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.Ball
import east.rlbot.data.Car
import east.rlbot.math.Vec3
import kotlin.math.sign
import kotlin.random.Random

fun decideKickoff(bot: BaseBot) {
    assert(bot.data.ball.pos == Vec3.ZERO) { "Ball is not a (0, 0)" }

    val teamByDist = bot.data.wholeTeam.sortedBy { it.pos.mag() + it.pos.x.sign * bot.team.ysign }
    if (teamByDist.indexOf(bot.data.me) == 0) {
        bot.maneuver = SimpleKickOff()
        bot.print("Going for kick off")
    }
    else {
        bot.maneuver = TimedOutputManeuver(1f) { OutputController().withThrottle(0f) }
        bot.print("Waiting during kick off")
    }
}

class SimpleKickOff() : SteppedManeuver(
    ConditionalOutputManeuver({ it.bot.data.me.pos.dist(it.bot.data.ball.pos) > 720 }, {
        val dist = it.bot.data.me.pos.dist(it.bot.data.ball.pos)
        it.bot.drive.towards(Vec3(y=it.bot.team.ysign * (dist * 0.5f - 500f + Random.nextFloat() * 10f)), Car.MAX_SPEED, 0)
    }),
    Dodge(Vec3(z=Ball.RADIUS))
)