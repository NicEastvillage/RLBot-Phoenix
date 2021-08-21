package east.rlbot.maneuver

import east.rlbot.OutputController
import east.rlbot.data.DataPack
import east.rlbot.util.DebugDraw

open class SteppedManeuver(val sequence: List<Maneuver>) : Maneuver {

    constructor(vararg steps: Maneuver) : this(steps.toList())

    private var currentIndex = 0

    override val done: Boolean get() = currentIndex >= sequence.size

    override fun exec(data: DataPack): OutputController? {
        while (!done) {
            val current = sequence[currentIndex]
            if (!current.done) {
                return current.exec(data)
            } else {
                currentIndex++
            }
        }
        return null
    }
}