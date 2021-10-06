package east.rlbot.training

import east.rlbot.BaseBot
import east.rlbot.OutputController
import east.rlbot.experimental.NumericArcLineArcStrikeMaster

class ArcLineArcTest : Training {

    var initalized = false
    lateinit var nalads: NumericArcLineArcStrikeMaster

    override fun exec(bot: BaseBot): OutputController? {
        if (!initalized) {
            initalized = true
            nalads = NumericArcLineArcStrikeMaster(bot.data.enemies[0], bot.data)

        } else {
            nalads.adjust(bot.data)
        }

        nalads.draw(bot.draw)

        return OutputController()
    }
}