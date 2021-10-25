package east.rlbot.experimental

import east.rlbot.math.Vec3

object AAALAFactory {

    var last: AdjustableAAALA? = null

    fun isSimilarToLast(
        start: Vec3,
        startDir: Vec3,
        end: Vec3,
        endDir: Vec3,
        aaala: AdjustableAAALA,
    ) : Boolean {
        if (startDir dot aaala.startDir < 0.1f) return false
        if (endDir dot aaala.endDir < 0.1f) return false
        if (start.distSqr(aaala.start) > 500*500) return false
        if (end.distSqr(aaala.end) > 500*500) return false
        return true
    }

    /**
     * Creates a new AdjustableAAALA. Assumes that the last AdjustableAAALA created by this factory is no longer
     * in use and may use that as a starting point.
     */
    fun create(
        start: Vec3,
        startDir: Vec3,
        startSpeed: Float,
        boostTotal: Float,
        end: Vec3,
        endDir: Vec3,
    ) : AdjustableAAALA {

        val vLast = last
        if (vLast != null && isSimilarToLast(start, startDir, end, endDir, vLast)) {
            vLast.adjust(
                start,
                startDir,
                startSpeed,
                boostTotal,
                end,
                endDir,
                iterations = 5,
            )
            return vLast
        }

        return AdjustableAAALA(
            start,
            startDir,
            startSpeed,
            boostTotal,
            end,
            endDir,
        ).also { last = it }
    }
}