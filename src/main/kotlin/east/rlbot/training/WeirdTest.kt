package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.Car

class WeirdTest : Training {

    private lateinit var path: FabrikPath
    var init = false

    override fun exec(bot: BaseBot): OutputController? {
        val car = bot.data.enemies[0]

        if (!init) {
            init = true
            path = FabrikPath(car.pos.withZ(Car.REST_HEIGHT), car.ori.forward.flat(), bot.data.ball.pos.withZ(Car.REST_HEIGHT), bot.data.ball.pos.dirTo2D(bot.data.myGoal.pos))
        }

        path.start = car.pos.withZ(Car.REST_HEIGHT)
        path.startDir = car.ori.forward.dir2D()
        path.end = bot.data.ball.pos.withZ(Car.REST_HEIGHT)
        path.endDir = bot.data.ball.pos.dirTo2D(bot.data.myGoal.pos)
        path.adjust(1)

        bot.draw.polyline(listOf(car.pos) + path.points + listOf(bot.data.ball.pos.withZ(Car.REST_HEIGHT)))
        for (i in path.points.indices) {
            bot.draw.rect3D(path.points[i], 10, 10)
        }

        return OutputController()
    }
}