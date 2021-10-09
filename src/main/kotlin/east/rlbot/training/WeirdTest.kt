package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.experimental.AccAwareArcLineArc
import east.rlbot.experimental.findAccAwareArcLineArc

class WeirdTest : Training {

    private var path: AccAwareArcLineArc? = null
    var init = false

    override fun exec(bot: BaseBot): OutputController? {

        if (!init) {
            init = true
        }

        path = findAccAwareArcLineArc(
            bot.data.enemies[0],
            bot.data.ball.asFuture(),
            bot.data.myGoal.pos,
            bot.draw,
        )

        return OutputController()
    }
}