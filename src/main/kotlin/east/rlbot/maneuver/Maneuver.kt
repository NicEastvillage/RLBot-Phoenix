package east.rlbot.maneuver

import east.rlbot.OutputController
import east.rlbot.data.DataPack

interface Maneuver {
    val done: Boolean
    fun exec(data: DataPack): OutputController?
}