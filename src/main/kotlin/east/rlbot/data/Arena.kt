package east.rlbot.data

import east.rlbot.math.Mat3
import east.rlbot.math.Plane
import east.rlbot.math.Vec3
import east.rlbot.math.axisToRotation
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

object Arena {
    const val LENGTH = 10240f
    const val LENGTH2 = LENGTH / 2f
    const val WIDTH = 8192f
    const val WIDTH2 = WIDTH / 2f
    const val HEIGHT = 2044f
    const val HEIGHT2 = HEIGHT / 2f
    val SEMI_SIZE = Vec3(WIDTH2, LENGTH2, HEIGHT / 2f)

    const val CORNER_WALL_AX_INTERSECT = 8064

    val GROUND = Plane(Vec3(), Vec3(z=1))
    val CEILING = Plane(Vec3(z= HEIGHT), Vec3(z=-1))
    val BLUE_BACKBOARD = Plane(Vec3(y=-LENGTH2), Vec3(y=1))
    val ORANGE_BACKBOARD = Plane(Vec3(y= LENGTH2), Vec3(y=-1))
    val LEFT_WALL = Plane(Vec3(x= WIDTH2), Vec3(x=-1)) // Blue POV
    val RIGHT_WALL = Plane(Vec3(x=-WIDTH2), Vec3(x=1)) // Blue POV
    val BLUE_RIGHT_CORNER_WALL = Plane(Vec3(y=-CORNER_WALL_AX_INTERSECT), Vec3(x=1, y=1).dir())
    val BLUE_LEFT_CORNER_WALL = Plane(Vec3(y=-CORNER_WALL_AX_INTERSECT), Vec3(x=-1, y=1).dir())
    val ORANGE_RIGHT_CORNER_WALL = Plane(Vec3(y=CORNER_WALL_AX_INTERSECT), Vec3(x=-1, y=-1).dir())
    val ORANGE_LEFT_CORNER_WALL = Plane(Vec3(y=CORNER_WALL_AX_INTERSECT), Vec3(x=1, y=-1).dir())

    val ALL_WALLS = listOf(
        GROUND,
        CEILING,
        BLUE_BACKBOARD,
        ORANGE_BACKBOARD,
        LEFT_WALL,
        RIGHT_WALL,
        BLUE_RIGHT_CORNER_WALL,
        BLUE_LEFT_CORNER_WALL,
        ORANGE_RIGHT_CORNER_WALL,
        ORANGE_LEFT_CORNER_WALL
    )

    val SIDE_WALLS = listOf(
        BLUE_BACKBOARD,
        ORANGE_BACKBOARD,
        LEFT_WALL,
        RIGHT_WALL,
        BLUE_RIGHT_CORNER_WALL,
        BLUE_LEFT_CORNER_WALL,
        ORANGE_RIGHT_CORNER_WALL,
        ORANGE_LEFT_CORNER_WALL
    )

    val SIDE_WALLS_AND_GROUND = listOf(
        GROUND,
        BLUE_BACKBOARD,
        ORANGE_BACKBOARD,
        LEFT_WALL,
        RIGHT_WALL,
        BLUE_RIGHT_CORNER_WALL,
        BLUE_LEFT_CORNER_WALL,
        ORANGE_RIGHT_CORNER_WALL,
        ORANGE_LEFT_CORNER_WALL
    )

    object SDF {

        private const val ROUNDNESS = 300f

        private val ROT_45 = cos(Math.PI/4f)
        private val ROT_45_MAT = Mat3.rotationMatrix(Vec3.UP, Math.PI / 4f)
        private val CORNER_SEMI_SIZE = Vec3(ROT_45 * CORNER_WALL_AX_INTERSECT, ROT_45 * CORNER_WALL_AX_INTERSECT, HEIGHT2)

        private val GOALS_SEMI_SIZE = Vec3(Goal.WIDTH2, LENGTH2 + Goal.DEPTH, Goal.HEIGHT / 2f)

        fun wallDistance(point: Vec3): Float {
            // SDF box https://www.youtube.com/watch?v=62-pRVZuS5c
            // SDF rounded corners https://www.youtube.com/watch?v=s5NGeUV2EyU

            // Base square
            val baseQ = (point - Vec3(z = HEIGHT2)).abs() - SEMI_SIZE + Vec3.ONES * ROUNDNESS
            val baseDistOutside = baseQ.coerceAtLeast(Vec3.ZERO).mag()
            val baseDistInside = min(baseQ.maxComponent(), 0f) // negative if point is inside
            val baseDist = baseDistOutside + baseDistInside

            // Corners square
            val cornerQ = ((ROT_45_MAT dot point) - Vec3(z = HEIGHT2)).abs() - CORNER_SEMI_SIZE + Vec3.ONES * ROUNDNESS
            val cornerDistOutside = cornerQ.coerceAtLeast(Vec3.ZERO).mag()
            val cornerDistInside = min(cornerQ.maxComponent(), 0f) // negative if point is inside
            val cornerDist = cornerDistOutside + cornerDistInside

            // Goals square
            val goalsQ = (point - Vec3(z = Goal.HEIGHT / 2f)).abs() - GOALS_SEMI_SIZE
            val goalsDistOutside = goalsQ.coerceAtLeast(Vec3.ZERO).mag()
            val goalsDistInside = min(goalsQ.maxComponent(), 0f) // negative if point is inside
            val goalsDist = goalsDistOutside + goalsDistInside

            // Intersection of base and corners
            val baseCornerDist = max(baseDist, cornerDist) - ROUNDNESS

            // Union with goals and invert result
            return -min(baseCornerDist, goalsDist)
        }

        fun normal(point: Vec3): Vec3 {
            // SDF normals https://www.iquilezles.org/www/articles/normalsSDF/normalsSDF.htm
            val h = 0.0004f
            return Vec3(
                wallDistance(point + Vec3(h, 0f, 0f)) - wallDistance(point - Vec3(h, 0f, 0f)),
                wallDistance(point + Vec3(0f, h, 0f)) - wallDistance(point - Vec3(0f, h, 0f)),
                wallDistance(point + Vec3(0f, 0f, h)) - wallDistance(point - Vec3(0f, 0f, h)),
            ).dir()
        }
    }
}
