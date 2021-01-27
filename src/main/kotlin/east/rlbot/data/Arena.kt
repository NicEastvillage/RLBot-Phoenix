package east.rlbot.data

import east.rlbot.math.Plane
import east.rlbot.math.Vec3
import kotlin.math.abs

object Arena {
    const val LENGTH = 10240f
    const val LENGTH2 = LENGTH / 2f
    const val WIDTH = 8192f
    const val WIDTH2 = WIDTH / 2f
    const val HEIGHT = 2044f

    const val CORNER_WALL_AX_INTERSECT = 8064

    val GROUND = Plane(Vec3(), Vec3(z=1))
    val CEILING = Plane(Vec3(z= HEIGHT), Vec3(z=-1))
    val BLUE_BACKBOARD = Plane(Vec3(y=-LENGTH2), Vec3(y=1))
    val ORANGE_BACKBOARD = Plane(Vec3(y= LENGTH2), Vec3(y=-1))
    val LEFT_WALL = Plane(Vec3(x= WIDTH2), Vec3(x=-1)) // Blue POV
    val RIGHT_WALL = Plane(Vec3(x=-WIDTH2), Vec3(x=1)) // Blue POV
    val BLUE_RIGHT_CORNER_WALL = Plane(Vec3(y=-CORNER_WALL_AX_INTERSECT), Vec3(x=1, y=1).unit())
    val BLUE_LEFT_CORNER_WALL = Plane(Vec3(y=-CORNER_WALL_AX_INTERSECT), Vec3(x=-1, y=1).unit())
    val ORANGE_RIGHT_CORNER_WALL = Plane(Vec3(y=CORNER_WALL_AX_INTERSECT), Vec3(x=-1, y=-1).unit())
    val ORANGE_LEFT_CORNER_WALL = Plane(Vec3(y=CORNER_WALL_AX_INTERSECT), Vec3(x=1, y=-1).unit())

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
}
