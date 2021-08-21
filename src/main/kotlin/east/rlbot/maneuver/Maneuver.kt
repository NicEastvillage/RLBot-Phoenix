package east.rlbot.maneuver

import east.rlbot.OutputController
import east.rlbot.data.DataPack
import east.rlbot.util.DebugDraw

interface Maneuver {
    val done: Boolean
    fun exec(data: DataPack): OutputController?
}