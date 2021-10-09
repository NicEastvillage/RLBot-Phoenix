package east.rlbot.training

import east.rlbot.math.Vec3
import east.rlbot.math.lerp

class FabrikPath(
    var start: Vec3,
    var startDir: Vec3,
    var end: Vec3,
    var endDir: Vec3,
) {

    val pointCount = 20
    var segmentLength: Float
    val points: MutableList<Vec3>
    val force = 0.95f

    init {
        val dir = start.dirTo(end)
        segmentLength = start.dist(end) / (pointCount + 1)
        points = MutableList(20) { i -> start + dir * segmentLength * (i + 1) }
    }

    fun adjust(iterations: Int=10) {
        for (i in 0 until iterations) {
            backwardPass()
            forwardPass()

            segmentLength = lerp(segmentLength, start.dist(end) / (pointCount + 15), 0.8f)
        }
    }

    private fun forwardPass() {
        var prev = start
        var dir = startDir
        for (i in points.indices) {
            val desiredPos = prev + dir * segmentLength
            points[i] = points[i].lerp(desiredPos, force)
            dir = prev.dirTo(points[i])
            prev = points[i]
        }
    }

    private fun backwardPass() {
        var prev = end
        var dir = endDir * -1f
        for (i in points.indices.reversed()) {
            val desiredPos = prev + dir * segmentLength
            points[i] = points[i].lerp(desiredPos, force)
            dir = prev.dirTo(points[i])
            prev = points[i]
        }
    }
}