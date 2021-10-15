package east.rlbot.experimental

import east.rlbot.data.Car
import east.rlbot.data.DataPack
import east.rlbot.experimental.NumericArcLineArcStrike
import east.rlbot.util.DebugDraw

/**
 * Considers multiple LineArcLineStrikes at the same time and picks the best one
 */
class NumericArcLineArcStrikeMaster(val car: Car, data: DataPack) {

    private val variants = listOf(
        NumericArcLineArcStrike(car, data, 1f),
        NumericArcLineArcStrike(car, data, -1f),
    )

    fun adjust(data: DataPack, iterations: Int = 10) {
        variants.forEach { it.adjust(data, iterations) }
    }

    fun draw(draw: DebugDraw) {
        val best = variants.minByOrNull { it.err }!!
        best.draw(draw)
    }
}