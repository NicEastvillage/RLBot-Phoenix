package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.data.Arena
import east.rlbot.math.Vec3
import java.awt.Color
import kotlin.random.Random

class PlaneTest : Training {

    val TEST_DURATION = 10f

    var nextTest = 12f

    var points = listOf<Vec3>()
    var projections = listOf<Vec3>()

    override fun exec(bot: BaseBot) {
        if (bot.data.match.time >= nextTest) {
            points = List(150) {
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

            bot.draw.cross(Color.CYAN, point, 50f)
            bot.draw.cross(Color.GREEN, projection, 50f)
            bot.draw.drawLine3d(Color.WHITE, point, projection)
        }

        for (wall in Arena.ALL_WALLS) {
            val n = wall.offset + wall.normal * 300
            bot.draw.cross(Color.RED, wall.offset, 50f)
            bot.draw.cross(Color.MAGENTA, n, 50f)
            bot.draw.drawLine3d(Color.YELLOW, wall.offset, n)
        }
    }
}