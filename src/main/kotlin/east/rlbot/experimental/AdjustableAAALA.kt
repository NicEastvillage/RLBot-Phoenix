package east.rlbot.experimental

import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.math.Vec3
import east.rlbot.simulation.turnRadius
import east.rlbot.util.DebugDraw
import east.rlbot.util.half
import java.awt.Color

class AdjustableAAALA(
    start: Vec3,
    startDir: Vec3,
    startSpeed: Float,
    boostTotal: Float,
    end: Vec3,
    endDir: Vec3,
    iterations: Int=35,
) {

    private val variants: List<AAALAVariant>

    init {

        val initRadius2 = turnRadius(1000f)

        // sign1, sign2, doBoost
        val setups = listOf(
            Triple(1f, 1f, true),
            Triple(1f, 1f, false),
            Triple(-1f, 1f, true),
            Triple(-1f, 1f, false),
            Triple(1f, -1f, true),
            Triple(1f, -1f, false),
            Triple(-1f, -1f, true),
            Triple(-1f, -1f, false),
        )

        variants = setups.map { (sign1, sign2, doBoost) ->
            AAALAVariant(
                sign1,
                sign2,
                doBoost,
                start,
                startDir,
                startSpeed,
                boostTotal,
                end,
                endDir,
                iterations,
            )
        }
    }

    fun draw(data: DataPack, draw: DebugDraw) {
        val colors = listOf(
            Color.WHITE,
            Color.GREEN.half(),
            Color.CYAN.half(),
            Color.RED.half(),
            Color.MAGENTA.half(),
            Color.BLUE.half(),
            Color.YELLOW.half(),
            Color.PINK.half()
        )
        for ((i, path) in variants.filter { !it.bad }.map { it.aaala }.sortedBy { it.duration }.withIndex().reversed()) {
            //draw.circle(end2.withZ(Car.REST_HEIGHT) + ballOri.right() * path.radius2 * path.sign2, Vec3.UP, path.radius2, color = Color.GRAY)

            draw.color = colors[i]
            draw.rect3D(path.end1.withZ(Car.REST_HEIGHT), 7, 7)
            draw.rect3D(path.start2.withZ(Car.REST_HEIGHT), 7, 7)
            draw.polyline(
                listOf(
                    path.start1.withZ(Car.REST_HEIGHT),
                    path.end1.withZ(Car.REST_HEIGHT),
                    path.start2.withZ(Car.REST_HEIGHT),
                    path.end2.withZ(Car.REST_HEIGHT),
                )
            )
            if (colors[i] == Color.WHITE) {
                //draw.string3D(path.end1.withZ(25), "${path.arc1Duration}")
                //draw.string3D(path.start2.withZ(25), "${path.arc1Duration + path.straightDuration}")
                draw.string3D(path.end2.withZ(25), "${data.match.time + path.duration}")
            }
        }
    }

    fun getBest() = variants.filter { !it.bad }.minByOrNull { it.aaala.duration }

    fun adjust(
        start: Vec3,
        startDir: Vec3,
        startSpeed: Float,
        boostTotal: Float,
        end: Vec3,
        endDir: Vec3,
        iterations: Int = 3,
    ) {
        for (variant in variants) {
            variant.adjust(
                start,
                startDir,
                startSpeed,
                boostTotal,
                end,
                endDir,
                iterations,
            )
        }
    }
}