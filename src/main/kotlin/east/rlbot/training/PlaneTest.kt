package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.data.Arena
import east.rlbot.math.Vec3
import east.rlbot.util.DebugDraw
import java.awt.Color
import kotlin.random.Random

class PlaneTest : Training {

    val TEST_DURATION = 10f

    var nextTest = 12f

    var points = listOf<Vec3>()
    var projections = listOf<Vec3>()

    override fun exec(bot: BaseBot): OutputController? {
        if (bot.index != 0) return null
        if (bot.data.match.time >= nextTest) {
            points = List(80) {
                Vec3(
                    Random.nextFloat() * 2 - 1,
                    Random.nextFloat() * 2 - 1,
                    Random.nextFloat()
                ) * Vec3(
                    Arena.WIDTH2,
                    Arena.LENGTH2,
                    Arena.HEIGHT
                )
            }

            projections = points.map { it.projectToNearest(Arena.SIDE_WALLS_AND_GROUND) }.toList()

            nextTest = bot.data.match.time + TEST_DURATION
        }

        for (i in 0 until points.size) {
            val point = points[i]
            val projection = projections[i]

            bot.draw.cross(point, 50f, Color.CYAN)
            bot.draw.cross(projection, 50f, Color.GREEN)
            bot.draw.line(point, projection, Color.WHITE)
        }

        for (wall in Arena.ALL_WALLS) {
            val n = wall.offset + wall.normal * 300
            bot.draw.cross(wall.offset, 50f, Color.RED)
            bot.draw.cross(n, 50f, Color.MAGENTA)
            bot.draw.line(wall.offset, n, Color.YELLOW)
        }

        return null
    }
}