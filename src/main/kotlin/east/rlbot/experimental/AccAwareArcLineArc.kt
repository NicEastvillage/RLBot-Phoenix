package east.rlbot.experimental

import east.rlbot.data.Car
import east.rlbot.math.Vec3
import east.rlbot.util.DebugDraw

data class AccAwareArcLineArc(
    val start1: Vec3,
    val start1Dir: Vec3,
    val end2: Vec3,
    val end2Dir: Vec3,
    val start2: Vec3, // Point where second turn should start
    val start2Dir: Vec3,
    val end1: Vec3,
    val sign1: Float,
    val sign2: Float,
    val radius2: Float,
    val angle1: Float,
    //val length: Float,
    val arc1Duration: Float,
    val straightDuration: Float,
    val arc2Duration: Float,

    val boostUsed: Float,
    val speedAtStart2: Float,
) {
    val end1Dir = start2Dir
    val duration = arc1Duration + straightDuration + arc2Duration

    fun draw(draw: DebugDraw) {
        draw.rect3D(end1.withZ(Car.REST_HEIGHT), 7, 7)
        draw.string3D(end1.withZ(25), "$arc1Duration")
        draw.rect3D(start2.withZ(Car.REST_HEIGHT), 7, 7)
        draw.string3D(start2.withZ(25), "${arc1Duration + straightDuration}")
        draw.string3D(end2.withZ(25), "$duration")

        draw.polyline(listOf(
            start1.withZ(Car.REST_HEIGHT),
            end1.withZ(Car.REST_HEIGHT),
            start2.withZ(Car.REST_HEIGHT),
            end2.withZ(Car.REST_HEIGHT),
        ))
    }
}

