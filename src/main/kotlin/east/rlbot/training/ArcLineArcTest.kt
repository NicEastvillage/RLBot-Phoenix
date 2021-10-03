package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.math.ArcLineArc
import east.rlbot.simulation.BallPredictionManager

class ArcLineArcTest : Training {
    override fun exec(bot: BaseBot): OutputController? {
        val car = bot.data.enemies[0]
        val arriveDir = (bot.data.enemyGoal.pos - bot.data.ball.pos).dir()

        ArcLineArc.findSmart(car, BallPredictionManager.getAtTime(bot.data.match.time + 0.02f)!!, arriveDir * 4000f, car.hitbox.size.x, draw = bot.draw)

        return OutputController()
    }
}