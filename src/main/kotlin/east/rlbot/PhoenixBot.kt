package east.rlbot

import rlbot.ControllerState
import rlbot.flat.GameTickPacket

class PhoenixBot(index: Int) : BaseBot(index) {
    override fun getOutput(request: GameTickPacket): OutputController {
        return OutputController().withBoost().withSteer(0.5).withThrottle(-1.0)
    }
}