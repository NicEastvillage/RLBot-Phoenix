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
    val duration: Float,
    val boostUsed: Float,
    val speedAtStart2: Float,
) {
    val end1Dir = start2Dir

    fun draw(draw: DebugDraw) {
        draw.rect3D(end1.withZ(Car.REST_HEIGHT), 7, 7)
        draw.rect3D(start2.withZ(Car.REST_HEIGHT), 7, 7)
        draw.polyline(listOf(
            start1.withZ(Car.REST_HEIGHT),
            end1.withZ(Car.REST_HEIGHT),
            start2.withZ(Car.REST_HEIGHT),
            end2.withZ(Car.REST_HEIGHT),
        ))
    }
}

