package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.math.ArcLineArc

class ArcLineArcTest : Training {
    override fun exec(bot: BaseBot): OutputController? {
        val car = bot.data.enemies[0]
        val arriveDir = (bot.data.enemyGoal.pos - bot.data.ball.pos).dir()

        val best = ArcLineArc.findShortest(car.pos.flat(), car.ori.forward.flat(), bot.data.ball.pos.flat(), arriveDir.flat(), 400f, 400f)
        best.draw(bot.draw)

        return OutputController()
    }
}