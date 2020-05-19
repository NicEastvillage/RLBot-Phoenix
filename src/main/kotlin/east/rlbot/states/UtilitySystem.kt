package east.rlbot.states

import east.rlbot.OutputController
import east.rlbot.data.DataPack

interface UtilityState {
    fun utility(data: DataPack): Float
    fun bias(data: DataPack): Float = 0.1f
    fun exec(data: DataPack): OutputController
    fun begin(data: DataPack) {}
    fun end(data: DataPack) {}
}

class UtilitySystem(private val states: List<UtilityState>) {

    private var prevBest: UtilityState? = null

    fun eval(data: DataPack): UtilityState {

        val best = states.maxBy { if (it == prevBest) it.utility(data) + it.bias(data) else it.utility(data) }!!

        if (best != prevBest) {
            prevBest?.end(data)
            best.begin(data)
        }

        prevBest = best

        return best
    }
}