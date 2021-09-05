package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.OutputController

interface Training {
    fun exec(bot: BaseBot): OutputController?
}